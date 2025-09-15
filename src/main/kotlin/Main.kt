package org.example

import org.example.coords.Block
import org.example.coords.Vec3
import org.example.textures.TexturesManager
import org.example.worlds.CityWorld
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

import org.jcodec.api.awt.AWTSequenceEncoder
import java.io.File



fun main() {
    val world = CityWorld.getWorld()

    TexturesManager.preloadTextures(world)

    val camera = Camera(
        Vec3(3f, 3f, 26f),
        Vec3(90.0f * Math.PI.toFloat() / 180f, 0.0f * Math.PI.toFloat() / 180f, 0f * Math.PI.toFloat() / 180f),
        134f,
        world
    )

    val frame = JFrame("Image Viewer")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
    frame.setSize(1920, 1080)


    var startTime = System.currentTimeMillis()
    var image = camera.sendRays()
    frame.contentPane.add(JLabel(ImageIcon(image)))
    frame.pack()
    println("FPS: ${1/ ((System.currentTimeMillis() - startTime)/1000f) }s")



    val images  = mutableListOf<BufferedImage>()
    val outputFile = File("output.mp4")




    for (i in 0..60)
    {
        startTime = System.currentTimeMillis()
        image = camera.sendRays()
        frame.contentPane.remove(0);
        frame.contentPane.add(JLabel(ImageIcon(image)))
        frame.pack()
        println("FPS: ${1/ ((System.currentTimeMillis() - startTime)/1000f) }s")
        camera.position = camera.position.plus(Vec3(1f,0f,0f))
        images.add(image)

    }

    createVideoFromImages(images, outputFile, fps = 30)
    println("Video created at: ${outputFile.absolutePath}")
}

fun createVideoFromImages(images: List<BufferedImage>, outputFile: File, fps: Int = 25) {
    // Create encoder with chosen FPS
    val encoder = AWTSequenceEncoder.createSequenceEncoder(outputFile, fps)

    for (img in images) {
        encoder.encodeImage(img) // Add each BufferedImage as a frame
    }

    encoder.finish() // Finalize video
}
