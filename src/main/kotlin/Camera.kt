package org.example

import org.example.coords.Block
import org.example.coords.Vec2
import org.example.coords.Vec3
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.tan

class Camera(val position: Vec3, val rotation: Vec3, val fov: Float = 90f, val world: Array<Block>) {
    private val SCREEN_SIZE = Pair(1920, 1080)
    private val viewVectors = getViewVectors()

    fun getViewVectors(): Array<Array<Vec3>> {
        val list = Array<Array<Vec3>>(SCREEN_SIZE.first) { Array(SCREEN_SIZE.second) { Vec3.ZERO } }


        val vecDist = tan(fov * Math.PI / 360).toFloat()
        for (x in 0..<SCREEN_SIZE.first) {
            for (z in 0..<SCREEN_SIZE.second) {
                val vector = Vec3(
                    -(x.toFloat() - SCREEN_SIZE.first / 2) * vecDist,
                    -(z.toFloat() - SCREEN_SIZE.second / 2) * vecDist,
                    SCREEN_SIZE.first.toFloat() / 2
                ).rotate(rotation)
                list[x][z] = vector.normalize()
            }
        }

        return list
    }

    fun sendRays(): BufferedImage {
        val hitValues = Array<Array<RayHit?>>(SCREEN_SIZE.first) { Array(SCREEN_SIZE.second) { null } }
        for ((x, line) in viewVectors.withIndex()) {
            for ((y, ray) in line.withIndex()) {

                val rayHit = raycast(world, Ray(position, ray), 50f)

                if (rayHit != null) {
                    hitValues[x][y] = rayHit
                }
            }
        }

        return generateImage(hitValues, 1)
    }



    fun generateImage(image: Array<Array<RayHit?>>, blockSize: Int = 1): BufferedImage {
        val width = image.size * blockSize
        val height = image[0].size * blockSize
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)


        for (x in image.indices) {
            for (y in image[0].indices) {
                val hit = image[x][y] ?: continue
//                val shadowColor =
//                    Color(abs(hit.face.x.toInt()) * 13, abs(hit.face.y.toInt()) * 13, abs(hit.face.z.toInt()) * 13)
                val distance = (hit.distance / 150f)
                val distanceShadow = Color(distance, distance, distance)
                val color = hit.block.getColor(hit.uv).min(distanceShadow)
                bufferedImage.setRGB(x, y, color.rgb)

            }
        }

        return bufferedImage
    }

    fun Color.mul(color: Color): Color {
        return Color(kotlin.math.min(255, this.red * color.red), kotlin.math.min(255, this.green * color.green), kotlin.math.min(255, this.blue * color.blue))
    }

    fun Color.add(color: Color): Color {
        return Color(kotlin.math.min(255, this.red + color.red), kotlin.math.min(255, this.green + color.green), kotlin.math.min(255, this.blue + color.blue))
    }

    fun Color.min(color: Color): Color {
        return Color(max(0, this.red - color.red), max(0, this.green - color.green), max(0, this.blue - color.blue))
    }

    data class Ray(val origin: Vec3, val direction: Vec3)
    data class RayHit(
        val block: Block,
        val position: Vec3, // voxel coords
        val face: Vec3, // normal of the face hit
        val uv: Vec2,
        val distance: Float = 20f
    )

    fun raycast(
        world: Array<Block>,
        ray: Ray,
        maxDistance: Float
    ): RayHit? {
        val worldSizeX = 60
        val worldSizeY = 20
        val worldSizeZ = 60

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
            val index = voxelX * 60 * 20 + voxelY * 60 + voxelZ
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
                val hitPoint = dir.mul(travelDistance)

                // Calculate UV coordinates - relative position on the block face (0 to 1)
                val uv = when (hitSide) {
                    0 -> { // X face - use Y and Z coordinates relative to block
                        val localY = hitPoint.y - voxelY.toFloat()
                        val localZ = hitPoint.z - voxelZ.toFloat()
                        Vec2(localY, localZ)
                    }

                    1 -> { // Y face - use X and Z coordinates relative to block
                        val localX = hitPoint.x - voxelX.toFloat()
                        val localZ = hitPoint.z - voxelZ.toFloat()
                        Vec2(localX, localZ)
                    }

                    2 -> { // Z face - use X and Y coordinates relative to block
                        val localX = hitPoint.x - voxelX.toFloat()
                        val localY = hitPoint.y - voxelY.toFloat()
                        Vec2(localX, localY)
                    }

                    else -> Vec2(0.5f, 0.5f) // Default to center of face
                }

                return RayHit(
                    block = block,
                    position = Vec3(voxelX.toFloat(), voxelY.toFloat(), voxelZ.toFloat()),
                    face = normal,
                    uv = uv,
                    distance = hitDistance
                )
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