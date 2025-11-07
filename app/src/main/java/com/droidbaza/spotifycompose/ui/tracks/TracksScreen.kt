package com.droidbaza.spotifycompose.ui.tracks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidbaza.spotifycompose.domain.model.TrackItem

@Composable
fun TracksScreen(viewModel: TracksViewModel = viewModel()) {
    val tracks by viewModel.tracks.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadInitial() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            if (loading && tracks.isEmpty()) {
                CircularProgressIndicator()
            } else {
                TrackList(tracks, onLoadMore = { viewModel.loadMore() })
                if (loading) {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun TrackList(list: List<TrackItem>, onLoadMore: () -> Unit) {
    LazyColumn {
        items(list) { item ->
            val title = item.title ?: "Untitled"
            val artist = item.artistName ?: "Unknown"
            Text(text = "$title — $artist")
        }
        item {
            Button(onClick = onLoadMore) { Text("Charger plus") }
        }
    }
}
