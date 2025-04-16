package net.tomczek.ar.puzzle.solver

import org.junit.Assert.*
import org.junit.Test

class SudokuBoardTest {

    @Test
    fun shouldSolveBoard() {
        val sudokuField = SudokuBoard(
            board = listOf(
                8, 9, 0, 7, 3, 0, 4, 6, 0,
                0, 4, 0, 2, 0, 8, 3, 5, 7,
                7, 0, 3, 0, 0, 0, 8, 9, 2,
                4, 6, 9, 3, 5, 7, 2, 0, 8,
                0, 0, 0, 9, 8, 0, 0, 0, 5,
                5, 1, 0, 4, 0, 0, 0, 3, 9,
                6, 8, 0, 0, 0, 9, 0, 7, 0,
                0, 7, 1, 8, 4, 3, 0, 0, 6,
                0, 3, 5, 1, 0, 0, 0, 8, 4
            ),
            originalValuesMask = listOf(
                true, true, false, true, true, false, true, true, false,
                false, true, false, true, false, true, true, true, true,
                true, false, true, false, false, false, true, true, true,
                true, true, true, true, true, true, true, false, true,
                false, false, false, true, true, false, false, false, true,
                true, true, false, true, false, false, false, true, true,
                true, true, false, false, false, true, false, true, false,
                false, true, true, true, true, true, false,false,true,
                false, true, true, true, false, false, false, true, true
            )
        )
        val result = sudokuField.solve()
        assertNotNull(result)
        assertEquals(result, listOf<Int>(
            8, 9, 2, 7, 3, 5, 4, 6, 1,
            1, 4, 6, 2, 9, 8, 3, 5, 7,
            7, 5, 3, 6, 1, 4, 8, 9, 2,
            4, 6, 9, 3, 5, 7, 2, 1, 8,
            3, 2, 7, 9, 8, 1, 6, 4, 5,
            5, 1, 8, 4, 6, 2, 7, 3, 9,
            6, 8, 4, 5, 2, 9, 1, 7, 3,
            9, 7, 1, 8, 4, 3, 5, 2, 6,
            2, 3, 5, 1, 7, 6, 9, 8, 4
        ))
    }

    @Test
    fun shouldNotSolveInvalidBoard() {
        val sudokuField = SudokuBoard(
            board = listOf(
                8, 9, 1, 7, 3, 0, 4, 6, 0,
                0, 4, 0, 2, 0, 8, 3, 5, 7,
                7, 0, 3, 0, 0, 0, 8, 9, 2,
                4, 6, 9, 3, 5, 7, 2, 0, 8,
                0, 0, 0, 9, 8, 0, 0, 0, 5,
                5, 1, 0, 4, 0, 0, 0, 3, 9,
                6, 8, 0, 0, 0, 9, 0, 7, 0,
                0, 7, 1, 8, 4, 3, 0, 0, 6,
                0, 3, 5, 1, 0, 0, 0, 8, 4
            ),
            originalValuesMask = listOf(
                true, true, false, true, true, false, true, true, false,
                false, true, false, true, false, true, true, true, true,
                true, false, true, false, false, false, true, true, true,
                true, true, true, true, true, true, true, false, true,
                false, false, false, true, true, false, false, false, true,
                true, true, false, true, false, false, false, true, true,
                true, true, false, false, false, true, false, true, false,
                false, true, true, true, true, true, false,false,true,
                false, true, true, true, false, false, false, true, true
            )
        )
        val result = sudokuField.solve()
        assertNull(result)
    }

    @Test
    fun shouldThrowExceptionForInvalidBoardSize() {
        val sudokuField = SudokuBoard(
            board = listOf(1, 2, 3),
            originalValuesMask = listOf(true, false, true)
        )
        try {
            sudokuField.solve()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid board size. Expected 81 cells.", e.message)
        }
    }


}