package fluid.physics;

import fluid.entity.FluidEntity;
import javafx.scene.paint.Color;

import java.util.stream.IntStream;

/**
 * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
 * http://www.dgp.toronto.edu/people/stam/reality/Research/pdf/GDC03.pdf
 *
 * Created by mjanes on 6/16/2014.
 */
public class FluidPhysics {

    private static final double CELL_AREA = Math.pow(FluidEntity.SPACE, 2);
    private static final double DENSITY_TO_VELOCITY_SCALE = .01;

    private static int timestep = 0;


    public static void incrementFluid(FluidEntity[][] entities) {
        if (entities == null) return;

        applyInput(entities);

        applyHeat(entities);
        applyPressure(entities);
        forwardAdvection(entities);
        reverseAdvection(entities);

        timestep++;
    }


    private static void applyInput(FluidEntity[][] entities) {

        double factor = Math.abs(Math.sin(Math.toRadians(timestep * 3)));

        entities[0][0].setMass(factor * 600 + 50);
        entities[0][0].setInk(entities[0][0].getMass() * factor, Color.ORANGE);
        entities[0][0].addDeltaX(factor * 28);
        entities[0][0].addDeltaY(factor * 17);
        entities[0][0].setHeat(factor);

        entities[1][0].setMass(factor * 500 + 20);
        entities[1][0].setInk(entities[1][0].getMass() * factor, Color.RED);
        entities[1][0].addDeltaX(factor * 22);
        entities[1][0].addDeltaY(factor * 16);
        entities[1][0].setHeat(1);

        factor = Math.abs(Math.sin(Math.toRadians(timestep * 2)));
        entities[0][1].setMass(factor * 500 + 100);
        entities[0][1].setInk(factor * 500 + 100, Color.ORANGE);
        entities[0][1].addDeltaX(22);
        entities[0][1].addDeltaY(16);
        entities[0][1].setHeat(1);

        entities[1][1].setMass(factor * 200);
        entities[1][1].setInk(factor * 200, Color.YELLOW);
        entities[1][1].addDeltaX(23);
        entities[1][1].addDeltaY(14);
        entities[1][1].setHeat(factor);


        factor = Math.abs(Math.cos(Math.toRadians(timestep * 2)));
        entities[120][80].setMass(600 * factor);
        entities[120][80].setDeltaX(-40 * factor);
        entities[120][80].setDeltaY(-4);
        entities[120][80].setInk(600, Color.BLUE);


        factor = Math.abs(Math.sin(Math.toRadians(timestep)));
        entities[100][10].setMass(400 * (1 - factor));
        entities[100][10].setDeltaX(-20);
        entities[100][10].setDeltaY(4);
        entities[100][10].setHeat(1 - factor);
        entities[100][10].setInk(600, Color.GREEN);
    }

    private static void applyHeat(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).parallel().forEach(x -> {
            IntStream.range(0, entities[0].length).forEach(y -> {
                entities[x][y].addDeltaY(entities[x][y].getHeat() / 4);
            });
        });
    }

    private static void applyPressure(FluidEntity[][] entities) {

        int d1 = entities.length;
        int d2 = entities[0].length;

        // pressure
        IntStream.range(0, d1).parallel().forEach(x -> {
            IntStream.range(0, d2).forEach(y -> {
                FluidEntity entity = entities[x][y];
                FluidEntity otherEntity;

                // Left Entity
                if (x > 0) {
                    otherEntity = entities[x - 1][y];
                    if (entity.getMass() > otherEntity.getMass()) {
                        otherEntity.addDeltaX(-(entity.getMass() - otherEntity.getMass()) * DENSITY_TO_VELOCITY_SCALE);
                    }
                }

                // Right entity
                if (x + 1 < d1) {
                    otherEntity = entities[x + 1][y];
                    if (entity.getMass() > otherEntity.getMass()) {
                        otherEntity.addDeltaX((entity.getMass() - otherEntity.getMass()) * DENSITY_TO_VELOCITY_SCALE);
                    }
                }

                // Upper entity
                if (y > 0) {
                    otherEntity = entities[x][y - 1];
                    if (entity.getMass() > otherEntity.getMass()) {
                        otherEntity.addDeltaY(-(entity.getMass() - otherEntity.getMass()) * DENSITY_TO_VELOCITY_SCALE);
                    }
                }

                // Lower entity
                if (y + 1 < d2) {
                    otherEntity = entities[x][y + 1];
                    if (entity.getMass() > otherEntity.getMass()) {
                        otherEntity.addDeltaY((entity.getMass() - otherEntity.getMass()) * DENSITY_TO_VELOCITY_SCALE);
                    }
                }
            });
        });

        applyStep(entities);
    }

    /**
     * Advection moves the quantities from point to its connections/neighbors. Quantities include velocity/mass/heat/etc.
     * The amount moved from one point to another is based on the given point's velocity.
     */
    private static void forwardAdvection(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).parallel().forEach(x -> {
            IntStream.range(0, entities[0].length).forEach(y -> {
                forwardAdvectionCellTransfer(entities, x, y);
            });
        });

        applyStep(entities);
    }

    private static void reverseAdvection(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).parallel().forEach(x -> {
            IntStream.range(0, entities[0].length).forEach(y -> {
                reverseAdvectionCellTransfer(entities, x, y);
            });
        });

        applyStep(entities);
    }

    private static void applyStep(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).forEach(x -> {
            IntStream.range(0, entities[0].length).forEach(y -> {
                entities[x][y].incrementStep();
            });
        });
    }

    /**
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     */
    private static void forwardAdvectionCellTransfer(FluidEntity[][] entities, int xIndex, int yIndex) {

        FluidEntity entity = entities[xIndex][yIndex];
        double deltaX = entity.getDeltaX();
        double deltaY = entity.getDeltaY();

        if (deltaX == 0 && deltaY == 0) return; // unsure this is needed

        int xIndexOffset = (int) deltaX / FluidEntity.SPACE;
        int yIndexOffset = (int) deltaY / FluidEntity.SPACE;

        boolean dxPositive = deltaX > 0;
        boolean dyPositive = deltaY > 0;

        int t1x = getLesserTargetIndex(xIndex, xIndexOffset, dxPositive);
        int t1y = getLesserTargetIndex(yIndex, yIndexOffset, dyPositive);

        int t2x = t1x + 1;
        int t2y = t1y + 1;

        double xPosInCell = dxPositive ? entity.getDeltaX() % FluidEntity.SPACE : FluidEntity.SPACE + entity.getDeltaX() % FluidEntity.SPACE;
        double yPosInCell = dyPositive ? entity.getDeltaY() % FluidEntity.SPACE : FluidEntity.SPACE + entity.getDeltaY() % FluidEntity.SPACE;

        // Area of top right
        double bottomLeftAreaInversion = (FluidEntity.SPACE - xPosInCell) * (FluidEntity.SPACE - yPosInCell);

        // area of top left
        double bottomRightAreaInversion = xPosInCell * (FluidEntity.SPACE - yPosInCell);

        // area of bottom right
        double topLeftAreaInversion = (FluidEntity.SPACE - xPosInCell) * yPosInCell;

        // area of bottom left
        double topRightAreaInversion = xPosInCell * yPosInCell;

        double bottomLeftRatio = bottomLeftAreaInversion / CELL_AREA;
        double bottomRightRatio = bottomRightAreaInversion / CELL_AREA;
        double topLeftRatio = topLeftAreaInversion / CELL_AREA;
        double topRightRatio = topRightAreaInversion / CELL_AREA;

        transferTo(entity, entities, t1x, t1y, bottomLeftRatio);
        transferTo(entity, entities, t2x, t1y, bottomRightRatio);
        transferTo(entity, entities, t1x, t2y, topLeftRatio);
        transferTo(entity, entities, t2x, t2y, topRightRatio);
    }

    /**
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     */
    private static void reverseAdvectionCellTransfer(FluidEntity[][] entities, int xIndex, int yIndex) {

        FluidEntity entity = entities[xIndex][yIndex];
        double negativeDeltaX = -entity.getDeltaX();
        double negativeDeltaY = -entity.getDeltaY();

        if (negativeDeltaX == 0 && negativeDeltaY == 0) return;

        int xIndexOffset = (int) negativeDeltaX / FluidEntity.SPACE;
        int yIndexOffset = (int) negativeDeltaY / FluidEntity.SPACE;

        boolean negativeDxPositive = negativeDeltaX > 0;
        boolean negativeDyPositive = negativeDeltaY > 0;

        int t1x = getLesserTargetIndex(xIndex, xIndexOffset, negativeDxPositive);
        int t1y = getLesserTargetIndex(yIndex, yIndexOffset, negativeDyPositive);

        int t2x = t1x + 1;
        int t2y = t1y + 1;

        double xPosInCell = negativeDxPositive ? -entity.getDeltaX() % FluidEntity.SPACE : FluidEntity.SPACE + (-entity.getDeltaX() % FluidEntity.SPACE);
        double yPosInCell = negativeDyPositive ? -entity.getDeltaY() % FluidEntity.SPACE : FluidEntity.SPACE + (-entity.getDeltaY() % FluidEntity.SPACE);


        // Area of top right
        double bottomLeftAreaInversion = (FluidEntity.SPACE - xPosInCell) * (FluidEntity.SPACE - yPosInCell);

        // area of top left
        double bottomRightAreaInversion = xPosInCell * (FluidEntity.SPACE - yPosInCell);

        // area of bottom right
        double topLeftAreaInversion = (FluidEntity.SPACE - xPosInCell) * yPosInCell;

        // area of bottom left
        double topRightAreaInversion = xPosInCell * yPosInCell;

        double bottomLeftRatio = bottomLeftAreaInversion / CELL_AREA;
        double bottomRightRatio = bottomRightAreaInversion / CELL_AREA;
        double topLeftRatio = topLeftAreaInversion / CELL_AREA;
        double topRightRatio = topRightAreaInversion / CELL_AREA;

        if (bottomLeftRatio > 1 || bottomRightRatio > 1 || topLeftRatio > 1 || topRightRatio > 1) {
            System.out.println("Math problem");
        }

        if ((bottomLeftRatio + bottomRightRatio + topLeftRatio + topRightRatio) - 1 > .001) {
            System.out.println("Math problem!");
        }

        /*
         * To achieve this result I first create a list that for each point records the four points that are sources for
         * that point, and the fraction of each point they want. Simultaneously I accumulate the fractions asked of each
         * source point. In an ideal world, this would add up to one, as the entire value is being moved somewhere
         * (including partially back where it started). But with our compressible field the amount of the value in each
         * point that is being moved can be greater than or less than one. If the total fraction required is greater
         * than one, then we can simply scale all the requested fraction by this value, which means the total will be
         * one. If less than one, then the requesting points can have the full amount requested. We should not scale in
         * this case as it will lead to significant errors.
         */

        transferFrom(entity, entities, t1x, t1y, bottomLeftRatio);
        transferFrom(entity, entities, t2x, t1y, bottomRightRatio);
        transferFrom(entity, entities, t1x, t2y, topLeftRatio);
        transferFrom(entity, entities, t2x, t2y, topRightRatio);
    }


    private static int getLesserTargetIndex(int sourceIndex, int indexOffset, boolean directionPositive) {

        int targetIndex;

        if (directionPositive) {
            targetIndex = sourceIndex + indexOffset;
        } else {
            targetIndex = sourceIndex + indexOffset - 1;
        }

        return targetIndex;
    }

    private static void transferTo(FluidEntity entity, FluidEntity[][] entities, int targetXIndex, int targetYIndex, double ratio) {
        FluidEntity targetEntity = null;

        if (!(targetXIndex < 0 || targetYIndex < 0 || targetXIndex >= entities.length || targetYIndex >= entities[targetXIndex].length)) {
            targetEntity = entities[targetXIndex][targetYIndex];
        }

        entity.recordTransferTo(targetEntity, ratio);
    }

    private static void transferFrom(FluidEntity entity, FluidEntity[][] entities, int originXIndex, int originYIndex, double ratio) {
        if (originXIndex < 0) return;
        if (originYIndex < 0) return;
        if (originXIndex >= entities.length) return;
        if (originYIndex >= entities[originXIndex].length) return;
        entities[originXIndex][originYIndex].recordTransferTo(entity, ratio);
    }

}
