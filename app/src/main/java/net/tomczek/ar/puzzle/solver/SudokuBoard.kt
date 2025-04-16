package net.tomczek.ar.puzzle.solver

data class SudokuBoard(val board: List<Int>, val originalValuesMask: List<Boolean>) {

    fun isOriginalValue(cellIndex: Int): Boolean {
        return originalValuesMask[cellIndex]
    }

    fun isSolved(): Boolean {
        return board.all { it != 0 }
    }

    fun solve(): List<Int>? {
        if (board.size != 81) {
            throw IllegalArgumentException("Invalid board size. Expected 81 cells.")
        }

        if (isSolved()) {
            return board
        }
        val solvedBoard = solveBoard(board)
        return solvedBoard
    }

    private fun solveBoard(currentBoard: List<Int>): List<Int>? {
        for (i in currentBoard.indices) {
            if (currentBoard[i] == 0) { // Leere Zelle gefunden
                val row = i / 9
                val col = i % 9

                for (num in 1..9) {
                    if (isValid(currentBoard, row, col, num)) {
                        val newBoard = currentBoard.toMutableList() // Erstelle eine veränderliche Kopie
                        newBoard[i] = num // Setze die Zahl
                        val result = solveBoard(newBoard) // Rekursiver Aufruf mit neuer Liste
                        if (result != null) return result // Lösung gefunden
                    }
                }
                return null // Keine gültige Zahl gefunden
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
            for (i in 0 until 9) {
                append(board.subList(i * 9, (i + 1) * 9).joinToString(" "))
                append("\n")
            }
        }
    }
}

