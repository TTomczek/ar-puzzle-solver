package net.tomczek.ar.puzzle.solver


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfInt
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.tasks.await


class SudokuImageProcessor {

    init {
        OpenCVLoader.initLocal()
    }

    @Throws(ImageProcessingException::class)
    suspend fun processImage(context: Context, image: Image): SudokuBoard {

        try {
            val bitmapImage = ImageConverter.imageToBitmap(image)
            if (bitmapImage == null) {
                throw ImageProcessingException("Failed to convert image to bitmap.")
            }

            val rotatedImage = rotateImageUpright(bitmapImage)

            val grayImage = toGrayscale(rotatedImage)
            if (grayImage.empty()) {
                throw ImageProcessingException("Failed to convert image to grayscale.")
            }

            val largestContour = findLargestContourInImage(context, grayImage)
            if (largestContour == null) {
                throw ImageProcessingException("Failed to find largest contour.")
            }

            val largestQuadrilateral = getLargestQuadrilateral(largestContour)

            val sudokuGrid = createAndApplyMaskOfLargestContour(largestQuadrilateral, grayImage)
            if (sudokuGrid.empty()) {
                throw ImageProcessingException("Failed to create and apply mask of largest contour.")
            }

            val sortedQuadrilateral = sortPoints(largestQuadrilateral)

            val transformedGrid = transformSudokuGrid(context, sudokuGrid, sortedQuadrilateral)
            if (transformedGrid.empty()) {
                throw ImageProcessingException("Failed to transform Sudoku grid.")
            }

            val sudokuCells = extractSudokuCells(context, transformedGrid)
            if (sudokuCells.isEmpty() || sudokuCells.size != 81) {
                throw ImageProcessingException("Failed to extract 81 Sudoku cells.")
            }

            val cellsAsInputImage = sudokuCells.map { cell ->
                val bitmapCell = matToBitmap(cell)
                InputImage.fromBitmap(bitmapCell, 0)
            }

            val extractedNumbersFromCells = extractNumbersFromCells(
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS),
                cellsAsInputImage
            )

            val sudokuBoard = SudokuBoard(
                extractedNumbersFromCells.map { it.recognizedText.toIntOrNull() ?: 0 },
                extractedNumbersFromCells.map { it.recognizedText.isNotEmpty() }
            )
            return sudokuBoard
        } catch (e: Exception) {
            Log.e("SudokuImageProcessor", "Error processing image: ${e.message}")
            throw ImageProcessingException("Failed to process image: ${e.message}", e)
        }
    }

    /**
     * Get the rotation of the given image and rotate it to upright.
     */
    fun rotateImageUpright(bitmapImage: Bitmap): Bitmap {
        val matrix = Matrix()
        // TODO The image is always rotated 90 degrees counter-clockwise. Why?
        matrix.postRotate(90f)

        return Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.width, bitmapImage.height, matrix, true)
    }

    /**
     * Convert the given Image to Grayscale with OpenCV.
     */
    fun toGrayscale(bitmapImage: Bitmap): Mat {
        val tmpMat = Mat(bitmapImage.width, bitmapImage.height, CvType.CV_8UC1)
        Utils.bitmapToMat(bitmapImage, tmpMat)
        Imgproc.cvtColor(tmpMat, tmpMat, Imgproc.COLOR_RGB2GRAY)
        return tmpMat
    }

    fun findLargestContourInImage(context: Context, grayScaleImage: Mat): MatOfPoint? {
        val blurredImage = Mat()
        Imgproc.GaussianBlur(grayScaleImage, blurredImage, Size(5.0, 5.0), 0.0)
        val edges = Mat()
        Imgproc.Canny(blurredImage, edges, 50.0, 200.0)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.CHAIN_APPROX_NONE)

        // Find the largest contour which should be the Sudoku grid
        var largestContour: MatOfPoint? = null
        var maxArea = 0.0
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > maxArea) {
                maxArea = area
                largestContour = contour
            }
        }
        return largestContour
    }

    fun getLargestQuadrilateral(contour: MatOfPoint): MatOfPoint2f {
        // Berechne die konvexe Hülle der Kontur
        val hull = MatOfInt()
        Imgproc.convexHull(contour, hull)

        // Extrahiere die Punkte der konvexen Hülle
        val hullPoints = mutableListOf<Point>()
        val contourArray = contour.toArray()
        for (index in hull.toArray()) {
            hullPoints.add(contourArray[index])
        }

        // Konvertiere die Punkte der Hülle in ein MatOfPoint2f
        val hullMat = MatOfPoint2f(*hullPoints.toTypedArray())

        // Approximieren der Hülle auf ein Viereck
        val approx = MatOfPoint2f()
        val epsilon = 0.02 * Imgproc.arcLength(hullMat, true)
        Imgproc.approxPolyDP(hullMat, approx, epsilon, true)

        // Überprüfen, ob das Ergebnis 4 Punkte hat
        if (approx.toArray().size == 4) {
            return approx
        }

        return approx
    }

    fun createAndApplyMaskOfLargestContour(largestContour: MatOfPoint2f, grayScaleImage: Mat): Mat {
        // Create a mask for the Sudoku grid
        val mask = Mat(grayScaleImage.size(), CvType.CV_8UC1, Scalar(0.0))
        Imgproc.drawContours(mask, listOf(MatOfPoint(*largestContour.toArray())), -1, Scalar(255.0), -1)

        // Apply the mask to the original image to extract the Sudoku grid
        val sudokuGrid = Mat()
        grayScaleImage.copyTo(sudokuGrid, mask)

        return sudokuGrid
    }

    fun sortPoints(points: MatOfPoint2f): MatOfPoint2f {
        val sortedPoints = points.toArray().sortedWith(compareBy({ it.y }, { it.x }))

        // Obere zwei Punkte (nach y-Wert sortiert)
        val topPoints = sortedPoints.take(2).sortedBy { it.x }
        // Untere zwei Punkte (nach y-Wert sortiert)
        val bottomPoints = sortedPoints.takeLast(2).sortedBy { it.x }

        return MatOfPoint2f(
            topPoints[0],  // Oben links
            topPoints[1],  // Oben rechts
            bottomPoints[1],  // Unten rechts
            bottomPoints[0]   // Unten links
        )
    }

    /**
     * Transform the Sudoku grid to a standard size and perspective.
     */
    fun transformSudokuGrid(context: Context, grayScaleCroppedImage: Mat, srcPoints: MatOfPoint2f): Mat {
        val transformedImageWidth = 288.0
        // Definiere die Zielpunkte für die Transformation
        val dstPoints = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(transformedImageWidth - 1, 0.0),
            Point(transformedImageWidth - 1, transformedImageWidth - 1),
            Point(0.0, transformedImageWidth - 1)
        )

        // Berechne die Transformationsmatrix
        val transformationMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

        // Wende die Perspektivtransformation an
        val transformedGrid = Mat()
        Imgproc.warpPerspective(grayScaleCroppedImage, transformedGrid, transformationMatrix, Size(transformedImageWidth, transformedImageWidth))

        return transformedGrid
    }

    /**
     * Extract the individual cells from the Sudoku grid.
     */
    fun extractSudokuCells(context: Context, transformedSudokuGrid: Mat): List<Mat> {
        val cellSize = transformedSudokuGrid.width() / 9
        val sudokuCells = mutableListOf<Mat>()

        for (i in 0 until 9) {
            for (j in 0 until 9) {
                val cell = Mat(transformedSudokuGrid, Rect(j * cellSize, i * cellSize, cellSize, cellSize))
                sudokuCells.add(cell)
                ImageHelper.saveMatToFile(context, cell, "sudoku_cell_${i}_${j}.png")
            }
        }

        return sudokuCells
    }

    fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = createBitmap(mat.cols(), mat.rows())
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

    @Throws(ImageProcessingException::class)
    suspend fun extractNumbersFromCells(
        textRecognizer: TextRecognizer,
        cells: List<InputImage>
    ): List<RecognizedNumberOfCell> = coroutineScope {
        cells.mapIndexed { index, cell ->
            async(Dispatchers.IO) {
                try {
                    val result = textRecognizer.process(cell).await()

                    if (result == null) {
                        throw ImageProcessingException("No result")
                    } else if (result.textBlocks.isEmpty() || result.textBlocks[0].lines.isEmpty()) {
                        RecognizedNumberOfCell(index, "", 0.0f)
                    } else if (result.textBlocks[0].lines[0].confidence < 0.7) {
                        throw ImageProcessingException("Confidence on index $index with text ${result.textBlocks[0].lines[0].text} too low: ${result.textBlocks[0].lines[0].confidence}")
                    } else {
                        val firstLineOfFirstBlock = result.textBlocks[0].lines[0]
                        val text = firstLineOfFirstBlock.text.trim()
                        val confidence = firstLineOfFirstBlock.confidence
                        RecognizedNumberOfCell(index, text, confidence)
                    }

                } catch (e: Exception) {
                    Log.i("SudokuImageProcessor", "Error extracting number from cell with index ${index}: ${e.message}")
                    throw ImageProcessingException("Failed to extract number from cell: ${e.message}")
                }
            }
        }.map { it.await() }
    }
}