package pastry;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pastry.Constants.BASE_16_IDS;

@Disabled("Disable to not waste CI resources")
public class UtilTest {

    Logger logger = LoggerFactory.getLogger(UtilTest.class);

    @Test
    public void testConversion() {
        logger.warn(System.lineSeparator() + System.lineSeparator()
                + "============== " + "testConversion"
                + "() =============" + System.lineSeparator());

        PastryNode.B_PARAMETER = BASE_16_IDS;
        BigInteger dec = new BigInteger("00003151");
        String hex = Util.convertFromDecimal(dec);
        assertEquals("C4F", hex);

        dec = Util.convertToDecimal("C4F");
        assertEquals(new BigInteger("00003151"), dec);
    }

}
