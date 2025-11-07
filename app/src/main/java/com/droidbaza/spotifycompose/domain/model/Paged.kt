package com.droidbaza.spotifycompose.domain.model

data class Paged<T>(
    val items: List<T>,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val hasMore: Boolean
)
