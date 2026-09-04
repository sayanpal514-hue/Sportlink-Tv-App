package com.sportlinktv.domain.repository

import com.sportlinktv.data.local.entity.Channel
import com.sportlinktv.data.local.entity.Playlist
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun getChannels(playlistId: Long): Flow<List<Channel>>
    fun getFavorites(): Flow<List<Channel>>
    suspend fun toggleFavorite(channelId: Long)
}

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addPlaylist(name: String, url: String): Long
}
