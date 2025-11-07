package org.example.coords

import kotlin.math.PI

class Plane(pointOnPlane: Vec3,normal: Vec3,rotation: Vec3) {
    val origin = pointOnPlane.rotateAroundPivot(rotation, Vec3(8f))
    val normalToPlane = normal.rotateAroundPivot(rotation, Vec3.ZERO)
    val a = normalToPlane.x
    val b = normalToPlane.y
    val c = normalToPlane.z
    val equals = (origin.x*normalToPlane.x + origin.y*normalToPlane.y + origin.z*normalToPlane.z)/16f
    //a(x-x0) + b(y-y0) + c(z-z0) = 0
    //<x0,y0,z0> -> pointOnPlane
    //<a,b,c> -> normal
    //ax + by + cz = equals
    val planeXDirection = Vec3(normal.z,normal.x,normal.y).rotateAroundPivot(rotation, Vec3.ZERO)
    val planeYDirection = Vec3(normal.y,normal.z,normal.x).rotateAroundPivot(rotation, Vec3.ZERO)

    override fun toString():String{
        return "$a x + $b y + $c z = $equals"
    }

    fun lineIntercept(line:Line):Vec3{
        // d * t = planeEquals
        // looking for t
        val d = a * line.direction.x + b * line.direction.y + c * line.direction.z
        val planeEquals = (-a * line.origin.x) + (-b * line.origin.y) + (-c * line.origin.z) + this.equals
        val t = planeEquals/d
        return line.direction.mul(t).plus(line.origin)
    }

    fun placePointOnPlane(point: Vec3): Vec2 {
        return Vec2(point.min(origin).dot(planeXDirection), point.min(origin).dot(planeYDirection))
    }
}