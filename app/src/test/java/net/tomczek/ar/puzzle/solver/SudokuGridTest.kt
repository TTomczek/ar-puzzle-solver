package net.tomczek.ar.puzzle.solver

import net.tomczek.ar.puzzle.solver.composables.sudoku.splitSudokuBoardForPresentation
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.SudokuBoard
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class SudokuGridTest {

    @Test
    fun splitSudokuBoardForPresentationTest() {
        val board = SudokuBoard(
            board = listOf(
                5, 3, 0, 0, 7, 0, 0, 0, 0,
                6, 0, 0, 1, 9, 5, 0, 0, 0,
                0, 9, 8, 0, 0, 0, 0, 6, 0,
                8, 0, 0, 0, 6, 0, 0, 0, 3,
                4, 0, 0, 8, 0, 3, 0, 0, 1,
                7, 0, 0, 0, 2, 0, 0, 0, 6,
                0, 6, 0, 0, 0, 0, 2, 8, 0,
                0, 0, 0, 4, 1, 9, 0, 0, 5,
                0, 0, 0, 0, 8, 0, 0, 7, 9
            ),
            originalValuesMask = listOf(
                true, true, false, false, true, false, false, false, false,
                true, false, false, true, true, true, false, false, false,
                false, true, true, false, false, false, false, true, false,
                true, false, false, false, true, false, false, false, true,
                true, false, false, true, false, true, false, false, true,
                true, false, false, false, true, false, false, false, true,
                false, true, false, false, false, false, true, true, false,
                false, false, false, true, true, true, false, false, true,
                false, false, false, false, true, false, false, true, true
            )
        )

        val subGridsRows = splitSudokuBoardForPresentation(board)
        assertThat("No three rows of subgrids", subGridsRows.size, `is`(3))
        assertThat("No three subgrids per row", subGridsRows[0].size, `is`(3))
        assertThat("No three rows per subgrid", subGridsRows[0][0].size, `is`(3))
        assertThat("No three columns per subgrid", subGridsRows[0][0][0].size, `is`(3))
        assertThat("No three columns per subgrid", subGridsRows[0][0][1].size, `is`(3))
        assertThat("No three columns per subgrid", subGridsRows[0][0][2].size, `is`(3))
        assertThat("First row of first subgrid incorrect", subGridsRows[0][0][0], `is`(listOf(5, 3, 0)))
        assertThat("Second row of first subgrid incorrect", subGridsRows[0][0][1], `is`(listOf(6, 0, 0)))
        assertThat("Third row of first subgrid incorrect", subGridsRows[0][0][2], `is`(listOf(0, 9, 8)))
        assertThat("First row of second subgrid incorrect", subGridsRows[1][0][0], `is`(listOf(8, 0, 0)))
        assertThat("Second row of second subgrid incorrect", subGridsRows[1][0][1], `is`(listOf(4, 0, 0)))
        assertThat("Third row of second subgrid incorrect", subGridsRows[1][0][2], `is`(listOf(7, 0, 0)))


    }
}