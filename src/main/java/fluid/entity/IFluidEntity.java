package fluid.entity;

import javafx.scene.paint.Color;

/**
 * Created by mjanes on 7/13/2014.
 */
public interface IFluidEntity extends IDimensionalEntity {

    public static final int SPACE = 5; // spacing between entities, currently writing this that they must be placed on a grid
    public static final double GAS_CONSTANT = .02;

    public static final double DEFAULT_TEMPERATURE = 10;
    public static final double DEFAULT_MASS = 10;
    public static final Color DEFAULT_COLOR = Color.TRANSPARENT;
    public static final double DEFAULT_DX = 0;
    public static final double DEFAULT_DY = 0;

    public static final double CELL_AREA = Math.pow(SPACE, 2);

    public double getDeltaX();
    public double getDeltaY();
    public double getMass();
    public double getPressure();
    public double getTemperature();
    public void recordRelativeTransferTo(FluidEntity targetEntity, double proportion);
    public void transferRelativeValues();
    public void recordTransferTo(FluidEntity targetEntity, double proportion);
    public void recordIncomingMass(IncomingMassRecord record);
    public void recordDeltaChange(DeltaChangeRecord record);

    public void addDeltaX(double deltaDeltaX);
    public void addDeltaY(double deltaDeltaY);
    public void addForceX(double forceX);
    public void addForceY(double forceY);
    public void addForceZ(double forceZ);
    public void addHeat(double deltaHeat);


    // TODO: Make the below variables based on the type of matter within the cell, and thus not default

    /**
     * https://en.wikipedia.org/wiki/Avogadro%27s_law
     */
    default public double getMolarWeight() {
        return 1;
    }


    /**
     * https://en.wikipedia.org/wiki/Thermal_conductivity
     */
    default public double getConductivity() {
        return .01;
    }

    /**
     * https://en.wikipedia.org/wiki/Viscosity
     */
    default public double getViscosity() {
        return .01;
    }


    /**
     * A RelativeTransferRecord is the a record of what proportion of this entity should be transferred to the target
     * entity.
     */
    public static class RelativeTransferRecord {
        final private FluidEntity mTargetEntity;
        final private double mProportion;

        public RelativeTransferRecord(FluidEntity targetEntity, double proportion) {
            mTargetEntity = targetEntity;
            mProportion = proportion;
        }

        public FluidEntity getTargetEntity() { return mTargetEntity; }

        public double getProportion() { return mProportion; }

    }

    /**
     * An absolute transfer record records values to be added to this entity's.
     */
    public static class IncomingMassRecord {

        final private double mMassTransfer;
        final private double mMassTemperature;
        final private double mVelocityX;
        final private double mVelocityY;
        final private Color mInkColor;

        public IncomingMassRecord(double massTransfer, double massTemperature, double velocityX, double velocityY, Color inkColor) {
            mMassTransfer = massTransfer;
            mMassTemperature = massTemperature;
            mVelocityX = velocityX;
            mVelocityY = velocityY;
            mInkColor = inkColor;
        }

        public void transfer(FluidEntity entity) {
            entity.addMass(mMassTransfer, mMassTemperature, mInkColor, mVelocityX, mVelocityY);
        }
    }

    public static class OutgoingMassRecord {
        final private double mMassTransfer;

        public OutgoingMassRecord(double massTransfer) {
            mMassTransfer = massTransfer;
        }

        public void transfer(FluidEntity entity) {
            entity.subtractMass(mMassTransfer);
        }
    }

    public static class DeltaChangeRecord {
        final private double mDeltaDeltaX;
        final private double mDeltaDeltaY;

        public DeltaChangeRecord(double deltaDeltaX, double deltaDeltaY) {
            mDeltaDeltaX = deltaDeltaX;
            mDeltaDeltaY = deltaDeltaY;
        }

        public void transfer(FluidEntity entity) {
            entity.addDeltaX(mDeltaDeltaX);
            entity.addDeltaY(mDeltaDeltaY);
        }
    }

}
