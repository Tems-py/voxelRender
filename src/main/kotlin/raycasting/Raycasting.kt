package me.tems.raycasting

import me.tems.coords.Block
import me.tems.coords.Geometry
import me.tems.coords.Vec2
import me.tems.coords.Vec3
import me.tems.coords.angleBetween
import me.tems.coords.distanceTo
import me.tems.coords.length
import me.tems.coords.mul
import me.tems.coords.x
import me.tems.coords.y
import me.tems.coords.z
import me.tems.raycasting.InBlockRayCast.getFaceFromNormal
import me.tems.raycasting.InBlockRayCast.inBlockRayCast
import me.tems.utils.ColorUtils.avg
import me.tems.utils.ColorUtils.avgWeighted
import me.tems.utils.ColorUtils.mul
import me.tems.worlds.World
import java.awt.Color
import kotlin.math.*

object Raycasting {
    data class Ray(val origin: FloatArray, val direction: FloatArray)
    data class RayHit(
        val block: Block,
        val position: FloatArray,
        val face: FloatArray,
        var color: Color,
        var incomingLight: Float,
        var distance: Float
    )

    data class Hit(
        val hit2d: Vec2,
        val hit3d: FloatArray,
        val bouncedDirection: FloatArray,
        val distance: Float,
        val normal: FloatArray,
        val geometry: Geometry
    )

    data class ColorOutgoing(
        var color: Color,
        val outgoingRay: Ray,
        val hitNormal: FloatArray
    )

    // Pre-allocated axis-aligned normals — never mutated after creation.
    private val NORM_POS_X = floatArrayOf( 1f, 0f, 0f)
    private val NORM_NEG_X = floatArrayOf(-1f, 0f, 0f)
    private val NORM_POS_Y = floatArrayOf(0f,  1f, 0f)
    private val NORM_NEG_Y = floatArrayOf(0f, -1f, 0f)
    private val NORM_POS_Z = floatArrayOf(0f, 0f,  1f)
    private val NORM_NEG_Z = floatArrayOf(0f, 0f, -1f)

    private val hitFaces =
        arrayOf(
            Vec3.ZERO,          // 0
            Vec3(0f, 1f, 1f),  // X+
            Vec3(0f, 1f, 0f),  // X-
            Vec3(0f, 0f, 0f),  // Y+
            Vec3(1f, 1f, 1f),  // Y-
            Vec3(1f, 1f, 0f),  // Z+
            Vec3(1f, 0f, 1f),  // Z-
        )

    fun sendRay(
        world: World,
        ray: Ray,
        maxDistance: Float,
        bouncesLeft: Int,
        getSkyboxColor: (FloatArray) -> Color = { _ -> Color(126, 225, 252) }
    ): RayHit? {

        val dir = ray.direction

        val currentX = ray.origin.x
        val currentY = ray.origin.y
        val currentZ = ray.origin.z

        var voxelX = floor(currentX).toInt()
        var voxelY = floor(currentY).toInt()
        var voxelZ = floor(currentZ).toInt()

        val stepX = if (dir.x > 0) 1 else if (dir.x < 0) -1 else 0
        val stepY = if (dir.y > 0) 1 else if (dir.y < 0) -1 else 0
        val stepZ = if (dir.z > 0) 1 else if (dir.z < 0) -1 else 0

        val deltaDistX = if (abs(dir.x) < 1e-6f) Float.MAX_VALUE else abs(1f / dir.x)
        val deltaDistY = if (abs(dir.y) < 1e-6f) Float.MAX_VALUE else abs(1f / dir.y)
        val deltaDistZ = if (abs(dir.z) < 1e-6f) Float.MAX_VALUE else abs(1f / dir.z)

        var sideDistX = if (stepX > 0) (voxelX + 1f - currentX) * deltaDistX
                        else if (stepX < 0) (currentX - voxelX) * deltaDistX
                        else Float.MAX_VALUE

        var sideDistY = if (stepY > 0) (voxelY + 1f - currentY) * deltaDistY
                        else if (stepY < 0) (currentY - voxelY) * deltaDistY
                        else Float.MAX_VALUE

        var sideDistZ = if (stepZ > 0) (voxelZ + 1f - currentZ) * deltaDistZ
                        else if (stepZ < 0) (currentZ - voxelZ) * deltaDistZ
                        else Float.MAX_VALUE

        var hitSide = -1
        var hitFace = 0
        var travelDistance = 0f
        val dirLength = dir.length()

        while (travelDistance < maxDistance) {
            if (voxelX < 0 || voxelX >= world.size.first ||
                voxelY < 0 || voxelY >= world.size.second ||
                voxelZ < 0 || voxelZ >= world.size.third
            ) {
                break
            }

            val index = voxelX * world.size.second * world.size.third + voxelY * world.size.third + voxelZ
            val block = world.blocks[index]
            if (!block.isAir && hitSide != -1) {
                // Compute hitPoint in one step — no intermediate mul() array.
                val ox = ray.origin.x; val oy = ray.origin.y; val oz = ray.origin.z
                val hitPoint = floatArrayOf(
                    dir.x * travelDistance + ox,
                    dir.y * travelDistance + oy,
                    dir.z * travelDistance + oz
                )

                val normal: FloatArray
                val uv = when (hitSide) {
                    0 -> {
                        normal = if (stepX < 0) NORM_POS_X else NORM_NEG_X
                        hitPoint[0] = round(hitPoint[0])   // in-place rounding, no new array
                        if (ray.direction.x < 0) {
                            Vec2(hitPoint[2] - voxelZ, 1f - (hitPoint[1] - voxelY))
                        } else {
                            Vec2(1f - (hitPoint[2] - voxelZ), hitPoint[1] - voxelY)
                        }
                    }

                    1 -> {
                        normal = if (stepY < 0) NORM_POS_Y else NORM_NEG_Y
                        hitPoint[1] = round(hitPoint[1])
                        if (ray.direction.y < 0) {
                            Vec2(1f - (hitPoint[2] - voxelZ), 1f - (hitPoint[0] - voxelX))
                        } else {
                            Vec2(hitPoint[2] - voxelZ, 1f - (hitPoint[0] - voxelX))
                        }
                    }

                    2 -> {
                        normal = if (stepZ < 0) NORM_POS_Z else NORM_NEG_Z
                        hitPoint[2] = round(hitPoint[2])
                        if (ray.direction.z < 0) {
                            Vec2(hitPoint[0] - voxelX, 1f - (hitPoint[1] - voxelY))
                        } else {
                            Vec2(1f - (hitPoint[0] - voxelX), 1f - (hitPoint[1] - voxelY))
                        }
                    }

                    else -> {
                        normal = NORM_POS_Y
                        Vec2(0f, 0f)
                    }
                }

                val uv2 = Vec2((uv.x % 1 + 1) % 1, (uv.y % 1 + 1) % 1)
                val uvOnPlane = uv2.placeOnPlane(normal)

                // Compute position in one allocation instead of Vec3+add+add.
                val hf = hitFaces[hitFace]
                val position = floatArrayOf(
                    voxelX + hf.x + uvOnPlane.x,
                    voxelY + hf.y + uvOnPlane.y,
                    voxelZ + hf.z + uvOnPlane.z
                )

                // Compute inBlockPosition without creating an intermediate Vec3.
                val inBlockPosition = floatArrayOf(
                    hitPoint[0] - voxelX,
                    hitPoint[1] - voxelY,
                    hitPoint[2] - voxelZ
                )

                var (color, outRay, realNormal) = inBlockRayCast(
                    block,
                    uv,
                    Ray(inBlockPosition, ray.direction),
                    normal
                )

                if (color.alpha != 0) {
                    val nextHit = if (bouncesLeft > 0) {
                        sendRay(
                            world,
                            Ray(
                                // Compute bounce origin without intermediate Vec3.
                                floatArrayOf(
                                    voxelX + outRay.origin.x,
                                    voxelY + outRay.origin.y,
                                    voxelZ + outRay.origin.z
                                ),
                                outRay.direction
                            ),
                            maxDistance,
                            if (color.alpha != 0) bouncesLeft - 1 else bouncesLeft,
                            getSkyboxColor,
                        )
                    } else null

                    // Inline distance — avoids sub() + length() allocations.
                    val hitDistance = hitPoint.distanceTo(ray.origin)
                    val cumulativeDistance = nextHit?.distance?.plus(hitDistance) ?: hitDistance

                    var illumination = (nextHit?.incomingLight ?: 0f) + block.illumination * min(
                        1f,
                        1f - min(1f, (hitDistance / 50))
                    )
                    val angleBetween = ray.direction.angleBetween(normal)

                    if ((block.reflective <= 0.5f || ((angleBetween / PI.toFloat() * 180f).toInt() < 100))) {
                        color = nextHit?.color?.avg(color) ?: color
                    }

                    if (nextHit != null) {
                        if (color.alpha != 255 && nextHit.color != color) {
                            color = nextHit.color.avg(color)
                        }
                    }

                    if (nextHit == null && bouncesLeft > 0) {
                        if (((angleBetween / PI.toFloat() * 180f).toInt() < 110) || block.reflective <= 0.5f)
                            color = color.avgWeighted(
                                getSkyboxColor(block.getReflectDirection(ray.direction, normal)),
                                7f,
                                1f
                            )
                        if (color.alpha != 255) {
                            color = color.avg(getSkyboxColor(ray.direction))
                        }

                        if (outRay.direction.z > 0 || outRay.direction.x > 0) illumination = 2f
                        else illumination += 0.1f
                    }

                    val mudkjpMultiplier = when (getFaceFromNormal(realNormal)) {
                        Geometry.FaceName.DOWN -> 0.5f
                        Geometry.FaceName.EAST -> 0.6f
                        Geometry.FaceName.WEST -> 0.6f
                        Geometry.FaceName.NORTH -> 0.8f
                        Geometry.FaceName.SOUTH -> 0.8f
                        else -> 1.0f
                    }

                    return RayHit(
                        block,
                        position,
                        normal,
                        color.mul(mudkjpMultiplier),
                        illumination,
                        cumulativeDistance
                    )
                }
            }

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

        return null
    }
}
