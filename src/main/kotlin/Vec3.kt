package org.example

import kotlin.math.sqrt

class Vec3(val x: Float, val y: Float, val z: Float) {
    fun normalize(): Vec3 {
        val length = length()
        return Vec3(x / length, y / length, z / length)
    }

    fun length(): Float {
        return sqrt(lengthSquared())
    }

    fun lengthSquared(): Float {
        return x*x + y*y + z*z
    }

    override fun toString(): String {
        return "<Vec3 $x, $y, $z>"
    }

    companion object {
        val ZERO = Vec3(0f, 0f, 0f)
    }
}