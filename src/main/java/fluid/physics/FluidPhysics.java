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
    private static final double GRAVITY_TO_VELOCITY_SCALE = .01;
    private static final double DENSITY_TO_VELOCITY_SCALE = .1;
    private static final double HEAT_TO_VELOCITY_SCALE = .02;


    public static void incrementFluid(FluidEntity[][] entities) {
        if (entities == null) return;

        applyPressure(entities);
        applyGravity(entities);
        advection(entities);
        applyStep(entities);
    }


    private static void applyPressure(FluidEntity[][] entities) {

        int d1 = entities.length;
        int d2 = entities[0].length;

        // pressure, heat, displacement, etc
        IntStream.range(0, d1).parallel().forEach(x -> IntStream.range(0, d2).forEach(y -> {
            FluidEntity entity = entities[x][y];

            // Left entities
            if (x > 0) {
                applyPressureToCell(entity, entities[x - 1][y], -1, 0);

                if (y > 0) {
                    applyPressureToCell(entity, entities[x - 1][y - 1], -1, -1);
                } else if (y + 1 < d2) {
                    applyPressureToCell(entity, entities[x - 1][y + 1], -1, 1);
                }
            }

            // Right entities
            if (x + 1 < d1) {
                applyPressureToCell(entity, entities[x + 1][y], 1, 0);

                if (y > 0) {
                    applyPressureToCell(entity, entities[x + 1][y - 1], 1, -1);
                } else if (y + 1 < d2) {
                    applyPressureToCell(entity, entities[x + 1][y + 1], 1, 1);
                }
            }

            // Lower entity
            if (y > 0) {
                applyPressureToCell(entity, entities[x][y - 1], 0, -1);
            }

            // Upper entity
            if (y + 1 < d2) {
                applyPressureToCell(entity, entities[x][y + 1], 0, 1);
            }
        }));
    }

    private static void applyPressureToCell(FluidEntity origin, FluidEntity target, int xOffset, int yOffset) {
        // For now, just assuming that xOffset and yOffset can only be 1, 0, -1
        double ratio;
        if (xOffset != 0 && yOffset != 0) {
            //ratio = .70710678; TODO: Fix this
            ratio = 0;
        } else {
            ratio = 1;
        }

        double forceX = 0;
        double forceY = 0;

        // pressure
        double massDifference = origin.getMass() - target.getMass();
        if (massDifference > 0) {
            forceX += xOffset * massDifference * DENSITY_TO_VELOCITY_SCALE * ratio;
            forceY += yOffset * massDifference * DENSITY_TO_VELOCITY_SCALE * ratio;
        }

        // heat
        double heatDifference = origin.getHeat() - target.getHeat();
        if (heatDifference > 0) {
            forceX += xOffset * heatDifference * HEAT_TO_VELOCITY_SCALE * ratio;
            forceY += yOffset * heatDifference * HEAT_TO_VELOCITY_SCALE * ratio;
        }

        if (forceX != 0) target.applyForceX(forceX);
        if (forceY != 0) target.applyForceY(forceY);


    }

    private static void applyGravity(FluidEntity[][] entities) {
        int d1 = entities.length;
        int d2 = entities[0].length;

        // pressure, heat, displacement, etc
        IntStream.range(0, d1).parallel().forEach(x -> IntStream.range(0, d2).forEach(y -> {
            FluidEntity entity = entities[x][y];

            // Lower entity
            if (y > 0) {
                applyGravityToCell(entity, entities[x][y - 1], -1);
            }

            // Upper entity
            if (y + 1 < d2) {
                applyGravityToCell(entity, entities[x][y + 1], 1);
            }
        }));
    }

    private static void applyGravityToCell(FluidEntity origin, FluidEntity target, int yOffset) {

        double massDifference = origin.getMass() - target.getMass();
        origin.applyForceY(yOffset * -massDifference * GRAVITY_TO_VELOCITY_SCALE);
    }


    /**
     * Advection moves the quantities from point to its connections/neighbors. Quantities include velocity/mass/heat/etc.
     * The amount moved from one point to another is based on the given point's velocity.
     */
    private static void advection(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> {
            forwardAdvectionCellTransfer(entities, x, y);
            reverseAdvectionCellTransfer(entities, x, y);
        }));
    }


    private static void applyStep(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].transferRelativeValues()));
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].transferAbsoluteValues()));
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

        entity.recordRelativeTransfer(targetEntity, ratio);
    }

    private static void transferFrom(FluidEntity entity, FluidEntity[][] entities, int originXIndex, int originYIndex, double ratio) {
        if (originXIndex < 0) return;
        if (originYIndex < 0) return;
        if (originXIndex >= entities.length) return;
        if (originYIndex >= entities[originXIndex].length) return;
        entities[originXIndex][originYIndex].recordRelativeTransfer(entity, ratio);
    }

}
