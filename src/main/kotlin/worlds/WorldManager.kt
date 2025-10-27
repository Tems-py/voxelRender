package org.example.worlds

import net.sandrohc.schematic4j.SchematicLoader
import org.example.coords.Block
import org.example.coords.Vec3
import org.example.textures.BlockManager.Companion.getBlock
import org.example.textures.BlockManager.Companion.loadGeometry
import java.util.stream.Collectors
import kotlin.math.PI
import kotlin.math.floor


object WorldManager {
    fun getWorld(path: String): World {
        val schematic = SchematicLoader.load(path)

        val flatWorld = Array(schematic.width() * schematic.height() * schematic.length()) { Block.air }

        schematic.blocks().collect(Collectors.toList()).forEach {

            val coords = it.left
            val schemBlock = it.right
            val properties = schemBlock.states()
            val index = coords.x * schematic.height() * schematic.length() + coords.y * schematic.length() + coords.z
            val block = getBlock(schemBlock.block.replace("minecraft:", ""))

            block.properties = properties

            handleBlockProperties(block)
            flatWorld[index] = block
        }

        return World(flatWorld, Triple(schematic.width(), schematic.height(), schematic.length()))
    }

    fun loadWorldFromString(worldString: String, size: Triple<Int, Int, Int> = Triple(20, 20, 20)): World {
        val flatWorld = Array(size.first * size.second * size.third) { Block.air }

        val stringSize = worldString.split(";").size
        worldString.split(";").forEachIndexed { index, s ->
            val blockData = s.split(",")
            val name = blockData[0]
            val properties =
                blockData.takeLast(blockData.size - 1).associate { Pair(it.split(":")[0], it.split(":")[1]) }
                    .toMutableMap()

            val block = getBlock(name.replace("minecraft:", ""))
            block.properties = properties

            handleBlockProperties(block)

            val newIndex = if (stringSize == 7 * 7 * 7) {
                val x = floor((index / (7 * 7)).toDouble())
                val y = floor(((index % (7 * 7)) / 7).toDouble())
                val z = 7 - index % 7

                (x * size.first * size.second + y * size.second + z).toInt()
            } else {
                val z = index / (10 * 7)
                val remainder = index % (10 * 7)
                val y = remainder / 7
                val x = 7 - remainder % 7

                x * size.first * size.second + y * size.second + (7 - z)
            }

            flatWorld[newIndex] = block
        }


        return World(flatWorld, size)
    }

    private fun handleBlockProperties(block: Block) {
        var rotation = Vec3.ZERO
        rotation = rotation.plus(
            when (block.properties["facing"]) {
                "east" -> Vec3(0f, (PI / 2).toFloat(), 0f)
                "west" -> Vec3(0f, 3 * (PI / 2).toFloat(), 0f)
                "north" -> Vec3.ZERO
                "south" -> Vec3(0f, (PI).toFloat(), 0f)
                else -> Vec3.ZERO
            }
        )

        rotation = rotation.plus(
            when (block.properties["face"]) {
                "floor" -> Vec3.ZERO
                "ceiling" -> Vec3(0f, 0f, (PI).toFloat())
                "wall" -> Vec3((PI / 2).toFloat(), 0f, 0f)
                else -> Vec3.ZERO
            }
        )

//            println(block.properties["type"])
        rotation = rotation.plus(
            when (block.properties["half"]) {
                "bottom" -> Vec3.ZERO
                "top" -> Vec3((PI).toFloat(), 0f, 0f)
                else -> Vec3.ZERO
            }
        )

        rotation = rotation.plus(
            when (block.properties["type"]) {
                "bottom" -> Vec3.ZERO
                "top" -> Vec3((PI).toFloat(), 0f, 0f)
                else -> Vec3.ZERO
            }
        )

        if (block.properties["type"] == "double") {
            block.geometries.map { it.clone() }.forEach {
                it.rotation = it.rotation.plus(Vec3((PI).toFloat(), 0f, 0f))
                block.geometries = block.geometries.plus(it)
            }
        }


        block.geometries.forEach {
            it.rotation = it.rotation.plus(rotation)
        }

        val sideRotations = mapOf(
            "east" to Vec3(0f, 0.5f * PI.toFloat(), 0f),
            "north" to Vec3(0f, 1f * PI.toFloat(), 0f),
            "south" to Vec3(0f, 0f, 0f),
            "westo" to Vec3(0f, 1.5f * PI.toFloat(), 0f)
        )

        val name = block.name
        if (block.properties.isNotEmpty()) {
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
        }
    }
}