package org.example.coords

import org.example.textures.BlockColor
import org.example.textures.TexturesManager
import org.example.utils.ColorUtils.add
import org.example.utils.ColorUtils.mul
import java.awt.Color
import java.awt.image.BufferedImage

class Block(val name: String) { // val position: Vec3,
    //    val color = BlockColor.blockColors[name] ?: BlockColor.ViewColor(0.0, 0.0, 0.0, 0.0)
    var isAir: Boolean = name == "air"

    fun getColor(uv: Vec2): Color {
        val clampedX = (((1f - -uv.y) % 1f) + 1f) % 1f
        val clampedY = (((1f - uv.x) % 1f) + 1f) % 1f

//        return Color(clampedY, 0f, clampedX)
        val image: BufferedImage =
            TexturesManager.getTexture(name) ?: return (BlockColor.blockColors[name]?.getJavaColor() ?: Color(
                126,
                225,
                252
            ))

        val px = (clampedX * (image.width - 1)).toInt()
        val py = (clampedY * (image.height - 1)).toInt()

        // Get pixel color
        val rgb = image.getRGB(px, py)

        val mulColor = if (name.contains("grass")) Color(119, 171, 47)
        else if (name.contains("leaves")) Color(119, 171, 47)
        else Color(1f, 1f, 1f)

        return Color(rgb, true).mul(mulColor)
    }

    companion object {
        val air = Block("air")
    }

    override fun toString(): String {
        return "<Block $name>"
    }
}