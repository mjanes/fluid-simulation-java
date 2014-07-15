package fluid.physics;

import fluid.entity.FluidEntity;
import fluid.entity.IFluidEntity;
import fluid.entity.MockFluidEntity;

import java.util.stream.IntStream;

/**
 * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
 * http://www.dgp.toronto.edu/people/stam/reality/Research/pdf/GDC03.pdf
 *
 * Created by mjanes on 6/16/2014.
 */
public class FluidPhysics {

    private static final double GRAVITATIONAL_CONSTANT = .00001;

    private static final IFluidEntity sMockFluidEntity = new MockFluidEntity();

    // TODO: Other border types to introduce:
    // - viscous
    // - something that isn't 100% reflective, but say converts x into y or something...
    public enum BorderType {
        REFLECTIVE,
        OPEN, // TODO: Need to improve open, it is still providing resistance
        NULLING};

    private static BorderType sBottomBorderType = BorderType.REFLECTIVE;
    private static BorderType sLeftBorderType = BorderType.OPEN;
    private static BorderType sRightBorderType = BorderType.OPEN;
    private static BorderType sUpperBorderType = BorderType.OPEN;

    public static void incrementFluid(FluidEntity[][] entities) {
        if (entities == null) return;

        // force applications
        applyBidirectionInteractions(entities);
        applyGravity(entities);

        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].convertHeatTransferToAbsoluteChange()));
        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].changeHeat()));
        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].changeForce()));

        // check for border effect
        checkBorder(entities);

        // transfer logging
        advection(entities);

        // transfer application
        sMockFluidEntity.convertMassTransferToAbsoluteChange();
        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].convertMassTransferToAbsoluteChange()));
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].changeMass()));
    }


    private static void applyBidirectionInteractions(FluidEntity[][] entities) {
        IntStream.range(-1, entities.length).parallel().forEach(i -> IntStream.range(-1, entities[0].length).forEach(j -> {
            IFluidEntity entity = null;
            if (i == -1 || j == -1) {
                if (!(i == -1 && j == -1)) {
                    if (i == -1 && sLeftBorderType.equals(BorderType.OPEN)) {
                        entity = sMockFluidEntity;
                    }
                    if (j == -1 && sBottomBorderType.equals(BorderType.OPEN)) {
                        entity = sMockFluidEntity;
                    }
                }
            } else {
                entity = entities[i][j];
            }

            // Right entity
            if (j != -1) {
                if (i + 1 < entities.length) {
                    applyConductionBetweenCells(entity, entities[i + 1][j]);
                    applyPressureBetweenCells(entity, entities[i + 1][j], true, false);
                    applyViscosityBetweenCells(entity, entities[i + 1][j], true, false);
                } else if (i + 1 == entities.length && sRightBorderType.equals(BorderType.OPEN)) {
                    applyConductionBetweenCells(entity, sMockFluidEntity);
                    applyPressureBetweenCells(entity, sMockFluidEntity, true, false);
                    applyViscosityBetweenCells(entity, sMockFluidEntity, true, false);
                }
            }

            // Upper entity
            if (i != -1) {
                if (j + 1 < entities[i].length) {
                    applyConductionBetweenCells(entity, entities[i][j + 1]);
                    applyPressureBetweenCells(entity, entities[i][j + 1], false, true);
                    applyViscosityBetweenCells(entity, entities[i][j + 1], false, true);
                } else if (j + 1 == entities[i].length && sUpperBorderType.equals(BorderType.OPEN)) {
                    applyConductionBetweenCells(entity, sMockFluidEntity);
                    applyPressureBetweenCells(entity, sMockFluidEntity, false, true);
                    applyViscosityBetweenCells(entity, sMockFluidEntity, false, true);
                }
            }
        }));
    }

    /**
     * https://en.wikipedia.org/wiki/Thermal_conductivity
     * TODO: Fix
     */
    private static void applyConductionBetweenCells(IFluidEntity a, IFluidEntity b) {
        if (a == null || b == null) return;
        double temperatureDifference = a.getTemperature() - b.getTemperature();
        double totalMass = a.getMass() + b.getMass();
        double meanConductivity = a.getConductivity() * (a.getMass() / totalMass) + b.getConductivity() * (b.getMass() / totalMass);

        a.addHeat(-temperatureDifference * b.getConductivity() * b.getMass());
        b.addHeat(temperatureDifference * a.getConductivity() * a.getMass());
    }

    private static void applyPressureBetweenCells(IFluidEntity a, IFluidEntity b, boolean xOffset, boolean yOffset) {
        if (a == null || b == null) return;
        double pressureDifference = a.getPressure() - b.getPressure();
        if (pressureDifference > 0) {
            if (xOffset) a.addForceX(pressureDifference);
            if (yOffset) a.addForceY(pressureDifference);
        } else if (pressureDifference < 0) {
            if (xOffset) b.addForceX(pressureDifference);
            if (yOffset) b.addForceY(pressureDifference);
        }
    }

    private static void applyViscosityBetweenCells(IFluidEntity a, IFluidEntity b, boolean xOffset, boolean yOffset) {
        if (a == null || b == null) return;
        double totalMass = a.getMass() + b.getMass();
        double meanViscosity = a.getViscosity() * (a.getMass() / totalMass) + b.getViscosity() * (b.getMass() / totalMass);

        if (xOffset) {
            double deltaYDifference = a.getDeltaY() - b.getDeltaY();
            double forceTransfer;
            a.recordForceChange(new IFluidEntity.ForceChangeRecord(0, (b.getMass() / totalMass) * deltaYDifference * meanViscosity));
            b.recordForceChange(new IFluidEntity.ForceChangeRecord(0, -(a.getMass() / totalMass) * deltaYDifference * meanViscosity));
        }
        if (yOffset) {
            double deltaXDifference = a.getDeltaX() - b.getDeltaX();
            double forceTransfer;
            a.recordForceChange(new IFluidEntity.ForceChangeRecord((b.getMass() / totalMass) * deltaXDifference * meanViscosity, 0));
            b.recordForceChange(new IFluidEntity.ForceChangeRecord(-(a.getMass() / totalMass) * deltaXDifference * meanViscosity, 0));
        }
    }

    private static void applyGravity(FluidEntity[][] entities) {
        final double[][] downwardPressure = new double[entities.length][entities[0].length];

        for (int i = 0; i < entities.length; i++) {
            downwardPressure[i][entities[i].length - 1] = sMockFluidEntity.getMass() * GRAVITATIONAL_CONSTANT;
            for (int j = entities[i].length - 2; j >= 0; j--) {
                downwardPressure[i][j] = downwardPressure[i][j + 1] + (entities[i][j].getMass() * GRAVITATIONAL_CONSTANT);
            }
        }

        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> {
            entities[x][y].addForceY(-downwardPressure[x][y]);
        }));
    }

    private static void checkBorder(FluidEntity[][] entities) {
        // Presume everything is in grid for simplicty's sake
        // get max and min x and y for everything
        double minX = entities[0][0].getX();
        double minY = entities[0][0].getY();
        double maxX = entities[entities.length - 1][0].getX();
        double maxY = entities[0][entities[0].length - 1].getY();

        IntStream.range(0, entities.length).parallel().forEach(i -> IntStream.range(0, entities[i].length).forEach(j -> {
            FluidEntity entity = entities[i][j];
            double nextX = entity.getX() + entity.getDeltaX();
            double nextY = entity.getY() + entity.getDeltaY();

            if (nextX < minX) {
                if (sLeftBorderType.equals(BorderType.REFLECTIVE)) {
                    entity.setDeltaX(-entity.getDeltaX());
                } else if (sLeftBorderType.equals(BorderType.NULLING)) {
                    entity.setDeltaX(0);
                }
            }

            if (nextX > maxX) {
                if (sRightBorderType.equals(BorderType.REFLECTIVE)) {
                    entity.setDeltaX(-entity.getDeltaX());
                } else if (sRightBorderType.equals(BorderType.NULLING)) {
                    entity.setDeltaX(0);
                }
            }

            if (nextY < minY) {
                if (sBottomBorderType.equals(BorderType.REFLECTIVE)) {
                    entity.setDeltaY(-entity.getDeltaY());
                } else if (sBottomBorderType.equals(BorderType.NULLING)) {
                    entity.setDeltaY(0);
                }
            }

            if (nextY > maxY){
                if (sUpperBorderType.equals(BorderType.REFLECTIVE)) {
                    entity.setDeltaY(-entity.getDeltaY());
                } else if (sUpperBorderType.equals(BorderType.NULLING)) {
                    entity.setDeltaY(0);
                }
            }
        }));
    }

    /**
     * Advection moves the quantities from point to its connections/neighbors. Quantities include velocity/mass/heat/etc.
     * The amount moved from one point to another is based on the given point's velocity.
     */
    private static void advection(FluidEntity[][] entities) {
        IntStream.range(0, entities.length).parallel().forEach(i -> IntStream.range(0, entities[i].length).forEach(j -> {
            forwardAdvectionCellTransfer(entities, i, j);
            reverseAdvectionCellTransfer(entities, i, j);
        }));
    }

    /**
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     */
    private static void forwardAdvectionCellTransfer(FluidEntity[][] entities, int xIndex, int yIndex) {
        FluidEntity entity = entities[xIndex][yIndex];
        double deltaX = entity.getDeltaX();
        double deltaY = entity.getDeltaY();

        if (deltaX == 0 && deltaY == 0) return;

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

        double bottomLeftRatio = bottomLeftAreaInversion / IFluidEntity.CELL_AREA;
        double bottomRightRatio = bottomRightAreaInversion / IFluidEntity.CELL_AREA;
        double topLeftRatio = topLeftAreaInversion / IFluidEntity.CELL_AREA;
        double topRightRatio = topRightAreaInversion / IFluidEntity.CELL_AREA;

        transferTo(entity, entities, t1x, t1y, bottomLeftRatio);
        transferTo(entity, entities, t2x, t1y, bottomRightRatio);
        transferTo(entity, entities, t1x, t2y, topLeftRatio);
        transferTo(entity, entities, t2x, t2y, topRightRatio);
    }

    private static void transferTo(FluidEntity originEntity, FluidEntity[][] entities, int targetXIndex, int targetYIndex, double ratio) {
        FluidEntity targetEntity = null;

        // If this is true, the target entity is on screen.
        if (!(targetXIndex < 0 || targetYIndex < 0 || targetXIndex >= entities.length || targetYIndex >= entities[targetXIndex].length)) {
            targetEntity = entities[targetXIndex][targetYIndex];
        }

        originEntity.recordMassTransfer(targetEntity, ratio);
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

        double bottomLeftRatio = bottomLeftAreaInversion / IFluidEntity.CELL_AREA;
        double bottomRightRatio = bottomRightAreaInversion / IFluidEntity.CELL_AREA;
        double topLeftRatio = topLeftAreaInversion / IFluidEntity.CELL_AREA;
        double topRightRatio = topRightAreaInversion / IFluidEntity.CELL_AREA;

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

    private static void transferFrom(FluidEntity targetEntity, FluidEntity[][] entities, int originXIndex, int originYIndex, double ratio) {
        IFluidEntity originEntity = null;

        if (originXIndex < 0) {
            if (sLeftBorderType.equals(BorderType.OPEN)) {
                originEntity = sMockFluidEntity;
            }
        } else if (originYIndex < 0) {
            if (sBottomBorderType.equals(BorderType.OPEN)) {
                originEntity = sMockFluidEntity;
            }
        } else if (originXIndex >= entities.length) {
            if (sRightBorderType.equals(BorderType.OPEN)) {
                originEntity = sMockFluidEntity;
            }
        } else if (originYIndex >= entities[originXIndex].length) {
            if (sUpperBorderType.equals(BorderType.OPEN)) {
                originEntity = sMockFluidEntity;
            }
        } else {
            originEntity = entities[originXIndex][originYIndex];
        }

        if (originEntity != null) {
            originEntity.recordMassTransfer(targetEntity, ratio);
        }
    }

    private static int getLesserTargetIndex(int sourceIndex, int indexOffset, boolean directionPositive) {
        return sourceIndex + indexOffset + (directionPositive ? 0 : -1);
    }

}
