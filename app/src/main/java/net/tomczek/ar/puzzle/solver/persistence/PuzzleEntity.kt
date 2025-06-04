package net.tomczek.ar.puzzle.solver.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzles")
open class PuzzleEntity(

    @PrimaryKey(autoGenerate = true)
    open val id: Long? = null,
    open val type: String = "",
    internal val data: String = "",
    val scanDate: Long? = null,
) {
    override fun toString(): String {
        return "PuzzleEntity(id=$id, type='$type', data='$data', scanDate=$scanDate)"
    }
}
