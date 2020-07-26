package fluid.display

import fluid.camera.Camera
import fluid.entity.FluidEntity
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import java.awt.geom.Point2D
import java.util.stream.IntStream

class FluidEntityCanvas(width: Int, height: Int, private val camera: Camera) : Canvas(width.toDouble(), height.toDouble()) {
    private val vector = Array2DRowRealMatrix(doubleArrayOf(0.0, 0.0, 0.0, 1.0))

    enum class DrawType {
        INK, TEMPERATURE, VELOCITY, MASS
    }

    fun drawEntities(entities: Array<Array<FluidEntity>>, drawType: DrawType) {
        val canvasWidth = width
        val canvasHeight = height
        val gc = graphicsContext2D
        gc.clearRect(0.0, 0.0, canvasWidth, canvasHeight)
        IntStream.range(0, entities.size).forEach { x: Int -> IntStream.range(0, entities[x].size).forEach { y: Int -> drawEntity(gc, camera, entities[x][y], canvasWidth, canvasHeight, drawType) } }
    }

    private fun drawEntity(gc: GraphicsContext, camera: Camera, entity: FluidEntity, canvasWidth: Double, canvasHeight: Double, drawType: DrawType) {
        var radius = 0.0
        var color = Color.TRANSPARENT

        // NOTE: When this stops being 2D will have to calculate radius
        if (drawType == DrawType.INK) {
            radius = Math.sqrt(entity.mass) // NOTE: Probably want to change this to cube root when we go 3d
            color = entity.color
            // TODO: Some UI controlled value to do this instead
            color = color.saturate().saturate().darker().darker().saturate()
        } else if (drawType == DrawType.TEMPERATURE) {
            radius = Math.sqrt(entity.mass)
            val temperature = entity.temperature
            color = if (temperature < FluidEntity.DEFAULT_TEMPERATURE) {
                val normalizedCold = (FluidEntity.DEFAULT_TEMPERATURE - temperature) / FluidEntity.DEFAULT_TEMPERATURE
                Color(0.toDouble(), 0.toDouble(), normalizedCold, 1.toDouble())
            } else {
                val normalizedHot = (temperature - FluidEntity.DEFAULT_TEMPERATURE) / FluidEntity.DEFAULT_TEMPERATURE
                Color(Math.min(1.0, normalizedHot), 0.toDouble(), 0.toDouble(), 1.toDouble())
            }
        } else if (drawType == DrawType.VELOCITY) {
            radius = 1.0
            color = Color.BLACK
        } else if (drawType == DrawType.MASS) {
            radius = Math.sqrt(entity.mass) // NOTE: Probably want to change this to cube root when we go 3
            color = Color.BLACK
        }
        if (radius < 1 || color == Color.TRANSPARENT) {
            return
        }
        val point = getCanvasLocation(camera, canvasWidth, canvasHeight, entity.r4Matrix) ?: return
        val xP = point.getX()
        val yP = point.getY()


        // Subtract half the radius from the projection point, because g.fillOval does not surround the center point
        gc.fill = color
        gc.fillOval(xP.toInt() - radius / 2, yP.toInt() - radius / 2, radius, radius)

        // Drawing where the entity is moving towards
        if (drawType == DrawType.VELOCITY) {
            entity.getNextLocationAsFluidEntity(vector, 20.0) // TODO: Make this factor parameter a user controlled variable
            val vectorPoint = getCanvasLocation(camera, canvasWidth, canvasHeight, vector)
            gc.stroke = Color.RED
            gc.strokeLine(xP, yP, vectorPoint!!.getX(), vectorPoint.getY())
        }
    }

    companion object {
        private const val EYE_DISTANCE = 5000

        /**
         * Looking into doing this all with matrix math for speed improvement.
         *
         *
         * http://www.matrix44.net/cms/notes/opengl-3d-graphics/basic-3d-math-matrices
         *
         * @param camera
         * @param canvasWidth
         * @param canvasHeight
         * @return
         */
        private fun getCanvasLocation(camera: Camera,
                                      canvasWidth: Double,
                                      canvasHeight: Double,
                                      entityMatrix: Array2DRowRealMatrix?): Point2D.Double? {
            /* Starting offset from camera
             * This is to set the camera at the center of things
             * 0, 0 is now the location of the camera.
             *
             * Bear in mind that we are still using the coordinate system of the display,
             * so something at 1, 1 would not be in the upper right quadrant, but would
             * be in the lower right quadrant. 1, -1 would be in the upper right.
             * May want to undo that later...
             */
            var matrix = camera.translate(entityMatrix)


            // Perform the rotations on the various axes
            // Note: Apparently order matters here, which I am somewhat confused by.

            // X axis rotation
            matrix = camera.performXRotation(matrix)

            // Y axis rotation
            matrix = camera.performYRotation(matrix)

            // Z axis rotation
            matrix = camera.performZRotation(matrix)

            // Rotation is complete
            var xP = matrix.getEntry(0, 0)
            var yP = matrix.getEntry(1, 0)
            val zP = matrix.getEntry(2, 0)

            // Objects with a negative zP will not be displayed.
            // Objects with a 0 zP are assumed to be on the camera, covering the screen essentially
            if (zP <= 0) return null

            // Project onto viewing plane, ie the further away it is, the more it will appear towards the center
            val distanceRatio = EYE_DISTANCE / zP
            xP *= distanceRatio
            yP *= distanceRatio

            // Adding width / 2 and height / 2 to the mX and mY projections, so that 0,0 appears in the middle of the screen
            // Resizing the radius, so that if an object's zP is equal to EYE_DISTANCE, it is shown at its default
            // radius, otherwise smaller if further away, larger if closer.
            xP += canvasWidth / 2
            yP += canvasHeight / 2
            return Point2D.Double(xP, yP)
        }
    }

}