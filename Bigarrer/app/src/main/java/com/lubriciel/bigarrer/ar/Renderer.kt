package com.lubriciel.bigarrer.ar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import com.google.android.filament.Engine
import com.google.android.filament.MaterialInstance
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.Session
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.Node

/**
 * Render-side of the scene.
 *
 * Holds Filament resources (materials) and knows how to build scene objects and add them
 * to the scene graph. Has no opinion about when or why an object should be spawned —
 * that decision belongs to business-layer code that calls [spawnCubeAt] or [spawnCubeAtAnchor].
 *
 * @param engine Filament engine, obtained via SceneView's `rememberEngine()`.
 * @param materialLoader Filament material loader, obtained via `rememberMaterialLoader(engine)`.
 * @param childNodes The scene's child-node list, obtained via `rememberNodes()`.
 * @param cubeColor Color applied to every cube spawned by this renderer.
 * @param cubeSizeM Edge length of each cube in meters.
 */
class Renderer(
    private val engine: Engine,
    materialLoader: MaterialLoader,
    private val childNodes: SnapshotStateList<Node>,
    cubeColor: Color = Color(0xFF4FC3F7),
    private val cubeSizeM: Float = 0.1f,
) {

    /** Number of cubes currently in the scene. Observable from Compose. */
    var cubeCount: Int by mutableIntStateOf(0)
        private set

    private val cubeMaterial: MaterialInstance = materialLoader.createColorInstance(
        color = cubeColor,
        metallic = 0.0f,
        roughness = 0.6f,
        reflectance = 0.5f,
    )

    /**
     * Anchors a cube in world space at [pose]. ARCore keeps the cube fixed at that
     * location as the user moves through the scene.
     */
    fun spawnCubeAt(session: Session, pose: Pose) {
        spawnCubeAtAnchor(session.createAnchor(pose))
    }

    /**
     * Attaches a cube to an existing [anchor] (e.g. a geospatial anchor created by the
     * caller). ARCore will keep the cube at the anchor's world position.
     */
    fun spawnCubeAtAnchor(anchor: Anchor) {
        val anchorNode = AnchorNode(engine = engine, anchor = anchor)
        val cube = CubeNode(
            engine = engine,
            size = Size(cubeSizeM, cubeSizeM, cubeSizeM),
            center = Position(0f, 0f, 0f),
            materialInstance = cubeMaterial,
        )
        anchorNode.addChildNode(cube)
        childNodes += anchorNode
        cubeCount += 1
    }
}
