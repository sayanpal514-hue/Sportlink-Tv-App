package com.sportlinktv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sportlinktv.data.local.entity.Channel
import com.sportlinktv.data.local.entity.Playlist
import com.sportlinktv.data.local.dao.ChannelDao
import com.sportlinktv.data.local.dao.PlaylistDao

@Database(entities = [Channel::class, Playlist::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun playlistDao(): PlaylistDao
}
