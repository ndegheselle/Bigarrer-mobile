package com.lubriciel.bigarrer

import android.view.MotionEvent
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
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.lubriciel.bigarrer.service.CubePlacementService
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.node.CubeNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberNodes
import kotlinx.coroutines.launch

private const val CUBE_SIZE = 0.2f

@Composable
fun ArCubeScene(cubePlacementService: CubePlacementService) {
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()
    val coroutineScope = rememberCoroutineScope()

    var arTracking by remember { mutableStateOf(false) }
    var earthTracking by remember { mutableStateOf(false) }
    var cubeCount by remember { mutableIntStateOf(0) }
    var savedLocationsLoaded by remember { mutableStateOf(false) }
    var sessionRef by remember { mutableStateOf<Session?>(null) }

    fun spawnCubeOnAnchor(anchor: com.google.ar.core.Anchor) {
        val anchorNode = AnchorNode(engine, anchor).apply {
            addChildNode(
                CubeNode(
                    engine = engine,
                    size = Float3(CUBE_SIZE),
                    center = Float3(0f),
                    materialLoader = materialLoader,
                )
            )
        }
        childNodes.add(anchorNode)
        cubeCount++
    }

    // Restore previously saved cubes once both session and Earth tracking are ready
    LaunchedEffect(earthTracking, sessionRef) {
        if (!earthTracking || savedLocationsLoaded) return@LaunchedEffect
        val session = sessionRef ?: return@LaunchedEffect
        val earth = session.earth ?: return@LaunchedEffect

        savedLocationsLoaded = true
        val saved = cubePlacementService.loadAll()
        for (location in saved) {
            try {
                val quat = floatArrayOf(
                    location.quatX, location.quatY,
                    location.quatZ, location.quatW
                )
                val anchor = earth.createAnchor(
                    location.latitude, location.longitude, location.altitude, quat
                )
                spawnCubeOnAnchor(anchor)
            } catch (_: Exception) {
                // Anchor may fail if geospatial data is unavailable at that point
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            childNodes = childNodes,
            cameraNode = cameraNode,
            sessionConfiguration = { session, config ->
                config.geospatialMode = Config.GeospatialMode.ENABLED
                // Enable depth hit-testing when the device supports it
                if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    config.depthMode = Config.DepthMode.AUTOMATIC
                }
            },
            onSessionUpdated = { session, frame ->
                sessionRef = session
                arTracking = frame.camera.trackingState == TrackingState.TRACKING
                earthTracking = session.earth?.trackingState == TrackingState.TRACKING
            },
            onTouchEvent = { motionEvent, hitResult ->
                if (motionEvent.action == MotionEvent.ACTION_UP
                    && hitResult != null
                    && earthTracking
                ) {
                    val earth = sessionRef?.earth
                    if (earth != null) {
                        coroutineScope.launch {
                            val location = cubePlacementService.placeAndSave(earth, hitResult)
                            if (location != null) {
                                val quat = floatArrayOf(
                                    location.quatX, location.quatY,
                                    location.quatZ, location.quatW
                                )
                                val anchor = earth.createAnchor(
                                    location.latitude, location.longitude,
                                    location.altitude, quat
                                )
                                spawnCubeOnAnchor(anchor)
                            }
                        }
                    }
                }
                true
            },
        )

        Text(
            text = when {
                !arTracking   -> "Initializing AR…"
                !earthTracking -> "Acquiring geospatial positioning…"
                else          -> "Tap to place a cube  •  Placed: $cubeCount"
            },
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        )
    }
}
