package me.tems.raycasting

import me.tems.coords.Block
import me.tems.coords.Geometry
import me.tems.coords.Geometry.FaceName.*
import me.tems.coords.Vec2
import me.tems.coords.Vec3
import me.tems.coords.abs
import me.tems.coords.add
import me.tems.coords.fixFloatingPointError
import me.tems.coords.length
import me.tems.coords.mul
import me.tems.coords.randomOutwardVector
import me.tems.coords.rotateAroundPivot
import me.tems.coords.rotateAroundPivotReversed
import me.tems.coords.sub
import me.tems.coords.x
import me.tems.coords.y
import me.tems.coords.z
import me.tems.raycasting.Raycasting.ColorOutgoing
import me.tems.raycasting.Raycasting.Hit
import me.tems.raycasting.Raycasting.Ray
import me.tems.textures.TexturesManager.Companion.getColorFromTexture
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object InBlockRayCast {
    fun inBlockRayCast(block: Block, uv: Vec2, ray: Ray, normal: FloatArray): ColorOutgoing {
        val uvMap = Pair(Vec2(0f, 0f), Vec2(16f, 16f))

        var rayOutPosition = ray.origin
        var rayOutDirection = block.getReflectDirection(ray.direction, normal)

        var textureName = block.name

        fun geometryHit(startPosition: FloatArray, direction: FloatArray, geometry: Geometry): List<Hit> {
            var from = geometry.from
            var to = geometry.to
            if (geometry.rotation.x != 0f || geometry.rotation.y != 0f || geometry.rotation.z != 0f) {
                to = to.rotateAroundPivot(geometry.rotation, Vec3(8f))
                from = from.rotateAroundPivot(geometry.rotation, Vec3(8f))
            }

            val realFrom = Vec3(x = min(a = from.x, b = to.x), y = min(a = from.y, b = to.y), z = min(a = from.z, b = to.z))
            val realTo   = Vec3(x = max(a = from.x, b = to.x), y = max(a = from.y, b = to.y), z = max(a = from.z, b = to.z))
            to = realTo
            from = realFrom

            val hits = mutableListOf<Hit>()
            var depthToTravel: Float
            var directionDivided: FloatArray
            var hitPosition: FloatArray
            var geometryNormal: FloatArray

            // Y plane
            if (direction.y > 0) {
                depthToTravel = from.y / 16f - startPosition.y
                geometryNormal = Vec3(0f, -1f, 0f)
            } else {
                depthToTravel = startPosition.y - to.y / 16f
                geometryNormal = Vec3(0f, 1f, 0f)
            }
            directionDivided = Vec3(direction.x / abs(direction.y), direction.y / abs(direction.y), direction.z / abs(direction.y))
            hitPosition = startPosition.add(directionDivided.mul(depthToTravel))
            if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                Hit(
                    Vec2(hitPosition.z, hitPosition.x),
                    hitPosition,
                    Vec3(direction.x, -direction.y, direction.z),
                    hitPosition.sub(startPosition).abs().length(),
                    geometryNormal,
                    geometry
                )
            )

            // X plane
            if (direction.x > 0) {
                depthToTravel = from.x / 16f - startPosition.x
                geometryNormal = Vec3(-1f, 0f, 0f)
            } else {
                depthToTravel = startPosition.x - to.x / 16f
                geometryNormal = Vec3(1f, 0f, 0f)
            }
            directionDivided = Vec3(direction.x / abs(direction.x), direction.y / abs(direction.x), direction.z / abs(direction.x))
            hitPosition = startPosition.add(directionDivided.mul(depthToTravel))
            if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                Hit(
                    Vec2(hitPosition.z, hitPosition.y),
                    hitPosition,
                    Vec3(-direction.x, direction.y, direction.z),
                    hitPosition.sub(startPosition).abs().length(),
                    geometryNormal,
                    geometry
                )
            )

            // Z plane
            if (direction.z > 0) {
                depthToTravel = from.z / 16f - startPosition.z
                geometryNormal = Vec3(0f, 0f, -1f)
            } else {
                depthToTravel = startPosition.z - to.z / 16f
                geometryNormal = Vec3(0f, 0f, 1f)
            }
            directionDivided = Vec3(direction.x / abs(direction.z), direction.y / abs(direction.z), direction.z / abs(direction.z))
            hitPosition = startPosition.add(directionDivided.mul(depthToTravel))
            if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                Hit(
                    Vec2(hitPosition.x, hitPosition.y),
                    hitPosition,
                    Vec3(direction.x, direction.y, -direction.z),
                    hitPosition.sub(startPosition).abs().length(),
                    geometryNormal,
                    geometry
                )
            )
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
                textureName = foundGeometry.faces[hitFace]!!.texture
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
