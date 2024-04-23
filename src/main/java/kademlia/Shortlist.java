package kademlia;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Shortlist {

    /**
     * Nodes onto which RPC has <B>not</B> been sent yet
     */
    private final HashSet<NodeReference> shortlist;

    /**
     * Nodes onto which RPC has been sent
     */
    private final HashSet<NodeReference> queried;

    /**
     * Nodes that are offline
     */
    private final HashSet<NodeReference> offline;

    private final int ALPHA;



    /**
     * Initialize shortlist from the alfa closest
     */
    public Shortlist(List<NodeReference> alphaFromRoutingTable) {
        ALPHA = alphaFromRoutingTable.size();
        shortlist = new HashSet<>(alphaFromRoutingTable);
        queried = new HashSet<>();
        offline = new HashSet<>();
    }

    public boolean hasUnqueried() {
        return !shortlist.isEmpty();
    }

    public List<NodeReference> pollAlphaNodesForQuery() {
        List<NodeReference> ret = shortlist.stream().limit(ALPHA).collect(Collectors.toList());
        queried.addAll(ret);
        ret.forEach(shortlist::remove);
        return ret;
    }

    public void addToQuery(NodeReference node) {
        if (queried.contains(node))
            return;
        shortlist.add(node);
    }

    public void addQueried(NodeReference node) {
        queried.add(node);
    }

    public void addOffline(NodeReference node) {
        offline.add(node);
    }

    public List<NodeReference> getOffline() {
        return new ArrayList<>(offline);
    }

    public List<NodeReference> getKBestQueried(BigInteger targetId, int k) {
        return queried.stream()
                .filter(node -> !offline.contains(node))
                .sorted(Comparator.comparing(node -> targetId.xor(node.getId())))
                .limit(k)
                .collect(Collectors.toList());
    }

}
