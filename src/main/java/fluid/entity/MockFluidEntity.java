package fluid.entity;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable mock entity to function as entity off the edge of the simulation.
 */
public class MockFluidEntity extends FluidEntity {

    private final ConcurrentHashMap<MassTransferRecord, Integer> massTransferRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<HeatTransferRecord, Integer> heatTransferRecords = new ConcurrentHashMap<>();

    public MockFluidEntity(double x, double y, double z, double mass, double temperature) {
        super(x, y, z, mass, temperature);
    }


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
    public double getDistance(DimensionalEntity other) {
        return 0;
    }

    @Override
    public Array2DRowRealMatrix getR4Matrix() {
        return null;
    }

    @Override
    public double getDeltaX() {
        return FluidEntity.DEFAULT_DX;
    }

    @Override
    public double getForceX() {
        return FluidEntity.DEFAULT_DX * FluidEntity.DEFAULT_MASS;
    }

    @Override
    public double getDeltaY() {
        return FluidEntity.DEFAULT_DY;
    }

    @Override
    public double getForceY() {
        return FluidEntity.DEFAULT_DY * FluidEntity.DEFAULT_MASS;
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
        return FluidEntity.DEFAULT_MASS;
    }

    @Override
    public double getPressure() {
        return GAS_CONSTANT * FluidEntity.DEFAULT_MASS * getTemperature() / getMolarWeight();
    }

    @Override
    public double getHeat() {
        return FluidEntity.DEFAULT_TEMPERATURE * FluidEntity.DEFAULT_MASS;
    }

    @Override
    public double getTemperature() {
        return FluidEntity.DEFAULT_TEMPERATURE;
    }

    @Override
    public void recordMassTransfer(FluidEntity targetEntity, double proportion) {
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
    public void recordForceChange(ForceChangeRecord record) {
    }

    @Override
    public void recordHeatTransfer(HeatTransferRecord record) {
        heatTransferRecords.put(record, 0);
    }

    @Override
    public void convertHeatTransferToAbsoluteChange() {
        heatTransferRecords.keySet().stream().filter(heatTransferRecord -> heatTransferRecord.getTargetEntity() != null).forEach(heatTransferRecord -> {
            double heatTransfer = heatTransferRecord.getHeatChange();

            if (heatTransferRecord.getTargetEntity() != null) {
                heatTransferRecord.getTargetEntity().recordHeatChange(new HeatChangeRecord(heatTransfer));
            }
        });

        heatTransferRecords.clear();
    }

    @Override
    public void recordHeatChange(HeatChangeRecord record) {
    }

}