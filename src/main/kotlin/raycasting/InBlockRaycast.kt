package org.example.raycasting

import org.example.coords.Block
import org.example.coords.Geometry
import org.example.coords.Geometry.FaceName.*
import org.example.coords.Line
import org.example.coords.Plane
import org.example.coords.Vec2
import org.example.coords.Vec3
import org.example.raycasting.Raycasting.ColorOutgoing
import org.example.raycasting.Raycasting.Hit
import org.example.raycasting.Raycasting.Ray
import org.example.textures.TexturesManager.Companion.getColorFromTexture
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object InBlockRayCast {
    fun inBlockRayCast(block: Block, uv: Vec2, ray: Ray, normal: Vec3): ColorOutgoing {
        val uvMap = Pair(Vec2(0f, 0f), Vec2(16f, 16f))

//        return ColorOutgoing(ray.origin.toColor(), Raycasting.Ray(Vec3.random(), Vec3.random()))

        var rayOutPosition = ray.origin
        var rayOutDirection = block.getReflectDirection(ray.direction, normal)

//        return ColorOutgoing(Color(if ((angleBetween / PI.toFloat() * 180f).toInt() < 100) 255 else 10, 0, 0), Raycasting.Ray(rayOutPosition, rayOutDirection))

        var textureName = block.name

        fun geometryHit(startPosition: Vec3, direction: Vec3, geometry: Geometry): List<Hit> {
            val from = geometry.from
            val to = geometry.to

            val planes = mutableListOf<Plane>(
                Plane(from,Vec3(-1f,0f,0f)).rotateAroundPivot(geometry.rotation,Vec3(8f)),
                Plane(from,Vec3(0f,-1f,0f)).rotateAroundPivot(geometry.rotation,Vec3(8f)),
                Plane(from,Vec3(0f,0f,-1f)).rotateAroundPivot(geometry.rotation,Vec3(8f)),
                Plane(to,Vec3(1f,0f,0f)).rotateAroundPivot(geometry.rotation,Vec3(8f)),
                Plane(to,Vec3(0f,1f,0f)).rotateAroundPivot(geometry.rotation,Vec3(8f)),
                Plane(to,Vec3(0f,0f,1f)).rotateAroundPivot(geometry.rotation,Vec3(8f))
            )

            val hits = mutableListOf<Hit>()
            var hitPosition: Vec3

            val line = Line(startPosition,direction)


            planes.forEach { it->
//                println(it.toString())
                hitPosition = it.lineIntercept(line)
                if(geometry.checkIfInsideBlock(hitPosition)){
                    hits.add(Hit(
                        Vec2(1f,1f),
                        hitPosition,
                        direction.mul(it.normal),
                        hitPosition.min(startPosition).abs().length(),
                        it.normal,
                        geometry
                    ))
                }
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
//                uvMap = foundGeometry.faces[hitFace]!!.uv
                textureName =
                    foundGeometry.faces[hitFace]?.texture ?: foundGeometry.textures[hitFace.toString().lowercase()]
                            ?: foundGeometry.textures["all"] ?: foundGeometry.textures.toList()
                        .first().second
                val calculatedColor = getColorFromTexture(uv, textureName, uvMap)
                if (calculatedColor.alpha != 0) {
                    return ColorOutgoing(
                        calculatedColor,
                        Ray(
                            rayOutPosition,
                            rayOutDirection
                        )
                    )
                }
            }
            val hits = mutableListOf<Hit>()
            for (geometry in block.geometries) {
                val hitsInGeometry =
                    geometryHit(ray.origin, ray.direction, geometry) // tutaj mozna for each uzyc ale to tam
                for (hit in hitsInGeometry) {
                    hits.add(hit)
                }
            }

            if (hits.isEmpty()) {
                return ColorOutgoing(
                    Color(0, 0, 0, 0),
                    Ray(rayOutPosition, rayOutDirection)
                )    // <= nic nie trafione
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
                        Ray(
                            rayOutPosition,
                            rayOutDirection
                        )
                    )
                }
//                    uvMap = foundGeometry.faces[hitFace]!!.uv
            }
        }
        //nic nie trafiono zwracamy po prostu kolor

        return ColorOutgoing(
            getColorFromTexture(uv, textureName, uvMap),
            Ray(
                rayOutPosition,
                rayOutDirection
            )
        )
    }

    private fun getFaceFromNormal(normal: Vec3): Geometry.FaceName {
        return if (normal.x > 0) {
            EAST
        } else if (normal.x < 0) {
            WEST
        } else if (normal.y > 0) {
            UP
        } else if (normal.y < 0) {
            DOWN
        } else if (normal.z > 0) {
            SOUTH
        } else {
            NORTH
        }
    }
}