package org.example.utils

import java.awt.Color
import kotlin.math.max

object ColorUtils {
    fun Color.mul(color: Color): Color {
        return Color(kotlin.math.min(255, (this.red * (color.red / 255f)).toInt()), kotlin.math.min(255, (this.green * (color.green / 255f)).toInt()), kotlin.math.min(255, (this.blue * (color.blue / 255f)).toInt()), this.alpha)
    }

    fun Color.add(color: Color): Color {
        return Color(kotlin.math.min(255, this.red + color.red), kotlin.math.min(255, this.green + color.green), kotlin.math.min(255, this.blue + color.blue), this.alpha)
    }

    fun Color.min(color: Color): Color {
        return Color(max(0, this.red - color.red), max(0, this.green - color.green), max(0, this.blue - color.blue), this.alpha)
    }

    fun Color.avg(color:Color): Color{
        return Color((this.red+ color.red*2)/3, (this.green+ color.green*2)/3,(this.blue+ color.blue*2)/3 )
    }
}