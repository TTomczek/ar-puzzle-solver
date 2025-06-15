package net.tomczek.ar.puzzle.solver.composables.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.tomczek.ar.puzzle.solver.R
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme

@Preview
@Composable
fun SudokuCellCorrectionModalPreview() {
    ArpuzzlesolverTheme {
        SudokuCellCorrectionModal(initialValue = 7, true) { _, _ -> }
    }
}

@Composable
fun SudokuCellCorrectionModal(
    initialValue: Int,
    isOriginalValue: Boolean,
    onValueChange: (Int, Boolean) -> Unit
) {
    var value by remember { mutableIntStateOf(initialValue) }

    Surface {
        Column {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3)
            ) {
                items(9) { index ->
                    SudokuCorrectionCell(index, (index + 1 == value) && isOriginalValue) { correctedValue ->
                        onValueChange(correctedValue, true)
                    }
                }
                item { Spacer(modifier = Modifier.fillMaxWidth(1f / 3f).aspectRatio(1f).border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface)) }
                item {
                    SudokuCorrectionCell(index = -1, isSelected = (0 == value) || !isOriginalValue, cellText = stringResource(R.string.sudoku_correction_cell_empty)) { correctedValue ->
                        onValueChange(correctedValue, false)
                    }
                }
                item { Spacer(modifier = Modifier.fillMaxWidth(1f / 3f).aspectRatio(1f).border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface)) }
            }
        }
    }
}

@Preview
@Composable
fun SudokuCorrectionCellPreview() {
    ArpuzzlesolverTheme {
        Box(modifier = Modifier.size(100.dp).aspectRatio(1f).background(Color.White)) {
            SudokuCorrectionCell(index = 0, isSelected = true, cellText = "Leeres Feld") { }
        }
    }
}

@Composable
fun SudokuCorrectionCell(index: Int, isSelected: Boolean, cellText: String? = null, clicked: (value: Int) -> Unit) {
    val cellValue = index + 1
    Text(
        text = cellText ?: cellValue.toString(),
        modifier = Modifier
            .fillMaxWidth(1f / 3f)
            .aspectRatio(1f)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            .clickable {
                clicked(cellValue)
            }
            .wrapContentHeight(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineMedium
    )
}