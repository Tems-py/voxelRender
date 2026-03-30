package me.tems.coords

class Block(val name: String) {
    var isAir: Boolean = name == "air"
    var isFull: Boolean = true
    var reflective: Float = 1f // 0 to full mirror, 1 to wcale
    var illumination = 0.0f
    var properties = mutableMapOf<String, String>()

    var geometries = listOf<Geometry>()

    fun getReflectDirection(direction: FloatArray, normal: FloatArray): FloatArray {
        return when (reflective) {
            0f -> direction.reflect(normal)
            1f -> normal.randomOutwardVector()
            else -> direction.reflect(normal).add(
                Vec3.random().mul(reflective)
            )
        }
    }

    companion object {
        val air = Block("air")
    }

    override fun toString(): String {
        return "<Block $name>"
    }
}
