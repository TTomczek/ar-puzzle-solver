package net.tomczek.ar.puzzle.solver.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.sceneview.ar.node.AnchorNode
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import net.tomczek.ar.puzzle.solver.ModelCreator
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.persistence.PuzzleDao

@HiltViewModel
class ArPuzzleSolverViewModel @Inject constructor(private val puzzleDao: PuzzleDao): ViewModel() {

    init {
        Log.i("MYAPP", "ArPuzzleSolverViewModel initialized")
        viewModelScope.launch {
            val savedPuzzles = puzzleDao.getAllPuzzles()
            puzzles = savedPuzzles.toMutableList()
        }
    }

    var puzzles by mutableStateOf(listOf<PuzzleEntity>())
        private set

    fun savePuzzle(puzzle: PuzzleEntity) {
        val datedPuzzle = PuzzleEntity(
            id = puzzle.id,
            type = puzzle.type,
            data = puzzle.data,
            scanDate = System.currentTimeMillis()
        )
        Log.i("MYAPP", "Saving puzzle: $datedPuzzle")
        viewModelScope.launch {
            val savedPuzzle = puzzleDao.insertPuzzle(datedPuzzle)
            if (savedPuzzle != null) {
                val updatedPuzzles = puzzleDao.getAllPuzzles()
                puzzles = updatedPuzzles.toMutableList()
            } else {
                Log.e("ME", "Failed to save puzzle")
            }
        }
    }

    fun deletePuzzle(puzzle: PuzzleEntity) {
        Log.i("MYAPP", "Deleting puzzle: $puzzle")
        viewModelScope.launch {
            puzzleDao.deletePuzzle(puzzle)
            val updatedPuzzles = puzzleDao.getAllPuzzles()
            puzzles = updatedPuzzles.toMutableList()
        }
    }

    fun updatePuzzle(puzzle: PuzzleEntity) {
        Log.i("MYAPP", "Updating puzzle: $puzzle")
        viewModelScope.launch {
            puzzleDao.updatePuzzle(puzzle)
            val updatedPuzzles = puzzleDao.getAllPuzzles()
            puzzles = updatedPuzzles.toMutableList()
        }
    }

}