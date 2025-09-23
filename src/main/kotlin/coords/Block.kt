package org.example.coords

import org.example.textures.BlockColor
import org.example.textures.TexturesManager
import org.example.utils.ColorUtils.mul
import java.awt.Color
import java.awt.image.BufferedImage

class Block(val name: String) { // val position: Vec3,
    //    val color = BlockColor.blockColors[name] ?: BlockColor.ViewColor(0.0, 0.0, 0.0, 0.0)
    var isAir: Boolean = name == "air"
    var isFull: Boolean = true

    var geometries = listOf<Geometry>()

    fun getColor(uv: Vec2, face: Geometry.FaceName): Color {
        val clampedX = (((-uv.x) % 1f) + 1f) % 1f
        val clampedY = (((uv.y) % 1f) + 1f) % 1f

        if (!isFull) {
            var inside = false
            for (geometry in geometries) {
                if (geometry.checkIfUvAssigned(Vec2(uv.x % 1, -(uv.y % 1)), face)) {
                    inside = true
                    break
                }
            }
            if (inside == false) {
                return Color(0, 0, 0, 0)
            }
        }

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

        var color = Color(rgb, true)

        val mulColor = if (name.contains("grass")) Color(119, 171, 47)
        else if (name.contains("leaves")) Color(119, 171, 47)
        else null

        if (mulColor != null) color = color.mul(mulColor)

        return color
    }


    companion object {
        val air = Block("air")
    }

    override fun toString(): String {
        return "<Block $name>"
    }
}