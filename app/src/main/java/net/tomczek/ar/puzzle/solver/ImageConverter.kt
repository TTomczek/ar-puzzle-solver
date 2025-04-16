package net.tomczek.ar.puzzle.solver

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.nio.ByteBuffer

class ImageConverter {
    companion object {

        fun imageToBitmap(image: Image): Bitmap? {
            if (image.format == ImageFormat.YUV_420_888) {
                val buffer: ByteBuffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                return yuv420888ToBitmap(image)
            }
            return null
        }

        fun yuv420888ToBitmap(image: Image): Bitmap? {
            if (image.format != ImageFormat.YUV_420_888) {
                throw IllegalArgumentException("Das Bild ist nicht im YUV_420_888-Format.")
            }

            val width = image.width
            val height = image.height

            // Extrahiert die Y-, U- und V-Ebenen aus dem Bild
            val yBuffer = image.planes[0].buffer // Y-Ebene
            val uBuffer = image.planes[1].buffer // U-Ebene
            val vBuffer = image.planes[2].buffer // V-Ebene

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            // Kombiniert die YUV-Daten in ein einziges Array
            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize) // V folgt Y
            uBuffer.get(nv21, ySize + vSize, uSize) // U folgt V

            // Dekodiert NV21 zu Bitmap
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val outputStream = java.io.ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, outputStream)
            val jpegBytes = outputStream.toByteArray()
            return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
        }
    }
}