package org.example


fun main() {
    val world = Array(5) { Array(5) { Array(5) { Block.air } } }

    world[4][4][4] = Block("stone")
    world[2][4][4] = Block("grass_block_top")
    world[4][4][0] = Block("dirt")
    world[3][4][0] = Block("dirt")
    world[4][4][0] = Block("dirt")
    world[2][4][0] = Block("dirt")
    world[1][4][0] = Block("dirt")
    world[0][4][0] = Block("dirt")
//    world[4][3][4] = Block("bell")

    world[2][4][2] = Block("stone")


    val camera = Camera(Vec3(2f, 2f, 2.0f), Vec2(0.0f, 0.0f), 90f, world)

    camera.getViewVectors()
    val startTime = System.currentTimeMillis()
    camera.sendRays()
    println("TIME: ${(System.currentTimeMillis() - startTime) / 1000f}s")
}