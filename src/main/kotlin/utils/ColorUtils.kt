package org.example.utils

import org.example.coords.Vec3
import java.awt.Color
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

object ColorUtils {
    fun Color.mul(color: Color): Color {
        return Color(
            kotlin.math.min(255, (this.red * (color.red / 255f)).toInt()),
            kotlin.math.min(255, (this.green * (color.green / 255f)).toInt()),
            kotlin.math.min(255, (this.blue * (color.blue / 255f)).toInt()),
            this.alpha
        )
    }

    fun Color.mul(float: Float): Color {
        try {
            return Color(
                kotlin.math.min(255, (this.red * float).toInt()),
                kotlin.math.min(255, (this.green * float).toInt()),
                kotlin.math.min(255, (this.blue * float).toInt()),
                this.alpha
            )
        } catch (e: Exception) {
            println(e)
            println(this)
            println(float)
        }
        return Color(0)
    }

    fun Color.add(color: Color): Color {
        return Color(
            kotlin.math.min(255, this.red + color.red),
            kotlin.math.min(255, this.green + color.green),
            kotlin.math.min(255, this.blue + color.blue),
            this.alpha
        )
    }

    fun Color.min(color: Color): Color {
        return Color(
            max(0, this.red - color.red),
            max(0, this.green - color.green),
            max(0, this.blue - color.blue),
            this.alpha
        )
    }

    fun Color.avg2(color: Color): Color {
        return Color(
            sqrt((this.red * color.red).toDouble()).toInt(),
            sqrt((this.green * color.green).toDouble()).toInt(),
            sqrt((this.blue * color.blue).toDouble()).toInt()
        )
    }

    fun Color.avg(color: Color): Color {
//        return Color(sqrt((this.red * color.red).toDouble()).toInt(), sqrt((this.green * color.green).toDouble()).toInt(), sqrt((this.blue * color.blue).toDouble()).toInt())
        val alpha = sqrt((this.alpha * color.alpha).toDouble()) / 255
        return Color(
            sqrt((this.red * color.red * alpha)).toInt(),
            sqrt((this.green * color.green * alpha)).toInt(),
            sqrt((this.blue * color.blue * alpha)).toInt()
        )
    }

    fun Color.avgWeighted(color: Color, weight1: Float, weight2: Float): Color {
        val w1 = weight1.coerceAtLeast(0f).toDouble()
        val w2 = weight2.coerceAtLeast(0f).toDouble()
        val total = if (w1 + w2 == 0.0) 1.0 else (w1 + w2)
        val nw1 = w1 / total
        val nw2 = w2 / total
        val alpha = sqrt((this.alpha * color.alpha).toDouble()) / 255.0
        fun comp(a: Int, b: Int): Int {
            val v1 = a / 255.0
            val v2 = b / 255.0
            val blended = (v1.pow(nw1) * v2.pow(nw2) * alpha).coerceIn(0.0, 1.0)
            return (blended * 255.0).toInt()
        }
        return Color(
            comp(this.red, color.red),
            comp(this.green, color.green),
            comp(this.blue, color.blue),
            this.alpha
        )
    }

    fun Color.avg(colors: List<Color>): Color {
        var red = this.red
        var green = this.green
        var blue = this.blue
        for (color in colors) {
            red += color.red
            green += color.green
            blue += color.blue
        }
        red /= (colors.size + 1)
        green /= (colors.size + 1)
        blue /= (colors.size + 1)
        return Color(red, green, blue);
    }

    fun sortVec3sByMagnitude(v1: Vec3, v2: Vec3): Pair<Vec3, Vec3> {
        return if (v1.lengthSquared() < v2.lengthSquared()) {
            Pair(v1, v2)
        } else {
            Pair(v2, v1)
        }
    }
}