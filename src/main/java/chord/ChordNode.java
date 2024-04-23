package chord;

import com.google.common.annotations.VisibleForTesting;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.Chord;
import proto.ChordServiceGrpc;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static chord.Util.*;

public class ChordNode {

    private static final Logger logger = LoggerFactory.getLogger(ChordNode.class);

    private final ReentrantLock lock = new ReentrantLock();

    private NodeReference predecessor;

    private final NodeReference node;

    private final ArrayList<Finger> fingerTable = new ArrayList<>();

    private final NavigableMap<BigInteger, String> localData = new TreeMap<>();

    private final Timer stabilizationTimer = new Timer();

    private TimerTask stabilizationTimerTask;

    @VisibleForTesting
    public static int m = 4; // [0-2^m) ids

    public static int STABILIZATION_INTERVAL = 2000;

    private final Server server;

    private ChordServiceGrpc.ChordServiceBlockingStub blockingStub;


    public ChordNode(String ip, int port) {
        this.node = new NodeReference(ip, port);
        this.predecessor = node;

        for (int i = 1; i <= m; i++) {
            BigInteger mod = BigInteger.valueOf(2L).pow(m);
            BigInteger start = node.id.add(BigInteger.valueOf(2L).pow(i-1)).mod(mod);
            BigInteger end = node.id.add(BigInteger.valueOf(2L).pow(i)).mod(mod);
            fingerTable.add(new Finger(start, end, node));
        }

        server = ServerBuilder.forPort(port)
                .addService(new ChordNodeServer())
                .build();
    }


    public void stopServer() {
        if (server != null) {
            server.shutdownNow();
            logger.warn("Server stopped, listening on {}", node.port);
        }
    }

    @VisibleForTesting
    public void simulateFail() {
        stopServer();
        stopFixThread();
    }

    public void shutdownChordNode()  {
        stopFixThread();
        stopServer();
    }


    /**
     * To be used in bash run or main method run to keep the network running
     * <p><b>Code example:</b>
     * <pre>
     * {@code
     *         ChordNode bootstrap = new ChordNode("localhost", 9100);
     *         bootstrap.createRing();
     *         for (int i = 9101; i < 9110 ; i++) {
     *             ChordNode n = new ChordNode("localhost", i);
     *             n.join(bootstrap);
     *         }
     *         bootstrap.blockUntilShutdown();
     * }
     * </pre>
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public int getDataSize() {
        return localData.size();
    }

    /**
     * Unified method update to Predecessor. GRPC call invoke new threads on the server side. <br>
     * It is therefore important to lock the finger table before updating it.
     */
    private void syncUpdatePredecessor(NodeReference newPredecessor) {
        lock.lock();
        try {
            predecessor = newPredecessor;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unified method to update finger table. GRPC call invoke new threads on the server side. <br>
     * It is therefore important to lock the finger table before updating it.
     */
    private void syncUpdateFingerTable(int index, NodeReference newFinger) {
        lock.lock();
        try {
            fingerTable.get(index).setNode(newFinger);
        } finally {
            lock.unlock();
        }
    }

    /**
     * distributedHashTable.put()
     */
    public void put(String key, String value) {
        BigInteger id = calculateSHA1(key);
        if(inRange_OpenClose(id, predecessor.id, node.id)) {
            localData.put(id, value);
            logger.info("<{},{}> saved on Node {}", id, value, node.id);
        } else {
            NodeReference n_ = findSuccessor(id);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
            blockingStub = ChordServiceGrpc.newBlockingStub(channel);
            Chord.PutRequest request = Chord.PutRequest.newBuilder()
                    .setKey(key)
                    .setId(calculateSHA1(key).toString())
                    .setValue(value)
                    .build();
            blockingStub.put(request);
            channel.shutdown();
        }
    }

    /**
     * distributedHashTable.get()
     */
    public String get(String key) {
        BigInteger id = calculateSHA1(key);
        if(inRange_OpenClose(id, predecessor.id, node.id)) {
            String value = localData.get(id);
            logger.info("Node {} returning <{},{}>", node.id, id, value);
            return value;
        } else {
            NodeReference n_ = findSuccessor(id);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
            blockingStub = ChordServiceGrpc.newBlockingStub(channel);
            Chord.GetRequest request = Chord.GetRequest.newBuilder()
                    .setId(id.toString())
                    .build();
            Chord.GetResponse response = blockingStub.get(request);
            channel.shutdown();
            if (response.getValue().isEmpty()) {
                return null;
            }
            return response.getValue();
        }
    }

    /**
     * distributedHashTable.delete()
     */
    public void delete(String key) {
        BigInteger id = calculateSHA1(key);
        NodeReference n_ = findSuccessor(id);
        if (inRange_OpenClose(id, predecessor.id, node.id)) {
            logger.info("Node {} removed <{},{}>", node.id, id, localData.get(id));
            localData.remove(id);
        } else {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
            blockingStub = ChordServiceGrpc.newBlockingStub(channel);
            Chord.DeleteRequest request = Chord.DeleteRequest.newBuilder()
                    .setId(id.toString())
                    .build();
            blockingStub.delete(request);
            channel.shutdown();
        }
    }

    /**
     * Create a new Chord ring
     */
    public void createRing() throws IOException {
        server.start();
        logger.warn("Server started, listening on {}", node.port);
        startFixThread();
        logger.debug("Node [{}] created a new ChordRing with id range [0, {})", node, BigInteger.valueOf(2).pow(m));
    }

    /**
     * Join an existing Chord ring
     */
    public void join(ChordNode n_) throws IOException {
        server.start();
        logger.warn("Server started, listening on {}", node.port);
        initFingerTable(n_.node);
        updateOthers();
        moveKeys_RPC(); //from the range (predecessor,n] from successor
        startFixThread();
        logger.debug("Node [{}] joined the network", node);
        printStatus();
    }

    /**
     * Set S and P of immediate neighbors to your address
     */
    private void updateNeighborsJoining_RPC() {
        NodeReference successor = fingerTable.get(0).node;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.getAddress()).usePlaintext().build();

        // node.S.P = node
        Chord.UpdateSuccessorRequest.Builder successorRequest = Chord.UpdateSuccessorRequest.newBuilder()
                .setNewIp(node.ip)
                .setNewPort(node.port);
        ChordServiceGrpc.newBlockingStub(channel).updateSuccessor(successorRequest.build());
        channel.shutdown();

        NodeReference predecessor = this.predecessor;
        channel = ManagedChannelBuilder.forTarget(predecessor.getAddress()).usePlaintext().build();

        // node.P.S = node
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.UpdatePredecessorRequest.Builder predecessorRequest = Chord.UpdatePredecessorRequest.newBuilder()
                .setNewIp(node.ip)
                .setNewPort(node.port);
        blockingStub.updatePredecessor(predecessorRequest.build());
        channel.shutdown();
    }

    /**
     * Upon leaving, node should be deleted from other nodes that store its reference
     * This means setting this.node.Predecessor.Successor, this.node.Successor.Predecessor
     * and finally any fingertable that this.node might be included in
     */
    public void leave() {
        stopFixThread();
        // no need to delete n from FingerTables since periodic fix_fingers will do it
        updateNeighborsWhenLeaving_RPC();
        if(!localData.isEmpty())
            moveKeysToSuccessor_RPC();

        logger.debug("Node [{}] left the network", node);
    }

    /**
     * Update S and P of your neighbors when leaving
     */
    private void updateNeighborsWhenLeaving_RPC() {
        // update S.P = this.node.P
        NodeReference successor = fingerTable.get(0).node;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.UpdateSuccessorRequest usr = Chord.UpdateSuccessorRequest.newBuilder()
                .setNewIp(this.predecessor.ip)
                .setNewPort(this.predecessor.port)
                .build();
        blockingStub.updateSuccessor(usr);
        channel.shutdown();

        // update P.S = this.node.S
        NodeReference predecessor = this.predecessor;
        channel = ManagedChannelBuilder.forTarget(predecessor.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.UpdatePredecessorRequest upr = Chord.UpdatePredecessorRequest.newBuilder()
                .setNewIp(successor.ip)
                .setNewPort(successor.port)
                .build();
        blockingStub.updatePredecessor(upr);
        channel.shutdown();
    }

    // all nodes from n are moved to n.successor
    private void moveKeysToSuccessor_RPC() {
        NodeReference successor = fingerTable.get(0).node;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.MoveKeysToSuccessorRequest.Builder request = Chord.MoveKeysToSuccessorRequest.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port);
        localData.forEach((k, v) -> {
            request.addKey(k.toString());
            request.addValue(v);
        });
        blockingStub.moveKeysToSuccessor(request.build());
        channel.shutdown();
    }

    private void printStatus() {
        logger.trace("[{}] P={}, S={}", node, predecessor, fingerTable.get(0).node);
        logger.trace("[{}]  ==== FT ====", node);
        for (int i = 0; i < fingerTable.size(); i++) {
            Finger finger = fingerTable.get(i);
            logger.trace("Index: {}, [{},{}), Succ: [{}]", i, finger.start, finger.end, finger.node);
        }
        logger.trace("=====================");
    }

    private void startFixThread() {
        logger.trace("[{}]  started FIX", node);
        // periodic stabilization
        stabilizationTimerTask = new TimerTask() {
            @Override
            public void run() {
                printStatus();
                stabilize();
                fix_fingers();
                printStatus();
            }
        };
        stabilizationTimer.schedule(stabilizationTimerTask,STABILIZATION_INTERVAL, STABILIZATION_INTERVAL);
    }

    private void stopFixThread() {
        if (stabilizationTimer != null) {
            stabilizationTimer.cancel();
            stabilizationTimer.purge();
        }
        if (stabilizationTimerTask != null) {
            stabilizationTimerTask.cancel();
        }
    }

    private void fix_fingers() {
        int i = new Random().nextInt(m-1)+1;
        syncUpdateFingerTable(i, findSuccessor(fingerTable.get(i).start));
    }

    private void stabilize() {
        // ask S for S.P, decide whether to set n.P = S.P instead
        NodeReference S = fingerTable.get(0).node;
        NodeReference s_p;

        try {
            s_p = getSuccessorsPredecessor_RPC(S);
        }
        catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                logger.error("[{}]  My Successor ({}) is offline. Will delete it and try to find new Successor to restore ChordRing.", node, S);
                deleteFromFingerTable(S);
                if (S.equals(predecessor)) {
                    // all nodes left or failed, this.node is last node online
                    syncUpdatePredecessor(this.node);
                    for (int i = 0; i < m; i++) {
                        syncUpdateFingerTable(0, this.node);
                    }
                    return;
                }
                // else walk around ring backwards until you find node which predecessor is offline
                NodeReference newSuccessor = findOfflinePredecessor_RPC(predecessor, S);
                logger.trace("[{}]  newSuccessor: {}", node, newSuccessor);
                syncUpdateFingerTable(0, newSuccessor);
                logger.trace("[{}]  checking S: {}", node, fingerTable.get(0).node);
                return;
            }
            return;
        }

        logger.debug("[{}]  s_p {} ?∈  ({}, {})", node, s_p, node.id, S.id);
        if (inRange_OpenOpen(s_p.id, node.id, S.id)) {
            syncUpdateFingerTable(0, s_p);
        }
        // notify n.S of n's existence
        logger.debug("[{}] existence notified to [{}:{}]", node, S, S.id);
        ManagedChannel channel = ManagedChannelBuilder.forTarget(S.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.Notification notification = Chord.Notification.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port)
                .setId(node.id.toString())
                .build();
        blockingStub.notify(notification);
        channel.shutdown();
    }

    /**
     * Called with each step of {@link ChordNode#findOfflinePredecessor_RPC(NodeReference, NodeReference)} <br>
     * Replace current node with next succeeding node or this.node
     */
    private void deleteFromFingerTable(NodeReference offlineNode) {
        // start from 1 since 0, the successor will be updated on findOfflinePredecessor_RPC() return
        NodeReference successorOfOffline = null;
        for (int i = 1; i < m; i++) {
            if (fingerTable.get(i).node.equals(offlineNode)) {
                syncUpdateFingerTable(i, this.node);
            }
        }
    }

    /**
     * Sends a request to the predecessor of this node, checking if its predecessor is offline.
     * <ul>
     *   <li>If the predecessor is offline, the predecessor is updated to the node that initiated the call, and the successor of the initiating node is set to this node.</li>
     *   <li>If the predecessor is online, the message is propagated forward.</li>
     * </ul>
     * Nodes encountered during this process should update their finger tables to prevent an inconsistent network state where the failed node is still preserved.
     */
    private NodeReference findOfflinePredecessor_RPC(NodeReference predecessor, NodeReference s) {
        // Nodes along the way should make updates to their fingerTable to prevent
        // inconsistent netowrk state in which the failed node is still preserved
        Chord.FindOfflinePredecessorRequest request = Chord.FindOfflinePredecessorRequest.newBuilder()
                .setInitialNodeIp(node.ip)
                .setInitialNodePort(node.port)
                .setOfflineNodeIp(s.ip)
                .setOfflineNodePort(s.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(predecessor.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        logger.trace("[{}]  asking [{}] for offlinePredecessor of [{}]", node, predecessor, s);
        Chord.FindOfflinePredecessorResponse response = blockingStub.findOfflinePredecessor(request);
        channel.shutdown();

        return new NodeReference(response.getNewInitialNodeSuccessorIp(), response.getNewInitialNodeSuccessorPort());
    }


    public NodeReference getSuccessorsPredecessor_RPC(NodeReference successor) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.getAddress()).usePlaintext().build();
        Chord.GetPredecessorRequest.Builder request = Chord.GetPredecessorRequest.newBuilder()
                .setRequestorIp(this.node.ip)
                .setRequestorPort(this.node.port);

        // when Successor node fails involuntarily, this is the first place on which it will be visible
        // current node will try to contact other nodes to try and restore the ring

        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.GetPredecessorResponse response;
        try {
            response = blockingStub.getPredecessor(request.build());
            channel.shutdown();
            return new NodeReference(response.getPredecessorIp(), response.getPredecessorPort());
        }
        catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE)
                throw e;
        }
        return null;
    }

    private void moveKeys_RPC() {
        NodeReference successor = fingerTable.get(0).node;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.MoveKeysRequest request = Chord.MoveKeysRequest.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port)
                .setRangeStart(predecessor.id.toString())
                .setRangeEnd(node.id.toString())
                .build();
        Chord.MoveKeysResponse response = blockingStub.moveKeys(request);
        int cnt = response.getValueCount();
        for (int i = 0; i < cnt; i++) {
            String value = response.getValue(i);
            BigInteger key = new BigInteger(response.getKey(i));
            localData.put(key, value);
        }
        channel.shutdown();
    }

    /**
     * Update all nodes whose finger table should refer to this node
     */
    private void updateOthers() {
        // update ith finger of p node
        for (int i = 0; i < m; i++) {
            try {
                Thread.sleep(75);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            BigInteger id;
            if (i == 0) {
                id = node.id.subtract(BigInteger.valueOf(0));
            } else {
                id = node.id.subtract(BigInteger.valueOf(2).pow(i-1));
                if (id.compareTo(BigInteger.ZERO) < 0) { // wraparound
                    id = id.add(BigInteger.valueOf(2).pow(m));
                }
            }
            NodeReference p = findPredecessor(id);
            logger.trace("[{}]  pred of {} is {}, i={}", node, p, calculateSHA1(p.getAddress()), i);
            updateFingerTableOf_RPC(p, node, i);
        }
    }

    private void updateFingerTableOf_RPC(NodeReference predecessor, NodeReference sender, int index) {
        Chord.UpdateFingerTableRequest req = Chord.UpdateFingerTableRequest.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port)
                .setIndex(index)
                .setNodeIp(sender.ip)
                .setNodePort(sender.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(predecessor.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        blockingStub.updateFingerTable(req);
        channel.shutdown();
    }


    /**
     * Joining node initializes its own FingerTable using the bootstrap node n_
     */
    private void initFingerTable(NodeReference n_) {
        BigInteger targetId = fingerTable.get(0).start;
        NodeReference S = findSuccessor_RPC(n_, targetId);
        syncUpdateFingerTable(0, S);

        syncUpdatePredecessor(getPredecessor_RPC(S));

        logger.debug("Node [{}] initialized finger table. P={}, S={}", node, fingerTable.get(0).node, predecessor);
        updateNeighborsJoining_RPC();

        for (int i = 0; i < m-1; i++) {
            Finger curr = fingerTable.get(i);
            Finger next = fingerTable.get(i+1);
            if (inRange_CloseOpen(next.start, node.id, curr.node.id)) {
                fingerTable.get(i+1).setNode(curr.node);
            } else {
                NodeReference n_s = findSuccessor_RPC(n_, next.start);
                fingerTable.get(i+1).setNode(n_s);
            }
        }
    }

    public NodeReference getPredecessor_RPC(NodeReference targetNode) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(targetNode.getAddress()).usePlaintext().build();
        Chord.GetPredecessorRequest request = Chord.GetPredecessorRequest.newBuilder()
                .setRequestorIp(this.node.ip)
                .setRequestorPort(this.node.port)
                .build();

        // when node fails involuntarily, this is the first place on which it will be visible
        // current node will try to contact other nodes to try and restore the ring

        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.GetPredecessorResponse response;
        try {
            response = blockingStub.getPredecessor(request);
            channel.shutdown();
            return new NodeReference(response.getPredecessorIp(), response.getPredecessorPort());

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE)
                logger.error("[{}]  My Successor ({}) seems to be offline. {} ", node, targetNode);
            return targetNode;
        }

    }

    public NodeReference findSuccessor_RPC(NodeReference n_, BigInteger targetId) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
        Chord.FindSuccessorRequest request = Chord.FindSuccessorRequest.newBuilder()
                .setSenderIp(this.node.ip)
                .setSenderPort(this.node.port)
                .setTargetId(targetId.toString())
                .build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);

        Chord.FindSuccessorResponse response = blockingStub.findSuccessor(request);
        channel.shutdown();
        return new NodeReference(response.getSuccessorIp(), response.getSuccessorPort());
    }

    /**
     * Successor of id is responsible for id (stores it locally) <br>
     * It is found by moving forward around the ChordRing toward most immediately preceding node of id using {@link ChordNode#findPredecessor(BigInteger) findPredecessor} <br>
     * Once the most immediate predecessor of id is found, we just return this node successor, therefore finding id's sucessor <br>
     */
    public NodeReference findSuccessor(BigInteger id) {
        NodeReference n_ = findPredecessor(id);
        return getSuccessor_RPC(n_);
    }

    /**
     * Prompt contact node to return it's successor
     */
    public NodeReference getSuccessor_RPC(NodeReference n_) {
        if (n_.equals(node)) {
            return fingerTable.get(0).node;
        }
        Chord.GetSuccessorRequest req = Chord.GetSuccessorRequest
                .newBuilder()
                .setRequestorIp(this.node.ip)
                .setRequestorPort(this.node.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.GetSuccessorResponse response = blockingStub.getSuccessor(req);
        channel.shutdown();
        return new NodeReference(response.getSuccessorIp(), response.getSuccessorPort());
    }

    /**
     * Contacts series of nodes moving around the ChordRing towards id
     */
    public NodeReference findPredecessor(BigInteger id) {
        // contacts series of nodes moving forward around the Chord towards id
        if (id.equals(node.id)) {
            return this.predecessor;
        }
        NodeReference n_ = this.node;
        NodeReference S = getSuccessor_RPC(n_);
        if (n_.equals(S)) { // when there is only bootstrap in the network
            return n_;
        }
        while (!inRange_OpenClose(id, n_.id, S.id)) {
            logger.trace("[{}] findPredecessor: {} !∈ [{}, {})", node, id, n_.id, S.id);
            n_ = closestPrecedingFingerOf(n_, id);
            S = getSuccessor_RPC(n_);
        }
        logger.trace("[{}] findPredecessor: {} ∈ [{}, {})", node, id, n_.id, S.id);
        return n_;
    }

    /**
     * Either find closest preceding finger of id from this node's finger table
     * or prompt contact node to return it's closest preceding finger of id
     */
    public NodeReference closestPrecedingFingerOf(NodeReference n_, BigInteger id) {
        // send request to node n_ with id
        if (n_.equals(node)) {
            for (int i = m-1; i >= 0; i--) {
                Finger curr = fingerTable.get(i);
                if (inRange_OpenOpen(curr.node.id, node.id, id)) {
                    return curr.node;
                }
            }
            return this.node;
        } else {
            return closestPrecedingFinger_RPC(n_, id);
        }
    }

    /**
     * Prompt contact node to return closest preceding finger of id from it's finger table
     */
    private NodeReference closestPrecedingFinger_RPC(NodeReference n_, BigInteger id) {
        Chord.ClosestPrecedingFingerRequest cpfr = Chord.ClosestPrecedingFingerRequest
                .newBuilder()
                .setTargetId(id.toString())
                .setSenderIp(this.node.ip)
                .setSenderPort(this.node.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
        logger.trace("[{}] asking node [{}] for closestPrecedingFinger of id={}", node, n_, id);
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.ClosestPrecedingFingerResponse response = blockingStub.closestPrecedingFinger(cpfr);
        channel.shutdown();
        NodeReference cpf = new NodeReference(response.getClosestPrecedingFingerIp(), response.getClosestPrecedingFingerPort());
        logger.trace("[{}] got {}", node, cpf);
        return cpf;
    }

    /**
     * Just a wrapper to avoid retyping annoying reentrant lock block
     */
    private void lockWrapper(Runnable action) {
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }




    /** Procedures served to other nodes */
    private class ChordNodeServer extends ChordServiceGrpc.ChordServiceImplBase {

        /**
         * Designed as blocking call chain to prevent further stabilizing calls from initial node
         */
        @Override
        public void findOfflinePredecessor(Chord.FindOfflinePredecessorRequest request, StreamObserver<Chord.FindOfflinePredecessorResponse> responseObserver) {

            NodeReference offlineNode = new NodeReference(request.getOfflineNodeIp(), request.getOfflineNodePort());
            deleteFromFingerTable(offlineNode);

            // if this.node.P is the offline node, then we found the node that broke the ring
            // Ring will be repaired by updating neighbours of the offline node
            if (predecessor.equals(offlineNode)) {
                // this.node.P = initialNode
                NodeReference initialNode = new NodeReference(request.getInitialNodeIp(), request.getInitialNodePort());
                syncUpdatePredecessor(initialNode);

                // initialNode.S = this.node
                Chord.FindOfflinePredecessorResponse.Builder resp = Chord.FindOfflinePredecessorResponse.newBuilder()
                        .setInitialNodeIp(initialNode.ip)
                        .setInitialNodePort(initialNode.port)
                        .setNewInitialNodeSuccessorIp(node.ip)
                        .setNewInitialNodeSuccessorPort(node.port);

                logger.trace("[{}]  found offlinePredecessor. P={} -> {}, sending back myself as new init.S", node, offlineNode, initialNode);
                responseObserver.onNext(resp.build());
                responseObserver.onCompleted();
            }
            else {
                // propagate the same message forward
                ManagedChannel channel = ManagedChannelBuilder.forTarget(predecessor.getAddress()).usePlaintext().build();
                blockingStub = ChordServiceGrpc.newBlockingStub(channel);
                Chord.FindOfflinePredecessorResponse response = blockingStub.findOfflinePredecessor(request);
                channel.shutdown();

                logger.trace("[{}]  forwarding offlinePredecessor to [{}]", node, predecessor);
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
         }

        /**
         * Requestor wants this.node to find successor of targetId
         */
        @Override
        public void findSuccessor(Chord.FindSuccessorRequest request, StreamObserver<Chord.FindSuccessorResponse> responseObserver) {
            BigInteger targetId = new BigInteger(request.getTargetId());
            NodeReference n = ChordNode.this.findSuccessor(targetId);

            Chord.FindSuccessorResponse r = Chord.FindSuccessorResponse.newBuilder()
                    .setSuccessorIp(n.ip)
                    .setSuccessorPort(n.port)
                    .build();
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        }

        /**
         * return this.successor
         */
        @Override
        public void getSuccessor(Chord.GetSuccessorRequest request, StreamObserver<Chord.GetSuccessorResponse> responseObserver) {
            NodeReference successor = fingerTable.get(0).node;
            Chord.GetSuccessorResponse response = Chord.GetSuccessorResponse.newBuilder()
                    .setSuccessorIp(successor.ip)
                    .setSuccessorPort(successor.port)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        /**
         * return this.predecessor
         */
        @Override
        public void getPredecessor(Chord.GetPredecessorRequest request, StreamObserver<Chord.GetPredecessorResponse> responseObserver) {
            Chord.GetPredecessorResponse response = Chord.GetPredecessorResponse.newBuilder()
                    .setPredecessorIp(predecessor.ip)
                    .setPredecessorPort(predecessor.port)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        /**
         * this.node prompted to insert sending node into its finger table
         */
        @Override
        public void updateFingerTable(Chord.UpdateFingerTableRequest request, StreamObserver<Chord.UpdateFingerTableResponse> responseObserver) {
            int index = request.getIndex();
            NodeReference sender = new NodeReference(request.getNodeIp(), request.getNodePort());
            NodeReference i = fingerTable.get(index).node;
            if (inRange_CloseOpen(sender.id, node.id, i.id)) {
                syncUpdateFingerTable(index, sender);
                updateFingerTableOf_RPC(predecessor, sender, index);
            }
            Chord.UpdateFingerTableResponse response = Chord.UpdateFingerTableResponse.newBuilder()
                    .setStatus("OK")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        /**
         * Requestor prompted this.node to return closest preceding finger of id (most immediate predecessor from it's point of view)
         */
        @Override
        public void closestPrecedingFinger(Chord.ClosestPrecedingFingerRequest request, StreamObserver<Chord.ClosestPrecedingFingerResponse> responseObserver) {
            BigInteger id = new BigInteger(request.getTargetId());
            for (int i = m-1; i >= 0; i--) {
                NodeReference curr = fingerTable.get(i).node;
                logger.trace("[{}]   {} ∈ ({}, {})", node, curr.id, node.id, id);
                if (inRange_OpenOpen(curr.id, node.id, id)) {
                    Chord.ClosestPrecedingFingerResponse r = Chord.ClosestPrecedingFingerResponse.newBuilder()
                            .setClosestPrecedingFingerIp(curr.ip)
                            .setClosestPrecedingFingerPort(curr.port)
                            .build();
                    logger.trace("[{}] responding with cpf of [{}] ", node, curr);
                    responseObserver.onNext(r);
                    responseObserver.onCompleted();
                    return;
                }
            }
            Chord.ClosestPrecedingFingerResponse r = Chord.ClosestPrecedingFingerResponse.newBuilder()
                    .setClosestPrecedingFingerIp(node.ip)
                    .setClosestPrecedingFingerPort(node.port)
                    .build();
            logger.trace("[{}] responding with cpf of [{}] ", node, node);
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        }

        /**
         * this.node is prompted to tranfer its keys to requestor (requestor has joined)
         */
        @Override
        public void moveKeys(Chord.MoveKeysRequest request, StreamObserver<Chord.MoveKeysResponse> responseObserver) {
            Chord.MoveKeysResponse.Builder response = Chord.MoveKeysResponse.newBuilder();
            BigInteger start = new BigInteger(request.getRangeStart());
            BigInteger end = new BigInteger(request.getRangeEnd());
            // range = (start, end]

            lockWrapper(() -> {
                if (!localData.isEmpty()) {
                    ArrayList<BigInteger> toRemove = new ArrayList<>();
                    if (start.compareTo(end) < 0) {
                        localData.subMap(start.add(BigInteger.ONE), end.add(BigInteger.ONE)).forEach((k, v) -> {
                            response.addKey(k.toString());
                            response.addValue(v);
                            // TODO: ConcurrentModificationException?
                            toRemove.add(k);
                        });
                    } else {
                        localData.subMap(start.add(BigInteger.ONE), BigInteger.valueOf(2).pow(m)).forEach((k, v) -> {
                            response.addKey(k.toString());
                            response.addValue(v);
                            toRemove.add(k);
                        });
                        localData.subMap(BigInteger.ZERO, end.add(BigInteger.ONE)).forEach((k, v) -> {
                            response.addKey(k.toString());
                            response.addValue(v);
                            toRemove.add(k);
                        });
                    }
                    toRemove.forEach(localData::remove);
                }
            });

            logger.debug("{} moves {} keys to {}", node.id, response.getKeyCount(), calculateSHA1(request.getSenderIp()+":"+request.getSenderPort()));
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }

        /**
         * distributedHashTable.put()
         */
        @Override
        public void put(Chord.PutRequest request, StreamObserver<Chord.PutResponse> responseObserver) {
            localData.put(new BigInteger(request.getId()), request.getValue());
            logger.trace("<{},{}> saved on {}", request.getId(), request.getValue(), node.id);
            responseObserver.onNext(Chord.PutResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * distributedHashTable.get()
         */
        @Override
        public void get(Chord.GetRequest request, StreamObserver<Chord.GetResponse> responseObserver) {
            BigInteger id = new BigInteger(request.getId());
            String value = localData.get(id);
            logger.trace("n:{} returning <{},{}>", node.id, request.getId(), value);
            if (value == null) {
                value = "";
            }
            Chord.GetResponse response = Chord.GetResponse.newBuilder()
                    .setValue(value)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        /**
         * distributedHashTable.remove()
         */
        @Override
        public void delete(Chord.DeleteRequest request, StreamObserver<Chord.DeleteResponse> responseObserver) {
            BigInteger id = new BigInteger(request.getId());
            logger.trace("n:{} removing <{},{}>", node.id, id, localData.get(id));
            localData.remove(id);
            responseObserver.onNext(Chord.DeleteResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * this.node was notified about predecessor's existence, will set P to new predecessor
         */
        @Override
        public void notify(Chord.Notification request, StreamObserver<Chord.NotificationResponse> responseObserver) {
            logger.trace("[{}] existence of node [{}:{}:{}] was notified", node, request.getSenderIp(), request.getSenderPort(), request.getId());
            logger.trace("[{}] will set P={}, if {} E ({}, {})", node, request.getId(), request.getId(), predecessor.id, node.id);
            BigInteger id = new BigInteger(request.getId());
            if (predecessor == node || inRange_OpenOpen(id, predecessor.id, node.id)) {
                syncUpdatePredecessor(new NodeReference(request.getSenderIp(), request.getSenderPort()));
            }
            responseObserver.onNext(Chord.NotificationResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * RPC called from this.Successor
         * @param request to change Predecessor to newIp and newPort
         */
        @Override
        public void updatePredecessor(Chord.UpdatePredecessorRequest request, StreamObserver<Chord.UpdatePredecessorResponse> responseObserver) {
            NodeReference oldSucc = fingerTable.get(0).node;
            logger.trace("[{}]  will set S={} (prev_s={})", node, request.getNewPort(), oldSucc);

            // update Successor
            syncUpdateFingerTable(0, new NodeReference(request.getNewIp(), request.getNewPort()));

            responseObserver.onNext(Chord.UpdatePredecessorResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * RPC called from this.Predecessor
         * @param request to change Predecessor to newIp and newPort
         */
        @Override
        public void updateSuccessor(Chord.UpdateSuccessorRequest request, StreamObserver<Chord.UpdateSuccessorRequest> responseObserver) {
            syncUpdatePredecessor(new NodeReference(request.getNewIp(), request.getNewPort()));
            responseObserver.onNext(Chord.UpdateSuccessorRequest.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * Preceding node leaves network and transfers its keys to this.node
         * @param request
         * @param responseObserver
         */
        @Override
        public void moveKeysToSuccessor(Chord.MoveKeysToSuccessorRequest request, StreamObserver<Chord.MoveKeysToSuccessorResponse> responseObserver) {
            for (int i = 0; i < request.getKeyCount(); i++) {
                localData.put(new BigInteger(request.getKey(i)), request.getValue(i));
            }
            responseObserver.onNext(Chord.MoveKeysToSuccessorResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @VisibleForTesting
    public NodeReference getNodeReference() {
        return node;
    }

    @VisibleForTesting
    public boolean containedInFingerTable(NodeReference n) {
        for (Finger f: this.fingerTable) {
            if(n.equals(f.node))
                return true;
        }
        return false;
    }

    @VisibleForTesting
    public NodeReference getPredecessor() {
        return predecessor;
    }

    @VisibleForTesting
    public NodeReference getSuccessor() {
        return fingerTable.get(0).node;
    }

    public static void main(String[] args) throws Exception {
        ChordNode.STABILIZATION_INTERVAL = 2000;
        ChordNode.m = 4;

        ChordNode bootstrap = new ChordNode("localhost", 9000);
        bootstrap.createRing();

        ChordNode n1 = new ChordNode("localhost", 9003);
        n1.join(bootstrap);

        ChordNode n2 = new ChordNode("localhost", 9004);
        n2.join(bootstrap);

        ChordNode n3 = new ChordNode("localhost", 9005);
        n3.join(bootstrap);


        Thread.sleep(5000);

        n1.simulateFail();


        bootstrap.blockUntilShutdown();
    }
}
