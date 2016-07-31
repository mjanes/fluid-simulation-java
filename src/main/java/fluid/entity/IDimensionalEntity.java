package fluid.entity;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

public interface IDimensionalEntity {

    void setX(double x);

    double getX();

    void setY(double y);

    double getY();

    void setZ(double z);

    double getZ();

    double getDistance(IDimensionalEntity other);

    static double getDistance(IDimensionalEntity a, IDimensionalEntity b) {
        return Math.sqrt(
                Math.pow((a.getX() - b.getX()), 2) +
                        Math.pow((a.getY() - b.getY()), 2) +
                        Math.pow((a.getZ() - b.getZ()), 2)
        );
    }

    Array2DRowRealMatrix getR4Matrix();
}
