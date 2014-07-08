package fluid.entity;

import javafx.scene.paint.Color;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
 *
 * Created by mjanes on 6/12/2014.
 */
public class FluidEntity implements IDimensionalEntity {

    public static final int SPACE = 5; // spacing between entities, currently writing this that they must be placed on a grid
    private static final double GAS_CONSTANT = .02;


    protected double mX;
    protected double mY;
    protected double mZ;

    protected Array2DRowRealMatrix r4Matrix = new Array2DRowRealMatrix(new double[] {0, 0, 0, 1});

    protected double mDeltaX;
    protected double mDeltaY;
    protected double mDeltaZ;
    protected double mMass;
    protected double mHeat;
    protected Color mInk;

    protected final ConcurrentHashMap<RelativeTransferRecord, Integer> mRelativeTransferRecords = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<AbsoluteTransferRecord, Integer> mAbsoluteTransferRecords = new ConcurrentHashMap<>();

    public FluidEntity(double x, double y, double z, double mass, double temperature) {
        setX(x);
        setY(y);
        setZ(z);
        setMass(mass);
        setTemperature(temperature);
        setInk(Color.TRANSPARENT);
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


    /** Velocity */

    public synchronized void setDeltaX(double deltaX) {
        mDeltaX = deltaX;
    }

    public synchronized void addDeltaX(double deltaDeltaX) {
        setDeltaX(mDeltaX + deltaDeltaX);
    }

    public synchronized double getDeltaX() {
        return mDeltaX;
    }

    public synchronized void applyForceX(double forceX) {
        addDeltaX(forceX / mMass);
    }

    public synchronized double getForceX() {
        return mDeltaX * mMass;
    }


    public synchronized void setDeltaY(double deltaY) {
        mDeltaY = deltaY;
    }

    public synchronized void addDeltaY(double deltaDeltaY) {
        setDeltaY(mDeltaY + deltaDeltaY);
    }

    public synchronized double getDeltaY() {
        return mDeltaY;
    }

    public synchronized void applyForceY(double forceY) {
        addDeltaY(forceY / mMass);
    }

    public synchronized double getForceY() {
        return mDeltaY * mMass;
    }


    public synchronized void setDeltaZ(double deltaZ) {
        mDeltaZ = deltaZ;
    }

    public synchronized void addDeltaZ(double deltaDeltaZ) {
        setDeltaZ(mDeltaZ + deltaDeltaZ);
    }

    public synchronized double getDeltaZ() {
        return mDeltaZ;
    }

    public synchronized void applyForceZ(double forceZ) {
        addDeltaZ(forceZ / mMass);
    }

    public synchronized double getForceZ() {
        return mDeltaZ * mMass;
    }



    /** Mass */

    public synchronized void setMass(double mass) {
        mMass = mass;
    }

    public synchronized double getMass() {
        return mMass;
    }

    public synchronized void addMass(double deltaMass, double massTemperature, Color color) {
        if (mMass + deltaMass < 0) {
            setMass(0);
            setHeat(0);
            return;
        }

        setMass(mMass + deltaMass);
        addHeat(deltaMass * massTemperature);

        if (deltaMass < 0 || color == null || mInk.equals(color)) return;

        // Ink
        double prevRed = mInk.getRed();
        double prevGreen = mInk.getGreen();
        double prevBlue = mInk.getBlue();
        double prevAlpha = mInk.getOpacity();

        double oldProportion = (mMass - deltaMass) / mMass;
        double newProportion = deltaMass / mMass;

        double newRed = prevRed * oldProportion + color.getRed() * newProportion;
        double newGreen = prevGreen * oldProportion + color.getGreen() * newProportion;
        double newBlue = prevBlue * oldProportion + color.getBlue() * newProportion;
        double newAlpha = prevAlpha * oldProportion + color.getOpacity() * newProportion;

        if (newRed < 0) {
            newRed = 0;
        }
        if (newGreen < 0) {
            newGreen = 0;
        }
        if (newBlue < 0) {
            newBlue = 0;
        }

        if (newRed > 1) {
            newRed = 1;
        }
        if (newGreen > 1) {
            newGreen = 1;
        }
        if (newBlue > 1) {
            newBlue = 1;
        }

        setInk(new Color(newRed, newGreen, newBlue, newAlpha));
    }


    /** Ink */

    public synchronized Color getInk() {
        return mInk;
    }

    public synchronized void setInk(Color color) {
        mInk = color;
    }


    /** Heat */

    public synchronized void setTemperature(double temperature) {
        mHeat = temperature * mMass;
    }

    public synchronized double getTemperature() {
        return mHeat / mMass;
    }

    public synchronized void setHeat(double heat) {
        mHeat = heat;
    }

    public synchronized void addHeat(double deltaHeat) {
        setHeat(mHeat = deltaHeat);
    }

    public synchronized double getHeat() { return mHeat; }


    /** Pressure */

    public synchronized double getPressure() {
        // https://en.wikipedia.org/wiki/Pressure
        // http://www.passmyexams.co.uk/GCSE/physics/pressure-temperature-relationship-of-gas-pressure-law.html
        return mHeat * GAS_CONSTANT; // NOTE: GAS_CONSTANT could be a variable that changes based on heat. Phase change
    }


    /** For display */

    public synchronized FluidEntity getNextLocationAsFluidEntity() {
        return new FluidEntity(mX + mDeltaX, mY + mDeltaY, mZ + mDeltaZ, mMass, getTemperature());
    }


    /** Stepping from to the next increment of the simulation */

    public void transferRelativeValues() {
        double totalRatio = 0;
        for (RelativeTransferRecord relativeTransferRecord : mRelativeTransferRecords.keySet()) {
            totalRatio += relativeTransferRecord.getProportion();
        }

        for (RelativeTransferRecord relativeTransferRecord : mRelativeTransferRecords.keySet()) {
            if (totalRatio > 1) {
                recordAbsoluteTransfer(relativeTransferRecord.getTargetEntity(), relativeTransferRecord.getProportion() / totalRatio);
            } else {
                recordAbsoluteTransfer(relativeTransferRecord.getTargetEntity(), relativeTransferRecord.getProportion());
            }
        }
    }

    public void transferAbsoluteValues() {
        for (AbsoluteTransferRecord absoluteTransferRecord : mAbsoluteTransferRecords.keySet()) {
            absoluteTransferRecord.transfer(this);
        }
    }

    public void clear() {
        mRelativeTransferRecords.clear();
        mAbsoluteTransferRecords.clear();
    }


    private void recordAbsoluteTransfer(FluidEntity targetEntity, double ratio) {
        double massTransfer = mMass * ratio;
        double forceXTransfer = getForceX() * ratio;
        double forceYTransfer = getForceY() * ratio;
        Color inkColor = mInk;

        recordAbsoluteTransfer(new AbsoluteTransferRecord(-massTransfer, getTemperature(), -forceXTransfer, -forceYTransfer, null));

        // If targetEntity is null, because the transfer is going off the border of the universe, the values are simply
        // removed from the simulation.
        if (targetEntity == null) return;

        targetEntity.recordAbsoluteTransfer(new AbsoluteTransferRecord(massTransfer, getTemperature(), forceXTransfer, forceYTransfer, inkColor));
    }

    public void recordRelativeTransfer(FluidEntity targetEntity, double ratio) {
        if (ratio < 0 || ratio > 1) {
            System.out.println("Error, ratio = " + ratio);
            return;
        }

        // Do not record transfers to self.
        if (this.equals(targetEntity)) return;

        mRelativeTransferRecords.put(new RelativeTransferRecord(targetEntity, ratio), 0);
    }

    public void recordAbsoluteTransfer(AbsoluteTransferRecord record) {
        mAbsoluteTransferRecords.put(record, 0);
    }


    /**
     * A RelativeTransferRecord is the a record of what proportion of this entity should be transferred to the target
     * entity.
     */
    private static class RelativeTransferRecord {
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
    private static class AbsoluteTransferRecord {

        final private double mMassTransfer;
        final private double mMassTemperature;
        final private double mForceXTransfer;
        final private double mForceYTransfer;
        final private Color mInkColor;


        public AbsoluteTransferRecord(double massTransfer, double massTemperature, double forceXTransfer, double forceYTransfer, Color inkColor) {
            mMassTransfer = massTransfer;
            mMassTemperature = massTemperature;
            mForceXTransfer = forceXTransfer;
            mForceYTransfer = forceYTransfer;
            mInkColor = inkColor;
        }

        public void transfer(FluidEntity entity) {
            entity.addMass(mMassTransfer, mMassTemperature, mInkColor);
            entity.applyForceX(mForceXTransfer);
            entity.applyForceY(mForceYTransfer);
        }
    }
}
