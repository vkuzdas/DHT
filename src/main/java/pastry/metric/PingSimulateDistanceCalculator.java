package pastry.metric;

import pastry.NodeReference;
import proto.Pastry;

import java.util.Random;

public class PingSimulateDistanceCalculator implements DistanceCalculator {
    @Override
    public long calculateDistance(NodeReference self, NodeReference other) {
        return new Random().nextInt(500) + 500;
    }

    @Override
    public long calculateDistance(NodeReference self, Pastry.NodeReference other) {
        return new Random().nextInt(500) + 500;
    }
}
