package kademlia.unit;

import kademlia.KademliaNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Disabled("Disable to not waste CI resources")
public class DeleteTest extends BaseTest {


    /**
     * 1. Join with K+1 nodes <br>
     * 2. Put a key on them <br>
     * 3. Validate presence <br>
     * 4. Delete the key <br>
     * 5. Validate absence <br>
     */
    @Test
    public void testDelete_simple() throws IOException {
        for (int i = 0; i < K+1; i++) {
            KademliaNode joiner = new KademliaNode(LOCAL_IP, BASE_PORT++, BigInteger.valueOf(i));

            if (runningNodes.isEmpty())
                joiner.initKademlia();
            else
                joiner.join(getRandomRunningNode().getNodeReference());

            runningNodes.add(joiner);
        }

        // PUT
        getRandomRunningNode().put("key1", "val1");

        String gotValue = getRandomRunningNode().get("key1");
        assertEquals("val1", gotValue);

        // DELETE
        getRandomRunningNode().delete("key1");

        gotValue = getRandomRunningNode().get("key1");
        assertNull(null, gotValue);
    }
}
