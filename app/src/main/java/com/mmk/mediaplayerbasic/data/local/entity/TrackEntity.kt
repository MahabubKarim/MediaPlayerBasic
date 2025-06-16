package com.mmk.mediaplayerbasic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val duration: Int,
    val audioUrl: String,
    val imageUrl: String,
    val lastUpdated: Long = System.currentTimeMillis()
)