package com.sportlinktv.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlinktv.data.DefaultPlaylist
import com.sportlinktv.data.DefaultPlaylists
import com.sportlinktv.data.PlaylistFormat
import com.sportlinktv.data.local.entity.Channel
import com.sportlinktv.util.JsonPlaylistParser
import com.sportlinktv.util.M3UParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val client = OkHttpClient()

    init {
        loadDefaultPlaylists()
    }

    private fun loadDefaultPlaylists() {
        _uiState.value = _uiState.value.copy(playlists = DefaultPlaylists.list, isLoading = false)
    }

    fun selectPlaylist(playlist: DefaultPlaylist) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val content = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url(playlist.url)
                        .header("User-Agent", "Mozilla/5.0")
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) response.body?.string() ?: "" else ""
                }
                if (content.isNotEmpty()) {
                    val channels = when (playlist.format) {
                        PlaylistFormat.JSON_ZEE5 -> JsonPlaylistParser.parseZee5(content, playlist.name)
                        PlaylistFormat.JSON_HOTSTAR -> JsonPlaylistParser.parseHotstar(content)
                        PlaylistFormat.M3U -> M3UParser.parse(content)
                    }
                    _uiState.value = _uiState.value.copy(
                        channels = channels,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load playlist"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearChannels() {
        _uiState.value = _uiState.value.copy(channels = emptyList(), error = null, searchQuery = "")
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}

data class HomeUiState(
    val playlists: List<DefaultPlaylist> = emptyList(),
    val channels: List<Channel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = ""
) {
    val filteredChannels: List<Channel>
        get() = if (searchQuery.isBlank()) channels else channels.filter { it.name.contains(searchQuery, ignoreCase = true) }
}
