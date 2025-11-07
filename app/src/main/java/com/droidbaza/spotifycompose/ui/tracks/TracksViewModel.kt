package com.droidbaza.spotifycompose.ui.tracks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.droidbaza.spotifycompose.domain.model.TrackItem
import com.droidbaza.spotifycompose.domain.usecase.GetPagedTracksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TracksViewModel(app: Application) : AndroidViewModel(app) {
    private val getPaged = GetPagedTracksUseCase(app)

    private val _tracks = MutableStateFlow<List<TrackItem>>(emptyList())
    val tracks: StateFlow<List<TrackItem>> = _tracks

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var offset: Int = 0
    private val limit: Int = 20
    private var hasMore: Boolean = true

    fun loadInitial() {
        _tracks.value = emptyList()
        offset = 0
        hasMore = true
        loadMore()
    }

    fun loadMore() {
        if (!hasMore || _loading.value) return
        _loading.value = true
        viewModelScope.launch {
            runCatching { getPaged(limit, offset) }
                .onSuccess { page ->
                    _tracks.value = _tracks.value + page.items
                    offset += page.items.size
                    hasMore = page.hasMore
                }
                .onFailure {
                    // noop: surface via UI as needed
                }
            _loading.value = false
        }
    }
}
