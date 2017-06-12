package fluid.entity;

import javafx.scene.paint.Color;

public class ReflectiveMockFluidEntity extends MockFluidEntity {

    public ReflectiveMockFluidEntity(double x, double y, double z) {
        super(x, y, z);
    }

    @Override
    public synchronized Color getColor() {
        return Color.BEIGE;
    }

    @Override
    public void applyNeighborInteractions(FluidEntity other) {
        checkReflection(other);
        super.applyNeighborInteractions(other);
    }

    private void checkReflection(FluidEntity other) {
        if (getX() != other.getX()) {
            if (getX() > other.getX() && other.getDeltaX() > 0 || getX() < other.getX() && other.getDeltaX() < 0) {
                other.recordForceChange(-other.forceX * 2, 0);
            }
        } else if (getY() != other.getY()) {
            if (getY() > other.getY() && other.getDeltaY() > 0 || getY() < other.getY() && other.getDeltaY() < 0) {
                other.recordForceChange(0, -other.forceY * 2);
            }
        }
    }
}
