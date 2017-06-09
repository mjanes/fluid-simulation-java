package fluid.entity;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

public interface DimensionalEntity {

    void setX(double x);

    double getX();

    void setY(double y);

    double getY();

    void setZ(double z);

    double getZ();

    double getDistance(DimensionalEntity other);

    static double getDistance(DimensionalEntity a, DimensionalEntity b) {
        return Math.sqrt(
                Math.pow((a.getX() - b.getX()), 2) +
                        Math.pow((a.getY() - b.getY()), 2) +
                        Math.pow((a.getZ() - b.getZ()), 2)
        );
    }

    Array2DRowRealMatrix getR4Matrix();
}
