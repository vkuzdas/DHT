package pastry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static int getSharedPrefixLength(String idBase, String selfId) {
        int l = 0;
        for (int i = 0; i < idBase.length(); i++) {
            if (idBase.charAt(i) == selfId.charAt(i)) {
                l++;
            } else {
                break;
            }
        }
        return l;
    }

    /**
     * Given node.address, hash it and return the hash as 4/16-base string number
     */
    public static String getId(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
            return BigInteger.ONE.negate().toString();
        }

        BigInteger decimal_128bit = new BigInteger(1, md.digest(input.getBytes(StandardCharsets.UTF_8)));

        int maxBits = PastryNode.B_PARAMETER * PastryNode.L_PARAMETER;
        BigInteger maxId = BigInteger.valueOf(2L).pow(maxBits);
        BigInteger decimal_maxBit = decimal_128bit.mod(maxId);

        String base_maxBit = convertFromDecimal(decimal_maxBit);
        int padding = PastryNode.L_PARAMETER - base_maxBit.length();
        if (padding > 0) {
            StringBuilder paddingBuilder = new StringBuilder();
            for (int i = 0; i < padding; i++) {
                paddingBuilder.append("0");
            }
            base_maxBit = paddingBuilder + base_maxBit;
        }
        return base_maxBit;
    }

    /**
     * To-Base is determined by {@link PastryNode#B_PARAMETER}
     */
    public static String convertFromDecimal(BigInteger decimalValue) {
        StringBuilder r = new StringBuilder();
        BigInteger baseValue = BigInteger.valueOf((int)Math.pow(2, PastryNode.B_PARAMETER));

        while (decimalValue.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] quotientAndRemainder = decimalValue.divideAndRemainder(baseValue);
            int remainder = quotientAndRemainder[1].intValue();
            if (remainder < 10) {
                r.insert(0, remainder);
            } else {
                r.insert(0, (char)('A'+remainder-10));
            }
            decimalValue = quotientAndRemainder[0];
        }

        return r.toString();
    }


    /**
     * From-Base is determined by {@link PastryNode#B_PARAMETER}
     */
    public static BigInteger convertToDecimal(String strNumber) {
        BigInteger r = BigInteger.ZERO;
        for (int i = 0; i < strNumber.length(); i++) {
            char dig = strNumber.charAt(i);
            int d,e;
            if (dig <= '9') {
                d = dig - '0';
                e = strNumber.length()-i-1;
            } else {
                d = dig - 'A'+10;
                e = strNumber.length()-i-1;
            }
            BigInteger m = BigInteger.valueOf(2).pow(PastryNode.B_PARAMETER).pow(e);
            r = r.add(BigInteger.valueOf(d).multiply(m));
        }
        return r;
    }

    public static int parseDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else {
            return c - 'A' + 10;
        }
    }
}
