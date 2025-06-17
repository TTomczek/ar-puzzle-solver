package net.tomczek.ar.puzzle.solver.hilt.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.tomczek.ar.puzzle.solver.persistence.PuzzleDao
import net.tomczek.ar.puzzle.solver.persistence.PuzzleDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PuzzleDatabase {
        return Room.databaseBuilder(
            context,
            PuzzleDatabase::class.java,
            "puzzle_database.db"
        ).build()
    }

    @Singleton
    @Provides
    fun providePuzzleDao(database: PuzzleDatabase): PuzzleDao = database.getPuzzleDao()
}