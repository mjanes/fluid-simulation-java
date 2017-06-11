package fluid.entity;

import javafx.scene.paint.Color;

public class MatchingMockFluidEntity extends MockFluidEntity {

    public MatchingMockFluidEntity(double x, double y, double z) {
        super(x, y, z);
    }

    @Override
    public synchronized Color getColor() {
        return Color.SKYBLUE;
    }


    @Override
    public void applyNeighborInteractions(FluidEntity other) {
        matchNeighbor(other);
        super.applyNeighborInteractions(other);
    }

    private void matchNeighbor(FluidEntity other) {
        if (!(other instanceof MockFluidEntity)) {
            this.mass = other.getMass();
            this.temperature = other.getTemperature();
        }
    }

}
