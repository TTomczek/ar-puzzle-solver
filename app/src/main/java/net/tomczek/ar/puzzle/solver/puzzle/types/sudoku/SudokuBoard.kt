package net.tomczek.ar.puzzle.solver.puzzle.types.sudoku

import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import jakarta.inject.Inject
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.BasePuzzle

class SudokuBoard @Inject constructor(
    @Expose
    var board: List<Int> = emptyList(),
    @Expose
    val originalValuesMask: List<Boolean> = emptyList(),
    id: Long? = null,
    scanDate: Long? = null
) : BasePuzzle(id = id, type = "sudoku", scanDate = scanDate) {

    override fun toPuzzleEntity(): PuzzleEntity {
        val data = gson.toJson(this)
        return PuzzleEntity(
            id = id,
            type = type,
            data = data,
            scanDate = scanDate
        )
    }

    fun isSolved(): Boolean {
        return board.all { it != 0 }
    }

    fun solve(): Boolean {
        if (board.size != 81) {
            return false
        }

        if (isSolved()) {
            return true
        }

        val solvedBoard = solveBoard(board)

        if (solvedBoard == null) {
            return false
        }

        board = solvedBoard
        return true
    }

    private fun solveBoard(currentBoard: List<Int>): List<Int>? {
        for (i in currentBoard.indices) {
            if (currentBoard[i] == 0) {
                val row = i / 9
                val col = i % 9

                for (num in 1..9) {
                    if (isValid(currentBoard, row, col, num)) {
                        val newBoard = currentBoard.toMutableList()
                        newBoard[i] = num
                        val result = solveBoard(newBoard)
                        if (result != null) return result
                    }
                }
                return null
            }
        }
        return currentBoard // Alles gelöst
    }

    private fun isValid(currentBoard: List<Int>, row: Int, col: Int, num: Int): Boolean {
        for (i in 0 until 9) {
            // Überprüfung der Zeile und Spalte
            if (currentBoard[row * 9 + i] == num || currentBoard[i * 9 + col] == num) return false

            // Überprüfung des 3x3-Blocks
            val blockRow = (row / 3) * 3 + i / 3
            val blockCol = (col / 3) * 3 + i % 3
            if (currentBoard[blockRow * 9 + blockCol] == num) return false
        }
        return true
    }

    override fun toString(): String {
        return buildString {
            append("Sudoku Board:\n")
            append("ID: $id\n")
            append("Scan Date: $scanDate\n")
            append("Type: $type\n")
            append("Board:\n")
            for (i in 0 until 9) {
                append(board.subList(i * 9, (i + 1) * 9).joinToString("\t"))
                append("\n")
            }
            append("Original Values Mask:\n")
            for (i in 0 until 9) {
                append(originalValuesMask.subList(i * 9, (i + 1) * 9).joinToString("\t"))
                append("\n")
            }
        }
    }

    fun getValuesForIndex(index: Int): Pair<Int, Boolean> {
        if (index < 0 || index >= board.size) {
            throw IndexOutOfBoundsException("Index $index out of bounds for board size ${board.size}")
        }
        return Pair(board[index], originalValuesMask[index])
    }

    companion object {

        private class SudokuBoardData(
            @Expose
            val board: List<Int>,
            @Expose
            val originalValuesMask: List<Boolean>
        )


        fun fromPuzzle(puzzle: PuzzleEntity): SudokuBoard {
            val id = puzzle.id
            val scanDate = puzzle.scanDate
            val sudokuBoardData =
                gson.fromJson<SudokuBoardData>(
                    puzzle.data,
                    object : TypeToken<SudokuBoardData>() {}.type
                )

            return SudokuBoard(
                id = id,
                scanDate = scanDate,
                board = sudokuBoardData.board,
                originalValuesMask = sudokuBoardData.originalValuesMask
            )
        }
    }
}