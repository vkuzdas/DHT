package chord;

import java.math.BigInteger;


public class Finger {
    public final BigInteger start;
    public final BigInteger end;
    public NodeReference node; // successor(F[i].start)

    public Finger(BigInteger start, BigInteger end, NodeReference node) {
        this.start = start;
        this.end = end;
        this.node = node;
    }

    public void setNode(NodeReference node) {
        this.node = node;
    }
}
