package org.example.textures

import org.example.coords.Block
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class TexturesManager {
    companion object {
        val cachedTextures = mutableMapOf<String, BufferedImage?>()

        fun getTexture(name: String): BufferedImage? = cachedTextures.getOrPut(name) {
            val image = try {
                ImageIO.read(File("assets/minecraft/textures/block/${name}.png"))
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
                if (getTexture(it) == null) println("$it cant find texture")
            }
        }
    }
}