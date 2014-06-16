package fluid.entity;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

public interface IDimensionalEntity {

    public void setX(double x);
    public double getX();

    public void setY(double y);
    public double getY();

	public void setZ(double z);
	public double getZ();

    public double getDistance(IDimensionalEntity other);

    public static double getDistance(IDimensionalEntity a, IDimensionalEntity b) {
        return Math.sqrt(
                Math.pow((a.getX() - b.getX()), 2) +
                        Math.pow((a.getY() - b.getY()), 2) +
                        Math.pow((a.getZ() - b.getZ()), 2)
        );
    }

    public Array2DRowRealMatrix getR4Matrix();
}
