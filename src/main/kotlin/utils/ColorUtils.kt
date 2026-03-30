package me.tems.utils

import me.tems.coords.lengthSquared
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
        val denom = weight1 + weight2
        return Color(
            ((this.red * weight1 + color.red * weight2) / denom).toInt(),
            ((this.green * weight1 + color.green * weight2) / denom).toInt(),
            ((this.blue * weight1 + color.blue * weight2) / denom).toInt(),
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

    fun sortVec3sByMagnitude(v1: FloatArray, v2: FloatArray): Pair<FloatArray, FloatArray> {
        return if (v1.lengthSquared() < v2.lengthSquared()) {
            Pair(v1, v2)
        } else {
            Pair(v2, v1)
        }
    }

    fun Color.withFullAlpha(): Color {
        return Color(this.red, this.green, this.blue)
    }

    // ── packed-ARGB Int colour operations (zero Color object allocation) ──────
    private inline fun Int.r(): Int = (this shr 16) and 0xFF
    private inline fun Int.g(): Int = (this shr  8) and 0xFF
    private inline fun Int.b(): Int = (this       ) and 0xFF
    private inline fun Int.a(): Int = (this ushr 24)
    private inline fun argb(a: Int, r: Int, g: Int, b: Int): Int =
        (a shl 24) or (r shl 16) or (g shl 8) or b

    fun Int.mul(other: Int): Int = argb(
        a(),
        kotlin.math.min(255, r() * other.r() / 255),
        kotlin.math.min(255, g() * other.g() / 255),
        kotlin.math.min(255, b() * other.b() / 255)
    )

    fun Int.mul(f: Float): Int {
        val c = f.coerceAtLeast(0f)
        return argb(
            a(),
            (r() * c).toInt().coerceIn(0, 255),
            (g() * c).toInt().coerceIn(0, 255),
            (b() * c).toInt().coerceIn(0, 255)
        )
    }

    fun Int.avg(other: Int): Int {
        val alpha = sqrt((a() * other.a()).toDouble()) / 255.0
        val oa = (alpha * 255).toInt().coerceIn(0, 255)
        return argb(
            oa,
            sqrt(r() * other.r() * alpha).toInt().coerceIn(0, 255),
            sqrt(g() * other.g() * alpha).toInt().coerceIn(0, 255),
            sqrt(b() * other.b() * alpha).toInt().coerceIn(0, 255)
        )
    }

    fun Int.avgWeighted(other: Int, weight1: Float, weight2: Float): Int {
        val denom = weight1 + weight2
        return argb(
            a(),
            ((r() * weight1 + other.r() * weight2) / denom).toInt(),
            ((g() * weight1 + other.g() * weight2) / denom).toInt(),
            ((b() * weight1 + other.b() * weight2) / denom).toInt()
        )
    }

    fun Int.withFullAlpha(): Int = this or (0xFF shl 24)
}