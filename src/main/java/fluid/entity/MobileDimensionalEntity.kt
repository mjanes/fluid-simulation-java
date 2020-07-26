package fluid.entity

/**
 * Created by mjanes on 6/12/2014.
 */
interface MobileDimensionalEntity : DimensionalEntity {
    fun addDeltaX(deltaDeltaX: Double)
    var deltaX: Double
    fun moveX(deltaX: Double)
    fun addDeltaY(deltaDeltaY: Double)
    var deltaY: Double
    fun moveY(deltaY: Double)
    fun addDeltaZ(deltaDeltaZ: Double)
    var deltaZ: Double
    fun moveZ(deltaZ: Double)
    fun move()
}