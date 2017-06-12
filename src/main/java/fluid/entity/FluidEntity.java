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


    double forceX;
    double forceY;
    private double forceZ;
    double mass;
    double heat;
    private Color color = new Color(DEFAULT_COLOR.getRed(),
            DEFAULT_COLOR.getGreen(),
            DEFAULT_COLOR.getBlue(),
            DEFAULT_COLOR.getOpacity());

    private double red;
    private double green;
    private double blue;
    private double opacity;

    private final ConcurrentHashMap<FluidEntity, Double> massTransferRecords = new ConcurrentHashMap<>();

    private double pendingDeltaMass;
    private double pendingDeltaHeat;
    private double pendingDeltaForceX;
    private double pendingDeltaForceY;
    private double pendingDeltaRed;
    private double pendingDeltaGreen;
    private double pendingDeltaBlue;
    private double pendingDeltaAlpha;


    public FluidEntity(double x, double y, double z, double mass, double temperature) {
        setX(x);
        setY(y);
        setZ(z);
        setMass(mass);
        heat = temperature * mass;
        setColor(Color.TRANSPARENT);
    }

    @Override
    public synchronized void setX(double x) {
        this.x = x;
        r4Matrix.setEntry(0, 0, x);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public synchronized void setY(double y) {
        this.y = y;
        r4Matrix.setEntry(1, 0, y);
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public synchronized void setZ(double z) {
        this.z = z;
        r4Matrix.setEntry(2, 0, z);
    }

    @Override
    public double getZ() {
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
     * Velocity/Force
     */

    public double getDeltaX() {
        if (mass < -FUZZ) {
            throw new IllegalStateException("Error: Mass cannot be negative");
        }
        if (mass <= 0) {
            return 0;
        }
        return forceX / mass;
    }

    public double getDeltaY() {
        if (mass < -FUZZ) {
            throw new IllegalStateException("Error: Mass cannot be negative");
        }
        if (mass <= 0) {
            return 0;
        }
        return forceY / mass;
    }

    private double getDeltaZ() {
        if (mass < -FUZZ) {
            throw new IllegalStateException("Error: Mass cannot be negative");
        }
        if (mass <= 0) {
            return 0;
        }
        return forceZ / mass;
    }

    public synchronized void recordForceChange(double deltaForceX, double deltaForceY) {
        pendingDeltaForceX += deltaForceX;
        pendingDeltaForceY += deltaForceY;
    }

    private synchronized void changeForce() {
        forceX += pendingDeltaForceX;
        pendingDeltaForceX = 0;

        forceY += pendingDeltaForceY;
        pendingDeltaForceY = 0;
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


    /**
     * Ink
     */

    public Color getColor() {
        return color;
    }

    public synchronized void setColor(Color color) {
        this.color = color;
    }

    synchronized void changeColor() {
        double newRed = color.getRed() + pendingDeltaRed;
        double newGreen = color.getGreen() + pendingDeltaGreen;
        double newBlue = color.getBlue() + pendingDeltaBlue;
        double newAlpha = color.getOpacity() + pendingDeltaAlpha;

        pendingDeltaRed = 0;
        pendingDeltaGreen = 0;
        pendingDeltaBlue = 0;
        pendingDeltaAlpha = 0;

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


    /**
     * Heat/temperature
     */

    public synchronized void setHeat(double heat) {
        this.heat = heat;
    }

    public double getTemperature() {
        if (mass < -FUZZ) {
            throw new IllegalStateException("Error: Mass cannot be negative");
        }
        if (mass <= 0) {
            return 0;
        }
        return heat / mass;
    }

    public synchronized void recordHeatChange(double deltaHeat) {
        pendingDeltaHeat += deltaHeat;
    }

    synchronized void changeHeat() {
        heat += pendingDeltaHeat;
        pendingDeltaHeat = 0;
    }



    /**
     * Pressure
     * <p>
     * We are presuming that the volume of a fluid entity cell is constant, but the amount of mass, and the heat
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

    private void convertMassTransfer() {
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

            recordMassChange(-massTransfer);
            recordHeatChange(-massTransfer * getTemperature());
            targetEntity.recordForceChange(-massTransfer * getDeltaX(), -massTransfer * getDeltaY());

            targetEntity.recordMassChange(massTransfer);
            targetEntity.recordHeatChange(massTransfer * getTemperature());
            targetEntity.recordForceChange(massTransfer * getDeltaX(), massTransfer * getDeltaY());

            /*
            // TODO: Color here

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
            */

        }

        massTransferRecords.clear();
    }

    public synchronized void recordMassChange(double deltaMass) {
        pendingDeltaMass += deltaMass;
    }

    protected synchronized void changeMass() {
        if (mass + pendingDeltaMass < -FUZZ) {
            throw new IllegalStateException("Error: Mass cannot be less than 0");
        }
        if (mass + pendingDeltaMass <= FUZZ) {
            mass = 0;
            heat = 0;
            forceX = 0;
            forceY = 0;
            forceZ = 0;
        } else {
            mass += pendingDeltaMass;
        }
        pendingDeltaMass = 0;
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
    private double getConductivity() {
        return .0001;
    }

    /**
     * https://en.wikipedia.org/wiki/Viscosity
     */
    private double getViscosity() {
        return .1;
    }


    /**
     * Incrementing from one step to the next
     */

    public synchronized void executePendingEnergy() {
        changeHeat();
        changeForce();
    }

    public synchronized void executePendingMass() {
        convertMassTransfer();
        changeMass();
        changeHeat();
        changeForce();
        changeColor();
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
     * If the two entities heat difference, record a heat transfer from the one with the higher heat to
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
            double forceAvailableForTransfer = getDeltaY() * getViscosity() + other.getDeltaY() * other.getViscosity();

            double forceLossFromA = -(getDeltaY() * getViscosity());
            double forceGainForA = ((getMass() / totalMass) * forceAvailableForTransfer);
            double forceTransfer = forceLossFromA + forceGainForA;

            recordForceChange(0, forceTransfer);
            recordForceChange(0, -forceTransfer);
        } else if (getY() != other.getY() && getDeltaX() - other.forceX != 0) {
            double forceAvailableForTransfer = getDeltaX() * getViscosity() + other.getDeltaX() * other.getViscosity();

            double forceLossFromA = -(getDeltaX() * getViscosity());
            double forceGainForA = ((getMass() / totalMass) * forceAvailableForTransfer);
            double forceTransfer = forceLossFromA + forceGainForA;

            recordForceChange(forceTransfer, 0);
            recordForceChange(-forceTransfer, 0);
        }
    }

}
