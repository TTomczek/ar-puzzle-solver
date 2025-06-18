package net.tomczek.ar.puzzle.solver.puzzle.types

import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.PuzzleSolverStrategy
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.SudokuSolverStrategy

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