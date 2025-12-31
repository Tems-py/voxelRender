package me.tems.coords

class Block(val name: String) {
    var isAir: Boolean = name == "air"
    var isFull: Boolean = true
    var reflective: Float = 1f // 0 to full mirror, 1 to wcale
    var illumination = 0.0f
    var properties = mutableMapOf<String, String>()

    var geometries = listOf<Geometry>()

    fun getReflectDirection(direction: Vec3, normal: Vec3): Vec3 {
        return when (reflective) {
            0f -> direction.reflect(normal)
            1f -> normal.randomOutwardVector()
            else -> direction.reflect(normal).plus(
                Vec3.random().mul(reflective)
            ) // generalnie wszystko mozna tym zrobić, ale te 2 wyzej to lekka optymalizacja
        }
    }

    companion object {
        val air = Block("air")
    }

    override fun toString(): String {
        return "<Block $name>"
    }
}