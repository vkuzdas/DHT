package chord;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * There are some unstable tests, if not sure about the result, run the test separately fixes it
 */
@Disabled("Disable to not waste CI resources")
class ChordNodeTest {

    static int FIX_FINGER_TIMEOUT;
    private final ArrayList<ChordNode> toShutdown = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        ChordNode.m = 4; // Chord space assumption for the test
        ChordNode.STABILIZATION_INTERVAL = 2000; // Chord space assumption for the test
        FIX_FINGER_TIMEOUT = 4*ChordNode.m * ChordNode.STABILIZATION_INTERVAL;
    }

    @BeforeEach
    void init(TestInfo testInfo) {
        System.out.println(System.lineSeparator() + System.lineSeparator()
                + "============== " + testInfo.getTestMethod().map(Method::getName).orElse(null)
                + "() =============" + System.lineSeparator());
    }

    @AfterEach
    void tearDown() {
        toShutdown.forEach(ChordNode::shutdownChordNode);
    }

    private void registerForShutdown(ChordNode ... nodes) {
        Collections.addAll(toShutdown, nodes);
    }

    @Test
    void testNodeFail() throws IOException, InterruptedException {
        ChordNode.STABILIZATION_INTERVAL = 500;
        ChordNode.m = 4;

        ChordNode bootstrap = new ChordNode("localhost", 9000);
        ChordNode n1 = new ChordNode("localhost", 9003);
        ChordNode n2 = new ChordNode("localhost", 9004);
        ChordNode n3 = new ChordNode("localhost", 9005);
        registerForShutdown(bootstrap, n1, n2, n3);

        bootstrap.createRing();

        n1.join(bootstrap);
        n2.join(bootstrap);
        n3.join(bootstrap);

        Thread.sleep(4L * ChordNode.STABILIZATION_INTERVAL); // let network stabilize

        n1.fail();

        Thread.sleep(4L * ChordNode.STABILIZATION_INTERVAL); // let network stabilize again

        assertFalse(bootstrap.containedInFingerTable(n1.getNodeReference()));
        assertFalse(n2.containedInFingerTable(n1.getNodeReference()));
        assertFalse(n3.containedInFingerTable(n1.getNodeReference()));
    }

    @Test
    void testDelete() throws IOException {

        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        registerForShutdown(bootstrap, node2);

        bootstrap.createRing();

        node2.join(bootstrap);

        node2.put("icecream", "sweet");
        bootstrap.put("lollipop", "sour");

        assertEquals(bootstrap.getDataSize() + node2.getDataSize(), 2);

        bootstrap.delete("icecream");
        bootstrap.delete("lollipop");

        assertEquals(bootstrap.getDataSize() + node2.getDataSize(), 0);

        bootstrap.stopServer();
        node2.stopServer();
    }

    @Test
    void testMoveKeys() throws IOException {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        registerForShutdown(bootstrap, node2);

        bootstrap.createRing();

        bootstrap.put("icecream", "sweet");
        bootstrap.put("lollipop", "sour");

        assertEquals(bootstrap.getDataSize(), 2);

        node2.join(bootstrap);

        assertEquals(bootstrap.getDataSize(), 1);
        assertEquals(node2.getDataSize(), 1);

        bootstrap.stopServer();
        node2.stopServer();
    }

    @Test
    void testThreeJoinOnBootstrap() throws Exception {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        ChordNode node3 = new ChordNode("localhost", 8982);
        registerForShutdown(bootstrap, node2, node3);

        bootstrap.createRing();

        node2.join(bootstrap);
        node3.join(bootstrap);

        assertEquals(bootstrap.getNodeReference().id, BigInteger.valueOf(10));
        assertEquals(node2.getNodeReference().id, BigInteger.valueOf(0));
        assertEquals(node3.getNodeReference().id, BigInteger.valueOf(2));

        int timeoutSeconds = 2;

        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getPredecessor().id.equals(BigInteger.valueOf(2)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getSuccessor().id.equals(BigInteger.valueOf(0)));

        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getPredecessor().id.equals(BigInteger.valueOf(10)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getSuccessor().id.equals(BigInteger.valueOf(2)));

        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getPredecessor().id.equals(BigInteger.valueOf(0)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getSuccessor().id.equals(BigInteger.valueOf(10)));

        bootstrap.stopServer();
        node2.stopServer();
        node3.stopServer();
    }

    @Test
    void testChainedJoin() throws IOException {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        ChordNode node3 = new ChordNode("localhost", 8982);
        registerForShutdown(bootstrap, node2, node3);

        bootstrap.createRing();

        node2.join(bootstrap);
        node3.join(node2);

        assertEquals(bootstrap.getNodeReference().id, BigInteger.valueOf(10));
        assertEquals(node2.getNodeReference().id, BigInteger.valueOf(0));
        assertEquals(node3.getNodeReference().id, BigInteger.valueOf(2));

        int timeoutSeconds = 2;

        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getPredecessor().id.equals(BigInteger.valueOf(2)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getSuccessor().id.equals(BigInteger.valueOf(0)));

        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getPredecessor().id.equals(BigInteger.valueOf(10)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getSuccessor().id.equals(BigInteger.valueOf(2)));

        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getPredecessor().id.equals(BigInteger.valueOf(0)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getSuccessor().id.equals(BigInteger.valueOf(10)));

        bootstrap.stopServer();
        node2.stopServer();
        node3.stopServer();
    }

    @Test
    void testLeaveFromBootstrap() throws IOException, InterruptedException {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        ChordNode node3 = new ChordNode("localhost", 8982);
        registerForShutdown(bootstrap, node2, node3);

        bootstrap.createRing();

        node2.join(bootstrap);
        node3.join(bootstrap);

        node2.leave();
        node2.stopServer();


        assertNotEquals(bootstrap.getPredecessor(), node2.getNodeReference());
        assertNotEquals(bootstrap.getSuccessor(), node2.getNodeReference());

        assertNotEquals(node3.getPredecessor(), node2.getNodeReference());
        assertNotEquals(node3.getSuccessor(), node2.getNodeReference());

        bootstrap.stopServer();
        node3.stopServer();
    }

    @Test
    void testChainedLeave() throws IOException {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        ChordNode node3 = new ChordNode("localhost", 8982);
        registerForShutdown(bootstrap, node2, node3);

        bootstrap.createRing();

        node2.join(bootstrap);
        node3.join(node2);

        node2.stopServer();
        node2.leave();

        assertNotEquals(bootstrap.getPredecessor(), node2.getNodeReference());
        assertNotEquals(bootstrap.getSuccessor(), node2.getNodeReference());

        assertNotEquals(node3.getPredecessor(), node2.getNodeReference());
        assertNotEquals(node3.getSuccessor(), node2.getNodeReference());

        bootstrap.stopServer();
        node3.stopServer();
    }

}
