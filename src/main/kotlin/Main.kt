package org.example


fun main() {
    val world = Array(5) { Array(5) { Array(5) { Block.air } } }

    world[4][4][4] = Block("stone")
    world[2][4][4] = Block("bell")
    world[4][4][0] = Block("dirt")
    world[3][4][0] = Block("dirt")
    world[4][4][0] = Block("dirt")
    world[2][4][0] = Block("dirt")
    world[1][4][0] = Block("dirt")
    world[0][4][0] = Block("dirt")
//    world[4][3][4] = Block("bell")

    val camera = Camera(Vec3(2f, 2f, 4.0f), 180f, world)

    camera.getViewVectors()
    camera.sendRays()
}