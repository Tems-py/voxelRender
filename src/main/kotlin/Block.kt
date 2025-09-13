package org.example

import org.example.textures.BlockColor

class Block(name: String) { // val position: Vec3,
    val color = BlockColor.blockColors[name] ?: BlockColor.ViewColor(0.0, 0.0, 0.0, 0.0)

    companion object {
        val air = Block("air")
    }

    override fun toString(): String {
        return "<Block $color>"
    }
}