package org.example.coords

import org.example.coords.Geometry.FaceName.*
import org.example.raycasting.Raycasting
import org.example.textures.BlockColor
import org.example.textures.TexturesManager
import org.example.utils.ColorUtils.mul
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.abs

class Block(val name: String) { // val position: Vec3,
    //    val color = BlockColor.blockColors[name] ?: BlockColor.ViewColor(0.0, 0.0, 0.0, 0.0)
    var isAir: Boolean = name == "air"
    var isFull: Boolean = true
    var reflective: Float = 1f
    var illumination = 0f

    data class Hit(
        val hit2d: Vec2,
        val hit3d: Vec3,
        val bouncedDirection: Vec3,
        val distance: Float,
        val normal: Vec3,
        val geometry: Geometry
    )

    data class ColorOutgoing(
        val color: Color,
        val outgoingRay: Raycasting.Ray
    )

    var geometries = listOf<Geometry>()

    fun getColor(uv: Vec2, ray: Raycasting.Ray, normal: Vec3): ColorOutgoing {
        var clampedX = (((-uv.x) % 1f) + 1f) % 1f
        var clampedY = (((uv.y) % 1f) + 1f) % 1f

//        return ColorOutgoing(ray.origin.toColor(), Raycasting.Ray(Vec3.random(), Vec3.random()))

        var rayOutPosition = ray.origin
        var rayOutDirection = if (reflective != 1f) ray.direction.reflect(normal).plus(Vec3.random().mul(reflective)) else normal.randomOutwardVector()
        var textureName = name

        fun geometryHit(startPosition: Vec3, direction: Vec3, geometry: Geometry): List<Hit> {
            val hits = mutableListOf<Hit>()
            var depthToTravel: Float
            var directionDivided: Vec3
            var hitPosition: Vec3
            var geometryNormal: Vec3

            // Y plane
            if (direction.y > 0) {
                depthToTravel = geometry.from.y / 16f - startPosition.y
                geometryNormal = Vec3(0f, -1f, 0f)
            } else {
                depthToTravel = startPosition.y - geometry.to.y / 16f
                geometryNormal = Vec3(0f, 1f, 0f)
            }

            directionDivided =
                Vec3(direction.x / abs(direction.y), direction.y / abs(direction.y), direction.z / abs(direction.y))
            hitPosition = startPosition.plus(directionDivided.mul(depthToTravel))
            if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                Hit(
                    Vec2(hitPosition.z, hitPosition.x),
                    hitPosition,
                    Vec3(direction.x, -direction.y, direction.z),
                    directionDivided.length(),
                    geometryNormal,
                    geometry
                )
            )

            // X plane

            if (direction.x > 0) {
                depthToTravel = geometry.from.x / 16f - startPosition.x
                geometryNormal = Vec3(-1f, 0f, 0f)
            } else {
                depthToTravel = startPosition.x - geometry.to.x / 16f
                geometryNormal = Vec3(1f, 0f, 0f)
            }

            directionDivided =
                Vec3(direction.x / abs(direction.x), direction.y / abs(direction.x), direction.z / abs(direction.x))
            hitPosition = startPosition.plus(directionDivided.mul(depthToTravel))
            if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                Hit(
                    Vec2(hitPosition.z, hitPosition.y),
                    hitPosition,
                    Vec3(-direction.x, direction.y, direction.z),
                    directionDivided.length(),
                    geometryNormal,
                    geometry
                )
            )

            // Z plane
            if (direction.z > 0) {
                depthToTravel = geometry.from.z / 16f - startPosition.z
                geometryNormal = Vec3(0f, 0f, -1f)
            } else {
                depthToTravel = startPosition.z - geometry.to.z / 16f
                geometryNormal = Vec3(0f, 0f, 1f)
            }
            directionDivided =
                Vec3(direction.x / abs(direction.z), direction.y / abs(direction.z), direction.z / abs(direction.z))
            hitPosition = startPosition.plus(directionDivided.mul(depthToTravel))
            if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                Hit(
                    Vec2(hitPosition.x, hitPosition.y),
                    hitPosition,
                    Vec3(direction.x, direction.y, -direction.z),
                    directionDivided.length(),
                    geometryNormal,
                    geometry
                )
            )
            return hits
        }


        if (!isFull) {
            val startPosition = ray.origin

            var foundGeometry: Geometry? = null

            for (geometry in geometries) {
                if (geometry.checkIfInsideBlock(startPosition)) {
                    foundGeometry = geometry
                    break
                }
            }
            if (foundGeometry == null) {
                val hits = mutableListOf<Hit>()
                for (geometry in geometries) {
                    val hitsInGeometry =
                        geometryHit(ray.origin, ray.direction, geometry) // tutaj mozna for each uzyc ale to tam
                    for (hit in hitsInGeometry) {
                        hits.add(hit)
                    }
                }

                if (hits.isEmpty()) {
                    return ColorOutgoing(
                        Color(0, 0, 0, 0),
                        Raycasting.Ray(rayOutPosition, rayOutDirection)
                    )    // <= nic nie trafione
                }


                val closestHit = hits.minByOrNull { it.distance }!!
                val randomBouncedDirection = closestHit.normal.randomOutwardVector()
                foundGeometry = closestHit.geometry
                rayOutPosition = closestHit.hit3d
                rayOutDirection = randomBouncedDirection
                clampedX = closestHit.hit2d.x
                clampedY = closestHit.hit2d.y

                val hitFace = getFaceFromNormal(closestHit.normal)

                textureName = foundGeometry.faces[hitFace]!!.texture
            } else {
                textureName = foundGeometry.faces[getFaceFromNormal(normal)]!!.texture
            }

            //najblizszy do startPosition hit to prawdziwy hit
        }
//        return(Color(0,0,0,0)) //<= wyjebac


//        return Color(clampedY, 0f, clampedX)

        val image: BufferedImage =
            TexturesManager.getTexture(textureName) ?: return ColorOutgoing(
                (BlockColor.blockColors[name]?.getJavaColor() ?: Color(
                    126,
                    225,
                    252
                )),
                Raycasting.Ray(rayOutPosition, rayOutDirection),

                )

        val px = (clampedX * (image.width - 1)).toInt()
        val py = (clampedY * (image.height - 1)).toInt()

        // Get pixel color
        val rgb = image.getRGB(px, py)

        var color = Color(rgb, true)

        val mulColor = if (name.contains("grass")) Color(119, 171, 47)
        else if (name.contains("leaves")) Color(119, 171, 47)
        else null

        if (mulColor != null) color = color.mul(mulColor)

        return ColorOutgoing(color, Raycasting.Ray(rayOutPosition, rayOutDirection))
    }


    companion object {
        val air = Block("air")
    }

    override fun toString(): String {
        return "<Block $name>"
    }

    fun getFaceFromNormal(normal: Vec3): Geometry.FaceName {
        return if (normal.x > 0) {
            NORTH
        } else if (normal.x < 0) {
            SOUTH
        } else if (normal.y > 0) {
            UP
        } else if (normal.y < 0) {
            DOWN
        } else if (normal.z > 0) {
            EAST
        } else {
            WEST
        }
    }
}