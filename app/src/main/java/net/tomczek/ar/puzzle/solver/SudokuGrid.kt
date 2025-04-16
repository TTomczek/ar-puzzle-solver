package net.tomczek.ar.puzzle.solver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme

fun splitSudokuBoardForPresentation(board: SudokuBoard): List<List<List<List<Int>>>> {
    val subGrids = mutableListOf<List<List<List<Int>>>>()
    for (rowBlock in 0 until 3) {
        val rowSubGrids = mutableListOf<List<List<Int>>>()
        for (colBlock in 0 until 3) {
            val subGrid = mutableListOf<List<Int>>()
            for (row in 0 until 3) {
                val startIndex = (rowBlock * 3 + row) * 9 + colBlock * 3
                subGrid.add(board.board.subList(startIndex, startIndex + 3))
            }
            rowSubGrids.add(subGrid)
        }
        subGrids.add(rowSubGrids)
    }
    return subGrids
}

@Preview
@Composable
fun SudokuGridPreview() {
    ArpuzzlesolverTheme {
        SudokuGrid(
            board = SudokuBoard(
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
                )
            )
        )
    }
}

@Composable
fun SudokuGrid(board: SudokuBoard) {
    val subGrids = splitSudokuBoardForPresentation(board)
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        subGrids.forEach { subGridsOfRow ->
            Row {
                subGrids.forEach { subGridsOfColumn ->
                    subGridsOfColumn.forEach { subGrid ->
                        SudokuSubGrid(subGrid)
                    }
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
                .background(color = MaterialTheme.colorScheme.background)
                .border(BorderStroke(2.dp, MaterialTheme.colorScheme.inverseSurface)),
            grid = listOf(
                listOf(5, 3, 0),
                listOf(6, 0, 0),
                listOf(0, 9, 8)
            )
        )
    }
}

@Composable
fun SudokuSubGrid(
    grid: List<List<Int>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.inverseSurface))) {
        grid.forEach { cell ->
            Row {
                cell.forEach { value ->
                    SudokuCell(value = value)
                }
            }
        }
    }
}

@Preview
@Composable
fun SudokuCellPreview() {
    ArpuzzlesolverTheme {
        SudokuCell(5, Modifier.background(color = MaterialTheme.colorScheme.surface))
    }
}

@Composable
fun SudokuCell(value: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.inverseSurface)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (value == 0) "" else value.toString(),
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}