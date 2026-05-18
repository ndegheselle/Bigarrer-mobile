package com.lubriciel.bigarrer

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lubriciel.bigarrer.ar.Renderer
import com.lubriciel.bigarrer.ar.World
import com.lubriciel.bigarrer.ar.features.CubeTrail
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView

/**
 * AR scene composable. Pure wiring across three layers:
 *  - [World]    : AR session config + per-frame events
 *  - [Renderer] : Filament materials and object spawning
 *  - [CubeTrail]: feature that decides when to ask the renderer to spawn a cube
 */
@Composable
fun ArCubeScene() {
    val engine = rememberEngine()
    val view = rememberView(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()
    val collisionSystem = rememberCollisionSystem(view)

    val world = remember { World() }
    val renderer = remember(engine, materialLoader) {
        Renderer(
            engine = engine,
            materialLoader = materialLoader,
            childNodes = childNodes,
        )
    }
    val cubeTrail = remember(world, renderer) {
        CubeTrail(world = world, renderer = renderer)
    }
    DisposableEffect(cubeTrail) {
        onDispose { cubeTrail.dispose() }
    }

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

        Text(
            text = if (world.isTracking)
                "Walk around — a cube every meter  •  Placed: ${renderer.cubeCount}"
            else
                "Initializing AR…",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        )
    }
}
