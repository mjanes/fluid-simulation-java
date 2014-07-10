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

    public static final double ROOM_TEMPERATURE = 10;
    public static final double DEFAULT_MASS = 10;

    private static final double CELL_AREA = Math.pow(FluidEntity.SPACE, 2);

    private static final double GRAVITATIONAL_CONSTANT = .02; // TODO: Better handle gravity

    public enum BorderType {REFLECTIVE, OPEN, NULLING};

    private static BorderType sBorderType = BorderType.NULLING;

    public static void incrementFluid(FluidEntity[][] entities) {
        if (entities == null) return;

        // force applications
        applyConduction(entities);
        applyPressure(entities);
        applyGravity(entities);
        applyViscosity(entities);

        // transfer logging
        advection(entities);

        // transfer application
        applyStep(entities);
    }


    private static void applyConduction(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> {
            FluidEntity entity = entities[x][y];

            // Right entity
            if (x + 1 < entities.length) {
                applyConductionBetweenCells(entity, entities[x + 1][y]);
            }

            // Upper entity
            if (y + 1 < entities[x].length) {
                applyConductionBetweenCells(entity, entities[x][y + 1]);
            }
        }));
    }

    private static void applyConductionBetweenCells(FluidEntity a, FluidEntity b) {
        double temperatureDifference = a.getTemperature() - b.getTemperature();
        a.addHeat(-temperatureDifference * b.getConductivity() * b.getMass());
        b.addHeat(temperatureDifference * a.getConductivity() * a.getMass());
    }

    private static void applyPressure(FluidEntity[][] entities) {
        // pressure, heat, displacement, etc
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> {
            FluidEntity entity = entities[x][y];

            // Right entity
            if (x + 1 < entities.length) {
                applyPressureBetweenCells(entity, entities[x + 1][y], true, false);
            }

            // Upper entity
            if (y + 1 < entities[x].length) {
                applyPressureBetweenCells(entity, entities[x][y + 1], false, true);
            }
        }));
    }

    private static void applyPressureBetweenCells(FluidEntity a, FluidEntity b, boolean xOffset, boolean yOffset) {
        double pressureDifference = a.getPressure() - b.getPressure();
        if (pressureDifference > 0) {
            if (xOffset) a.addForceX(pressureDifference);
            if (yOffset) a.addForceY(pressureDifference);
        } else if (pressureDifference < 0) {
            if (xOffset) b.addForceX(pressureDifference);
            if (yOffset) b.addForceY(pressureDifference);
        }
    }

    private static void applyGravity(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length - 1).forEach(y -> {
            FluidEntity lower = entities[x][y];
            FluidEntity upper = entities[x][y + 1];
            double massDifference = upper.getMass() - lower.getMass();
            if (massDifference > 0) {
                upper.addForceY(-massDifference * GRAVITATIONAL_CONSTANT);
            }
        }));
    }

    /**
     * https://en.wikipedia.org/wiki/Shear_stress
     */
    private static void applyViscosity(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> {
            FluidEntity a = entities[x][y];

            // Right entity
            if (x + 1 < entities.length) {
                FluidEntity b = entities[x + 1][y];
                double deltaYDifference = a.getDeltaY() - b.getDeltaY();
                a.addForceY(-deltaYDifference * b.getViscosity() * b.getMass());
                b.addForceY(deltaYDifference * a.getViscosity() * a.getMass());
            }

            // Upper entity
            if (y + 1 < entities[x].length) {
                FluidEntity b = entities[x][y + 1];
                double deltaXDifference = a.getDeltaX() - b.getDeltaX();
                a.addForceX(-deltaXDifference * b.getViscosity() * b.getMass());
                b.addForceX(deltaXDifference * a.getViscosity() * a.getMass());
            }
        }));
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
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].transferOutgoingValues()));
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].transferIncomingValues()));
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

        // If this is true, the target entity is on screen.
        if (!(targetXIndex < 0 || targetYIndex < 0 || targetXIndex >= entities.length || targetYIndex >= entities[targetXIndex].length)) {
            targetEntity = entities[targetXIndex][targetYIndex];

        }

        if (targetEntity == null) {
            if (sBorderType.equals(BorderType.OPEN)) {
                entity.recordRelativeTransfer(targetEntity, ratio);
            } else {
                // Testing, if the entity would target off screen, reflect/bounce back
                // Or just neutralize?
                if (targetXIndex < 0 || targetXIndex >= entities.length) {
                    if (sBorderType.equals(BorderType.REFLECTIVE)) {
                        entity.setDeltaX(-entity.getDeltaX());
                    } else if (sBorderType.equals(BorderType.NULLING)) {
                        entity.setDeltaX(0);
                    }
                }
                if (targetYIndex < 0 || targetYIndex >= entities[0].length) {
                    if (sBorderType.equals(BorderType.REFLECTIVE)) {
                        entity.setDeltaY(-entity.getDeltaY());
                    } else if (sBorderType.equals(BorderType.NULLING)) {
                        entity.setDeltaY(0);
                    }
                }
            }

        } else {
            entity.recordRelativeTransfer(targetEntity, ratio);
        }
    }

    private static void transferFrom(FluidEntity entity, FluidEntity[][] entities, int originXIndex, int originYIndex, double ratio) {
        if (originXIndex < 0) return;
        if (originYIndex < 0) return;
        if (originXIndex >= entities.length) return;
        if (originYIndex >= entities[originXIndex].length) return;
        entities[originXIndex][originYIndex].recordRelativeTransfer(entity, ratio);
    }

}
