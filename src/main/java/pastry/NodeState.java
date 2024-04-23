package pastry;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pastry.metric.DistanceCalculator;
import proto.Pastry;
import proto.PastryServiceGrpc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Synchronizes and unifies access to node's Neighbors, Leafs and RoutingTable
 */
public class NodeState {

    private static final Logger logger = LoggerFactory.getLogger(PastryNode.class);

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * log(base,N) rows, base columns
     */
    private final List<List<NodeReference>> routingTable = new ArrayList<>();
    /**
     * Closest nodes per metric
     */
    private final List<NodeReference> neighborSet = new ArrayList<>();
    /**
     * Numerically closest larger nodeIds
     */
    private final List<NodeReference> upLeafs = new ArrayList<>();
    /**
     * Nodes with numerically closest smaller nodeIds
     */
    private final List<NodeReference> downLeafs = new ArrayList<>();

    private final int leafSize;

    private final NodeReference self;
    private final DistanceCalculator distanceCalculator;

    public NodeState(int bParameter, int routingTableSize, int leafSize, String ip, int port, long x, long y, DistanceCalculator distanceCalculator) {
        this.self = new NodeReference(ip, port, x, y, 0);

        this.leafSize = leafSize;

        for (int i = 0; i < routingTableSize; i++) {
            ArrayList<NodeReference> emptyRow = new ArrayList<>();
            for (int j = 0; j < (int)Math.pow(2, bParameter); j++) {
                emptyRow.add(null);
            }
            routingTable.add(emptyRow);
        }

        this.distanceCalculator = distanceCalculator;
    }

    public ArrayList<NodeReference> getNeighborsCopy() {
        ArrayList<NodeReference> copy = new ArrayList<>();
        lock.lock();
        try {
            copy.addAll(neighborSet);
        } finally {
            lock.unlock();
        }
        return copy;
    }

    public ArrayList<ArrayList<NodeReference>> getRoutingTableCopy() {
        ArrayList<ArrayList<NodeReference>> copy = new ArrayList<>();
        lock.lock();
        try {
            for (List<NodeReference> row : routingTable) {
                ArrayList<NodeReference> newRow = new ArrayList<>(row);
                copy.add(newRow);
            }
        } finally {
            lock.unlock();
        }
        return copy;
    }

    public NodeReference getSelf() {
        return self;
    }

    public int getUpLeafSize() {
        return syncSizeGet(upLeafs);
    }

    public int getDownLeafSize() {
        return syncSizeGet(downLeafs);
    }

    public boolean inLeafRange(String id_base) {
        if ( ! (getDownLeafSize() > 0 && getUpLeafSize() > 0) ) {
            return false;
        }

        BigInteger id_dec = Util.convertToDecimal(id_base);
        BigInteger minLeaf = syncLastGet(downLeafs).getDecimalId();
        BigInteger maxLeaf = syncLastGet(upLeafs).getDecimalId();

        // minLeaf <= id <= maxLeaf
        return minLeaf.compareTo(id_dec) <= 0 &&  id_dec.compareTo(maxLeaf) <= 0;
    }

    public NodeReference routingTableGet(int row, int col) {
        NodeReference r;
        lock.lock();
        try {
            r = routingTable.get(row).get(col);
        } finally {
            lock.unlock();
        }
        return r;
    }

    ///////////////////
    ///  STABILIZE  ///
    ///////////////////

    /**
     * Return the <b>first</b> neighbor's neighbor that is not in the current neighbor set
     */
    public NodeReference getNewNeighbor(PastryServiceGrpc.PastryServiceBlockingStub blockingStub) {
        lock.lock();
        NodeReference newNode = null;
        try {
            for (NodeReference neighbor : neighborSet) {
                ManagedChannel channel = ManagedChannelBuilder.forTarget(neighbor.getAddress()).usePlaintext().build();
                blockingStub = PastryServiceGrpc.newBlockingStub(channel);
                Pastry.NeighborSetResponse response = blockingStub.getNeighborSet(Pastry.NeighborSetRequest.newBuilder().build());
                for (Pastry.NodeReference n : response.getNeighborSetList()) {
                    newNode = new NodeReference(n);
                    if (!neighborSet.contains(newNode) && !newNode.equals(self)) {
                        return newNode;
                    }
                }
                channel.shutdown();
            }
        } finally {
            lock.unlock();
        }
        return newNode;
    }

    public Pastry.NeighborSetResponse neighborsToProto() {
        Pastry.NeighborSetResponse.Builder response = Pastry.NeighborSetResponse.newBuilder();
        lock.lock();
        try {
            neighborSet.forEach(n -> response.addNeighborSet(Pastry.NodeReference.newBuilder()
                    .setIp(n.getIp())
                    .setPort(n.getPort())
                    .build()));
        } finally {
            lock.unlock();
        }
        return response.build();
    }

    public void unregisterFailedNode(NodeReference failed) {
        lock.lock();
        try {
            neighborSet.remove(failed);

            List<NodeReference> leafSet = failed.getDecimalId().compareTo(self.getDecimalId()) < 0 ? downLeafs : upLeafs;
            leafSet.remove(failed);

            int l = Util.getSharedPrefixLength(failed.getId(), self.getId());
            List<NodeReference> row = routingTable.get(l);
            if(row.contains(failed)) {
                row.set(row.indexOf(failed), null);
            }
        } finally {
            lock.unlock();
        }
    }


    /////////////////
    ///  ROUTING  ///
    /////////////////

    /**
     * Return the closest leaf to the given id
     */
    public NodeReference getNumericallyClosestLeaf(String id_base) {
        BigInteger id_dec = Util.convertToDecimal(id_base);
        lock.lock();
        NodeReference closest = self;
        try {
            BigInteger minDistance = self.getDecimalId().subtract(id_dec).abs();
            for (NodeReference leaf : upLeafs) {
                BigInteger distance = leaf.getDecimalId().subtract(id_dec).abs();
                if (distance.compareTo(minDistance) < 0) {
                    minDistance = distance;
                    closest = leaf;
                }
            }

            for (NodeReference leaf : downLeafs) {
                BigInteger distance = leaf.getDecimalId().subtract(id_dec).abs();
                if (distance.compareTo(minDistance) < 0) {
                    minDistance = distance;
                    closest = leaf;
                }
            }
        } finally {
            lock.unlock();
        }

        return closest;
    }

    /**
     * <i> forward to T ∈ (L ∪ R ∪ M), s.th. shl(T, D) >= l && |T - D| < |A - D|</i>
     * Find to a node that shares prefix with the key at least as long as the local node and is numerically closer to the key than the present node’s id.
     */
    public NodeReference findSameLengthMatch(String id_base, int l) {
        lock.lock();
        try {
            // start by searching R[l] entries (same len entries)
            for (int i = l; i < routingTable.size(); i++) {
                List<NodeReference> row = routingTable.get(i);
                for(NodeReference n : row) {
                    if (neighborSet.contains(n) || (upLeafs.contains(n) || downLeafs.contains(n)) ){
                        BigInteger t = n.getDecimalId();
                        BigInteger k = Util.convertToDecimal(id_base);
                        BigInteger s = self.getDecimalId();
                        if (t.subtract(k).abs().compareTo(s.subtract(k).abs()) < 0) {
                            return n;
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        logger.debug("[{}]  No same length match found for {}, routing to self", self, id_base);
        return self;
    }


    //////////////
    ///  JOIN  ///
    //////////////

    /**
     * X is the joining node (self) <br>
     * A is bootstrap node <br>
     * Z is node onto which join request converges since it has closest nodeId to X <br>
     */
    public void updateNodeState(Pastry.JoinResponse resp) {
        // statement about joining and copying node state from nodes A and Z does not hold
        // a DHT should be able to handle join no matter the boostrap distance
        // we should therefore copy ALL nodes that we can get from joinResponse
        // note that number of join hops should be log of the number of nodes in the network
        // therefore it should not be expensive to copy all nodes from all NodeStates

        ArrayList<NodeReference> allNodes = new ArrayList<>();

        // Register all nodes from the response
        for (Pastry.NodeState state : resp.getNodeStateList()) {

            Pastry.NodeReference owner = state.getOwner();
            if (!allNodes.contains(new NodeReference(owner)))
                allNodes.add(new NodeReference(owner));

            for (Pastry.NodeReference neigh : state.getNeighborSetList()) {
                if (!allNodes.contains(new NodeReference(neigh)))
                    allNodes.add(new NodeReference(neigh));
            }

            for (Pastry.NodeReference leaf : state.getLeafSetList()) {
                if (!allNodes.contains(new NodeReference(leaf)))
                    allNodes.add(new NodeReference(leaf));
            }

            for (Pastry.RoutingTableRow row : state.getRoutingTableList()) {
                for (Pastry.NodeReference node : row.getRoutingTableEntryList()) {
                    if (!allNodes.contains(new NodeReference(node)))
                        allNodes.add(new NodeReference(node));
                }
            }

        }
        allNodes.forEach(this::registerNewNode);

        // A usually in proximity to X => A.neighborSet to initialize X.neighborSet (set is updated periodically)
//        int len = resp.getNodeStateCount();
////        List<Pastry.NodeReference> A_neighborSet = resp.getNodeState(len-1).getNeighborSetList();
////        A_neighborSet.forEach(node -> syncInsertIntoNeighborSet(new NodeReference(node.getIp(), node.getPort())));
//
//        // Z has the closest existing nodeId to X, thus its leaf set is the basis for X’s leaf set.
//        List<Pastry.NodeReference> Z_leafs = resp.getNodeState(0).getLeafSetList();
//        Z_leafs.forEach(node -> syncInsertIntoLeafSet(new NodeReference(node.getIp(), node.getPort())));
//
//        // Routing table
//        initRoutingTable(resp);
//
//        // register each forwarding node
//        for (Pastry.NodeState node : resp.getNodeStateList()) {
//            registerNewNode(new NodeReference(node.getOwner().getIp(), node.getOwner().getPort()));
//        }
        printNodeState();
    }

    public NodeReference getClosestUpleaf() {
        if(syncSizeGet(upLeafs) == 0) {
            return null;
        }
        return syncGet(0, upLeafs);
    }

    public NodeReference getClosestDownleaf() {
        if(syncSizeGet(downLeafs) == 0) {
            return null;
        }
        return syncGet(0, downLeafs);
    }

    /**
     * Let A be the first node encoutered in path of join request <br>
     * Let A<sub>0</sub> be the first row of A's routing table <br>
     * - Nodes in A<sub>0</sub> share no common prefix with A, same is true about X, therefore it can be set as X<sub>0</sub> <br>
     * - Nodes in B<sub>1</sub> share the first digit with X, since B and X share it. B<sub>1</sub> can be therefore set as X<sub>1</sub> <br>
     */
    private void initRoutingTable(Pastry.JoinResponse response) {
        int len = response.getNodeStateCount();
        lock.lock();
        try {
            int j = 0;
            for (int i = len-1; i >= 0; i--) { // iterate over nodes A, B, C, ...
                List<NodeReference> jthRow = routingTable.get(j);
                int selfIndex = Integer.parseInt(self.getId().charAt(j)+"");

                for (Pastry.NodeReference node : response.getNodeStateList().get(i).getRoutingTableList().get(j).getRoutingTableEntryList()) {
                    NodeReference newNode = new NodeReference(node);
                    int newNodeIndex = Integer.parseInt(newNode.getId().substring(j, j+1));
                    int l = Util.getSharedPrefixLength(newNode.getId(), self.getId());
                    if (selfIndex != newNodeIndex && l == j) { // prefix must match since join could be routed through non-longer matching prefix
                        jthRow.set(newNodeIndex, newNode);
                    }
                }
                j++; // iterate from first row
            }
        } finally {
            lock.unlock();
        }
    }


    //////////////
    ///  MISC  ///
    //////////////

    public ArrayList<NodeReference> getAllNodes() {
        ArrayList<NodeReference> allMyNodes = new ArrayList<>();
        lock.lock();
        try {
            neighborSet.forEach(n -> {
                if (!allMyNodes.contains(n))
                    allMyNodes.add(n);
            });
            downLeafs.forEach(n -> {
                if (!allMyNodes.contains(n))
                    allMyNodes.add(n);
            });
            upLeafs.forEach(n -> {
                if (!allMyNodes.contains(n))
                    allMyNodes.add(n);
            });
            routingTable.forEach(row -> row.forEach(n -> {
                if (n != null && !allMyNodes.contains(n))
                    allMyNodes.add(n);
            }));
        } finally {
            lock.unlock();
        }
        return allMyNodes;
    }

    public Pastry.NodeState toProto() {
        Pastry.NodeState.Builder stateBuilder = Pastry.NodeState.newBuilder();
        Pastry.NodeReference.Builder nodeBuilder = Pastry.NodeReference.newBuilder();
        Pastry.RoutingTableRow.Builder rowBuilder = Pastry.RoutingTableRow.newBuilder();

        lock.lock();
        try {
            neighborSet.forEach(n ->
                    stateBuilder.addNeighborSet(nodeBuilder.setIp(n.getIp()).setPort(n.getPort()).setX(n.getX()).setY(n.getY()).build())
            );
            downLeafs.forEach(n ->
                    stateBuilder.addLeafSet(nodeBuilder.setIp(n.getIp()).setPort(n.getPort()).setX(n.getX()).setY(n.getY()).build())
            );
            upLeafs.forEach(n ->
                    stateBuilder.addLeafSet(nodeBuilder.setIp(n.getIp()).setPort(n.getPort()).setX(n.getX()).setY(n.getY()).build())
            );
            routingTable.forEach(row -> {
                row.forEach(n -> {
                            if(n != null)
                                rowBuilder.addRoutingTableEntry(nodeBuilder.setIp(n.getIp()).setPort(n.getPort()).setX(n.getX()).setY(n.getY()).build());
                        }
                );
                stateBuilder.addRoutingTable(rowBuilder.build());
            });
        } finally {
            lock.unlock();
        }

        stateBuilder.setOwner(nodeBuilder
                .setIp(self.getIp())
                .setPort(self.getPort())
                .setX(self.getX())
                .setY(self.getY())
                .build());
        return stateBuilder.build();
    }

    public void printNodeState() {
        lock.lock();
        try {
            logger.trace("[{}]  Node state: ", self);
            logger.trace("  Neighbor set: {}", neighborSet);
            logger.trace("  Down leafs: {}", downLeafs);
            logger.trace("  Up leafs: {}", upLeafs);

            logger.trace("  Routing table: ");
            for (int i = 0; i < routingTable.size(); i++) {
                logger.trace("    Row {}: {}", i, routingTable.get(i));
            }
        } finally {
            lock.unlock();
        }
    }


    ////////////////
    ///  INSERT  ///
    ////////////////

    /**
     * Does not insert duplicates
     */
    public void registerNewNode(NodeReference newNode) {
        if (newNode.equals(self)) {
            return;
        }
        newNode.setDistance(distanceCalculator.calculateDistance(self, newNode));

        syncInsertIntoNeighborSet(newNode);
        syncInsertIntoLeafSet(newNode);
        syncInsertIntoRoutingTable(newNode);
    }

    /**
     * Insert node into neighborSet sorted by distance <br>
     */
    private void syncInsertIntoNeighborSet(NodeReference newNode) {
        lock.lock();
        try {
            if (!neighborSet.contains(newNode)) {
                int i = 0;
                for (NodeReference curr : neighborSet) {
                    if (curr.getDistance() < newNode.getDistance()) {
                        break;
                    }
                    i++;
                }

                neighborSet.add(i, newNode);

                if (neighborSet.size() > leafSize) {
                    neighborSet.remove(neighborSet.size() - 1);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Insert node into <b>appropriate (up/down)</b> leafSet <br>
     */
    public void syncInsertIntoLeafSet(NodeReference newNode) {
        lock.lock();
        try {
            if (newNode.getId().compareTo(self.getId()) > 0) {
                if (!upLeafs.contains(newNode)) {
                    int i = 0;
                    for (NodeReference curr : upLeafs) {
                        if (curr.getId().compareTo(newNode.getId()) > 0) {
                            break;
                        }
                        i++;
                    }

                    upLeafs.add(i, newNode);

                    if (upLeafs.size() > leafSize / 2) {
                        upLeafs.remove(upLeafs.size() - 1);
                    }
                }
            }
            else {
                if (!downLeafs.contains(newNode)) {
                    int i = 0;
                    for (NodeReference curr : downLeafs) {
                        if (curr.getId().compareTo(newNode.getId()) < 0) {
                            break;
                        }
                        i++;
                    }

                    downLeafs.add(i, newNode);

                    if (downLeafs.size() > leafSize / 2) {
                        downLeafs.remove(downLeafs.size() - 1);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void syncInsertIntoRoutingTable(NodeReference newNode) {
        int i = Util.getSharedPrefixLength(newNode.getId(), self.getId());
        int j = Util.parseDigit(newNode.getId().charAt(i));
        lock.lock();
        try {
            List<NodeReference> row = routingTable.get(i);
            // a node corresponding to self is empty in each row
            if (row.get(j) == null) {
                row.set(j, newNode);
            }
            // nodes with better distance are prefered
            else if (row.get(j).getDistance() > newNode.getDistance()) {
                row.set(j, newNode);
            }
        } finally {
            lock.unlock();
        }
    }


    /////////////
    ///  GET  ///
    /////////////

    private int syncSizeGet(List<NodeReference> list) {
        int s;
        lock.lock();
        try {
            s = list.size();
        } finally {
            lock.unlock();
        }
        return s;
    }

    private NodeReference syncGet(int index, List<NodeReference> list) {
        NodeReference r;
        lock.lock();
        try {
            r = list.get(index);
        } finally {
            lock.unlock();
        }
        return r;
    }

    public NodeReference syncLastGet(List<NodeReference> list) {
        NodeReference r;
        lock.lock();
        try {
            r = list.get(list.size()-1);
        } finally {
            lock.unlock();
        }
        return r;
    }

    public DistanceCalculator getDistanceCalculator() {
        return distanceCalculator;
    }


}
