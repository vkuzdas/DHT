package dht;

public interface DHTNodeInterface {

    String getIp();
    int getPort();

    /**
     * Initialize network. Node becomes bootstrap.
     */
    void init();

    /**
     * Join onto the specified bootstrap ip & port combination
     */
    void join(String ip, int port);

    /**
     * Orderly leave
     */
    void leave();

    /**
     * Leave without notice
     */
    void fail();

    void put(String key, String value);
    String get(String key);
    void delete(String key);

    void shutdown();
}
