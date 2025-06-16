package com.mmk.mediaplayerbasic.data.remote

import com.google.gson.annotations.SerializedName

data class TrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val title: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("artist_name") val artist: String,
    @SerializedName("audio") val audioUrl: String,
    @SerializedName("image") val imageUrl: String
)

data class TrackResponse(
    @SerializedName("results") val tracks: List<TrackDto>
)