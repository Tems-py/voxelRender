package org.example.worlds

import net.sandrohc.schematic4j.SchematicLoader
import org.example.coords.Block
import org.example.coords.Vec3
import org.example.textures.BlockManager.Companion.getBlock
import java.util.stream.Collectors
import kotlin.math.PI
import kotlin.math.floor


object WorldManager {
    fun getWorld(path: String): World {
        val schematic = SchematicLoader.load(path)

//        schematic.blocks().collect(Collectors.toList())
//        schematic.blockEntities().collect(Collectors.toList())
//        schematic.entities().collect(Collectors.toList())


        val flatWorld = Array<Block>(schematic.width() * schematic.height() * schematic.length()) { Block.air }

        schematic.blocks().collect(Collectors.toList()).forEach {
            val coords = it.left
            val schemBlock = it.right
            val index = coords.x * schematic.height() * schematic.length() + coords.y * schematic.length() + coords.z
            val block = getBlock(schemBlock.block.replace("minecraft:", ""))

            block.properties = schemBlock.states()
            var rotation = Vec3.ZERO
            rotation = rotation.plus(
                when (block.properties["facing"]) {
                    "north" -> Vec3(0f, (PI / 2).toFloat(), 0f)
                    "south" -> Vec3(0f, 3 * (PI / 2).toFloat(), 0f)
                    "east" -> Vec3.ZERO
                    "west" -> Vec3(0f, (PI).toFloat(), 0f)
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

            rotation = rotation.plus(
                when (block.properties["half"]) {
                    "bottom" -> Vec3.ZERO
                    "top" -> Vec3((PI).toFloat(), 0f, 0f)
                    else -> Vec3.ZERO
                }
            )


            block.geometries.forEach {
                it.rotation = rotation
            }

            flatWorld[index] = block
        }

        return World(flatWorld, Triple(schematic.width(), schematic.height(), schematic.length()))
    }


    // minecraft:spruce_log,axis:y;minecraft:air;minecraft:air;minecraft:spruce_log,axis:y;minecraft:air;minecraft:air;minecraft:spruce_log,axis:y;minecraft:spruce_log,axis:y;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:spruce_log,axis:y;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:spruce_log,axis:y;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:spruce_leaves,distance:2,persistent:true,waterlogged:false;minecraft:air;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:air;minecraft:spruce_leaves,distance:2,persistent:true,waterlogged:false;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:spruce_leaves,distance:2,persistent:true,waterlogged:false;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_leaves,distance:2,persistent:true,waterlogged:false;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:air;minecraft:air;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:air;minecraft:air;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:spruce_leaves,distance:2,persistent:true,waterlogged:false;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_leaves,distance:2,persistent:true,waterlogged:false;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_stairs,facing:east,half:bottom,shape:straight,waterlogged:false;minecraft:spruce_stairs,facing:east,half:bottom,shape:straight,waterlogged:false;minecraft:spruce_stairs,facing:east,half:bottom,shape:straight,waterlogged:false;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_log,axis:y;minecraft:spruce_planks;minecraft:spruce_log,axis:y;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_log,axis:y;minecraft:glass_pane,east:false,north:true,south:true,waterlogged:false,west:false;minecraft:spruce_log,axis:y;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_stairs,facing:south,half:bottom,shape:straight,waterlogged:false;minecraft:spruce_planks;minecraft:spruce_planks;minecraft:spruce_planks;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_slab,type:bottom,waterlogged:false;minecraft:spruce_slab,type:bottom,waterlogged:false;minecraft:spruce_slab,type:bottom,waterlogged:false;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_planks;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:glass_pane,east:true,north:false,south:false,waterlogged:false,west:true;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:air;minecraft:air;minecraft:spruce_stairs,facing:south,half:bottom,shape:straight,waterlogged:false;minecraft:spruce_planks;minecraft:spruce_planks;minecraft:spruce_planks;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_slab,type:bottom,waterlogged:false;minecraft:spruce_slab,type:bottom,waterlogged:false;minecraft:spruce_slab,type:bottom,waterlogged:false;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_log,axis:y;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_log,axis:y;minecraft:spruce_planks;minecraft:spruce_log,axis:y;minecraft:spruce_log,axis:y;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_log,axis:y;minecraft:glass_pane,east:false,north:true,south:true,waterlogged:false,west:false;minecraft:spruce_log,axis:y;minecraft:spruce_log,axis:y;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:air;minecraft:spruce_stairs,facing:south,half:bottom,shape:straight,waterlogged:false;minecraft:spruce_planks;minecraft:spruce_planks;minecraft:spruce_planks;minecraft:spruce_leaves,distance:1,persistent:true,waterlogged:false;minecraft:air;minecraft:air;minecraft:air;minecraft:spruce_slab,type:bottom,waterlogged:false;minecraft:spruce_slab,type:bottom,waterlogged:false;minecraft:spruce_slab,type:bottom,waterlogged:false;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air

    fun loadWorldFromString(worldString: String, size: Triple<Int, Int, Int> = Triple(20, 20, 20)): World {
        val flatWorld = Array<Block>(size.first * size.second * size.third) { Block.air }

        val stringSize = worldString.split(";").size
        worldString.split(";").forEachIndexed { index, s ->
            val blockData = s.split(",")
            var name = blockData[0]
            if (name == "minecraft:spruce_fence") {
                name = "minecraft:spruce_fence_post"
            } else if (name == "minecraft:oak_fence") {
                name = "minecraft:oak_fence_post"
            } else if (name == "minecraft:cobblestone_wall") {
                name = "minecraft:cobblestone_wall_post"
            }
            val properties =
                blockData.takeLast(blockData.size - 1).associate { Pair(it.split(":")[0], it.split(":")[1]) }
                    .toMutableMap()

            val block = getBlock(name.replace("minecraft:", ""))

//           if (properties.isNotEmpty()) println(properties)
            block.properties = properties

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
                when (block.properties["type"]) {
                    "bottom" -> Vec3.ZERO
                    "top" -> Vec3((PI).toFloat(), 0f, 0f)
                    else -> Vec3.ZERO
                }
            )

            if (properties["type"] == "double") {
                block.geometries.map { it.clone() }.forEach {
                    it.rotation = it.rotation.plus(Vec3((PI).toFloat(), 0f, 0f))
                    block.geometries = block.geometries.plus(it)
                }
            }

            block.geometries.forEach {
                it.rotation = it.rotation.plus(rotation)
            }


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
}