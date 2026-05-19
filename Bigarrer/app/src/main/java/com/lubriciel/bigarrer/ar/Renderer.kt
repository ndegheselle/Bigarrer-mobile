package com.lubriciel.bigarrer.ar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import com.google.android.filament.Engine
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
 * Holds Filament resources and knows how to build scene objects and add them to the scene
 * graph. The color of each cube is chosen by the caller at spawn time, so a single
 * renderer instance can serve multiple features with distinct colours.
 *
 * @param engine Filament engine, obtained via SceneView's `rememberEngine()`.
 * @param materialLoader Filament material loader, obtained via `rememberMaterialLoader(engine)`.
 * @param childNodes The scene's child-node list, obtained via `rememberNodes()`.
 * @param cubeSizeM Edge length of each cube in meters.
 */
class Renderer(
    private val engine: Engine,
    private val materialLoader: MaterialLoader,
    private val childNodes: SnapshotStateList<Node>,
    private val cubeSizeM: Float = 0.1f,
) {

    /** Total number of cubes currently in the scene. Observable from Compose. */
    var cubeCount: Int by mutableIntStateOf(0)
        private set

    /**
     * Anchors a cube in world space at [pose] with the given [color].
     * ARCore keeps the cube fixed at that location as the user moves.
     */
    fun spawnCubeAt(session: Session, pose: Pose, color: Color) {
        spawnCubeAtAnchor(session.createAnchor(pose), color)
    }

    /**
     * Attaches a cube with the given [color] to an existing [anchor] (e.g. a geospatial
     * anchor created by the caller).
     */
    fun spawnCubeAtAnchor(anchor: Anchor, color: Color) {
        val material = materialLoader.createColorInstance(
            color = color,
            metallic = 0.0f,
            roughness = 0.6f,
            reflectance = 0.5f,
        )
        val anchorNode = AnchorNode(engine = engine, anchor = anchor)
        val cube = CubeNode(
            engine = engine,
            size = Size(cubeSizeM, cubeSizeM, cubeSizeM),
            center = Position(0f, 0f, 0f),
            materialInstance = material,
        )
        anchorNode.addChildNode(cube)
        childNodes += anchorNode
        cubeCount += 1
    }
}
