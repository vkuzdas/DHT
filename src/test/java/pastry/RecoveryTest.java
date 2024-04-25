package pastry;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Disable to not waste CI resources")
public class RecoveryTest extends BaseTest {

    private final Logger logger = LoggerFactory.getLogger(DhtApiTest.class);


    @Test
    public void testLeaveStabilize_MoveKeys() throws IOException, InterruptedException {
        PastryNode.setStabiliation(true);
        PastryNode.STABILIZATION_INTERVAL = 1500;
        PastryNode bootstrap = new PastryNode("localhost", BASE_PORT++, 0, 0);
        bootstrap.initPastry();
        runningNodes.add(bootstrap);

        for (int i = 0; i < 10; i++) {
            PastryNode node = new PastryNode("localhost", BASE_PORT++, 0, 0);
            node.joinPastry(bootstrap.getNode());
            runningNodes.add(node);
        }

        List<BigInteger> keys = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            bootstrap.put("key" + i, "value");
            keys.add(Util.convertToDecimal(Util.getId("key" + i)));
        }


        for (int i = 0; i < 5; i++) {
            Thread.sleep(PastryNode.STABILIZATION_INTERVAL * 2L);
            PastryNode node = runningNodes.get(new Random().nextInt(runningNodes.size()));
            node.leave();
            runningNodes.remove(node);
        }


        for (BigInteger key : keys) {
            PastryNode closest = runningNodes.stream().min(Comparator.comparing(n -> n.getNode().getDecimalId().subtract(key).abs())).get();
            assertNotNull(closest.getLocalData().get(key), "Expected " + key + " to be in closest node " + closest.getNode());
        }

    }



    @Test
    public void testLeaveInSuccession_MoveKeys() throws IOException {
        PastryNode bootstrap = new PastryNode("localhost", BASE_PORT++, 0, 0);
        bootstrap.initPastry();
        runningNodes.add(bootstrap);

        for (int i = 0; i < 10; i++) {
            PastryNode node = new PastryNode("localhost", BASE_PORT++, 0, 0);
            node.joinPastry(bootstrap.getNode());
            runningNodes.add(node);
        }

        List<BigInteger> keys = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            bootstrap.put("key" + i, "value");
            keys.add(Util.convertToDecimal(Util.getId("key" + i)));
        }

        // eventual delivery is guaranteed unless |L|/2 nodes with adjacent nodeIds fail simultaneously
        for (int i = 0; i < 5; i++) {
            PastryNode node = runningNodes.get(new Random().nextInt(runningNodes.size()));
            node.leave();
            runningNodes.remove(node);
        }

        for (BigInteger key : keys) {
            PastryNode closest = runningNodes.stream().min(Comparator.comparing(n -> n.getNode().getDecimalId().subtract(key).abs())).get();
            assertNotNull(closest.getLocalData().get(key), "Expected " + key + " to be in closest node " + closest.getNode());
        }

    }

    @Test
    public void testRemoveFailed() throws IOException, InterruptedException {
        PastryNode.setStabiliation(true);
        PastryNode.STABILIZATION_INTERVAL = 1500;

        PastryNode bootstrap = new PastryNode("localhost", BASE_PORT++, 0, 0);
        bootstrap.initPastry();

        PastryNode node1 = new PastryNode("localhost", BASE_PORT++, 50, 50);
        node1.joinPastry(bootstrap.getNode());

        PastryNode node2 = new PastryNode("localhost", BASE_PORT++, 100, 100);
        node2.joinPastry(bootstrap.getNode());

        PastryNode node3 = new PastryNode("localhost", BASE_PORT++, 150, 150);
        node3.joinPastry(bootstrap.getNode());

        registerAllRunningNodes(bootstrap, node1, node2, node3);

        Thread.sleep(PastryNode.STABILIZATION_INTERVAL * 2L);
        node2.fail();
        Thread.sleep(PastryNode.STABILIZATION_INTERVAL * 2L);

        // assert that node2 was deleted from all NodeStates
        bootstrap.getAllNodes().forEach(node -> assertNotEquals(node2.getNode().getId(), node.getId()));
        node1.getAllNodes().forEach(node -> assertNotEquals(node2.getNode().getId(), node.getId()));
        node3.getAllNodes().forEach(node -> assertNotEquals(node2.getNode().getId(), node.getId()));
    }
}
