package me.tems.raycasting

import me.tems.coords.Block
import me.tems.coords.Geometry
import me.tems.coords.Geometry.FaceName.*
import me.tems.coords.Vec2
import me.tems.coords.Vec3
import me.tems.coords.distanceTo
import me.tems.coords.fixFloatingPointError
import me.tems.coords.mul
import me.tems.coords.randomOutwardVector
import me.tems.coords.rotateAroundPivotReversed
import me.tems.coords.x
import me.tems.coords.y
import me.tems.coords.z
import me.tems.raycasting.Raycasting.ColorOutgoing
import me.tems.raycasting.Raycasting.Hit
import me.tems.raycasting.Raycasting.Ray
import me.tems.textures.TexturesManager.Companion.getColorFromTexture
import java.awt.Color
import kotlin.math.abs

object InBlockRayCast {

    // Pre-allocated axis-aligned normals — never mutated after creation.
    private val NORM_NEG_Y = floatArrayOf(0f, -1f, 0f)
    private val NORM_POS_Y = floatArrayOf(0f,  1f, 0f)
    private val NORM_NEG_X = floatArrayOf(-1f, 0f, 0f)
    private val NORM_POS_X = floatArrayOf( 1f, 0f, 0f)
    private val NORM_NEG_Z = floatArrayOf(0f, 0f, -1f)
    private val NORM_POS_Z = floatArrayOf(0f, 0f,  1f)

    // Thread-local scratch for the hit-position calculation in geometryHit —
    // avoids allocating a new FloatArray(3) per plane per geometry per ray.
    private val hitPosScratch = ThreadLocal.withInitial { FloatArray(3) }

    fun inBlockRayCast(block: Block, uv: Vec2, ray: Ray, normal: FloatArray): ColorOutgoing {
        val uvMap = Pair(Vec2(0f, 0f), Vec2(16f, 16f))

        var rayOutPosition = ray.origin
        var rayOutDirection = block.getReflectDirection(ray.direction, normal)

        var textureName = block.name

        fun geometryHit(startPosition: FloatArray, direction: FloatArray, geometry: Geometry): List<Hit> {
            // Use pre-computed rotated AABB bounds — no recomputation or allocation.
            val from = geometry.realFrom
            val to   = geometry.realTo

            val hits = mutableListOf<Hit>()
            val hp = hitPosScratch.get()   // scratch for hit position

            // ── Y plane ───────────────────────────────────────────────────────
            if (direction.y != 0f) {
                val invAbsY = 1f / abs(direction.y)
                val depth: Float
                val normY: FloatArray
                if (direction.y > 0) {
                    depth = from.y / 16f - startPosition.y
                    normY = NORM_NEG_Y
                } else {
                    depth = startPosition.y - to.y / 16f
                    normY = NORM_POS_Y
                }
                hp[0] = startPosition.x + direction.x * invAbsY * depth
                hp[1] = startPosition.y + (if (direction.y > 0) 1f else -1f) * depth
                hp[2] = startPosition.z + direction.z * invAbsY * depth
                if (geometry.checkIfInsideBlock(hp)) hits.add(
                    Hit(
                        Vec2(hp[2], hp[0]),
                        floatArrayOf(hp[0], hp[1], hp[2]),
                        floatArrayOf(direction.x, -direction.y, direction.z),
                        hp.distanceTo(startPosition),
                        normY,
                        geometry
                    )
                )
            }

            // ── X plane ───────────────────────────────────────────────────────
            if (direction.x != 0f) {
                val invAbsX = 1f / abs(direction.x)
                val depth: Float
                val normX: FloatArray
                if (direction.x > 0) {
                    depth = from.x / 16f - startPosition.x
                    normX = NORM_NEG_X
                } else {
                    depth = startPosition.x - to.x / 16f
                    normX = NORM_POS_X
                }
                hp[0] = startPosition.x + (if (direction.x > 0) 1f else -1f) * depth
                hp[1] = startPosition.y + direction.y * invAbsX * depth
                hp[2] = startPosition.z + direction.z * invAbsX * depth
                if (geometry.checkIfInsideBlock(hp)) hits.add(
                    Hit(
                        Vec2(hp[2], hp[1]),
                        floatArrayOf(hp[0], hp[1], hp[2]),
                        floatArrayOf(-direction.x, direction.y, direction.z),
                        hp.distanceTo(startPosition),
                        normX,
                        geometry
                    )
                )
            }

            // ── Z plane ───────────────────────────────────────────────────────
            if (direction.z != 0f) {
                val invAbsZ = 1f / abs(direction.z)
                val depth: Float
                val normZ: FloatArray
                if (direction.z > 0) {
                    depth = from.z / 16f - startPosition.z
                    normZ = NORM_NEG_Z
                } else {
                    depth = startPosition.z - to.z / 16f
                    normZ = NORM_POS_Z
                }
                hp[0] = startPosition.x + direction.x * invAbsZ * depth
                hp[1] = startPosition.y + direction.y * invAbsZ * depth
                hp[2] = startPosition.z + (if (direction.z > 0) 1f else -1f) * depth
                if (geometry.checkIfInsideBlock(hp)) hits.add(
                    Hit(
                        Vec2(hp[0], hp[1]),
                        floatArrayOf(hp[0], hp[1], hp[2]),
                        floatArrayOf(direction.x, direction.y, -direction.z),
                        hp.distanceTo(startPosition),
                        normZ,
                        geometry
                    )
                )
            }

            return hits
        }


        if (!block.isFull) {
            val startPosition = ray.origin.fixFloatingPointError()

            var foundGeometry: Geometry? = null

            for (geometry in block.geometries) {
                if (geometry.checkIfInsideBlock(startPosition)) {
                    foundGeometry = geometry
                    break
                }
            }
            if (foundGeometry != null) {
                val hitFace = getFaceFromNormal(normal)
                textureName =
                    foundGeometry.faces[hitFace]?.texture ?: foundGeometry.textures[hitFace.toString().lowercase()]
                            ?: foundGeometry.textures["all"] ?: foundGeometry.textures.toList().first().second
                val calculatedColor = getColorFromTexture(uv, textureName, uvMap)
                if (calculatedColor.alpha != 0) {
                    return ColorOutgoing(
                        calculatedColor,
                        Ray(rayOutPosition, rayOutDirection),
                        normal
                    )
                }
            }
            val hits = mutableListOf<Hit>()
            for (geometry in block.geometries) {
                val hitsInGeometry = geometryHit(ray.origin, ray.direction, geometry)
                for (hit in hitsInGeometry) {
                    hits.add(hit)
                }
            }

            if (hits.isEmpty()) {
                return ColorOutgoing(
                    Color(0, 0, 0, 0),
                    Ray(rayOutPosition, rayOutDirection),
                    normal
                )
            }

            hits.sortBy { it.distance }

            for (hit in hits) {
                val randomBouncedDirection = hit.normal.randomOutwardVector()
                foundGeometry = hit.geometry
                rayOutPosition = hit.hit3d
                rayOutDirection = randomBouncedDirection

                val hitNormalRotated =
                    hit.normal.rotateAroundPivotReversed(foundGeometry.rotation.mul(-1f), Vec3(0f, 0f, 0f))
                        .fixFloatingPointError()
                val hitFace = getFaceFromNormal(hitNormalRotated)
                textureName = foundGeometry.faces[hitFace]?.texture
                    ?: foundGeometry.textures[hitFace.toString().lowercase()]
                    ?: foundGeometry.textures["all"]
                    ?: foundGeometry.textures.values.firstOrNull()
                    ?: textureName
                val color = getColorFromTexture(hit.hit2d, textureName, uvMap)
                if (color.alpha != 0) {
                    return ColorOutgoing(
                        color,
                        Ray(rayOutPosition, rayOutDirection),
                        hitNormalRotated
                    )
                }
            }
        }

        return ColorOutgoing(
            getColorFromTexture(uv, textureName, uvMap),
            Ray(rayOutPosition, rayOutDirection),
            normal
        )
    }

    fun getFaceFromNormal(normal: FloatArray): Geometry.FaceName {
        return if (normal.x > 0) EAST
        else if (normal.x < 0) WEST
        else if (normal.y > 0) UP
        else if (normal.y < 0) DOWN
        else if (normal.z > 0) SOUTH
        else NORTH
    }
}
