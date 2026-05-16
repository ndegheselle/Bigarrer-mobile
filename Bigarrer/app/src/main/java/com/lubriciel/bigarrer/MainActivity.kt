package com.lubriciel.bigarrer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.lubriciel.bigarrer.ui.theme.BigarrerTheme

class MainActivity : ComponentActivity() {

    private val cameraPermissionState = mutableStateOf(false)

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionState.value = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraPermissionState.value = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!cameraPermissionState.value) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        setContent {
            BigarrerTheme {
                val hasPermission by cameraPermissionState
                if (hasPermission) {
                    ArCubeTrailScene()
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Camera permission is required for AR",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
