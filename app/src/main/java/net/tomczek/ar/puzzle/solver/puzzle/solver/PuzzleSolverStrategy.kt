package net.tomczek.ar.puzzle.solver.puzzle.solver

import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.SupportedPuzzleTypes

interface PuzzleSolverStrategy {
    fun canHandle(type: SupportedPuzzleTypes): Boolean
    fun solve(puzzleEntity: PuzzleEntity): Pair<Boolean, PuzzleEntity>
}