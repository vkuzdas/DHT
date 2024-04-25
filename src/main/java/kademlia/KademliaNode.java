package kademlia;

import com.google.common.annotations.VisibleForTesting;
import dht.DHTNodeInterface;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.Kademlia;
import proto.KademliaServiceGrpc;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static kademlia.Util.getId;
import static kademlia.Util.randomWithinBucket;

public class KademliaNode implements DHTNodeInterface {

    private static final Logger logger = LoggerFactory.getLogger(KademliaNode.class);

    private final NodeReference self;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Local data storage
     */
    private final Map<BigInteger, String> localData = new HashMap<>();

    /**
     * Republish task handle for cancellation/rescheduling
     */
    private final Map<BigInteger, ScheduledFuture<?>> republishTasks = new HashMap<>();

    /**
     * Expiration task handle for cancellation/rescheduling
     */
    private final Map<BigInteger, ScheduledFuture<?>> expireTasks = new HashMap<>();

    /**
     * Refresh k-bucket that have not been queried in the last refreshInterval
     */
    private final Map<Integer, ScheduledFuture<?>> refreshTasks = new HashMap<>();

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);


    /**
     * Contains {@link KademliaNode#ID_LENGTH} number of {@link KBucket}s
     */
    private final RoutingTable routingTable;

    /**
     * Determines the length of the ID in bits as well as number of {@link KBucket}s a single node has
     */
    private static int ID_LENGTH = 6;

    /**
     * Number of concurrent lookups
     */
    private static int ALPHA_PARAMETER = 3;

    /**
     * Max size of {@link KBucket} and replication parameter
     */
    private static int K_PARAMETER = 4;

    private final Server server;

    /**
     * Time after which the <b>original publisher</b> must republish a key/value pair <br>
     * Note: For opt-1, original protocol assumes a certain network delay which results in non-racing intervals between the nodes.
     * Since this implementation runs mainly locally, we need to 'desynchronize' the republishing intervals
     */
    private static Duration republishInterval = Duration.ofMinutes(15);

    /**
     * Time after which a key/value pair expires; this is a time-to-live (TTL) from the original publication date
     */
    private static Duration expireInterval = Duration.ofMinutes(15).plus(Duration.ofSeconds(10));

    /**
     * Time after which node should refresh (send random id query) otherwise unqueried k-bucket
     */
    private static Duration refreshInterval = Duration.ofMinutes(10);


    ///////////////////////////////
    ///  NODE-STATE INITIATION  ///
    ///////////////////////////////

    public KademliaNode(String ip, int port) {
        this.self = new NodeReference(ip, port);
        this.routingTable = new RoutingTable(ID_LENGTH, ALPHA_PARAMETER, K_PARAMETER, self);

        server = ServerBuilder.forPort(port)
                .addService(new KademliaNodeServer())
                .build();
    }

    public NodeReference getNodeReference() {
        return self;
    }

    public static int getIdLength() {
        return ID_LENGTH;
    }

    public static void setIdLength(int idLength) {
        if (idLength < 4 || idLength > 160) {
            throw new IllegalArgumentException("ID length must be between 4 and 160 bits");
        }
        ID_LENGTH = idLength;
    }

    public static int getAlpha() {
        return ALPHA_PARAMETER;
    }

    public static void setAlpha(int alpha) {
        if (alpha < 1 || alpha > 10) {
            throw new IllegalArgumentException("Alpha must be between 1 and 10");
        }
        KademliaNode.ALPHA_PARAMETER = alpha;
    }

    public static int getK() {
        return K_PARAMETER;
    }

    public static void setK(int k) {
        if (k < 1 || k > 20) {
            throw new IllegalArgumentException("K must be between 1 and 20");
        }
        if (k > ID_LENGTH) {
            throw new IllegalArgumentException("K must be <= ID length");
        }
        KademliaNode.K_PARAMETER = k;
    }

    /**
     * There is high probability that new Kad node is inserted in the last KB
     * which means there would be no refresh. <br>
     * We need to set custom id for testing purposes.
     */
    @VisibleForTesting
    public KademliaNode(String ip, int port, BigInteger id) {
        this.self = new NodeReference(ip, port, id);
        this.routingTable = new RoutingTable(ID_LENGTH, ALPHA_PARAMETER, K_PARAMETER, self);

        server = ServerBuilder.forPort(port)
                .addService(new KademliaNodeServer())
                .build();
    }

    @VisibleForTesting
    public static void setRepublishInterval(Duration duration) {
        republishInterval = duration;
    }

    @VisibleForTesting
    public static void setExpireInterval(Duration duration) {
        expireInterval = duration;
    }

    @VisibleForTesting
    public static void setRefreshInterval(Duration duration) {
        refreshInterval = duration;
    }


    @VisibleForTesting
    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    @VisibleForTesting
    public Map<BigInteger, String> getLocalData() {
        return localData;
    }


    ////////////////////////////////
    ///  CLIENT-SIDE PROCESSING  ///
    ////////////////////////////////

    ////  Node run administration  ////

    public void initKademlia() throws IOException {
        startServer();
        startRefreshing();
    }

    @Override
    public void init() {
        try {
            initKademlia();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getIp() {
        return self.getIp();
    }

    @Override
    public int getPort() {
        return self.getPort();
    }

    private void startServer() throws IOException {
        server.start();
        logger.warn("[{}]  Server started, listening on {}", self, self.port);
    }

    @Override
    public void leave() {
        logger.warn("[{}]  Leaves the network", self);
        shutdown();
    }

    @Override
    public void fail() {
        leave();
    }

    @Override
    public void shutdown() {
        logger.warn("[{}]  Initiated node shutdown!", self);
        stopServer();
        descheduleAll();
    }

    public void stopServer() {
        if (server != null) {
            server.shutdownNow();
            logger.warn("[{}]  Server stopped, listening on {}", self, self.port);
        }
    }

    /**
     * Schedule refresh for all K-buckets
     */
    private void startRefreshing() {
        lockWrapper(() -> {
            for (int i = 0; i < ID_LENGTH; i++) {
                final int finalI = i;
                ScheduledFuture<?> refreshTimer = executor.scheduleAtFixedRate(() -> refreshBucket(finalI), refreshInterval.toMillis(), refreshInterval.toMillis(), TimeUnit.MILLISECONDS);
                refreshTasks.put(i, refreshTimer);
            }
        });
    }

    private void descheduleAll() {
        lockWrapper(() -> {
            republishTasks.forEach((k, v) -> v.cancel(true));
            refreshTasks.forEach((k, v) -> v.cancel(true));
            expireTasks.forEach((k, v) -> v.cancel(true));
            executor.shutdownNow();
        });
    }


    ////  RPC  calls to other nodes  ////

    /**
     * When new node J is joining, it contacts bootsrap node B <br>
     * J inserts B into appropriate K-bucket <br>
     * J then prompts W to lookup U.id <br>
     * Finally, J will refresh all K-buckets further away than the B's K-bucket
     */
    public void join(NodeReference bootstrap) throws IOException {
        initKademlia();

//        logger.warn("[{}]  Joining KadNetwork!", self);

        insertIntoRoutingTable(bootstrap);

        // prompt bootstrap to do lookup for an ID
        ManagedChannel channel = ManagedChannelBuilder.forTarget(bootstrap.getAddress()).usePlaintext().build();
        Kademlia.LookupRequest request = Kademlia.LookupRequest.newBuilder()
                .setTargetId(self.getId().toString())
                .setJoiningNode(self.toProto())
                .build();

        logger.trace("[{}]  JOIN - prompting boostrap node [{}] for myId lookup", self, bootstrap);
        Kademlia.LookupResponse response = KademliaServiceGrpc.newBlockingStub(channel).promptNodeLookup(request);
        channel.shutdown();

        response.getFoundNodesList().forEach(n -> insertIntoRoutingTable(new NodeReference(n)));

        // refresh all KB further away than the B's KB (refresh = lookup for random id in bucket range)
        // Note: some sources suggest to refresh all KB
        int bootstrapIndex = routingTable.getBucketIndex(bootstrap.getId());
        logger.trace("[{}]  JOIN - initiating refresh from {}th KB", self, bootstrapIndex);
        for (int i = bootstrapIndex+1; i < ID_LENGTH; i++) {
            refreshBucket(i);
        }
        logger.debug("[{}]  Joined KadNetwork!", self);
    }

    public void join(String ip, int port) {
        NodeReference bootstrap = new NodeReference(ip, port);
        try {
            join(bootstrap);
        } catch (IOException e) {
            logger.error("[{}]  Error while joining network: {}", self, e.toString());
        }
    }

    /**
     * Locate K globally-closest nodes to the targetId <br>
     * If joiningNode is not null, all nodes found during the lookup are returned
     * @param joiningNode - node that is joining the network, null if it's a regular lookup
     * @return List of nodes that were found during the lookup <br>
     */
    private List<NodeReference> nodeLookup__(BigInteger targetId, Kademlia.NodeReference joiningNode) {
        logger.trace("[{}]  initiating nodeLookup", self);

        if (routingTable.getSize() == 0) {
            logger.trace("[{}]  My routing table is empty", self);
            return new ArrayList<>();
        }

        List<NodeReference> k_best = routingTable.findAlphaClosest(targetId);
        logger.trace("[{}]  Initial alfa best: {}", self, k_best);
        HashSet<NodeReference> allFoundNodes = new HashSet<>(k_best);
        Set<NodeReference> failedNodes = new HashSet<>();
        Set<NodeReference> bestAndProbed = new HashSet<>();

        while (true) { // while better results are coming

            Set<NodeReference> foundInOneIteration = multicastFindNode(new ArrayList<>(k_best), targetId, joiningNode, failedNodes);
            logger.trace("[{}]  FoundInOneIteration: {}", self, foundInOneIteration);
            if (!failedNodes.isEmpty()) {
                for (NodeReference failedNode : failedNodes) {
                    routingTable.remove(failedNode);
                    k_best.remove(failedNode);
                    foundInOneIteration.remove(failedNode);
                    allFoundNodes.remove(failedNode);
                }
            }

            if (joiningNode != null)
                allFoundNodes.addAll(foundInOneIteration);

            BigInteger currBest = getBestDistance(k_best, targetId);
            BigInteger newBest = getBestDistance(foundInOneIteration, targetId);

            foundInOneIteration.addAll(k_best);

            k_best = foundInOneIteration.stream()
                    .sorted(Comparator.comparing(node -> targetId.xor(node.getId())))
                    .limit(K_PARAMETER)
                    .collect(Collectors.toList());

            // Terminate when same or worse node found
            if (currBest == null || newBest == null || newBest.compareTo(currBest) >= 0) {
                break;
            }
        }

        if (joiningNode != null) {
            logger.trace("[{}]  Node lookup finished w joining node", self);
            // all nodes found during the lookup = all nodes into which joiningNode was inserted to
            return new ArrayList<>(allFoundNodes);
        }

        logger.trace("[{}]  Node lookup finished w/o joining node", self);
        return k_best;
    }

    /**
     * Locate K globally-closest nodes to the targetId <br>
     * If joiningNode is not null, all nodes found during the lookup are returned
     * @param joiningNode - node that is joining the network, null if it's a regular lookup
     * @return List of nodes that were found during the lookup <br>
     */
    private List<NodeReference> nodeLookup(BigInteger targetId, Kademlia.NodeReference joiningNode) {
        logger.trace("[{}]  initiating nodeLookup", self);

        if (routingTable.getSize() == 0) {
            logger.trace("[{}]  My routing table is empty", self);
            return new ArrayList<>();
        }

        Shortlist SL = new Shortlist(routingTable.findAlphaClosest(targetId));

        while (SL.hasUnqueried()) {
            multicastFindNode(SL, targetId, joiningNode);
        }
        SL.getOffline().forEach(routingTable::remove);

        return SL.getKBestQueried(targetId, K_PARAMETER);
    }

    /**
     *  Return K number of closest nodes to the given ID from the given colletion
     */
    public List<NodeReference> selectKClosest(Collection<NodeReference> col, BigInteger targetId) {
        return col.stream()
                .sorted(Comparator.comparing(node -> targetId.xor(node.getId())))
                .limit(K_PARAMETER)
                .collect(Collectors.toList());
    }

    /**
     * Makes at most ALPHA concurrent calls (depends on the size of toQuery)
     * Returns all nodes found during the lookup
     */
    private void multicastFindNode(Shortlist SL, BigInteger targetId, Kademlia.NodeReference joiningNode) {
        List<NodeReference> toQuery = SL.pollAlphaNodesForQuery();

        CountDownLatch latch = new CountDownLatch(toQuery.size());
        Set<NodeReference> fromRecipient = Collections.synchronizedSet(new HashSet<>());

        for (NodeReference recipient : toQuery) {

            ManagedChannel channel = ManagedChannelBuilder.forTarget(recipient.getAddress()).usePlaintext().build();
            Kademlia.FindNodeRequest.Builder request = Kademlia.FindNodeRequest.newBuilder()
                    .setTargetId(targetId.toString())
                    .setSender(self.toProto());
            if (joiningNode != null)
                request.setJoiningNode(joiningNode);
            KademliaServiceGrpc.newStub(channel).findNode(request.build(), new StreamObserver<Kademlia.FindNodeResponse>() {
                @Override
                public void onNext(Kademlia.FindNodeResponse findNodeResponse) {
                    findNodeResponse.getKClosestList().stream().map(NodeReference::new).forEach(fromRecipient::add);
                }

                @Override
                public void onError(Throwable throwable) {
                    if (throwable instanceof StatusRuntimeException) {
                        StatusRuntimeException e = (StatusRuntimeException) throwable;
                        if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                            logger.error("[{}]  asyncFindNode: Node is unresponsive, will delete [{}]", self, recipient);
                        } else {
                            logger.error("[{}]  asyncFindNode: Unexpected code when contacting node [{}]: {}", self, recipient, e.getStatus());
                        }
                    } else {
                        logger.error("[{}]  asyncFindNode: Unexpected exception when contacting node [{}]: {}", self, recipient, throwable.toString());
                    }
                    SL.addOffline(recipient);

                    latch.countDown();
                    channel.shutdown();
                }

                @Override
                public void onCompleted() {
                    insertIntoRoutingTable(recipient);

                    latch.countDown();
                    channel.shutdown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("[{}]  Waiting for async calls interrupted during multiCast", self);
        }

        fromRecipient.forEach(SL::addToQuery);
    }

    /**
     * Makes at most ALPHA concurrent calls (depends on the size of toQuery)
     * Returns all nodes found during the lookup
     */
    private Set<NodeReference> multicastFindNode(List<NodeReference> toQuery, BigInteger targetId, Kademlia.NodeReference joiningNode, Set<NodeReference> failedNodes) {
        toQuery.remove(self); // do not query self
        Set<NodeReference> foundInOneIteration = new HashSet<>();

        while (!toQuery.isEmpty()) {
            int calls = Math.min(ALPHA_PARAMETER, toQuery.size());
            CountDownLatch latch = new CountDownLatch(calls);
            Set<NodeReference> found = Collections.synchronizedSet(new HashSet<>());

            for (int i = 0; i < calls; i++) {
                NodeReference recipient = toQuery.remove(toQuery.size() - 1);

                ManagedChannel channel = ManagedChannelBuilder.forTarget(recipient.getAddress()).usePlaintext().build();
                Kademlia.FindNodeRequest.Builder request = Kademlia.FindNodeRequest.newBuilder()
                        .setTargetId(targetId.toString())
                        .setSender(self.toProto());
                if (joiningNode != null)
                    request.setJoiningNode(joiningNode);
                KademliaServiceGrpc.newStub(channel).findNode(request.build(), new StreamObserver<Kademlia.FindNodeResponse>() {
                    @Override
                    public void onNext(Kademlia.FindNodeResponse findNodeResponse) {
                        findNodeResponse.getKClosestList().stream().map(NodeReference::new).forEach(found::add);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (throwable instanceof StatusRuntimeException) {
                            StatusRuntimeException e = (StatusRuntimeException) throwable;
                            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                                logger.error("[{}]  asyncFindNode: Node is unresponsive, will delete [{}]", self, recipient);
                            } else {
                                logger.error("[{}]  asyncFindNode: Unexpected code when contacting node [{}]: {}", self, recipient, e.getStatus());
                            }
                        } else {
                            logger.error("[{}]  asyncFindNode: Unexpected exception when contacting node [{}]: {}", self, recipient, throwable.toString());
                        }
                        failedNodes.add(recipient);
                        latch.countDown();
                        channel.shutdown();
                    }

                    @Override
                    public void onCompleted() {
                        insertIntoRoutingTable(recipient);
                        latch.countDown();
                        channel.shutdown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
//                e.printStackTrace();
                logger.error("[{}]  Waiting for async calls interrupted during multiCast", self);
            }

            foundInOneIteration.addAll(found);
        }
        return foundInOneIteration;
    }

    /**
     * Node becomes <b>original publisher</b> of the key. It is responsible for periodical republishing to the K-closest nodes. Nodes on which key was not republished in the last expireInterval will delete the key.
     */
    public void put(String key, String value) {
        BigInteger keyHash = getId(key);

        if (routingTable.getSize() == 0) {
            lockWrapper(() -> {
                localData.put(keyHash, value);
                ScheduledFuture<?> expireTimer = executor.schedule(getExpireTask(keyHash), expireInterval.toMillis(), TimeUnit.MILLISECONDS);
                expireTasks.put(keyHash, expireTimer);
                ScheduledFuture<?> republishTimer = executor.scheduleAtFixedRate(getRepublishTask(key, keyHash, value), republishInterval.toMillis(), republishInterval.toMillis(), TimeUnit.MILLISECONDS);
                republishTasks.put(keyHash, republishTimer);
            });
            return;
        }

        lockWrapper(() -> {
            ScheduledFuture<?> republishTimer = executor.scheduleAtFixedRate(getRepublishTask(key, keyHash, value), republishInterval.toMillis(), republishInterval.toMillis(), TimeUnit.MILLISECONDS);
            republishTasks.put(keyHash, republishTimer);
        });

        Runnable republishTask = getRepublishTask(key, keyHash, value);
        republishTask.run();
    }

    /**
     * Retrieve value associated with the key from the K-closest nodes to the keyhash
     */
    public String get(String key) {
        BigInteger keyHash = getId(key);

        if(routingTable.getSize() == 0) {
            return lockGetWrapper(() -> localData.get(keyHash));
        }

        List<NodeReference> kClosest = nodeLookup(keyHash, null);
        CountDownLatch latch = new CountDownLatch(kClosest.size());
        ArrayList<String> arr = new ArrayList<>(kClosest.size());
        logger.debug("[{}]  Retrieving key={} from k-closest: {}", self, key, kClosest);


        for(NodeReference node : kClosest) {

            ManagedChannel channel = ManagedChannelBuilder.forTarget(node.getAddress()).usePlaintext().build();
            Kademlia.RetrieveRequest.Builder request = Kademlia.RetrieveRequest.newBuilder()
                    .setSender(self.toProto())
                    .setKey(keyHash.toString());

            KademliaServiceGrpc.newStub(channel).retrieve(request.build(), new StreamObserver<Kademlia.RetrieveResponse>() {
                @Override
                public void onNext(Kademlia.RetrieveResponse response) {
                    synchronized (arr) {
                        arr.add(response.getValue());
                    }
                    latch.countDown();
                }

                @Override
                public void onError(Throwable t) {
                    latch.countDown();
                    routingTable.remove(node);
                    logger.error("[{}]  RETRIEVE: Error while finding node[{}]: {}, cause:", self, node, t.toString(), t.getCause());
                    channel.shutdown();
                }

                @Override
                public void onCompleted() {
                    insertIntoRoutingTable(node);
                    channel.shutdown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("[{}]  Waiting for async calls interrupted", self, e);
        }
        return arr.stream().filter(Objects::nonNull).filter(s -> !s.isEmpty()).findFirst().orElse(null);
    }


    /**
     * Delete key-value pair from the K-closest nodes to the keyhash. <br>
     */
    public void delete(String key) {
        BigInteger keyHash = getId(key);

        if(routingTable.getSize() == 0) {
            lockWrapper(() -> localData.remove(keyHash));
            return;
        }

        List<NodeReference> kClosest = nodeLookup(keyHash, null);
        logger.debug("[{}]  Deleting key={} from k-closest: {}", self, key, kClosest);
        CountDownLatch latch = new CountDownLatch(kClosest.size());

        for (NodeReference node : kClosest) {

            if (node.equals(self)){
                deleteAndDeschedule(keyHash);
                latch.countDown();
                continue;
            }

            ManagedChannel channel = ManagedChannelBuilder.forTarget(node.getAddress()).usePlaintext().build();
            Kademlia.DeleteRequest.Builder request = Kademlia.DeleteRequest.newBuilder()
                    .setKey(keyHash.toString())
                    .setSender(self.toProto());
            KademliaServiceGrpc.newStub(channel).delete(request.build(), new StreamObserver<Kademlia.DeleteResponse>() {
                @Override
                public void onNext(Kademlia.DeleteResponse deleteResponse) {/*do nothing when response*/}

                @Override
                public void onError(Throwable throwable) {
                    routingTable.remove(node);
                    logger.error("[{}]  DELETE: Error while finding node[{}]: {}", self, node, throwable.toString());
                    latch.countDown();
                    channel.shutdown();
                }

                @Override
                public void onCompleted() {
                    insertIntoRoutingTable(node);
                    latch.countDown();
                    channel.shutdown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("[{}]  Waiting for async calls interrupted", self, e);
        }
    }


    ////  Utility methods  ////

    private Runnable getRepublishTask(String key, BigInteger keyHash, String value) {
        return () -> {
            List<NodeReference> kClosest = nodeLookup(keyHash, null);
            CountDownLatch latch = new CountDownLatch(kClosest.size());

            logger.debug("[{}]  Asynchronously republishing key {} to k-closest: {}", self, key, kClosest);

            for (NodeReference node : kClosest) {
                ManagedChannel channel = ManagedChannelBuilder.forTarget(node.getAddress()).usePlaintext().build();
                Kademlia.StoreRequest request = Kademlia.StoreRequest.newBuilder()
                        .setKey(keyHash.toString())
                        .setValue(value)
                        .setSender(self.toProto())
                        .build();
                KademliaServiceGrpc.newStub(channel).store(request, new StreamObserver<Kademlia.StoreResponse>() {
                    @Override
                    public void onNext(Kademlia.StoreResponse storeResponse) {}
                    @Override
                    public void onError(Throwable throwable) {
                        logger.error("[{}]  republish: Error while storing key[{}] on node[{}]: {}", self, keyHash, node, throwable.toString());
                        routingTable.remove(node);
                        latch.countDown();
                        channel.shutdown();
                    }
                    @Override
                    public void onCompleted() {
                        insertIntoRoutingTable(node);
                        latch.countDown();
                        channel.shutdown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error("[{}]  Waiting for async calls interrupted", self, e);
            }
        };
    }

    private Runnable getExpireTask(BigInteger keyHash) {
        return () -> {
            logger.trace("[{}]  Key[{}] expired!", self, keyHash);
            lockWrapper(() ->localData.remove(keyHash));
        };
    }

    private void deleteAndDeschedule(BigInteger keyhash) {
        lockWrapper(() -> {
            localData.remove(keyhash);
            expireTasks.get(keyhash).cancel(true);
        });
    }

    /**
     * Insert into K-bucket, postpone its refresh task
     */
    private void insertIntoRoutingTable(NodeReference node) {
        int bucketIndex = routingTable.getBucketIndex(node.getId());
        routingTable.insert(node);

        lockWrapper(() -> {
            refreshTasks.get(bucketIndex).cancel(false);
            try {
                ScheduledFuture<?> refreshTimer = executor.scheduleAtFixedRate(() -> refreshBucket(bucketIndex), refreshInterval.toMillis(), refreshInterval.toMillis(), TimeUnit.MILLISECONDS);
                refreshTasks.replace(bucketIndex, refreshTimer);
            } catch (RejectedExecutionException e) {
                if (executor.isShutdown()) {
                    logger.error("[{}]  Cannot schedule bucket refresh, node seems to be shut-down: {}", self, e.toString());
                }
                if (executor.isTerminated()) {
                    logger.error("[{}]  Cannot schedule bucket refresh, node seems to be terminated: {}", self, e.toString());
                }
            }
        });
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

    private String lockGetWrapper(Supplier<String> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    private void refreshBucket(int index) {
        logger.trace("[{}]  Refreshing bucket {}", self, index);
        nodeLookup(randomWithinBucket(index), null).forEach(this::insertIntoRoutingTable);
    }

    private BigInteger getBestDistance(Collection<NodeReference> collection, BigInteger targetId) {
        return collection.stream()
                .map(n -> targetId.xor(n.getId()))
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    ////////////////////////////////
    ///  SERVER-SIDE PROCESSING  ///
    ////////////////////////////////

    /**
     * Server-side of Kademlia node
     */
    private class KademliaNodeServer extends KademliaServiceGrpc.KademliaServiceImplBase {

        /**
         * Initiated on joining node, sent to Booststrap node
         */
        @Override
        public void promptNodeLookup(Kademlia.LookupRequest request, StreamObserver<Kademlia.LookupResponse> responseObserver) {
            NodeReference joiningNode = new NodeReference(request.getJoiningNode());
            logger.trace("[{}]  Node lookup initiated from [{}]", self, joiningNode);

            List<NodeReference> kClosest = nodeLookup(new BigInteger(request.getTargetId()), request.getJoiningNode());
            insertIntoRoutingTable(joiningNode); // break the "insert most recently contacted" rule to not query the joining node

            Kademlia.LookupResponse.Builder response = Kademlia.LookupResponse.newBuilder()
                    .addAllFoundNodes(kClosest.stream().map(NodeReference::toProto).collect(Collectors.toList()));

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }

        /**
         * Instructs a node to store a key-value pair for later retrieval. Also acts as republish prompt.
         */
        @Override
        public void store(Kademlia.StoreRequest request, StreamObserver<Kademlia.StoreResponse> responseObserver) {
            insertIntoRoutingTable(new NodeReference(request.getSender()));
            logger.trace("[{}]  Received STORE rpc from {}", self, request.getSender().getPort());

            BigInteger key = new BigInteger(request.getKey());
            String value = request.getValue();

            lockWrapper(() -> {
                if (!localData.containsKey(key)) {
                    // new -> schedule
                    localData.put(key, value);
                    ScheduledFuture<?> expireTimer = executor.schedule(getExpireTask(key), expireInterval.toMillis(), TimeUnit.MILLISECONDS);
                    expireTasks.put(key, expireTimer);
                }
                else {
                    // already contains -> reschedule
                    if (!localData.get(key).equals(value)) {
                        localData.replace(key, value);
                    }
                    expireTasks.get(key).cancel(true);
                    ScheduledFuture<?> expireTimer = executor.schedule(getExpireTask(key), expireInterval.toMillis(), TimeUnit.MILLISECONDS);
                    expireTasks.replace(key, expireTimer);
                }
            });

            responseObserver.onNext(Kademlia.StoreResponse.newBuilder().setStatus(Kademlia.Status.SUCCESS).build());
            responseObserver.onCompleted();
        }

        /**
         * Instructs a node to retrieve the value associated with the given key
         */
        @Override
        public void retrieve(Kademlia.RetrieveRequest request, StreamObserver<Kademlia.RetrieveResponse> responseObserver) {
            insertIntoRoutingTable(new NodeReference(request.getSender()));

            BigInteger key = new BigInteger(request.getKey());
            String value = lockGetWrapper(() ->localData.get(key));

            Kademlia.RetrieveResponse response;
            if (value == null) {
                response = Kademlia.RetrieveResponse.newBuilder()
                        .setStatus(Kademlia.Status.NOT_FOUND)
                        .build();
            } else {
                response = Kademlia.RetrieveResponse.newBuilder()
                        .setStatus(Kademlia.Status.SUCCESS)
                        .setValue(value)
                        .build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        /**
         * Recipient returns k nodes it knows about closest to the target ID
         */
        @Override
        public void findNode(Kademlia.FindNodeRequest request, StreamObserver<Kademlia.FindNodeResponse> responseObserver) {
//            logger.trace("[{}]  Received FIND_NODE rpc", self);
//            insertIntoRoutingTable(new NodeReference(request.getSender()));

            BigInteger targetId = new BigInteger(request.getTargetId());
            List<NodeReference> kClosest = routingTable.findKClosest(targetId);
//            logger.trace("[{}]  Out of all my nodes {}, returning k closest: {} nodes to {}", self, routingTable.buckets, kClosest, targetId);

            insertIntoRoutingTable(new NodeReference(request.getSender()));
            if (request.hasJoiningNode()) {
                insertIntoRoutingTable(new NodeReference(request.getJoiningNode()));
            }

            Kademlia.FindNodeResponse.Builder response = Kademlia.FindNodeResponse.newBuilder();
            kClosest.forEach(node -> {
                Kademlia.NodeReference.Builder nodeBuilder = Kademlia.NodeReference.newBuilder()
                        .setIp(node.getIp())
                        .setPort(node.getPort())
                        .setId(node.getId().toString());
                response.addKClosest(nodeBuilder);
            });

//            logger.trace("[{}]  Sending FIND_NODE response", self);

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }

        @Override
        public void delete(Kademlia.DeleteRequest request, StreamObserver<Kademlia.DeleteResponse> responseObserver) {
            insertIntoRoutingTable(new NodeReference(request.getSender()));

            BigInteger key = new BigInteger(request.getKey());
            String value = lockGetWrapper(() -> localData.get(key));

            Kademlia.DeleteResponse response;
            if (value == null) {
                response = Kademlia.DeleteResponse.newBuilder()
                        .setStatus(Kademlia.Status.NOT_FOUND)
                        .build();
            } else {
                deleteAndDeschedule(key);
                response = Kademlia.DeleteResponse.newBuilder()
                        .setStatus(Kademlia.Status.SUCCESS)
                        .build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void ping(Kademlia.Empty request, StreamObserver<Kademlia.Empty> responseObserver) {
            super.ping(request, responseObserver);
        }
    }

}
