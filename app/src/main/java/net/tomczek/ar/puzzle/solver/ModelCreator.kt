package net.tomczek.ar.puzzle.solver

import android.content.Context
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.ArModelStrategy
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.Sudoku3dModelStrategy

object ModelCreator {

    private val modelStrategies: List<ArModelStrategy> = listOf(
        Sudoku3dModelStrategy
    )

    fun getModel(entity: PuzzleEntity, anchor: Anchor, context: Context, materialLoader: MaterialLoader, engine: Engine): AnchorNode? {
        val strategy = modelStrategies.firstOrNull { it.canHandle(entity) }

        return strategy?.createModel(entity, anchor, context, materialLoader, engine)
    }
}