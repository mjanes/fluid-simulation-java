package fluid.entity;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable mock entity to function as entity off the edge of the simulation.
 */
public class MockFluidEntity implements IFluidEntity {

    protected final ConcurrentHashMap<RelativeTransferRecord, Integer> mRelativeTransferRecords = new ConcurrentHashMap<>();

    @Override
    public void setX(double x) {
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public void setY(double y) {
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public void setZ(double z) {
    }

    @Override
    public double getZ() {
        return 0;
    }

    @Override
    public double getDistance(IDimensionalEntity other) {
        return 0;
    }

    @Override
    public Array2DRowRealMatrix getR4Matrix() {
        return null;
    }

    @Override
    public double getDeltaX() {
        return IFluidEntity.DEFAULT_DX;
    }

    @Override
    public double getDeltaY() {
        return IFluidEntity.DEFAULT_DY;
    }

    public void addDeltaX(double deltaDeltaX) {}
    public void addDeltaY(double deltaDeltaY) {}
    public void addForceX(double forceX) {}
    public void addForceY(double forceY) {}
    public void addForceZ(double forceZ) {}
    public void addHeat(double deltaHeat) {}


    @Override
    public double getMass() {
        return IFluidEntity.DEFAULT_MASS;
    }

    @Override
    public double getPressure() {
        return GAS_CONSTANT * IFluidEntity.DEFAULT_MASS * getTemperature() / getMolarWeight();
    }

    @Override
    public double getTemperature() {
        return IFluidEntity.DEFAULT_TEMPERATURE;
    }

    @Override
    public void recordRelativeTransferTo(FluidEntity targetEntity, double proportion) {
        if (proportion < 0 || proportion > 1) {
            System.out.println("Error, proportion = " + proportion);
            return;
        }

        mRelativeTransferRecords.put(new RelativeTransferRecord(targetEntity, proportion), 0);
    }

    @Override
    public void recordIncomingMass(IncomingMassRecord record) {}

    @Override
    public void recordDeltaChange(DeltaChangeRecord record) {}

    @Override
    public void transferRelativeValues() {
     for (RelativeTransferRecord relativeTransferRecord : mRelativeTransferRecords.keySet()) {
            recordTransferTo(relativeTransferRecord.getTargetEntity(), relativeTransferRecord.getProportion());
        }

        mRelativeTransferRecords.clear();
    }

    @Override
    public void recordTransferTo(FluidEntity targetEntity, double proportion) {
        if (proportion == 0) return;

        double massTransfer = IFluidEntity.DEFAULT_MASS * proportion;

        // If targetEntity is null, it is because the transfer is going off the border of the universe
        if (targetEntity == null) return;
        targetEntity.recordIncomingMass(new IncomingMassRecord(massTransfer, IFluidEntity.DEFAULT_TEMPERATURE, IFluidEntity.DEFAULT_DX, IFluidEntity.DEFAULT_DY, IFluidEntity.DEFAULT_COLOR));
    }

}