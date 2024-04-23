package pastry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

import static pastry.Constants.BASE_4_IDS;
import static pastry.Constants.LEAF_SET_SIZE_8;

/**
 * Base test class to be extended
 */
@Disabled("Disable to not waste CI resources")
public class BaseTest {

    protected final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected static int BASE_PORT = 10_000;
    protected int MAX_NODES;
    protected int MAX_KEYS;

    /**
     * All running nodes are shutdown after each test
     */
    protected final ArrayList<PastryNode> runningNodes = new ArrayList<>();

    @BeforeEach
    public void printInfo(TestInfo testInfo) {
        logger.warn(System.lineSeparator() + System.lineSeparator()+ "============== {} =============" + System.lineSeparator(), testInfo.getDisplayName());
    }

    /**
     * Whether the test is running in Continuous Integration environment
     */
    public static boolean isRunningInCI() {
        String ci = System.getenv("CI");
        return "true".equals(ci);
    }

    @BeforeEach
    public void init() {
        PastryNode.setStabiliation(false);
        PastryNode.setBase(BASE_4_IDS);
        PastryNode.setLeafSize(LEAF_SET_SIZE_8);
        MAX_NODES = isRunningInCI() ? 10 : 50;
        MAX_KEYS = 2 * MAX_NODES;
    }

    @AfterEach
    public void tearDown() {
        for (PastryNode node : runningNodes) {
            node.shutdownPastryNode();
        }
        runningNodes.clear();
    }

    public void registerAllRunningNodes(PastryNode ... nodes) {
        this.runningNodes.addAll(Arrays.asList(nodes));
    }


}
