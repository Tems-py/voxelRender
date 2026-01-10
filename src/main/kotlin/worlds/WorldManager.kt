package me.tems.worlds

import me.tems.coords.Block
import me.tems.textures.BlockManager.Companion.getBlock

object WorldManager {
    fun loadWorldFromString(
        worldString: String,
        stringWorldSize: Triple<Int, Int, Int> = Triple(7, 7, 7),
        outputWorldSize: Triple<Int, Int, Int> = Triple(20, 20, 20)
    ): World {
        val flatWorld = Array(outputWorldSize.first * outputWorldSize.second * outputWorldSize.third) { Block.air }

        worldString.split(";").forEachIndexed { index, s ->
            val blockData = s.split(",")
            val name = blockData[0]
            val properties =
                blockData.takeLast(blockData.size - 1).associate { Pair(it.split(":")[0], it.split(":")[1]) }
                    .toMutableMap()

            val block = getBlock(name.replace("minecraft:", ""), properties)

            val z = index / (stringWorldSize.second * stringWorldSize.third)
            val remainder = index % (stringWorldSize.second * stringWorldSize.third)
            val y = remainder / stringWorldSize.third
            val x = stringWorldSize.first - remainder % stringWorldSize.first
            val newIndex = x * outputWorldSize.first * outputWorldSize.second + y * outputWorldSize.second + (7 - z)

            flatWorld[newIndex] = block
        }

        return World(flatWorld, outputWorldSize)
    }
}