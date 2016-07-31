package fluid.entity;

import javafx.scene.paint.Color;

/**
 * Created by mjanes on 7/13/2014.
 */
public interface IFluidEntity extends IDimensionalEntity {

    int SPACE = 5; // spacing between entities, currently writing this that they must be placed on a grid
    double GAS_CONSTANT = .02;

    double DEFAULT_TEMPERATURE = 10;
    double DEFAULT_MASS = 10;
    Color DEFAULT_COLOR = Color.TRANSPARENT;
    double DEFAULT_DX = 0;
    double DEFAULT_DY = 0;

    double CELL_AREA = Math.pow(SPACE, 2);

    double getDeltaX();

    double getDeltaY();

    double getForceX();

    double getForceY();

    double getMass();

    double getPressure();

    double getHeat();

    double getTemperature();

    void recordMassTransfer(FluidEntity targetEntity, double proportion);

    void convertMassTransferToAbsoluteChange();

    void recordMassChange(MassChangeRecord record);

    void recordForceChange(ForceChangeRecord record);

    void recordHeatTransfer(HeatTransferRecord record);

    void convertHeatTransferToAbsoluteChange();

    void recordHeatChange(HeatChangeRecord record);

    void addForceX(double forceX);

    void addForceY(double forceY);

    void addForceZ(double forceZ);

    void addHeat(double deltaHeat);


    // TODO: Make the below variables based on the type of matter within the cell, and thus not default

    /**
     * https://en.wikipedia.org/wiki/Avogadro%27s_law
     */
    default double getMolarWeight() {
        return 1;
    }


    /**
     * https://en.wikipedia.org/wiki/Thermal_conductivity
     */
    default double getConductivity() {
        return .0001;
    }

    /**
     * https://en.wikipedia.org/wiki/Viscosity
     */
    default double getViscosity() {
        return .1;
    }


    /**
     * A RelativeTransferRecord is the a record of what proportion of this entity should be transferred to the target
     * entity.
     */
    class MassTransferRecord {
        final private FluidEntity mTargetEntity;
        final private double mProportion;

        MassTransferRecord(FluidEntity targetEntity, double proportion) {
            mTargetEntity = targetEntity;
            mProportion = proportion;
        }

        FluidEntity getTargetEntity() {
            return mTargetEntity;
        }

        double getProportion() {
            return mProportion;
        }
    }

    class MassChangeRecord {

        final private double mMassChange;
        final private double mMassTemperature;
        final private double mVelocityX;
        final private double mVelocityY;
        final private Color mInkColor;

        MassChangeRecord(double massChange, double massTemperature, double velocityX, double velocityY, Color inkColor) {
            mMassChange = massChange;
            mMassTemperature = massTemperature;
            mVelocityX = velocityX;
            mVelocityY = velocityY;
            mInkColor = inkColor;
        }

        void transfer(FluidEntity entity) {
            if (mMassChange < 0) {
                entity.subtractMass(mMassChange);
            } else if (mMassChange > 0) {
                entity.addMass(mMassChange, mMassTemperature, mInkColor, mVelocityX, mVelocityY);
            }
        }
    }

    class ForceChangeRecord {
        final private double mForceX;
        final private double mForceY;

        public ForceChangeRecord(double forceX, double forceY) {
            mForceX = forceX;
            mForceY = forceY;
        }

        void transfer(FluidEntity entity) {
            entity.addForceX(mForceX);
            entity.addForceY(mForceY);
        }
    }

    class HeatTransferRecord {
        final private IFluidEntity mTargetEntity;
        final private double mHeatChange;

        /**
         * @param targetEntity
         * @param heatChange   Should always be positive.
         */
        public HeatTransferRecord(IFluidEntity targetEntity, double heatChange) {
            mTargetEntity = targetEntity;
            mHeatChange = heatChange;
        }

        IFluidEntity getTargetEntity() {
            return mTargetEntity;
        }

        double getHeatChange() {
            return mHeatChange;
        }
    }

    class HeatChangeRecord {
        final private double mHeatChange;

        HeatChangeRecord(double heatChange) {
            mHeatChange = heatChange;
        }

        void transfer(FluidEntity entity) {
            entity.addHeat(mHeatChange);
        }
    }

}
