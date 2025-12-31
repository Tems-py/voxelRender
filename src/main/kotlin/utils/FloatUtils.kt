package me.tems.utils

import kotlin.math.roundToInt

object FloatUtils {

    /**
     * Maps a normalized float value (this) from the range [0.0f, 1.0f]
     * to a new range defined by min and max (both Floats).
     *
     * @param min The minimum value of the target range (Float).
     * @param max The maximum value of the target range (Float).
     * @return The mapped float value within [min, max].
     */
    fun Float.mapToRange(min: Float, max: Float): Float {
        // Calculate the size of the target range.
        val rangeSize = max - min

        // Scale the normalized value (this) by the range size,
        // then shift the result by adding the minimum value.
        return min + (this * rangeSize)
    }

    fun Float.fixFloatingPointError(tolerance: Float = 0.0001f): Float {
        if (this.isNaN()) return 0f
        val rounded = this.roundToInt()
        return if (kotlin.math.abs(this - rounded) < tolerance) {
            rounded.toFloat()
        } else {
            this
        }
    }
}