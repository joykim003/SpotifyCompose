package com.droidbaza.spotifycompose.network

data class TrackDto(
    val id: String,
    val title: String,
    val artist: String? = null
)
