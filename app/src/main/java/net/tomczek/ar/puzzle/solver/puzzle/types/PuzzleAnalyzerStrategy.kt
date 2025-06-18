package net.tomczek.ar.puzzle.solver.puzzle.types

import com.google.ar.core.Frame
import com.google.ar.core.Session
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity

interface PuzzleAnalyzerStrategy {
    fun canHandle(augmentedImage: String): Boolean
    fun analyzePuzzle(session: Session, frame: Frame, foundPuzzle: (PuzzleEntity?) -> Unit)
}