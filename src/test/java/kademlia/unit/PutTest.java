package kademlia.unit;

import kademlia.KademliaNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Disable to not waste CI resources")
public class PutTest extends BaseTest {

    @Test
    public void testPut_singleNode() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        bootstrap.put("key1", "val1");
        assertEquals(1, bootstrap.getLocalData().size());

        String value = bootstrap.get("key1");
        assertEquals("val1", value);
    }

    @Test
    public void testPut_twoNode() throws IOException {
        KademliaNode bootstrap = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ZERO);
        runningNodes.add(bootstrap);
        bootstrap.initKademlia();

        KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.ONE);
        runningNodes.add(joiner);
        joiner.join(bootstrap.getNodeReference());

        bootstrap.put("key1", "val1");

        String value = joiner.get("key1");
        assertEquals("val1", value);
    }

}
