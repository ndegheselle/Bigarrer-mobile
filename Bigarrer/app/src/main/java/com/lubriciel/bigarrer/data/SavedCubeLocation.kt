package com.lubriciel.bigarrer.data

data class SavedCubeLocation(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    // EUS (East-Up-South) quaternion from ARCore GeospatialPose
    val quatX: Float,
    val quatY: Float,
    val quatZ: Float,
    val quatW: Float,
    val timestamp: Long = System.currentTimeMillis()
)
