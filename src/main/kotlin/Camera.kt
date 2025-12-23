package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.example.coords.Vec3
import org.example.raycasting.Raycasting
import org.example.textures.TexturesManager
import org.example.utils.ColorUtils.avgWeighted
import org.example.utils.ColorUtils.mul
import org.example.worlds.World
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.random.Random

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
    val skyboxTexture = ImageIO.read(File("assets/skybox/stars.png"))

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
                            Raycasting.Ray(position.plus(Vec3.random().abs().mul(0.005f)), ray),
                            100f,
                            settings.bounces,
                            null,
                            ::getSkyboxColor
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
                lightValues[x][y] = (lightValues[x][y] * sample + (rayHit?.incomingLight ?: lightValues[x][y])) / (sample + 1)
            }
        }
        sample += 1

        lastHits
    }

    fun getColors(): Array<Array<Color>> {
        val image: Array<Array<Color>> =
            Array(settings.screenSize.first) { x -> Array(settings.screenSize.second) { y -> getSkyboxColor(viewVectors[x][y]) } }
        lastHits.forEachIndexed { x, rayHits ->
            rayHits.forEachIndexed { y, rayHit ->
                var color = rayHit?.color ?: return@forEachIndexed
                if (colorValues[x][y].rgb != -16777216)
                    color = color.avgWeighted(
                        colorValues[x][y],
                        lightValues[x][y],
                        1.3.pow(rayHit.incomingLight.toDouble()).toFloat()
                    )
                colorValues[x][y] = color
                image[x][y] = color.mul(color.alpha / 255f).mul(min(1f, lightValues[x][y]))
            }
        }
        return image
    }

    fun checkIfVectorTowardsSun(origin: Vec3, dir: Vec3, spherePos: Vec3, radius: Float): Boolean {
        val d = dir.normalize()
        val oc = origin.min(spherePos)

        val a = d.dot(d)
        val b = 2.0 * oc.dot(d)
        val c = oc.dot(oc) - radius * radius

        // discriminant of quadratic: b² - 4ac
        val discriminant = b * b - 4.0 * a * c

        if (discriminant < 0.0) return false // no intersection

        // find the nearest intersection t
        val t1 = (-b - sqrt(discriminant)) / (2.0 * a)
        val t2 = (-b + sqrt(discriminant)) / (2.0 * a)

        // if either intersection is in front of the origin (t ≥ 0)
        return t1 >= 0.0 || t2 >= 0.0
    }


    fun getSkyboxColor(vector: Vec3): Color {
        val normal = vector.normalize().abs()

        return Color(skyboxTexture.getRGB((normal.x * (skyboxTexture.width - 1)).toInt(), (normal.y * (skyboxTexture.height - 1)).toInt()), true)

//        val rand = Random((vector.x * 3281321 + vector.y * 8321687).toInt())
//
//        if (checkIfVectorTowardsSun(
//                position.plus(
//                    Vec3(
//                        rand.nextFloat() - 0.5f,
//                        rand.nextFloat() - 0.5f,
//                        rand.nextFloat() - 0.5f
//                    ).mul(0.02f)
//                ),
//                vector.plus(Vec3(
//                    rand.nextFloat() - 0.5f,
//                    rand.nextFloat() - 0.5f,
//                    rand.nextFloat() - 0.5f
//                ).mul(0.02f)),
//                Vec3(25f, 14f, -50f), 10f
//            )
//        ) {
//            return Color(249, 255, 135)
//        }
//
//        // return vector.toColor() tęcza
//        return if (vector.y + (rand.nextFloat() / 3) < 0) {
//            Color(155, 198, 232)
//        } else {
//            Color(66, 170, 255)
//        }

        return Color(155, 198, 232)
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