package org.example.coords

import org.example.raycasting.Raycasting
import org.example.textures.BlockColor
import org.example.textures.TexturesManager
import org.example.utils.ColorUtils.mul
import org.example.wrapTo01
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
                (ray.origin.x.wrapTo01() + 1).wrapTo01(),
                (ray.origin.y.wrapTo01() + 1).wrapTo01(),
                (ray.origin.z.wrapTo01() + 1).wrapTo01()
            )
            val direction = if (ray.origin.z % 1 == 0f) {
                Vec3(ray.direction.x, ray.direction.y, ray.direction.z)
            } else {
                Vec3(-ray.direction.x, ray.direction.y, ray.direction.z)
            }
            var foundGeometry: Geometry? = null
            for (geometry in geometries) {
                if (geometry.checkIfInsideBlock(startBlockPosition)) {
                    foundGeometry = geometry
                    break
                }
            }
            if (foundGeometry == null) {
                var blockPosition = startBlockPosition
                while (blockPosition.min(startBlockPosition).abs().length() < 3.5f) {
                    blockPosition = blockPosition.plus(direction.mul(0.001f))
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