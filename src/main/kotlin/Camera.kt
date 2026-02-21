package me.tems

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import me.tems.coords.Vec2
import me.tems.coords.Vec3
import me.tems.raycasting.Raycasting
import me.tems.utils.ColorUtils.avgWeighted
import me.tems.utils.ColorUtils.mul
import me.tems.worlds.World
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.tan

data class CameraSettings(
    val fov: Float = 90f, // janku tutaj nie zmieniaj ustawień kamery OKOK
    val bounces: Int = 0, // janku tutaj nie zmieniaj ustawień kamery OKOK
    val screenSize: Pair<Int, Int> = Pair(1920, 1080) // janku tutaj nie zmieniaj ustawień kamery OKOK
)

class Camera(
    var position: Vec3,
    var rotation: Vec3,
    val settings: CameraSettings,
    val world: World,
    val skyboxTexture: BufferedImage
) {
    private var viewVectors = generateViewVectors()
    private val lastHits: Array<Array<Raycasting.RayHit?>> =
        Array(settings.screenSize.first) { Array(settings.screenSize.second) { null } }
    private var colorValues: Array<Array<Color>> =
        Array(settings.screenSize.first) { Array(settings.screenSize.second) { Color.BLACK } }
    private val lightValues: Array<Array<Float>> = Array(settings.screenSize.first) { Array(settings.screenSize.second) { 0f } }
    private var skyboxImage =
        Array(settings.screenSize.first) { x -> Array(settings.screenSize.second) { y -> Color.black } }
    private var sample = 0

    private fun generateViewVectors(): Array<Array<Vec3>> {
        val list = Array(settings.screenSize.first) { Array(settings.screenSize.second) { Vec3.ZERO } }

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

    init {
        skyboxImage = Array(settings.screenSize.first) { x -> Array(settings.screenSize.second) { y -> getSkyboxColor(viewVectors[x][y]) } }
    }

    @Suppress("unused")
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
        skyboxImage = Array(settings.screenSize.first) { x -> Array(settings.screenSize.second) { y -> getSkyboxColor(viewVectors[x][y]) } }
        colorValues = Array(settings.screenSize.first) { Array(settings.screenSize.second) { Color.BLACK } }
        sample = 0
    }

    fun sendRays(): Array<Array<Raycasting.RayHit?>> = runBlocking {
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
                            100f * settings.bounces,
                            settings.bounces,
                            ::getSkyboxColor
                        )
                        if (rayHit != null) {
                            columnHits[y] = rayHit
                        }
                    }
                    batchResults.add(x to columnHits)
                }

                batchResults
            }
        }
        jobs.awaitAll().flatten().forEach { (x, columnHits) ->
            lastHits[x] = columnHits

            columnHits.forEachIndexed { y, rayHit ->
                lightValues[x][y] =
                    (lightValues[x][y] * sample + (rayHit?.incomingLight ?: lightValues[x][y])) / (sample + 1)
            }
        }
        sample += 1

        lastHits
    }

    private fun getColors(): Array<Array<Color>> {
        val image: Array<Array<Color>> = skyboxImage.map { it.clone() }.toTypedArray()
        lastHits.forEachIndexed { x, rayHits ->
            rayHits.forEachIndexed rayHits@{ y, rayHit ->
                var color = rayHit?.color ?: return@rayHits
                if (colorValues[x][y].rgb != -16777216)
                    color = color.avgWeighted(
                        colorValues[x][y],
                        lightValues[x][y],
                        1.3.pow(rayHit.incomingLight.toDouble()).toFloat()
                    )
                colorValues[x][y] = color
                image[x][y] =
                    color.mul(color.alpha / 255f).mul(min(1f, if (settings.bounces != 1) lightValues[x][y] else 1.0f))
            }
        }
        return image
    }

    fun getSkyboxUV(dir: Vec3): Vec2 {
        // 1. Normalize the direction vector
        val length = Math.sqrt((dir.x * dir.x + dir.y * dir.y + dir.z * dir.z).toDouble()).toFloat()
        val dx = dir.x / length
        val dy = dir.y / length
        val dz = dir.z / length

        val absX = Math.abs(dx)
        val absY = Math.abs(dy)
        val absZ = Math.abs(dz)

        var uc = 0f
        var vc = 0f
        var maxAxis = 1f

        // Grid indices (0 to 2 for X, 0 to 1 for Y)
        var xTile = 0
        var yTile = 0

        // 2. Determine which face the vector hits
        if (absX >= absY && absX >= absZ) {
            maxAxis = absX
            if (dx > 0f) { // RIGHT
                uc = -dz; vc = dy; xTile = 2; yTile = 0
            } else {       // LEFT
                uc = dz; vc = dy; xTile = 0; yTile = 0
            }
        } else if (absY >= absX && absY >= absZ) {
            maxAxis = absY
            if (dy > 0f) { // TOP
                uc = dx; vc = -dz; xTile = 1; yTile = 1
            } else {       // EMPTY (Bottom slot)
                // If your "empty" is actually a "Bottom", use:
                // uc = dx; vc = dz; xTile = 0; yTile = 1
                xTile = 0; yTile = 1
            }
        } else {
            maxAxis = absZ
            if (dz > 0f) { // FRONT
                uc = dx; vc = dy; xTile = 1; yTile = 0
            } else {       // BACK
                uc = -dx; vc = dy; xTile = 2; yTile = 1
            }
        }

        // 3. Project to [0, 1] range within the tile
        val uTile = 0.5f * (uc / maxAxis + 1f)
        val vTile = 0.5f * (vc / maxAxis + 1f)

        // 4. Map to the full 3x2 texture sheet
        // We divide by 3.0 (width) and 2.0 (height)
        val finalU = (xTile + uTile) / 3f
        val finalV = (yTile + vTile) / 2f

        return Vec2(finalU, finalV)
    }

    fun getPixelFromUV(uv: Vec2, image: BufferedImage): Color {
        val width = image.width
        val height = image.height

        // 1. Map U to X [0, width - 1]
        // We use coerceIn to prevent out-of-bounds errors at the very edge (1.0)
        val x = (uv.x * width).toInt().coerceIn(0, width - 1)

        // 2. Map V to Y [0, height - 1]
        // IMPORTANT: BufferedImage (0,0) is TOP-left.
        // Skybox math usually treats (0,0) as BOTTOM-left.
        // We flip the V axis: (1.0 - v)
        val y = ((1.0f - uv.y) * height).toInt().coerceIn(0, height - 1)

        // 3. Return the color at that pixel
        val rgb = image.getRGB(x, y)
        return Color(rgb, true)
    }


    fun getSkyboxColor(vector: Vec3): Color {
        return getPixelFromUV(getSkyboxUV(vector), skyboxTexture)
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