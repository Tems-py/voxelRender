package org.example.worlds

import org.example.coords.Block
import java.io.File

object CityWorld {

    fun getWorld(): Array<Array<Array<Block>>> {
        val world = Array(60) { Array(20) { Array(60) { Block.air } } }

        File("worlds/city.da").forEachLine {
            val line = it.split(" = ")
            val coords = line[0].split("|").map { it.toInt() }
            val block = line[1].split(",")[0]

            world[coords[0]][coords[1]][coords[2]] = Block(block)
        }

        return world
    }
}