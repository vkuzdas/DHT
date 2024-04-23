package chord;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Util {

    static boolean inRange_OpenOpen(BigInteger id, BigInteger start, BigInteger end) {
        return inRange_CloseOpen(id, start, end) && !id.equals(start);
    }

    static boolean inRange_CloseOpen(BigInteger id, BigInteger start, BigInteger end) {
        // id ∈ [start, end) % 2^m
        if(start.compareTo(end) > 0) {
            return !inRange_CloseOpen(id, end, start);
        }
        return id.compareTo(start) >= 0 && id.compareTo(end) < 0;
    }

    static boolean inRange_OpenClose(BigInteger id, BigInteger start, BigInteger end) {
        // id ∈ (start, end] % 2^m
        if(start.compareTo(end) > 0) {
            return !inRange_OpenClose(id, end, start);
        }
        return id.compareTo(start) > 0 && id.compareTo(end) <= 0;
    }

    /**
     * Calculate the SHA-1 hash of the input string and return the result as a BigInteger
     * @param input key or address of node
     * @param m number of bits in the hash
     * @return SHA-1 hash of the input string
     */
    public static BigInteger calculateSHA1(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
            return BigInteger.ONE.negate();
        }

        BigInteger lastId = BigInteger.valueOf(2L).pow(ChordNode.m).add(BigInteger.ONE.negate());
        return new BigInteger(1, md.digest(input.getBytes(StandardCharsets.UTF_8)))
                .mod(lastId);
    }

}
