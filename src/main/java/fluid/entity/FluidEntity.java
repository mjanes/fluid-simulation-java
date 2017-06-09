package fluid.entity;

import javafx.scene.paint.Color;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
 * <p>
 * Created by mjanes on 6/12/2014.
 */
public class FluidEntity implements DimensionalEntity {

    public static final int SPACE = 5; // spacing between entities, currently writing this that they must be placed on a grid
    static final double GAS_CONSTANT = .02;

    public static final double DEFAULT_TEMPERATURE = 10;
    public static final double DEFAULT_MASS = 10;
    static final Color DEFAULT_COLOR = Color.TRANSPARENT;
    static final double DEFAULT_DX = 0;
    static final double DEFAULT_DY = 0;

    public static final double CELL_AREA = Math.pow(SPACE, 2);


    private double x;
    private double y;
    private double z;

    private final Array2DRowRealMatrix r4Matrix = new Array2DRowRealMatrix(new double[]{0, 0, 0, 1});

    private double deltaX;
    private double deltaY;
    private double deltaZ;
    private double mass;
    private double heat;
    private Color color;

    private final ConcurrentHashMap<MassTransferRecord, Integer> massTransferRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<MassChangeRecord, Integer> massChangeRecords = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<ForceChangeRecord, Integer> forceChangeRecords = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<HeatTransferRecord, Integer> heatTransferRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<HeatChangeRecord, Integer> heatChangeRecords = new ConcurrentHashMap<>();

    public FluidEntity(double x, double y, double z, double mass, double temperature) {
        setX(x);
        setY(y);
        setZ(z);
        setMass(mass);
        setTemperature(temperature);
        setColor(Color.TRANSPARENT);
    }

    @Override
    public synchronized void setX(double x) {
        this.x = x;
        r4Matrix.setEntry(0, 0, x);
    }

    @Override
    public synchronized double getX() {
        return x;
    }

    @Override
    public synchronized void setY(double y) {
        this.y = y;
        r4Matrix.setEntry(1, 0, y);
    }

    @Override
    public synchronized double getY() {
        return y;
    }

    @Override
    public synchronized void setZ(double z) {
        this.z = z;
        r4Matrix.setEntry(2, 0, z);
    }

    @Override
    public synchronized double getZ() {
        return z;
    }

    @Override
    public synchronized double getDistance(DimensionalEntity other) {
        return DimensionalEntity.getDistance(this, other);
    }

    @Override
    public synchronized Array2DRowRealMatrix getR4Matrix() {
        return r4Matrix;
    }


    /**
     * Velocity
     */

    public synchronized void setDeltaX(double deltaX) {
        this.deltaX = deltaX;
    }

    private synchronized void addDeltaX(double deltaDeltaX) {
        setDeltaX(deltaX + deltaDeltaX);
    }

    public synchronized double getDeltaX() {
        return deltaX;
    }

    public synchronized void addForceX(double forceX) {
        if (mass <= 0) {
            setDeltaX(0);
            return;
        }

        // TODO: Probably need some efficiency thing here, if mass is too small, and force is too high, turn some energy
        // into heat?
        addDeltaX(forceX / mass);
    }

    public synchronized double getForceX() {
        return deltaX * mass;
    }


    public synchronized void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }

    public synchronized double getForceY() {
        return deltaY * mass;
    }

    private synchronized void addDeltaY(double deltaDeltaY) {
        setDeltaY(deltaY + deltaDeltaY);
    }

    public synchronized double getDeltaY() {
        return deltaY;
    }

    public synchronized void addForceY(double forceY) {
        if (mass <= 0) {
            setDeltaY(0);
            return;
        }

        // TODO: Probably need some efficiency thing here, if mass is too small, and force is too high, turn some energy
        // into heat?
        addDeltaY(forceY / mass);
    }


    private synchronized void setDeltaZ(double deltaZ) {
        this.deltaZ = deltaZ;
    }

    private synchronized void addDeltaZ(double deltaDeltaZ) {
        setDeltaZ(deltaZ + deltaDeltaZ);
    }

    public synchronized double getDeltaZ() {
        return deltaZ;
    }

    public synchronized void addForceZ(double forceZ) {
        if (mass <= 0) {
            setDeltaZ(0);
            return;
        }


        // TODO: Probably need some efficiency thing here, if mass is too small, and force is too high, turn some energy
        // into heat?
        addDeltaZ(forceZ / mass);
    }


    /**
     * Mass
     */

    private synchronized void setMass(double mass) {
        if (mass < 0) {
            this.mass = 0;
            return;
        }
        this.mass = mass;
    }

    public synchronized double getMass() {
        return mass;
    }

    public synchronized void addMass(double deltaMass, double massTemperature, Color color) {
        addMass(deltaMass, massTemperature, color, 0, 0);
    }

    public synchronized void addMass(double deltaMass, double massTemperature, Color color, double incomingDeltaX, double incomingDeltaY) {
        if (deltaMass < 0) {
            System.out.println("Negative delta mass in addMass");
            return;
        }

        // We know that deltaMass > 0
        // Order that these are done in is important. Must set delta after setting mass.

        double oldDeltaX = getDeltaX();
        double oldDeltaY = getDeltaY();

        setMass(mass + deltaMass);

        double oldProportion = (mass - deltaMass) / mass;
        double newProportion = deltaMass / mass;

        double newDeltaX = oldDeltaX * oldProportion + incomingDeltaX * newProportion;
        double newDeltaY = oldDeltaY * oldProportion + incomingDeltaY * newProportion;

        setDeltaX(newDeltaX);
        setDeltaY(newDeltaY);

        // Unlike force, heat is independent of mass.
        addHeat(deltaMass * massTemperature);

        if (!(color == null || color.equals(this.color))) {
            // Ink - doing this in a separate block
            double prevRed = this.color.getRed();
            double prevGreen = this.color.getGreen();
            double prevBlue = this.color.getBlue();
            double prevAlpha = this.color.getOpacity();

            double newRed = prevRed * oldProportion + color.getRed() * newProportion;
            double newGreen = prevGreen * oldProportion + color.getGreen() * newProportion;
            double newBlue = prevBlue * oldProportion + color.getBlue() * newProportion;
            double newAlpha = prevAlpha * oldProportion + color.getOpacity() * newProportion;

            if (newRed < 0) newRed = 0;
            if (newGreen < 0) newGreen = 0;
            if (newBlue < 0) newBlue = 0;
            if (newAlpha < 0) newAlpha = 0;
            if (newRed > 1) newRed = 1;
            if (newGreen > 1) newGreen = 1;
            if (newBlue > 1) newBlue = 1;
            if (newAlpha > 1) newAlpha = 1;

            setColor(new Color(newRed, newGreen, newBlue, newAlpha));
        }
    }

    /**
     * If the change in mass is negative, then the delta of the remaining mass will be the same, though the force
     * of that mass will be correspondingly lessened.
     * <p>
     * Heat though will be decreased.
     * <p>
     * I suppose force is analogous to heat, and delta is analogous to temperature here.
     */
    private void subtractMass(double deltaMass) {
        if (mass + deltaMass <= 0) {
            setMass(0);
            setHeat(0);
            setDeltaX(0);
            setDeltaY(0);
            setDeltaZ(0);
        } else {
            double proportion = deltaMass / mass;
            setMass(mass + deltaMass);
            addHeat(heat * proportion);
        }
    }


    /**
     * Ink
     */

    public synchronized Color getColor() {
        return color;
    }

    public synchronized void setColor(Color color) {
        this.color = color;
    }


    /**
     * Heat
     */

    public synchronized void setTemperature(double temperature) {
        heat = temperature * mass;
    }

    public synchronized double getTemperature() {
        if (mass <= 0) return 0;
        return heat / mass;
    }

    private synchronized void setHeat(double heat) {
        if (heat < 0) {
            this.heat = 0;
            return;
        }
        this.heat = heat;
    }

    public synchronized void addHeat(double deltaHeat) {
        setHeat(heat + deltaHeat);
    }

    public synchronized double getHeat() {
        return heat;
    }


    /**
     * Pressure
     * <p>
     * We are presuming that the volume of of a fluid entity cell is constant, but the amount of mass, and
     * the temperature of that mass may change.
     * <p>
     * https://en.wikipedia.org/wiki/Pressure
     * http://www.passmyexams.co.uk/GCSE/physics/pressure-temperature-relationship-of-gas-pressure-law.html
     * https://en.wikipedia.org/wiki/Charles%27s_Law
     */
    public synchronized double getPressure() {
        return GAS_CONSTANT * mass * getTemperature() / getMolarWeight();
    }


    /**
     * For display
     */

    public synchronized FluidEntity getNextLocationAsFluidEntity(double velocityFactor) {
        return new FluidEntity(x + (deltaX * velocityFactor), y + (deltaY * velocityFactor), z + (deltaZ * velocityFactor), mass, getTemperature());
    }

    /**
     * Stepping from to the next increment of the simulation
     */

    /**
     * Mass transfers
     *
     * These are done in two stages, so that the order in which each method of the same type is applied to a cell does
     * not matter.
     */

    /**
     * Records a transfer from this entity to targetEntity, a proportion of this entities values
     */
    public void recordMassTransfer(FluidEntity targetEntity, double proportion) {
        if (proportion < 0 || proportion > 1) {
            System.out.println("Error, proportion = " + proportion);
            return;
        }

        if (mass == 0) return;

        // Do not record transfers to self.
        if (this.equals(targetEntity)) return;

        massTransferRecords.put(new MassTransferRecord(targetEntity, proportion), 0);
    }

    /**
     * Stage 1 of the mass transfer steps.
     */
    public void convertMassTransferToAbsoluteChange() {
        double totalRatio = 0;
        for (MassTransferRecord massTransferRecord : massTransferRecords.keySet()) {
            totalRatio += massTransferRecord.getProportion();
        }

        for (MassTransferRecord massTransferRecord : massTransferRecords.keySet()) {
            double massTransfer;
            if (totalRatio > 1) {
                massTransfer = getMass() * massTransferRecord.getProportion() / totalRatio;
            } else {
                massTransfer = getMass() * massTransferRecord.getProportion();
            }

            recordMassChange(new MassChangeRecord(-massTransfer, getTemperature(), getDeltaX(), getDeltaY(), color));

            if (massTransferRecord.getTargetEntity() != null) {
                massTransferRecord.getTargetEntity().recordMassChange(new MassChangeRecord(massTransfer, getTemperature(), getDeltaX(), getDeltaY(), color));
            }
        }

        massTransferRecords.clear();
    }

    /**
     * Transferring mass to a fluid entity
     */
    void recordMassChange(MassChangeRecord record) {
        massChangeRecords.put(record, 0);
    }

    public void changeMass() {
        for (MassChangeRecord transferRecord : massChangeRecords.keySet()) {
            transferRecord.transfer(this);
        }

        massChangeRecords.clear();
    }


    /**
     * Force transfers
     */

    public void recordForceChange(ForceChangeRecord record) {
        forceChangeRecords.put(record, 0);
    }

    public void changeForce() {
        for (ForceChangeRecord forceChangeRecord : forceChangeRecords.keySet()) {
            forceChangeRecord.transfer(this);
        }

        forceChangeRecords.clear();
    }

    /**
     * Heat transfers
     *
     * This is done in two parts, similar to how mass transfers are done, because heat cannot go negative, and thus,
     */

    /**
     * Record heat transfer away from this entity, to target entity, in HeatTransferRecord.
     */
    public void recordHeatTransfer(HeatTransferRecord record) {
        heatTransferRecords.put(record, 0);
    }

    public void convertHeatTransferToAbsoluteChange() {
        double totalHeatTransfer = 0;
        for (HeatTransferRecord heatChangeRecord : heatTransferRecords.keySet()) {
            totalHeatTransfer += heatChangeRecord.getHeatChange();
        }

        for (HeatTransferRecord heatTransferRecord : heatTransferRecords.keySet()) {
            double heatTransfer;
            if (totalHeatTransfer - heat < 0) {
                heatTransfer = heatTransferRecord.getHeatChange() / totalHeatTransfer;
            } else {
                heatTransfer = heatTransferRecord.getHeatChange();
            }

            recordHeatChange(new HeatChangeRecord(-heatTransfer));

            if (heatTransferRecord.getTargetEntity() != null) {
                heatTransferRecord.getTargetEntity().recordHeatChange(new HeatChangeRecord(heatTransfer));
            }
        }

        heatTransferRecords.clear();
    }

    void recordHeatChange(HeatChangeRecord record) {
        heatChangeRecords.put(record, 0);
    }

    public void changeHeat() {
        for (HeatChangeRecord heatChangeRecord : heatChangeRecords.keySet()) {
            heatChangeRecord.transfer(this);
        }

        heatChangeRecords.clear();
    }

    /*********************************************************************
     *
     */

    /**
     * https://en.wikipedia.org/wiki/Avogadro%27s_law
     */
    double getMolarWeight() {
        return 1;
    }


    /**
     * https://en.wikipedia.org/wiki/Thermal_conductivity
     */
    public double getConductivity() {
        return .0001;
    }

    /**
     * https://en.wikipedia.org/wiki/Viscosity
     */
    public double getViscosity() {
        return .1;
    }

    /*********************************************************************
     * Record classes
     */

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

    public static class MassChangeRecord {

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

    public static class ForceChangeRecord {
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

    public static class HeatTransferRecord {
        final private FluidEntity targetEntity;
        final private double heatChange;

        /**
         * @param targetEntity
         * @param heatChange   Should always be positive.
         */
        public HeatTransferRecord(FluidEntity targetEntity, double heatChange) {
            this.targetEntity = targetEntity;
            this.heatChange = heatChange;
        }

        FluidEntity getTargetEntity() {
            return targetEntity;
        }

        double getHeatChange() {
            return heatChange;
        }
    }

    public static class HeatChangeRecord {
        final private double heatChange;

        HeatChangeRecord(double heatChange) {
            this.heatChange = heatChange;
        }

        void transfer(FluidEntity entity) {
            entity.addHeat(heatChange);
        }
    }
}
