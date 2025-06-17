package net.tomczek.ar.puzzle.solver.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.sceneview.ar.node.AnchorNode
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.tomczek.ar.puzzle.solver.ModelCreator
import net.tomczek.ar.puzzle.solver.R
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.persistence.PuzzleDao
import net.tomczek.ar.puzzle.solver.puzzle.solver.PuzzleSolver

@HiltViewModel
class ArPuzzleSolverViewModel @Inject constructor(private val puzzleDao: PuzzleDao): ViewModel() {
    private val _toastEvent = MutableSharedFlow<Pair<Int, Int>>()
    val toastEvent = _toastEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            val savedPuzzles = puzzleDao.getAllPuzzles()
            puzzles = savedPuzzles.toMutableList()
        }
    }

    var puzzles by mutableStateOf(listOf<PuzzleEntity>())
        private set

    var selectedPuzzle by mutableStateOf<PuzzleEntity?>(null)
        private set

    var showPuzzleSolution by mutableStateOf(false)
        private set

    suspend fun savePuzzle(puzzle: PuzzleEntity): Long? {
        val datedPuzzle = PuzzleEntity(
            id = puzzle.id,
            type = puzzle.type,
            data = puzzle.data,
            scanDate = System.currentTimeMillis()
        )
        val puzzleToSave = solvePuzzle(datedPuzzle)
        val savedPuzzle = puzzleDao.insertPuzzle(puzzleToSave)


        if (savedPuzzle != null) {
            val updatedPuzzles = puzzleDao.getAllPuzzles()
            puzzles = updatedPuzzles.toMutableList()
        } else {
            Log.e("ME", "Failed to save puzzle")
        }
        return savedPuzzle
    }

    fun deletePuzzle(puzzle: PuzzleEntity) {
        viewModelScope.launch {
            puzzleDao.deletePuzzle(puzzle)
            val updatedPuzzles = puzzleDao.getAllPuzzles()
            puzzles = updatedPuzzles.toMutableList()
        }
    }

    fun updatePuzzle(puzzle: PuzzleEntity) {
        viewModelScope.launch {
            val puzzleToUpdate = solvePuzzle(puzzle)
            puzzleDao.updatePuzzle(puzzleToUpdate)
            val updatedPuzzles = puzzleDao.getAllPuzzles()
            puzzles = updatedPuzzles.toMutableList()
        }
    }

    fun selectPuzzle(puzzle: Long?) {
        selectedPuzzle = if (puzzle == null) {
            null
        } else {
            puzzles.find { it.id == puzzle }
        }
    }

    fun togglePuzzleSolution(state: Boolean? = null) {
        showPuzzleSolution = state ?: !showPuzzleSolution
    }

    private fun solvePuzzle(puzzle: PuzzleEntity): PuzzleEntity {
        val solvingResult = PuzzleSolver.solve(puzzle)
        val solved = solvingResult.first
        val solvedPuzzle = solvingResult.second

        viewModelScope.launch {
            if (solved) {
                _toastEvent.emit(Pair(R.string.toast_puzzle_solved, Toast.LENGTH_LONG))
            } else {
                _toastEvent.emit(Pair(R.string.toast_puzzle_not_solved, Toast.LENGTH_LONG))
            }
        }

        val puzzleToSave = if (solved) {
            solvedPuzzle
        } else {
            puzzle
        }

        return puzzleToSave
    }

}