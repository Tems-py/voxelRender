package org.example

import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

class Camera(val position: Vec3, val rotation: Vec2, val fov: Float = 90f, val world: Array<Array<Array<Block>>>) {
    private val SCREEN_SIZE = Pair(800, 600)
    private val viewVectors = getViewVectors()

    fun getViewVectors(): Array<Array<Vec3>> {
        val list = Array<Array<Vec3>>(SCREEN_SIZE.first) { Array(SCREEN_SIZE.second) { Vec3.ZERO } }

        val vecDist = tan(fov / 2)
        println(vecDist)
        for (x in 0..<SCREEN_SIZE.first) {
            for (z in 0..<SCREEN_SIZE.second) {
                val vector = Vec3((x.toFloat() - SCREEN_SIZE.first / 2) * vecDist, SCREEN_SIZE.first.toFloat() / 2, (z.toFloat() - SCREEN_SIZE.second / 2) * vecDist)
                list[x][z] = vector.normalize()
            }
        }

        return list
    }

    fun sendRays() {
        val hitValues = Array<Array<RayHit?>>(SCREEN_SIZE.first) { Array(SCREEN_SIZE.second) { null } }
        for ((x, line) in viewVectors.withIndex()) {
            for ((y, ray) in line.withIndex()) {

                val rayHit = raycast(world, Ray(position, ray), 100f)

                if (rayHit != null) {
                    hitValues[x][y] = rayHit
                }

            }
        }


        val image = generateImage(hitValues, 1)
        showImage(image)
    }

    fun showImage(image: BufferedImage) {
        val frame = JFrame("Image Viewer")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.contentPane.add(JLabel(ImageIcon(image)))
        frame.pack()
        frame.isVisible = true
        frame.setSize(image.width, image.height)
    }

    fun generateImage(image: Array<Array<RayHit?>>, blockSize: Int = 2): BufferedImage {
        val width = image.size * blockSize
        val height = image[0].size * blockSize
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = bufferedImage.createGraphics()

        for (x in image.indices) {
            for (y in image[0].indices) {
                val hit = image[x][y] ?: continue
                val shadowColor = Color(abs(hit.face.x.toInt()) * 13, abs(hit.face.y.toInt()) * 13, abs(hit.face.z.toInt()) * 13)
                graphics.color = hit.block.color.getJavaColor().min(shadowColor)
                graphics.fillRect(x * blockSize, y * blockSize, blockSize, blockSize)
            }
        }

        graphics.dispose()
        return bufferedImage
    }

    fun Color.min(color: Color): Color {
        return Color(this.red - color.red, this.green - color.green, this.blue - color.blue)
    }

    data class Ray(val origin: Vec3, val direction: Vec3)
    data class RayHit(
        val block: Block,
        val position: Vec3, // voxel coords
        val face: Vec3, // normal of the face hit
    )

    fun raycast(
        world: Array<Array<Array<Block>>>,
        ray: Ray,
        maxDistance: Float
    ): RayHit? {
        val sizeX = world.size
        val sizeY = world[0].size
        val sizeZ = world[0][0].size

        var x = ray.origin.x.toInt()
        var y = ray.origin.y.toInt()
        var z = ray.origin.z.toInt()

        val dirX = ray.direction.x
        val dirY = ray.direction.y
        val dirZ = ray.direction.z

        val stepX = if (dirX > 0) 1 else if (dirX < 0) -1 else 0
        val stepY = if (dirY > 0) 1 else if (dirY < 0) -1 else 0
        val stepZ = if (dirZ > 0) 1 else if (dirZ < 0) -1 else 0

        fun intBound(s: Float, ds: Float): Float {
            if (ds > 0) return (s.toInt() + 1 - s) / ds
            if (ds < 0) return (s - s.toInt()) / -ds
            return Float.MAX_VALUE
        }

        var tMaxX = if (stepX != 0) intBound(ray.origin.x, dirX) else Float.MAX_VALUE
        var tMaxY = if (stepY != 0) intBound(ray.origin.y, dirY) else Float.MAX_VALUE
        var tMaxZ = if (stepZ != 0) intBound(ray.origin.z, dirZ) else Float.MAX_VALUE

        val tDeltaX = if (stepX != 0) 1f / abs(dirX) else Float.MAX_VALUE
        val tDeltaY = if (stepY != 0) 1f / abs(dirY) else Float.MAX_VALUE
        val tDeltaZ = if (stepZ != 0) 1f / abs(dirZ) else Float.MAX_VALUE

        var dist = 0f
        var hitFace = Vec3(0f, 0f, 0f)

        while (x in 0 until sizeX && y in 0 until sizeY && z in 0 until sizeZ && dist <= maxDistance) {
            val block = world[x][y][z]
            if (block != Block.air) {
                val hitPos = ray.origin.plus(ray.direction).mul(dist)


                return RayHit(block, Vec3(x.toFloat(), y.toFloat(), z.toFloat()), hitFace)
            }

            // Advance
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX
                    dist = tMaxX
                    tMaxX += tDeltaX
                    hitFace = Vec3((-stepX).toFloat(), 0f, 0f)
                } else {
                    z += stepZ
                    dist = tMaxZ
                    tMaxZ += tDeltaZ
                    hitFace = Vec3(0f, 0f, (-stepZ).toFloat())
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY
                    dist = tMaxY
                    tMaxY += tDeltaY
                    hitFace = Vec3(0f, (-stepY).toFloat(), 0f)
                } else {
                    z += stepZ
                    dist = tMaxZ
                    tMaxZ += tDeltaZ
                    hitFace = Vec3(0f, 0f, (-stepZ).toFloat())
                }
            }
        }

        return null
    }
}