package chord;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Disable to not waste CI resources")
public class BigTest {
    static int FIX_FINGER_TIMEOUT;
    private final ArrayList<ChordNode> runningNodes = new ArrayList<>();
    private static int BASE_PORT = 10_000;

    @BeforeAll
    static void setUp() {
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
        for (ChordNode node : runningNodes) {
            node.shutdown();
        }
        runningNodes.clear();
    }


    @Test
    void test_put_get() throws IOException, InterruptedException {
        ChordNode.STABILIZATION_INTERVAL = 1000;
        ChordNode.m = 100; // size of id has significant impact on speed of RPC calls

        ChordNode bootstrap = new ChordNode("localhost", BASE_PORT++);
        bootstrap.createRing();

        runningNodes.add(bootstrap);
        // start nodes
        for (int i = 1; i < 10 ; i++) { // depends on your machine how many nodes it can run
            ChordNode n = new ChordNode("localhost", BASE_PORT++);
            runningNodes.add(n);
            n.join(bootstrap);
        }

        ArrayList<String> inserted = new ArrayList<>();
        Random rand = new Random();
        int node;
        // put to random node
        for (int i = 0; i < 50; i++) {
            node = rand.nextInt(runningNodes.size());
            runningNodes.get(node).put("key"+i, "value"+i);
            inserted.add("value"+i);
        }

        // two nodes leave
        node = rand.nextInt(runningNodes.size());
        runningNodes.get(node).leave();
        runningNodes.remove(node);

        node = rand.nextInt(runningNodes.size());
        runningNodes.get(node).leave();
        runningNodes.remove(node);

        Thread.sleep(5000);
        System.out.println("Chord stabilized after 5s");


        ArrayList<String> fetched = new ArrayList<>();
        // get
        for (int i = 0; i < 50; i++) {
            node = rand.nextInt(runningNodes.size());
            String v = runningNodes.get(node).get("key"+i);
            assertTrue(inserted.contains(v));
        }
    }

    @Test
    void test_m160_n60() throws IOException {
        BASE_PORT = 11_000;
        ChordNode.STABILIZATION_INTERVAL = 5000;
        ChordNode.m = 160; // size of id has significant impact on speed of RPC calls

        ChordNode bootstrap = new ChordNode("localhost", BASE_PORT++);
        bootstrap.createRing();

        runningNodes.add(bootstrap);
        // start nodes
        for (int i = 0; i < 60; i++) {
            ChordNode n = new ChordNode("localhost", BASE_PORT++);
            runningNodes.add(n);
            n.join(bootstrap);
        }
    }

    @Test
    void test_put_delete() throws IOException, InterruptedException {
        ChordNode.STABILIZATION_INTERVAL = 1000;
        ChordNode.m = 100; // size of id has significant impact on speed of RPC calls

        ChordNode bootstrap = new ChordNode("localhost", BASE_PORT++);
        bootstrap.createRing();

        runningNodes.add(bootstrap);
        // start nodes
        for (int i = 1; i < 10 ; i++) { // depends on your machine how many nodes it can run
            ChordNode n = new ChordNode("localhost", BASE_PORT++);
            runningNodes.add(n);
            n.join(bootstrap);
        }

        ArrayList<String> inserted = new ArrayList<>();
        Random rand = new Random();
        int node;
        // put to random node
        for (int i = 0; i < 50; i++) {
            node = rand.nextInt(runningNodes.size());
            runningNodes.get(node).put("key"+i, "value"+i);
            inserted.add("value"+i);
        }

        // two nodes leave
        node = rand.nextInt(runningNodes.size());
        runningNodes.get(node).leave();
        runningNodes.remove(node);

        node = rand.nextInt(runningNodes.size());
        runningNodes.get(node).leave();
        runningNodes.remove(node);

        Thread.sleep(5000);
        System.out.println("Chord stabilized after 5s");


        ArrayList<String> deleted = new ArrayList<>();
        // delete half of the keys
        for (int i = 0; i < 50; i++) {
            node = rand.nextInt(runningNodes.size());
            runningNodes.get(node).delete("key"+i);
            deleted.add("key"+i);
        }

        for(String key : deleted) {
            node = rand.nextInt(runningNodes.size());
            String v = runningNodes.get(node).get(key);
            assertNull(v);
        }

    }

}
