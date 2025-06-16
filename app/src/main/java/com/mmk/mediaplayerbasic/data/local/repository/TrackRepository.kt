package com.mmk.mediaplayerbasic.data.local.repository

import com.mmk.mediaplayerbasic.data.local.dao.TrackDao
import com.mmk.mediaplayerbasic.data.local.entity.TrackEntity
import com.mmk.mediaplayerbasic.data.remote.JamendoApiService
import com.mmk.mediaplayerbasic.data.remote.TrackDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val apiService: JamendoApiService
) {
    fun getAllTracks(): Flow<List<TrackEntity>> {
        return trackDao.getAllTracks()
    }

    suspend fun refreshTracks(clientId: String) {
        try {
            val response = apiService.getPopularTracks(clientId)
            val entities = response.tracks.map { it.toEntity() }
            trackDao.clearAll()
            trackDao.insertAll(entities)
        } catch (e: Exception) {
            throw Exception("Failed to refresh tracks: ${e.message}")
        }
    }

    private fun TrackDto.toEntity(): TrackEntity {
        return TrackEntity(
            id = id,
            title = title,
            artist = artist,
            duration = duration,
            audioUrl = audioUrl,
            imageUrl = imageUrl
        )
    }
}