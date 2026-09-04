package com.sportlinktv.data.local.dao

import androidx.room.*
import com.sportlinktv.data.local.entity.Channel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    fun getChannels(playlistId: Long): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<Channel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Query("UPDATE channels SET isFavorite = NOT isFavorite WHERE id = :channelId")
    suspend fun toggleFavorite(channelId: Long)
}
