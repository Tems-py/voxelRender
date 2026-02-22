package me.tems.utils

import me.tems.coords.Vec3
import java.awt.Color
import kotlin.math.max
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
        val f = float.coerceAtLeast(0f)
        return Color(
            (this.red * f).toInt().coerceIn(0, 255),
            (this.green * f).toInt().coerceIn(0, 255),
            (this.blue * f).toInt().coerceIn(0, 255),
            this.alpha
        )
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
        val alpha = sqrt((this.alpha * color.alpha).toDouble()) / 255
        val outputAlpha = (alpha * 255).toInt().coerceIn(0, 255)
        return Color(
            sqrt((this.red * color.red * alpha)).toInt().coerceIn(0, 255),
            sqrt((this.green * color.green * alpha)).toInt().coerceIn(0, 255),
            sqrt((this.blue * color.blue * alpha)).toInt().coerceIn(0, 255),
            outputAlpha
        )
    }

    fun Color.avgWeighted(color: Color, weight1: Float, weight2: Float): Color {
        return Color(
            ((this.red * weight1 + color.red * weight2) / (weight1 + weight2)).toInt(),
            ((this.green * weight1 + color.green * weight2) / (weight1 + weight2)).toInt(),
            ((this.blue * weight1 + color.blue * weight2) / (weight1 + weight2)).toInt(),
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
        return Color(red, green, blue)
    }

    fun sortVec3sByMagnitude(v1: Vec3, v2: Vec3): Pair<Vec3, Vec3> {
        return if (v1.lengthSquared() < v2.lengthSquared()) {
            Pair(v1, v2)
        } else {
            Pair(v2, v1)
        }
    }

    fun Color.withFullAlpha(): Color {
        return Color(this.red, this.green, this.blue)
    }
}