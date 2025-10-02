package org.example.coords

import org.example.raycasting.Raycasting
import org.example.textures.BlockColor
import org.example.textures.TexturesManager
import org.example.utils.ColorUtils.mul
import org.example.wrapTo01
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.round

class Block(val name: String) { // val position: Vec3,
    //    val color = BlockColor.blockColors[name] ?: BlockColor.ViewColor(0.0, 0.0, 0.0, 0.0)
    var isAir: Boolean = name == "air"
    var isFull: Boolean = true

    var geometries = listOf<Geometry>()

    fun getColor(uv: Vec2, ray: Raycasting.Ray): Color {
        var clampedX = (((-uv.x) % 1f) + 1f) % 1f
        var clampedY = (((uv.y) % 1f) + 1f) % 1f

//        val colors = listOf(
//            Color(255, 0, 0),
//            Color(255, 255, 0),
//            Color(0, 255, 0),
//            Color(0, 255, 255),
//            Color(0, 0, 255),
//            Color(255, 0, 255),
//            Color(128, 128, 255),
//            Color(255, 128, 0),
//            Color(0, 0, 128),
//            Color(128, 0, 0),
//        )


//        return Vec3(ray.origin.x, ray.origin.y, ray.origin.z).toColor()

        if (!isFull) {
//            val startBlockPosition = Vec3(
//                if (ray.direction.x > 0) ray.origin.x % 1 else ray.origin.x % 1 + 1f,
//                if (ray.direction.y > 0) ray.origin.y % 1 else ray.origin.y % 1 + 1f,
//                if (ray.direction.z > 0) ray.origin.z % 1 else ray.origin.z % 1 + 1f,
//            )

//            val startBlockPosition = Vec3(
//                (ray.origin.x + 1E-6f).wrapTo01(),
//                (ray.origin.y + 1E-6f).wrapTo01(),
//                (ray.origin.z + 1E-6f).wrapTo01(),
//
////                ray.origin.y.wrapTo01(),
////                ray.origin.z.wrapTo01(),
//            )
//            return startBlockPosition.toColor()


            val startBlockPosition = ray.origin
//            val startBlockPosition2 = Vec3(
//                1f -(ray.origin.x ).wrapTo01(),
//                (ray.origin.y ).wrapTo01(),
//                (ray.origin.z ).wrapTo01(),
//
////                ray.origin.y.wrapTo01(),
////                ray.origin.z.wrapTo01(),
//            )

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
                    blockPosition = blockPosition.plus(ray.direction.mul(0.001f))
                    for (geometry in geometries) {
                        if (geometry.checkIfInsideBlock(blockPosition)) {
                            foundGeometry = geometry
                            val closestWall = geometry.findClosestWall(blockPosition)
                            when (closestWall.face) {
                                Geometry.FaceName.NORTH -> {
                                    clampedX = (((-blockPosition.z) % 1f) + 1f) % 1f
                                    clampedY = (((blockPosition.y) % 1f) + 1f) % 1f
                                }
                                Geometry.FaceName.SOUTH -> {
                                    clampedX = (((-blockPosition.z) % 1f) + 1f) % 1f
                                    clampedY = (((blockPosition.x) % 1f) + 1f) % 1f
                                }
                                Geometry.FaceName.UP -> {
                                    clampedX = (((blockPosition.z) % 1f) + 1f) % 1f
                                    clampedY = (((-blockPosition.x) % 1f) + 1f) % 1f
                                }
                                Geometry.FaceName.DOWN -> {
                                    clampedX = (((blockPosition.z) % 1f) + 1f) % 1f
                                    clampedY = (((-blockPosition.x) % 1f) + 1f) % 1f
                                }
                                Geometry.FaceName.EAST -> {
                                    clampedX = (((-blockPosition.z) % 1f) + 1f) % 1f
                                    clampedY = (((-blockPosition.x) % 1f) + 1f) % 1f
                                }
                                Geometry.FaceName.WEST -> {
                                    clampedX = (((-blockPosition.z) % 1f) + 1f) % 1f
                                    clampedY = (((-blockPosition.x) % 1f) + 1f) % 1f
                                }
                            }

                            break
                        }
                    }
                    if (foundGeometry != null) break
                }
            }

            if (foundGeometry == null) {
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