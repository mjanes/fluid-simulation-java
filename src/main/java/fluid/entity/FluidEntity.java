package fluid.entity;

import fluid.physics.Universe;
import javafx.scene.paint.Color;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
 * <p>
 * Created by mjanes on 6/12/2014.
 */
public class FluidEntity implements DimensionalEntity {

    public static final double FUZZ = .0000001;

    public static final int SPACE = 5; // spacing between entities, currently writing this that they must be placed on a grid
    private static final double GAS_CONSTANT = .02;

    public static final double DEFAULT_TEMPERATURE = 10;
    public static final double DEFAULT_MASS = 10;
    static final Color DEFAULT_COLOR = Color.TRANSPARENT;

    public static final double CELL_AREA = Math.pow(SPACE, 2);


    private double x;
    private double y;
    private double z;

    private final Array2DRowRealMatrix r4Matrix = new Array2DRowRealMatrix(new double[]{0, 0, 0, 1});

    private double deltaX;
    private double deltaY;
    private double deltaZ;
    protected double mass;
    protected double temperature;
    private Color color;

    protected final ConcurrentHashMap<FluidEntity, Double> massTransferRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<MassChangeRecord, Integer> massChangeRecords = new ConcurrentHashMap<>();

    private double pendingDeltaHeat;
    private double pendingDeltaForceX;
    private double pendingDeltaForceY;

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
        if (mass < -FUZZ) {
            throw new IllegalStateException("Error: Mass cannot be less than 0");
        }
        if (mass <= 0) {
            setDeltaX(0);
            return;
        }

        addDeltaX(forceX / mass);
    }

    private double getForceX() {
        return deltaX * mass;
    }


    public synchronized void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }

    private double getForceY() {
        return deltaY * mass;
    }

    private synchronized void addDeltaY(double deltaDeltaY) {
        setDeltaY(deltaY + deltaDeltaY);
    }

    public double getDeltaY() {
        return deltaY;
    }

    public synchronized void addForceY(double forceY) {
        if (mass < -FUZZ) {
            throw new IllegalStateException("Error: Mass cannot be less than 0");
        }
        if (mass <= 0) {
            setDeltaY(0);
            return;
        }

        addDeltaY(forceY / mass);
    }


    private synchronized void setDeltaZ(double deltaZ) {
        this.deltaZ = deltaZ;
    }

    private synchronized void addDeltaZ(double deltaDeltaZ) {
        setDeltaZ(deltaZ + deltaDeltaZ);
    }

    public double getDeltaZ() {
        return deltaZ;
    }

    public synchronized void addForceZ(double forceZ) {
        if (mass <= 0) {
            setDeltaZ(0);
            return;
        }

        addDeltaZ(forceZ / mass);
    }


    /**
     * Mass
     */

    public synchronized void setMass(double mass) {
        if (mass < 0) {
            this.mass = 0;
            return;
        }
        this.mass = mass;
    }

    public double getMass() {
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
        double oldTemperature = getTemperature();

        setMass(mass + deltaMass);

        // TODO: Simplify and add force and heat instead of setting delta and temp?
        double oldProportion = (mass - deltaMass) / mass;
        double newProportion = deltaMass / mass;

        double newDeltaX = oldDeltaX * oldProportion + incomingDeltaX * newProportion;
        double newDeltaY = oldDeltaY * oldProportion + incomingDeltaY * newProportion;
        double newTemperature = oldTemperature * oldProportion + massTemperature * newProportion;

        setDeltaX(newDeltaX);
        setDeltaY(newDeltaY);
        setTemperature(newTemperature);

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
    private synchronized void subtractMass(double deltaMass) {
        if (mass + deltaMass < -FUZZ) {
            throw new IllegalStateException("Error: Mass cannot be less than 0");
        }
        if (mass + deltaMass <= FUZZ) {
            setMass(0);
            setTemperature(0);
            setDeltaX(0);
            setDeltaY(0);
            setDeltaZ(0);
        } else {
            setMass(mass + deltaMass);
        }
    }


    /**
     * Ink
     */

    public Color getColor() {
        return color;
    }

    public synchronized void setColor(Color color) {
        this.color = color;
    }


    /**
     * Heat
     */

    public synchronized void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTemperature() {
        return temperature;
    }

    public synchronized void addHeat(double deltaHeat) {
        if (mass < -FUZZ) {
            throw new IllegalStateException("Error: Mass cannot be negative");
        }
        if (mass <= 0) {
            return;
        }
        double deltaTemperature = deltaHeat / mass;

        if (this.temperature + deltaTemperature < FUZZ) {
            throw new IllegalStateException("Error: Temperature cannot be negative");
        }
        this.temperature += deltaTemperature;
    }


    /**
     * Pressure
     * <p>
     * We are presuming that the volume of a fluid entity cell is constant, but the amount of mass, and the temperature
     * of that mass may change.
     * <p>
     * https://en.wikipedia.org/wiki/Pressure
     * http://www.passmyexams.co.uk/GCSE/physics/pressure-temperature-relationship-of-gas-pressure-law.html
     * https://en.wikipedia.org/wiki/Charles%27s_Law
     */
    public double getPressure() {
        return GAS_CONSTANT * mass * getTemperature() / getMolarWeight();
    }


    /**
     * For display
     */

    public void getNextLocationAsFluidEntity(Array2DRowRealMatrix vector, double velocityFactor) {
        vector.setEntry(0, 0, getX() + getDeltaX() * velocityFactor);
        vector.setEntry(1, 0, getY() + getDeltaY() * velocityFactor);
        vector.setEntry(2, 0, getZ() + getDeltaZ() * velocityFactor);
    }


    /**
     * Mass transfers
     *
     * These are done in two stages, so that the order in which each method of the same type is applied to a cell does
     * not matter.
     */

    /**
     * Records a transfer from this entity to targetEntity, a proportion of this entity's values
     */
    public void recordMassTransferTo(FluidEntity targetEntity, double proportion) {
        if (proportion < 0 || proportion > 1) {
            System.out.println("Error, proportion = " + proportion);
            return;
        }

        if (mass == 0) return;

        // Do not record transfers to self.
        if (this.equals(targetEntity)) return;

        if (massTransferRecords.get(targetEntity) != null) {
            proportion += massTransferRecords.get(targetEntity);
        }
        massTransferRecords.put(targetEntity, proportion);
    }

    /**
     * Stage 1 of the mass transfer steps.
     */
    public void convertMassTransferToAbsoluteChange() {
        double totalRatio = 0;
        for (Double proportion : massTransferRecords.values()) {
            totalRatio += proportion;
        }

        for (FluidEntity targetEntity : massTransferRecords.keySet()) {
            double massTransfer;
            if (totalRatio > 1) {
                massTransfer = getMass() * massTransferRecords.get(targetEntity) / totalRatio;
            } else {
                massTransfer = getMass() * massTransferRecords.get(targetEntity);
            }

            recordMassChange(new MassChangeRecord(-massTransfer, getTemperature(), getDeltaX(), getDeltaY(), color));

            targetEntity.recordMassChange(new MassChangeRecord(massTransfer, getTemperature(), getDeltaX(), getDeltaY(), color));
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

    public void recordForceChange(double deltaForceX, double deltaForceY) {
        pendingDeltaForceX += deltaForceX;
        pendingDeltaForceY += deltaForceY;
    }

    public void changeForce() {
        addForceX(pendingDeltaForceX);
        pendingDeltaForceX = 0;

        addForceY(pendingDeltaForceY);
        pendingDeltaForceY = 0;
    }

    /**
     * Heat transfers
     */

    public void recordHeatChange(double deltaHeat) {
        pendingDeltaHeat += deltaHeat;
    }

    public void changeHeat() {
        addHeat(pendingDeltaHeat);
        pendingDeltaHeat = 0;
    }


    /**
     * https://en.wikipedia.org/wiki/Avogadro%27s_law
     */
    private double getMolarWeight() {
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

    static class MassChangeRecord {

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

    /****
     * Interactions
     */

    public void applySoloEffects() {
        gravity();
    }

    private void gravity() {
        recordForceChange(0, getMass() * -Universe.GRAVITATIONAL_CONSTANT);
    }

    public void applyNeighborInteractions(FluidEntity other) {
        applyHeatConduction(other);
        applyPressure(other);
        //applyViscosityBetweenCells(other);
    }

    /**
     * If the two entities temperature difference, record a heat transfer from the one with the higher temperature to
     * the lower. The amount of heat transferred is dependant upon the conductivity of the entities.
     * <p>
     * Newton's law of cooling states that the rate of heat loss of a body is proportional to the difference in temperatures between the body and its surroundings.
     * <p>
     * https://en.wikipedia.org/wiki/Thermal_conductivity
     * https://en.wikipedia.org/wiki/Heat_equation
     * https://en.wikipedia.org/wiki/Newton%27s_law_of_cooling
     * <p>
     * TODO: Actually use heat equation
     */
    void applyHeatConduction(FluidEntity other) {
        double temperatureDifference = getTemperature() - other.getTemperature();
        if (temperatureDifference > FUZZ) {
            double heatAvailableForTransfer = (getMass() * temperatureDifference * getConductivity()) / Universe.MAX_NEIGHBORS;
            recordHeatChange(-heatAvailableForTransfer);
            other.recordHeatChange(heatAvailableForTransfer);
        }
    }

    private void applyPressure(FluidEntity other) {
        double pressureDifference = getPressure() - other.getPressure();
        if (pressureDifference > 0) {
            if (Math.abs(getX() - other.getX()) > FUZZ) {
                if (getX() > other.getX()) {
                    other.recordForceChange(-pressureDifference, 0);
                } else {
                    other.recordForceChange(pressureDifference, 0);
                }
            }
            if (Math.abs(getY() - other.getY()) > FUZZ) {
                if (getY() > other.getY()) {
                    other.recordForceChange(0, -pressureDifference);
                } else {
                    other.recordForceChange(0, pressureDifference);
                }
            }
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Viscosity
     * https://en.wikipedia.org/wiki/Shear_stress
     * <p>
     * TODO: Make this math cleaner and easier to understand.
     * TODO: Make this not redundant, since it's being applied twice
     */
    private void applyViscosityBetweenCells(FluidEntity other) {
        double totalMass = getMass() + other.getMass();

        if (getX() != other.getX() && getDeltaY() - other.getDeltaY() != 0) {
            double forceAvailableForTransfer = getForceY() * getViscosity() + other.getForceY() * other.getViscosity();

            double forceLossFromA = -(getForceY() * getViscosity());
            double forceGainForA = ((getMass() / totalMass) * forceAvailableForTransfer);
            double forceTransfer = forceLossFromA + forceGainForA;

            recordForceChange(0, forceTransfer);
            recordForceChange(0, -forceTransfer);
        } else if (getY() != other.getY() && getDeltaX() - other.getDeltaX() != 0) {
            double forceAvailableForTransfer = getForceX() * getViscosity() + other.getForceX() * other.getViscosity();

            double forceLossFromA = -(getForceX() * getViscosity());
            double forceGainForA = ((getMass() / totalMass) * forceAvailableForTransfer);
            double forceTransfer = forceLossFromA + forceGainForA;

            recordForceChange(forceTransfer, 0);
            recordForceChange(-forceTransfer, 0);
        }
    }

}
