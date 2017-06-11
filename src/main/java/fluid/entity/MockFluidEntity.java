package fluid.entity;

import javafx.scene.paint.Color;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable mock entity to function as entity off the edge of the simulation.
 */
public class MockFluidEntity extends FluidEntity {

    private final ConcurrentHashMap<MassTransferRecord, Integer> massTransferRecords = new ConcurrentHashMap<>();

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
    public void recordMassTransferTo(FluidEntity targetEntity, double proportion) {
        if (proportion < 0 || proportion > 1) {
            System.out.println("Error, proportion = " + proportion);
            return;
        }

        massTransferRecords.put(new MassTransferRecord(targetEntity, proportion), 0);
    }

    @Override
    public void convertMassTransferToAbsoluteChange() {
        massTransferRecords.keySet().stream().filter(massTransferRecord -> massTransferRecord.getTargetEntity() != null).forEach(massTransferRecord -> {
            double massTransfer = massTransferRecord.getProportion() * getMass();
            massTransferRecord.getTargetEntity().recordMassChange(new MassChangeRecord(massTransfer, getTemperature(), getDeltaX(), getDeltaY(), DEFAULT_COLOR));
        });

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

    @Override
    public void applyNeighborInteractions(FluidEntity other) {
        //matchNeighbor(other);
        super.applyNeighborInteractions(other);
    }

    private void matchNeighbor(FluidEntity other) {
        if (!(other instanceof MockFluidEntity)) {
            this.mass = other.getMass();
            this.temperature = other.getTemperature();
        }
    }

}