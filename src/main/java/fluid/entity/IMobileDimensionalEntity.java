package fluid.entity;

/**
 * Created by mjanes on 6/12/2014.
 */
public interface IMobileDimensionalEntity extends IDimensionalEntity {

    public void setDeltaX(double deltaX);
    public void addDeltaX(double deltaDeltaX);
    public double getDeltaX();
    public void moveX(double deltaX);

    public void setDeltaY(double deltaY);
    public void addDeltaY(double deltaDeltaY);
    public double getDeltaY();
    public void moveY(double deltaY);

    public void setDeltaZ(double deltaZ);
    public void addDeltaZ(double deltaDeltaZ);
    public double getDeltaZ();
    public void moveZ(double deltaZ);

    public void move();


}
