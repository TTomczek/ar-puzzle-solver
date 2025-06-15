package net.tomczek.ar.puzzle.solver.puzzle.analyzer

import android.content.Context
import com.google.ar.core.Frame
import com.google.ar.core.Session
import net.tomczek.ar.puzzle.solver.R
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.viewmodel.ArCameraViewModel

object ImageAnalyzer {
    private val strategies = mutableListOf<PuzzleAnalyzerStrategy>(
        SudokuAnalyzerStrategy
    )

    @Throws(IllegalArgumentException::class)
    fun analyze(context: Context, session: Session, frame: Frame, viewModel: ArCameraViewModel, augmentedImage: String, foundPuzzle: (PuzzleEntity) -> Unit) {
        val strategy = strategies.find { it.canHandle(augmentedImage) }
            ?: throw IllegalArgumentException("No strategy found for augmented image type: $augmentedImage")

        if (viewModel.currentlyProcessing) {
            return
        }
        viewModel.setProcessingState(true)
        viewModel.updateStatusText(R.string.hint_processing)

        strategy.analyzePuzzle(context, session, frame) { puzzle ->
            if (puzzle != null) {
                viewModel.updateStatusText(R.string.hint_searching_puzzle)
                viewModel.resetRecognitionFailures()
                viewModel.setProcessingState(false)
                foundPuzzle(puzzle)
            } else {
                viewModel.incrementRecognitionFailures()
                if (viewModel.recognitionFailures > 2) {
                    viewModel.updateStatusText(R.string.hint_different_angle)
                } else {
                    viewModel.updateStatusText(R.string.hint_searching_puzzle)
                }
                viewModel.setProcessingState(false)
            }
        }
    }
}