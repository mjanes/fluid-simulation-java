package fluid.display;

import fluid.camera.Camera;
import fluid.entity.FluidEntity;
import fluid.entity.IFluidEntity;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.awt.geom.Point2D;
import java.util.stream.IntStream;

public class FluidEntityCanvas extends Canvas {

    private final Camera mCamera;

    private static final int EYE_DISTANCE = 5000;

    public enum DrawType {
        INK, TEMPERATURE, VELOCITY, MASS
    }

    public FluidEntityCanvas(int width, int height, Camera camera) {
        super(width, height);
        mCamera = camera;
    }

    public void drawEntities(final FluidEntity[][] entities, final DrawType drawType) {
        final double canvasWidth = getWidth();
        final double canvasHeight = getHeight();
        final GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> drawEntity(gc, mCamera, entities[x][y], canvasWidth, canvasHeight, drawType)));
    }

    private void drawEntity(final GraphicsContext gc, Camera camera, final FluidEntity entity, final double canvasWidth, final double canvasHeight, final DrawType drawType) {

        double radius = 0;
        Color color = Color.TRANSPARENT;

        // NOTE: When this stops being 2D will have to calculate radius
        if (drawType.equals(DrawType.INK)) {
            radius = Math.sqrt(entity.getMass()); // NOTE: Probably want to change this to cube root when we go 3d
            color = entity.getColor();
            // TODO: Some UI controlled value to do this instead
            color = color.saturate().saturate().darker().darker().saturate();
        } else if (drawType.equals(DrawType.TEMPERATURE)) {
            radius = Math.sqrt(entity.getMass());
            double temperature = entity.getTemperature();
            if (temperature < IFluidEntity.DEFAULT_TEMPERATURE) {
                double normalizedCold = (IFluidEntity.DEFAULT_TEMPERATURE - temperature) / IFluidEntity.DEFAULT_TEMPERATURE;
                color = new Color(0, 0, normalizedCold, 1);
            } else {
                double normalizedHot = (temperature - IFluidEntity.DEFAULT_TEMPERATURE) / IFluidEntity.DEFAULT_TEMPERATURE;
                color = new Color(Math.min(1, normalizedHot), 0, 0, 1);
            }
        } else if (drawType.equals(DrawType.VELOCITY)) {
            radius = 1;
            color = Color.BLACK;
        } else if (drawType.equals(DrawType.MASS)) {
            radius = Math.sqrt(entity.getMass()); // NOTE: Probably want to change this to cube root when we go 3
            color = Color.BLACK;
        }

        if (radius < 1 || color.equals(Color.TRANSPARENT)) {
            return;
        }

        Point2D.Double point = getCanvasLocation(camera, canvasWidth, canvasHeight, entity);
        if (point == null) return;

        double xP = point.getX();
        double yP = point.getY();


        // Subtract half the radius from the projection point, because g.fillOval does not surround the center point

        gc.setFill(color);
        gc.fillOval((int) xP - radius / 2, (int) yP - radius / 2, radius, radius);

        if (drawType.equals(DrawType.VELOCITY)) {
            FluidEntity vector = entity.getNextLocationAsFluidEntity(20); // TODO: Make this factor parameter a user controlled variable
            Point2D.Double vectorPoint = getCanvasLocation(camera, canvasWidth, canvasHeight, vector);
            gc.setStroke(Color.RED);
            gc.strokeLine(xP, yP, vectorPoint.getX(), vectorPoint.getY());
        }
    }

    /**
     * Looking into doing this all with matrix math for speed improvement.
     * <p>
     * http://www.matrix44.net/cms/notes/opengl-3d-graphics/basic-3d-math-matrices
     *
     * @param camera
     * @param canvasWidth
     * @param canvasHeight
     * @return
     */
    private static Point2D.Double getCanvasLocation(Camera camera, final double canvasWidth, final double canvasHeight, FluidEntity entity) {
        if (entity == null) return null;

        /* Starting offset from camera
         * This is to set the camera at the center of things
         * 0, 0 is now the location of the camera.
         *
         * Bear in mind that we are still using the coordinate system of the display,
         * so something at 1, 1 would not be in the upper right quadrant, but would
         * be in the lower right quadrant. 1, -1 would be in the upper right.
         * May want to undo that later...
         */
        Array2DRowRealMatrix matrix = camera.translate(entity);


        // Perform the rotations on the various axes
        // Note: Apparently order matters here, which I am somewhat confused by.

        // X axis rotation
        matrix = camera.performXRotation(matrix);

        // Y axis rotation
        matrix = camera.performYRotation(matrix);

        // Z axis rotation
        matrix = camera.performZRotation(matrix);

        // Rotation is complete
        double xP = matrix.getEntry(0, 0);
        double yP = matrix.getEntry(1, 0);
        double zP = matrix.getEntry(2, 0);

        // Objects with a negative zP will not be displayed.
        // Objects with a 0 zP are assumed to be on the camera, covering the screen essentially
        if (zP <= 0) return null;

        // Project onto viewing plane, ie the further away it is, the more it will appear towards the center
        double distanceRatio = EYE_DISTANCE / zP;
        xP = xP * distanceRatio;
        yP = yP * distanceRatio;

        // Adding width / 2 and height / 2 to the mX and mY projections, so that 0,0 appears in the middle of the screen
        // Resizing the radius, so that if an object's zP is equal to EYE_DISTANCE, it is shown at its default
        // radius, otherwise smaller if further away, larger if closer.
        xP += (canvasWidth / 2);
        yP += (canvasHeight / 2);

        return new Point2D.Double(xP, yP);
    }

}
