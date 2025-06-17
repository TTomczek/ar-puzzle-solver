package net.tomczek.ar.puzzle.solver.puzzle.solver

import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity

object PuzzleSolver {
    private val strategies = mutableListOf<PuzzleSolverStrategy>(
        SudokuSolverStrategy
    )

    @Throws(IllegalArgumentException::class)
    fun solve(puzzleEntity: PuzzleEntity): Pair<Boolean, PuzzleEntity> {
        val strategy = strategies.find { it.canHandle(puzzleEntity.type) }
            ?: throw IllegalArgumentException("No strategy found for puzzle type: ${puzzleEntity.type}")
        return strategy.solve(puzzleEntity)
    }
}