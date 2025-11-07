package com.droidbaza.spotifycompose.data.tracks

import com.droidbaza.spotifycompose.data.auth.ApiResponse
import com.droidbaza.spotifycompose.network.generated.model.PaginatedTracks
import retrofit2.http.GET
import retrofit2.http.Query

interface TracksApi {
    @GET("tracks")
    suspend fun getTracks(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ApiResponse<PaginatedTracks>
}
