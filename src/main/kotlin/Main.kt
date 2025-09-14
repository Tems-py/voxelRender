package org.example

import org.example.coords.Block
import org.example.coords.Vec3
import org.example.textures.TexturesManager
import org.example.worlds.CityWorld


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
    camera.sendRays()
    println("TIME: ${(System.currentTimeMillis() - startTime) / 1000f}s")
}
