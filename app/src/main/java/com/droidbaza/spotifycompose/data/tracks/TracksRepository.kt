package com.droidbaza.spotifycompose.data.tracks

import android.content.Context
import com.droidbaza.spotifycompose.domain.model.Paged
import com.droidbaza.spotifycompose.domain.model.TrackItem
import com.droidbaza.spotifycompose.network.Network
import com.droidbaza.spotifycompose.network.auth.AuthInterceptor
import com.droidbaza.spotifycompose.network.auth.TokenAuthenticator
import com.droidbaza.spotifycompose.network.auth.TokenStore
import com.droidbaza.spotifycompose.network.generated.model.Track

class TracksRepository(
    private val context: Context,
    private val injectedApi: TracksApi? = null
) {

    private val api: TracksApi by lazy {
        injectedApi ?: run {
            val store = TokenStore.getInstance(context)
            val retrofit = Network.retrofit(AuthInterceptor(store), TokenAuthenticator(store))
            retrofit.create(TracksApi::class.java)
        }
    }

    suspend fun getTracks(limit: Int, offset: Int): Paged<TrackItem> {
        val res = api.getTracks(limit = limit, offset = offset)
        val paged = res.data ?: error("Réponse vide")
        val items: List<Track> = paged.items ?: emptyList()
        return Paged(
            items = items.map { it.toItem() },
            total = paged.total ?: 0,
            limit = paged.limit ?: limit,
            offset = paged.offset ?: offset,
            hasMore = paged.hasMore ?: false
        )
    }

    private fun Track.toItem(): TrackItem = TrackItem(
        id = this.id?.toString() ?: "",
        title = this.title,
        artistName = this.artist?.name,
        durationMs = this.durationMs?.toLong()
    )
}
