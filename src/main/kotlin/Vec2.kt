package org.example

import kotlin.math.sqrt

class Vec2(val x: Float, val y: Float) {
    override fun toString(): String {
        return "<Vec2 $x, $y>"
    }

    fun normalize(): Vec2 {
        val length = length()
        return Vec2(x / length, y / length)
    }

    fun length(): Float {
        return sqrt(lengthSquared())
    }

    fun lengthSquared(): Float {
        return x*x + y*y
    }

    fun abs(): Vec2 {
        return Vec2(kotlin.math.abs(x), kotlin.math.abs(y))
    }
}