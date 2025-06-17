package net.tomczek.ar.puzzle.solver.puzzle.types.sudoku

data class CellRecognitionResult(val cellIndex: Int, val recognizedText: String, val confidence: Float) {
    override fun toString(): String {
        return "(cI=$cellIndex, rT=$recognizedText, con=$confidence)"
    }
}
