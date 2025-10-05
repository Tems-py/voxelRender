package org.example.raycasting

import org.example.coords.Block
import org.example.coords.Vec2
import org.example.coords.Vec3
import org.example.utils.ColorUtils.avg
import org.example.utils.ColorUtils.mul
import org.example.worlds.World
import java.awt.Color
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min

object Raycasting {

    data class Ray(val origin: Vec3, val direction: Vec3)
    data class RayHit(
        val block: Block,
        val position: Vec3, // voxel coords
        val face: Vec3, // normal of the face hit
        var color: Color,
        var incomingLight: Float
    )

    val hitFaces =
        arrayOf(
            Vec3.ZERO, // 0
            Vec3(0f, 1f, 1f), // X+
            Vec3(0f, 1f, 0f), // X-
            Vec3(0f, 0f, 0f), // Y+
            Vec3(1f, 1f, 1f), // Y-
            Vec3(1f, 1f, 0f), // Z+
            Vec3(1f, 0f, 1f), // Z-
        )
//    arrayOf(Vec3.ZERO, Vec3(-1f, 0f, 0f), Vec3.ZERO, Vec3(0f, -1f, 0f), Vec3.ZERO, Vec3(0f, 0f, -1f), Vec3.ZERO)
    // tutaj mozliwe ze rozwaliłem raycasting, dlatego zostawiam to jako poprzednia wartosc ktora dzialala dla raycastingu

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
                var hitPoint = dir.mul(travelDistance).plus(Vec3(ray.origin.x, ray.origin.y, ray.origin.z))
                val directionSign = ray.direction.sign()

                // Calculate UV coordinates - relative position on the block face (0 to 1)
                val uv = when (hitSide) {
                    0 -> { // X face - use Y and Z coordinates relative to block
                        normal = Vec3(-stepX.toFloat(), 0f, 0f)
//                        hitPoint = Vec3(round(hitPoint.x), hitPoint.y, hitPoint.z)
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
//                        hitPoint = Vec3(hitPoint.x, round(hitPoint.y), hitPoint.z)
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
//                        hitPoint = Vec3(hitPoint.x, hitPoint.y, round(hitPoint.z))
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


                val uv2 = Vec2((uv.x % 1 + 1)  % 1, (uv.y % 1 + 1)  % 1)
                val uvOnPlane = uv2.placeOnPlane(normal)

                val position = Vec3(voxelX.toFloat(), voxelY.toFloat(), voxelZ.toFloat()).plus(hitFaces[hitFace])
                    .plus(uvOnPlane)

                // hitface 1-6 (0 error)
                // nie dziala dla -Z (6?)
                val inBlockPosition = hitFaces[hitFace].plus(uvOnPlane)

                val (color,outRay) = block.getColor(uv, Ray(inBlockPosition, ray.direction),normal)//.min(distanceShadow)

                if (color.alpha != 0 && !(hitSide != 0 && (block.name == "poppy" || block.name == "short_grass"))) { // tutaj lepiej zrobić returnowanie czy cos dla kwiatka
                    val rayHit = previousRayHit ?: RayHit(
                        block,
                        position,
                        normal,
                        color,
                        0f
                    )

                    if (block.name == "glowstone") rayHit.incomingLight += 2f
//                    rayHit.color = rayHit.color.avg(color)

                    if (bouncesLeft == 0) {
                        return rayHit;
                    }
                    return sendRay(
                        world,
                        Ray(
                            Vec3(voxelX.toFloat(), voxelY.toFloat(), voxelZ.toFloat()).plus(outRay.origin),
                            outRay.direction
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