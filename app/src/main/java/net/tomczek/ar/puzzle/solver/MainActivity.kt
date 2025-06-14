package net.tomczek.ar.puzzle.solver

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import dagger.hilt.android.AndroidEntryPoint
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.addAugmentedImage
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.node.Node
import io.github.sceneview.node.ViewNode2
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import io.github.sceneview.rememberViewNodeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.tomczek.ar.puzzle.solver.composables.sudoku.SudokuBoardPage
import net.tomczek.ar.puzzle.solver.persistence.PuzzleEntity
import net.tomczek.ar.puzzle.solver.puzzle.types.SupportedPuzzleTypes
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.SudokuBoard
import net.tomczek.ar.puzzle.solver.puzzle.types.sudoku.SudokuImageProcessor
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme
import net.tomczek.ar.puzzle.solver.viewmodel.ArCameraViewModel
import net.tomczek.ar.puzzle.solver.viewmodel.ArPuzzleSolverViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val arPuzzleSolverViewModel: ArPuzzleSolverViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImageHelper.clearSavedImagesByPrefix("aps")

        setContent {
            var permissionState by remember { mutableStateOf(checkCameraPermission()) }

            // Re-evaluiere die Berechtigung, wenn die Activity resumed wird
            DisposableEffect(Unit) {
                val lifecycleOwner = this@MainActivity
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        permissionState = checkCameraPermission()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            ArpuzzlesolverTheme {
                if (!permissionState) {
                    NoPermissionScreen(textId = R.string.no_camera_permission)
                } else {
                    ArPuzzleSolver(resources, arPuzzleSolverViewModel)
                }
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ArPuzzleSolver(resources: Resources, arPuzzleSolverViewModel: ArPuzzleSolverViewModel) {
    val arCameraViewModel: ArCameraViewModel = remember { ArCameraViewModel() }
    val coroutineScope = rememberCoroutineScope()
    val navigationDrawerState = rememberDrawerState(DrawerValue.Closed)
    val pagerState = rememberPagerState(0, 0.0f) { arPuzzleSolverViewModel.puzzles.size }
    val context = LocalContext.current

    LaunchedEffect(arPuzzleSolverViewModel.toastEvent) {
        arPuzzleSolverViewModel.toastEvent.collect { (message, duration) ->
            Toast.makeText(context, message, duration).show()
        }
    }

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

                    Column {
                        Box(
                            modifier = Modifier
                                .then(
                                    if (puzzle.id == arPuzzleSolverViewModel.selectedPuzzle?.id) Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    ) else Modifier
                                )
                        ) {
                            when (puzzle.type) {
                                SupportedPuzzleTypes.SUDOKU -> {
                                    SudokuBoardPage(puzzle, arPuzzleSolverViewModel.showPuzzleSolution) { updatedPuzzle ->
                                        arPuzzleSolverViewModel.updatePuzzle(updatedPuzzle)
                                    }
                                }

                                else -> {
                                    Text(text = "Unsupported puzzle type: ${puzzle.type}", modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = {
                                if (puzzle.id != arPuzzleSolverViewModel.selectedPuzzle?.id) {
                                    arPuzzleSolverViewModel.selectPuzzle(puzzle.id)
                                    arCameraViewModel.updateStatusText(R.string.hint_projecting_puzzle)
                                } else {
                                    arPuzzleSolverViewModel.selectPuzzle(null)
                                    arCameraViewModel.updateStatusText(R.string.hint_searching_puzzle)
                                }
                            }) {
                                val textDecoration: TextDecoration =
                                    if (puzzle.id != arPuzzleSolverViewModel.selectedPuzzle?.id) TextDecoration.None else TextDecoration.LineThrough
                                Text("AR", textDecoration = textDecoration)
                            }
                            Spacer(Modifier.width(15.dp))
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
                        Text(
                            text = stringResource(
                                R.string.page,
                                pageIndex + 1,
                                arPuzzleSolverViewModel.puzzles.size
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Button(
                    onClick = {
                        arPuzzleSolverViewModel.togglePuzzleSolution()
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.End)
                ) {
                    Text(
                        text = if (arPuzzleSolverViewModel.showPuzzleSolution) stringResource(R.string.btn_hide_solution) else stringResource(
                            R.string.btn_show_solution
                        )
                    )
                }
            }
        }) {
        ArCameraView(
            resources,
            arCameraViewModel,
            arPuzzleSolverViewModel,
            arPuzzleSolverViewModel.selectedPuzzle
        ) { puzzle ->
            coroutineScope.launch {
                val savedPuzzleId = arPuzzleSolverViewModel.savePuzzle(puzzle)
                arPuzzleSolverViewModel.selectPuzzle(savedPuzzleId)
                arCameraViewModel.updateStatusText(R.string.hint_projecting_puzzle)
                arCameraViewModel.updateCameraPaused(true)
                pagerState.scrollToPage(0)
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
    arPuzzleSolverViewModel: ArPuzzleSolverViewModel,
    selectedPuzzle: PuzzleEntity? = null,
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
    val viewNodeWindowManager = rememberViewNodeManager(context, creator = {
        SceneView.createViewNodeManager(context)
    })

    LaunchedEffect(viewModel.cameraPaused) {
        if (viewModel.cameraPaused) {
            Log.i("MYAPP", "Pausing AR session")
            arSceneSession?.pause()
        } else {
            Log.i("MYAPP", "Resuming AR session")
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
        viewNodeWindowManager = viewNodeWindowManager,
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
                if (selectedPuzzle != null && augmentedImage.trackingState == TrackingState.TRACKING && childNodes.find { it.name == augmentedImage.name } == null) {
                    augmentedImage.createAnchorOrNull(augmentedImage.centerPose)?.let { anchor ->
                        create3dModelByType(
                            selectedPuzzle,
                            augmentedImage,
                            anchor,
                            viewNodeWindowManager,
                            materialLoader,
                            engine,
                            arPuzzleSolverViewModel.showPuzzleSolution
                        )?.let {
                            childNodes += it
                        }
                    }
                }

                if (selectedPuzzle == null) {
                    analyzeImage(context, session, frame, viewModel, augmentedImage.name) {
                        foundPuzzle(it)
                    }
                }
            }
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

fun create3dModelByType(
    puzzleEntity: PuzzleEntity,
    augmentedImage: AugmentedImage,
    anchor: Anchor,
    viewNodeWindowManager: ViewNode2.WindowManager,
    materialLoader: MaterialLoader,
    engine: Engine,
    showSolution: Boolean = false
): Node? {
    return ModelCreator.getModel(puzzleEntity, augmentedImage, anchor, viewNodeWindowManager, materialLoader, engine, showSolution)
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

@Composable
fun NoPermissionScreen(@StringRes textId: Int) {
    val context = LocalContext.current
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(textId),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }) {
                Text(stringResource(id = R.string.open_settings))
            }
        }
    }
}
