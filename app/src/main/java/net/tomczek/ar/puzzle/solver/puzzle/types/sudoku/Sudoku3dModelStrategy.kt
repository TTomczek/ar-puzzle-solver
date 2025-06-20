package net.tomczek.ar.puzzle.solver.puzzle.types.sudoku

import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ViewNode2
import net.tomczek.ar.puzzle.solver.composables.sudoku.SudokuGrid
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.ArModelStrategy
import net.tomczek.ar.puzzle.solver.puzzle.types.SupportedPuzzleTypes
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme

object Sudoku3dModelStrategy : ArModelStrategy {
    val MODEL_TYPE = SupportedPuzzleTypes.SUDOKU

    override fun canHandle(entity: PuzzleEntity): Boolean {
        return entity.type == MODEL_TYPE
    }

    override fun createModel(
        entity: PuzzleEntity,
        augmentedImage: AugmentedImage,
        anchor: Anchor,
        viewNodeWindowManager: ViewNode2.WindowManager,
        materialLoader: MaterialLoader,
        engine: Engine,
        showSolution: Boolean,
        onClick: () -> Unit
    ): AnchorNode? {
        if (entity.type != MODEL_TYPE) {
            return null
        }

        val sudokuBoard = SudokuBoard.fromPuzzle(entity)

        val viewNode = ViewNode2(engine, viewNodeWindowManager, materialLoader) {
            ArpuzzlesolverTheme {
                SudokuGrid(sudokuBoard, showSolution)
            }
        }.apply {
            scale = Scale(augmentedImage.extentX / 4, augmentedImage.extentZ / 4, 1f)
            onSingleTapConfirmed = {
                onClick()
                true
            }
            rotation = Rotation(-90f, 0f, 0f)
        }

        val anchorNode = AnchorNode(engine, anchor).apply {
            name = "sudoku"
        }
        anchorNode.addChildNode(viewNode)

        return anchorNode
    }
}