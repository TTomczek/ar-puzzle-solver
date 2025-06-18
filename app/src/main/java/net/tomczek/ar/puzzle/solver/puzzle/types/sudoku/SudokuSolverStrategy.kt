package net.tomczek.ar.puzzle.solver.puzzle.types.sudoku

import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.PuzzleSolverStrategy
import net.tomczek.ar.puzzle.solver.puzzle.types.SupportedPuzzleTypes

object SudokuSolverStrategy : PuzzleSolverStrategy {

    override fun canHandle(type: SupportedPuzzleTypes): Boolean {
        return type == SupportedPuzzleTypes.SUDOKU
    }

    override fun solve(puzzleEntity: PuzzleEntity): Pair<Boolean, PuzzleEntity> {
        val sudokuBoard = SudokuBoard.Companion.fromPuzzle(puzzleEntity)
        val isSolved = sudokuBoard.solve()
        return Pair<Boolean, PuzzleEntity>(isSolved, sudokuBoard.toPuzzleEntity())
    }
}