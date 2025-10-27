package org.example.textures

import kotlinx.serialization.json.Json
import org.example.coords.Block
import org.example.coords.Geometry
import org.example.coords.Geometry.FaceName.*
import org.example.coords.Vec2
import org.example.coords.Vec3
import org.example.textures.TexturesManager.Companion.getTexture
import textures.MinecraftModel
import java.io.File
import kotlin.math.PI

class BlockManager {
    companion object {
        private val notFoundGeometries = mutableListOf<String>()
        private val geometriesCache = mutableMapOf<String, List<Geometry>>()
        private val jsonParser = Json { ignoreUnknownKeys = true }

        fun getBlock(name: String): Block {

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
            if (name == "stone_bricks") block.reflective = 0.6f

            return block
        }

        fun loadGeometry(name: String): List<Geometry> {
            if (notFoundGeometries.contains(name)) return listOf()
            val cache = geometriesCache[name]
            if (cache != null) return cache.map { it.clone() } // cache nie działa - chyba płytka kopia gdzies jest czy coś IDK

            val file = File("assets/minecraft/models/block/${name}.json")
            if (!file.isFile) {
                notFoundGeometries.add(name)
                println("No model: $name")
                return listOf()
            }
            val geometries = mutableListOf<Geometry>()


            val json = jsonParser.decodeFromString<MinecraftModel>(file.readText())
            if (json.parent != null) { // tinted_cross - trawa, kwiatki itp
                val parent = json.parent.replace("minecraft:block/", "").replace("block/", "")
                if (parent != "block") {
                    geometries.addAll(loadGeometry(parent))
                }
            }

            json.elements?.forEach {
                val rotationVec = Vec3(
                    if (it.rotation?.axis == "x" && it.rotation.angle != null) it.rotation.angle * PI.toFloat() / 180F else 0f,
                    if (it.rotation?.axis == "y" && it.rotation.angle != null) it.rotation.angle * PI.toFloat() / 180F else 0f,
                    if (it.rotation?.axis == "z" && it.rotation.angle != null) it.rotation.angle * PI.toFloat() / 180F else 0f,
                )


                val geo = Geometry(
                    Vec3(it.from[0], it.from[1], it.from[2]),
                    Vec3(it.to[0], it.to[1], it.to[2]),
                    mapOf(
                        DOWN to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.down?.uv?.get(0) ?: 0f, it.faces?.down?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.down?.uv?.get(2) ?: 16f, it.faces?.down?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.down?.texture ?: "air"
                        ),
                        UP to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.up?.uv?.get(0) ?: 0f, it.faces?.up?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.up?.uv?.get(2) ?: 16f, it.faces?.up?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.up?.texture ?: "air"
                        ),
                        WEST to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.west?.uv?.get(0) ?: 0f, it.faces?.west?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.west?.uv?.get(2) ?: 16f, it.faces?.west?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.west?.texture ?: "air"
                        ),
                        EAST to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.east?.uv?.get(0) ?: 0f, it.faces?.east?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.east?.uv?.get(2) ?: 16f, it.faces?.east?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.east?.texture ?: "air"
                        ),
                        NORTH to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.north?.uv?.get(0) ?: 0f, it.faces?.north?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.north?.uv?.get(2) ?: 16f, it.faces?.north?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.north?.texture ?: "air"
                        ),
                        SOUTH to Geometry.Face(
                            Pair(
                                Vec2(it.faces?.south?.uv?.get(0) ?: 0f, it.faces?.south?.uv?.get(1) ?: 0f), Vec2(
                                    it.faces?.south?.uv?.get(2) ?: 16f, it.faces?.south?.uv?.get(3) ?: 16f
                                )
                            ), it.faces?.south?.texture ?: "air"
                        ),
                    ),
                    json.textures ?: mapOf(),
                    rotationVec
                )

                geometries.add(geo)
            }

            val textures = json.textures

            if (textures != null) {
                geometries.forEach {
                    it.faces.forEach forEach2@{ (_, u) ->
                        val newTexture =
                            textures[u.texture.replace("#", "")]?.replace("minecraft:block/", "")?.replace("block/", "")
                                ?: return@forEach2

                        u.texture = newTexture
                    }
                }
            }

            geometries.forEach {
                it.textures.forEach { (_, u) ->
                    getTexture(u)
                }
            }

            geometriesCache[name] = geometries.map { it.clone() }

            return geometries
        }
    }
}