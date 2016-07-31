package fluid.entity;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable mock entity to function as entity off the edge of the simulation.
 */
public class MockFluidEntity implements IFluidEntity {

    private final ConcurrentHashMap<MassTransferRecord, Integer> mMassTransferRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<HeatTransferRecord, Integer> mHeatTransferRecords = new ConcurrentHashMap<>();


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
    public double getForceX() {
        return IFluidEntity.DEFAULT_DX * IFluidEntity.DEFAULT_MASS;
    }

    @Override
    public double getDeltaY() {
        return IFluidEntity.DEFAULT_DY;
    }

    @Override
    public double getForceY() {
        return IFluidEntity.DEFAULT_DY * IFluidEntity.DEFAULT_MASS;
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
    public double getMass() {
        return IFluidEntity.DEFAULT_MASS;
    }

    @Override
    public double getPressure() {
        return GAS_CONSTANT * IFluidEntity.DEFAULT_MASS * getTemperature() / getMolarWeight();
    }

    @Override
    public double getHeat() {
        return IFluidEntity.DEFAULT_TEMPERATURE * IFluidEntity.DEFAULT_MASS;
    }

    @Override
    public double getTemperature() {
        return IFluidEntity.DEFAULT_TEMPERATURE;
    }

    @Override
    public void recordMassTransfer(FluidEntity targetEntity, double proportion) {
        if (proportion < 0 || proportion > 1) {
            System.out.println("Error, proportion = " + proportion);
            return;
        }

        mMassTransferRecords.put(new MassTransferRecord(targetEntity, proportion), 0);
    }

    @Override
    public void convertMassTransferToAbsoluteChange() {
        mMassTransferRecords.keySet().stream().filter(massTransferRecord -> massTransferRecord.getTargetEntity() != null).forEach(massTransferRecord -> {
            double massTransfer = massTransferRecord.getProportion() * getMass();
            massTransferRecord.getTargetEntity().recordMassChange(new MassChangeRecord(massTransfer, getTemperature(), getDeltaX(), getDeltaY(), DEFAULT_COLOR));
        });

        mMassTransferRecords.clear();
    }

    @Override
    public void recordMassChange(MassChangeRecord record) {
    }

    @Override
    public void recordForceChange(ForceChangeRecord record) {
    }

    @Override
    public void recordHeatTransfer(HeatTransferRecord record) {
        mHeatTransferRecords.put(record, 0);
    }

    @Override
    public void convertHeatTransferToAbsoluteChange() {
        mHeatTransferRecords.keySet().stream().filter(heatTransferRecord -> heatTransferRecord.getTargetEntity() != null).forEach(heatTransferRecord -> {
            double heatTransfer = heatTransferRecord.getHeatChange();

            if (heatTransferRecord.getTargetEntity() != null) {
                heatTransferRecord.getTargetEntity().recordHeatChange(new HeatChangeRecord(heatTransfer));
            }
        });

        mHeatTransferRecords.clear();
    }

    @Override
    public void recordHeatChange(HeatChangeRecord record) {
    }

}