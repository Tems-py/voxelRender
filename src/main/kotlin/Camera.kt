package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.example.coords.Block
import org.example.coords.Vec3
import org.example.raycasting.Raycasting
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.tan

class Camera(var position: Vec3, var rotation: Vec3, val fov: Float = 90f, val world: Array<Block>) {
    private val SCREEN_SIZE = Pair(1980, 1080)
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

    fun sendRays(): BufferedImage = runBlocking {
        val hitColors = Array(SCREEN_SIZE.first) { Array<Color?>(SCREEN_SIZE.second) { null } }

        val jobs = viewVectors.mapIndexed { x, line ->
            async(Dispatchers.Default) {
                val columnHits = Array<Color?>(SCREEN_SIZE.second) { null }
                for ((y, ray) in line.withIndex()) {
                    val rayHitColor = Raycasting.raycast(
                        world,
                        Raycasting.Ray(position, ray),
                        10f,
                        5,
                        10
                    )
                    if (rayHitColor != null) {
                        columnHits[y] = rayHitColor
                    }
                }
                x to columnHits
            }
        }

        jobs.awaitAll().forEach { (x, columnHits) ->
            hitColors[x] = columnHits
        }

        generateImage(hitColors, 1)
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
}