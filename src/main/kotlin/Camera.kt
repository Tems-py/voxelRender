package org.example

import org.example.coords.Block
import org.example.coords.Vec3
import org.example.raycasting.Raycasting
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.tan

class Camera(var position: Vec3, var rotation: Vec3, val fov: Float = 90f, val world: Array<Block>) {
    private val SCREEN_SIZE = Pair(1920, 1080)
    private var viewVectors = getViewVectors()

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

    fun rotateCamera(newRotation: Vec3) {
        rotation = newRotation
        viewVectors = getViewVectors()
    }

    fun sendRays(): BufferedImage {
        val hitColors = Array<Array<Color?>>(SCREEN_SIZE.first) { Array(SCREEN_SIZE.second) { null } }
        for ((x, line) in viewVectors.withIndex()) {
            for ((y, ray) in line.withIndex()) {

                val rayHitColor = Raycasting.raycast(world, Ray(position, ray), 100f, 3,16)

                if (rayHitColor != null) {
                    hitColors[x][y] = rayHitColor
                }
            }
        }

        return generateImage(hitColors, 1)
    }


    fun generateImage(image: Array<Array<Color?>>, blockSize: Int = 1): BufferedImage {
        val width = image.size * blockSize
        val height = image[0].size * blockSize
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (x in image.indices) {
            for (y in image[0].indices) {
                val hit = image[x][y]
                if (hit == null) {
                    bufferedImage.setRGB(x, y, Color(126, 225, 252).rgb)

                    continue
                }
//                val shadowColor =
//                    Color(abs(hit.face.x.toInt()) * 13, abs(hit.face.y.toInt()) * 13, abs(hit.face.z.toInt()) * 13)

                bufferedImage.setRGB(x, y, hit.rgb)
            }
        }

        return bufferedImage
    }

    data class Ray(val origin: Vec3, val direction: Vec3)
    data class RayHit(
        val block: Block,
        val position: Vec3, // voxel coords
        val face: Vec3, // normal of the face hit
        var color: Color,
    )

    val worldSizeX = 201
    val worldSizeY = 89
    val worldSizeZ = 101


}