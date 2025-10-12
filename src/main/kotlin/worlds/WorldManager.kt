package org.example.worlds

import net.sandrohc.schematic4j.SchematicLoader
import org.example.coords.Block
import org.example.coords.Vec3
import org.example.textures.BlockManager.Companion.getBlock
import java.util.stream.Collectors
import kotlin.math.PI


object WorldManager {
    fun getWorld(path: String): World {
        val schematic = SchematicLoader.load(path)

        schematic.blocks().collect(Collectors.toList())
        schematic.blockEntities().collect(Collectors.toList())
        schematic.entities().collect(Collectors.toList())


        val flatWorld = Array<Block>(schematic.width() * schematic.height() * schematic.length()) { Block.air }

        schematic.blocks().collect(Collectors.toList()).forEach {
            val coords = it.left
            val schemBlock = it.right
            val index = coords.x * schematic.height() * schematic.length() + coords.y * schematic.length() + coords.z
            val block = getBlock(schemBlock.block.replace("minecraft:", ""))

            block.properties = schemBlock.states()
            var rotation = Vec3.ZERO
            rotation = rotation.plus( when (block.properties["facing"]) {
                "north" -> Vec3(0f, (PI / 2).toFloat(), 0f)
                "south" -> Vec3(0f, 3 * (PI / 2).toFloat(), 0f)
                "east" -> Vec3.ZERO
                "west" -> Vec3(0f, (PI).toFloat(), 0f)
                else -> Vec3.ZERO
            })

            rotation = rotation.plus( when (block.properties["face"]) {
                "floor" -> Vec3.ZERO
                "ceiling" -> Vec3(0f, 0f, (PI).toFloat())
                "wall" -> Vec3((PI / 2).toFloat(), 0f, 0f)
                else -> Vec3.ZERO
            })


            block.geometries.forEach {
                it.rotation = rotation
            }

            flatWorld[index] = block
        }

        return World(flatWorld, Triple(schematic.width(), schematic.height(), schematic.length()))
    }
}