package org.example.textures

import kotlinx.serialization.json.Json
import org.example.coords.*
import org.example.coords.Geometry.FaceName.*
import org.example.textures.TexturesManager.Companion.getTexture
import org.example.textures.TexturesManager.Companion.preloadTextures
import textures.MinecraftModel
import java.io.File
import kotlin.math.PI

class BlockManager {
    companion object {
        private val blockCache = mutableMapOf<String, Block>()

        fun getBlock(name: String): Block = blockCache.getOrPut(name) {
            val block = Block(name)

            val geometries = loadGeometry(name)

            if (geometries.isNotEmpty()) { // better non-full block detection
                block.geometries = geometries
                block.isFull = false
            }

//            if (name == "stone_bricks") block.reflective = 0f


            if (name == "glowstone") block.illumination = 3f
            if (name == "sea_lantern") block.illumination = 3f
            if (name == "dragon_egg") block.illumination = 3f

            blockCache[name] = block
            return block
        }

        fun loadGeometry(name: String): List<Geometry> {
            val file = File("assets/minecraft/models/block/${name}.json")
            if (!file.isFile) {
                println("No model: $name")
                return listOf()
            }
            val geometries = mutableListOf<Geometry>()


            val json = Json { ignoreUnknownKeys = true }.decodeFromString<MinecraftModel>(file.readText())
            if (json.parent != null) { // tinted_cross - trawa, kwiatki itp
                val parent = json.parent.replace("minecraft:block/", "").replace("block/", "")
                if (parent != "block") {
                    geometries.addAll(loadGeometry(parent))
                }
            }

            json.elements?.forEach {
                val geo = Geometry(
                    Vec3(it.from[0], it.from[1], it.from[2]),
                    Vec3(it.to[0], it.to[1], it.to[2]),
                    mapOf(
                        DOWN to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.down?.uv?.get(0) ?: 0f, it.faces?.down?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.down?.uv?.get(2) ?: 16f, it.faces?.down?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.down?.texture ?: "stone"
                        ),
                        UP to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.up?.uv?.get(0) ?: 0f, it.faces?.up?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.up?.uv?.get(2) ?: 16f, it.faces?.up?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.up?.texture ?: "stone"
                        ),
                        WEST to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.west?.uv?.get(0) ?: 0f, it.faces?.west?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.west?.uv?.get(2) ?: 16f, it.faces?.west?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.west?.texture ?: "stone"
                        ),
                        EAST to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.east?.uv?.get(0) ?: 0f, it.faces?.east?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.east?.uv?.get(2) ?: 16f, it.faces?.east?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.east?.texture ?: "stone"
                        ),
                        NORTH to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.north?.uv?.get(0) ?: 0f, it.faces?.north?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.north?.uv?.get(2) ?: 16f, it.faces?.north?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.north?.texture ?: "stone"
                        ),
                        SOUTH to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.south?.uv?.get(0) ?: 0f, it.faces?.south?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.south?.uv?.get(2) ?: 16f, it.faces?.south?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.south?.texture ?: "stone"
                        ),
                    ),
                    json.textures ?: mapOf(),
                    Vec3(0f, 3*(PI/2).toFloat(), 0f)
                )

                geometries.add(geo)
            }

            val textures = json.textures

            if (textures != null) {
                geometries.forEach {
                    it.faces.forEach forEach2@{ (t, u) ->
                        val newTexture = textures[u.texture.replace("#", "")]?.replace("minecraft:block/", "")?.replace("block/", "") ?: return@forEach2

                        u.texture = newTexture
                    }
                }
            }

            println("$name $textures")
            geometries.forEach {
                it.textures.forEach { t, u ->
                    getTexture(u)
                }
            }
            return geometries
        }
    }
}