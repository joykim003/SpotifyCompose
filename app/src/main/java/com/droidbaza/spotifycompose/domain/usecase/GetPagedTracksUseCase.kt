package com.droidbaza.spotifycompose.domain.usecase

import android.content.Context
import com.droidbaza.spotifycompose.data.tracks.TracksRepository
import com.droidbaza.spotifycompose.domain.model.Paged
import com.droidbaza.spotifycompose.domain.model.TrackItem

class GetPagedTracksUseCase(private val context: Context) {
    private val repo = TracksRepository(context)
    suspend operator fun invoke(limit: Int, offset: Int): Paged<TrackItem> =
        repo.getTracks(limit, offset)
}
