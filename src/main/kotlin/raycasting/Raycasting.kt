package org.example.raycasting

import org.example.coords.Block
import org.example.coords.Geometry
import org.example.coords.Vec2
import org.example.coords.Vec3
import org.example.utils.ColorUtils.avg
import org.example.utils.ColorUtils.mul
import org.example.worlds.World
import org.example.wrapTo01
import java.awt.Color
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

object Raycasting {

    data class Ray(val origin: Vec3, val direction: Vec3)
    data class RayHit(
        val block: Block,
        val position: Vec3, // voxel coords
        val face: Vec3, // normal of the face hit
        var color: Color,
        var incomingLight: Float
    )

    val hitFaces = arrayOf(Vec3.ZERO, Vec3(-1f, 0f, 0f), Vec3.ZERO, Vec3(0f, -1f, 0f), Vec3.ZERO, Vec3(0f, 0f, -1f), Vec3.ZERO)

    fun raycast(
        world: World,
        ray: Ray,
        maxDistance: Float,
        bouncesLeft: Int,
        sampling: Int
    ): Color? {
        val colors = mutableListOf<Color>()
        var incomingLight = 0f
        for (i in 0..sampling) {
            val rayHit = sendRay(world, ray, maxDistance, bouncesLeft) ?: continue
            colors.add(rayHit.color)
            incomingLight += rayHit.incomingLight
        }
//        return Color(min(1f, lightIncoming / 5f), min(1f, lightIncoming / 5f), min(1f, lightIncoming / 5f))
        if (colors.isEmpty()) return null;
//        return colors[0].avg(colors).mul(min(1f, lightIncoming))
        return colors[0].avg(colors).mul(min(1f, incomingLight / sampling * 2))

    }

    fun sendRay(
        world: World,
        ray: Ray,
        maxDistance: Float,
        bouncesLeft: Int,
        previousRayHit: RayHit? = null
    ): RayHit? {

        // Use the original direction (don't normalize yet)
        val dir = ray.direction

        // Current position along the ray
        val currentX = ray.origin.x
        val currentY = ray.origin.y
        val currentZ = ray.origin.z

        // Current voxel coordinates
        var voxelX = floor(currentX).toInt()
        var voxelY = floor(currentY).toInt()
        var voxelZ = floor(currentZ).toInt()

        // Direction to step in (either 1 or -1 for each axis)
        val stepX = if (dir.x > 0) 1 else if (dir.x < 0) -1 else 0
        val stepY = if (dir.y > 0) 1 else if (dir.y < 0) -1 else 0
        val stepZ = if (dir.z > 0) 1 else if (dir.z < 0) -1 else 0

        // Avoid division by zero
        val deltaDistX = if (abs(dir.x) < 1e-6f) Float.MAX_VALUE else abs(1f / dir.x)
        val deltaDistY = if (abs(dir.y) < 1e-6f) Float.MAX_VALUE else abs(1f / dir.y)
        val deltaDistZ = if (abs(dir.z) < 1e-6f) Float.MAX_VALUE else abs(1f / dir.z)

        // Calculate distance to next voxel boundary
        var sideDistX = if (stepX > 0) {
            (voxelX + 1f - currentX) * deltaDistX
        } else if (stepX < 0) {
            (currentX - voxelX) * deltaDistX
        } else {
            Float.MAX_VALUE
        }

        var sideDistY = if (stepY > 0) {
            (voxelY + 1f - currentY) * deltaDistY
        } else if (stepY < 0) {
            (currentY - voxelY) * deltaDistY
        } else {
            Float.MAX_VALUE
        }

        var sideDistZ = if (stepZ > 0) {
            (voxelZ + 1f - currentZ) * deltaDistZ
        } else if (stepZ < 0) {
            (currentZ - voxelZ) * deltaDistZ
        } else {
            Float.MAX_VALUE
        }

        var hitSide = -1
        var hitFace = 0
        var travelDistance = 0f
        val dirLength = dir.length()

        while (travelDistance < maxDistance) {
            // Check bounds first
            if (voxelX < 0 || voxelX >= world.size.first ||
                voxelY < 0 || voxelY >= world.size.second ||
                voxelZ < 0 || voxelZ >= world.size.third
            ) {
                break
            }

            // Check if current voxel is solid
            val index = voxelX * world.size.second * world.size.third + voxelY * world.size.third + voxelZ
            val block = world.blocks[index]
            if (!block.isAir && hitSide != -1) {
                // We hit a solid block, calculate hit details
                var hitDistance = 0f
                var normal = Vec3(0f, 0f, 0f)

                // Calculate exact hit point
                var hitPoint = dir.mul(travelDistance).plus(Vec3(ray.origin.x.wrapTo01(), ray.origin.y.wrapTo01(), ray.origin.z.wrapTo01()))
                val directionSign = ray.direction.sign()

                // Calculate UV coordinates - relative position on the block face (0 to 1)
                val uv = when (hitSide) {
                    0 -> { // X face - use Y and Z coordinates relative to block
                        normal = Vec3(-stepX.toFloat(), 0f, 0f)
                        hitPoint = Vec3(hitPoint.x.roundToInt().toFloat(), hitPoint.y, hitPoint.z)
                        if (directionSign.x < 1) {
                            val localY = 1f - (hitPoint.y - voxelY.toFloat())
                            val localZ = 1f - (hitPoint.z - voxelZ.toFloat())
                            Vec2(localZ, localY)
                        } else { //przod
                            val localY = 1f - (hitPoint.y - voxelY.toFloat())
                            val localZ = 1f - (hitPoint.z - voxelZ.toFloat())
                            Vec2(localZ, localY)
                        }
                    }

                    1 -> { // Y face - use X and Z coordinates relative to block
                        normal = Vec3(0f, -stepY.toFloat(), 0f)
                        hitPoint = Vec3(hitPoint.x, hitPoint.y.roundToInt().toFloat(), hitPoint.z)
                        if (directionSign.y < 1) {  //dol
                            val localZ = 1f - (hitPoint.x - voxelX.toFloat())
                            val localX = 1f - (hitPoint.z - voxelZ.toFloat())
                            Vec2(localX, localZ)
                        } else { //gora
                            val localZ = 1f - (hitPoint.x - voxelX.toFloat())
                            val localX = (hitPoint.z - voxelZ.toFloat())
                            Vec2(localX, localZ)
                        }
                    }

                    2 -> { // Z face - use X and Y coordinates relative to block
                        normal = Vec3(0f, 0f, -stepZ.toFloat())
                        hitPoint = Vec3(hitPoint.x, hitPoint.y, hitPoint.z.roundToInt().toFloat())
                        if (directionSign.z < 1) { //lewo
                            val localX = -(1f - (hitPoint.y - voxelY.toFloat()))
                            val localY = -(hitPoint.x - voxelX.toFloat())
                            Vec2(localX, localY)
                        } else { //prawo
                            val localX = 1f - (hitPoint.y - voxelY.toFloat())
                            val localY = 1f - (hitPoint.x - voxelX.toFloat())
                            Vec2(localX, localY)
                        }
                    }

                    else -> {
                        Vec2(0.0f, 0.0f) // Default to center of face
                    }
                }


                val color = block.getColor(uv, Ray(hitPoint, ray.direction))//.min(distanceShadow)
                if (color.alpha != 0 && !(hitSide != 0 && (block.name == "poppy" || block.name == "short_grass"))) { // tutaj lepiej zrobić returnowanie czy cos dla kwiatka
                    val uv2 = Vec2(uv.x % 1, uv.y % 1)
                    val position = Vec3(voxelX.toFloat(), voxelY.toFloat(), voxelZ.toFloat()).plus(hitFaces[hitFace])
                        .plus(uv2.placeOnPlane(normal)).plus(Vec3.ONE)

                    val rayHit = previousRayHit ?: RayHit(
                        block,
                        position,
                        normal,
                        color,
                        1f
                    )

                    if (block.name == "glowstone") rayHit.incomingLight += 2f
//                    rayHit.color = rayHit.color.avg(color)

                    if (bouncesLeft == 0) {
                        return rayHit;
                    }

                    return sendRay(
                        world,
                        Ray(
                            position,
                            normal.randomOutwardVector()
                        ),
                        maxDistance,
                        bouncesLeft - 1,
                        rayHit
                    ) ?: rayHit
                }
            }


            // Move to next voxel
            if (sideDistX <= sideDistY && sideDistX <= sideDistZ) {
                travelDistance = sideDistX * dirLength
                sideDistX += deltaDistX
                voxelX += stepX
                hitSide = 0
                hitFace = if (stepX > 0) 1 else 2

            } else if (sideDistY <= sideDistZ) {
                travelDistance = sideDistY * dirLength
                sideDistY += deltaDistY
                voxelY += stepY
                hitSide = 1
                hitFace = if (stepY > 0) 3 else 4
            } else {
                travelDistance = sideDistZ * dirLength
                sideDistZ += deltaDistZ
                voxelZ += stepZ
                hitSide = 2
                hitFace = if (stepZ > 0) 5 else 6
            }
        }


        return null // No hit found
    }
}