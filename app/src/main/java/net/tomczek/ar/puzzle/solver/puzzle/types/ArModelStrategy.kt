package net.tomczek.ar.puzzle.solver.puzzle.types

import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.node.ViewNode2
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity

interface ArModelStrategy {
    fun canHandle(entity: PuzzleEntity): Boolean
    fun createModel(entity: PuzzleEntity, augmentedImage: AugmentedImage, anchor: Anchor, viewNodeWindowManager: ViewNode2.WindowManager, materialLoader: MaterialLoader,engine: Engine, showSolution: Boolean, onClick: () -> Unit = {}): AnchorNode?
}