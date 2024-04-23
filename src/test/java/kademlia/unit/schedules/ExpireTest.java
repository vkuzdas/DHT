package kademlia.unit.schedules;

import kademlia.KademliaNode;
import kademlia.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import kademlia.unit.BaseTest;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit test for expiration tasting in isolation (without republishing)
 */
@Disabled("Disable to not waste CI resources")
public class ExpireTest extends BaseTest {

    // TODO: this probably goes against single node republishing
    // TODO: @Override public void store(Kademlia.StoreRequest request) will need some readjusting in order to support this along with republishing

    private final Duration expireInterval = Duration.ofSeconds(3);

    @BeforeEach
    @Override
    public void init(TestInfo testInfo) {
        super.init(testInfo);

        BITS = 10;
        KademliaNode.setIdLength(BITS);

        KademliaNode.setExpireInterval(expireInterval);
    }


    /**
     * 1. Join with K+1 nodes <br>
     * 2. Put a key on them <br>
     * 3. Validate presence <br>
     * 4. Wait for key expiration <br>
     * 5. Validate absence <br>
     */
    @Test
    public void testExpire() throws IOException {
        for (int i = 0; i < K+1; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.valueOf(i));

            if (runningNodes.isEmpty())
                joiner.initKademlia();
            else
                joiner.join(getRandomRunningNode().getNodeReference());

            runningNodes.add(joiner);
        }

        // PUT
        getRandomRunningNode().put("key1", "val1");

        // GET
        getRandomRunningNode().get("key1");

        // WAIT FOR EXPIRATION
        await().atMost((long) (1.5*expireInterval.toMillis()), TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertNull(runningNodes.get(3).getLocalData().get(Util.getId("key"))));
    }
}
