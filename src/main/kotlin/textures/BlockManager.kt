package org.example.textures

import org.example.coords.*
import org.example.coords.Geometry.FaceName.*

class BlockManager {
    companion object {
        private val blockCache = mutableMapOf<String, Block>()

        fun getBlock(name: String): Block = blockCache.getOrPut(name) {
            val block = Block(name)

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

            if (name == "oak_slab") {
                block.geometries = listOf(
                    Geometry(
                        Vec3Int.ZERO,
                        Vec3Int(16, 1, 16),
                        mapOf(
                            Pair(DOWN, Geometry.Face(Pair(Vec2Int(0,0), Vec2Int(16,16)), "oak_planks")),
                            Pair(UP, Geometry.Face(Pair(Vec2Int(0,0), Vec2Int(16,16)), "oak_planks")),
                            Pair(NORTH, Geometry.Face(Pair(Vec2Int(0,8), Vec2Int(16,16)), "oak_planks")),
                            Pair(SOUTH, Geometry.Face(Pair(Vec2Int(0,8), Vec2Int(16,16)), "oak_planks")),
                            Pair(WEST, Geometry.Face(Pair(Vec2Int(0,8), Vec2Int(16,16)), "oak_planks")),
                            Pair(EAST, Geometry.Face(Pair(Vec2Int(0,8), Vec2Int(16,16)), "oak_planks")),
                        )
                    )
                )

                block.isFull = false
            }

            blockCache[name] = block
            return block
        }
    }
}