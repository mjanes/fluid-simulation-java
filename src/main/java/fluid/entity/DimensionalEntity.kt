package fluid.entity

import org.apache.commons.math3.linear.Array2DRowRealMatrix

interface DimensionalEntity {
    var x: Double
    var y: Double
    var z: Double
    fun getDistance(other: DimensionalEntity?): Double
    val r4Matrix: Array2DRowRealMatrix?

    companion object {
        @JvmStatic
        fun getDistance(a: DimensionalEntity, b: DimensionalEntity): Double {
            return Math.sqrt(
                    Math.pow(a.x - b.x, 2.0) +
                            Math.pow(a.y - b.y, 2.0) +
                            Math.pow(a.z - b.z, 2.0)
            )
        }
    }
}