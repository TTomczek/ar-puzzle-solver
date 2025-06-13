package net.tomczek.ar.puzzle.solver

import android.content.Context
import android.util.Log
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.node.ViewNode2
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.ArModelStrategy
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.Sudoku3dModelStrategy

object ModelCreator {

    private val modelStrategies: List<ArModelStrategy> = listOf(
        Sudoku3dModelStrategy
    )

    fun getModel(entity: PuzzleEntity, augmentedImage: AugmentedImage, anchor: Anchor, viewNodeWindowManager: ViewNode2.WindowManager, materialLoader: MaterialLoader, engine: Engine, showSolution: Boolean): AnchorNode? {
        val strategy = modelStrategies.firstOrNull { it.canHandle(entity) }

        return strategy?.createModel(entity, augmentedImage, anchor, viewNodeWindowManager, materialLoader, engine, showSolution)
    }
}