package kademlia.unit.routingtable;

import kademlia.KademliaNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import kademlia.unit.BaseTest;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Disable to not waste CI resources")
public class DropNodeTest extends BaseTest {

    /**
     * Validate that after joining K+1 nodes from the same bucket, a node is dropped <br> <br>
     * <i>Note: Exact dropped node cannot be validated since nodelookup is done asynchronously -> nodes respond in random, therefore are inserted into routing table at random</i>
     */
    @Test
    public void testKBucketDropsNode_simple() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        BigInteger thirdBucketStartId = new BigInteger("2").pow(3);

        // fill bucket with K nodes
        for (int i = 0; i < K; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, thirdBucketStartId.add(BigInteger.valueOf(i)));
            runningNodes.add(joiner);
            joiner.join(bootstrap.getNodeReference());
        }

        int fullBucketIndex = bootstrap.getRoutingTable().getBucketIndex(thirdBucketStartId);
        assertEquals(K, bootstrap.getRoutingTable().getKBucket(fullBucketIndex).getSize());

        // insert K+1-th node, thus dropping a node
        KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, thirdBucketStartId.add(BigInteger.valueOf(K)));
        runningNodes.add(joiner);
        joiner.join(bootstrap.getNodeReference());

        // still K -> a node was dropped
        assertEquals(K, bootstrap.getRoutingTable().getKBucket(fullBucketIndex).getSize());
    }

}
