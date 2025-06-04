package net.tomczek.ar.puzzle.solver

data class CellRegocnitionResult(val cellIndex: Int, val recognizedText: String, val confidence: Float) {
    override fun toString(): String {
        return "(cI=$cellIndex, rT=$recognizedText, con=$confidence)"
    }
}
