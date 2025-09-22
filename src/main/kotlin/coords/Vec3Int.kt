package org.example.coords

class Vec3Int(val x: Int, val y: Int, val z: Int) {
    companion object {
        val ZERO = Vec3Int(0, 0, 0)
        val ONE = Vec3Int(1, 1, 1)
    }

    fun plus(vec3: Vec3Int): Vec3Int {
        return Vec3Int(x + vec3.x, y + vec3.y, z + vec3.z)
    }

    fun min(vec3: Vec3Int): Vec3Int {
        return Vec3Int(x - vec3.x, y - vec3.y, z - vec3.z)
    }

    fun mul(vec3: Vec3Int): Vec3Int {
        return Vec3Int(x * vec3.x, y * vec3.y, z * vec3.z)
    }

    fun mul(n: Int): Vec3Int {
        return Vec3Int(x * n, y * n, z * n)
    }
}