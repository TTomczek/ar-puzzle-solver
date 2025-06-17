package net.tomczek.ar.puzzle.solver

import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.SudokuBoard
import org.junit.Assert.assertEquals
import org.junit.Test

class PuzzleTest {

    val sudokuBoard = SudokuBoard(
        id = 1L,
        scanDate = 123456789L,
        board = listOf(1,2,3,4,5,6,7,8,9,0,0),
        originalValuesMask = listOf(true, true, true, true, true, true, true, true, true, false, false)
    )

    @Test
    fun `Should convert SudokuBoard to puzzle`() {
        val puzzle = sudokuBoard.toPuzzleEntity()
        assertEquals(puzzle.id, sudokuBoard.id)
        assertEquals(puzzle.type, sudokuBoard.type)
        assertEquals(puzzle.scanDate, sudokuBoard.scanDate)
        assertEquals("{\"board\":[1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,0.0,0.0],\"originalValuesMask\":[true,true,true,true,true,true,true,true,true,false,false]}", puzzle.data)

        val convertedBack = SudokuBoard.fromPuzzle(puzzle)
        assertEquals(sudokuBoard.id, convertedBack.id)
        assertEquals(sudokuBoard.type, convertedBack.type)
        assertEquals(sudokuBoard.board, convertedBack.board)
        assertEquals(sudokuBoard.originalValuesMask, convertedBack.originalValuesMask)
        assertEquals(sudokuBoard.scanDate, convertedBack.scanDate)

    }
}