package org.example.utils

import java.awt.Color
import kotlin.math.max

object ColorUtils {
    fun Color.mul(color: Color): Color {
        return Color(
            kotlin.math.min(255, (this.red * (color.red / 255f)).toInt()),
            kotlin.math.min(255, (this.green * (color.green / 255f)).toInt()),
            kotlin.math.min(255, (this.blue * (color.blue / 255f)).toInt()),
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

    fun Color.avg(color: Color): Color {
        return Color((this.red + color.red) / 2, (this.green + color.green) / 2, (this.blue + color.blue) / 2)
    }

    fun Color.avg(colors: List<Color>): Color {
        var red = this.red
        var green = this.green
        var blue = this.blue
        for (color in colors){
            red+= color.red
            green+= color.green
            blue+= color.blue
        }
        red/= (colors.size+1)
        green/= (colors.size+1)
        blue/= (colors.size+1)
        return Color(red,green,blue);
    }
}