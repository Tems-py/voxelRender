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



            if (name == "anvil") {
                block.geometries = listOf(
                    // Anvil base
                    Geometry(
                        Vec3Int(2, 0, 2),
                        Vec3Int(14, 4, 14),
                        mapOf(
                            DOWN  to Geometry.Face(Pair(Vec2Int(2, 2), Vec2Int(14, 14)), "body"),
                            UP    to Geometry.Face(Pair(Vec2Int(2, 2), Vec2Int(14, 14)), "body"),
                            NORTH to Geometry.Face(Pair(Vec2Int(2,12), Vec2Int(14,16)), "body"),
                            SOUTH to Geometry.Face(Pair(Vec2Int(2,12), Vec2Int(14,16)), "body"),
                            WEST  to Geometry.Face(Pair(Vec2Int(0, 2), Vec2Int(4,14)),  "body"),
                            EAST  to Geometry.Face(Pair(Vec2Int(4, 2), Vec2Int(0,14)),  "body")
                        )
                    ),
                    // Lower narrow portion
                    Geometry(
                        Vec3Int(4, 4, 3),
                        Vec3Int(12, 5, 13),
                        mapOf(
                            UP    to Geometry.Face(Pair(Vec2Int(4, 3), Vec2Int(12,13)), "body"),
                            NORTH to Geometry.Face(Pair(Vec2Int(4,11), Vec2Int(12,12)), "body"),
                            SOUTH to Geometry.Face(Pair(Vec2Int(4,11), Vec2Int(12,12)), "body"),
                            WEST  to Geometry.Face(Pair(Vec2Int(4, 3), Vec2Int(5,13)),  "body"),
                            EAST  to Geometry.Face(Pair(Vec2Int(5, 3), Vec2Int(4,13)),  "body")
                        )
                    ),
                    // Wider section beneath top portion
                    Geometry(
                        Vec3Int(6, 5, 4),
                        Vec3Int(10,10,12),
                        mapOf(
                            NORTH to Geometry.Face(Pair(Vec2Int(6, 6), Vec2Int(10,11)), "body"),
                            SOUTH to Geometry.Face(Pair(Vec2Int(6, 6), Vec2Int(10,11)), "body"),
                            WEST  to Geometry.Face(Pair(Vec2Int(5, 4), Vec2Int(10,12)), "body"),
                            EAST  to Geometry.Face(Pair(Vec2Int(10,4), Vec2Int(5,12)),  "body")
                        )
                    ),
                    // Anvil top
                    Geometry(
                        Vec3Int(3,10,0),
                        Vec3Int(13,16,16),
                        mapOf(
                            DOWN  to Geometry.Face(Pair(Vec2Int(3, 0), Vec2Int(13,16)), "body"),
                            UP    to Geometry.Face(Pair(Vec2Int(3, 0), Vec2Int(13,16)), "top"),
                            NORTH to Geometry.Face(Pair(Vec2Int(3, 0), Vec2Int(13, 6)), "body"),
                            SOUTH to Geometry.Face(Pair(Vec2Int(3, 0), Vec2Int(13, 6)), "body"),
                            WEST  to Geometry.Face(Pair(Vec2Int(10,0), Vec2Int(16,16)), "body"),
                            EAST  to Geometry.Face(Pair(Vec2Int(16,0), Vec2Int(10,16)), "body")
                        )
                    )
                )

                block.isFull = false
            }

            if (name == "oak_slab") {
                block.geometries = listOf(
                    Geometry(
                        Vec3Int.ZERO,
                        Vec3Int(16, 8, 16),
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

//            "elements": [
//            {   "from": [ 0, 0, 0 ],
//                "to": [ 16, 8, 16 ],
//                "faces": {
//                    "down":  { "uv": [ 0, 0, 16, 16 ], "texture": "#bottom", "cullface": "down" },
//                    "up":    { "uv": [ 0, 0, 16, 16 ], "texture": "#top" },
//                    "north": { "uv": [ 0, 8, 16, 16 ], "texture": "#side", "cullface": "north" },
//                    "south": { "uv": [ 0, 8, 16, 16 ], "texture": "#side", "cullface": "south" },
//                    "west":  { "uv": [ 0, 8, 16, 16 ], "texture": "#side", "cullface": "west" },
//                    "east":  { "uv": [ 0, 8, 16, 16 ], "texture": "#side", "cullface": "east" }
//                }
//            },
//            {   "from": [ 8, 8, 0 ],
//                "to": [ 16, 16, 16 ],
//                "faces": {
//                    "up":    { "uv": [ 8, 0, 16, 16 ], "texture": "#top", "cullface": "up" },
//                    "north": { "uv": [ 0, 0,  8,  8 ], "texture": "#side", "cullface": "north" },
//                    "south": { "uv": [ 8, 0, 16,  8 ], "texture": "#side", "cullface": "south" },
//                    "west":  { "uv": [ 0, 0, 16,  8 ], "texture": "#side" },
//                    "east":  { "uv": [ 0, 0, 16,  8 ], "texture": "#side", "cullface": "east" }
//                }
//            }
//            ]
            // slab + 1/4



            if (name == "oak_stairs") {
                block.geometries = listOf(
                    Geometry(
                        Vec3Int.ZERO,
                        Vec3Int(16, 8, 16),
                        mapOf(
                            Pair(DOWN, Geometry.Face(Pair(Vec2Int(0,0), Vec2Int(16,16)), "oak_planks")),
                            Pair(UP, Geometry.Face(Pair(Vec2Int(0,0), Vec2Int(16,16)), "oak_planks")),
                            Pair(NORTH, Geometry.Face(Pair(Vec2Int(0,8), Vec2Int(16,16)), "oak_planks")),
                            Pair(SOUTH, Geometry.Face(Pair(Vec2Int(0,8), Vec2Int(16,16)), "oak_planks")),
                            Pair(WEST, Geometry.Face(Pair(Vec2Int(0,8), Vec2Int(16,16)), "oak_planks")),
                            Pair(EAST, Geometry.Face(Pair(Vec2Int(0,8), Vec2Int(16,16)), "oak_planks")),
                        )
                    ),
                    Geometry(
                        Vec3Int(8,8,0),
                        Vec3Int(16, 16, 16),
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