package org.example.coords


class Geometry(val from: Vec3Int, val to: Vec3Int, val faces: Map<FaceName, Face>) {
    enum class FaceName {
        NORTH, SOUTH, DOWN, UP, WEST, EAST,
    }

    val hitFaces =
        arrayOf(Vec3(1f, 0f, 0f), Vec3(-1f, 0f, 0f), Vec3(0f, 1f, 0f), Vec3(0f, -1f, 0f), Vec3(0f, 0f, 1f), Vec3(0f, 0f, -1f))

    data class Face(
        val uv: Pair<Vec2Int, Vec2Int>,
        val texture: String
    )

    //{   "from": [ 0, 0, 0 ],
//    "to": [ 16, 8, 16 ],
//    "faces": {
//    "down":  { "uv": [ 0, 0, 16, 16 ], "texture": "#bottom", "cullface": "down" },
//    "up":    { "uv": [ 0, 0, 16, 16 ], "texture": "#top" },
//    "north": { "uv": [ 0, 8, 16, 16 ], "texture": "#side", "cullface": "north" },
//    "south": { "uv": [ 0, 8, 16, 16 ], "texture": "#side", "cullface": "south" },
//    "west":  { "uv": [ 0, 8, 16, 16 ], "texture": "#side", "cullface": "west" },
//    "east":  { "uv": [ 0, 8, 16, 16 ], "texture": "#side", "cullface": "east" }
//}
//}

    fun checkIfInsideBlock(vec: Vec3): Boolean {
        val position = vec.mul(16f).round()

        return position.x <= to.x && position.y <= to.y && position.z <= to.z && from.x <= position.x && from.y <= position.y && from.z <= position.z
    }
}