package net.tomczek.ar.puzzle.solver.puzzle.types

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity

abstract class BasePuzzle(
    val id: Long? = null,
    val type: String = "",
    internal val data: String = "",
    val scanDate: Long? = null
) {
    override fun toString(): String {
        return "BasePuzzle(id=$id, type='$type', data='$data', scanDate=$scanDate)"
    }

    companion object {
        @JvmStatic
        protected val gson: Gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }

    abstract fun toPuzzleEntity(): PuzzleEntity
}