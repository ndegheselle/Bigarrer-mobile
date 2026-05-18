package com.lubriciel.bigarrer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import kotlin.math.sqrt

/** Edge length of each cube in meters. */
private const val CUBE_SIZE_M = 0.1f

/** Drop a new cube each time the camera has moved this far from the previous one. */
private const val CUBE_SPACING_M = 0.5f

/**
 * Minimal AR scene built on Filament (via SceneView) and ARCore.
 *
 * Behavior: while AR is tracking, the camera pose is sampled every frame and a small cube
 * is anchored at the camera's current world position whenever it has moved at least
 * [CUBE_SPACING_M] meters from the previously placed cube. The result is a breadcrumb-style
 * trail of cubes following the user's path.
 */
@Composable
fun ArCubeScene() {
    val engine = rememberEngine()
    val view = rememberView(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()
    val collisionSystem = rememberCollisionSystem(view)

    // One material shared by every cube — light blue, slightly rough.
    val cubeMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(
            color = Color(0xFF4FC3F7),
            metallic = 0.0f,
            roughness = 0.6f,
            reflectance = 0.5f,
        )
    }

    var arTracking by remember { mutableStateOf(false) }
    var cubeCount by remember { mutableIntStateOf(0) }
    var lastCubePos by remember { mutableStateOf<Position?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            view = view,
            childNodes = childNodes,
            cameraNode = cameraNode,
            collisionSystem = collisionSystem,
            sessionConfiguration = { session, config ->
                // Plain local AR — no geospatial / no plane finding needed for a trail.
                config.depthMode =
                    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                        Config.DepthMode.AUTOMATIC
                    else
                        Config.DepthMode.DISABLED
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.planeFindingMode = Config.PlaneFindingMode.DISABLED
            },
            onSessionUpdated = { session, frame ->
                val camera = frame.camera
                arTracking = camera.trackingState == TrackingState.TRACKING
                if (!arTracking) return@ARScene

                val pose = camera.pose
                val current = Position(pose.tx(), pose.ty(), pose.tz())

                val last = lastCubePos
                val shouldPlace = last == null || distance(current, last) >= CUBE_SPACING_M
                if (!shouldPlace) return@ARScene

                // Anchor a cube in world space at the camera's current position. Using the
                // raw camera pose keeps it simple — ARCore will hold each cube fixed in the
                // world as you move past it.
                val anchor = session.createAnchor(pose)
                val anchorNode = AnchorNode(engine = engine, anchor = anchor)
                val cube = CubeNode(
                    engine = engine,
                    size = Size(CUBE_SIZE_M, CUBE_SIZE_M, CUBE_SIZE_M),
                    center = Position(0f, 0f, 0f),
                    materialInstance = cubeMaterial,
                )
                anchorNode.addChildNode(cube)
                childNodes += anchorNode

                lastCubePos = current
                cubeCount += 1
            },
        )

        Text(
            text = if (arTracking)
                "Walk around — a cube every $CUBE_SPACING_M m  •  Placed: $cubeCount"
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

private fun distance(a: Position, b: Position): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    val dz = a.z - b.z
    return sqrt(dx * dx + dy * dy + dz * dz)
}
