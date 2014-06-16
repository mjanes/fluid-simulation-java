package fluid.entity;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mjanes on 6/12/2014.
 */
public class FluidEntity implements IConnectedEntity, IDimensionalEntity {

    protected double mX;
    protected double mY;
    protected double mZ;

    private List<IConnectedEntity> mConnections = new ArrayList<>();

    protected Array2DRowRealMatrix r4Matrix = new Array2DRowRealMatrix(new double[] {0, 0, 0, 1});

    public FluidEntity(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
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

    @Override
    public void addConnection(IConnectedEntity entity) {
        mConnections.add(entity);
    }

    @Override
    public void removeConnection(IConnectedEntity entity) {
        mConnections.remove(entity);
    }

    @Override
    public List<IConnectedEntity> getConnections() {
        return mConnections;
    }
}
