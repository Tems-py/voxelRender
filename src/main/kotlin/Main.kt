package org.example

import org.example.coords.Vec3
import org.example.textures.TexturesManager
import org.example.worlds.WorldManager
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel


fun main() {
    data class RenderPosition(
        val worldPath: String,
        val position: Vec3,
        val rotationDegrees: Vec3,
        val sampling: Int,
        val bounces: Int
    )

    val savedRenderPositions = listOf<RenderPosition>(
        RenderPosition("worlds/village.schem", Vec3(13f, 18f, 13f), Vec3(45.0f, 0f, 50f), 10, 3), // village
        RenderPosition("worlds/glowstone_test.schem", Vec3(3f, 3f, 4.5f), Vec3(90.0f, 0f, 0f), 50, 1), // glowstone
        RenderPosition("worlds/glowstone_test.schem", Vec3(8f, 3f, 4.5f), Vec3(270.0f, 0f, 0f), 10, 3), // glowstone od tyłu
        RenderPosition("worlds/glowstone_test.schem", Vec3(3f, 3f, 4.5f), Vec3(90.0f, 0f, 0f), 10, 0), // glowstone
        RenderPosition("worlds/testowy_city.schem", Vec3(3f, 3f, 26f), Vec3(90.0f, 0f, 0f), 10, 2), // miasto
        RenderPosition("worlds/mapsall.schem", Vec3(66f, 11f, 66f), Vec3(90.0f, 0f, 30f), 20, 2), // budowle losowe - ogromna mapa, ale niska
        RenderPosition("worlds/taigatest.schem", Vec3(15f, 17f, 36f), Vec3(110.0f, 0f, 0f), 10, 2), // liscie, ziemia inna, krzaczki
    )

    val RENDER = 1

    val renderPosition = savedRenderPositions[RENDER]

    renderImage(
        renderPosition.worldPath,
        renderPosition.position,
        renderPosition.rotationDegrees,
        renderPosition.sampling,
        renderPosition.bounces
    )


    //    Vec3(65f, 15f, 69f), // mount
    //Vec3(3f, 3f, 26f), // city
}

fun renderImage(worldPath: String, position: Vec3, rotationDegrees: Vec3, sampling: Int, bounces: Int) {
    val world = WorldManager.getWorld(worldPath)

    TexturesManager.preloadTextures(world.blocks)

    val camera = Camera(
        position,
        Vec3(
            rotationDegrees.x * Math.PI.toFloat() / 180f,
            rotationDegrees.y * Math.PI.toFloat() / 180f,
            rotationDegrees.z * Math.PI.toFloat() / 180f
        ),
        CameraSettings(134f, sampling, bounces),
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