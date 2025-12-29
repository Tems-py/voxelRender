package org.example

import org.example.coords.Vec3
import org.example.textures.TexturesManager
import org.example.utils.ImageTransferable
import org.example.worlds.World
import org.example.worlds.WorldManager
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.roundToInt

data class RenderPosition(
    val worldPath: String,
    val position: Vec3,
    val rotationDegrees: Vec3,
    val sampling: Int,
    val bounces: Int
)

val savedRenderPositions = listOf(
    RenderPosition("worlds/village.schem", Vec3(13f, 18f, 13f), Vec3(45.0f, 0f, 50f), 25, 3), // village
    RenderPosition("worlds/glowstone_test.schem", Vec3(1f, 3f, 4.5f), Vec3(90.0f, 0f, 0f), 1800, 3), // glowstone
    RenderPosition(
        "worlds/glowstone_test.schem",
        Vec3(8f, 3f, 4.5f),
        Vec3(270.0f, 0f, 0f),
        10,
        3
    ), // glowstone od tyłu
    RenderPosition("worlds/glowstone_test.schem", Vec3(3f, 3f, 4.5f), Vec3(90.0f, 0f, 0f), 1, 0), // glowstone
    RenderPosition("worlds/testowy_city.schem", Vec3(3f, 3f, 26f), Vec3(90.0f, 0f, 0f), 100, 10), // miasto
    RenderPosition(
        "worlds/mapsall.schem",
        Vec3(66f, 11f, 66f),
        Vec3(90.0f, 0f, 30f),
        200,
        2
    ), // budowle losowe - ogromna mapa, ale niska
    RenderPosition(
        "worlds/mapsall.schem",
        Vec3(128f, 9f, 187f),
        Vec3(0.0f, 0f, 30f),
        1,
        10
    ), // budowle losowe - ogromna mapa, ale niska
    RenderPosition(
        "worlds/taigatest.schem",
        Vec3(15f, 17f, 36f),
        Vec3(110.0f, 0f, 0f),
        10,
        20
    ), // liscie, ziemia inna, krzaczki
    RenderPosition("worlds/blocks_test.schem", Vec3(1f, 3f, 5.5f), Vec3(90.0f, 0f, 0f), 32, 4), // anvil grass
    RenderPosition("worlds/stairs_test.schem", Vec3(1f, 3f, 6.5f), Vec3(90.0f, 0f, 0f), 1, 2),
    RenderPosition("-", Vec3(0.1f, 7f, 11.5f), Vec3(160.0f, 0f, 30f), 1, 1),
)

fun main() {
//    val RENDER = 8
//    val renderPosition = savedRenderPositions[RENDER]
//    val world = WorldManager.getWorld(renderPosition.worldPath)
//        renderImage(
//            world,
//            renderPosition.position,
//            renderPosition.rotationDegrees,
//            renderPosition.sampling,
//            renderPosition.bounces
//        )

    val builds = getBuildsFromTxt("assets/to_render.txt", 1, 10000)
    val start = System.currentTimeMillis()
    builds.forEachIndexed { index, build ->
        val eta = (System.currentTimeMillis() - start) / (index / builds.size.toFloat())
        println("Builds: ${index}/${builds.size} ${((index / builds.size.toFloat()) * 10000).roundToInt() / 100}% | ETA: ${(eta / 1000) / 60}min")
        val image = renderImage(
            build.second,
            Vec3(0.1f, 7f, 11.5f),
            Vec3(155.0f, 0f, 25f),
            8,
            3
        )
        ImageIO.write(image, "png", File("renders/${build.first}.png"));
    }
}


fun getBuildsFromTxt(file: String, fromIndex: Int, toIndex: Int): List<Pair<String, World>> {
    return File(file).readLines().filterIndexed { index, s -> index in fromIndex..toIndex }
        .map { // filterIndexed { index, s -> index == 62 }.7
            val name = it.split(";")[0]
            val worldString = it.takeLast(it.length - (name.length + 1))
            val world = WorldManager.loadWorldFromString(worldString)
            println(name)
            return@map Pair(name, world)
        }
}


fun renderImage(world: World, position: Vec3, rotationDegrees: Vec3, sampling: Int, bounces: Int): BufferedImage {
    TexturesManager.preloadTextures(world.blocks)

    val camera = Camera(
        position,
        Vec3(
            rotationDegrees.x * Math.PI.toFloat() / 180f,
            rotationDegrees.y * Math.PI.toFloat() / 180f,
            rotationDegrees.z * Math.PI.toFloat() / 180f
        ),
        CameraSettings(90f, bounces, Pair(640, 640)),
        world
    )

    var image = BufferedImage(640, 640, BufferedImage.TYPE_INT_RGB)
//    val (jFrame, label) = showImage(image, "")

    for (i in 0 until sampling) {
        val startTime = System.currentTimeMillis()
        camera.sendRays()
        image = camera.generateImage()
        val time = (System.currentTimeMillis() - startTime) / 1000f
//        println("Sample: $i, time: $time s")
//        label.icon = ImageIcon(image)
//        label.icon = ImageIcon((image.getScaledInstance(image.width * 4, image.height * 4, java.awt.Image.SCALE_SMOOTH)))
//        jFrame.title = "Sample: $i, time: $time"
    }

    return image
}

fun showImage(image: BufferedImage, infoString: String): Pair<JFrame, JLabel> {
    val frame = JFrame("Voxel renderer")

    val menuBar = JMenuBar()
    val copyImageItem = JMenuItem("Copy Image")
    val editMenu = JMenu("Edit")
    val info = JLabel(infoString)
    editMenu.add(copyImageItem)
    menuBar.add(editMenu)
    menuBar.add(info)

    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    val label = JLabel(ImageIcon(image))
    frame.contentPane.add(label)
    frame.pack()
    frame.isVisible = true
    frame.setSize(image.width, image.height)
    frame.jMenuBar = menuBar

    copyImageItem.addActionListener {
        val transferable = ImageTransferable(image)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard

        clipboard.setContents(transferable, null)
    }

    return Pair(frame, label)
}

/**
 * Maps a normalized float value (this) from the range [0.0f, 1.0f]
 * to a new range defined by min and max (both Floats).
 *
 * @param min The minimum value of the target range (Float).
 * @param max The maximum value of the target range (Float).
 * @return The mapped float value within [min, max].
 */
fun Float.mapToRange(min: Float, max: Float): Float {
    // Calculate the size of the target range.
    val rangeSize = max - min

    // Scale the normalized value (this) by the range size,
    // then shift the result by adding the minimum value.
    return min + (this * rangeSize)
}

fun Float.fixFloatingPointError(tolerance: Float = 0.0001f): Float {
    if (this.isNaN()) return 0f
    val rounded = this.roundToInt()
    return if (kotlin.math.abs(this - rounded) < tolerance) {
        rounded.toFloat()
    } else {
        this
    }
}