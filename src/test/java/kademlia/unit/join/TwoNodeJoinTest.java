package kademlia.unit.join;

import kademlia.KademliaNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import kademlia.unit.BaseTest;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the join operation of bootstrap and joiner node
 */
@Disabled("Disable to not waste CI resources")
public class TwoNodeJoinTest extends BaseTest {

    @Test
    public void testJoin_noRefresh() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, new BigInteger("2").pow(19));
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        // bootstrap falls into joiner's last k-bucket -> there is no refresh done
        KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ONE);
        runningNodes.add(joiner);
        joiner.join(bootstrap.getNodeReference());

        assertRoutingTables(bootstrap, joiner);
    }

    @Test
    public void testJoin_refreshAllBuckets() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ONE);
        runningNodes.add(joiner);
        joiner.join(bootstrap.getNodeReference());


        assertRoutingTables(bootstrap, joiner);
    }

    @Test
    public void testJoin_refreshHalfBuckets() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, new BigInteger("2").pow(10));
        runningNodes.add(joiner);
        joiner.join(bootstrap.getNodeReference());

        assertRoutingTables(bootstrap, joiner);
    }

    private void assertRoutingTables(KademliaNode bootstrap, KademliaNode joiner) {
        assertEquals(1, joiner.getRoutingTable().getSize());
        assertEquals(1, bootstrap.getRoutingTable().getSize());

        BigInteger distance = joiner.getNodeReference().getId().xor(bootstrap.getNodeReference().getId());
        int index = distance.bitLength() - 1;

        // only the k-bucket at XOR index is filled
        assertEquals(1, joiner.getRoutingTable().getKBucket(index).getSize());
        assertEquals(1, bootstrap.getRoutingTable().getKBucket(index).getSize());

        // check that the k-bucket at index filled with each other
        assertEquals(bootstrap.getNodeReference(), joiner.getRoutingTable().getKBucket(index).getHead());
        assertEquals(joiner.getNodeReference(), bootstrap.getRoutingTable().getKBucket(index).getHead());

        // all other k-buckets are empty
        for (int i = 0; i < joiner.getRoutingTable().getSize(); i++) {
            if (i == index) continue;
            assertEquals(0, joiner.getRoutingTable().getKBucket(i).getSize());
            assertEquals(0, bootstrap.getRoutingTable().getKBucket(i).getSize());
        }
    }

}
