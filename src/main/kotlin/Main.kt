package org.example

import org.example.coords.Block
import org.example.coords.Vec3


fun main() {
    val world = Array(25) { Array(25) { Array(25) { Block.air } } }

    world[0][1][3] = Block("oak_planks")
    world[1][1][3] = Block("oak_planks")
    world[2][1][3] = Block("green_wool")
    world[3][1][3] = Block("brown_wool")
    world[4][1][3] = Block("yellow_wool")
    world[5][1][3] = Block("oak_planks")
    world[0][2][3] = Block("oak_planks")
    world[1][2][3] = Block("green_wool")
    world[2][2][3] = Block("green_wool")
    world[3][2][3] = Block("brown_wool")
    world[4][2][3] = Block("yellow_wool")
    world[5][2][3] = Block("oak_planks")
    world[6][2][3] = Block("oak_planks")
    world[0][3][3] = Block("oak_planks")
    world[1][3][3] = Block("green_wool")
    world[2][3][3] = Block("green_wool")
    world[3][3][3] = Block("brown_wool")
    world[4][3][3] = Block("yellow_wool")
    world[5][3][3] = Block("oak_planks")
    world[6][3][3] = Block("oak_planks")
    world[0][4][3] = Block("oak_planks")
    world[1][4][3] = Block("oak_planks")
    world[2][4][3] = Block("green_wool")
    world[3][4][3] = Block("brown_wool")
    world[4][4][3] = Block("yellow_wool")
    world[5][4][3] = Block("oak_planks")
    world[6][4][3] = Block("oak_planks")
    world[0][5][3] = Block("oak_planks")
    world[1][5][3] = Block("oak_planks")
    world[2][5][3] = Block("green_wool")
    world[3][5][3] = Block("brown_wool")
    world[4][5][3] = Block("yellow_wool")
    world[5][5][3] = Block("oak_planks")


    val camera = Camera(
        Vec3(2f, 3f, 9f),
        Vec3(0.0f * Math.PI.toFloat() / 180f, 90.0f * Math.PI.toFloat() / 180f, -110.0f * Math.PI.toFloat() / 180f),
        134f,
        world
    )

    camera.getViewVectors()
    val startTime = System.currentTimeMillis()
    camera.sendRays()
    println("TIME: ${(System.currentTimeMillis() - startTime) / 1000f}s")
}