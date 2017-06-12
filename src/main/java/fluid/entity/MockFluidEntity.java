package fluid.entity;

import javafx.scene.paint.Color;

/**
 * Immutable mock entity to function as entity off the edge of the simulation.
 */
public class MockFluidEntity extends FluidEntity {

    MockFluidEntity(double x, double y, double z) {
        super(x, y, z, DEFAULT_MASS, DEFAULT_TEMPERATURE);
    }

    public void addForceX(double forceX) {
    }


    public void addForceY(double forceY) {
    }

    public void addForceZ(double forceZ) {
    }

    public void addHeat(double deltaHeat) {
    }

    @Override
    public void recordMassChange(double deltaMass) {
    }

    @Override
    protected void changeMass() {
    }

    @Override
    public void recordForceChange(double deltaForceX, double deltaForceY) {
    }

    @Override
    public void recordHeatChange(double deltaHeat) {
    }

}