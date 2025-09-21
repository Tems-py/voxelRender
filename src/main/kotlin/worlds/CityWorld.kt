package org.example.worlds

import org.example.coords.Block
import java.io.File

object CityWorld {

    fun getWorld(): Array<Block> {
        val worldSizeX = 9
        val worldSizeY = 9
        val worldSizeZ = 9

        val flatWorld = Array<Block>(worldSizeX * worldSizeY * worldSizeZ) { Block.air }

        File("worlds/glowstone.da").forEachLine {
            val line = it.split(" = ")
            val coords = line[0].split("|").map { it.toInt() }
            val block = line[1].split(",")[0]
            val index = coords[0] * worldSizeY * worldSizeZ + coords[1] * worldSizeZ + coords[2]
            flatWorld[index] = Block(block)
        }

        return flatWorld
    }
}