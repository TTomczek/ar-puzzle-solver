package net.tomczek.ar.puzzle.solver

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndexBuffer
import com.google.android.filament.MaterialInstance
import com.google.android.filament.RenderableManager
import com.google.android.filament.VertexBuffer
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import dagger.hilt.android.AndroidEntryPoint
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.addAugmentedImage
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.math.Box
import io.github.sceneview.math.Position
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.tomczek.ar.puzzle.solver.composables.sudoku.SudokuBoardPage
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.SudokuBoard
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme
import net.tomczek.ar.puzzle.solver.viewmodel.ArCameraViewModel
import net.tomczek.ar.puzzle.solver.viewmodel.ArPuzzleSolverViewModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.core.graphics.createBitmap
import com.google.ar.core.Anchor
import io.github.sceneview.SceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import net.tomczek.ar.puzzle.solver.composables.sudoku.SudokuGrid
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.Sudoku3dModelStrategy

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val arPuzzleSolverViewModel: ArPuzzleSolverViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImageHelper.clearSavedImagesByPrefix("aps")

        setContent {
            ArpuzzlesolverTheme {
                ArPuzzleSolver(resources, arPuzzleSolverViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArPuzzleSolver(resources: Resources, arPuzzleSolverViewModel: ArPuzzleSolverViewModel) {
    val arCameraViewModel: ArCameraViewModel = remember { ArCameraViewModel() }
    val coroutineScope = rememberCoroutineScope()
    val navigationDrawerState = rememberDrawerState(DrawerValue.Closed)
    val pagerState = rememberPagerState(0, 0.0f) { arPuzzleSolverViewModel.puzzles.size }
    var showSolution by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = navigationDrawerState,
        drawerContent = {
            ModalDrawerSheet {
                VerticalPager(
                    state = pagerState,
                    key = { pageIndex -> arPuzzleSolverViewModel.puzzles[pageIndex].id!! },
                    pageSpacing = 50.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .padding(16.dp)
                ) { pageIndex ->
                    val puzzle = arPuzzleSolverViewModel.puzzles[pageIndex]

                    when (puzzle.type) {
                        "sudoku" -> {
                            SudokuBoardPage(puzzle, showSolution) { updatedPuzzle ->
                                arPuzzleSolverViewModel.updatePuzzle(updatedPuzzle)
                            }
                        }

                        else -> {
                            Text(text = "Unsupported puzzle type: ${puzzle.type}")
                        }
                    }
                    Button(onClick = {
                        arPuzzleSolverViewModel.deletePuzzle(puzzle)
                        coroutineScope.launch {
                            navigationDrawerState.close()
                            arCameraViewModel.updateCameraPaused(false)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                        )
                    }
                }
                Button(
                    onClick = {
                        showSolution = !showSolution
                        val currentPage = pagerState.currentPage
                        val currentPuzzle = arPuzzleSolverViewModel.puzzles[currentPage]

                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.End)
                ) {
                    Text(
                        text = if (showSolution) stringResource(R.string.btn_hide_solution) else stringResource(
                            R.string.btn_show_solution
                        )
                    )
                }
            }
        }) {
        ArCameraView(
            resources,
            arCameraViewModel
        ) { puzzle ->
            arPuzzleSolverViewModel.savePuzzle(puzzle)
            coroutineScope.launch {
                arCameraViewModel.updateCameraPaused(true)
                navigationDrawerState.open()
            }
        }
        Button(onClick = {
            coroutineScope.launch {
                arCameraViewModel.updateCameraPaused(true)
                navigationDrawerState.open()
            }
        }) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu"
            )
        }
    }
}

@Composable
fun ArCameraView(
    resources: Resources,
    viewModel: ArCameraViewModel,
    foundPuzzle: (PuzzleEntity) -> Unit
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    var childNodes = rememberNodes()
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)
    val context = LocalContext.current
    var arSceneSession: Session? = remember { null }

    LaunchedEffect(viewModel.cameraPaused) {
        if (viewModel.cameraPaused) {
            arSceneSession?.pause()
        } else {
            arSceneSession?.resume()
        }
    }

    ARScene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        planeRenderer = false,
        materialLoader = materialLoader,
        modelLoader = modelLoader,
        view = view,
        cameraNode = cameraNode,
        collisionSystem = collisionSystem,
        childNodes = childNodes,
        sessionConfiguration = { session, config ->
            arSceneSession = session
            config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED)
            config.addAugmentedImage(
                session,
                "sudoku",
                BitmapFactory.decodeResource(resources, R.raw.sudoku)
            )
        },
        onSessionUpdated = { session, frame ->
            frame.getUpdatedAugmentedImages().forEach { augmentedImage ->
                if (augmentedImage.trackingState != TrackingState.TRACKING) {
                    return@forEach
                }

                analyzeImage(context, session, frame, viewModel, augmentedImage.name) {
                    foundPuzzle(it)
                    augmentedImage.createAnchorOrNull(augmentedImage.centerPose)?.let { anchor ->
                        Log.i("MYAPP", "Found puzzle: ${it}, creating 3D model")
                        create3dModelByType(it, anchor, context, materialLoader, engine)?.let {
                            Log.i("MYAPP", "Created 3D model for puzzle: ${it.name}")
                            childNodes += it
                        }
                    }

                }
            }
        },
        onSessionPaused = {
            Log.i("MYAPP", "Session paused")
        },
        onSessionResumed = {
            Log.i("MYAPP", "Session resumed")
        }
    )
    Text(
        text = stringResource(viewModel.statusText),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp)
            .wrapContentHeight(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineSmall
    )
}

fun analyzeImage(
    context: Context,
    session: Session,
    frame: Frame,
    viewModel: ArCameraViewModel,
    augmentedImageName: String,
    foundPuzzle: (PuzzleEntity) -> Unit
) {
    if (viewModel.currentlyProcessing) {
        return
    }
    viewModel.setProcessingState(true)
    viewModel.updateStatusText(R.string.hint_processing)

    when (augmentedImageName) {
        "sudoku" -> {
            processSudoku(context, session, frame) { sudokuBoard ->

                if (sudokuBoard != null) {
                    viewModel.updateStatusText(R.string.hint_searching_puzzle)
                    viewModel.resetRecognitionFailures()
                    viewModel.setProcessingState(false)
                    try {
                        sudokuBoard.solve()
                    } catch (e: Exception) {
                        Log.i("MYAPP", "Error solving sudoku: ${e.message}")
                    }
                    val puzzleEntity = sudokuBoard.toPuzzleEntity()
                    foundPuzzle(puzzleEntity)
                } else {
                    viewModel.incrementRecognitionFailures()
                    if (viewModel.recognitionFailures > 2) {
                        viewModel.updateStatusText(R.string.hint_different_angle)
                    } else {
                        viewModel.updateStatusText(R.string.hint_searching_puzzle)
                    }
                    viewModel.setProcessingState(false)

                }
            }
        }

        else -> {
            viewModel.setProcessingState(false)
        }
    }
}

fun create3dModelByType(puzzleEntity: PuzzleEntity, anchor: Anchor, context: Context, materialLoader: MaterialLoader, engine: Engine): Node? {
    return ModelCreator.getModel(puzzleEntity, anchor, context, materialLoader, engine)
}

fun processSudoku(
    ctx: Context,
    session: Session,
    frame: Frame,
    processingCallback: (result: SudokuBoard?) -> Unit
) {
    lateinit var image: Image
    try {
        image = frame.acquireCameraImage()
        session.update()
        processSudokuImageInCoroutine(ctx, image) { sudokuBoard ->
            processingCallback(sudokuBoard)
            image.close()
        }
    } catch (e: Exception) {
        image.close()
        processingCallback(null)
    }
}

fun processSudokuImageInCoroutine(
    context: Context,
    image: Image,
    finishedProcessing: (result: SudokuBoard?) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        withContext(Dispatchers.IO) {
            try {
                Log.i("MYAPP", "Processing image...")
                val recognitionResult = SudokuImageProcessor().processImage(context, image)
                Log.i("MYAPP", "$recognitionResult")
                finishedProcessing(recognitionResult)
            } catch (e: Exception) {
                Log.i("MYAPP", "Error processing image: ${e.message}")
                finishedProcessing(null)
            }
        }
    }
}
