package me.tems.coords

import me.tems.utils.FloatUtils.fixFloatingPointError
import java.awt.Color
import kotlin.math.*
import kotlin.random.Random

class Vec3(val data: FloatArray) {

    constructor(x: Float, y: Float, z: Float) : this(floatArrayOf(x, y, z))
    constructor(value: Float) : this(floatArrayOf(value, value, value))

    val x: Float get() = data[0]
    val y: Float get() = data[1]
    val z: Float get() = data[2]

    operator fun get(index: Int): Float = data[index]

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

    fun angleBetween(b: Vec3): Float {
        val dot = this.dot(b)
        val lenA = this.length()
        val lenB = b.length()
        if (lenA == 0.0f || lenB == 0.0f) return 0.0f
        val cosTheta = (dot / (lenA * lenB)).coerceIn(-1.0f, 1.0f)
        return acos(cosTheta)
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
        if (normal.x != 0f) {
            return Vec3(-this.x, this.y, this.z)
        }
        if (normal.y != 0f) {
            return Vec3(this.x, -this.y, this.z)
        }
        if (normal.z != 0f) {
            return Vec3(this.x, this.y, -this.z)
        }
        throw Exception()
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
        val pitch = angles.x
        val yaw = angles.y
        val roll = angles.z

        val cp = cos(pitch)
        val sp = sin(pitch)
        val cy = cos(yaw)
        val sy = sin(yaw)
        val cr = cos(roll)
        val sr = sin(roll)

        val newX = x * (cy * cp) + y * (cy * sp * sr - sy * cr) + z * (cy * sp * cr + sy * sr)
        val newY = x * (sy * cp) + y * (sy * sp * sr + cy * cr) + z * (sy * sp * cr - cy * sr)
        val newZ = x * (-sp) + y * (cp * sr) + z * (cp * cr)

        return Vec3(newX, newY, newZ)
    }

    fun rotateAroundPivotReversed(angles: Vec3, pivot: Vec3): Vec3 {
        val radX = angles.x
        val radY = angles.y
        val radZ = angles.z

        val pPrime = this.min(pivot)

        var px = pPrime.x.toDouble()
        var py = pPrime.y.toDouble()
        var pz = pPrime.z.toDouble()

        var tempY: Double
        var tempZ: Double
        var tempX: Double

        // Z-axis
        tempX = px
        tempY = py
        px = tempX * cos(radZ) - tempY * sin(radZ)
        py = tempX * sin(radZ) + tempY * cos(radZ)

        // Y-axis
        tempX = px
        tempZ = pz
        px = tempX * cos(radY) + tempZ * sin(radY)
        pz = -tempX * sin(radY) + tempZ * cos(radY)

        // X-axis
        tempY = py
        tempZ = pz
        py = tempY * cos(radX) - tempZ * sin(radX)
        pz = tempY * sin(radX) + tempZ * cos(radX)

        val rotatedPoint = Vec3(
            px.toFloat().fixFloatingPointError(),
            py.toFloat().fixFloatingPointError(),
            pz.toFloat().fixFloatingPointError()
        )
        return rotatedPoint.plus(pivot)
    }

    fun rotateAroundPivot(angles: Vec3, pivot: Vec3): Vec3 {
        val radX = angles.x
        val radY = angles.y
        val radZ = angles.z

        val pPrime = this.min(pivot)

        var px = pPrime.x.toDouble()
        var py = pPrime.y.toDouble()
        var pz = pPrime.z.toDouble()

        var tempY: Double
        var tempZ: Double
        var tempX: Double

        // X-axis
        tempY = py
        tempZ = pz
        py = tempY * cos(radX) - tempZ * sin(radX)
        pz = tempY * sin(radX) + tempZ * cos(radX)

        // Y-axis
        tempX = px
        tempZ = pz
        px = tempX * cos(radY) + tempZ * sin(radY)
        pz = -tempX * sin(radY) + tempZ * cos(radY)

        // Z-axis
        tempX = px
        tempY = py
        px = tempX * cos(radZ) - tempY * sin(radZ)
        py = tempX * sin(radZ) + tempY * cos(radZ)

        val rotatedPoint = Vec3(
            px.toFloat().fixFloatingPointError(),
            py.toFloat().fixFloatingPointError(),
            pz.toFloat().fixFloatingPointError()
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
