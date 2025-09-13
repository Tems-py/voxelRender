package org.example

import org.example.textures.BlockColor
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import kotlin.math.abs

class Camera(val position: Vec3, val fov: Float = 90f, val world: Array<Array<Array<Block>>>) {
    private val SCREEN_SIZE = Pair(800, 600)
    private val viewVectors = getViewVectors()

    fun getViewVectors(): Array<Array<Vec3>> {
        val list = Array<Array<Vec3>>(SCREEN_SIZE.first) { Array(SCREEN_SIZE.second) { Vec3.ZERO } }
        for (x in 0..<SCREEN_SIZE.first) {
            for (z in 0..<SCREEN_SIZE.second) {
                val vector = Vec3(x.toFloat() - SCREEN_SIZE.first / 2, 50.0f, z.toFloat() - SCREEN_SIZE.second / 2)
                list[x][z] = vector.normalize()
            }
        }

        return list
    }

    fun sendRays() {
        val hitValues = Array(SCREEN_SIZE.first) { Array(SCREEN_SIZE.second) { BlockColor.blankColor } }
        for ((x, line) in viewVectors.withIndex()) {
            for ((y, ray) in line.withIndex()) {

                val block = raycast(world, Ray(position, ray), 100f)

                if (block != null) {
                    println("$block $x, $y, $ray")
                    hitValues[x][y] = block.color
                }

            }
        }

        println(hitValues.size)

        val image = generateImage(hitValues, 2)
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

    fun generateImage(image: Array<Array<BlockColor.ViewColor>>, blockSize: Int = 2): BufferedImage {
        val width = image.size * blockSize
        val height = image[0].size * blockSize
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = bufferedImage.createGraphics()

        for (x in image.indices) {
            for (y in image[0].indices) {
                graphics.color = image[x][y].getJavaColor()
                graphics.fillRect(x * blockSize, y * blockSize, blockSize, blockSize)
            }
        }

        graphics.dispose()
        return bufferedImage
    }

    data class Ray(val origin: Vec3, val direction: Vec3)

    fun raycast(
        world: Array<Array<Array<Block>>>,
        ray: Ray,
        maxDistance: Float
    ): Block? {
        val sizeX = world.size
        val sizeY = world[0].size
        val sizeZ = world[0][0].size

        // Current voxel position
        var x = ray.origin.x.toInt()
        var y = ray.origin.y.toInt()
        var z = ray.origin.z.toInt()

        val dirX = ray.direction.x
        val dirY = ray.direction.y
        val dirZ = ray.direction.z

        // Step in each direction
        val stepX = if (dirX > 0) 1 else if (dirX < 0) -1 else 0
        val stepY = if (dirY > 0) 1 else if (dirY < 0) -1 else 0
        val stepZ = if (dirZ > 0) 1 else if (dirZ < 0) -1 else 0

        // Distance to next boundary
        fun intBound(s: Float, ds: Float): Float {
            if (ds > 0) {
                return ((s + 1.0f) - s) / ds
            } else if (ds < 0) {
                return (s - s) / -ds
            }
            return Float.MAX_VALUE
        }

        var tMaxX = if (stepX != 0) intBound(ray.origin.x, dirX) else Float.MAX_VALUE
        var tMaxY = if (stepY != 0) intBound(ray.origin.y, dirY) else Float.MAX_VALUE
        var tMaxZ = if (stepZ != 0) intBound(ray.origin.z, dirZ) else Float.MAX_VALUE

        val tDeltaX = if (stepX != 0) 1.0f / abs(dirX) else Float.MAX_VALUE
        val tDeltaY = if (stepY != 0) 1.0f / abs(dirY) else Float.MAX_VALUE
        val tDeltaZ = if (stepZ != 0) 1.0f / abs(dirZ) else Float.MAX_VALUE

        var dist = 0.0f

        while (x in 0 until sizeX && y in 0 until sizeY && z in 0 until sizeZ && dist <= maxDistance) {
            val block = world[x][y][z]
            if (block != Block.air) {
                return block // Hit a block
            }

            // Advance to next voxel
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX
                    dist = tMaxX
                    tMaxX += tDeltaX
                } else {
                    z += stepZ
                    dist = tMaxZ
                    tMaxZ += tDeltaZ
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY
                    dist = tMaxY
                    tMaxY += tDeltaY
                } else {
                    z += stepZ
                    dist = tMaxZ
                    tMaxZ += tDeltaZ
                }
            }
        }

        return null // No hit
    }
}