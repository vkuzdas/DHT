package pastry.metric;

import pastry.NodeReference;
import proto.Pastry;

public interface DistanceCalculator {
    long calculateDistance(NodeReference self, NodeReference other);
    long calculateDistance(NodeReference self, Pastry.NodeReference other);
}