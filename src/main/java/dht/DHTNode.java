package dht;

import chord.ChordNode;
import kademlia.KademliaNode;
import pastry.PastryNode;
import pastry.metric.DistanceCalculator;

import java.time.Duration;

/**
 * Distributed Hash Table wrapper <br>
 * Offers put, get, and delete operations on three basic DHT implementations: Pastry, Chord, and Kademlia
 */
public class DHTNode implements DHTNodeInterface{

    private DHTNodeInterface dhtNode;

    public static void setCustomChordSettings(
            int m,
            int stabilizeInterval
    ) {
        ChordNode.m = m;
        ChordNode.STABILIZATION_INTERVAL = stabilizeInterval;
    }

    public static void setCustomPastrySettings(
            int idBase, // 4 or 16
            int leafSize, // 8 16 32
            DistanceCalculator calculator
    ) {
        PastryNode.setBase(idBase);
        PastryNode.setLeafSize(leafSize);
        PastryNode.setDefaultCalculator(calculator);
    }

    public static void setCustomKademliaSettings(
            int k,
            int alpha,
            int idLength,
            Duration refreshInterval,
            Duration republishInterval,
            Duration expireInterval
    ) {
        KademliaNode.setK(k);
        KademliaNode.setAlpha(alpha);
        KademliaNode.setIdLength(idLength);
        KademliaNode.setRefreshInterval(refreshInterval);
        KademliaNode.setRepublishInterval(republishInterval);
        KademliaNode.setExpireInterval(expireInterval);
    }

    /**
     * Default or custom (if defined) settings of given type
     */
    public DHTNode(DHTType type, String ip, int port) {
        switch (type) {
            case Chord:
                this.dhtNode = new ChordNode(ip, port);
                break;
            case Pastry:
                this.dhtNode = new PastryNode(ip, port, 0, 0);
                break;
            case Kademlia:
                this.dhtNode = new KademliaNode(ip, port);
                break;
        }
    }

    /**
     * Initialize new network, node becomes bootstrap
     */
    @Override
    public void init() {
        this.dhtNode.init();
    }

    /**
     * Join existing DHT network
     */
    @Override
    public void join(String ip, int port) {
        this.dhtNode.join(ip, port);
    }

    @Override
    public void leave() {
        this.dhtNode.leave();
    }

    @Override
    public void fail() {
        this.dhtNode.fail();
    }

    @Override
    public void put(String key, String value) {
        this.dhtNode.put(key, value);
    }

    @Override
    public String get(String key) {
        return this.dhtNode.get(key);
    }

    @Override
    public void delete(String key) {
        this.dhtNode.delete(key);
    }

    @Override
    public void shutdown() {
        this.dhtNode.shutdown();
    }

    @Override
    public String getIp() {
        return this.dhtNode.getIp();
    }

    @Override
    public int getPort() {
        return this.dhtNode.getPort();
    }
}
