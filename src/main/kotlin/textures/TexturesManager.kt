package org.example.textures

import org.example.coords.Block
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class TexturesManager {
    companion object {
        val cachedTextures = mutableMapOf<String, BufferedImage?>()

        fun getTexture(name: String): BufferedImage? {
            if (cachedTextures.containsKey(name)) return cachedTextures[name]

            val image = try {
                ImageIO.read(File("textures/${name}.png"))
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
                getTexture(it)
            }
        }
    }
}