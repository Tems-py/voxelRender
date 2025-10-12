package org.example.coords

import org.example.fixFloatingPointError
import java.awt.Color
import kotlin.math.*
import kotlin.random.Random

class Vec3(val x: Float, val y: Float, val z: Float) {
    companion object {
        fun random(): Vec3 {
            return Vec3(Random.nextFloat() * 2 - 1, Random.nextFloat() * 2 - 1, Random.nextFloat() * 2 - 1)
        }

        val ZERO = Vec3(0f, 0f, 0f)
        val ONE = Vec3(1f, 1f, 1f)

        val randomUnitVectors = Array(10_000) {
            val z = Random.nextFloat() * 2f - 1f
            val a = Random.nextFloat() * (2f * PI.toFloat())
            val r = sqrt(1f - z * z)
            Vec3(r * cos(a), r * sin(a), z)
        }
    }

    constructor(value: Float) : this(value, value, value)

    fun normalize(): Vec3 {
        val length = length()
        return Vec3(x / length, y / length, z / length)
    }

    fun addToNonZero(value: Float): Vec3 {
        return Vec3(
            if (x != 0f) x + value else 0f,
            if (y != 0f) y + value else 0f,
            if (z != 0f) z + value else 0f,
        )
    }

    fun length(): Float {
        return sqrt(lengthSquared())
    }

    fun lengthSquared(): Float {
        return x * x + y * y + z * z
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

    fun dot(other: Vec3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun randomOutwardVector(): Vec3 {
        var v = randomUnitVectors[Random.nextInt(randomUnitVectors.size)]
        if (v.dot(this) < 0f) v = v.reverse()
        return v
    }

    private fun reverse(): Vec3 {
        return Vec3(-x, -y, -z)
    }

    fun sign(): Vec3 {
        return Vec3(
            if (x < 0) -1f else if (x > 0) 1f else 0f,
            if (y < 0) -1f else if (y > 0) 1f else 0f,
            if (z < 0) -1f else if (z > 0) 1f else 0f
        )
    }

    fun reflect(normal: Vec3): Vec3 {
//        val n = normal.normalize()
//        return this.min(n.mul((2.0f * (this.dot(n))))) // WERSJA JAŚKA GÓRĄ

        if (normal.x != 0f) {
            return Vec3(-this.x, this.y, this.z)
        }
        if (normal.y != 0f) {
            return Vec3(this.x, -this.y, this.z)
        }
        if (normal.z != 0f) {
            return Vec3(this.x, this.y, -this.z)
        }
        throw Exception();
    }

    fun cross(vec: Vec3) = Vec3(
        y * vec.z - z * vec.y,
        z * vec.x - x * vec.z,
        x * vec.y - y * vec.x
    )

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

    fun rotateAroundPivot(angles: Vec3, pivot: Vec3): Vec3 {
        val radX = angles.x
        val radY = angles.y
        val radZ = angles.z


        // 2. Translate the point so the pivot becomes the origin (0, 0, 0)
        // P' = P - A
        var pPrime = this.min(pivot)

        // Use Doubles for intermediate calculation precision
        var x = pPrime.x.toDouble()
        var y = pPrime.y.toDouble()
        var z = pPrime.z.toDouble()

        var tempY: Double
        var tempZ: Double
        var tempX: Double

        // 3. Apply Rotations Sequentially (X -> Y -> Z order)

        // --- 3a. Rotate around the X-axis (Roll) ---
        // x remains, y and z transform
        tempY = y
        tempZ = z
        y = tempY * cos(radX) - tempZ * sin(radX)
        z = tempY * sin(radX) + tempZ * cos(radX)

        // --- 3b. Rotate around the Y-axis (Pitch) ---
        // y remains, x and z transform
        tempX = x
        tempZ = z
        x = tempX * cos(radY) + tempZ * sin(radY)
        z = -tempX * sin(radY) + tempZ * cos(radY)

        // --- 3c. Rotate around the Z-axis (Yaw) ---
        // z remains, x and y transform
        tempX = x
        tempY = y
        x = tempX * cos(radZ) - tempY * sin(radZ)
        y = tempX * sin(radZ) + tempY * cos(radZ)


        // 4. Translate the rotated point back to the original pivot position
        // P_final = P_rot + A
        val rotatedPoint = Vec3(
            x.toFloat().fixFloatingPointError(),
            y.toFloat().fixFloatingPointError(),
            z.toFloat().fixFloatingPointError()
        )
        return rotatedPoint.plus(pivot)
    }

    fun toColor(): Color {
        val vec = normalize().abs()
        return Color(vec.x, vec.y, vec.z)
    }

    fun fixFloatingPointError(): Vec3 {
        return Vec3(
            x.fixFloatingPointError(),
            y.fixFloatingPointError(),
            z.fixFloatingPointError()
        )
    }
}