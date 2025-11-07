package com.droidbaza.spotifycompose.domain.model

data class TrackItem(
    val id: String,
    val title: String?,
    val artistName: String?,
    val durationMs: Long?
)
