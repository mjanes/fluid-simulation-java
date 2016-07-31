package fluid.entity;

import javafx.scene.paint.Color;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
 * <p>
 * Created by mjanes on 6/12/2014.
 */
public class FluidEntity implements IFluidEntity {

    private double mX;
    private double mY;
    private double mZ;

    private final Array2DRowRealMatrix r4Matrix = new Array2DRowRealMatrix(new double[]{0, 0, 0, 1});

    private double mDeltaX;
    private double mDeltaY;
    private double mDeltaZ;
    private double mMass;
    private double mHeat;
    private Color mColor;

    private final ConcurrentHashMap<MassTransferRecord, Integer> mMassTransferRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<MassChangeRecord, Integer> mMassChangeRecords = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<ForceChangeRecord, Integer> mForceChangeRecords = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<HeatTransferRecord, Integer> mHeatTransferRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<HeatChangeRecord, Integer> mHeatChangeRecords = new ConcurrentHashMap<>();

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
        mX = x;
        r4Matrix.setEntry(0, 0, x);
    }

    @Override
    public synchronized double getX() {
        return mX;
    }

    @Override
    public synchronized void setY(double y) {
        mY = y;
        r4Matrix.setEntry(1, 0, y);
    }

    @Override
    public synchronized double getY() {
        return mY;
    }

    @Override
    public synchronized void setZ(double z) {
        mZ = z;
        r4Matrix.setEntry(2, 0, z);
    }

    @Override
    public synchronized double getZ() {
        return mZ;
    }

    @Override
    public synchronized double getDistance(IDimensionalEntity other) {
        return IDimensionalEntity.getDistance(this, other);
    }

    @Override
    public synchronized Array2DRowRealMatrix getR4Matrix() {
        return r4Matrix;
    }


    /**
     * Velocity
     */

    public synchronized void setDeltaX(double deltaX) {
        mDeltaX = deltaX;
    }

    private synchronized void addDeltaX(double deltaDeltaX) {
        setDeltaX(mDeltaX + deltaDeltaX);
    }

    @Override
    public synchronized double getDeltaX() {
        return mDeltaX;
    }

    @Override
    public synchronized void addForceX(double forceX) {
        if (mMass <= 0) {
            setDeltaX(0);
            return;
        }

        // TODO: Probably need some efficiency thing here, if mass is too small, and force is too high, turn some energy
        // into heat?
        addDeltaX(forceX / mMass);
    }

    @Override
    public synchronized double getForceX() {
        return mDeltaX * mMass;
    }


    public synchronized void setDeltaY(double deltaY) {
        mDeltaY = deltaY;
    }

    @Override
    public synchronized double getForceY() {
        return mDeltaY * mMass;
    }

    private synchronized void addDeltaY(double deltaDeltaY) {
        setDeltaY(mDeltaY + deltaDeltaY);
    }

    @Override
    public synchronized double getDeltaY() {
        return mDeltaY;
    }

    @Override
    public synchronized void addForceY(double forceY) {
        if (mMass <= 0) {
            setDeltaY(0);
            return;
        }

        // TODO: Probably need some efficiency thing here, if mass is too small, and force is too high, turn some energy
        // into heat?
        addDeltaY(forceY / mMass);
    }


    private synchronized void setDeltaZ(double deltaZ) {
        mDeltaZ = deltaZ;
    }

    private synchronized void addDeltaZ(double deltaDeltaZ) {
        setDeltaZ(mDeltaZ + deltaDeltaZ);
    }

    public synchronized double getDeltaZ() {
        return mDeltaZ;
    }

    @Override
    public synchronized void addForceZ(double forceZ) {
        if (mMass <= 0) {
            setDeltaZ(0);
            return;
        }


        // TODO: Probably need some efficiency thing here, if mass is too small, and force is too high, turn some energy
        // into heat?
        addDeltaZ(forceZ / mMass);
    }


    /**
     * Mass
     */

    private synchronized void setMass(double mass) {
        if (mass < 0) {
            mMass = 0;
            return;
        }
        mMass = mass;
    }

    @Override
    public synchronized double getMass() {
        return mMass;
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

        setMass(mMass + deltaMass);

        double oldProportion = (mMass - deltaMass) / mMass;
        double newProportion = deltaMass / mMass;

        double newDeltaX = oldDeltaX * oldProportion + incomingDeltaX * newProportion;
        double newDeltaY = oldDeltaY * oldProportion + incomingDeltaY * newProportion;

        setDeltaX(newDeltaX);
        setDeltaY(newDeltaY);

        // Unlike force, heat is independent of mass.
        addHeat(deltaMass * massTemperature);

        if (!(color == null || color.equals(mColor))) {
            // Ink - doing this in a separate block
            double prevRed = mColor.getRed();
            double prevGreen = mColor.getGreen();
            double prevBlue = mColor.getBlue();
            double prevAlpha = mColor.getOpacity();

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
    public void subtractMass(double deltaMass) {
        if (mMass + deltaMass <= 0) {
            setMass(0);
            setHeat(0);
            setDeltaX(0);
            setDeltaY(0);
            setDeltaZ(0);
        } else {
            double proportion = deltaMass / mMass;
            setMass(mMass + deltaMass);
            addHeat(mHeat * proportion);
        }
    }


    /**
     * Ink
     */

    public synchronized Color getColor() {
        return mColor;
    }

    public synchronized void setColor(Color color) {
        mColor = color;
    }


    /**
     * Heat
     */

    public synchronized void setTemperature(double temperature) {
        mHeat = temperature * mMass;
    }

    @Override
    public synchronized double getTemperature() {
        if (mMass <= 0) return 0;
        return mHeat / mMass;
    }

    private synchronized void setHeat(double heat) {
        if (heat < 0) {
            mHeat = 0;
            return;
        }
        mHeat = heat;
    }

    @Override
    public synchronized void addHeat(double deltaHeat) {
        setHeat(mHeat + deltaHeat);
    }

    @Override
    public synchronized double getHeat() {
        return mHeat;
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
        return GAS_CONSTANT * mMass * getTemperature() / getMolarWeight();
    }


    /**
     * For display
     */

    public synchronized FluidEntity getNextLocationAsFluidEntity(double velocityFactor) {
        return new FluidEntity(mX + (mDeltaX * velocityFactor), mY + (mDeltaY * velocityFactor), mZ + (mDeltaZ * velocityFactor), mMass, getTemperature());
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
    @Override
    public void recordMassTransfer(FluidEntity targetEntity, double proportion) {
        if (proportion < 0 || proportion > 1) {
            System.out.println("Error, proportion = " + proportion);
            return;
        }

        if (mMass == 0) return;

        // Do not record transfers to self.
        if (this.equals(targetEntity)) return;

        mMassTransferRecords.put(new MassTransferRecord(targetEntity, proportion), 0);
    }

    /**
     * Stage 1 of the mass transfer steps.
     */
    @Override
    public void convertMassTransferToAbsoluteChange() {
        double totalRatio = 0;
        for (MassTransferRecord massTransferRecord : mMassTransferRecords.keySet()) {
            totalRatio += massTransferRecord.getProportion();
        }

        for (MassTransferRecord massTransferRecord : mMassTransferRecords.keySet()) {
            double massTransfer;
            if (totalRatio > 1) {
                massTransfer = getMass() * massTransferRecord.getProportion() / totalRatio;
            } else {
                massTransfer = getMass() * massTransferRecord.getProportion();
            }

            recordMassChange(new MassChangeRecord(-massTransfer, getTemperature(), getDeltaX(), getDeltaY(), mColor));

            if (massTransferRecord.getTargetEntity() != null) {
                massTransferRecord.getTargetEntity().recordMassChange(new MassChangeRecord(massTransfer, getTemperature(), getDeltaX(), getDeltaY(), mColor));
            }
        }

        mMassTransferRecords.clear();
    }

    /**
     * Transferring mass to a fluid entity
     */
    @Override
    public void recordMassChange(MassChangeRecord record) {
        mMassChangeRecords.put(record, 0);
    }

    public void changeMass() {
        for (MassChangeRecord transferRecord : mMassChangeRecords.keySet()) {
            transferRecord.transfer(this);
        }

        mMassChangeRecords.clear();
    }


    /**
     * Force transfercs
     */

    @Override
    public void recordForceChange(ForceChangeRecord record) {
        mForceChangeRecords.put(record, 0);
    }

    public void changeForce() {
        for (ForceChangeRecord forceChangeRecord : mForceChangeRecords.keySet()) {
            forceChangeRecord.transfer(this);
        }

        mForceChangeRecords.clear();
    }

    /**
     * Heat transfers
     *
     * This is done in two parts, similar to how mass transfers are done, because heat cannot go negative, and thus,
     */

    /**
     * Record heat transfer away from this entity, to target entity, in HeatTransferRecord.
     */
    @Override
    public void recordHeatTransfer(HeatTransferRecord record) {
        mHeatTransferRecords.put(record, 0);
    }

    public void convertHeatTransferToAbsoluteChange() {
        double totalHeatTransfer = 0;
        for (HeatTransferRecord heatChangeRecord : mHeatTransferRecords.keySet()) {
            totalHeatTransfer += heatChangeRecord.getHeatChange();
        }

        for (HeatTransferRecord heatTransferRecord : mHeatTransferRecords.keySet()) {
            double heatTransfer;
            if (totalHeatTransfer - mHeat < 0) {
                heatTransfer = heatTransferRecord.getHeatChange() / totalHeatTransfer;
            } else {
                heatTransfer = heatTransferRecord.getHeatChange();
            }

            recordHeatChange(new HeatChangeRecord(-heatTransfer));

            if (heatTransferRecord.getTargetEntity() != null) {
                heatTransferRecord.getTargetEntity().recordHeatChange(new HeatChangeRecord(heatTransfer));
            }
        }

        mHeatTransferRecords.clear();
    }

    @Override
    public void recordHeatChange(HeatChangeRecord record) {
        mHeatChangeRecords.put(record, 0);
    }

    public void changeHeat() {
        for (HeatChangeRecord heatChangeRecord : mHeatChangeRecords.keySet()) {
            heatChangeRecord.transfer(this);
        }

        mHeatChangeRecords.clear();
    }
}
