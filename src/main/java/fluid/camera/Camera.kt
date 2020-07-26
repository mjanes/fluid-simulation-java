package fluid.camera

import fluid.entity.DimensionalEntity
import fluid.entity.DimensionalEntity.Companion.getDistance
import fluid.entity.MobileDimensionalEntity
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import kotlin.math.cos
import kotlin.math.sin

/**
 * A camera in three dimensional space.
 *
 *
 * And I understand that I'm probably using non-traditional terms for everything here
 * I suppose I should be using: https://en.wikipedia.org/wiki/Euler_angles
 * But at the moment I'm trying to teach myself things.
 *
 *
 * May change to official Euler angles later. Or pitch/yaw/roll.
 *
 * @author mjanes
 */
class Camera : MobileDimensionalEntity {
    override var deltaX = 0.0
    override var deltaY = 0.0
    override var deltaZ = 0.0

    // Matrices for display math
    private val translationMatrix = Array2DRowRealMatrix(arrayOf(doubleArrayOf(1.0, 0.0, 0.0, 0.0), doubleArrayOf(0.0, 1.0, 0.0, 0.0), doubleArrayOf(0.0, 0.0, 1.0, 0.0), doubleArrayOf(0.0, 0.0, 0.0, 1.0)))

    // Orientation values
    // All of these are angles
    // Such that if the camera is located at x = 0, y = 0, z = 1,
    // With orientation xAngle = 0, yAngle = 0, and zAngle = 0
    // A point located at x = 0, y = 0, z = 0, would appear in the center of the field of view
    // If the camera is located at x = 0, y = 0, z = -1,
    // With orientation xAngle = 0, yAngle = 0, and zAngle = 0
    // A point located at x = 0, y = 180, z = 0, would appear in the center of the field of view
    // Imagine each axis, and each of these angles as a clockwise rotation around that axis
    private var xAngle: Double = 0.0
    private var yAngle: Double = 0.0
    private var zAngle: Double = 0.0
    private val xRotationMatrix = Array2DRowRealMatrix(arrayOf(doubleArrayOf(1.0, 0.0, 0.0, 0.0), doubleArrayOf(0.0, cos(-Math.toRadians(xAngle)), sin(Math.toRadians(xAngle)), 0.0), doubleArrayOf(0.0, sin(-Math.toRadians(xAngle)), -cos(Math.toRadians(xAngle)), 0.0), doubleArrayOf(0.0, 0.0, 0.0, 1.0)))
    private val yRotationMatrix = Array2DRowRealMatrix(arrayOf(doubleArrayOf(-cos(Math.toRadians(yAngle)), 0.0, -sin(Math.toRadians(yAngle)), 0.0), doubleArrayOf(0.0, 1.0, 0.0, 0.0), doubleArrayOf(sin(Math.toRadians(yAngle)), 0.0, -cos(Math.toRadians(yAngle)), 0.0), doubleArrayOf(0.0, 0.0, 0.0, 1.0)))
    private val zRotationMatrix = Array2DRowRealMatrix(arrayOf(doubleArrayOf(-cos(Math.toRadians(zAngle)), sin(Math.toRadians(zAngle)), 0.0, 0.0), doubleArrayOf(-sin(Math.toRadians(yAngle)), -cos(Math.toRadians(yAngle)), 0.0, 0.0), doubleArrayOf(0.0, 0.0, 1.0, 0.0), doubleArrayOf(0.0, 0.0, 0.0, 1.0)))

    constructor(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(x: Double, y: Double, z: Double, xAngle: Double, yAngle: Double, zAngle: Double) {
        this.x = x
        this.y = y
        this.z = z
        this.xAngle = xAngle
        this.yAngle = yAngle
        this.zAngle = zAngle
    }

    /**********************************************************************
     * Movement, positions, getters, setters
     */

    override fun addDeltaX(deltaDeltaX: Double) {
        deltaX += deltaDeltaX
    }

    override fun moveX(deltaX: Double) {
        x += deltaX
    }

    override fun addDeltaY(deltaDeltaY: Double) {
        deltaY += deltaDeltaY
    }

    override fun moveY(deltaY: Double) {
        y += deltaY
    }

    override fun addDeltaZ(deltaDeltaZ: Double) {
        deltaZ += deltaDeltaZ
    }

    override fun moveZ(deltaZ: Double) {
        z += deltaZ
    }

    override fun move() {
        moveX(deltaX)
        moveY(deltaY)
        moveZ(deltaZ)
    }

    override var x: Double
        set(value) {
            field = value
            translationMatrix.setEntry(0, 3, -value)
        }

    override var y: Double
        set(value) {
            field = value
            translationMatrix.setEntry(1, 3, -value)
        }

    override var z: Double
        set(value) {
            field = value
            translationMatrix.setEntry(2, 3, -value)
        }

    /*******************************************************************************************************************
     * Distance calculations
     */
    override fun getDistance(other: DimensionalEntity?): Double {
        return getDistance(this, other!!)
    }

    override val r4Matrix: Array2DRowRealMatrix?
        get() = null

    /*********************************************************************
     * Angles
     */
    private fun getXAngle(): Double {
        return xAngle
    }

    private fun setXAngle(xAngle: Double) {
        this.xAngle = xAngle % 360
        xRotationMatrix.setEntry(1, 1, -cos(Math.toRadians(this.xAngle)))
        xRotationMatrix.setEntry(1, 2, sin(Math.toRadians(this.xAngle)))
        xRotationMatrix.setEntry(2, 1, -sin(Math.toRadians(this.xAngle)))
        xRotationMatrix.setEntry(2, 2, -cos(Math.toRadians(this.xAngle)))
    }

    private fun incrementXAngle(increment: Double) {
        setXAngle(xAngle + increment)
    }

    private fun getYAngle(): Double {
        return yAngle
    }

    private fun setYAngle(yAngle: Double) {
        this.yAngle = yAngle % 360
        yRotationMatrix.setEntry(0, 0, -cos(Math.toRadians(this.yAngle)))
        yRotationMatrix.setEntry(0, 2, -sin(Math.toRadians(this.yAngle)))
        yRotationMatrix.setEntry(2, 0, sin(Math.toRadians(this.yAngle)))
        yRotationMatrix.setEntry(2, 2, -cos(Math.toRadians(this.yAngle)))
    }

    private fun incrementYAngle(increment: Double) {
        setYAngle(yAngle + increment)
    }

    private fun getZAngle(): Double {
        return zAngle
    }

    private fun setZAngle(zAngle: Double) {
        this.zAngle = zAngle % 360
        zRotationMatrix.setEntry(0, 0, -cos(Math.toRadians(this.zAngle)))
        zRotationMatrix.setEntry(0, 1, sin(Math.toRadians(this.zAngle)))
        zRotationMatrix.setEntry(1, 0, -sin(Math.toRadians(this.zAngle)))
        zRotationMatrix.setEntry(1, 1, -cos(Math.toRadians(this.zAngle)))
    }

    private fun incrementZAngle(increment: Double) {
        setZAngle(zAngle + increment)
    }
    /********************************************************************************
     * Incrementing angles relative to the orientation of the screen
     */
    /**
     * Incrementing the relative x angle, meaning, moving the camera angle up and
     * down relative to the monitor. If the z angle is 90 degrees, then incrementing
     * the x angle 1 degree would instead increment the y angle 1 degree
     *
     * @param increment
     */
    fun incrementRelativeXAngle(increment: Double) {
        val angle = Math.toRadians(getZAngle())
        val cos = cos(angle)
        val sin = sin(angle)
        val xAngle = increment * cos
        val yAngle = increment * sin
        incrementXAngle(xAngle)
        incrementYAngle(yAngle)
    }

    fun incrementRelativeYAngle(increment: Double) {
        val angle = Math.toRadians((90 + getZAngle()) % 360)
        val cos = cos(angle)
        val sin = sin(angle)
        val xAngle = increment * cos
        val yAngle = increment * sin
        incrementXAngle(xAngle)
        incrementYAngle(yAngle)
    }

    fun incrementRelativeZAngle(increment: Double) {
        incrementZAngle(increment)
    }

    /********************************************************************************
     * Movement functions relative to the direction the camera is facing in.
     *
     * After some initial math on this was broken, turning towards references:
     * http://www.mathsisfun.com/polar-cartesian-coordinates.html
     * https://en.wikipedia.org/wiki/Spherical_coordinate_system
     * https://en.wikipedia.org/wiki/List_of_common_coordinate_transformations#To_Cartesian_coordinates
     */
    fun addDeltaSelfX(delta: Double) {
        var deltaX: Double

        // Handle rotation on Y axis
        var angle: Double = Math.toRadians(getYAngle())
        deltaX = delta * cos(angle)
        val deltaZ = delta * sin(angle)

        // Handle rotation on Z axis
        angle = Math.toRadians(getZAngle())
        deltaX *= cos(angle)
        val deltaY = deltaX * sin(angle)

        // Apply the motion
        addDeltaX(deltaX)
        addDeltaY(deltaY)
        addDeltaZ(deltaZ)
    }

    fun addDeltaSelfY(delta: Double) {
        var deltaY: Double

        // Handle rotation on X axis
        var angle: Double = Math.toRadians(getXAngle())
        deltaY = delta * cos(angle)
        val deltaZ = delta * sin(angle)

        // Handle rotation on Z axis
        angle = Math.toRadians(getZAngle())
        deltaY *= cos(angle)
        val deltaX = deltaY * sin(angle)

        // Apply the new motion
        addDeltaX(deltaX)
        addDeltaY(deltaY)
        addDeltaZ(deltaZ)
    }

    fun addDeltaSelfZ(delta: Double) {
        var deltaZ: Double

        // Handle rotation on Y axis
        var angle: Double = Math.toRadians(getYAngle())
        val deltaX = delta * sin(angle)
        deltaZ = delta * cos(angle)

        // Handle rotation on X axis
        angle = Math.toRadians(getXAngle())
        deltaZ *= cos(angle)
        val deltaY = deltaZ * sin(angle)

        // Apply the new motion
        addDeltaX(deltaX)
        addDeltaY(deltaY)
        addDeltaZ(deltaZ)
    }

    /***************************************************************************************************
     * Translation and rotation functions to take an entity and create output for use by the camera
     */
    fun translate(matrix: Array2DRowRealMatrix?): Array2DRowRealMatrix {
        return translationMatrix.multiply(matrix)
    }

    fun performXRotation(matrix: Array2DRowRealMatrix?): Array2DRowRealMatrix {
        return xRotationMatrix.multiply(matrix)
    }

    fun performYRotation(matrix: Array2DRowRealMatrix?): Array2DRowRealMatrix {
        return yRotationMatrix.multiply(matrix)
    }

    fun performZRotation(matrix: Array2DRowRealMatrix?): Array2DRowRealMatrix {
        return zRotationMatrix.multiply(matrix)
    }
}