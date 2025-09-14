package org.example.coords

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Vec3(val x: Float, val y: Float, val z: Float) {
    companion object {
        val ZERO = Vec3(0f, 0f, 0f)
    }

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

    fun rotate(angles: Vec3): Vec3 {
        // Angles in radians
        val pitch = angles.x
        val yaw = angles.y
        val roll = angles.z

        // Rotation matrices components
        val cp = cos(pitch)
        val sp = sin(pitch)
        val cy = cos(yaw)
        val sy = sin(yaw)
        val cr = cos(roll)
        val sr = sin(roll)

        // Apply rotation (roll → pitch → yaw)
        val newX = x * (cy * cp) + y * (cy * sp * sr - sy * cr) + z * (cy * sp * cr + sy * sr)
        val newY = x * (sy * cp) + y * (sy * sp * sr + cy * cr) + z * (sy * sp * cr - cy * sr)
        val newZ = x * (-sp) + y * (cp * sr) + z * (cp * cr)

        return Vec3(newX, newY, newZ)
    }
}