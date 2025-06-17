package net.tomczek.ar.puzzle.solver.puzzle.analyzer

import android.content.Context
import com.google.ar.core.Frame
import com.google.ar.core.Session
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity

interface PuzzleAnalyzerStrategy {
    fun canHandle(augmentedImage: String): Boolean
    fun analyzePuzzle(context: Context, session: Session, frame: Frame, foundPuzzle: (PuzzleEntity?) -> Unit)
}