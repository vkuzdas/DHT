package pastry.metric;

import pastry.NodeReference;
import proto.Pastry;

public class PortDifferenceDistanceCalculator implements DistanceCalculator {
    @Override
    public long calculateDistance(NodeReference self, NodeReference node2) {
        return Math.abs(self.getPort() - node2.getPort());
    }

    @Override
    public long calculateDistance(NodeReference self, Pastry.NodeReference other) {
        return Math.abs(self.getPort() - other.getPort());
    }
}