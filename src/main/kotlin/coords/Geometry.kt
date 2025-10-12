package org.example.coords

import org.example.utils.ColorUtils.sortVec3sByMagnitude
import kotlin.math.abs


class Geometry(val from: Vec3, val to: Vec3, val faces: Map<FaceName, Face>, val textures: Map<String, String>, val rotation: Vec3) {
    enum class FaceName {
        NORTH, SOUTH, DOWN, UP, WEST, EAST,
    }

    init {
        println(this)
        println(from.rotateAroundPivot(rotation, Vec3(8f, 8f, 8f)))
        println(to.rotateAroundPivot(rotation, Vec3(8f, 8f, 8f)))
    }

    override fun toString(): String {
        return "<Geometry $from, $to, ${faces.size} faces>"
    }

    data class Face(
        val uv: Pair<Vec2, Vec2>,
        var texture: String
    )

    fun checkIfInsideBlock(vec: Vec3): Boolean {
        var position = vec.mul(16f)

        if (rotation.x != 0f || rotation.y != 0f || rotation.z != 0f) {
            position = position.rotateAroundPivot(rotation, Vec3(8f, 8f, 8f))
        }

//        println("${vec.mul(16f)} $position")
//        println("$vec $position")

        return position.x <= to.x && position.y <= to.y && position.z <= to.z && from.x <= position.x && from.y <= position.y && from.z <= position.z
    }

    data class ClosestWallResult(val face: FaceName, val distance: Float)

    fun findClosestWall(pos: Vec3): ClosestWallResult {
        // Ensure 'from' is truly the min corner and 'to' is the max corner for safety,
        // though typically box definition ensures this. We'll use Math.min/max later.

        // Initialize with a default value (first wall)
        var minDistance = Float.MAX_VALUE
        var closestWall = FaceName.NORTH

        // Helper list of all walls and their corresponding coordinate values
        val wallsToCheck = listOf(
            Pair(FaceName.NORTH, from.x),
            Pair(FaceName.SOUTH, to.x),
            Pair(FaceName.DOWN, from.y),
            Pair(FaceName.UP, to.y),
            Pair(FaceName.WEST, from.z),
            Pair(FaceName.EAST, to.z)
        )

        // Iterate through all six walls
        for ((wall, wallCoordinate) in wallsToCheck) {
            val currentDistance: Float = when (wall) {
                // Distance in X dimension
                FaceName.NORTH, FaceName.SOUTH -> abs((pos.x * 16) - wallCoordinate)
                // Distance in Y dimension
                FaceName.DOWN, FaceName.UP -> abs((pos.y * 16) - wallCoordinate)
                // Distance in Z dimension
                FaceName.WEST, FaceName.EAST -> abs((pos.z * 16) - wallCoordinate)
            }

            if (currentDistance < minDistance) {
                minDistance = currentDistance
                closestWall = wall
            }
        }

        return ClosestWallResult(closestWall, minDistance)
    }
}