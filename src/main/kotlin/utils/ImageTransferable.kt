package me.tems.utils

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage

class ImageTransferable(private val image: BufferedImage) : Transferable {

    // Define the data formats (flavors) this object supports
    override fun getTransferDataFlavors(): Array<DataFlavor> {
        // We only support the standard image data flavor
        return arrayOf(DataFlavor.imageFlavor)
    }

    // Check if a specific flavor is supported
    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
        return flavor == DataFlavor.imageFlavor
    }

    // Get the actual data for the supported flavor
    override fun getTransferData(flavor: DataFlavor): Any {
        if (isDataFlavorSupported(flavor)) {
            return image
        }
        throw UnsupportedFlavorException(flavor)
    }
}