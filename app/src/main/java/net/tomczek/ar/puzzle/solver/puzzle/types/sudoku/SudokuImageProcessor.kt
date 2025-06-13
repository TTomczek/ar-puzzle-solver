package net.tomczek.ar.puzzle.solver.puzzle.types.sudoku


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
import net.tomczek.ar.puzzle.solver.ImageConverter
import net.tomczek.ar.puzzle.solver.ImageProcessingException
import org.opencv.core.Core


class SudokuImageProcessor {

    init {
        OpenCVLoader.initLocal()
    }

    @Throws(ImageProcessingException::class)
    suspend fun processImage(context: Context, image: Image): SudokuBoard? {

        try {
            val bitmapImage = ImageConverter.Companion.imageToBitmap(image)
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

            val cellsAsInputImage = sudokuCells.mapIndexed { index, cell ->
                val bitmapCell = matToBitmap(cell)
                InputImage.fromBitmap(bitmapCell, 0)
            }

            val extractedNumbersFromCells = extractNumbersFromCells(
                context,
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS),
                cellsAsInputImage,
                sudokuCells
            )

            val sudokuBoard = SudokuBoard(
                board = extractedNumbersFromCells.map { it.recognizedText.toIntOrNull() ?: 0 },
                originalValuesMask = extractedNumbersFromCells.map { it.recognizedText.isNotEmpty() }
            )
            return sudokuBoard
        } catch (e: Exception) {
            Log.e("SudokuImageProcessor", "Error processing image: ${e.message}")
            return null
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
        val grayMat = Mat(bitmapImage.width, bitmapImage.height, CvType.CV_8UC1)
        Utils.bitmapToMat(bitmapImage, grayMat)
        Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_RGB2GRAY)
        return grayMat
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
        // Höhere Auflösung für mehr Details
        val transformedImageWidth = 288.0

        // Zielpunkte für die Transformation
        val dstPoints = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(transformedImageWidth - 1, 0.0),
            Point(transformedImageWidth - 1, transformedImageWidth - 1),
            Point(0.0, transformedImageWidth - 1)
        )

        // Transformationsmatrix berechnen
        val transformationMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

        // Perspektivtransformation mit verbesserter Interpolation
        val transformedGrid = Mat()
        Imgproc.warpPerspective(
            grayScaleCroppedImage,
            transformedGrid,
            transformationMatrix,
            Size(transformedImageWidth, transformedImageWidth),
            Imgproc.INTER_CUBIC // Verbesserte Interpolation für schärfere Ergebnisse
        )

        // Hintergrund aufhellen mit Gamma-Korrektur
        val gammaImg = Mat()
        transformedGrid.convertTo(gammaImg, CvType.CV_32F, 1.0/255.0, 0.0)
        val gamma = 0.7 // Wert < 1 hellt auf, > 1 verdunkelt
        Core.pow(gammaImg, gamma, gammaImg)
        gammaImg.convertTo(gammaImg, CvType.CV_8U, 255.0, 0.0)

        // Bildschärfung anwenden
        val sharpened = Mat()
        val kernel = Mat(3, 3, CvType.CV_32F)
        kernel.put(0, 0, -1.0, -1.0, -1.0)
        kernel.put(1, 0, -1.0, 9.0, -1.0)
        kernel.put(2, 0, -1.0, -1.0, -1.0)
        Imgproc.filter2D(gammaImg, sharpened, -1, kernel)

//        // CLAHE (Contrast Limited Adaptive Histogram Equalization) anwenden
//        val clahe = Imgproc.createCLAHE(3.0, Size(8.0, 8.0))
//        val claheResult = Mat()
//        clahe.apply(sharpened, claheResult)
//
//        // Adaptives Thresholding für lokale Kontrastverbesserung
//        val binarized = Mat()
//        Imgproc.adaptiveThreshold(
//            claheResult,
//            binarized,
//            255.0,
//            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
//            Imgproc.THRESH_BINARY_INV,
//            11,
//            2.0
//        )
//
//        // Morphologische Operationen zum Entfernen von Rauschen
//        val element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0, 2.0))
//        val cleaned = Mat()
//        // Hier war vorher Imgproc.MORPH_OPEN, aber MORPH_CLOSE könnte besser sein
//        Imgproc.morphologyEx(binarized, cleaned, Imgproc.MORPH_CLOSE, element)
//
//        // Invertieren, um hellen Hintergrund und dunkle Ziffern zu erhalten
//        val result = Mat()
//        Core.bitwise_not(cleaned, result)

        return sharpened
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
            }
        }

        return sudokuCells
    }

    fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = createBitmap(mat.cols(), mat.rows())
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

    fun sharpenImage(mat: Mat): Mat {
        val sharpened = Mat()
        val kernel = Mat(3, 3, CvType.CV_32F, Scalar(-1.0))
        kernel.put(1, 1, 9.0)
        Imgproc.filter2D(mat, sharpened, mat.depth(), kernel)
        return sharpened
    }

    suspend fun extractNumbersFromCells(
        context: Context,
        textRecognizer: TextRecognizer,
        cells: List<InputImage>,
        originalMats: List<Mat>
    ): List<CellRegocnitionResult> = coroutineScope {
        cells.mapIndexed { index, cell ->
            async(Dispatchers.Default) {
                val confidenceMap = mutableMapOf<String, MutableList<Float>>()

                for (attempt in 1..6) {
                    try {
                        val inputImage = if (attempt == 1) {
                            cell // Originalbild beim ersten Versuch
                        } else {
                            // Geschärftes Bild oder andere Varianten für weitere Versuche
                            val processedMat = when (attempt) {
                                2 -> sharpenImage(originalMats[index])
                                3 -> applyCLAHE(originalMats[index])
                                4 -> invertImage(originalMats[index])
                                5 -> applyMorphology(originalMats[index], Imgproc.MORPH_OPEN)
                                6 -> applyMorphology(originalMats[index], Imgproc.MORPH_CLOSE)
                                else -> originalMats[index]
                            }
                            val processedBitmap = matToBitmap(processedMat)
                            InputImage.fromBitmap(processedBitmap, 0)
                        }

                        val result = textRecognizer.process(inputImage).await()

                        if (result != null && result.textBlocks.isNotEmpty() &&
                            result.textBlocks[0].lines.isNotEmpty()) {

                            val firstLine = result.textBlocks[0].lines[0]
                            val text = firstLine.text.trim()
                            val confidence = firstLine.confidence

                            if (text.length == 1 && text.matches(Regex("[1-9]"))) {
                                confidenceMap.getOrPut(text) { mutableListOf() }.add(confidence)

                                if (confidence >= 0.75f) {
                                    return@async CellRegocnitionResult(index, text, confidence)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.i("MYAPP", "Fehler bei Versuch $attempt für Zelle $index: ${e.message}")
                    }
                }

                // Wenn keine Confidence über 0.8 erreicht wurde, berechne den Durchschnitt
                val bestGuess = confidenceMap.maxByOrNull { (_, confidences) ->
                    confidences.average()
                }

                if (bestGuess != null) {
                    val (text, confidences) = bestGuess
                    val averageConfidence = confidences.average().toFloat()
                    return@async CellRegocnitionResult(index, text, averageConfidence)
                }

                return@async CellRegocnitionResult(index, "", 0.0f)
            }
        }.map { it.await() }
    }

    fun applyCLAHE(mat: Mat): Mat {
        val clahe = Imgproc.createCLAHE(3.0, Size(8.0, 8.0))
        val result = Mat()
        clahe.apply(mat, result)
        return result
    }

    fun invertImage(mat: Mat): Mat {
        val inverted = Mat()
        Core.bitwise_not(mat, inverted)
        return inverted
    }

    fun applyMorphology(mat: Mat, operation: Int): Mat {
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        val result = Mat()
        Imgproc.morphologyEx(mat, result, operation, kernel)
        return result
    }
}