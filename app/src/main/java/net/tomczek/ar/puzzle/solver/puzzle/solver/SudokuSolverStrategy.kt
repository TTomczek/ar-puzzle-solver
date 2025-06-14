package net.tomczek.ar.puzzle.solver.puzzle.solver

import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.SupportedPuzzleTypes
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.SudokuBoard

object SudokuSolverStrategy : PuzzleSolverStrategy {

    override fun canHandle(type: SupportedPuzzleTypes): Boolean {
        return type == SupportedPuzzleTypes.SUDOKU
    }

    override fun solve(puzzleEntity: PuzzleEntity): Pair<Boolean, PuzzleEntity> {
        val sudokuBoard = SudokuBoard.fromPuzzle(puzzleEntity)
        val isSolved = sudokuBoard.solve()
        return Pair<Boolean, PuzzleEntity>(isSolved, sudokuBoard.toPuzzleEntity())
    }
}