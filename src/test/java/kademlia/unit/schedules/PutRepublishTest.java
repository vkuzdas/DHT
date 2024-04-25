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
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * Unit test for republish testing in isolation (without expiration) <br> <br>
 * From the paper: <i>To ensure the persistence of key-value pairs, nodes must periodically republish
 * keys. Otherwise, two phenomena may cause lookups for valid keys to fail. First, some of the k
 * nodes that initially get a key-value pair when it is published may leave the network. Second,
 * new nodes may join the network with IDs closer to some published key than the nodes on which
 * the key-value pair was originally published.</i>
 */
@Disabled("Disable to not waste CI resources")
public class PutRepublishTest extends BaseTest {

    private final Duration republishInterval = Duration.ofSeconds(3);

    @BeforeEach
    @Override
    public void init(TestInfo testInfo) {
        super.init(testInfo);

        BITS = 10;
        KademliaNode.setIdLength(BITS);

        KademliaNode.setRepublishInterval(republishInterval);
    }

    /**
     * <i>"New nodes may join the network with IDs closer to some published key than the nodes on which
     * the key-value pair was originally published"</i> <br><br>
     * Validate that key is republished on K closest nodes after specified republish interval <br>
     * 1. put a key on a Single node <br>
     * 2. join with other node <br>
     * 3. wait for republish interval <br>
     * 4. validate that key is on newly joined <br>
     */
    @Test
    public void testPutJoinRepublish() throws IOException {
        Duration interval = Duration.ofSeconds(3);
        KademliaNode.setRepublishInterval(interval);

        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        bootstrap.initKademlia();
        bootstrap.put("key", "value");

        KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ONE);
        joiner.join(bootstrap.getNodeReference());

        // no data before republish
        assertNull(joiner.getLocalData().get(Util.getId("key")));

        // has data after republish
        await().atMost(interval.toMillis(), TimeUnit.MILLISECONDS).untilAsserted(() -> {
                    assertEquals("value", joiner.getLocalData().get(Util.getId("key")));
                });
    }



    /**
     * <i>"Some of the k nodes that initially get a key-value pair when it is published may leave the network."</i> <br><br>
     * Validate that key is republished on K closest nodes after specified republish interval <br>
     * 1. initialize with K+1 nodes <br>
     * 2. put a key into DHT <br>
     * 3. simulate fail on one of the storing node (not the original publisher!) <br>
     * 4. wait for republish interval <br>
     * 5. validate that key is on K XOR-closest nodes <br>
     */
    @Test
    public void testPutLeaveRepublish() throws IOException {
        for (int i = 0; i < K+1; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.valueOf(i));
            if (runningNodes.isEmpty())
                joiner.initKademlia();
            else
                joiner.join(getRandomRunningNode().getNodeReference());
            runningNodes.add(joiner);
        }
        runningNodes.get(2).put("key", "value");

        // the last node (id=4) should be the farthest and therefore not contain the key initially
        assertNull(runningNodes.get(runningNodes.size()-1).getLocalData().get(Util.getId("key")));

        // simulate fail on one of the storing nodes
        runningNodes.get(1).shutdown();
        runningNodes.remove(1);

        // after the republish interval, key should be present on the last
        // node since it is now globally one of the K XOR-closest nodes
        await().atMost((long) (1.5*republishInterval.toMillis()), TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertNotNull(runningNodes.get(runningNodes.size()-1).getLocalData().get(Util.getId("key"))));
    }
}
