package org.example.coords

import org.example.textures.BlockColor
import org.example.textures.TexturesManager
import java.awt.Color
import java.awt.image.BufferedImage

class Block(val name: String) { // val position: Vec3,
//    val color = BlockColor.blockColors[name] ?: BlockColor.ViewColor(0.0, 0.0, 0.0, 0.0)

    fun getColor(uv: Vec2): Color {
        val clampedX = (((uv.x) % 1) + 1) % 1
        val clampedY = (((uv.y) % 1) + 1) % 1

//        return Color(clampedY, 0f, clampedX)
        val image: BufferedImage = TexturesManager.getTexture(name) ?: return (BlockColor.blockColors[name]?.getJavaColor() ?: Color(126, 225, 252))

        val px = (clampedX * (image.width - 1)).toInt()
        val py = (clampedY * (image.height - 1)).toInt()

        // Get pixel color
        val rgb = image.getRGB(px, py)
        return Color(rgb, true) // 'true' to include alpha
    }

    companion object {
        val air = Block("air")
    }

    override fun toString(): String {
        return "<Block $name>"
    }
}