package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.example.coords.Vec3
import org.example.raycasting.Raycasting
import org.example.utils.ColorUtils.avg
import org.example.utils.ColorUtils.avgWeighted
import org.example.utils.ColorUtils.mul
import org.example.worlds.World
import java.awt.Color
import java.awt.image.BufferedImage
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min
import kotlin.math.tan

data class CameraSettings(
    val fov: Float = 90f, // janku tutaj nie zmieniaj ustawień kamery OKOK
    val bounces: Int = 0, // janku tutaj nie zmieniaj ustawień kamery OKOK
    val screenSize: Pair<Int, Int> = Pair(1920, 1080) // janku tutaj nie zmieniaj ustawień kamery OKOK
)

class Camera(var position: Vec3, var rotation: Vec3, val settings: CameraSettings, val world: World) {
    private var viewVectors = generateViewVectors()
    val lastHits: Array<Array<Raycasting.RayHit?>> =
        Array(settings.screenSize.first) { Array<Raycasting.RayHit?>(settings.screenSize.second) { null } }
    val colorValues: Array<Array<Color>> =
        Array(settings.screenSize.first) { Array<Color>(settings.screenSize.second) { Color.BLACK } }
    val lightValues: Array<Array<Float>> = Array(settings.screenSize.first) { Array(settings.screenSize.second) { 0f } }
    var sample = 0

    fun generateViewVectors(): Array<Array<Vec3>> {
        val list = Array<Array<Vec3>>(settings.screenSize.first) { Array(settings.screenSize.second) { Vec3.ZERO } }

        val vecDist = tan(settings.fov * Math.PI / 360).toFloat()
        for (x in 0..<settings.screenSize.first) {
            for (z in 0..<settings.screenSize.second) {
                val vector = Vec3(
                    -(x.toFloat() - settings.screenSize.first / 2) * vecDist,
                    -(z.toFloat() - settings.screenSize.second / 2) * vecDist,
                    settings.screenSize.first.toFloat() / 2
                ).rotate(rotation)
                list[x][z] = vector.normalize()
            }
        }

        return list
    }

    fun move(newPosition: Vec3, newRotation: Vec3) {
        rotation = newRotation
        position = newPosition
        viewVectors = generateViewVectors()
        // reset previous frame data
        lightValues.forEachIndexed { index, _ ->
            lightValues[index] = Array(settings.screenSize.second) { 0f }
        }
        lastHits.forEachIndexed { index, _ ->
            lastHits[index] = Array<Raycasting.RayHit?>(settings.screenSize.second) { null }
        }
        sample = 0
    }

    fun sendRays(): Array<Array<Raycasting.RayHit?>> = runBlocking {
        val totalJobs = viewVectors.size
        val completed = AtomicInteger(0)
        val startTime = Instant.now()

        val numBatches = min(12, viewVectors.size)
        val batchSize = (viewVectors.size + numBatches - 1) / numBatches


        val jobs = (0 until numBatches).map { batchIndex ->
            async(Dispatchers.Default) {
                val start = batchIndex * batchSize
                val endExclusive = min(start + batchSize, viewVectors.size)
                val batchResults = mutableListOf<Pair<Int, Array<Raycasting.RayHit?>>>()

                for (x in start until endExclusive) {
                    val line = viewVectors[x]
                    val columnHits = Array<Raycasting.RayHit?>(settings.screenSize.second) { null }
                    for ((y, ray) in line.withIndex()) {
                        val rayHit = Raycasting.sendRay(
                            world,
                            Raycasting.Ray(position, ray),
                            100f,
                            settings.bounces
                        )
                        if (rayHit != null) {
                            columnHits[y] = rayHit
                        }
                    }
                    batchResults.add(x to columnHits)

                    // progress tracking (per column)
                    val done = completed.incrementAndGet()
                    val elapsed = Duration.between(startTime, Instant.now()).toMillis()
                    val avgPerJob = elapsed.toDouble() / done
                    val remaining = totalJobs - done
                    val etaMillis = (remaining * avgPerJob).toLong()
                    val eta = Duration.ofMillis(etaMillis)

                    if (done % 50 == 0 && false)
                        println(
                            "Finished column $x ($done/$totalJobs) " +
                                    "- Elapsed: ${elapsed / 1000.0}s, " +
                                    "ETA: ${eta.toSeconds()}s"
                        )
                }

                batchResults
            }
        }
        jobs.awaitAll().flatten().forEach { (x, columnHits) ->
            lastHits[x] = columnHits

            columnHits.forEachIndexed { y, rayHit ->
                lightValues[x][y] = (lightValues[x][y] * sample + (rayHit?.incomingLight ?: 0f)) / (sample + 1)
            }
        }
        sample += 1

        lastHits
    }

    fun getColors(): Array<Array<Color>> {
        val defaultColor = Color(126, 225, 252)
        val image: Array<Array<Color>> =
            Array(settings.screenSize.first) { Array(settings.screenSize.second) { defaultColor } }
        lastHits.forEachIndexed { x, rayHits ->
            rayHits.forEachIndexed { y, rayHit ->
                var color = rayHit?.color ?: return@forEachIndexed
                if (colorValues[x][y].rgb != -16777216)
                    color = color.avgWeighted(colorValues[x][y], 1.toFloat(), sample.toFloat())
                colorValues[x][y] = color
                image[x][y] = color.mul(color.alpha / 255f).mul(min(1f, lightValues[x][y]))
            }
        }
        return image
    }

    fun generateImage(): BufferedImage {
        val image = getColors()
        val bufferedImage =
            BufferedImage(settings.screenSize.first, settings.screenSize.second, BufferedImage.TYPE_INT_RGB)

        for (x in image.indices) {
            for (y in image[0].indices) {
                val hit = image[x][y]

                bufferedImage.setRGB(x, y, hit.rgb)
            }
        }

        return bufferedImage
    }
}