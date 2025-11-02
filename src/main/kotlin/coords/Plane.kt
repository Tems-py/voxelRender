package org.example.coords

class Plane(val pointOnPlane: Vec3,val normal: Vec3) {
    val a = normal.x
    val b = normal.y
    val c = normal.z
    val equals = (pointOnPlane.x*normal.x + pointOnPlane.y*normal.y + pointOnPlane.z*normal.z)/16f
    //a(x-x0) + b(y-y0) + c(z-z0) = 0
    //<x0,y0,z0> -> pointOnPlane
    //<a,b,c> -> normal
    //ax + by + cz = equals

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

    fun rotateAroundPivot(rotation: Vec3,pivot:Vec3):Plane{
        return Plane(pointOnPlane.rotateAroundPivot(rotation,pivot),normal.rotateAroundPivot(rotation,Vec3.ZERO))
    }
}