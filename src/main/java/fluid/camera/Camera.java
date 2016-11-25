package fluid.camera;

import fluid.entity.IMobileDimensionalEntity;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import fluid.entity.IDimensionalEntity;

/**
 * A camera in three dimensional space.
 * <p>
 * And I understand that I'm probably using non-traditional terms for everything here
 * I suppose I should be using: https://en.wikipedia.org/wiki/Euler_angles
 * But at the moment I'm trying to teach myself things.
 * <p>
 * May change to official Euler angles later. Or pitch/yaw/roll.
 *
 * @author mjanes
 */
public class Camera implements IMobileDimensionalEntity {

    private double x;
    private double y;
    private double z;

    private double deltaX;
    private double deltaY;
    private double deltaZ;

    // Matrices for display math
    private final Array2DRowRealMatrix translationMatrix = new Array2DRowRealMatrix(new double[][]{
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}});

    // Orientation values
    // All of these are angles
    // Such that if the camera is located at x = 0, y = 0, z = 1,
    // With orientation xAngle = 0, yAngle = 0, and zAngle = 0
    // A point located at x = 0, y = 0, z = 0, would appear in the center of the field of view

    // If the camera is located at x = 0, y = 0, z = -1,
    // With orientation xAngle = 0, yAngle = 0, and zAngle = 0
    // A point located at x = 0, y = 180, z = 0, would appear in the center of the field of view

    // Imagine each axis, and each of these angles as a clockwise rotation around that axis
    private double xAngle;
    private double yAngle;
    private double zAngle;

    private final Array2DRowRealMatrix xRotationMatrix = new Array2DRowRealMatrix(new double[][]{
            {1, 0, 0, 0},
            {0, Math.cos(-Math.toRadians(xAngle)), Math.sin(Math.toRadians(xAngle)), 0},
            {0, Math.sin(-Math.toRadians(xAngle)), -Math.cos(Math.toRadians(xAngle)), 0},
            {0, 0, 0, 1}});

    private final Array2DRowRealMatrix yRotationMatrix = new Array2DRowRealMatrix(new double[][]{
            {-Math.cos(Math.toRadians(yAngle)), 0, -Math.sin(Math.toRadians(yAngle)), 0},
            {0, 1, 0, 0},
            {Math.sin(Math.toRadians(yAngle)), 0, -Math.cos(Math.toRadians(yAngle)), 0},
            {0, 0, 0, 1}});

    private final Array2DRowRealMatrix zRotationMatrix = new Array2DRowRealMatrix(new double[][]{
            {-Math.cos(Math.toRadians(zAngle)), Math.sin(Math.toRadians(zAngle)), 0, 0},
            {-Math.sin(Math.toRadians(yAngle)), -Math.cos(Math.toRadians(yAngle)), 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}});


    public Camera(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        xAngle = 0;
        yAngle = 0;
        zAngle = 0;
    }

    public Camera(double x, double y, double z, double xAngle, double yAngle, double zAngle) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xAngle = xAngle;
        this.yAngle = yAngle;
        this.zAngle = zAngle;
    }

    /**********************************************************************
     * Movement, positions, getters, setters
     **********************************************************************/

    @Override
    public void setX(double x) {
        this.x = x;
        translationMatrix.setEntry(0, 3, -x);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setDeltaX(double deltaX) {
        this.deltaX = deltaX;
    }

    @Override
    public void addDeltaX(double deltaDeltaX) {
        deltaX += deltaDeltaX;
    }

    @Override
    public double getDeltaX() {
        return deltaX;
    }

    @Override
    public void moveX(double deltaX) {
        setX(x + deltaX);
    }

    @Override
    public void setY(double y) {
        this.y = y;
        translationMatrix.setEntry(1, 3, -y);
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }

    @Override
    public void addDeltaY(double deltaDeltaY) {
        deltaY += deltaDeltaY;
    }

    @Override
    public double getDeltaY() {
        return deltaY;
    }

    @Override
    public void moveY(double deltaY) {
        setY(y + deltaY);
    }

    @Override
    public void setZ(double z) {
        this.z = z;
        translationMatrix.setEntry(2, 3, -z);
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public void setDeltaZ(double deltaZ) {
        this.deltaZ = deltaZ;
    }

    @Override
    public void addDeltaZ(double deltaDeltaZ) {
        deltaZ += deltaDeltaZ;
    }

    @Override
    public double getDeltaZ() {
        return deltaZ;
    }

    @Override
    public void moveZ(double deltaZ) {
        setZ(z + deltaZ);
    }

    @Override
    public void move() {
        moveX(deltaX);
        moveY(deltaY);
        moveZ(deltaZ);
    }


    /*******************************************************************************************************************
     * Distance calculations
     ******************************************************************************************************************/

    @Override
    public double getDistance(IDimensionalEntity other) {
        return IDimensionalEntity.getDistance(this, other);
    }

    @Override
    public Array2DRowRealMatrix getR4Matrix() {
        return null;
    }


    /*********************************************************************
     * Angles
     *********************************************************************/

    private double getXAngle() {
        return xAngle;
    }

    private void setXAngle(double xAngle) {
        this.xAngle = xAngle % 360;
        xRotationMatrix.setEntry(1, 1, -Math.cos(Math.toRadians(this.xAngle)));
        xRotationMatrix.setEntry(1, 2, Math.sin(Math.toRadians(this.xAngle)));
        xRotationMatrix.setEntry(2, 1, -Math.sin(Math.toRadians(this.xAngle)));
        xRotationMatrix.setEntry(2, 2, -Math.cos(Math.toRadians(this.xAngle)));
    }

    private void incrementXAngle(double increment) {
        setXAngle(xAngle + increment);
    }

    private double getYAngle() {
        return yAngle;
    }

    private void setYAngle(double yAngle) {
        this.yAngle = yAngle % 360;
        yRotationMatrix.setEntry(0, 0, -Math.cos(Math.toRadians(this.yAngle)));
        yRotationMatrix.setEntry(0, 2, -Math.sin(Math.toRadians(this.yAngle)));
        yRotationMatrix.setEntry(2, 0, Math.sin(Math.toRadians(this.yAngle)));
        yRotationMatrix.setEntry(2, 2, -Math.cos(Math.toRadians(this.yAngle)));
    }

    private void incrementYAngle(double increment) {
        setYAngle(yAngle + increment);
    }

    private double getZAngle() {
        return zAngle;
    }

    private void setZAngle(double zAngle) {
        this.zAngle = zAngle % 360;
        zRotationMatrix.setEntry(0, 0, -Math.cos(Math.toRadians(this.zAngle)));
        zRotationMatrix.setEntry(0, 1, Math.sin(Math.toRadians(this.zAngle)));
        zRotationMatrix.setEntry(1, 0, -Math.sin(Math.toRadians(this.zAngle)));
        zRotationMatrix.setEntry(1, 1, -Math.cos(Math.toRadians(this.zAngle)));
    }

    private void incrementZAngle(double increment) {
        setZAngle(zAngle + increment);
    }


    /********************************************************************************
     * Incrementing angles relative to the orientation of the screen
     ********************************************************************************/

    /**
     * Incrementing the relative x angle, meaning, moving the camera angle up and
     * down relative to the monitor. If the z angle is 90 degrees, then incrementing
     * the x angle 1 degree would instead increment the y angle 1 degree
     *
     * @param increment
     */
    public void incrementRelativeXAngle(double increment) {
        double angle = Math.toRadians(getZAngle());
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double xAngle = increment * cos;
        double yAngle = increment * sin;
        incrementXAngle(xAngle);
        incrementYAngle(yAngle);
    }

    public void incrementRelativeYAngle(double increment) {
        double angle = Math.toRadians((90 + getZAngle()) % 360);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double xAngle = increment * cos;
        double yAngle = increment * sin;
        incrementXAngle(xAngle);
        incrementYAngle(yAngle);
    }

    public void incrementRelativeZAngle(double increment) {
        incrementZAngle(increment);
    }


    /********************************************************************************
     * Movement functions relative to the direction the camera is facing in.
     *
     * After some initial math on this was broken, turning towards references:
     * http://www.mathsisfun.com/polar-cartesian-coordinates.html
     * https://en.wikipedia.org/wiki/Spherical_coordinate_system
     * https://en.wikipedia.org/wiki/List_of_common_coordinate_transformations#To_Cartesian_coordinates
     ********************************************************************************/

    public void addDeltaSelfX(final double delta) {
        double deltaX;
        double angle;

        // Handle rotation on Y axis
        angle = Math.toRadians(getYAngle());
        deltaX = delta * Math.cos(angle);
        final double deltaZ = delta * Math.sin(angle);

        // Handle rotation on Z axis
        angle = Math.toRadians(getZAngle());
        deltaX = deltaX * Math.cos(angle);
        final double deltaY = deltaX * Math.sin(angle);

        // Apply the motion
        addDeltaX(deltaX);
        addDeltaY(deltaY);
        addDeltaZ(deltaZ);
    }

    public void addDeltaSelfY(final double delta) {
        double deltaY;
        double angle;

        // Handle rotation on X axis
        angle = Math.toRadians(getXAngle());
        deltaY = delta * Math.cos(angle);
        final double deltaZ = delta * Math.sin(angle);

        // Handle rotation on Z axis
        angle = Math.toRadians(getZAngle());
        deltaY = deltaY * Math.cos(angle);
        final double deltaX = deltaY * Math.sin(angle);

        // Apply the new motion
        addDeltaX(deltaX);
        addDeltaY(deltaY);
        addDeltaZ(deltaZ);
    }


    public void addDeltaSelfZ(final double delta) {
        double deltaZ;
        double angle;

        // Handle rotation on Y axis
        angle = Math.toRadians(getYAngle());
        final double deltaX = delta * Math.sin(angle);
        deltaZ = delta * Math.cos(angle);

        // Handle rotation on X axis
        angle = Math.toRadians(getXAngle());
        deltaZ = deltaZ * Math.cos(angle);
        final double deltaY = deltaZ * Math.sin(angle);

        // Apply the new motion
        addDeltaX(deltaX);
        addDeltaY(deltaY);
        addDeltaZ(deltaZ);
    }


    /***************************************************************************************************
     * Translation and rotation functions to take an entity and create output for use by the camera
     ***************************************************************************************************/

    public Array2DRowRealMatrix translate(IDimensionalEntity dimensionalEntity) {
        return translationMatrix.multiply(dimensionalEntity.getR4Matrix());
    }

    public Array2DRowRealMatrix performXRotation(Array2DRowRealMatrix matrix) {
        return xRotationMatrix.multiply(matrix);
    }

    public Array2DRowRealMatrix performYRotation(Array2DRowRealMatrix matrix) {
        return yRotationMatrix.multiply(matrix);
    }

    public Array2DRowRealMatrix performZRotation(Array2DRowRealMatrix matrix) {
        return zRotationMatrix.multiply(matrix);
    }

}
