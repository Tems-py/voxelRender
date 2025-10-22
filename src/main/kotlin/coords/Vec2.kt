package org.example.coords

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Vec2(val x: Float, val y: Float) {
    override fun toString(): String {
        return "<Vec2 $x, $y>"
    }

    fun min(vec: Vec2): Vec2 {
        return Vec2(x - vec.x, y - vec.y)
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

    fun rotate(rotation:Float) : Vec2{
        return Vec2(this.x*cos(rotation)-this.y*sin(rotation),this.y*cos(rotation)+this.x*sin(rotation))
    }

    fun placeOnPlane(normal: Vec3): Vec3 {
        val n = normal.normalize()
//        println(this)

        val ref = if (abs(n.x) < 0.9) Vec3(1.0f, 0.0f, 0.0f) else Vec3(0.0f, 1.0f, 0.0f)

        val u = n.cross(ref).normalize()
        val v = n.cross(u)

        return Vec3(
            u.x * x + v.x * y,
            u.y * x + v.y * y,
            u.z * x + v.z * y
        )
    }
}