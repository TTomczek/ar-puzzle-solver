package net.tomczek.ar.puzzle.solver.puzzle.types.sudoku

import android.content.Context
import android.media.Image
import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.PuzzleAnalyzerStrategy
import net.tomczek.ar.puzzle.solver.puzzle.types.SupportedPuzzleTypes

object SudokuAnalyzerStrategy : PuzzleAnalyzerStrategy {

    override fun canHandle(augmentedImage: String): Boolean {
        return augmentedImage == SupportedPuzzleTypes.SUDOKU.string
    }

    override fun analyzePuzzle(session: Session, frame: Frame, finishedProcessing: (PuzzleEntity?) -> Unit) {
        lateinit var image: Image
        try {
            image = frame.acquireCameraImage()
            session.update()
            processSudokuImageInCoroutine(image) { sudokuBoard ->
                finishedProcessing(sudokuBoard?.toPuzzleEntity())
                image.close()
            }
        } catch (_: Exception) {
            image.close()
            finishedProcessing(null)
        }
    }

    private fun processSudokuImageInCoroutine(
        image: Image,
        finishedProcessing: (result: SudokuBoard?) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    Log.i("MYAPP", "Processing image...")
                    val recognitionResult = SudokuImageProcessor().processImage(image)
                    finishedProcessing(recognitionResult)
                } catch (e: Exception) {
                    Log.i("MYAPP", "Error processing image: ${e.message}")
                    finishedProcessing(null)
                }
            }
        }
    }
}