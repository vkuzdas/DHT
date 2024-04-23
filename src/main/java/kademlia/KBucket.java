package kademlia;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Each KBucket is kept sorted by time last seen: most-recently seen at the tail
 */
public class KBucket {
    private final int MAX_SIZE;
    private final NodeReference owner;
    private final ArrayDeque<NodeReference> nodes;


    public KBucket(int k, NodeReference owner) {
        this.MAX_SIZE = k;
        this.owner = owner;
        nodes = new ArrayDeque<>();
    }

    /**
     * Insert most recently seen at the tail/end
     */
    public boolean add(NodeReference newNode) {

        if (owner.equals(newNode)) return false;

        if (nodes.contains(newNode)) {
            nodes.remove(newNode);
            nodes.addLast(newNode);
            return false;
        }

        if (nodes.size() == MAX_SIZE)  {
            nodes.removeFirst();
            nodes.addLast(newNode);
            return false;
        }

        nodes.addLast(newNode);
        return true;
    }

    public boolean addTail(NodeReference node) {
        if (nodes.size() < MAX_SIZE) {
            nodes.addLast(node);
            return true;
        }
        return false;
    }

    public boolean addHead(NodeReference node) {
        if (nodes.size() < MAX_SIZE) {
            nodes.addFirst(node);
            return true;
        }
        return false;
    }

    public int getSize() {
        return nodes.size();
    }

    public List<NodeReference> toList() {
        return new ArrayList<>(nodes);
    }

    public Stream<NodeReference> toStream() {
        return nodes.stream();
    }

    public boolean contains(NodeReference node) {
        return nodes.contains(node);
    }

    public boolean isFull() {
        return nodes.size() == MAX_SIZE;
    }

    public boolean isEmpty() {
        return nodes.size() == 0;
    }

    public NodeReference getHead() {
        return nodes.peekFirst();
    }

    public NodeReference getTail() {
        return nodes.peekLast();
    }

    public boolean remove(NodeReference toRemove) {
        return nodes.remove(toRemove);
    }

    public String toString() {
        if (nodes.isEmpty()) return "";
        String r = "";
        for (NodeReference n : nodes) {
            r += n + " ";
        }
        return r;
    }
}
