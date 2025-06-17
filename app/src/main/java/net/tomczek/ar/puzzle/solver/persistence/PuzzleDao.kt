package net.tomczek.ar.puzzle.solver.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PuzzleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: PuzzleEntity): Long?

    @Query("SELECT * FROM puzzles WHERE id = :puzzleId")
    suspend fun getPuzzleById(puzzleId: Int): PuzzleEntity?

    @Query("SELECT * FROM puzzles ORDER BY scanDate DESC")
    suspend fun getAllPuzzles(): List<PuzzleEntity>

    @Delete
    suspend fun deletePuzzle(puzzle: PuzzleEntity): Int

    @Query("SELECT * FROM puzzles WHERE type = :type ORDER BY scanDate DESC")
    suspend fun getPuzzlesByType(type: String): List<PuzzleEntity>

    @Update
    suspend fun updatePuzzle(puzzle: PuzzleEntity): Int

    @Query("DELETE FROM puzzles")
    suspend fun deleteAll(): Int
}