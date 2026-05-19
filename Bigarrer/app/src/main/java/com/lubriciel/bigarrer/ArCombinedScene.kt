package com.lubriciel.bigarrer

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import com.lubriciel.bigarrer.ar.Renderer
import com.lubriciel.bigarrer.ar.World
import com.lubriciel.bigarrer.ar.features.CubeTrail
import com.lubriciel.bigarrer.ar.features.TouchPlacement
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView

/**
 * AR scene that runs both features simultaneously in a single ARCore session.
 *
 *  - [CubeTrail]      : light-blue cubes dropped automatically every meter of movement.
 *  - [TouchPlacement] : orange cubes placed on tap, anchored via the Geospatial API
 *                       (falls back to a local anchor while Earth is initializing).
 *
 * The two features share one [World] (session config, frame fanout) but each owns a
 * separate [Renderer] so their cubes have distinct colours.
 */
@Composable
fun ArCombinedScene() {
    val engine = rememberEngine()
    val view = rememberView(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()
    val collisionSystem = rememberCollisionSystem(view)

    val world = remember {
        World(
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL,
            enableGeospatial = true,
        )
    }

    // Light-blue trail renderer — one cube per meter of camera movement.
    val trailRenderer = remember(engine, materialLoader) {
        Renderer(
            engine = engine,
            materialLoader = materialLoader,
            childNodes = childNodes,
            cubeColor = Color(0xFF4FC3F7),
        )
    }
    // Orange tap renderer — one cube per screen tap.
    val tapRenderer = remember(engine, materialLoader) {
        Renderer(
            engine = engine,
            materialLoader = materialLoader,
            childNodes = childNodes,
            cubeColor = Color(0xFFFF5722),
        )
    }

    val cubeTrail = remember(world, trailRenderer) {
        CubeTrail(world = world, renderer = trailRenderer)
    }
    DisposableEffect(cubeTrail) {
        onDispose { cubeTrail.dispose() }
    }

    val touchPlacement = remember(world, tapRenderer) { TouchPlacement(world, tapRenderer) }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            view = view,
            childNodes = childNodes,
            cameraNode = cameraNode,
            collisionSystem = collisionSystem,
            sessionConfiguration = world::configureSession,
            onSessionUpdated = world::onSessionUpdated,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(touchPlacement) {
                    detectTapGestures { offset ->
                        touchPlacement.onTap(offset.x, offset.y)
                    }
                },
        )

        val geoLabel = when {
            !world.isTracking -> "Initializing AR…"
            world.earthTrackingState == TrackingState.TRACKING -> "Geospatial ready"
            else -> "Locating…"
        }
        Text(
            text = "$geoLabel  •  Trail: ${trailRenderer.cubeCount}  •  Tap: ${tapRenderer.cubeCount}",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        )
    }
}
