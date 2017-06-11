package fluid.entity;

import javafx.scene.paint.Color;

/**
 * Immutable mock entity to function as entity off the edge of the simulation.
 */
public class MockFluidEntity extends FluidEntity {

    MockFluidEntity(double x, double y, double z) {
        super(x, y, z, DEFAULT_MASS, DEFAULT_TEMPERATURE);
    }

    @Override
    public synchronized void setDeltaX(double deltaX) {
    }

    public void addForceX(double forceX) {
    }

    @Override
    public synchronized void setDeltaY(double deltaY) {
    }

    public void addForceY(double forceY) {
    }

    public void addForceZ(double forceZ) {
    }

    public void addHeat(double deltaHeat) {
    }

    @Override
    public synchronized void addMass(double deltaMass, double massTemperature, Color color) {
    }

    @Override
    public synchronized void addMass(double deltaMass, double massTemperature, Color color, double incomingDeltaX, double incomingDeltaY) {
    }

    @Override
    public void convertMassTransferToAbsoluteChange() {
        for(FluidEntity fluidEntity : massTransferRecords.keySet()) {
            double massTransfer = massTransferRecords.get(fluidEntity) * getMass();
            fluidEntity.recordMassChange(new MassChangeRecord(massTransfer, getTemperature(), getDeltaX(), getDeltaY(), DEFAULT_COLOR));
        }
        massTransferRecords.clear();
    }

    @Override
    public void recordMassChange(MassChangeRecord record) {
    }

    @Override
    public void recordForceChange(double deltaForceX, double deltaForceY) {
    }

    @Override
    public void recordHeatChange(double deltaHeat) {
    }

}