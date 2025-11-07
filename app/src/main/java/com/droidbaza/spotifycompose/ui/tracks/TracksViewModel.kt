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

    private val _loadingInitial = MutableStateFlow(false)
    val loadingInitial: StateFlow<Boolean> = _loadingInitial

    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var offset: Int = 0
    private val limit: Int = 20
    private var hasMore: Boolean = true

    fun loadInitial() {
        _tracks.value = emptyList()
        offset = 0
        hasMore = true
        _loadingInitial.value = true
        viewModelScope.launch {
            runCatching { getPaged(limit, offset) }
                .onSuccess { page ->
                    _tracks.value = page.items
                    offset = page.items.size
                    hasMore = page.hasMore
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Une erreur est survenue"
                }
            _loadingInitial.value = false
        }
    }

    fun loadMore() {
        if (!hasMore || _loadingMore.value) return
        _loadingMore.value = true
        viewModelScope.launch {
            runCatching { getPaged(limit, offset) }
                .onSuccess { page ->
                    _tracks.value = _tracks.value + page.items
                    offset += page.items.size
                    hasMore = page.hasMore
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Une erreur est survenue"
                }
            _loadingMore.value = false
        }
    }

    fun clearError() { _errorMessage.value = null }
}
