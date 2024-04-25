package pastry;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.Pastry;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DHT API (put, get, delete)
 */
@Disabled("Disable to not waste CI resources")
public class DhtApiTest extends BaseTest {
    private final Logger logger = LoggerFactory.getLogger(DhtApiTest.class);

    @Test
    public void testRandomJoin_RandomPutGet() throws IOException {

        long x = 0;
        long y = 0;

        int j = 0;

        PastryNode bootstrap = new PastryNode("localhost", BASE_PORT++, x++, y++);
        bootstrap.initPastry();
        runningNodes.add(bootstrap);

        for (int i = 0; i < 50 ; i++) {
            PastryNode node = new PastryNode("localhost", BASE_PORT++, x++, y++);
            node.joinPastry(runningNodes.get(new Random().nextInt(runningNodes.size())).getNode());
            runningNodes.add(node);
        }

        // randomly put 50 random keys, assert closest node is owner
        for (int i = 0; i < MAX_KEYS; i++) {
            j++;
            String key = "key" + j;
            PastryNode randomNode = runningNodes.get(new Random().nextInt(runningNodes.size()));

            NodeReference owner = randomNode.putWithNode(key, "value" + j);

            assertNumericallyClosestOfAll(Util.getId(key), owner, runningNodes);
        }

        // get 50 keys
        for (int i = 0; i < MAX_KEYS; i++) {
            String key = "key" + j;

            PastryNode randomNode = runningNodes.get(new Random().nextInt(runningNodes.size()));

            assertEquals("value" + j, randomNode.get(key));

            j--;
        }

        // remove 50 present keys
        for (int i = 0; i < MAX_KEYS; i++) {
            j++;
            String key = "key" + j;

            PastryNode randomNode = runningNodes.get(new Random().nextInt(runningNodes.size()));

            assertEquals(Pastry.ForwardResponse.StatusCode.REMOVED, randomNode.deleteWithStatus(key));
        }

        // remove 10 *not* present keys
        for (int i = 0; i < 10; i++) {
            j++;
            String key = "key" + j;

            PastryNode randomNode = runningNodes.get(new Random().nextInt(runningNodes.size()));

            assertEquals(Pastry.ForwardResponse.StatusCode.NOT_FOUND, randomNode.deleteWithStatus(key));
        }
    }

    private void assertNumericallyClosestOfAll(String keyHash, NodeReference closest, List<PastryNode> nodes) {
        nodes.sort(Comparator.comparing(o -> o.getNode().getDecimalId().subtract(Util.convertToDecimal(keyHash)).abs()));
        assertEquals(nodes.get(0).getNode(), closest, "Actual closest to " + keyHash + " is " + nodes.get(0).getNode() + ", not " + closest);
    }

}
