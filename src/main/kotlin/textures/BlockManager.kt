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
        val notFoundGeometries = mutableListOf<String>()
        val geometriesCache = mutableMapOf<String, List<Geometry>>()

        fun getBlock(name: String,properties: Map<String, String>): Block {
            var name = name

            if(properties.isNotEmpty()){
                if(name.contains("fence") ){

                    var block = Block(name+"_post")
                    var geometries = loadGeometry(name+"_post")


                    val sideName = name+"_side"

                    if(properties["east"] == "true"){
                        val sideGeometries = loadGeometry(sideName)
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = Vec3(0f,0.5f * PI.toFloat(),0f)
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                    if(properties["north"] == "true"){
                        val sideGeometries = loadGeometry(sideName)
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = Vec3(0f,1f * PI.toFloat(),0f)
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                    if(properties["south"] == "true"){
                        val sideGeometries = loadGeometry(sideName)
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = Vec3(0f,0f,0f)
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                    if(properties["west"] == "true"){
                        val sideGeometries = loadGeometry(sideName)
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = Vec3(0f,1.5f * PI.toFloat(),0f)
                            geometries = geometries.plus(sideGeometry)
                        }
                    }

                    block.geometries = geometries
                    block.isFull = false
                    return block
                }
                if(name.contains("cobblestone_wall")){
                    var block = Block(name+"_post")
                    var geometries = loadGeometry(name+"_post")


                    if(properties["east"] != "none"){
                        var sideName = "cobblestone_wall_side"
                        if(properties["north"] == "tall"){
                            sideName += "_tall"
                        }
                        val sideGeometries = loadGeometry(sideName)
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = Vec3(0f,0.5f * PI.toFloat(),0f)
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                    if(properties["north"] != "none"){
                        var sideName = "cobblestone_wall_side"
                        if(properties["north"] == "tall"){
                            sideName += "_tall"
                        }
                        val sideGeometries = loadGeometry(sideName)
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = Vec3(0f,1f * PI.toFloat(),0f)
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                    if(properties["south"] != "none"){
                        var sideName = "cobblestone_wall_side"
                        if(properties["north"] == "tall"){
                            sideName += "_tall"
                        }
                        val sideGeometries = loadGeometry(sideName)
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = Vec3(0f,0f,0f)
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                    if(properties["west"] != "none"){
                        var sideName = "cobblestone_wall_side"
                        if(properties["north"] == "tall"){
                            sideName += "_tall"
                        }
                        val sideGeometries = loadGeometry(sideName)
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = Vec3(0f,1.5f * PI.toFloat(),0f)
                            geometries = geometries.plus(sideGeometry)
                        }
                    }

                    block.geometries = geometries
                    block.isFull = false
                    return block
                }

                if(name.contains("bars")){
                    var block = Block(name)
                    var geometries = loadGeometry(name+"_post").plus(loadGeometry(name+"_post_ends"))

//                    var geometries = listOf<Geometry>()

                    if(properties["east"] == "true"){
                        val sideGeometries = loadGeometry(name+"_side")
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = sideGeometry.rotation.plus(Vec3(0f,1.5f * PI.toFloat(),0f))
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                    if(properties["north"] == "true"){
                        val sideGeometries = loadGeometry(name+"_side")
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = sideGeometry.rotation.plus(Vec3(0f,0f,0f))
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                    if(properties["south"] == "true"){
                        val sideGeometries = loadGeometry(name+"_side")
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = sideGeometry.rotation.plus(Vec3(0f,1f * PI.toFloat(),0f))
                            geometries = geometries.plus(sideGeometry)
                        }
                    }
                    if(properties["west"] == "true"){
                        val sideGeometries = loadGeometry(name+"_side")
                        for (sideGeometry in sideGeometries){
                            sideGeometry.rotation = sideGeometry.rotation.plus(Vec3(0f,0.5f * PI.toFloat(),0f))
                            geometries = geometries.plus(sideGeometry)
                        }
                    }

                    block.geometries = geometries
                    block.isFull = false
                    return block
                }
            }

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

            return block
        }

        fun loadGeometry(name: String): List<Geometry> {
            if (notFoundGeometries.contains(name)) return listOf()
            val cache = geometriesCache[name]
//            if (cache != null) return cache // cache nie działa - chyba płytka kopia gdzies jest czy coś IDK

            val file = File("assets/minecraft/models/block/${name}.json")
            if (!file.isFile) {
                notFoundGeometries.add(name)
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
//                if (textures["all"] == null) {
                    geometries.forEach {
                        it.faces.forEach forEach2@{ (t, u) ->
                            val newTexture =
                                textures[u.texture.replace("#", "")]?.replace("minecraft:block/", "")?.replace("block/", "")
                                    ?: return@forEach2

                            u.texture = newTexture
                        }
                    }
//                } else {
//                    geometries.forEach {
//                        it.faces.forEach forEach2@{ (t, u) ->
//                            u.texture = textures["all"].toString().replace("minecraft:block/", "").replace("block/", "")
//                        }
//                    }
//                }
            }

            geometries.forEach {
                it.textures.forEach { t, u ->
                    getTexture(u)
                }
            }

            geometriesCache[name] = geometries.map { it.clone() }

            return geometries
        }
    }
}