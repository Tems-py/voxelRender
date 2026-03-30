package me.tems.coords

import me.tems.utils.FloatUtils.fixFloatingPointError
import java.awt.Color
import kotlin.math.*
import kotlin.random.Random

// Vec3 is now a plain FloatArray(3) — no wrapper class.
// This object acts as the companion: factory, constants, statics.
object Vec3 {
    operator fun invoke(x: Float, y: Float, z: Float): FloatArray = floatArrayOf(x, y, z)
    operator fun invoke(v: Float): FloatArray = floatArrayOf(v, v, v)

    fun random(): FloatArray =
        floatArrayOf(Random.nextFloat() * 2 - 1, Random.nextFloat() * 2 - 1, Random.nextFloat() * 2 - 1)

    val ZERO: FloatArray = floatArrayOf(0f, 0f, 0f)
    val ONE: FloatArray = floatArrayOf(1f, 1f, 1f)

    val randomUnitVectors: Array<FloatArray> = Array(10_000) {
        val z = Random.nextFloat() * 2f - 1f
        val a = Random.nextFloat() * (2f * PI.toFloat())
        val r = sqrt(1f - z * z)
        floatArrayOf(r * cos(a), r * sin(a), z)
    }
}

// ── indexed access helpers ────────────────────────────────────────────────────
inline val FloatArray.x: Float get() = this[0]
inline val FloatArray.y: Float get() = this[1]
inline val FloatArray.z: Float get() = this[2]

// ── arithmetic ────────────────────────────────────────────────────────────────
fun FloatArray.add(b: FloatArray): FloatArray = floatArrayOf(x + b.x, y + b.y, z + b.z)
fun FloatArray.sub(b: FloatArray): FloatArray = floatArrayOf(x - b.x, y - b.y, z - b.z)
fun FloatArray.mul(b: FloatArray): FloatArray = floatArrayOf(x * b.x, y * b.y, z * b.z)
fun FloatArray.mul(n: Float): FloatArray = floatArrayOf(x * n, y * n, z * n)

// ── geometry ──────────────────────────────────────────────────────────────────
fun FloatArray.dot(b: FloatArray): Float = x * b.x + y * b.y + z * b.z

fun FloatArray.cross(b: FloatArray): FloatArray = floatArrayOf(
    y * b.z - z * b.y,
    z * b.x - x * b.z,
    x * b.y - y * b.x
)

fun FloatArray.lengthSquared(): Float = x * x + y * y + z * z
fun FloatArray.length(): Float = sqrt(lengthSquared())

fun FloatArray.normalize(): FloatArray {
    val len = length()
    return floatArrayOf(x / len, y / len, z / len)
}

fun FloatArray.abs(): FloatArray = floatArrayOf(abs(x), abs(y), abs(z))

fun FloatArray.sign(): FloatArray = floatArrayOf(
    if (x < 0) -1f else if (x > 0) 1f else 0f,
    if (y < 0) -1f else if (y > 0) 1f else 0f,
    if (z < 0) -1f else if (z > 0) 1f else 0f
)

fun FloatArray.reflect(normal: FloatArray): FloatArray {
    if (normal.x != 0f) return floatArrayOf(-x, y, z)
    if (normal.y != 0f) return floatArrayOf(x, -y, z)
    if (normal.z != 0f) return floatArrayOf(x, y, -z)
    throw Exception()
}

fun FloatArray.angleBetween(b: FloatArray): Float {
    val lenA = length()
    val lenB = b.length()
    if (lenA == 0f || lenB == 0f) return 0f
    return acos((dot(b) / (lenA * lenB)).coerceIn(-1f, 1f))
}

fun FloatArray.addToNonZero(value: Float): FloatArray = floatArrayOf(
    if (x != 0f) x + value else 0f,
    if (y != 0f) y + value else 0f,
    if (z != 0f) z + value else 0f
)

fun FloatArray.randomOutwardVector(): FloatArray {
    var v = Vec3.randomUnitVectors[Random.nextInt(Vec3.randomUnitVectors.size)]
    if (v.dot(this) < 0f) v = floatArrayOf(-v[0], -v[1], -v[2])
    return v
}

// ── rotation ──────────────────────────────────────────────────────────────────
fun FloatArray.rotate(angles: FloatArray): FloatArray {
    val cp = cos(angles.x); val sp = sin(angles.x)
    val cy = cos(angles.y); val sy = sin(angles.y)
    val cr = cos(angles.z); val sr = sin(angles.z)
    return floatArrayOf(
        x * (cy * cp) + y * (cy * sp * sr - sy * cr) + z * (cy * sp * cr + sy * sr),
        x * (sy * cp) + y * (sy * sp * sr + cy * cr) + z * (sy * sp * cr - cy * sr),
        x * (-sp)     + y * (cp * sr)                + z * (cp * cr)
    )
}

fun FloatArray.rotateAroundPivotReversed(angles: FloatArray, pivot: FloatArray): FloatArray {
    val radX = angles.x; val radY = angles.y; val radZ = angles.z
    val pPrime = sub(pivot)
    var px = pPrime.x.toDouble(); var py = pPrime.y.toDouble(); var pz = pPrime.z.toDouble()
    var tmp: Double

    tmp = px; px = tmp * cos(radZ) - py * sin(radZ); py = tmp * sin(radZ) + py * cos(radZ)
    tmp = px; px = tmp * cos(radY) + pz * sin(radY); pz = -tmp * sin(radY) + pz * cos(radY)
    tmp = py; py = tmp * cos(radX) - pz * sin(radX); pz = tmp * sin(radX) + pz * cos(radX)

    return floatArrayOf(
        px.toFloat().fixFloatingPointError() + pivot.x,
        py.toFloat().fixFloatingPointError() + pivot.y,
        pz.toFloat().fixFloatingPointError() + pivot.z
    )
}

fun FloatArray.rotateAroundPivot(angles: FloatArray, pivot: FloatArray): FloatArray {
    val radX = angles.x; val radY = angles.y; val radZ = angles.z
    val pPrime = sub(pivot)
    var px = pPrime.x.toDouble(); var py = pPrime.y.toDouble(); var pz = pPrime.z.toDouble()
    var tmp: Double

    tmp = py; py = tmp * cos(radX) - pz * sin(radX); pz = tmp * sin(radX) + pz * cos(radX)
    tmp = px; px = tmp * cos(radY) + pz * sin(radY); pz = -tmp * sin(radY) + pz * cos(radY)
    tmp = px; px = tmp * cos(radZ) - py * sin(radZ); py = tmp * sin(radZ) + py * cos(radZ)

    return floatArrayOf(
        px.toFloat().fixFloatingPointError() + pivot.x,
        py.toFloat().fixFloatingPointError() + pivot.y,
        pz.toFloat().fixFloatingPointError() + pivot.z
    )
}

// ── misc ──────────────────────────────────────────────────────────────────────
fun FloatArray.toColor(): Color {
    val v = normalize().abs()
    return Color(v.x, v.y, v.z)
}

fun FloatArray.fixFloatingPointError(): FloatArray = floatArrayOf(
    x.fixFloatingPointError(),
    y.fixFloatingPointError(),
    z.fixFloatingPointError()
)
