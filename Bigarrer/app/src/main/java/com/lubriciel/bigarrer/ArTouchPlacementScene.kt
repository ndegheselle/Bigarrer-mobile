package com.lubriciel.bigarrer

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.lubriciel.bigarrer.ar.features.TouchPlacement
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView

/**
 * AR scene that places cubes at tapped real-world surfaces using the Geospatial API.
 *
 * Wiring:
 *  - [World]          : session config (geospatial + planes enabled), per-frame events,
 *                       latest session/frame for hit testing.
 *  - [Renderer]       : Filament material and node spawning.
 *  - [TouchPlacement] : converts a screen tap into a depth/plane hit, then creates a
 *                       geospatial anchor (or local anchor as fallback) and hands it
 *                       to the renderer.
 */
@Composable
fun ArTouchPlacementScene() {
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
    val renderer = remember(engine, materialLoader) {
        Renderer(
            engine = engine,
            materialLoader = materialLoader,
            childNodes = childNodes,
            cubeColor = Color(0xFFFF5722),
        )
    }
    val touchPlacement = remember(world, renderer) { TouchPlacement(world, renderer) }

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

        // Transparent overlay that captures taps and forwards them to TouchPlacement.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(touchPlacement) {
                    detectTapGestures { offset ->
                        touchPlacement.onTap(offset.x, offset.y)
                    }
                },
        )

        val statusText = when {
            !world.isTracking ->
                "Initializing AR…"
            world.earthTrackingState == TrackingState.TRACKING ->
                "Geospatial ready — tap to place  •  Placed: ${renderer.cubeCount}"
            else ->
                "Locating… tap to place (local anchor)  •  Placed: ${renderer.cubeCount}"
        }
        Text(
            text = statusText,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        )
    }
}
