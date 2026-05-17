package com.lubriciel.bigarrer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.CubeNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberNodes
import kotlin.math.sqrt
import kotlinx.coroutines.delay

private const val SPAWN_INTERVAL_MS = 500L
private const val CUBE_SIZE = 0.15f
private const val MIN_MOVE_DISTANCE = 0.1f
// Offset cube downward so it sits below eye level
private const val CUBE_Y_OFFSET = -0.3f

@Composable
fun ArCubeTrailScene() {
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()

    var cubeCount by remember { mutableIntStateOf(0) }
    var isTracking by remember { mutableStateOf(false) }
    // Camera position updated each frame from ARCore
    var cameraPos by remember { mutableStateOf<FloatArray?>(null) }
    var lastSpawnPos by remember { mutableStateOf<FloatArray?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(SPAWN_INTERVAL_MS)

            val pos = cameraPos
            if (!isTracking || pos == null) continue

            val last = lastSpawnPos
            if (last != null) {
                val dx = pos[0] - last[0]
                val dy = pos[1] - last[1]
                val dz = pos[2] - last[2]
                if (sqrt(dx * dx + dy * dy + dz * dz) < MIN_MOVE_DISTANCE) continue
            }

            val cube = CubeNode(
                engine = engine,
                size = Float3(CUBE_SIZE, CUBE_SIZE, CUBE_SIZE),
                center = Float3(0f, 0f, 0f),
                materialLoader = materialLoader
            ).apply {
                worldPosition = Position(pos[0], pos[1] + CUBE_Y_OFFSET, pos[2])
            }
            childNodes.add(cube)
            lastSpawnPos = pos.copyOf()
            cubeCount++
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            childNodes = childNodes,
            cameraNode = cameraNode,
            onSessionUpdated = { _, frame ->
                val camera = frame.camera
                isTracking = camera.trackingState == TrackingState.TRACKING
                if (isTracking) {
                    val pose = camera.pose
                    cameraPos = floatArrayOf(pose.tx(), pose.ty(), pose.tz())
                }
            }
        )

        Text(
            text = when {
                !isTracking -> "Move device slowly to initialize AR tracking"
                else -> "Cubes placed: $cubeCount"
            },
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        )
    }
}
