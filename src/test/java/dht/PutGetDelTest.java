package dht;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pastry.metric.PortDifferenceDistanceCalculator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PutGetDelTest {

    protected static int BASE_PORT = 11_000;
    protected static String LOCAL_IP = "localhost";
    protected final Random random = new Random();
    protected final ArrayList<DHTNode> runningNodes = new ArrayList<>();

    /**
     * Init and run <i>nodes</i> number of nodes of given type. Nodes join using random bootstrap.
     */
    public void runNodesOfType(int nodes, DHTType type) {
        for (int i = 0; i < nodes; i++) {
            DHTNode joiner = new DHTNode(type, LOCAL_IP, BASE_PORT++);
            if (runningNodes.isEmpty())
                joiner.init();
            else
                joiner.join(getRandomRunningNode().getIp(), getRandomRunningNode().getPort());
            runningNodes.add(joiner);
        }
    }

    @Test
    public void testCustomKademliaSettings() {
        DHTNode.setCustomKademliaSettings(5, 6, 10, Duration.ofSeconds(7), Duration.ofSeconds(7), Duration.ofSeconds(7));
        test10_defaultKademlia();
    }

    @Test
    public void testCustomPastrySettings() {
        DHTNode.setCustomPastrySettings(16, 32, new PortDifferenceDistanceCalculator());
        test10_defaultPastry();
    }

    @Test
    public void testCustomChordSettings() {
        DHTNode.setCustomChordSettings(24, 3200);
        test10_defaultPastry();
    }

    @Test
    public void test10_defaultKademlia() {
        runNodesOfType(10, DHTType.Kademlia);

        getRandomRunningNode().put("key", "value");
        assertEquals(getRandomRunningNode().get("key"), "value");

        getRandomRunningNode().delete("key");
        assertNull(getRandomRunningNode().get("key"));
    }

    @Test
    public void test10_defaultPastry() {
        runNodesOfType(10, DHTType.Pastry);

        getRandomRunningNode().put("key", "value");
        assertEquals(getRandomRunningNode().get("key"), "value");

        getRandomRunningNode().delete("key");
        assertNull(getRandomRunningNode().get("key"));
    }

    @Test
    public void test10_defaultChord() {
        runNodesOfType(10, DHTType.Chord);

        getRandomRunningNode().put("key", "value");
        assertEquals(getRandomRunningNode().get("key"), "value");

        getRandomRunningNode().delete("key");
        assertNull(getRandomRunningNode().get("key"));
    }



    protected DHTNode getRandomRunningNode() {
        return runningNodes.get(random.nextInt(runningNodes.size()));
    }

}
