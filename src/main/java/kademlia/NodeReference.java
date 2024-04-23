package kademlia;


import com.google.common.annotations.VisibleForTesting;
import proto.Kademlia;

import java.math.BigInteger;

/**
 * Reference to a PastryNode from the point of view of current node
 */
public class NodeReference {
    public final String ip;
    public final int port;
    private final BigInteger id;

    public NodeReference(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.id = Util.getId(getAddress());
    }

    @VisibleForTesting
    public NodeReference(String ip, int port, BigInteger id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }

    public NodeReference(Kademlia.NodeReference nodeReference) {
        this.ip = nodeReference.getIp();
        this.port = nodeReference.getPort();
        this.id = new BigInteger(nodeReference.getId());
    }

    public Kademlia.NodeReference toProto() {
        return Kademlia.NodeReference.newBuilder()
                .setIp(ip)
                .setPort(port)
                .setId(id.toString()).build();
    }

    public BigInteger getId() {
        return id;
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }


    @Override
    public String toString() {
        return
//                ip + ":" +
                        port + ":" + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NodeReference)) {
            return false;
        }
        NodeReference other = (NodeReference) obj;
        return this.port == other.port && this.ip.equals(other.ip);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ip.hashCode();
        result = 31 * result + port;
        return result;
    }

}
