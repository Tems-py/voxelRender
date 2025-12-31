# Raycasting Renderer

This project is a simple raycasting-based renderer written in Kotlin. It simulates light rays to render 3D scenes based on voxel data.

![img.png](https://i.imgur.com/yu7x4rU.jpg)


## Example usage
```kotlin 
// set path to textures
System.setProperty("renderer.block-textures-path", "assets2/minecraft/textures/block/")
// set path to models
System.setProperty("renderer.block-models-path", "assets2/minecraft/models/block/")

// create world + place some blocks
val world = World(Array<Block>(7 * 7 * 7) { Block.air }, Triple(7, 7, 7))
world.blocks[7 * 2 + 4] = Block("stone")
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

### We are open to PR's!
