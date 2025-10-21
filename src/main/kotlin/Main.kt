package org.example

import org.example.coords.Vec3
import org.example.raycasting.Raycasting
import org.example.textures.TexturesManager
import org.example.utils.ColorUtils.mul
import org.example.utils.ImageTransferable
import org.example.worlds.World
import org.example.worlds.WorldManager
import java.awt.Color
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


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
        RenderPosition("worlds/glowstone_test.schem", Vec3(1f, 3f, 4.5f), Vec3(90.0f, 0f, 0f), 180, 3), // glowstone
        RenderPosition(
            "worlds/glowstone_test.schem",
            Vec3(8f, 3f, 4.5f),
            Vec3(270.0f, 0f, 0f),
            10,
            3
        ), // glowstone od tyłu
        RenderPosition("worlds/glowstone_test.schem", Vec3(3f, 3f, 4.5f), Vec3(90.0f, 0f, 0f), 1, 0), // glowstone
        RenderPosition("worlds/testowy_city.schem", Vec3(3f, 3f, 26f), Vec3(90.0f, 0f, 0f), 10, 4), // miasto
        RenderPosition(
            "worlds/mapsall.schem",
            Vec3(66f, 11f, 66f),
            Vec3(90.0f, 0f, 30f),
            20,
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
            2
        ), // liscie, ziemia inna, krzaczki
        RenderPosition("worlds/blocks_test.schem", Vec3(3f, 3f, 4.5f), Vec3(90.0f, 0f, 0f), 1, 0), // anvil grass
        RenderPosition("worlds/stairs_test.schem", Vec3(1f, 3f, 6.5f), Vec3(90.0f, 0f, 0f), 1, 2),
        RenderPosition("-", Vec3(0.1f, 7f, 11.5f), Vec3(160.0f, 0f, 30f), 1, 1),
    )

    val RENDER = 1

    val renderPosition = savedRenderPositions[RENDER]

    val image = renderImage2(
        WorldManager.getWorld(renderPosition.worldPath),
        renderPosition.position,
        renderPosition.rotationDegrees,
        renderPosition.sampling,
        renderPosition.bounces
    )


    showImage(image, "")

//    renderImage(
//        WorldManager.loadWorldFromString(
//            "minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:air;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:double;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:double;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:smooth_stone_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:double;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:air;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:quartz_slab,waterlogged:false,type:bottom;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:quartz_slab,waterlogged:false,type:double;minecraft:air;minecraft:smooth_stone_slab,waterlogged:false,type:bottom;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air;minecraft:air"
//        ),
//        renderPosition.position,
//        renderPosition.rotationDegrees,
//        renderPosition.sampling,
//        renderPosition.bounces
//    )


    //    Vec3(65f, 15f, 69f), // mount
    //Vec3(3f, 3f, 26f), // city


//    renderBuildsFromTxt()
}

fun renderBuildsFromTxt() {
    val builds = File("assets/to_render.txt").readLines().filterIndexed { index, s -> index == 8 }.map { // filterIndexed { index, s -> index == 62 }.
        val name = it.split(";")[0]
        val worldString = it.takeLast(it.length - (name.length + 1))
        val world = WorldManager.loadWorldFromString(worldString)
        println(name)
        return@map Pair(name, world)
    }//.filterIndexed { index, pair ->
//        pair.first == "7066"
//    }



    builds.forEachIndexed { index, build ->
        println("Builds: ${index}/${builds.size} ${(index/builds.size) * 100}%")
        val image = renderImage(
            build.second,
            Vec3(0.1f, 7f, 11.5f),
            Vec3(155.0f, 0f, 25f),
            10,
            5
        )

        showImage(image, "")
        ImageIO.write(image, "png", File("renders/${build.first}.png"));
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
        CameraSettings(100f, sampling, bounces),
        world
    )

    val startTime = System.currentTimeMillis()
    val image = camera.sendRays()
    val time = "${(System.currentTimeMillis() - startTime) / 1000f}s"
    println("TIME: $time")
    return image as BufferedImage
}

fun renderImage2(world: World, position: Vec3, rotationDegrees: Vec3, sampling: Int, bounces: Int): BufferedImage {
    TexturesManager.preloadTextures(world.blocks)

    val camera = Camera(
        position,
        Vec3(
            rotationDegrees.x * Math.PI.toFloat() / 180f,
            rotationDegrees.y * Math.PI.toFloat() / 180f,
            rotationDegrees.z * Math.PI.toFloat() / 180f
        ),
        CameraSettings(120f, sampling, bounces),
        world
    )


    val lightValues:  Array<Array<Float>> = Array(1920) { Array(1080) { 0f } }

    val rays = camera.sendRays()
    rays.forEachIndexed { x, rayHits ->
        rayHits.forEachIndexed { y, rayHit ->
            lightValues[x][y] = rayHit?.incomingLight ?: 0f
        }
    }

    val image = generateImageFromHits(rays, lightValues)
    val (jFrame, label) = showImage(image, "")

    for (i in 1..sampling) {
        val startTime = System.currentTimeMillis()
        val rays = camera.sendRays()
        rays.forEachIndexed { x, rayHits ->
            rayHits.forEachIndexed { y, rayHit ->
                lightValues[x][y] = (lightValues[x][y] * i + (rayHit?.incomingLight ?: 0f)) / (i + 1)
            }
        }

        val image = generateImageFromHits(rays, lightValues)
//        val img = camera.sendRays().average(image)
        val time = "${(System.currentTimeMillis() - startTime) / 1000f}s"
        println("Sample: $i, time: $time")
        label.icon = ImageIcon(image)
        jFrame.title = "Sample: $i, time: $time"
    }

    return image
}

fun generateImageFromHits(image: Array<Array<Raycasting.RayHit?>>, lightValues: Array<Array<Float>>): BufferedImage {
    val width = image.size
    val height = image[0].size
    val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    for (x in image.indices) {
        for (y in image[0].indices) {
            val hit = image[x][y]
            if (hit == null) {
                bufferedImage.setRGB(x, y, Color(126, 225, 252).rgb)

                continue
            }
//                val shadowColor =
//                    Color(abs(hit.face.x.toInt()) * 13, abs(hit.face.y.toInt()) * 13, abs(hit.face.z.toInt()) * 13)

            bufferedImage.setRGB(x, y, hit.color.mul(hit.color.alpha / 255f).mul(min(1f, lightValues[x][y])).rgb)
        }
    }

    return bufferedImage
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