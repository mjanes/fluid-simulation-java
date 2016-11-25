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
        final private FluidEntity targetEntity;
        final private double proportion;

        MassTransferRecord(FluidEntity targetEntity, double proportion) {
            this.targetEntity = targetEntity;
            this.proportion = proportion;
        }

        FluidEntity getTargetEntity() {
            return targetEntity;
        }

        double getProportion() {
            return proportion;
        }
    }

    class MassChangeRecord {

        final private double massChange;
        final private double massTemperature;
        final private double velocityX;
        final private double velocityY;
        final private Color inkColor;

        MassChangeRecord(double massChange, double massTemperature, double velocityX, double velocityY, Color inkColor) {
            this.massChange = massChange;
            this.massTemperature = massTemperature;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.inkColor = inkColor;
        }

        void transfer(FluidEntity entity) {
            if (massChange < 0) {
                entity.subtractMass(massChange);
            } else if (massChange > 0) {
                entity.addMass(massChange, massTemperature, inkColor, velocityX, velocityY);
            }
        }
    }

    class ForceChangeRecord {
        final private double forceX;
        final private double forceY;

        public ForceChangeRecord(double forceX, double forceY) {
            this.forceX = forceX;
            this.forceY = forceY;
        }

        void transfer(FluidEntity entity) {
            entity.addForceX(forceX);
            entity.addForceY(forceY);
        }
    }

    class HeatTransferRecord {
        final private IFluidEntity targetEntity;
        final private double heatChange;

        /**
         * @param targetEntity
         * @param heatChange   Should always be positive.
         */
        public HeatTransferRecord(IFluidEntity targetEntity, double heatChange) {
            this.targetEntity = targetEntity;
            this.heatChange = heatChange;
        }

        IFluidEntity getTargetEntity() {
            return targetEntity;
        }

        double getHeatChange() {
            return heatChange;
        }
    }

    class HeatChangeRecord {
        final private double heatChange;

        HeatChangeRecord(double heatChange) {
            this.heatChange = heatChange;
        }

        void transfer(FluidEntity entity) {
            entity.addHeat(heatChange);
        }
    }

}
