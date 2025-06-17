package net.tomczek.ar.puzzle.solver.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(Converters::class)
@Database(version = 1, entities = [PuzzleEntity::class], exportSchema = false)
abstract class PuzzleDatabase : RoomDatabase() {
    abstract fun getPuzzleDao(): PuzzleDao

}