package org.example.coords

import org.example.raycasting.Raycasting
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

    fun getColor(uv: Vec2, ray: Raycasting.Ray): Color {
        var clampedX = (((-uv.x) % 1f) + 1f) % 1f
        var clampedY = (((uv.y) % 1f) + 1f) % 1f

        if (!isFull) {
            val startBlockPosition = Vec3(
                (ray.origin.x % 1 + 1) % 1,
                (ray.origin.y % 1 + 1) % 1,
                (ray.origin.z % 1 + 1) % 1
            )
            var foundGeometry: Geometry? = null
            for (geometry in geometries) {
                if (geometry.checkIfInsideBlock(startBlockPosition)) {
                    foundGeometry = geometry
                    break
                }
            }
            if (foundGeometry == null) {
                var blockPosition = startBlockPosition
                while (blockPosition.min(startBlockPosition).abs().length() < 1.8f) {
                    blockPosition = blockPosition.plus(ray.direction.normalize().mul(0.001f))
                    for (geometry in geometries) {
                        if (geometry.checkIfInsideBlock(blockPosition)) {
                            foundGeometry = geometry
                            clampedX = (((-blockPosition.z) % 1f) + 1f) % 1f
                            clampedY = (((blockPosition.x) % 1f) + 1f) % 1f
                            break
                        }
                    }
                    if (foundGeometry != null) break
                }
            }

            if (foundGeometry == null){
                println(ray.origin)
                return Color(0, 0, 0, 0)
            }
            // JANKU TUTAJ JEST SLAB ROBIONY WSM
            // tutaj trzeba zrobić raycast dodatkowy dot. wewnętrznych miejsc geometry


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