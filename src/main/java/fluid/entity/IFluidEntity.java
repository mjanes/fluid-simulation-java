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
    public double getHeat();
    public double getTemperature();
    public void recordMassTransfer(FluidEntity targetEntity, double proportion);
    public void convertMassTransferToAbsoluteChange();
    public void recordMassChange(MassChangeRecord record);
    public void recordForceChange(ForceChangeRecord record);
    public void recordHeatTransfer(HeatTransferRecord record);
    public void convertHeatTransferToAbsoluteChange();
    public void recordHeatChange(HeatChangeRecord record);

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
        return .0001;
    }

    /**
     * https://en.wikipedia.org/wiki/Viscosity
     */
    default public double getViscosity() {
        return .08;
    }


    /**
     * A RelativeTransferRecord is the a record of what proportion of this entity should be transferred to the target
     * entity.
     */
    public static class MassTransferRecord {
        final private FluidEntity mTargetEntity;
        final private double mProportion;

        public MassTransferRecord(FluidEntity targetEntity, double proportion) {
            mTargetEntity = targetEntity;
            mProportion = proportion;
        }

        public FluidEntity getTargetEntity() { return mTargetEntity; }

        public double getProportion() { return mProportion; }
    }

    public static class MassChangeRecord {

        final private double mMassChange;
        final private double mMassTemperature;
        final private double mVelocityX;
        final private double mVelocityY;
        final private Color mInkColor;

        public MassChangeRecord(double massChange, double massTemperature, double velocityX, double velocityY, Color inkColor) {
            mMassChange = massChange;
            mMassTemperature = massTemperature;
            mVelocityX = velocityX;
            mVelocityY = velocityY;
            mInkColor = inkColor;
        }

        public void transfer(FluidEntity entity) {
            if (mMassChange < 0) {
                entity.subtractMass(mMassChange);
            } else if (mMassChange > 0) {
                entity.addMass(mMassChange, mMassTemperature, mInkColor, mVelocityX, mVelocityY);
            }
        }
    }

    public static class ForceChangeRecord {
        final private double mForceX;
        final private double mForceY;

        public ForceChangeRecord(double forceX, double forceY) {
            mForceX = forceX;
            mForceY = forceY;
        }

        public void transfer(FluidEntity entity) {
            entity.addForceX(mForceX);
            entity.addForceY(mForceY);
        }
    }

    public static class HeatTransferRecord {
        final private IFluidEntity mTargetEntity;
        final private double mHeatChange;

        /**
         *
         * @param targetEntity
         * @param heatChange    Should always be positive.
         */
        public HeatTransferRecord(IFluidEntity targetEntity, double heatChange) {
            mTargetEntity = targetEntity;
            mHeatChange = heatChange;
        }

        public IFluidEntity getTargetEntity() { return mTargetEntity; }

        public double getHeatChange() { return mHeatChange; }
    }

    public static class HeatChangeRecord {
        final private double mHeatChange;

        public HeatChangeRecord(double heatChange) {
            mHeatChange = heatChange;
        }

        public void transfer(FluidEntity entity) {
            entity.addHeat(mHeatChange);
        }
    }

}
