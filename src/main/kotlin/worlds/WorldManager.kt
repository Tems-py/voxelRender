package org.example.worlds

import net.sandrohc.schematic4j.SchematicLoader
import org.example.coords.Block
import java.util.stream.Collectors


object WorldManager {
    fun getWorld(): World {
        val schematic = SchematicLoader.load("worlds/glowstone_test.schem")

        schematic.blocks().collect(Collectors.toList())
        schematic.blockEntities().collect(Collectors.toList())
        schematic.entities().collect(Collectors.toList())


        val flatWorld = Array<Block>(schematic.width() * schematic.height() * schematic.length()) { Block.air }

        schematic.blocks().collect(Collectors.toList()).forEach {
            val coords = it.left
            val block = it.right
            val index = coords.x * schematic.height() * schematic.length() + coords.y * schematic.length() + coords.z
            flatWorld[index] = Block(block.block.replace("minecraft:", ""))
        }

        return World(flatWorld, Triple(schematic.width(), schematic.height(), schematic.length()))
    }
}