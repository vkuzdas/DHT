package kademlia.integration;

import kademlia.KademliaNode;
import kademlia.NodeReference;
import kademlia.Util;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("Disable to not waste CI resources")
public class BigTest extends BigTestBase {


    /**
     * If you want to test-run for a certain amount of time, just put Thread.sleep() at the end of the test
     */
    @Test
    @Disabled("Manual run, dont waste CI resources")
    public void testCustomRun() throws IOException, InterruptedException {
        KademliaNode.setK(K);
        KademliaNode.setAlpha(ALPHA);
        KademliaNode.setIdLength(160);
        KademliaNode.setRepublishInterval(Duration.ofSeconds(7));
        KademliaNode.setRefreshInterval(Duration.ofSeconds(7));
        KademliaNode.setExpireInterval(Duration.ofSeconds(7));


        for (int i = 0; i < K+1; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.valueOf(i));
            if (runningNodes.isEmpty())
                joiner.initKademlia();
            else
                joiner.join(getRandomRunningNode().getNodeReference());
            runningNodes.add(joiner);
        }

        Thread.sleep(60000);
    }

    /**
     * Put value onto bootstrap, validate that it will be carried to XOR-closer nodes through republish-expire
     */
    @Test
    public void testConvergeRepublishOntoClosest() throws IOException {
        BITS = 10;
        KademliaNode.setIdLength(BITS);
        int tExpire = 4;
        int tRepublish = 5;
        KademliaNode.setExpireInterval(Duration.ofSeconds(tExpire));
        KademliaNode.setRepublishInterval(Duration.ofSeconds(tRepublish)); // no republication

        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();
        String KEY = "key6";
        BigInteger keyHash = Util.getId(KEY); // 1010000000
        bootstrap.put(KEY, "val1");

        assertEquals(1, bootstrap.getLocalData().size());

        for (int i = 0; i < K; i++) {
            BigInteger nextId = keyHash.setBit(i); // this way, the id will be XOR-closer to keyhash
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, nextId);
            joiner.join(bootstrap.getNodeReference());
            runningNodes.add(joiner);
        }

        await().atMost(tRepublish+1, TimeUnit.SECONDS).untilAsserted(() -> {
            for (int i = 1; i < K+1; i++) {
                KademliaNode curr = runningNodes.get(i);
                assertEquals(1, curr.getLocalData().size());
                assertTrue(getGloballyXORClosest(KEY).contains(curr.getNodeReference()));
            }
            assertEquals(0, bootstrap.getLocalData().size());
            assertFalse(getGloballyXORClosest(KEY).contains(bootstrap.getNodeReference()));
        });
    }

    @Test
    public void testConvergeRepublishOntoClosest_parameterChange() throws IOException {
        BITS = 10;
        KademliaNode.setIdLength(BITS);
        ALPHA = 9;
        KademliaNode.setAlpha(ALPHA);
        K = 10;
        KademliaNode.setK(K);
        int tExpire = 4;
        int tRepublish = 5;
        KademliaNode.setExpireInterval(Duration.ofSeconds(tExpire));
        KademliaNode.setRepublishInterval(Duration.ofSeconds(tRepublish)); // no republication

        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();
        String KEY = "key6";
        BigInteger keyHash = Util.getId(KEY); // 1010000000
        bootstrap.put(KEY, "val1");

        assertEquals(1, bootstrap.getLocalData().size());

        for (int i = 0; i < K; i++) {
            BigInteger nextId = keyHash.setBit(i); // this way, the id will be XOR-closer to keyhash
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, nextId);
            joiner.join(bootstrap.getNodeReference());
            runningNodes.add(joiner);
        }

        await().atMost(tRepublish+1, TimeUnit.SECONDS).untilAsserted(() -> {
            for (int i = 1; i < K+1; i++) {
                KademliaNode curr = runningNodes.get(i);
                assertEquals(1, curr.getLocalData().size());
                assertTrue(getGloballyXORClosest(KEY).contains(curr.getNodeReference()));
            }
            assertEquals(0, bootstrap.getLocalData().size());
            assertFalse(getGloballyXORClosest(KEY).contains(bootstrap.getNodeReference()));
        });
    }

    @Test
    public void testPutGetExpire50() throws IOException {
        BITS = 160;
        KademliaNode.setIdLength(BITS);
        int tExpire = 6;
        KademliaNode.setExpireInterval(Duration.ofSeconds(tExpire));
        KademliaNode.setRepublishInterval(Duration.ofDays(1)); // no republication

        for (int i = 0; i < 50; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++);
            if (runningNodes.isEmpty())
                joiner.initKademlia();
            else
                joiner.join(getRandomRunningNode().getNodeReference());
            runningNodes.add(joiner);
        }
        for (int i = 0; i < 50; i++) {
            getRandomRunningNode().put("key_"+i,"val_"+i);
        }
        for (int i = 0; i < 50; i++) {
            assertEquals("val_"+i, getRandomRunningNode().get("key_"+i));
        }

        // Expire all
        await().atMost(tExpire, TimeUnit.SECONDS).untilAsserted(() -> {
            for (int i = 0; i < 50; i++) {
                assertEquals(0, runningNodes.get(0).getLocalData().size());
            }
        });
    }

    @Test
    public void testPutGetExpire50_parameterChange() throws IOException {
        BITS = 160;
        KademliaNode.setIdLength(BITS);
        ALPHA = 9;
        KademliaNode.setAlpha(ALPHA);
        K = 15;
        KademliaNode.setK(K);
        int tExpire = 6;
        KademliaNode.setExpireInterval(Duration.ofSeconds(tExpire));
        KademliaNode.setRepublishInterval(Duration.ofDays(1)); // no republication

        for (int i = 0; i < 50; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++);
            if (runningNodes.isEmpty())
                joiner.initKademlia();
            else
                joiner.join(getRandomRunningNode().getNodeReference());
            runningNodes.add(joiner);
        }
        for (int i = 0; i < 50; i++) {
            getRandomRunningNode().put("key_"+i,"val_"+i);
        }
        for (int i = 0; i < 50; i++) {
            assertEquals("val_"+i, getRandomRunningNode().get("key_"+i));
        }

        // Expire all
        await().atMost(tExpire, TimeUnit.SECONDS).untilAsserted(() -> {
            for (int i = 0; i < 50; i++) {
                assertEquals(0, runningNodes.get(0).getLocalData().size());
            }
        });
    }



    /**
     * Fail of original publisher should result in expiration of published key
     */
    @Test
    public void testPublisherFail() throws IOException {
        BITS = 160;
        KademliaNode.setIdLength(BITS);
        int tRepublish = 5;
        KademliaNode.setRepublishInterval(Duration.ofSeconds(tRepublish));
        int tRefresh = 7;
        KademliaNode.setRefreshInterval(Duration.ofSeconds(tRefresh));
        int tExpire = 7;
        KademliaNode.setExpireInterval(Duration.ofSeconds(tExpire));


        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        bootstrap.put("KEY", "val1");

        assertEquals(1, bootstrap.getLocalData().size());

        for (int i = 0; i < K-1; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++);
            joiner.join(getRandomRunningNode().getNodeReference());
            runningNodes.add(joiner);
        }

        await().atMost(tRepublish+1, TimeUnit.SECONDS).untilAsserted(() -> {
            for (KademliaNode node : runningNodes) {
                assertEquals(1, node.getLocalData().size());
            }
        });

        bootstrap.shutdown();
        runningNodes.remove(bootstrap);


        await().atMost(tExpire+1, TimeUnit.SECONDS).untilAsserted(() -> {
            for (KademliaNode node : runningNodes) {
                assertEquals(0, node.getLocalData().size());
            }
        });
    }


    @Test
    public void testPublisherFail_parameterChange() throws IOException {
        BITS = 160;
        KademliaNode.setIdLength(BITS);
        ALPHA = 9;
        KademliaNode.setAlpha(ALPHA);
        K = 15;
        KademliaNode.setK(K);
        int tRepublish = 5;
        KademliaNode.setRepublishInterval(Duration.ofSeconds(tRepublish));
        // when there is 160 buckets, refresh takes a long time
//        int tRefresh = 7;
//        KademliaNode.setRefreshInterval(Duration.ofSeconds(tRefresh));
        int tExpire = 7;
        KademliaNode.setExpireInterval(Duration.ofSeconds(tExpire));


        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        bootstrap.put("KEY", "val1");

        assertEquals(1, bootstrap.getLocalData().size());

        for (int i = 0; i < K-1; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++);
            joiner.join(getRandomRunningNode().getNodeReference());
            runningNodes.add(joiner);
        }

        await().atMost(tRepublish+1, TimeUnit.SECONDS).untilAsserted(() -> {
            for (KademliaNode node : runningNodes) {
                assertEquals(1, node.getLocalData().size());
            }
        });

        bootstrap.shutdown();
        runningNodes.remove(bootstrap);


        await().atMost(tExpire+1, TimeUnit.SECONDS).untilAsserted(() -> {
            for (KademliaNode node : runningNodes) {
                assertEquals(0, node.getLocalData().size());
            }
        });
    }

    @Test
    public void test50() throws IOException {
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
        for (int i = 0; i < 100; i++) {
            getRandomRunningNode().put("key_"+i,"val_"+i);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals("val_"+i, getRandomRunningNode().get("key_"+i));
        }
        for (int i = 0; i < 100; i++) {
            getRandomRunningNode().delete("key_"+i);
        }
        runningNodes.forEach(node -> {
            if (!node.getLocalData().isEmpty()) {
                logger.error("Node "+node.getNodeReference().getId() + " has data: {}", node.getLocalData());
            }
            assertEquals(0, node.getLocalData().size(), "Node "+node.getNodeReference().getId() + " has data");
        });
    }

    private List<NodeReference> getGloballyXORClosest(String keyHash) {
        return runningNodes.stream()
                .map(KademliaNode::getNodeReference)
                .sorted(Comparator.comparing(node -> Util.getId(keyHash).xor(node.getId())))
                .limit(K)
                .collect(Collectors.toList());
    }
}
