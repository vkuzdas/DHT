package pastry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import static pastry.Constants.BASE_16_IDS;
import static pastry.Constants.LEAF_SET_SIZE_32;

@Disabled("Works locally but not in CI")
public class RecoveryHexTest extends RecoveryTest {

        @Override
        @BeforeEach
        public void init() {
            PastryNode.setStabiliation(true);
            PastryNode.STABILIZATION_INTERVAL = 1500;
            PastryNode.setB(BASE_16_IDS);
            PastryNode.setLeafSize(LEAF_SET_SIZE_32);
            MAX_NODES = isRunningInCI() ? 10 : 50;
            MAX_KEYS = 2 * MAX_NODES;
        }
}
