package com.sportlinktv.data.repository

import com.sportlinktv.data.local.dao.ChannelDao
import com.sportlinktv.data.local.dao.PlaylistDao
import com.sportlinktv.data.local.entity.Channel
import com.sportlinktv.data.local.entity.Playlist
import com.sportlinktv.domain.repository.ChannelRepository
import com.sportlinktv.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor(
    private val channelDao: ChannelDao
) : ChannelRepository {
    override fun getChannels(playlistId: Long): Flow<List<Channel>> = channelDao.getChannels(playlistId)
    override fun getFavorites(): Flow<List<Channel>> = channelDao.getFavorites()
    override suspend fun toggleFavorite(channelId: Long) = channelDao.toggleFavorite(channelId)
}

class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {
    override fun getPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    override suspend fun addPlaylist(name: String, url: String): Long {
        return playlistDao.insertPlaylist(Playlist(name = name, url = url))
    }
}
