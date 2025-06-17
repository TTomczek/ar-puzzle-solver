package net.tomczek.ar.puzzle.solver

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import java.io.File
import java.io.FileOutputStream

class ImageHelper {
    companion object {
        /**
         * Save the given OpenCV Mat with the given filename to camera roll
         */
        fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String) {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "aps-$fileName"
            )
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Update the media database
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        }

        fun saveMatToFile(context: Context, mat: Mat, fileName: String) {
            val bitmap = createBitmap(mat.cols(), mat.rows(), Config.ARGB_8888)
            Utils.matToBitmap(mat, bitmap)
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "aps-$fileName")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
//            Log.i("MYAPP", "Saved image to aps-$fileName")

            // Update the media database
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        }

        fun saveMatOfPoint2fToFile(context: Context, mat: MatOfPoint2f, fileName: String) {
            val bitmap = createBitmap(mat.cols(), mat.rows(), Config.ARGB_8888)
            Utils.matToBitmap(mat, bitmap)
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "aps-$fileName")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.i("MYAPP", "Saved image to aps-$fileName")

            // Update the media database
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        }

        fun clearSavedImagesByPrefix(prefix: String) {
            Log.i("MYAPP", "Clearing saved images with prefix: $prefix")
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val files = picturesDir.listFiles { file ->
                file.isFile && file.name.startsWith("$prefix-")
            }
            files?.forEach { file ->
                file.delete()
            }
        }

        fun saveInputImageToFile(context: Context, inputImage: InputImage, fileName: String) {
            val bitmap = Bitmap.createBitmap(inputImage.width, inputImage.height, Config.ARGB_8888)
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "aps-${fileName}")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Update the media database
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        }
    }
}