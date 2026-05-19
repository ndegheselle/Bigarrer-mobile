package com.lubriciel.bigarrer.ar.features

import androidx.compose.ui.graphics.Color
import com.google.ar.core.DepthPoint
import com.google.ar.core.InstantPlacementPoint
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.lubriciel.bigarrer.ar.Renderer
import com.lubriciel.bigarrer.ar.World

/**
 * Feature: place a cube in the AR scene at a screen tap.
 *
 * When [onTap] is called with screen-space pixel coordinates, it performs an ARCore hit
 * test against depth, detected planes, and instant-placement surfaces (in that priority
 * order). If the ARCore Geospatial API is tracking, the resulting pose is converted to
 * geospatial coordinates and a geospatial anchor is created so the cube persists at a
 * real-world lat/lng/altitude even across sessions. Otherwise a standard local anchor is
 * used as a fallback.
 *
 * Hit-test priority:
 *  1. [DepthPoint]            — most accurate; uses the depth image if the device has one.
 *  2. [Plane] (in polygon)    — reliable once planes are detected; requires
 *                               PlaneFindingMode.HORIZONTAL_AND_VERTICAL in World config.
 *  3. [InstantPlacementPoint] — approximate surface estimate; useful before planes are found.
 *
 * @param world AR scaffolding that exposes the latest session/frame and tracking state.
 * @param renderer Render API used to materialize cubes.
 * @param color Color of each placed cube.
 */
class TouchPlacement(
    private val world: World,
    private val renderer: Renderer,
    private val color: Color = Color(0xFFFF5722),
) {

    /**
     * Attempt to place a cube at the real-world surface behind screen point ([x], [y]).
     * Both coordinates are in pixels (as reported by Compose pointer input).
     * No-ops if AR is not tracking or if no hit is found.
     */
    fun onTap(x: Float, y: Float) {
        val session = world.latestSession ?: return
        val frame = world.latestFrame ?: return
        if (!world.isTracking) return

        val hits = frame.hitTest(x, y)

        val hit = hits.firstOrNull { it.trackable is DepthPoint }
            ?: hits.firstOrNull {
                val trackable = it.trackable
                trackable is Plane &&
                    trackable.trackingState == TrackingState.TRACKING &&
                    trackable.isPoseInPolygon(it.hitPose)
            }
            ?: hits.firstOrNull { it.trackable is InstantPlacementPoint }
            ?: return

        val earth = session.earth
        if (earth != null && earth.trackingState == TrackingState.TRACKING) {
            val geoPose = earth.getGeospatialPose(hit.hitPose)
            val anchor = earth.createAnchor(
                geoPose.latitude,
                geoPose.longitude,
                geoPose.altitude,
                geoPose.eastUpSouthQuaternion,
            )
            renderer.spawnCubeAtAnchor(anchor, color)
        } else {
            renderer.spawnCubeAt(session, hit.hitPose, color)
        }
    }
}
