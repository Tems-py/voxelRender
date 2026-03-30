package me.tems.textures

import kotlinx.coroutines.*
import me.tems.coords.Block
import me.tems.coords.Vec2
import me.tems.utils.ColorUtils.mul
import me.tems.utils.FloatUtils.mapToRange
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.min

class TexturesManager {
    companion object {
        private val cachedTextures = mutableMapOf<String, BufferedImage?>()
        private val blockTexturesPath = System.getProperty("renderer.block-textures-path") ?: "assets/minecraft/textures/block/"

        // Raw ARGB pixel arrays for direct indexed access — much faster than
        // per-pixel BufferedImage.getRGB() calls.
        private data class TexData(val pixels: IntArray, val width: Int, val height: Int)
        private val pixelCache = mutableMapOf<String, TexData?>()

        fun getTexture(name: String): BufferedImage? = cachedTextures.getOrPut(name) {
            val image = try {
                ImageIO.read(File("$blockTexturesPath${name}.png"))
            } catch (e: Exception) {
                null
            }
            cachedTextures[name] = image
            return image
        }

        private fun getTexData(name: String): TexData? = pixelCache.getOrPut(name) {
            val img = getTexture(name) ?: return@getOrPut null
            val w = img.width; val h = img.height
            // If the image already has a TYPE_INT_* data buffer, grab the backing
            // array directly (zero copy).  Otherwise do one bulk getRGB() pass.
            val pixels: IntArray = if (img.raster.dataBuffer is DataBufferInt) {
                (img.raster.dataBuffer as DataBufferInt).data
            } else {
                img.getRGB(0, 0, w, h, IntArray(w * h), 0, w)
            }
            TexData(pixels, w, h)
        }

        fun preloadTextures(world: Array<Block>) {
            val textures = mutableListOf<String>()
            world.forEach { if (!textures.contains(it.name)) textures.add(it.name) }
            textures.forEach {
                if (it == "air") return@forEach
                CoroutineScope(Dispatchers.IO).launch {
                    BlockManager.getBlock(it)
                }
            }
        }

        fun getColorFromTexture(uv: Vec2, textureName: String, uvMap: Pair<Vec2, Vec2>): Color {
            val tex = getTexData(textureName) ?: return Color(0, 0, 0, 0)

            val clampedX = (((uv.x) % 1f) + 1f) % 1f
            val clampedY = (((uv.y) % 1f) + 1f) % 1f

            val px = min((clampedX.mapToRange(uvMap.first.x, uvMap.second.x)).toInt(), tex.width - 1)
            val py = min((clampedY.mapToRange(uvMap.first.y, uvMap.second.y)).toInt(), tex.height - 1)

            val rgb = tex.pixels[py * tex.width + px]

            var color = Color(rgb, true)

            val mulColor = if (textureName.contains("grass")) Color(119, 171, 47)
            else if (textureName.contains("leaves")) Color(119, 171, 47)
            else null

            if (mulColor != null) color = color.mul(mulColor)

            return color
        }
    }
}
