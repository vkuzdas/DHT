package pastry;

import proto.Pastry;

import java.math.BigInteger;

/**
 * Reference to a PastryNode from the point of view of current node
 */
public class NodeReference {
    public final String ip;
    public final int port;
    public final long x;
    public final long y;
    public long distance;
    public final String id;

    public NodeReference(String ip, int port, long x, long y) {
        this.ip = ip;
        this.port = port;
        this.id = Util.getId(this.getAddress());
        this.x = x;
        this.y = y;
        this.distance = Long.MAX_VALUE;
    }

    public NodeReference(String ip, int port, long x, long y, long distance) {
        this.ip = ip;
        this.port = port;
        this.id = Util.getId(this.getAddress());
        this.x = x;
        this.y = y;
        this.distance = distance;
    }

    public NodeReference(Pastry.NodeReference nodeReference) {
        this.ip = nodeReference.getIp();
        this.port = nodeReference.getPort();
        this.id = Util.getId(this.getAddress());
        this.x = nodeReference.getX();
        this.y = nodeReference.getY();
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getDistance() {
        return distance;
    }

    public String getId() {
        return id;
    }

    public BigInteger getDecimalId() {
        return Util.convertToDecimal(id);
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    public Pastry.NodeReference toProto() {
        return Pastry.NodeReference.newBuilder().setIp(ip).setPort(port).setX(x).setY(y).build();
    }

    @Override
    public String toString() {
        return
//                ip + ":" +
                        port + ":" + id + ":" + getDecimalId() + ":" + distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NodeReference)) {
            return false;
        }
        NodeReference other = (NodeReference) obj;
        return this.port == other.port && this.ip.equals(other.ip);
    }
}
