# Raycasting Renderer

This project is a simple raycasting-based renderer written in Kotlin. It simulates light rays to render 3D scenes based on voxel data.

![img.png](https://i.imgur.com/yu7x4rU.jpg)


## Example usage
Kotlin
```kotlin 
// set path to textures
System.setProperty("renderer.block-textures-path", "assets/minecraft/textures/block/")
// set path to models
System.setProperty("renderer.block-models-path", "assets/minecraft/models/block/")

// create world + place some blocks
val world = World(Array<Block>(7 * 7 * 7) { Block.air }, Triple(7, 7, 7))
world.blocks[7 * 2 + 4] = getBlock("stone")
world.blocks[7 * 2 + 5] = Block("stone")
world.blocks[7 * 2 + 6] = Block("stone")
world.blocks[7 * 3 + 5] = Block("stone")
world.blocks[7 * 4 + 5] = Block("dirt")

val camera = Camera(
    Vec3(6.5f, 4f, 6.5f), // camera pos
    Vec3( // camera rotation in radiant
        270 * Math.PI.toFloat() / 180f,
        0 * Math.PI.toFloat() / 180f,
        0 * Math.PI.toFloat() / 180f
    ),
    CameraSettings(90f, 3, Pair(640, 640)), // fov, bounces of rays, resolution
    world,
    ImageIO.read(File("assets2/skybox/day.png")) // skybox
)

for (i in 0..9) {
    camera.sendRays() // sampling
}
val image = camera.generateImage()
ImageIO.write(image, "png", File("test.png")) // save to file
```
Java
```java
import kotlin.Pair;
import kotlin.Triple;
import me.tems.Camera;
import me.tems.CameraSettings;
import me.tems.coords.Block;
import me.tems.coords.Vec3;
import me.tems.worlds.World;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // Set path to textures
        System.setProperty("renderer.block-textures-path", "assets/minecraft/textures/block/");
        // Set path to models
        System.setProperty("renderer.block-models-path", "assets/minecraft/models/block/");

        // Create world + place some blocks
        int size = 7;
        Block[] blocks = new Block[size * size * size];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = Block.Companion.getAir(); // assuming static factory or constant
        }

        World world = new World(blocks, new Triple<>(size, size, size));

        world.getBlocks()[size * 2 + 4] = new Block("stone");
        world.getBlocks()[size * 2 + 5] = new Block("stone");
        world.getBlocks()[size * 2 + 6] = new Block("stone");
        world.getBlocks()[size * 3 + 5] = new Block("stone");
        world.getBlocks()[size * 4 + 5] = new Block("dirt");

        Vec3 cameraPos = new Vec3(6.5f, 4f, 6.5f);
        Vec3 cameraRot = new Vec3(
                (float) Math.toRadians(270),
                (float) Math.toRadians(0),
                (float) Math.toRadians(0)
        );

        CameraSettings settings = new CameraSettings(90f, 3, new Pair<>(640, 640));

        BufferedImage skybox = ImageIO.read(new File("assets/skybox/day.png"));
        Camera camera = new Camera(cameraPos, cameraRot, settings, world, skybox);

        for (int i = 0; i <= 9; i++) {
            camera.sendRays(); // sampling
        }

        BufferedImage image = camera.generateImage();
        ImageIO.write(image, "png", new File("test.png")); // save to file
    }
}
```

### We are open to PR's!
