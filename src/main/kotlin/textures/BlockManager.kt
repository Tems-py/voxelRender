package org.example.textures

import kotlinx.serialization.json.Json
import org.example.coords.Block
import org.example.coords.Geometry
import org.example.coords.Geometry.FaceName.*
import org.example.coords.Vec2Int
import org.example.coords.Vec3Int
import textures.MinecraftModel
import java.io.File

class BlockManager {
    companion object {
        private val blockCache = mutableMapOf<String, Block>()

        fun getBlock(name: String): Block = blockCache.getOrPut(name) {
            val block = Block(name)

            val geometries = loadGeometry(name)

            if (geometries.isNotEmpty()) {
                block.geometries = geometries
                block.isFull = false
            }

            blockCache[name] = block
            return block
        }

        fun loadGeometry(name: String): List<Geometry> {
            val file = File("assets/minecraft/models/block/${name}.json")
            if (!file.isFile) return listOf()
            if (name == "glow_lichen" || name == "lever" || name == "ladder" || name == "tripwire_hook") return listOf()
            val geometries = mutableListOf<Geometry>()


            val json = Json { ignoreUnknownKeys = true }.decodeFromString<MinecraftModel>(file.readText())
            if (json.parent != null) { // tinted_cross - trawa, kwiatki itp
                val parent = json.parent.replace("minecraft:block/", "").replace("block/", "")
                if (parent != "block" && parent != "tinted_cross" && parent != "cube_all" && parent != "template_torch_wall" && parent != "cross" && parent != "glow_lichen") {
                    geometries.addAll(loadGeometry(parent))
                }
            }

            json.elements?.forEach {
                val geo = Geometry(
                    Vec3Int(it.from[0], it.from[1], it.from[2]),
                    Vec3Int(it.to[0], it.to[1], it.to[2]),
                    mapOf(
                        DOWN to Geometry.Face(
                            Pair(
                                Vec2Int(it.faces?.down?.uv?.get(0) ?: 0, it.faces?.down?.uv?.get(1) ?: 0), Vec2Int(
                                    it.faces?.down?.uv?.get(0) ?: 16, it.faces?.down?.uv?.get(1) ?: 16
                                )
                            ), it.faces?.down?.texture ?: "stone"
                        ),
                        UP to Geometry.Face(
                            Pair(
                                Vec2Int(it.faces?.up?.uv?.get(0) ?: 0, it.faces?.up?.uv?.get(1) ?: 0), Vec2Int(
                                    it.faces?.up?.uv?.get(0) ?: 16, it.faces?.up?.uv?.get(1) ?: 16
                                )
                            ), it.faces?.up?.texture ?: "stone"
                        ),
                        WEST to Geometry.Face(
                            Pair(
                                Vec2Int(it.faces?.west?.uv?.get(0) ?: 0, it.faces?.west?.uv?.get(1) ?: 0), Vec2Int(
                                    it.faces?.west?.uv?.get(0) ?: 16, it.faces?.west?.uv?.get(1) ?: 16
                                )
                            ), it.faces?.west?.texture ?: "stone"
                        ),
                        EAST to Geometry.Face(
                            Pair(
                                Vec2Int(it.faces?.east?.uv?.get(0) ?: 0, it.faces?.east?.uv?.get(1) ?: 0), Vec2Int(
                                    it.faces?.east?.uv?.get(0) ?: 16, it.faces?.east?.uv?.get(1) ?: 16
                                )
                            ), it.faces?.east?.texture ?: "stone"
                        ),
                        NORTH to Geometry.Face(
                            Pair(
                                Vec2Int(it.faces?.north?.uv?.get(0) ?: 0, it.faces?.north?.uv?.get(1) ?: 0), Vec2Int(
                                    it.faces?.north?.uv?.get(0) ?: 16, it.faces?.north?.uv?.get(1) ?: 16
                                )
                            ), it.faces?.north?.texture ?: "stone"
                        ),
                        SOUTH to Geometry.Face(
                            Pair(
                                Vec2Int(it.faces?.south?.uv?.get(0) ?: 0, it.faces?.south?.uv?.get(1) ?: 0), Vec2Int(
                                    it.faces?.south?.uv?.get(0) ?: 16, it.faces?.south?.uv?.get(1) ?: 16
                                )
                            ), it.faces?.south?.texture ?: "stone"
                        ),
                    )
                )

                geometries.add(geo)
            }

            val textures = json.textures

            if (textures != null) {
                geometries.forEach {
                    it.faces.forEach forEach2@{ (t, u) ->
                        val newTexture = textures[u.texture.replace("#", "")]?.replace("minecraft:block/", "") ?: return@forEach2

                        u.texture = newTexture
                    }
                }
            }

            if (geometries.isNotEmpty()) {
                TexturesManager.getTexture(geometries.first().faces.toList().first().second.texture)
            }

            return geometries
        }


    }
}