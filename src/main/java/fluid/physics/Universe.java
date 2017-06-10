package fluid.physics;

import fluid.entity.FluidEntity;

import java.util.stream.IntStream;

/**
 * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
 * http://www.dgp.toronto.edu/people/stam/reality/Research/pdf/GDC03.pdf
 */
public class Universe {

    /**
     * Maximum number of immediate neighbors that each {@link FluidEntity} may have bidirectional interactions with.
     * Used to ensure that an entity does not transfer more than 100% of its heat/pressure/etc to its neighbors
     */
    public static final int MAX_NEIGHBORS = 4;

    private int step = 0;

    private final FluidEntity[][] entities;

    public Universe(FluidEntity[][] entities) {
        this.entities = entities;
    }

    /**
     * Run round of physics
     */
    public synchronized void updateUniverseState() {
        ExternalInput.applyInput(entities, step);
        incrementFluid();
        step++;
    }

    public FluidEntity[][] getEntities() {
        return entities;
    }

    private static final double GRAVITATIONAL_CONSTANT = .00001;

    private void incrementFluid() {
        // force applications
        applyBidirectionalInteractions();
        applyGravity();

        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].changeHeat()));
        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].changeForce()));

        // transfer logging
        advection();

        // transfer application
        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].convertMassTransferToAbsoluteChange()));
        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].changeMass()));
    }


    private void applyBidirectionalInteractions() {
        IntStream.range(0, entities.length - 1).parallel().forEach(i -> IntStream.range(0, entities[i].length - 1).forEach(j -> {
            FluidEntity entity = entities[i][j];

            // Right entity
            entity.applyBidirectionalInteractions(entities[i + 1][j]);

            // Upper entity
            entity.applyBidirectionalInteractions(entities[i][j + 1]);
        }));
    }

    private void applyGravity() {
        final double[][] downwardPressure = new double[entities.length][entities[0].length];

        for (int i = 0; i < entities.length; i++) {
            for (int j = entities[i].length - 2; j >= 0; j--) {
                downwardPressure[i][j] = downwardPressure[i][j + 1] + (entities[i][j].getMass() * GRAVITATIONAL_CONSTANT);
            }
        }

        IntStream.range(0, entities.length).parallel().forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].addForceY(-downwardPressure[x][y])));
    }
    
    /**
     * Advection moves the quantities from point to its connections/neighbors. Quantities include velocity/mass/heat/etc.
     * The amount moved from one point to another is based on the given point's velocity.
     */
    private void advection() {
        IntStream.range(0, entities.length).parallel().forEach(i -> IntStream.range(0, entities[i].length).forEach(j -> {
            forwardAdvectionCellTransfer(i, j);
            reverseAdvectionCellTransfer(i, j);
        }));
    }

    /**
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     */
    private void forwardAdvectionCellTransfer(int xIndex, int yIndex) {
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

        double bottomLeftRatio = bottomLeftAreaInversion / FluidEntity.CELL_AREA;
        double bottomRightRatio = bottomRightAreaInversion / FluidEntity.CELL_AREA;
        double topLeftRatio = topLeftAreaInversion / FluidEntity.CELL_AREA;
        double topRightRatio = topRightAreaInversion / FluidEntity.CELL_AREA;

        transferTo(entity, t1x, t1y, bottomLeftRatio);
        transferTo(entity, t2x, t1y, bottomRightRatio);
        transferTo(entity, t1x, t2y, topLeftRatio);
        transferTo(entity, t2x, t2y, topRightRatio);
    }

    /**
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     */
    private void reverseAdvectionCellTransfer(int xIndex, int yIndex) {
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

        double bottomLeftRatio = bottomLeftAreaInversion / FluidEntity.CELL_AREA;
        double bottomRightRatio = bottomRightAreaInversion / FluidEntity.CELL_AREA;
        double topLeftRatio = topLeftAreaInversion / FluidEntity.CELL_AREA;
        double topRightRatio = topRightAreaInversion / FluidEntity.CELL_AREA;

        if (bottomLeftRatio > 1 || bottomRightRatio > 1 || topLeftRatio > 1 || topRightRatio > 1) {
            System.out.println("Math problem");
        }

        if ((bottomLeftRatio + bottomRightRatio + topLeftRatio + topRightRatio) - 1 > .001) {
            System.out.println("Math problem!");
        }

        transferFrom(entity, t1x, t1y, bottomLeftRatio);
        transferFrom(entity, t2x, t1y, bottomRightRatio);
        transferFrom(entity, t1x, t2y, topLeftRatio);
        transferFrom(entity, t2x, t2y, topRightRatio);
    }

    private void transferTo(FluidEntity originEntity, int targetXIndex, int targetYIndex, double ratio) {
        FluidEntity targetEntity = getEntity(targetXIndex, targetYIndex);
        originEntity.recordMassTransferTo(targetEntity, ratio);
    }

    private void transferFrom(FluidEntity targetEntity, int originXIndex, int originYIndex, double ratio) {
        FluidEntity originEntity = getEntity(originXIndex, originYIndex);
        originEntity.recordMassTransferTo(targetEntity, ratio);
    }

    private FluidEntity getEntity(int xIndex, int yIndex) {
        // Handle if outside of universe
        if (xIndex < 0) {
            xIndex = 0;
        } else if (xIndex >= entities.length) {
            xIndex = entities.length - 1;
        }
        if (yIndex < 0) {
            yIndex = 0;
        } else if (yIndex >= entities[xIndex].length) {
            yIndex = entities[xIndex].length - 1;
        }
        return entities[xIndex][yIndex];
    }

    private int getLesserTargetIndex(int sourceIndex, int indexOffset, boolean directionPositive) {
        return sourceIndex + indexOffset + (directionPositive ? 0 : -1);
    }

}
