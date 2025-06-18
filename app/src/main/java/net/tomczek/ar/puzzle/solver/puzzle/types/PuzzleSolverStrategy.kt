package net.tomczek.ar.puzzle.solver.puzzle.types

import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity

interface PuzzleSolverStrategy {
    fun canHandle(type: SupportedPuzzleTypes): Boolean
    fun solve(puzzleEntity: PuzzleEntity): Pair<Boolean, PuzzleEntity>
}