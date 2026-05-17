package com.lubriciel.bigarrer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lubriciel.bigarrer.service.CubePlacementService
import com.lubriciel.bigarrer.service.SqliteLocationService
import com.lubriciel.bigarrer.ui.theme.BigarrerTheme

class MainActivity : ComponentActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    private val permissionsGranted = mutableStateOf(false)

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted.value = results.values.all { it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionsGranted.value = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionsGranted.value) {
            requestPermissions.launch(requiredPermissions)
        }

        val cubePlacementService = CubePlacementService(SqliteLocationService(applicationContext))

        setContent {
            BigarrerTheme {
                val granted by permissionsGranted
                if (granted) {
                    ArCubeScene(cubePlacementService)
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Camera and location permissions are required for AR",
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                        )
                    }
                }
            }
        }
    }
}
