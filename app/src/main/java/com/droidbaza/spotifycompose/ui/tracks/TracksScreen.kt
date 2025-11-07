package com.droidbaza.spotifycompose.ui.tracks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidbaza.spotifycompose.domain.model.TrackItem
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun TracksScreen(viewModel: TracksViewModel = viewModel()) {
    val tracks by viewModel.tracks.collectAsState()
    val loadingInitial by viewModel.loadingInitial.collectAsState()
    val loadingMore by viewModel.loadingMore.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadInitial() }
    val listState = rememberLazyListState()

    // Infinite scroll: trigger loadMore when approaching the end
    LaunchedEffect(listState, tracks) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .map { lastIndex -> lastIndex >= tracks.lastIndex - 5 }
            .distinctUntilChanged()
            .filter { it }
            .collect { viewModel.loadMore() }
    }

    // Error toast
    val context = LocalContext.current
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            if (loadingInitial && tracks.isEmpty()) {
                CircularProgressIndicator()
            } else {
                TrackList(tracks, state = listState)
                if (loadingMore) {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun TrackList(list: List<TrackItem>, state: androidx.compose.foundation.lazy.LazyListState) {
    LazyColumn(state = state) {
        items(list) { item ->
            val title = item.title ?: "Untitled"
            val artist = item.artistName ?: "Unknown"
            Text(text = "$title — $artist")
        }
    }
}
