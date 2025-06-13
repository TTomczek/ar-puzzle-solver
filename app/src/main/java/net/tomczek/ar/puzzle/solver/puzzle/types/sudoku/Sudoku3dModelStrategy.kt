package net.tomczek.ar.puzzle.solver.puzzle.types.sudoku

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.createBitmap
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
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme

object Sudoku3dModelStrategy : ArModelStrategy {
    const val MODEL_TYPE = "sudoku"

    override fun canHandle(entity: PuzzleEntity): Boolean {
        return entity.type == MODEL_TYPE
    }

    override fun createModel(entity: PuzzleEntity, augmentedImage: AugmentedImage, anchor: Anchor, viewNodeWindowManager: ViewNode2.WindowManager, materialLoader: MaterialLoader,engine: Engine): AnchorNode? {
        if (entity.type != MODEL_TYPE) {
            return null
        }

        Log.i("MYAPP", "Scaling to : ${augmentedImage.extentX}, ${augmentedImage.extentZ}")

        val sudokuBoard = SudokuBoard.fromPuzzle(entity)
        val viewNode = ViewNode2(engine, viewNodeWindowManager, materialLoader) {
            ArpuzzlesolverTheme {
                SudokuGrid(sudokuBoard)
            }
        }.apply {
            // Scaling: X = extentX, Y = 0.01f (Dicke), Z = extentZ
            scale = Scale(augmentedImage.extentX / 4, augmentedImage.extentZ / 4, 1f)

            rotation = Rotation(-90f, 0f, 0f)
        }

        val anchorNode = AnchorNode(engine, anchor).apply {
            name = "sudoku"
        }
        anchorNode.addChildNode(viewNode)

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