package net.tomczek.ar.puzzle.solver.composables.sudoku

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import net.tomczek.ar.puzzle.solver.R
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.SudokuBoard
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme
import net.tomczek.ar.puzzle.solver.viewmodel.ArPuzzleSolverViewModel
import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun convertMillisToDateString(millis: Long?): String {
    if (millis == null) {
        return ""
    }
    val date = Date(millis)
    val dateFormat = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.MEDIUM,
        Locale.getDefault()
    )
    return dateFormat.format(date)
}

@Preview
@Composable
fun SudokuBoardPagePreview() {
    val puzzle = PuzzleEntity(
        id = 1,
        type = "sudoku",
        data = """{"board":[8,9,0,7,3,0,4,6,0,0,4,0,2,0,8,3,5,7,2,0,3,0,0,0,8,9,2,4,6,6,3,5,7,2,0,8,0,0,0,9,8,0,0,0,5,5,1,0,4,0,0,0,3,9,6,8,0,0,0,6,0,7,0,0,7,1,8,4,3,0,0,6,0,3,5,1,0,0,0,8,4],"originalValuesMask":[true,true,false,true,true,false,true,true,false,false,true,false,true,false,true,true,true,true,true,false,true,false,false,false,true,true,true,true,true,true,true,true,true,true,false,true,false,false,false,true,true,false,false,false,true,true,true,false,true,false,false,false,true,true,true,true,false,false,false,true,false,true,false,false,true,true,true,true,true,false,false,true,false,true,true,true,false,false,false,true,true]}""",
        scanDate = System.currentTimeMillis()
    )
    ArpuzzlesolverTheme(darkTheme = true) {
        SudokuBoardPage(puzzle, showSolution = false)
    }
}

@Composable
fun SudokuBoardPage(
    puzzle: PuzzleEntity,
    showSolution: Boolean,
    updatePuzzle: (PuzzleEntity) -> Unit = {},
) {
    var dialogData by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val sudokuBoard = SudokuBoard.fromPuzzle(puzzle)

    LaunchedEffect(showSolution) {
        if (showSolution && !sudokuBoard.isSolved()) {
            val solvingSuccess = sudokuBoard.solve()
            if (solvingSuccess) {
                updatePuzzle(sudokuBoard.toPuzzleEntity())
            } else {
                Log.i("MYAPP", "Failed to solve the Sudoku puzzle.")
            }
        }
    }

    Surface(color = Color.Transparent) {
        Column {
            Text(
                convertMillisToDateString(sudokuBoard.scanDate),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            SudokuGrid(sudokuBoard, showSolution = showSolution) {
                Log.i("MYAPP", "Clicked on cell: $it")
                dialogData = Pair(it, sudokuBoard.board[it])
            }
            Text(
                stringResource(R.string.sudoku_tap_to_edit),
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    dialogData?.let {
        SudokuCorrectionDialog(
            initialValue = it.second,
            dismissRequest = {
                dialogData = null
            },
        ) { newValue, isOriginal ->
            Log.i("MYAPP", "New value: $newValue, Original: $isOriginal")
            val updatedBoard = getUpdatedSudokuBoard(
                sudokuBoard = sudokuBoard,
                index = it.first,
                newValue = newValue,
                isOriginal = isOriginal
            )
            val updatedPuzzle = updatedBoard.toPuzzleEntity()
            updatePuzzle(updatedPuzzle)
            dialogData = null
        }
    }
}

fun getUpdatedSudokuBoard(
    sudokuBoard: SudokuBoard,
    index: Int,
    newValue: Int,
    isOriginal: Boolean
): SudokuBoard {
    val mutableBoard = sudokuBoard.board.toMutableList()
    val mutableOriginalValuesMask = sudokuBoard.originalValuesMask.toMutableList()
    mutableBoard[index] = newValue
    mutableOriginalValuesMask[index] = isOriginal
    return SudokuBoard(
        id = sudokuBoard.id,
        board = mutableBoard,
        originalValuesMask = mutableOriginalValuesMask,
        scanDate = sudokuBoard.scanDate
    )
}

@Composable
fun SudokuCorrectionDialog(
    initialValue: Int,
    dismissRequest: () -> Unit,
    valueChange: (Int, Boolean) -> Unit
) {
    Dialog(
        onDismissRequest = dismissRequest
    ) {
        SudokuCellCorrectionModal(
            initialValue = initialValue,
            onValueChange = valueChange
        )
    }
}