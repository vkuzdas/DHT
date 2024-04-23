package chord;

import java.math.BigInteger;

import static chord.Util.calculateSHA1;

/**
 * Reference holder for a single node in the Chord network.
 */
public class NodeReference {
    public final String ip;
    public final int port;
    public final BigInteger id;

    public NodeReference(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.id = calculateSHA1(this.getAddress());
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    @Override
    public String toString() {
        return /*ip + ":" +*/ port + ":" + id;
    }

    @Override
    public boolean equals(Object obj) {
        NodeReference other = (NodeReference) obj;
        return this.port == other.port && this.ip.equals(other.ip);
    }
}
