package fluid.physics;

import fluid.entity.FluidEntity;

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

    public static void incrementFluid(FluidEntity[][] entities) {
        if (entities == null) return;

        applyPressure(entities);
        forwardAdvection(entities);
        reverseAdvection(entities);
    }

    private static void applyPressure(FluidEntity[][] entities) {

        int d1 = entities.length;
        int d2 = entities[0].length;

        // pressure
        IntStream.range(0, d1).forEach(x -> {
            IntStream.range(0, d2).forEach(y -> {
                FluidEntity entity = entities[x][y];
                FluidEntity otherEntity;

                // Left Entity
                if (x > 0) {
                    otherEntity = entities[x - 1][y];
                    if (entity.getMass() > otherEntity.getMass()) {
                        otherEntity.addNextDeltaX(-(entity.getMass() - otherEntity.getMass()) * DENSITY_TO_VELOCITY_SCALE);
                    }
                }

                // Right entity
                if (x + 1 < d1) {
                    otherEntity = entities[x + 1][y];
                    if (entity.getMass() > otherEntity.getMass()) {
                        otherEntity.addNextDeltaX((entity.getMass() - otherEntity.getMass()) * DENSITY_TO_VELOCITY_SCALE);
                    }
                }

                // Upper entity
                if (y > 0) {
                    otherEntity = entities[x][y - 1];
                    if (entity.getMass() > otherEntity.getMass()) {
                        otherEntity.addNextDeltaY(-(entity.getMass() - otherEntity.getMass()) * DENSITY_TO_VELOCITY_SCALE);
                    }
                }

                // Lower entity
                if (y + 1 < d2) {
                    otherEntity = entities[x][y + 1];
                    if (entity.getMass() > otherEntity.getMass()) {
                        otherEntity.addNextDeltaY((entity.getMass() - otherEntity.getMass()) * DENSITY_TO_VELOCITY_SCALE);
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
        IntStream.range(0, entities.length).forEach(x -> {
            IntStream.range(0, entities[0].length).forEach(y -> {
                forwardAdvectionCellTransfer(entities, x, y);
            });
        });

        applyStep(entities);
    }

    private static void reverseAdvection(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).forEach(x -> {
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

        final int d1 = entities.length;
        final int d2 = entities[0].length;

        FluidEntity entity = entities[xIndex][yIndex];
        double deltaX = entity.getDeltaX();
        double deltaY = entity.getDeltaY();

        if (deltaX == 0 && deltaY == 0) return;

        int xIndexOffset = (int) deltaX / FluidEntity.SPACE;
        int yIndexOffset = (int) deltaY / FluidEntity.SPACE;


        int t1x = getLesserTargetIndex(xIndex, d1, xIndexOffset, deltaX > 0);
        int t1y = getLesserTargetIndex(yIndex, d2, yIndexOffset, deltaY > 0);

        int t2x = t1x + 1;
        int t2y = t1y + 1;

        FluidEntity bottomLeft = entities[t1x][t1y];
        FluidEntity bottomRight = entities[t2x][t1y];
        FluidEntity topLeft = entities[t1x][t2y];
        FluidEntity topRight = entities[t2x][t2y];

        FluidEntity nextLocation = entity.getNextLocationAsFluidEntity();
        double nextX = nextLocation.getX();
        double nextY = nextLocation.getY();

        // This should only happen in border locations where t1x/y was reset, and should be impossible otherwise
        if (nextX < bottomLeft.getX()) {
            nextX = bottomLeft.getX();
        }
        if (nextX > bottomRight.getX()) {
            nextX = bottomRight.getX();
        }
        if (nextY < bottomLeft.getY()) {
            nextY = bottomLeft.getY();
        }
        if (nextY > topLeft.getY()) {
            nextY = topLeft.getY();
        }


        // Area of top right
        double bottomLeftAreaInversion = (topRight.getX() - nextX) * (topRight.getY() - nextY);

        // area of top left
        double bottomRightAreaInversion = (nextX - topLeft.getX()) * (topLeft.getY() - nextY);

        // area of bottom right
        double topLeftAreaInversion = (bottomRight.getX() - nextX) * (nextY - bottomRight.getY());

        // area of bottom left
        double topRightAreaInversion = (nextX - bottomLeft.getX()) * (nextY - bottomLeft.getY());

        double bottomLeftRatio = bottomLeftAreaInversion / CELL_AREA;
        double bottomRightRatio = bottomRightAreaInversion / CELL_AREA;
        double topLeftRatio = topLeftAreaInversion / CELL_AREA;
        double topRightRatio = topRightAreaInversion / CELL_AREA;

        if ((bottomLeftRatio + bottomRightRatio + topLeftRatio + topRightRatio) - 1 > .001) {
            System.out.println("Math problem!");
        }

        entity.transfer(bottomLeft, bottomLeftRatio);
        entity.transfer(bottomRight, bottomRightRatio);
        entity.transfer(topLeft, topLeftRatio);
        entity.transfer(topRight, topRightRatio);
    }

    /**
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     */
    private static void reverseAdvectionCellTransfer(FluidEntity[][] entities, int xIndex, int yIndex) {

        final int d1 = entities.length;
        final int d2 = entities[0].length;

        FluidEntity entity = entities[xIndex][yIndex];
        double negativeDeltaX = -entity.getDeltaX();
        double negativeDeltaY = -entity.getDeltaY();

        if (negativeDeltaX == 0 && negativeDeltaY == 0) return;

        int xIndexOffset = (int) negativeDeltaX / FluidEntity.SPACE;
        int yIndexOffset = (int) negativeDeltaY / FluidEntity.SPACE;

        int t1x = getLesserTargetIndex(xIndex, d1, xIndexOffset, negativeDeltaX > 0);
        int t1y = getLesserTargetIndex(yIndex, d2, yIndexOffset, negativeDeltaY > 0);

        int t2x = t1x + 1;
        int t2y = t1y + 1;

        FluidEntity bottomLeft = entities[t1x][t1y];
        FluidEntity bottomRight = entities[t2x][t1y];
        FluidEntity topLeft = entities[t1x][t2y];
        FluidEntity topRight = entities[t2x][t2y];

        FluidEntity previousLocation = entity.getPreviousLocationAsFluidEntity();
        double previousX = previousLocation.getX();
        double previousY = previousLocation.getY();

        // This should only happen in border locations where t1x/y was reset, and should be impossible otherwise
        if (previousX < bottomLeft.getX()) {
            previousX = bottomLeft.getX();
        }
        if (previousX > bottomRight.getX()) {
            previousX = bottomRight.getX();
        }
        if (previousY < bottomLeft.getY()) {
            previousY = bottomLeft.getY();
        }
        if (previousY > topLeft.getY()) {
            previousY = topLeft.getY();
        }

        // Area of top right
        double bottomLeftAreaInversion = (topRight.getX() - previousX) * (topRight.getY() - previousY);

        // area of top left
        double bottomRightAreaInversion = (previousX - topLeft.getX()) * (topLeft.getY() - previousY);

        // area of bottom right
        double topLeftAreaInversion = (bottomRight.getX() - previousX) * (previousY - bottomRight.getY());

        // area of bottom left
        double topRightAreaInversion = (previousX - bottomLeft.getX()) * (previousY - bottomLeft.getY());

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

        bottomLeft.transfer(entity, bottomLeftRatio);
        bottomRight.transfer(entity, bottomRightRatio);
        topLeft.transfer(entity, topLeftRatio);
        topRight.transfer(entity, topRightRatio);
    }


    private static int getLesserTargetIndex(int sourceIndex, int dimension, int indexOffset, boolean directionPositive) {

        int targetIndex;

        if (directionPositive) {
            targetIndex = sourceIndex + indexOffset;
        } else {
            targetIndex = sourceIndex + indexOffset - 1;
        }

        // Deal with border conditions
        if (targetIndex < 0) {
            targetIndex = 0;
        }
        if (targetIndex + 1 >= dimension) {
            targetIndex = dimension - 2;
        }

        return targetIndex;
    }

}
