package org.example.raycasting

import org.example.coords.Block
import org.example.coords.Vec2
import org.example.coords.Vec3
import org.example.utils.ColorUtils.avg
import org.example.utils.ColorUtils.mul
import java.awt.Color
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

object Raycasting {

    val worldSizeX = 201
    val worldSizeY = 89
    val worldSizeZ = 101

    data class Ray(val origin: Vec3, val direction: Vec3)
    data class RayHit(
        val block: Block,
        val position: Vec3, // voxel coords
        val face: Vec3, // normal of the face hit
        var color: Color,
        var incomingLight: Float
    )

    fun raycast(
        world: Array<Block>,
        ray: Ray,
        maxDistance: Float,
        bouncesLeft: Int,
        sampling: Int
    ): Color? {
        val colors = mutableListOf<Color>()
        var lightIncoming = 0.2f
        for (i in 0..sampling) {
            val rayHit = sendRay(world, ray, maxDistance, bouncesLeft) ?: continue
            lightIncoming += rayHit.incomingLight
            colors.add(rayHit.color)
        }
//        return Color(min(1f, lightIncoming / 5f), min(1f, lightIncoming / 5f), min(1f, lightIncoming / 5f))
        if (colors.isEmpty()) return null;
        return colors[0].avg(colors).mul(min(1f, lightIncoming / 5f))
    }

    fun sendRay(
        world: Array<Block>,
        ray: Ray,
        maxDistance: Float,
        bouncesLeft: Int
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
        var travelDistance = 0f
        val dirLength = dir.length()

        while (travelDistance < maxDistance) {
            // Check bounds first
            if (voxelX < 0 || voxelX >= worldSizeX ||
                voxelY < 0 || voxelY >= worldSizeY ||
                voxelZ < 0 || voxelZ >= worldSizeZ
            ) {
                break
            }

            // Check if current voxel is solid
            val index = voxelX * worldSizeY * worldSizeZ + voxelY * worldSizeZ + voxelZ
            val block = world[index]
            if (!block.isAir) {
                // We hit a solid block, calculate hit details
                var hitDistance = 0f
                var normal = Vec3(0f, 0f, 0f)

                when (hitSide) {
                    0 -> { // Hit X face
                        hitDistance = if (stepX > 0) {
                            (voxelX - ray.origin.x) / dir.x
                        } else {
                            (voxelX + 1f - ray.origin.x) / dir.x
                        }
                        normal = Vec3(-stepX.toFloat(), 0f, 0f)
                    }

                    1 -> { // Hit Y face
                        hitDistance = if (stepY > 0) {
                            (voxelY - ray.origin.y) / dir.y
                        } else {
                            (voxelY + 1f - ray.origin.y) / dir.y
                        }
                        normal = Vec3(0f, -stepY.toFloat(), 0f)
                    }

                    2 -> { // Hit Z face
                        hitDistance = if (stepZ > 0) {
                            (voxelZ - ray.origin.z) / dir.z
                        } else {
                            (voxelZ + 1f - ray.origin.z) / dir.z
                        }
                        normal = Vec3(0f, 0f, -stepZ.toFloat())
                    }

                    else -> { // First voxel we're checking
                        // If we start inside a solid block, use a default
                        hitDistance = 0f
                        normal = Vec3(0f, 1f, 0f) // Default up normal
                    }
                }

                // Calculate exact hit point
                val hitPoint = dir.mul(travelDistance).plus(Vec3(ray.origin.x % 1f, ray.origin.y % 1f, ray.origin.z % 1f))

                // Calculate UV coordinates - relative position on the block face (0 to 1)
                val uv = when (hitSide) {
                    0 -> { // X face - use Y and Z coordinates relative to block
                        val localY = 1f - -(hitPoint.y - voxelY.toFloat())
                        val localZ = 1f - (hitPoint.z - voxelZ.toFloat())
                        Vec2(localY, localZ)
                    }

                    1 -> { // Y face - use X and Z coordinates relative to block
                        val localX = hitPoint.x - voxelX.toFloat()
                        val localZ = hitPoint.z - voxelZ.toFloat()
                        Vec2(localX, localZ)
                    }

                    2 -> { // Z face - use X and Y coordinates relative to block
                        val localX = 1f - -(hitPoint.y - voxelY.toFloat())
                        val localY = hitPoint.x - voxelX.toFloat()
                        Vec2(localX, localY)
                    }

                    else -> {
                        Vec2(0.0f, 0.0f) // Default to center of face
                    }
                }


                val distance = (hitDistance / 600f)
//                val distanceShadow = Color(distance, distance, distance)
                val color = block.getColor(uv)//.min(distanceShadow)
                if (color.alpha != 0 && !(hitSide != 0 && (block.name == "poppy" || block.name == "short_grass"))) { // tutaj lepiej zrobić returnowanie czy cos dla kwiatka
                    val position = Vec3(voxelX.toFloat(), voxelY.toFloat(), voxelZ.toFloat())
                    val rayHit = RayHit(
                        block = block,
                        position,
                        face = normal,
                        color = color,
                        0.1f
                    );
                    val uv2 = Vec2(uv.x % 1, uv.y % 1)

                    if (bouncesLeft == 0)
                        return rayHit;


                    val nextRayHit = sendRay(
                        world,
                        Ray(
                            position.plus(normal).plus(uv2.placeOnPlane(normal)),
                            ray.direction.reflect(normal).mul(Vec3.random())
                        ),
                        maxDistance,
                        bouncesLeft - 1
                    )

                    if (nextRayHit == null) {
//                            rayHit.color = rayHit.color.avg(Color(255, 255, 255))
                        rayHit.incomingLight += 8f
                        return rayHit;
                    } else {
                        rayHit.color = rayHit.color.avg(nextRayHit.color)
                        return rayHit
                    }
                }
            }

            // Move to next voxel
            if (sideDistX <= sideDistY && sideDistX <= sideDistZ) {
                travelDistance = sideDistX * dirLength
                sideDistX += deltaDistX
                voxelX += stepX
                hitSide = 0
            } else if (sideDistY <= sideDistZ) {
                travelDistance = sideDistY * dirLength
                sideDistY += deltaDistY
                voxelY += stepY
                hitSide = 1
            } else {
                travelDistance = sideDistZ * dirLength
                sideDistZ += deltaDistZ
                voxelZ += stepZ
                hitSide = 2
            }
        }


        return null // No hit found
    }
}