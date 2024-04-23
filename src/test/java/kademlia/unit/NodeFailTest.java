package kademlia.unit;

import kademlia.KademliaNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Disable to not waste CI resources")
public class NodeFailTest extends BaseTest {
    /**
     * If node fails to respond to multicastFindNode, it should be removed from routing table
     */
    @Test
    public void testMulticastFindNode_fail() throws IOException {
        KademliaNode boostrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.valueOf(0));
        boostrap.initKademlia();
        runningNodes.add(boostrap);

        KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.valueOf(1));
        joiner.join(boostrap.getNodeReference());
        runningNodes.add(joiner);

        int index = boostrap.getRoutingTable().getBucketIndex(joiner.getNodeReference().getId());
        assertTrue(boostrap.getRoutingTable().getKBucket(index).contains(joiner.getNodeReference()));

        joiner.shutdownKademliaNode();
        runningNodes.remove(joiner);

        // join triggers nodeLookup and multicastFindNode, when joiner does not respond, it should be removed from bootstraps routing table
        KademliaNode joiner2 = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.valueOf(2));
        joiner2.join(boostrap.getNodeReference());
        runningNodes.add(joiner2);

        assertFalse(boostrap.getRoutingTable().getKBucket(index).contains(joiner2.getNodeReference()));
    }
}
