package org.example

import org.example.textures.BlockColor
import org.example.textures.TexturesManager
import java.awt.Color
import java.awt.image.BufferedImage

class Block(val name: String) { // val position: Vec3,
//    val color = BlockColor.blockColors[name] ?: BlockColor.ViewColor(0.0, 0.0, 0.0, 0.0)

    fun getColor(uv: Vec2): Color {
//        return Color(((uv.x / 2 % 1) * 255).toInt(), 1, (uv.y / 2 % 1 * 255).toInt())
        try {
            val image: BufferedImage = TexturesManager.getTexture(name)

            val clampedX = uv.x / 2 % 1
            val clampedY = uv.y / 2 % 1

            // Convert normalized to pixel coordinates
            val px = (clampedX * (image.width - 1)).toInt()
            val py = (clampedY * (image.height - 1)).toInt()

            // Get pixel color
            val rgb = image.getRGB(px, py)
            return Color(rgb, true) // 'true' to include alpha
        } catch (e: Exception) {
            println(e)
        }

        return (BlockColor.blockColors[name] ?: BlockColor.ViewColor(0.0, 0.0, 0.0, 0.0)).getJavaColor()
    }

    companion object {
        val air = Block("air")
    }

    override fun toString(): String {
        return "<Block $name>"
    }
}