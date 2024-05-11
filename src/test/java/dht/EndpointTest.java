package dht;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EndpointTest {

    @Test
    public void testRestPut() throws IOException {
        DHTNode node = new DHTNode(DHTType.Pastry, "localhost", 8080);
        node.startRestEndpoint(8081);

        getRequest("http://localhost:8081/init");
        getRequest("http://localhost:8081/put/hello/there");

        assertEquals("there", node.get("hello"));
    }

    public void getRequest(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.getResponseCode();
        con.getInputStream();
    }
}
