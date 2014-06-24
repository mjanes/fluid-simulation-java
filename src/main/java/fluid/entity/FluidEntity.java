package fluid.entity;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

/**
 * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
 *
 * Created by mjanes on 6/12/2014.
 */
public class FluidEntity implements IDimensionalEntity {

    public static final int SPACE = 10; // spacing between entities, currently writing this that they must be placed on a grid

    protected double mX;
    protected double mY;
    protected double mZ;

    protected Array2DRowRealMatrix r4Matrix = new Array2DRowRealMatrix(new double[] {0, 0, 0, 1});

    // All of these are going to require next values, and changes will be applied to the values in the next timestep.
    protected double mDeltaX;
    protected double mDeltaY;
    protected double mDeltaZ;
    protected double mMass;

    protected double mNextDeltaX;
    protected double mNextDeltaY;
    protected double mNextDeltaZ;
    protected double mNextMass;    // I suppose I'm presuming space is constant, so density scales linearly with mass.


    public FluidEntity(double x, double y, double z, double mass) {
        setX(x);
        setY(y);
        setZ(z);
        setMass(mass);
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

    public void setDeltaX(double deltaX) {
        mDeltaX = deltaX;
        mNextDeltaX = deltaX;
    }

    public void addDeltaX(double deltaDeltaX) {
        mDeltaX += deltaDeltaX;
    }

    public void addNextDeltaX(double deltaDeltaX) {
        mNextDeltaX += deltaDeltaX;
    }

    public double getDeltaX() {
        return mDeltaX;
    }

    public void setDeltaY(double deltaY) {
        mDeltaY = deltaY;
        mNextDeltaY = deltaY;
    }

    public void addDeltaY(double deltaDeltaY) {
        mDeltaY += deltaDeltaY;
    }

    public void addNextDeltaY(double deltaDeltaY) {
        mNextDeltaY += deltaDeltaY;
    }

    public double getDeltaY() {
        return mDeltaY;
    }

    public void setDeltaZ(double deltaZ) {
        mDeltaZ = deltaZ;
        mNextDeltaZ = deltaZ;
    }

    public void addNextDeltaZ(double deltaDeltaZ) {
        mNextDeltaZ += deltaDeltaZ;
    }

    public double getDeltaZ() {
        return mDeltaZ;
    }


    /** Mass */

    public void setMass(double mass) {
        mMass = mass;
        mNextMass = mass;
    }

    public double getMass() {
        return mMass;
    }

    public void addNextMass(double deltaMass) {
        if (mNextMass + deltaMass < 0) {
            mNextMass = 0;
        }

        mNextMass += deltaMass;
    }

    /** For display */

    public FluidEntity getNextLocationAsFluidEntity() {
        return new FluidEntity(mX + mDeltaX, mY + mDeltaY, mZ + mDeltaZ, mNextMass);
    }

    public FluidEntity getPreviousLocationAsFluidEntity() {
        return new FluidEntity(mX - mDeltaX, mY - mDeltaY, mZ - mDeltaZ, mNextMass);
    }



    /** Stepping from to the next increment of the simulation */

    public void incrementStep() {
        mMass = mNextMass;
        mDeltaX = mNextDeltaX;
        mDeltaY = mNextDeltaY;
        mDeltaZ = mNextDeltaZ;
    }

    public void transferTo(FluidEntity targetEntity, double ratio) {
        if (ratio < 0 || ratio > 1) {
            System.out.println("Error, ratio = " + ratio);
            return;
        }

        double massTransfer = mMass * ratio;
        if (mNextMass - massTransfer < -.00001) { // rounding with doubles
            System.out.println("mNextMass would have gone to less than 0");
            return;
        }
        addNextMass(-massTransfer);
        if (targetEntity != null) {
            targetEntity.addNextMass(massTransfer);
        }

        double deltaXTransfer = mDeltaX * ratio;
        addNextDeltaX(-deltaXTransfer);
        if (targetEntity != null) {
            targetEntity.addNextDeltaX(deltaXTransfer);
        }

        double deltaYTransfer = mDeltaY * ratio;
        addNextDeltaY(-deltaYTransfer);
        if (targetEntity != null) {
            targetEntity.addNextDeltaY(deltaYTransfer);
        }

        //addNextDeltaZ(-getDeltaZ() * ratio);
        //otherEntity.addNextDeltaZ(getDeltaZ() * ratio);
    }
}
