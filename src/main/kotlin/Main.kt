package org.example

import org.example.coords.Vec3
import org.example.textures.TexturesManager
import org.example.worlds.WorldManager
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel


fun main() {
    val world = WorldManager.getWorld()

    TexturesManager.preloadTextures(world.blocks)

//    Vec3(65f, 15f, 69f),
//Vec3(3f, 3f, 26f),
    // Vec3(13f, 18f, 13f) - village 45.0f, 0, 50f

    val camera = Camera(
        Vec3(3f, 3.2f, 4.5f),
        Vec3(90.0f * Math.PI.toFloat() / 180f, 0.0f * Math.PI.toFloat() / 180f, 0 * Math.PI.toFloat() / 180f),
        134f,
        world
    )

    val startTime = System.currentTimeMillis()
    val image = camera.sendRays()
    println("TIME: ${(System.currentTimeMillis() - startTime) / 1000f}s")
    showImage(image)
//    testTexture("short_grass")
}

fun showImage(image: BufferedImage) {
    val frame = JFrame("Image Viewer")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.contentPane.add(JLabel(ImageIcon(image)))
    frame.pack()
    frame.isVisible = true
    frame.setSize(image.width, image.height)
}


fun Float.wrapTo01(): Float {
    val r = this % 1.0f
    val positiveR = if (r < 0) r + 1.0f else r
    return if (positiveR == 0.0f && this != 0.0f) 1.0f else positiveR
}

fun Float.wrapTo01Positive(): Float {
    return (this.wrapTo01() + 1).wrapTo01()
}