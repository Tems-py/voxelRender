package me.tems.coords


class Geometry(val from: Vec3, val to: Vec3, val faces: Map<FaceName, Face>, val textures: Map<String, String>, var rotation: Vec3) {
    enum class FaceName {
        NORTH, SOUTH, DOWN, UP, WEST, EAST,
    }

    override fun toString(): String {
        return "<Geometry $from, $to, ${faces.size} faces, $rotation rotation>"
    }

    data class Face(
        val uv: Pair<Vec2, Vec2>,
        var texture: String
    )

    fun checkIfInsideBlock(vec: Vec3): Boolean {
        var position = vec.mul(16f)

        if (rotation.x != 0f || rotation.y != 0f || rotation.z != 0f) {
            position = position.rotateAroundPivotReversed(rotation.mul(-1f), Vec3(8f, 8f, 8f)) // negative rotation for point + reverse rotation order
        }

        return position.x <= to.x && position.y <= to.y && position.z <= to.z && from.x <= position.x && from.y <= position.y && from.z <= position.z
    }

    fun clone(): Geometry {
        val clonedFaces = faces.mapValues { (_, face) -> face.copy() }
        return Geometry(from.plus(Vec3.ZERO), to.plus(Vec3.ZERO), clonedFaces, textures.toMap(), rotation.plus(Vec3.ZERO))
    }
}

