package com.lubriciel.bigarrer.service

import com.google.ar.core.Earth
import com.google.ar.core.HitResult
import com.lubriciel.bigarrer.data.SavedCubeLocation

class CubePlacementService(private val storage: LocationStorageService) {

    suspend fun placeAndSave(earth: Earth, hitResult: HitResult): SavedCubeLocation? {
        return try {
            val geoPose = earth.getGeospatialPose(hitResult.hitPose)
            val quat = geoPose.eastUpSouthQuaternion
            val location = SavedCubeLocation(
                latitude  = geoPose.latitude,
                longitude = geoPose.longitude,
                altitude  = geoPose.altitude,
                quatX     = quat[0],
                quatY     = quat[1],
                quatZ     = quat[2],
                quatW     = quat[3],
            )
            val id = storage.save(location)
            location.copy(id = id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loadAll(): List<SavedCubeLocation> = storage.getAll()

    suspend fun remove(id: Long) = storage.delete(id)
}
