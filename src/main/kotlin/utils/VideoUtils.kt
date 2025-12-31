package me.tems.utils

import org.jcodec.api.SequenceEncoder
import org.jcodec.common.model.ColorSpace
import org.jcodec.common.model.Picture
import org.jcodec.scale.AWTUtil
import java.awt.image.BufferedImage
import java.io.File

object VideoUtils {
    fun makeVideoFromImages(images: List<BufferedImage>, outputFile: File, fps: Int = 30) {
        require(images.isNotEmpty()) { "Image list must not be empty" }

        // Create a SequenceEncoder to write MP4 video
        val encoder = SequenceEncoder.createSequenceEncoder(outputFile, fps)

        try {
            for (image in images) {
                val picture: Picture = AWTUtil.fromBufferedImage(image, ColorSpace.RGB)
                encoder.encodeNativeFrame(picture)
            }
        } finally {
            encoder.finish()
        }
    }
}