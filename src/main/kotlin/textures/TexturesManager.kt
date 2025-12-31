package me.tems.textures

import kotlinx.coroutines.*
import me.tems.coords.Block
import me.tems.coords.Vec2
import me.tems.utils.ColorUtils.mul
import me.tems.utils.FloatUtils.mapToRange
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.min

class TexturesManager {
    companion object {
        private val cachedTextures = mutableMapOf<String, BufferedImage?>()
        private val blockTexturesPath = System.getProperty("renderer.block-textures-path") ?: "assets/minecraft/textures/block/"

        fun getTexture(name: String): BufferedImage? = cachedTextures.getOrPut(name) {
            val image = try {
                ImageIO.read(File("$blockTexturesPath${name}.png"))
            } catch (e: Exception) {
                null
            }
            cachedTextures[name] = image
            return image
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
            val image: BufferedImage = getTexture(textureName) ?: return Color(0, 0, 0, 0)

            val clampedX = (((uv.x) % 1f) + 1f) % 1f
            val clampedY = (((uv.y) % 1f) + 1f) % 1f

            val px = min((clampedX.mapToRange(uvMap.first.x, uvMap.second.x)).toInt(), image.width - 1)
            val py = min((clampedY.mapToRange(uvMap.first.y, uvMap.second.y)).toInt(), image.height - 1)
            //to moze powodowac rozjechanie tekstury jak wylecimy poza nią zamiast wywalic blad?

            val rgb: Int
            try {
                rgb = image.getRGB(px, py)
            } catch (e: ArrayIndexOutOfBoundsException) {
                println(this)
                println("$px $py")
                println("$clampedX $clampedY")
                return Color(0, 0, 0, 0)
            }

            var color = Color(rgb, true)

            val mulColor = if (textureName.contains("grass")) Color(119, 171, 47) // LEPSZY HANDLING BIOMÓW TUTAJ TRZEBE BEDZIE
            else if (textureName.contains("leaves")) Color(119, 171, 47)
            else null

            if (mulColor != null) color = color.mul(mulColor)

            return color
        }
    }
}