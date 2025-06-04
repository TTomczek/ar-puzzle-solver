package net.tomczek.ar.puzzle.solver.puzzle.types

import android.content.Context
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity

interface ArModelStrategy {
    fun canHandle(entity: PuzzleEntity): Boolean
    fun createModel(entity: PuzzleEntity, anchor: Anchor, context: Context, materialLoader: MaterialLoader,engine: Engine): AnchorNode?
}