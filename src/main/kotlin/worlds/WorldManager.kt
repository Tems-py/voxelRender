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
                blockData.drop(1).associate { val parts = it.split(":", limit = 2); Pair(parts[0], parts.getOrElse(1) { "" }) }
                    .toMutableMap()

            val block = getBlock(name.replace("minecraft:", ""), properties)

            val z = index / (stringWorldSize.second * stringWorldSize.third)
            val remainder = index % (stringWorldSize.second * stringWorldSize.third)
            val y = remainder / stringWorldSize.third
            val x = stringWorldSize.first - 1 - remainder % stringWorldSize.first
            val newIndex = x * outputWorldSize.second * outputWorldSize.third + y * outputWorldSize.third + (outputWorldSize.third - 1 - z)

            flatWorld[newIndex] = block
        }

        return World(flatWorld, outputWorldSize)
    }
}