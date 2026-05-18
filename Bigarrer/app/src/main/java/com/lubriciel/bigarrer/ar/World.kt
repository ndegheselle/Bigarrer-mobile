package com.lubriciel.bigarrer.ar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState

/**
 * AR-side scaffolding for the scene.
 *
 * Owns the ARCore session configuration, exposes tracking state, and fans per-frame
 * updates out to subscribed [UpdateListener]s. Renderers and other scene components
 * subscribe here to receive frame data without having to know about the SceneView
 * Composable wiring.
 *
 * Lifecycle: instantiate once per scene (typically inside a Compose `remember {}`),
 * forward [configureSession] from `ARScene(sessionConfiguration = ...)` and
 * [onSessionUpdated] from `ARScene(onSessionUpdated = ...)`.
 */
class World {

    /** Whether the camera is currently tracking. Observable from Compose. */
    var isTracking: Boolean by mutableStateOf(false)
        private set

    private val updateListeners = mutableListOf<UpdateListener>()

    /** Subscribe to per-frame updates. Only invoked while AR is tracking. */
    fun addUpdateListener(listener: UpdateListener) {
        updateListeners += listener
    }

    fun removeUpdateListener(listener: UpdateListener) {
        updateListeners -= listener
    }

    /** Forward this from `ARScene(sessionConfiguration = ...)`. */
    fun configureSession(session: Session, config: Config) {
        config.depthMode =
            if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                Config.DepthMode.AUTOMATIC
            else
                Config.DepthMode.DISABLED
        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
    }

    /** Forward this from `ARScene(onSessionUpdated = ...)`. */
    fun onSessionUpdated(session: Session, frame: Frame) {
        isTracking = frame.camera.trackingState == TrackingState.TRACKING
        if (!isTracking) return
        // Iterate over a snapshot so listeners can safely add/remove during dispatch.
        updateListeners.toList().forEach { it.onArUpdate(session, frame) }
    }

    /** Called once per frame while AR is tracking. */
    fun interface UpdateListener {
        fun onArUpdate(session: Session, frame: Frame)
    }
}
