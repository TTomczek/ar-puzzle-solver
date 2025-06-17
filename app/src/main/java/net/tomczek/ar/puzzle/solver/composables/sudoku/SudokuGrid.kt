package net.tomczek.ar.puzzle.solver.composables.sudoku

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.SudokuBoard
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme

data class SudokuGridCellConfig(
    val index: Int,
    val value: Int,
    val isOriginal: Boolean
)

fun splitSudokuBoardForPresentation(board: SudokuBoard): List<List<List<List<SudokuGridCellConfig>>>> {
    val subGrids = mutableListOf<List<List<List<SudokuGridCellConfig>>>>()
    for (rowBlock in 0 until 3) {
        val rowSubGrids = mutableListOf<List<List<SudokuGridCellConfig>>>()
        for (colBlock in 0 until 3) {
            val subGrid = mutableListOf<List<SudokuGridCellConfig>>()
            for (row in 0 until 3) {
                val startIndex = (rowBlock * 3 + row) * 9 + colBlock * 3
                val values = board.board.subList(startIndex, startIndex + 3)
                val originalValues = board.originalValuesMask.subList(startIndex, startIndex + 3)
                val mergedList =
                    values.zip(originalValues).mapIndexed { index, (value, isOriginal) ->
                        SudokuGridCellConfig(
                            index = startIndex + index,
                            value = value,
                            isOriginal = isOriginal
                        )
                    }
                subGrid.add(mergedList)
            }
            rowSubGrids.add(subGrid)
        }
        subGrids.add(rowSubGrids)
    }
    return subGrids
}

@Preview
@Composable
fun SudokuGridShowDontSolutionPreview() {
    val sudokuBoard = SudokuBoard(
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
            false, false, false, false, true, false, false, true, true,
        ),
        scanDate = null
    )
    sudokuBoard.solve()
    ArpuzzlesolverTheme {
        SudokuGrid(
            board = sudokuBoard,
            showSolution = false
        )
    }
}

@Preview
@Composable
fun SudokuGridShowSolutionPreview() {
    val sudokuBoard = SudokuBoard(
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
            false, false, false, false, true, false, false, true, true,
        ),
        scanDate = null
    )
    sudokuBoard.solve()
    ArpuzzlesolverTheme {
        SudokuGrid(
            board = sudokuBoard,
            showSolution = true
        )
    }
}

@Composable
fun SudokuGrid(board: SudokuBoard, showSolution: Boolean = false, cellClick: (Int) -> Unit = {}) {
    val subGrids = splitSudokuBoardForPresentation(board)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        subGrids.forEachIndexed { _, subGridsOfRow ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                subGridsOfRow.forEachIndexed { _, subGridsOfColumn ->
                    SudokuSubGrid(
                        subGridsOfColumn,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        showSolution = showSolution,
                        cellClick = cellClick
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SudokuSubGridPreview() {
    ArpuzzlesolverTheme {
        SudokuSubGrid(
            modifier = Modifier
                .border(BorderStroke(2.dp, MaterialTheme.colorScheme.inverseSurface))
                .aspectRatio(1f),
            showSolution = true,
            grid = listOf(
                listOf(
                    SudokuGridCellConfig(0, 5, true), SudokuGridCellConfig(1, 3, true),
                    SudokuGridCellConfig(2, 1, false)
                ),
                listOf(
                    SudokuGridCellConfig(3, 6, true), SudokuGridCellConfig(4, 4, false),
                    SudokuGridCellConfig(5, 7, false)
                ),
                listOf(
                    SudokuGridCellConfig(6, 2, false), SudokuGridCellConfig(7, 9, true),
                    SudokuGridCellConfig(8, 8, true)
                )
            )
        )
    }
}

@Composable
fun SudokuSubGrid(
    grid: List<List<SudokuGridCellConfig>>,
    modifier: Modifier = Modifier,
    showSolution: Boolean = false,
    cellClick: (Int) -> Unit = {}
) {
    Column(
        modifier = modifier.border(
            BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.inverseSurface
            )
        )
    ) {
        grid.forEach { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                row.forEach { config ->
                    SudokuCell(
                        config = config,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        showSolution = showSolution,
                        cellClick = cellClick
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SudokuCellShowSolutionPreview() {
    ArpuzzlesolverTheme {
        SudokuCell(
            SudokuGridCellConfig(2, 5, false),
            Modifier.background(color = Color.Transparent),
            showSolution = true
        )
    }
}

@Preview
@Composable
fun SudokuCellOriginalPreview() {
    ArpuzzlesolverTheme {
        SudokuCell(
            SudokuGridCellConfig(2, 5, true),
            Modifier.background(color = Color.Transparent),
            showSolution = true
        )
    }
}

@Composable
fun SudokuCell(
    config: SudokuGridCellConfig,
    modifier: Modifier = Modifier,
    showSolution: Boolean = false,
    cellClick: (index: Int) -> Unit = {}
) {
    val (index, value, isOriginal) = config
    Box(
        modifier = modifier
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.inverseSurface)),
        contentAlignment = Alignment.Center,
    ) {
        val text = when {
            value == 0 -> ""
            showSolution -> value.toString()
            isOriginal -> value.toString()
            else -> ""
        }
        Text(
            text = text,
            modifier = Modifier
                .fillMaxSize()
                .clickable(true) { cellClick(index) }
                .wrapContentSize(Alignment.Center),  // Sorgt für perfekte Zentrierung
            color = if (isOriginal) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge  // Größere Schriftart für bessere Sichtbarkeit
        )
    }
}
