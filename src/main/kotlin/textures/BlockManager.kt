package me.tems.textures

import kotlinx.serialization.json.Json
import me.tems.coords.Block
import me.tems.coords.Geometry
import me.tems.coords.Geometry.FaceName.*
import me.tems.coords.Vec2
import me.tems.coords.Vec3
import me.tems.coords.add
import me.tems.textures.TexturesManager.Companion.getTexture
import textures.MinecraftModel
import java.io.File
import kotlin.math.PI

class BlockManager {
    companion object {
        private val notFoundGeometries = mutableSetOf<String>()
        private val geometriesCache = mutableMapOf<String, List<Geometry>>()
        private val jsonParser = Json { ignoreUnknownKeys = true }
        private val blockTexturesPath =
            System.getProperty("renderer.block-models-path") ?: "assets/minecraft/models/block/"

        fun getBlock(name: String, properties: MutableMap<String, String> = mutableMapOf()): Block {
            val block = Block(name)
            block.properties = properties

            val geometries = loadGeometry(name)

            if (geometries.isNotEmpty()) { // better non-full block detection
                block.geometries = geometries
                block.isFull = false
            }

//            if (name == "stone_bricks") block.reflective = 0f

            if (name == "glowstone") block.illumination = 3f
            if (name == "sea_lantern") block.illumination = 3f
            if (name == "dragon_egg") block.illumination = 3f
            if (name == "torch") block.reflective = 1.6f


            return handleBlockProperties(block)
        }

        fun loadGeometry(name: String): List<Geometry> {
            if (notFoundGeometries.contains(name)) return listOf()
            val cache = geometriesCache[name]
            if (cache != null) return cache.map { it.clone() } // cache nie działa - chyba płytka kopia gdzies jest czy coś IDK

            val file = File("$blockTexturesPath${name}.json")
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

        fun handleBlockProperties(block: Block): Block {
            var rotation = Vec3.ZERO
            rotation = rotation.add(
                when (block.properties["facing"]) {
                    "east" -> Vec3(0f, (PI / 2).toFloat(), 0f)
                    "west" -> Vec3(0f, 3 * (PI / 2).toFloat(), 0f)
                    "north" -> Vec3.ZERO
                    "south" -> Vec3(0f, (PI).toFloat(), 0f)
                    else -> Vec3.ZERO
                }
            )

            rotation = rotation.add(
                when (block.properties["face"]) {
                    "floor" -> Vec3.ZERO
                    "ceiling" -> Vec3(0f, 0f, (PI).toFloat())
                    "wall" -> Vec3((PI / 2).toFloat(), 0f, 0f)
                    else -> Vec3.ZERO
                }
            )

//            println(block.properties["type"])
            rotation = rotation.add(
                when (block.properties["half"]) {
                    "bottom" -> Vec3.ZERO
                    "top" -> Vec3((PI).toFloat(), 0f, 0f)
                    else -> Vec3.ZERO
                }
            )

            rotation = rotation.add(
                when (block.properties["type"]) {
                    "bottom" -> Vec3.ZERO
                    "top" -> Vec3((PI).toFloat(), 0f, 0f)
                    else -> Vec3.ZERO
                }
            )

            if (block.properties["type"] == "double") {
                block.geometries.map { it.clone() }.forEach {
                    it.rotation = it.rotation.add(Vec3((PI).toFloat(), 0f, 0f))
                    block.geometries = block.geometries.plus(it)
                }
            }


            block.geometries.forEach {
                it.rotation = it.rotation.add(rotation)
            }

            val sideRotations = mapOf(
                "east" to Vec3(0f, 0.5f * PI.toFloat(), 0f),
                "north" to Vec3(0f, 1f * PI.toFloat(), 0f),
                "south" to Vec3(0f, 0f, 0f),
                "west" to Vec3(0f, 1.5f * PI.toFloat(), 0f)
            )

            val name = block.name
            if (name.contains("fence")) {
                var geometries = loadGeometry(name + "_post")
                val sideName = name + "_side"

                sideRotations.forEach { (side, rot) ->
                    if (block.properties[side] == "true") {
                        loadGeometry(sideName).forEach {
                            it.rotation = rot
                            geometries = geometries.plus(it)

                        }
                    }
                }

                block.geometries = geometries
                block.isFull = false
            }
            if (name.contains("cobblestone_wall")) {
                var geometries = loadGeometry(name + "_post")

                sideRotations.forEach { (side, rot) ->
                    if (block.properties[side] != "none") {
                        var sideName = "cobblestone_wall_side"
                        if (block.properties[side] == "tall") {
                            sideName += "_tall"
                        }
                        val sideGeometries = loadGeometry(sideName)
                        for (sideGeometry in sideGeometries) {
                            sideGeometry.rotation = rot
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                }

                block.geometries = geometries
                block.isFull = false
            }

            if (name.contains("bars")) {
                var geometries = loadGeometry(name + "_post").plus(loadGeometry(name + "_post_ends"))

                sideRotations.forEach { (side, rot) ->
                    if (block.properties[side] == "true") {
                        loadGeometry(name + "_side").forEach {
                            it.rotation = rot
                            geometries = geometries.plus(it)
                        }
                    }
                }

                block.geometries = geometries
                block.isFull = false
            }

            return block
        }
    }
}