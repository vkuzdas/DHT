package kademlia.unit.join;

import kademlia.KademliaNode;
import kademlia.NodeReference;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import kademlia.unit.BaseTest;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Disable to not waste CI resources")
public class MoreNodeJoinTest extends BaseTest {
    @Test
    public void testThree_joinBootstrap() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        KademliaNode joiner1 = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ONE);
        runningNodes.add(joiner1);
        joiner1.join(bootstrap.getNodeReference());

        KademliaNode joiner2 = new KademliaNode(LOCAL_IP, BASE_PORT++, new BigInteger("2"));
        runningNodes.add(joiner2);
        joiner2.join(bootstrap.getNodeReference());

        assertEquals(2, bootstrap.getRoutingTable().getSize());
        assertEquals(2, joiner1.getRoutingTable().getSize());
        assertEquals(2, joiner2.getRoutingTable().getSize());

        assertEquals(2, countAllRoutingTableEntries(bootstrap));
        assertEquals(2, countAllRoutingTableEntries(joiner1));
        assertEquals(2, countAllRoutingTableEntries(joiner2));
    }


    @Test
    public void testThree_joinChain() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        KademliaNode joiner1 = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ONE);
        runningNodes.add(joiner1);
        joiner1.join(bootstrap.getNodeReference());

        KademliaNode joiner2 = new KademliaNode(LOCAL_IP, BASE_PORT++, new BigInteger("2"));
        runningNodes.add(joiner2);
        joiner2.join(joiner1.getNodeReference());


        assertEquals(2, bootstrap.getRoutingTable().getSize());
        assertEquals(2, joiner1.getRoutingTable().getSize());
        assertEquals(2, joiner2.getRoutingTable().getSize());

        assertEquals(2, countAllRoutingTableEntries(bootstrap));
        assertEquals(2, countAllRoutingTableEntries(joiner1));
        assertEquals(2, countAllRoutingTableEntries(joiner2));
    }



    @Test
    public void testFive_joinBoostrap() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        int couple = 5;

        for (int i = 1; i < couple; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, new BigInteger(""+i));
            runningNodes.add(joiner);
            joiner.join(bootstrap.getNodeReference());
        }

        for(KademliaNode node : runningNodes) {
            assertEquals(couple-1, node.getRoutingTable().getSize());
            assertEquals(couple-1, countAllRoutingTableEntries(node));
        }

    }

    @Test
    public void testFive_joinChained() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        NodeReference prev = bootstrap.getNodeReference();

        int couple = 5;

        for (int i = 1; i < couple; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, new BigInteger(""+i));
            runningNodes.add(joiner);
            joiner.join(prev);
            prev = joiner.getNodeReference();
        }

        for(KademliaNode node : runningNodes) {
            assertEquals(couple-1, node.getRoutingTable().getSize());
            assertEquals(couple-1, countAllRoutingTableEntries(node));
        }
    }

    /**
     * <b>Not all nodes have whole network in their Routing Table:</b>
     * for example, when 10_009 joins, bootstrap sends FIND_NODE to nodes [10_005,..,10_008]
     * therefore 10_009 will be only inserted into those routing tables
     */
    @Test
    public void testTen_joinBoostrap() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        int couple = 10;

        for (int i = 1; i < couple; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, new BigInteger(""+i));
            runningNodes.add(joiner);
            joiner.join(bootstrap.getNodeReference());
        }

        // not sure if there is a general consistent assert to be made
//        for(KademliaNode node : runningNodes) {
//            assertEquals(couple-1, node.getRoutingTable().getSize());
//            assertEquals(couple-1, countAllRoutinTableEntries(node));
//        }
    }

    @Test
    public void testTen_joinChained() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        NodeReference prev = bootstrap.getNodeReference();

        int couple = 10;

        for (int i = 1; i < couple; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, new BigInteger(""+i));
            runningNodes.add(joiner);
            joiner.join(prev);
            prev = joiner.getNodeReference();
        }
    }

    /**
     * Since im not very confident about {@link kademlia.RoutingTable#getSize()} method
     */
    private int countAllRoutingTableEntries(KademliaNode node) {
        int cnt = 0;
        for (int i = 0; i < BITS; i++) {
            cnt += node.getRoutingTable().getKBucket(i).getSize();
        }
        return cnt;
    }
}
