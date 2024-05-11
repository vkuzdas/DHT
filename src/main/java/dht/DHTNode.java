package dht;

import chord.ChordNode;
import io.javalin.Javalin;
import kademlia.KademliaNode;
import pastry.PastryNode;
import pastry.metric.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * Distributed Hash Table wrapper <br>
 * Offers put, get, and delete operations on three basic DHT implementations: Pastry, Chord, and Kademlia
 */
public class DHTNode implements DHTNodeInterface {

    private DHTNodeInterface dhtNode;
    private Javalin restEndpoint;

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

    public static void setCustomSettingsFromPropertiesFile() throws IOException {
        Properties prop = new Properties();
        InputStream fis = DHTNode.class.getClassLoader().getResourceAsStream("dht.properties");
        prop.load(fis);

        setCustomChordSettings(
                Integer.parseInt(prop.getProperty("dht.chord.m")),
                Integer.parseInt(prop.getProperty("dht.chord.stabilizeInterval"))
        );

        String pastryCalculator = prop.getProperty("dht.pastry.calculator");
        DistanceCalculator calculator;
        switch (pastryCalculator) {
            case "ZeroDistanceCalculator":
                calculator = new ZeroDistanceCalculator();
                break;
            case "PortDifferenceDistanceCalculator":
                calculator = new PortDifferenceDistanceCalculator();
                break;
            case "NumericalDifferenceDistanceCalculator":
                calculator = new NumericalDifferenceDistanceCalculator();
                break;
            case "CoordinateDistanceCalculator":
                calculator = new CoordinateDistanceCalculator();
                break;
            default:
                throw new IllegalArgumentException("Unknown pastry calculator: " + pastryCalculator);
        }
        setCustomPastrySettings(
                Integer.parseInt(prop.getProperty("dht.pastry.idBase")),
                Integer.parseInt(prop.getProperty("dht.pastry.leafSize")),
                calculator
        );

        setCustomKademliaSettings(
                Integer.parseInt(prop.getProperty("dht.kademlia.k")),
                Integer.parseInt(prop.getProperty("dht.kademlia.alpha")),
                Integer.parseInt(prop.getProperty("dht.kademlia.idLength")),
                Duration.ofSeconds(Integer.parseInt(prop.getProperty("dht.kademlia.refreshInterval"))),
                Duration.ofSeconds(Integer.parseInt(prop.getProperty("dht.kademlia.republishInterval"))),
                Duration.ofSeconds(Integer.parseInt(prop.getProperty("dht.kademlia.expireInterval")))
        );
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
                this.dhtNode = new PastryNode(ip, port, new Random().nextLong(), new Random().nextLong());
                break;
            case Kademlia:
                this.dhtNode = new KademliaNode(ip, port);
                break;
        }
    }

    public void startRestEndpoint(int restPort) {
        Javalin app = Javalin.create().start(restPort);
        this.restEndpoint = app;

        this.restEndpoint.get("/join/{ip}/{port}", ctx -> {
            String ipJoin = ctx.pathParam("ip");
            int portJoin = Integer.parseInt(ctx.pathParam("port"));
            this.join(ipJoin, portJoin);
            ctx.result("joining " + ipJoin + ":" + portJoin);
        });

        this.restEndpoint.get("/init", ctx -> {
            this.init();
            ctx.result("initialized single node network on " + this.getIp() + ":" + this.getPort());
        });

        this.restEndpoint.get("/put/{key}/{value}", ctx -> {
            String key = ctx.pathParam("key");
            String value = ctx.pathParam("value");
            this.put(key, value);
            ctx.result("stored " + key + ":" + value);
        });

        this.restEndpoint.get("/get/{key}", ctx -> {
            String key = ctx.pathParam("key");
            String value = this.get(key);
            ctx.result(value);
        });

        this.restEndpoint.get("/delete/{key}", ctx -> {
            String key = ctx.pathParam("key");
            this.delete(key);
            ctx.result("OK");
        });

        this.restEndpoint.get("/shutdown", ctx -> {
            this.shutdown();
            ctx.result("shutting down");
        });

        this.restEndpoint.get("/fail", ctx -> {
            this.fail();
            ctx.result("simulating failure");
        });

        this.restEndpoint.get("/leave", ctx -> {
            this.leave();
            ctx.result("leaving current network");
        });

        this.restEndpoint.get("/getLocalData", ctx -> {
            ctx.json(this.getLocalData());
        });

    }

    /**
     * Initialize new network, node becomes bootstrap
     */
    @Override
    public void init() {
        this.dhtNode.init();
    }

    @Override
    public void blockUntilShutdown() {
        this.dhtNode.blockUntilShutdown();
    }

    @Override
    public Map<BigInteger, String> getLocalData() {
        return this.dhtNode.getLocalData();
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
