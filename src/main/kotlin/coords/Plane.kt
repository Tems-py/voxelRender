package org.example.coords

class Plane(val pointOnPlane: Vec3,val normal: Vec3) {
    val x = normal.x
    val y = normal.y
    val z = normal.z
    val equals = pointOnPlane.x*normal.x + pointOnPlane.y*normal.y + pointOnPlane.z*normal.z
    //ax + by + cz = equals

    fun lineIntercept(line:Line):Vec3{
        // d * t = lineEquals
        // looking for t
        val d = x * line.direction.x + y * line.direction.y + z * line.direction.z
        val lineEquals = -x * line.origin.x + -y * line.origin.y + -z * line.origin.z + this.equals
        val t = lineEquals/d
        return line.direction.mul(t).plus(line.origin)
    }

    fun rotateAroundPivot(rotation: Vec3,pivot:Vec3):Plane{
        return Plane(pointOnPlane.rotateAroundPivot(rotation,pivot),normal.rotateAroundPivot(rotation,pivot))
    }
}