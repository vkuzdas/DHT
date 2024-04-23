package pastry;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pastry.metric.CoordinateDistanceCalculator;
import pastry.metric.DistanceCalculator;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for joining nodes
 */
@Disabled("Disable to not waste CI resources")
public class JoinTest extends BaseTest {

    private final Logger logger = LoggerFactory.getLogger(DhtApiTest.class);

    @Test
    public void testJoin_MoveKeys() throws IOException {
        PastryNode bootstrap = new PastryNode("localhost", BASE_PORT++, 0, 0);
        bootstrap.initPastry();
        runningNodes.add(bootstrap);

        List<BigInteger> keys = new ArrayList<>();

        for (int i = 0; i < MAX_KEYS; i++) {
            bootstrap.put("key" + i, "value");
            keys.add(Util.convertToDecimal(Util.getId("key" + i)));
        }

        assertEquals(MAX_KEYS, bootstrap.getLocalData().size(), "Expected all keys in bootstrap");


        for (int i = 0; i < MAX_NODES; i++) {
            PastryNode node = new PastryNode("localhost", BASE_PORT++, 0, 0);
            node.joinPastry(bootstrap.getNode());
            runningNodes.add(node);
        }

        for (BigInteger key : keys) {
            PastryNode closest = runningNodes.stream().min(Comparator.comparing(n -> n.getNode().getDecimalId().subtract(key).abs())).get();
            assertNotNull(closest.getLocalData().get(key), "Expected key to be in closest node");
        }
    }

    @Test
    @Disabled("Maven cannot use Coord calculator for some reason, local manual run should work")
    public void testMetric_routingTableRewritten() throws IOException {
        PastryNode.setDefaultCalculator(new CoordinateDistanceCalculator());

        // 10000:13202231
        PastryNode bootstrap = new PastryNode("localhost", BASE_PORT, 0, 0);
        bootstrap.initPastry();


        //10002:02032101
        PastryNode node1 = new PastryNode("localhost", 10_002, 1654, 1321);
        node1.joinPastry(bootstrap.getNode());
        assertEquals(node1.getNode(), bootstrap.getRoutingTable().get(0).get(0), "Expected node1");

        //10007:00012102 (should overwrite node1)
        PastryNode node2 = new PastryNode("localhost", 10_007, 964, 912);
        node2.joinPastry(bootstrap.getNode());
        assertEquals(node2.getNode(), bootstrap.getRoutingTable().get(0).get(0), "Expected node2 to rewrite node1");



        //10001:33130012
        PastryNode node3 = new PastryNode("localhost", 10_001, 445, 401);
        node3.joinPastry(bootstrap.getNode());
        assertEquals(node3.getNode(), bootstrap.getRoutingTable().get(0).get(3), "Expected node3");


        //10009:33331223 (should overwrite node3)
        PastryNode node4 = new PastryNode("localhost", 10_009, 390, 303);
        node4.joinPastry(bootstrap.getNode());
        assertEquals(node4.getNode(), bootstrap.getRoutingTable().get(0).get(3), "Expected node4 to rewrite node3");


        registerAllRunningNodes(bootstrap, node1, node2, node3, node4);

        DistanceCalculator calculator = new CoordinateDistanceCalculator();

        // verify that distance in nodeState is calculated correctly
        bootstrap.getAllNodes().forEach(computed -> {
            PastryNode constructed = runningNodes.stream().filter(n -> n.getNode().equals(computed)).findFirst().get();
            assertEquals(calculator.calculateDistance(bootstrap.getNode(), constructed.getNode()), computed.getDistance());
        });
        node1.getAllNodes().forEach(computed -> {
            PastryNode constructed = runningNodes.stream().filter(n -> n.getNode().equals(computed)).findFirst().get();
            assertEquals(calculator.calculateDistance(node1.getNode(), constructed.getNode()), computed.getDistance());
        });
        node4.getAllNodes().forEach(computed -> {
            PastryNode constructed = runningNodes.stream().filter(n -> n.getNode().equals(computed)).findFirst().get();
            assertEquals(calculator.calculateDistance(node4.getNode(), constructed.getNode()), computed.getDistance());
        });
    }


    @Test
    public void testBootstrapJoin_alwaysJoinClosest() throws IOException {
        PastryNode bootstrap = new PastryNode("localhost", BASE_PORT++, 0, 0);
        bootstrap.initPastry();
        runningNodes.add(bootstrap);

        for (int i = 0; i < MAX_NODES ; i++) {
            PastryNode node = new PastryNode("localhost", BASE_PORT++, 0, 0);
            NodeReference closest = node.joinPastry(bootstrap.getNode());
            assertNumericallyClosestOfAll(node.getNode(), closest, runningNodes);
            runningNodes.add(node);
        }
    }

    @Test
    public void testRandomJoin_alwaysJoinClosest() throws IOException {
        PastryNode bootstrap = new PastryNode("localhost", BASE_PORT++, 0, 0);
        bootstrap.initPastry();
        runningNodes.add(bootstrap);

        for (int i = 0; i < MAX_NODES; i++) {
            PastryNode node = new PastryNode("localhost", BASE_PORT++, 0, 0);
            NodeReference closest = node.joinPastry(runningNodes.get(new Random().nextInt(runningNodes.size())).getNode());
            assertNumericallyClosestOfAll(node.getNode(), closest, runningNodes);
            runningNodes.add(node);
        }
    }

    private void assertNumericallyClosestOfAll(NodeReference node, NodeReference closest, List<PastryNode> nodes) {
        nodes.sort(Comparator.comparing(o -> o.getNode().getDecimalId().subtract(node.getDecimalId()).abs()));
        assertEquals(nodes.get(0).getNode(), closest, "Actual closest to " + node + " is " + nodes.get(0).getNode() + ", not " + closest);
    }

}
