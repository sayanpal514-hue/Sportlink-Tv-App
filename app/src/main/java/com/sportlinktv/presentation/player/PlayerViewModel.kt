package com.sportlinktv.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.sportlinktv.data.local.entity.Channel
import com.sportlinktv.player.PendingChannelHolder
import com.sportlinktv.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoQuality(
    val label: String,
    val width: Int,
    val height: Int,
    val bitrate: Int
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val pendingHolder: PendingChannelHolder
) : ViewModel() {

    val player: ExoPlayer get() = playerManager.player
    val trackSelector: DefaultTrackSelector get() = playerManager.trackSelector

    private val _qualities = MutableStateFlow<List<VideoQuality>>(emptyList())
    val qualities: StateFlow<List<VideoQuality>> = _qualities

    private val _selectedQualityLabel = MutableStateFlow("Auto")
    val selectedQualityLabel: StateFlow<String> = _selectedQualityLabel

    /** Called from PlayerScreen – uses pendingHolder if available, else falls back to url/name */
    fun play(channelUrl: String, channelName: String) {
        val channel = pendingHolder.pendingChannel?.takeIf { it.url == channelUrl }
            ?: Channel(url = channelUrl, name = channelName)
        pendingHolder.pendingChannel = null

        playerManager.play(channel)
        _qualities.value = emptyList()
        _selectedQualityLabel.value = "Auto"

        // Poll for available tracks after the player prepares
        viewModelScope.launch {
            repeat(12) {
                delay(1500)
                refreshTracks()
                if (_qualities.value.isNotEmpty()) return@launch
            }
        }
    }

    fun refreshTracks() {
        val tracks = player.currentTracks
        val found = mutableListOf<VideoQuality>()
        for (group in tracks.groups) {
            if (group.type != C.TRACK_TYPE_VIDEO) continue
            val trackGroup = group.mediaTrackGroup
            for (i in 0 until trackGroup.length) {
                val fmt = trackGroup.getFormat(i)
                val h = fmt.height
                val w = fmt.width
                val br = fmt.bitrate
                if (h <= 0) continue
                val label = when {
                    h >= 2160 -> "4K (${h}p)"
                    h >= 1080 -> "FHD (${h}p)"
                    h >= 720  -> "HD (${h}p)"
                    h >= 480  -> "SD (${h}p)"
                    h >= 360  -> "Low (${h}p)"
                    else      -> "${h}p"
                }
                found.add(VideoQuality(label = label, width = w, height = h, bitrate = br))
            }
        }
        _qualities.value = found.distinctBy { it.height }.sortedByDescending { it.height }
    }

    fun setQuality(quality: VideoQuality?) {
        if (quality == null) {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .clearVideoSizeConstraints()
                    .setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
                    .setMaxVideoBitrate(Int.MAX_VALUE)
                    .setForceHighestSupportedBitrate(false)
            )
            _selectedQualityLabel.value = "Auto"
        } else {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .setMaxVideoSize(
                        if (quality.width > 0) quality.width else Int.MAX_VALUE,
                        quality.height
                    )
                    .setMinVideoSize(1, quality.height / 2)
                    .setMaxVideoBitrate(
                        if (quality.bitrate > 0) quality.bitrate + 300_000 else Int.MAX_VALUE
                    )
            )
            _selectedQualityLabel.value = quality.label
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.pause()
    }
}
