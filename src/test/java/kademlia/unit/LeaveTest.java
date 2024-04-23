package kademlia.unit;

import kademlia.KademliaNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;

@Disabled("Disable to not waste CI resources")
public class LeaveTest extends BaseTest {

    @Test
    public void testLeave_simple() throws IOException {
        for (int i = 0; i < K+1; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.valueOf(i));
            if (runningNodes.isEmpty())
                joiner.initKademlia();
            else
                joiner.join(getRandomRunningNode().getNodeReference());
            runningNodes.add(joiner);
        }

        for (int i = 0; i < K+1; i++) {
            KademliaNode n = getRandomRunningNode();
            n.leave();
            runningNodes.remove(n);
        }
    }
}
