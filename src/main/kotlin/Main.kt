package org.example

import org.example.coords.Block
import org.example.coords.Vec3
import org.example.textures.TexturesManager
import org.example.worlds.CityWorld
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel


fun main() {
    val world = CityWorld.getWorld()

    TexturesManager.preloadTextures(world)

    val camera = Camera(
        Vec3(3f, 3f, 26f),
        Vec3(90.0f * Math.PI.toFloat() / 180f, 0.0f * Math.PI.toFloat() / 180f, 0f * Math.PI.toFloat() / 180f),
        134f,
        world
    )

    val startTime = System.currentTimeMillis()
    val image = camera.sendRays()
    println("TIME: ${(System.currentTimeMillis() - startTime) / 1000f}s")
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