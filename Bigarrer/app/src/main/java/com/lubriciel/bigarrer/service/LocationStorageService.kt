package com.lubriciel.bigarrer.service

import com.lubriciel.bigarrer.data.SavedCubeLocation

interface LocationStorageService {
    suspend fun save(location: SavedCubeLocation): Long
    suspend fun getAll(): List<SavedCubeLocation>
    suspend fun delete(id: Long)
}
