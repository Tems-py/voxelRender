package org.example.textures

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class TexturesManager {
    companion object {
        val cachedTextures = hashMapOf<String, BufferedImage>()

        fun getTexture(name: String): BufferedImage {
            if (cachedTextures.containsKey(name)) return cachedTextures[name] ?: error("Cache error")
            val image = ImageIO.read(File("textures/${name}.png"))
            cachedTextures[name] = image
            return image
        }
    }
}