package dht;

import chord.ChordNode;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pastry.PastryNode;
import pastry.metric.PortDifferenceDistanceCalculator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LeaveTest {

    protected static final Logger logger = LoggerFactory.getLogger(LeaveTest.class);
    protected static int BASE_PORT = 11_200;
    protected static String LOCAL_IP = "localhost";
    protected final Random random = new Random();
    protected final ArrayList<DHTNode> runningNodes = new ArrayList<>();


    @BeforeEach
    public void init(TestInfo testInfo) {
        logger.warn(System.lineSeparator() + System.lineSeparator()+ "============== {} =============" + System.lineSeparator(), testInfo.getDisplayName());
    }

    @AfterEach
    public void tearDown() {
        for (DHTNode node : runningNodes) {
            node.shutdown();
        }
        runningNodes.clear();
    }

    @Test
    public void testCustomKademliaSettings() {
        DHTNode.setCustomKademliaSettings(2, 3, 160, Duration.ofSeconds(3), Duration.ofSeconds(3), Duration.ofSeconds(3));
        testRandomLeave(10, DHTType.Kademlia);
    }

    @Test
    public void testCustomPastrySettings() {
        DHTNode.setCustomPastrySettings(16, 32, new PortDifferenceDistanceCalculator());
        testRandomLeave(10, DHTType.Pastry);
    }

    @Test
    public void testCustomChordSettings() {
        DHTNode.setCustomChordSettings(30, 4200);
        testRandomLeave(10, DHTType.Chord);
    }

    @Test
    @Order(1)
    public void testRandomFailKademlia() {
        testRandomLeave(10, DHTType.Kademlia);
    }

    @Test
    @Order(2)
    public void testRandomFailPastry() {
        testRandomLeave(10, DHTType.Pastry);
    }

    @Test
    @Order(3)
    public void testRandomFailChord() {
        testRandomLeave(10, DHTType.Chord);
    }


    /**
     * Initialize and run <i>nodes</i> number of nodes of given type. Nodes join using random bootstrap.
     */
    public void testRandomLeave(int nodes, DHTType type) {

        long stabilizationTimeout = 0;

        switch (type) {
            case Kademlia:
                // no wait needed in Kademlia
                break;
            case Pastry:
                stabilizationTimeout = (long)(PastryNode.STABILIZATION_INTERVAL * 1.2);
                break;
            case Chord:
                stabilizationTimeout = (long)(ChordNode.STABILIZATION_INTERVAL * 1.2);
                break;
        }

        for (int i = 0; i < nodes; i++) {
            DHTNode joiner = new DHTNode(type, LOCAL_IP, BASE_PORT++);
            if (runningNodes.isEmpty())
                joiner.init();
            else
                joiner.join(getRandomRunningNode().getIp(), getRandomRunningNode().getPort());
            runningNodes.add(joiner);
        }


        DHTNode departingNode = getRandomRunningNode();
        departingNode.put("key", "value");
        departingNode.leave();
        runningNodes.remove(departingNode);

        try {
            Thread.sleep(stabilizationTimeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // validate that left nodes' data is still accessible
        assertEquals(getRandomRunningNode().get("key"), "value");
    }

    protected DHTNode getRandomRunningNode() {
        return runningNodes.get(random.nextInt(runningNodes.size()));
    }

}
