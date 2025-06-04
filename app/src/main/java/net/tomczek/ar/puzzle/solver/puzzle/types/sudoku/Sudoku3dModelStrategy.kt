package net.tomczek.ar.puzzle.solver.puzzle.types.sudoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.createBitmap
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.node.ImageNode
import net.tomczek.ar.puzzle.solver.ImageHelper
import net.tomczek.ar.puzzle.solver.composables.sudoku.SudokuGrid
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.ArModelStrategy

object Sudoku3dModelStrategy : ArModelStrategy {
    const val MODEL_TYPE = "sudoku"

    override fun canHandle(entity: PuzzleEntity): Boolean {
        return entity.type == MODEL_TYPE
    }

    override fun createModel(entity: PuzzleEntity, anchor: Anchor, context: Context, materialLoader: MaterialLoader,engine: Engine): AnchorNode? {
        if (entity.type != MODEL_TYPE) {
            return null
        }

        val sudokuBoard = SudokuBoard.fromPuzzle(entity)
        val sudokuGridBitmap = renderComposableToBitmap(context, 800, 800) {
            SudokuGrid(sudokuBoard)
        }
        ImageHelper.saveBitmapToFile(context, sudokuGridBitmap, "sudoku_grid.png")

        val imageNode = ImageNode(
            materialLoader = materialLoader,
            bitmap = sudokuGridBitmap
        )
        val anchorNode = AnchorNode(engine, anchor)
        anchorNode.addChildNode(imageNode)

        return anchorNode
    }

    fun renderComposableToBitmap(
        context: Context,
        width: Int,
        height: Int,
        content: @Composable () -> Unit
    ): Bitmap {
        // Erstelle einen ComposeView, der dein Composable enthält.
        val composeView = ComposeView(context).apply {
            setContent { content() }
            // Messt die Composable-Größe exakt
            measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            // Legt das Layout fest.
            layout(0, 0, width, height)
        }
        // Erzeuge ein Bitmap mit der gewünschten Größe und Zeichne den Inhalt
        return createBitmap(width, height).also { bitmap ->
            val canvas = Canvas(bitmap)
            composeView.draw(canvas)
        }
    }
}