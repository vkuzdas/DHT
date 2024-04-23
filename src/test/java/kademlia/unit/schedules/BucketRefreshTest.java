package kademlia.unit.schedules;

import kademlia.KademliaNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import kademlia.unit.BaseTest;

import java.io.IOException;
import java.time.Duration;

@Disabled("Disable to not waste CI resources")
public class BucketRefreshTest extends BaseTest {

    @Test
    @Disabled("run manually")
    // may throw IO exceptions when shuttingdown during bucket refresh -> refreshInterval and time of test tun must be adjusted
    // refresh=40, bits=20,  nodes=50, sleep=70 - works on CZ laptop
    // refresh=40, bits=160, nodes=40, sleep=70 - throws exceptions on CZ laptop
    // refresh=50, bits=160, nodes=10, sleep=90 - works on CZ laptop
    public void testRefreshBucket() throws IOException, InterruptedException {
        KademliaNode.setRefreshInterval(Duration.ofSeconds(40));
        BITS = 20;
        KademliaNode.setIdLength(BITS);

        for (int i = 0; i < 50; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++);
            if (runningNodes.isEmpty())
                joiner.initKademlia();
            else
                joiner.join(getRandomRunningNode().getNodeReference());
            runningNodes.add(joiner);
        }

        Thread.sleep(1000*70);
    }
}
