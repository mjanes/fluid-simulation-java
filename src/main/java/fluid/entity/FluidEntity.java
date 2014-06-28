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

    protected double mX;
    protected double mY;
    protected double mZ;

    protected Array2DRowRealMatrix r4Matrix = new Array2DRowRealMatrix(new double[] {0, 0, 0, 1});

    protected double mDeltaX;
    protected double mDeltaY;
    protected double mDeltaZ;
    protected double mMass;     // TODO: Add density in addition to mass.
    protected double mHeat;

    protected double mInkMass;
    protected Color mInkColor;
    protected double mDisplayRadius; // Display Radius


    protected final ConcurrentHashMap<RelativeTransferRecord, Integer> mRelativeTransferRecords = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<AbsoluteTransferRecord, Integer> mAbsoluteTransferRecords = new ConcurrentHashMap<>();

    public FluidEntity(double x, double y, double z, double mass, double heat) {
        setX(x);
        setY(y);
        setZ(z);
        setMass(mass);
        setHeat(heat);
        setInk(0, Color.BLACK);
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

    public synchronized void setDeltaY(double deltaY) {
        mDeltaY = deltaY;
    }

    public synchronized void addDeltaY(double deltaDeltaY) {
        setDeltaY(mDeltaY + deltaDeltaY);
    }

    public synchronized double getDeltaY() {
        return mDeltaY;
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


    /** Mass */

    public synchronized void setMass(double mass) {
        mMass = mass;
    }

    public synchronized double getMass() {
        return mMass;
    }

    public synchronized void addMass(double deltaMass) {
        if (mMass + deltaMass < 0) {
            //System.out.println("Mass being set to less than 0");
            setMass(0);
            return;
        }
        setMass(mMass + deltaMass);
    }


    /** Radius */

    public synchronized double getDisplayRadius() {
        return mDisplayRadius;
    }

    /**
     * Currently done in 2d
     */
    private synchronized void setDisplayRadius() {
        mDisplayRadius = mInkMass > 1 ? Math.sqrt(mInkMass) : 0;
    }


    /** Ink */

    public synchronized Color getInkColor() {
        return mInkColor;
    }

    public synchronized void setInk(double ink, Color color) {
        mInkMass = ink;
        mInkColor = color;
        setDisplayRadius();
    }

    public synchronized void addInk(double deltaInkMass, Color newColor) {
        if (deltaInkMass <= 0 || newColor == null || mInkColor.equals(newColor)) {
            setInk(mInkMass + deltaInkMass, mInkColor);
            return;
        }

        double prevRed = mInkColor.getRed();
        double prevGreen = mInkColor.getGreen();
        double prevBlue = mInkColor.getBlue();
        double newInkMass = mInkMass + deltaInkMass;

        double oldProportion = mInkMass / newInkMass;
        double newProportion = deltaInkMass / newInkMass;

        double newRed = prevRed * oldProportion + newColor.getRed() * newProportion;
        double newGreen = prevGreen * oldProportion + newColor.getGreen() * newProportion;
        double newBlue = prevBlue * oldProportion + newColor.getBlue() * newProportion;

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

        setInk(newInkMass, new Color(newRed, newGreen, newBlue, 1));
    }


    // TODO: Force = mass * velocity

    // TODO: Force


    /** Heat */

    public synchronized void setHeat(double heat) {
        mHeat = heat;
    }

    public synchronized double getHeat() {
        return mHeat;
    }

    public synchronized void addHeat(double deltaHeat) {
        mHeat += deltaHeat;
    }


    /** For display */

    public synchronized FluidEntity getNextLocationAsFluidEntity() {
        return new FluidEntity(mX + mDeltaX, mY + mDeltaY, mZ + mDeltaZ, mMass, mHeat);
    }


    /** Stepping from to the next increment of the simulation */

    public void transferRelativeValues() {
        double totalRatio = 0;
        for (RelativeTransferRecord relativeTransferRecord : mRelativeTransferRecords.keySet()) {
            totalRatio += relativeTransferRecord.getRatio();
        }

        for (RelativeTransferRecord relativeTransferRecord : mRelativeTransferRecords.keySet()) {
            if (totalRatio > 1) {
                recordAbsoluteTransfer(relativeTransferRecord.getTargetEntity(), relativeTransferRecord.getRatio() / totalRatio);
            } else {
                recordAbsoluteTransfer(relativeTransferRecord.getTargetEntity(), relativeTransferRecord.getRatio());
            }
        }

        mRelativeTransferRecords.clear();
    }

    public void transferAbsoluteValues() {
        for (AbsoluteTransferRecord absoluteTransferRecord : mAbsoluteTransferRecords.keySet()) {
            absoluteTransferRecord.transfer(this);
        }

        mAbsoluteTransferRecords.clear();
    }


    private void recordAbsoluteTransfer(FluidEntity targetEntity, double ratio) {
        double massTransfer = mMass * ratio;
        double deltaXTransfer = mDeltaX * ratio;
        double deltaYTransfer = mDeltaY * ratio;
        double heatTransfer = mHeat * ratio;
        double inkTransfer = mInkMass * ratio;

        recordAbsoluteTransfer(new AbsoluteTransferRecord(-massTransfer, -deltaXTransfer, -deltaYTransfer, -heatTransfer, -inkTransfer, null));

        if (targetEntity == null) return;

        targetEntity.recordAbsoluteTransfer(new AbsoluteTransferRecord(massTransfer, deltaXTransfer, deltaYTransfer, heatTransfer, inkTransfer, mInkColor));
    }

    public void recordRelativeTransfer(FluidEntity targetEntity, double ratio) {
        if (ratio < 0 || ratio > 1) {
            System.out.println("Error, ratio = " + ratio);
            return;
        }

        if (this.equals(targetEntity)) {
            return;
        }

        mRelativeTransferRecords.put(new RelativeTransferRecord(targetEntity, ratio), 0);
    }

    public void recordAbsoluteTransfer(AbsoluteTransferRecord record) {
        mAbsoluteTransferRecords.put(record, 0);
    }


    private static class RelativeTransferRecord {
        private FluidEntity mTargetEntity;
        private double mRatio;

        public RelativeTransferRecord(FluidEntity targetEntity, double ratio) {
            mTargetEntity = targetEntity;
            mRatio = ratio;
        }

        public FluidEntity getTargetEntity() { return mTargetEntity; }

        public double getRatio() { return mRatio; }

    }

    private static class AbsoluteTransferRecord {

        double mMassTransfer;
        double mDeltaXTransfer;
        double mDeltaYTransfer;
        double mHeatTransfer;
        double mInkTransfer;
        Color mInkColor;


        public AbsoluteTransferRecord(double massTransfer, double deltaXTransfer, double deltaYTransfer, double heatTransfer, double inkTransfer, Color inkColor) {
            mMassTransfer = massTransfer;
            mDeltaXTransfer = deltaXTransfer;
            mDeltaYTransfer = deltaYTransfer;
            mHeatTransfer = heatTransfer;
            mInkTransfer = inkTransfer;
            mInkColor = inkColor;
        }

        public void transfer(FluidEntity entity) {
            entity.addMass(mMassTransfer);
            entity.addDeltaX(mDeltaXTransfer);
            entity.addDeltaY(mDeltaYTransfer);
            entity.addHeat(mHeatTransfer);
            entity.addInk(mInkTransfer, mInkColor);
        }
    }
}
