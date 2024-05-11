package dht;

import java.io.IOException;

public class Main {

    /**
     * This example shows how to start a DHT node with a REST endpoint <br>
     * <br>
     *
     * This class can be started from command line (or IDE) with dhtPort and restPoint as arguments.
     * dhtPort is used for the DHT interface, restPort is used for the REST interface. Two instances can be started in IntelliJ IDEA in 'Edit Run Configuration'.
     * There you can copy the Main class and set the CLI arguments (this is the way in which it the class was tested). <br> <br>
     *
     * To run a minimal working two-node DHT instance using this class, run two instances on 4 different ports.
     * For example, run first instance on 8080 and 8081, and the second instance on 8090 and 8091.
     * Note that xxx0 is used for the DHT interface and xxx1 is used for the REST interface.<br> <br>
     *
     * After starting both class instances, nodes are started but not initialized.
     * Initialize the first by connecting onto <b>http://localhost:8081/init</b> in your browser (or get requst using CURL or Postman).
     * Then you can prompt the second node to connect to the first by sending another get request to <b>http://localhost:8091/join/localhost/8080</b>. <br> <br>
     *
     * After both nodes are connected, you can:
     * <ul>
     *     <li>store a key-value pair by sending a get request to <b>http://localhost:xxx1/put/key/value</b></li>
     *     <li>get all key-value pairs stored on the node by sending a get request to <b>http://localhost:xxx1/getLocalData</b></li>
     *     <li>retrieve a value by sending a get request to <b>http://localhost:xxx1/get/key</b></li>
     *     <li>delete a key-value pair by sending a get request to <b>http://localhost:xxx1/delete/key</b></li>
     *     <li>shut down a node by sending a get request to <b>http://localhost:xxx1/shutdown</b></li>
     *     <li>simulate a node failure by sending a get request to <b>http://localhost:xxx1/fail</b></li>
     *     <li>leave the network by sending a get request to <b>http://localhost:xxx1/leave</b></li>
     * </ul>
     *
     *
     *
     */
    public static void main(String[] args) throws IOException {

        int port = args[0] == null ? 8080 : Integer.parseInt(args[0]);
        int restPort = args[1] == null ? 8081 : Integer.parseInt(args[1]);

        // pulls configuration from ../../main/resources/dht.properties
        DHTNode.setCustomSettingsFromPropertiesFile();

        DHTNode node = new DHTNode(DHTType.Pastry, "localhost", port);

        node.startRestEndpoint(restPort);

        // will let the node run indefinetely so you can send requests onto localhost:8081
        node.blockUntilShutdown();
    }
}
