package pastry.metric;

import pastry.NodeReference;
import proto.Pastry;

public class ZeroDistanceCalculator implements DistanceCalculator {
    @Override
    public long calculateDistance(NodeReference self, NodeReference other) {
        return 0L;
    }

    @Override
    public long calculateDistance(NodeReference self, Pastry.NodeReference other) {
        return 0L;
    }
}
