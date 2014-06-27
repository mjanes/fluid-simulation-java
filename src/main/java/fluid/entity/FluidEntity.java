package fluid.entity;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.ArrayList;

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

    protected ArrayList<TransferRecord> mTransferRecords = new ArrayList<>();

    protected double mRadius; // Display Radius

    public FluidEntity(double x, double y, double z, double mass, double heat) {
        setX(x);
        setY(y);
        setZ(z);
        setMass(mass);
        setHeat(heat);
    }

    @Override
    public void setX(double x) {
        mX = x;
        r4Matrix.setEntry(0, 0, x);
    }

    @Override
    public double getX() {
        return mX;
    }

    @Override
    public void setY(double y) {
        mY = y;
        r4Matrix.setEntry(1, 0, y);
    }

    @Override
    public double getY() {
        return mY;
    }

    @Override
    public void setZ(double z) {
        mZ = z;
        r4Matrix.setEntry(2, 0, z);
    }

    @Override
    public double getZ() {
        return mZ;
    }

    @Override
    public double getDistance(IDimensionalEntity other) {
        return IDimensionalEntity.getDistance(this, other);
    }

    @Override
    public Array2DRowRealMatrix getR4Matrix() {
        return r4Matrix;
    }


    /** Velocity */

    public synchronized void setDeltaX(double deltaX) {
        mDeltaX = deltaX;
    }

    public synchronized void addDeltaX(double deltaDeltaX) {
        setDeltaX(mDeltaX + deltaDeltaX);
    }

    public double getDeltaX() {
        return mDeltaX;
    }

    public synchronized void setDeltaY(double deltaY) {
        mDeltaY = deltaY;
    }

    public synchronized void addDeltaY(double deltaDeltaY) {
        setDeltaY(mDeltaY + deltaDeltaY);
    }

    public double getDeltaY() {
        return mDeltaY;
    }

    public synchronized void setDeltaZ(double deltaZ) {
        mDeltaZ = deltaZ;
    }

    public double getDeltaZ() {
        return mDeltaZ;
    }


    /** Mass */

    public synchronized void setMass(double mass) {
        mMass = mass;
        setRadius();
    }

    public double getMass() {
        return mMass;
    }

    public synchronized void addMass(double deltaMass) {
        if (mMass + deltaMass < 0) {
            System.out.println("Mass being set to less than 0");
            return;
        }
        setMass(mMass + deltaMass);
    }


    /** Radius */


    public double getRadius() {
        return mRadius;
    }

    /**
     * Currently done in 2d
     */
    private void setRadius() {
        mRadius = Math.sqrt(mMass);
    }

    // TODO: Force = mass * velocity

    // TODO: Force


    /** Heat */

    public synchronized void setHeat(double heat) {
        mHeat = heat;
    }

    public double getHeat() {
        return mHeat;
    }

    public synchronized void addHeat(double deltaHeat) {
        mHeat += deltaHeat;
    }


    /** For display */

    public FluidEntity getNextLocationAsFluidEntity() {
        return new FluidEntity(mX + mDeltaX, mY + mDeltaY, mZ + mDeltaZ, mMass, mHeat);
    }



    /** Stepping from to the next increment of the simulation */

    public synchronized void incrementStep() {
        double totalToRatio = 0;
        for (TransferRecord transferRecord : mTransferRecords) {
            totalToRatio += transferRecord.getRatio();
        }

        for (TransferRecord transferRecord : mTransferRecords) {
            if (totalToRatio > 1) {
                transferTo(transferRecord.getTargetEntity(), transferRecord.getRatio() / totalToRatio);
            } else {
                transferTo(transferRecord.getTargetEntity(), transferRecord.getRatio());
            }
        }

        mTransferRecords.clear();
    }

    public synchronized void recordTransferTo(FluidEntity targetEntity, double ratio) {
        if (ratio < 0 || ratio > 1) {
            System.out.println("Error, ratio = " + ratio);
            return;
        }
        mTransferRecords.add(new TransferRecord(targetEntity, ratio));
    }


    private void transferTo(FluidEntity targetEntity, double ratio) {
        double massTransfer = mMass * ratio;
        double deltaXTransfer = mDeltaX * ratio;
        double deltaYTransfer = mDeltaY * ratio;
        double heatTransfer = mHeat * ratio;

        addMass(-massTransfer);
        addDeltaX(-deltaXTransfer);
        addDeltaY(-deltaYTransfer);
        addHeat(-heatTransfer);

        if (targetEntity != null) {
            targetEntity.addMass(massTransfer);
            targetEntity.addDeltaX(deltaXTransfer);
            targetEntity.addDeltaY(deltaYTransfer);
            targetEntity.addHeat(heatTransfer);
        }
    }

    private static class TransferRecord {
        private FluidEntity mTargetEntity;
        private double mRatio;

        public TransferRecord(FluidEntity targetEntity, double ratio) {
            mTargetEntity = targetEntity;
            mRatio = ratio;
        }

        public FluidEntity getTargetEntity() { return mTargetEntity; }

        public double getRatio() { return mRatio; }
    }
}
