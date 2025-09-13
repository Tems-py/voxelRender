package org.example

import kotlin.math.abs
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


    fun plus(vec3: Vec3): Vec3 {
        return Vec3(x + vec3.x, y + vec3.y, z + vec3.z)
    }

    fun min(vec3: Vec3): Vec3 {
        return Vec3(x - vec3.x, y - vec3.y, z - vec3.z)
    }

    fun mul(vec3: Vec3): Vec3 {
        return Vec3(x * vec3.x, y * vec3.y, z * vec3.z)
    }

    fun mul(n: Float): Vec3 {
        return Vec3(x * n, y * n, z * n)
    }

    fun abs(): Vec3 {
        return Vec3(abs(x), abs(y), abs(z))
    }

    companion object {
        val ZERO = Vec3(0f, 0f, 0f)
    }
}