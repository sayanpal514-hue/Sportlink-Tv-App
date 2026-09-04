package com.sportlinktv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val logo: String = "",
    val group: String = "General",
    val tvgId: String = "",
    val isFavorite: Boolean = false,
    val playlistId: Long = 0,
    val drmKeyId: String? = null,
    val drmKey: String? = null,
    val cookie: String? = null,
    val userAgent: String? = null,
    val referer: String? = null,
    val origin: String? = null
)
