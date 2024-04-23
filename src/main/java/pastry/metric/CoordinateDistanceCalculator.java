package pastry.metric;

import pastry.NodeReference;
import proto.Pastry;

public class CoordinateDistanceCalculator implements DistanceCalculator {
    @Override
    public long calculateDistance(NodeReference self, NodeReference other) {
        long xDiff = self.getX() - other.getX();
        long yDiff = self.getY() - other.getY();
        return (long) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    @Override
    public long calculateDistance(NodeReference self, Pastry.NodeReference other) {
        long xDiff = self.getX() - other.getX();
        long yDiff = self.getY() - other.getY();
        return (long) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
}
