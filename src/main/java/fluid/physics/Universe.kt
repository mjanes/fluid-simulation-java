package fluid.physics

import fluid.entity.FluidEntity
import java.util.stream.IntStream

/**
 * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
 * http://www.dgp.toronto.edu/people/stam/reality/Research/pdf/GDC03.pdf
 */
class Universe(val entities: Array<Array<FluidEntity>>) {
    private var step = 0

    /**
     * Run round of physics
     */
    @Synchronized
    fun updateUniverseState() {
        ExternalInput.applyInput(entities, step)
        incrementFluid()
        step++
    }

    private fun incrementFluid() {
        // force applications
        applySoloEffects()
        applyNeighborInteractions()
        IntStream.range(0, entities.size).forEach { x: Int -> IntStream.range(0, entities[x].size).forEach { y: Int -> entities[x][y].changeHeat() } }
        IntStream.range(0, entities.size).forEach { x: Int -> IntStream.range(0, entities[x].size).forEach { y: Int -> entities[x][y].changeForce() } }
        advection()

        // transfer application
        IntStream.range(0, entities.size).forEach { x: Int -> IntStream.range(0, entities[x].size).forEach { y: Int -> entities[x][y].convertMassTransferToAbsoluteChange() } }
        IntStream.range(0, entities.size).parallel().forEach { x: Int -> IntStream.range(0, entities[x].size).forEach { y: Int -> entities[x][y].changeMass() } }
    }

    fun applySoloEffects() {
        IntStream.range(0, entities.size).parallel().forEach { i: Int -> IntStream.range(0, entities[i].size).forEach { j: Int -> entities[i][j].applySoloEffects() } }
    }

    fun applyNeighborInteractions() {

        // Left entity
        IntStream.range(1, entities.size).parallel().forEach { i: Int ->
            IntStream.range(0, entities[i].size).forEach { j: Int ->
                val entity = entities[i][j]
                entity.applyNeighborInteractions(entities[i - 1][j])
            }
        }

        // Right entity
        IntStream.range(0, entities.size - 1).parallel().forEach { i: Int ->
            IntStream.range(0, entities[i].size).forEach { j: Int ->
                val entity = entities[i][j]
                entity.applyNeighborInteractions(entities[i + 1][j])
            }
        }

        // Lower entity
        IntStream.range(0, entities.size).parallel().forEach { i: Int ->
            IntStream.range(1, entities[i].size).forEach { j: Int ->
                val entity = entities[i][j]
                entity.applyNeighborInteractions(entities[i][j - 1])
            }
        }

        // Upper entity
        IntStream.range(0, entities.size).parallel().forEach { i: Int ->
            IntStream.range(0, entities[i].size - 1).forEach { j: Int ->
                val entity = entities[i][j]
                entity.applyNeighborInteractions(entities[i][j + 1])
            }
        }
    }

    /**
     * Advection moves the quantities from point to its connections/neighbors. Quantities include velocity/mass/heat/etc.
     * The amount moved from one point to another is based on the given point's velocity.
     */
    private fun advection() {
        IntStream.range(0, entities.size).parallel().forEach { i: Int ->
            IntStream.range(0, entities[i].size).forEach { j: Int ->
                forwardAdvectionCellTransfer(i, j)
                reverseAdvectionCellTransfer(i, j)
            }
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     */
    private fun forwardAdvectionCellTransfer(xIndex: Int, yIndex: Int) {
        val entity = entities[xIndex][yIndex]
        val deltaX = entity.deltaX
        val deltaY = entity.deltaY
        if (deltaX == 0.0 && deltaY == 0.0) return
        val xIndexOffset = deltaX.toInt() / FluidEntity.SPACE
        val yIndexOffset = deltaY.toInt() / FluidEntity.SPACE
        val dxPositive = deltaX > 0
        val dyPositive = deltaY > 0
        val t1x = getLesserTargetIndex(xIndex, xIndexOffset, dxPositive)
        val t1y = getLesserTargetIndex(yIndex, yIndexOffset, dyPositive)
        val t2x = t1x + 1
        val t2y = t1y + 1
        val xPosInCell = if (dxPositive) entity.deltaX % FluidEntity.SPACE else FluidEntity.SPACE + entity.deltaX % FluidEntity.SPACE
        val yPosInCell = if (dyPositive) entity.deltaY % FluidEntity.SPACE else FluidEntity.SPACE + entity.deltaY % FluidEntity.SPACE

        // Area of top right
        val bottomLeftAreaInversion = (FluidEntity.SPACE - xPosInCell) * (FluidEntity.SPACE - yPosInCell)

        // area of top left
        val bottomRightAreaInversion = xPosInCell * (FluidEntity.SPACE - yPosInCell)

        // area of bottom right
        val topLeftAreaInversion = (FluidEntity.SPACE - xPosInCell) * yPosInCell

        // area of bottom left
        val topRightAreaInversion = xPosInCell * yPosInCell
        val bottomLeftRatio = bottomLeftAreaInversion / FluidEntity.CELL_AREA
        val bottomRightRatio = bottomRightAreaInversion / FluidEntity.CELL_AREA
        val topLeftRatio = topLeftAreaInversion / FluidEntity.CELL_AREA
        val topRightRatio = topRightAreaInversion / FluidEntity.CELL_AREA
        transferTo(entity, t1x, t1y, bottomLeftRatio)
        transferTo(entity, t2x, t1y, bottomRightRatio)
        transferTo(entity, t1x, t2y, topLeftRatio)
        transferTo(entity, t2x, t2y, topRightRatio)
    }

    /**
     * https://en.wikipedia.org/wiki/Bilinear_interpolation
     */
    private fun reverseAdvectionCellTransfer(xIndex: Int, yIndex: Int) {
        val entity = entities[xIndex][yIndex]
        val negativeDeltaX = -entity.deltaX
        val negativeDeltaY = -entity.deltaY
        if (negativeDeltaX == 0.0 && negativeDeltaY == 0.0) return
        val xIndexOffset = negativeDeltaX.toInt() / FluidEntity.SPACE
        val yIndexOffset = negativeDeltaY.toInt() / FluidEntity.SPACE
        val negativeDxPositive = negativeDeltaX > 0
        val negativeDyPositive = negativeDeltaY > 0
        val t1x = getLesserTargetIndex(xIndex, xIndexOffset, negativeDxPositive)
        val t1y = getLesserTargetIndex(yIndex, yIndexOffset, negativeDyPositive)
        val t2x = t1x + 1
        val t2y = t1y + 1
        val xPosInCell = if (negativeDxPositive) -entity.deltaX % FluidEntity.SPACE else FluidEntity.SPACE + -entity.deltaX % FluidEntity.SPACE
        val yPosInCell = if (negativeDyPositive) -entity.deltaY % FluidEntity.SPACE else FluidEntity.SPACE + -entity.deltaY % FluidEntity.SPACE


        // Area of top right
        val bottomLeftAreaInversion = (FluidEntity.SPACE - xPosInCell) * (FluidEntity.SPACE - yPosInCell)

        // area of top left
        val bottomRightAreaInversion = xPosInCell * (FluidEntity.SPACE - yPosInCell)

        // area of bottom right
        val topLeftAreaInversion = (FluidEntity.SPACE - xPosInCell) * yPosInCell

        // area of bottom left
        val topRightAreaInversion = xPosInCell * yPosInCell
        val bottomLeftRatio = bottomLeftAreaInversion / FluidEntity.CELL_AREA
        val bottomRightRatio = bottomRightAreaInversion / FluidEntity.CELL_AREA
        val topLeftRatio = topLeftAreaInversion / FluidEntity.CELL_AREA
        val topRightRatio = topRightAreaInversion / FluidEntity.CELL_AREA
        if (bottomLeftRatio > 1 || bottomRightRatio > 1 || topLeftRatio > 1 || topRightRatio > 1) {
            println("Math problem")
        }
        if (bottomLeftRatio + bottomRightRatio + topLeftRatio + topRightRatio - 1 > .001) {
            println("Math problem!")
        }
        transferFrom(entity, t1x, t1y, bottomLeftRatio)
        transferFrom(entity, t2x, t1y, bottomRightRatio)
        transferFrom(entity, t1x, t2y, topLeftRatio)
        transferFrom(entity, t2x, t2y, topRightRatio)
    }

    private fun transferTo(originEntity: FluidEntity, targetXIndex: Int, targetYIndex: Int, ratio: Double) {
        val targetEntity = getEntity(targetXIndex, targetYIndex)
        originEntity.recordMassTransferTo(targetEntity, ratio)
    }

    private fun transferFrom(targetEntity: FluidEntity, originXIndex: Int, originYIndex: Int, ratio: Double) {
        val originEntity = getEntity(originXIndex, originYIndex)
        originEntity.recordMassTransferTo(targetEntity, ratio)
    }

    private fun getEntity(xIndex: Int, yIndex: Int): FluidEntity {
        // Handle if outside of universe
        var x = xIndex
        var y = yIndex
        if (xIndex < 0) {
            x = 0
        } else if (xIndex >= entities.size) {
            x = entities.size - 1
        }
        if (y < 0) {
            y = 0
        } else if (y >= entities[x].size) {
            y = entities[x].size - 1
        }
        return entities[x][y]
    }

    private fun getLesserTargetIndex(sourceIndex: Int, indexOffset: Int, directionPositive: Boolean): Int {
        return sourceIndex + indexOffset + if (directionPositive) 0 else -1
    }

    companion object {
        /**
         * Maximum number of immediate neighbors that each [FluidEntity] may have bidirectional interactions with.
         * Used to ensure that an entity does not transfer more than 100% of its heat/pressure/etc to its neighbors
         */
        const val MAX_NEIGHBORS = 4
        const val GRAVITATIONAL_CONSTANT = .0001
    }

}