package kademlia.integration;

import kademlia.KademliaNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

@Disabled("Disable to not waste CI resources")
public class BigTestBase {


    protected static final Logger logger = LoggerFactory.getLogger(BigTestBase.class);
    protected static int BASE_PORT = 11_000;
    protected static String LOCAL_IP = "localhost";
    protected int BITS = 10;
    protected int K = 4;
    protected int ALPHA = 3;
    protected final Random random = new Random();

    protected final ArrayList<KademliaNode> runningNodes = new ArrayList<>();


    @BeforeEach
    public void init(TestInfo testInfo) {
        logger.warn(System.lineSeparator() + System.lineSeparator()+ "============== {} =============" + System.lineSeparator(), testInfo.getDisplayName());
        KademliaNode.setAlpha(ALPHA);
        KademliaNode.setK(K);
        KademliaNode.setIdLength(BITS);
        KademliaNode.setRepublishInterval(Duration.ofDays(1)); // turn off republishing
        KademliaNode.setExpireInterval(Duration.ofDays(1)); // turn off expiring
        KademliaNode.setRefreshInterval(Duration.ofDays(1)); // turn off refreshing
    }

    /**
     * Shutdown all running nodes in order to free UDP ports
     */
    @AfterEach
    public void tearDown() {
        for (KademliaNode node : runningNodes) {
            node.shutdown();
        }
        runningNodes.clear();
    }

    protected KademliaNode getRandomRunningNode() {
        return runningNodes.get(random.nextInt(runningNodes.size()));
    }

    protected BigInteger getRandomId() {
        return new BigInteger(""+random.nextInt((int)Math.pow(2,BITS)));
    }
}
