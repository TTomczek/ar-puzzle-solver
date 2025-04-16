package net.tomczek.ar.puzzle.solver

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.addAugmentedImage
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
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
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ArpuzzlesolverTheme {
                ArPuzzleSolver(resources)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArPuzzleSolver(resources: Resources) {
    val bottomSheetState = rememberModalBottomSheetState()
    val pagerState = rememberPagerState(0, 0.5f) { 1 }
    var board: SudokuBoard? by remember { mutableStateOf(null) }

    LaunchedEffect(board) {
        if (board != null) {
            bottomSheetState.show()
        }
    }

    ArCameraView(resources) { foundBoard ->
        board = foundBoard
    }
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = {
        }
    ) {
        HorizontalPager(
            state = pagerState
        ) {
            board?.let { SudokuGrid(it) }
        }
    }
}

@Composable
fun ArCameraView(resources: Resources, foundBoard: (board: SudokuBoard) -> Unit = {}) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)
    val context = LocalContext.current

    var currentlyProcessing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    statusText = stringResource(R.string.hint_searching_puzzle)
    var recognitionFailures by remember { mutableIntStateOf(0) }
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
            config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED)
            config.addAugmentedImage(
                session,
                "sudoku",
                BitmapFactory.decodeResource(resources, R.raw.sudoku)
            )
        },
        onSessionUpdated = { session, frame ->
            frame.getUpdatedAugmentedImages().forEach { augmentedImage ->
                when (augmentedImage.name) {
                    "sudoku" -> {
                        processSudoku(context, session, frame, currentlyProcessing) { result ->
                            val processing = result.first
                            val successful = result.second

                            currentlyProcessing = processing
                            if (processing) return@processSudoku

                            if (successful == true) {
                                Log.i("ProcessSudoku", "Recognition successful")
                                statusText = ""
                                recognitionFailures = 0
                            } else {
                                recognitionFailures++
                                Log.i("ProcessSudoku", "Recognition failed with tries: $recognitionFailures")
                                if (recognitionFailures > 3) {
                                    statusText = context.getString(R.string.hint_different_angle)
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    )
    Text(
        text = statusText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp) // Abstand von oben, um es im oberen Drittel zu platzieren
            .wrapContentHeight(), // Zentriert den Text vertikal innerhalb des Bereichs
        textAlign = TextAlign.Center, // Zentriert den Text horizontal
        color = MaterialTheme.colorScheme.onBackground, // Weiß, wenn das Theme es unterstützt
        style = MaterialTheme.typography.headlineSmall // Passe den Stil an, falls nötig
    )
}

fun processSudoku(
    ctx: Context,
    session: Session,
    frame: Frame,
    currentlyProcessing: Boolean,
    processingCallback: (result: Pair<Boolean, Boolean?>) -> Unit
) {
    if (currentlyProcessing) {
        return
    }
    lateinit var image: Image
    try {
        image = frame.acquireCameraImage()
        session.update()
        processingCallback(Pair(true, null))
        processSudokuImageInCoroutine(ctx, image) { successful ->
            processingCallback(Pair(false, successful))
            Log.i("ProcessSudokuImage", "Processing finished success: $successful")
            image.close()
        }
    } catch (e: Exception) {
        image.close()
        processingCallback(Pair(false, false))
    }
}

fun createTrackingFrame(
    engine: Engine,
    image: AugmentedImage,
    arSceneChildNodes: MutableList<AnchorNode>
) {
    if (image.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
        val anchor: Anchor = image.createAnchor(image.getCenterPose())
        val anchorNode = AnchorNode(engine, anchor)
        arSceneChildNodes.add(anchorNode)
    }
}

fun processSudokuImageInCoroutine(
    context: Context,
    image: Image,
    finishedProcessing: (successful: Boolean) -> Unit
) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    Log.i("ProcessSudokuImageCoroutine", "Processing image...")
                    val recognitionResult = SudokuImageProcessor().processImage(context, image)
                    Log.i("ProcessSudokuImageCoroutine", "$recognitionResult")
                    finishedProcessing(true)
                } catch (e: Exception) {
                    finishedProcessing(false)
                }
            }
        }
}

