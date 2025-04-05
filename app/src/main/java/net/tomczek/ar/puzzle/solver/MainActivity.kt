package net.tomczek.ar.puzzle.solver

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Config
import com.google.ar.core.Trackable
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.addAugmentedImage
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedAugmentedImages
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import net.tomczek.ar.puzzle.solver.ui.theme.ArpuzzlesolverTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ArpuzzlesolverTheme {
                ArCameraView(resources)
            }
        }
    }
}

@Composable
fun ArCameraView(resources: Resources) {
    var foundImage by remember { mutableStateOf("") }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)
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

                if (augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
                    augmentedImage.createAnchorOrNull(augmentedImage.centerPose)?.let { anchor ->
                        if (childNodes.isEmpty()) {
                            childNodes += AnchorNode(engine, anchor).apply {
                                addChildNode(
                                    ModelNode(
                                        modelInstance = modelLoader.createModelInstance(R.raw.damaged_helmet),
                                        scaleToUnits = 0.1f,
                                        centerOrigin = Position(0.0f)
                                    ).apply {
                                        isEditable = true
                                    }
                                )
                            }
                        }
                        foundImage = augmentedImage.name
                    }
                }
            }
        }
    )
    Row {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp), text = "Found image: $foundImage"
        )
    }
}