package com.lubriciel.bigarrer.ar.features

import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.lubriciel.bigarrer.ar.Renderer
import com.lubriciel.bigarrer.ar.World
import io.github.sceneview.math.Position
import kotlin.math.sqrt

/**
 * Feature: leave a trail of cubes behind the AR camera.
 *
 * Subscribes to per-frame updates from a [World] and, every time the AR camera has moved
 * at least [spacingM] meters from the previously placed cube, asks the [Renderer] to
 * spawn a new one. Owns the "where am I, where was the last cube, do I need a new one
 * yet" decision — the layer above (the Composable) doesn't see any of that, and the
 * layers below (Renderer, World) don't know about the trail rule.
 *
 * @param world AR scaffolding to subscribe to.
 * @param renderer Render API used to materialize cubes.
 * @param spacingM Minimum distance the camera must travel between cubes, in meters.
 */
class CubeTrail(
    private val world: World,
    private val renderer: Renderer,
    private val spacingM: Float = 1.0f,
) {

    private var lastCubePos: Position? = null

    private val updateListener = World.UpdateListener { session, frame ->
        onArUpdate(session, frame)
    }

    init {
        world.addUpdateListener(updateListener)
    }

    /** Detach from world updates. Call when this feature is no longer needed. */
    fun dispose() {
        world.removeUpdateListener(updateListener)
    }

    private fun onArUpdate(session: Session, frame: Frame) {
        val pose = frame.camera.pose
        val current = Position(pose.tx(), pose.ty(), pose.tz())

        val last = lastCubePos
        if (last != null && distance(current, last) < spacingM) return

        renderer.spawnCubeAt(session, pose)
        lastCubePos = current
    }

    private fun distance(a: Position, b: Position): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dz = a.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
