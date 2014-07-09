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
    private static final double GAS_CONSTANT = .02; // TODO: Make this variable based on fluid material

    protected double mX;
    protected double mY;
    protected double mZ;

    protected Array2DRowRealMatrix r4Matrix = new Array2DRowRealMatrix(new double[] {0, 0, 0, 1});

    protected double mDeltaX;
    protected double mDeltaY;
    protected double mDeltaZ;
    protected double mMass;
    protected double mHeat;
    protected Color mColor;

    protected final ConcurrentHashMap<RelativeTransferRecord, Integer> mRelativeTransferRecords = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<TransferToRecord, Integer> mIncomingTransferRecords = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<TransferAwayRecord, Integer> mOutgoingTransferRecords = new ConcurrentHashMap<>();

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

    public synchronized void addForceX(double forceX) {
        if (mMass <= 0) {
            setDeltaX(0);
            return;
        }

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

    public synchronized void addForceY(double forceY) {
        if (mMass <= 0) {
            setDeltaY(0);
            return;
        }

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

    public synchronized void addForceZ(double forceZ) {
        if (mMass <= 0) {
            setDeltaY(0);
            return;
        }
        addDeltaZ(forceZ / mMass);
    }

    public synchronized double getForceZ() {
        return mDeltaZ * mMass;
    }



    /** Mass */

    public synchronized void setMass(double mass) {
        if (mass < 0) {
            mMass = 0;
            return;
        }
        mMass = mass;
    }

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
     *
     * Heat though will be decreased.
     *
     * I suppose force is analogous to heat, and delta is analogous to temperature here.
     */
    private void subtractMass(double deltaMass) {
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



    /** Ink */

    public synchronized Color getColor() {
        return mColor;
    }

    public synchronized void setColor(Color color) {
        mColor = color;
    }


    /** Heat */

    public synchronized void setTemperature(double temperature) {
        mHeat = temperature * mMass;
    }

    public synchronized double getTemperature() {
        return mHeat / mMass;
    }

    public synchronized void setHeat(double heat) {
        if (heat < 0) {
            mHeat = 0;
            return;
        }
        mHeat = heat;
    }

    public synchronized void addHeat(double deltaHeat) {
        setHeat(mHeat + deltaHeat);
    }

    public synchronized double getHeat() { return mHeat; }


    /**
     * Pressure
     *
     * We are presuming that the volume of of a fluid entity cell is constant, but the amount of mass, and
     * the temperature of that mass may change.
     *
     * https://en.wikipedia.org/wiki/Pressure
     * http://www.passmyexams.co.uk/GCSE/physics/pressure-temperature-relationship-of-gas-pressure-law.html
     */
    public synchronized double getPressure() {
        return GAS_CONSTANT * mMass * getTemperature(); // NOTE: GAS_CONSTANT could be a variable that changes based on heat. Phase change
    }


    /** For display */

    public synchronized FluidEntity getNextLocationAsFluidEntity(double velocityFactor) {
        return new FluidEntity(mX + (mDeltaX * velocityFactor), mY + (mDeltaY * velocityFactor), mZ + (mDeltaZ * velocityFactor), mMass, getTemperature());
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

        mRelativeTransferRecords.clear();
    }

    public void transferOutgoingValues() {
        for (TransferAwayRecord transferRecord : mOutgoingTransferRecords.keySet()) {
            transferRecord.transfer(this);
        }

        mOutgoingTransferRecords.clear();
    }

    public void transferIncomingValues() {
        for (TransferToRecord transferRecord : mIncomingTransferRecords.keySet()) {
            transferRecord.transfer(this);
        }

        mIncomingTransferRecords.clear();
    }

    private void recordAbsoluteTransfer(FluidEntity targetEntity, double ratio) {
        if (mMass == 0 || ratio == 0) return;

        double massTransfer = mMass * ratio;

        recordTransferAway(new TransferAwayRecord(-massTransfer));

        // If targetEntity is null, because the transfer is going off the border of the universe, the values are simply
        // removed from the simulation.
        // TODO: Implement better/different borders for the the universe.
        if (targetEntity == null) return;

        targetEntity.recordTransferTo(new TransferToRecord(massTransfer, getTemperature(), getDeltaX(), getDeltaY(), mColor));
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

    public void recordTransferTo(TransferToRecord record) {
        mIncomingTransferRecords.put(record, 0);
    }

    public void recordTransferAway(TransferAwayRecord record) {
        mOutgoingTransferRecords.put(record, 0);
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
    private static class TransferToRecord {

        final private double mMassTransfer;
        final private double mMassTemperature;
        final private double mVelocityX;
        final private double mVelocityY;
        final private Color mInkColor;

        public TransferToRecord(double massTransfer, double massTemperature, double velocityX, double velocityY, Color inkColor) {
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

    private static class TransferAwayRecord {
        final private double mMassTransfer;

        public TransferAwayRecord(double massTransfer) {
            mMassTransfer = massTransfer;
        }

        public void transfer(FluidEntity entity) {
            entity.subtractMass(mMassTransfer);
        }

    }
}
