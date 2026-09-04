package com.sportlinktv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val localPath: String = "",
    val channelCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
