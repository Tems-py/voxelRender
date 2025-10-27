package org.example

import org.example.coords.Vec3
import org.example.textures.TexturesManager
import org.example.worlds.World
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.ImageIcon

data class RenderReturn(
    val image: BufferedImage,
    val time: Float
)

fun main() {
    var returnText = ""
    val renders = mutableListOf<RenderReturn>()
    var totalTime = 0f
    val sampling = 10
    val bounces = 10

    val builds = getBuildsFromTxt("assets/to_render.txt", 0, 10)

    builds.forEachIndexed { index, build ->
        println("Builds: ${index}/${builds.size} ${(index / builds.size) * 100}%")
        val render = renderImage(
            build.second,
            Vec3(0.1f, 7f, 11.5f),
            Vec3(155.0f, 0f, 25f),
            sampling,
            bounces,
            autoClose = true
        )
        renders.add(render)
        totalTime += render.time
        returnText += index.toString() + ". " + render.time + "s\n"
//        ImageIO.write(image, "png", File("renders/${build.first}.png"));
    }

    val outputFile = File(".").resolve("testOutput.txt")
    outputFile.writeText("sampling:$sampling bounces:$bounces\ntotalTime:$totalTime s\n$returnText")

}

fun renderImage(
    world: World,
    position: Vec3,
    rotationDegrees: Vec3,
    sampling: Int,
    bounces: Int,
    autoClose: Boolean = true
): RenderReturn {
    TexturesManager.preloadTextures(world.blocks)

    val camera = Camera(
        position,
        Vec3(
            rotationDegrees.x * Math.PI.toFloat() / 180f,
            rotationDegrees.y * Math.PI.toFloat() / 180f,
            rotationDegrees.z * Math.PI.toFloat() / 180f
        ),
        CameraSettings(110f, bounces, Pair(1920, 1080)),
        world
    )

    var image = BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB)
    val (jFrame, label) = showImage(image, "")

    var totalTime = 0f
    for (i in 0 until sampling) {
        val startTime = System.currentTimeMillis()

        camera.sendRays()

        image = camera.generateImage()
        val time = (System.currentTimeMillis() - startTime) / 1000f
        totalTime += time
        println("Sample: $i, time: $time s")
        label.icon = ImageIcon(image)
//        label.icon = ImageIcon((image.getScaledInstance(image.width * 4, image.height * 4, java.awt.Image.SCALE_SMOOTH)))
        jFrame.title = "Sample: $i, time: $time"
    }
    if (autoClose) {
        jFrame.dispose()
    }
    return RenderReturn(image, totalTime)
}