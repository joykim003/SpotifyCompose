package com.droidbaza.spotifycompose.network

import retrofit2.http.GET

interface ApiService {
    // Placeholder endpoint; adapt paths and models to the actual OpenAPI spec
    @GET("tracks")
    suspend fun getTracks(): List<TrackDto>
}
