package com.sportlinktv.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.sportlinktv.data.local.entity.Channel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val trackSelector: DefaultTrackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            buildUponParameters()
                .setPreferredAudioLanguage("en")
                .setAllowVideoMixedMimeTypeAdaptiveness(true)
        )
    }

    val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(15_000, 50_000, 2_500, 5_000)
                    .build()
            )
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                playWhenReady = true
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            }
    }

    fun play(channel: Channel) {
        // Build MediaItem (NO DrmConfiguration — we use DrmSessionManager below)
        val itemBuilder = MediaItem.Builder()
            .setUri(channel.url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(channel.name)
                    .setArtworkUri(
                        if (channel.logo.isNotEmpty()) Uri.parse(channel.logo) else null
                    )
                    .build()
            )

        // Determine stream format
        val hasDrmKeys = !channel.drmKeyId.isNullOrEmpty() && !channel.drmKey.isNullOrEmpty()
        val isDash = channel.url.contains(".mpd", ignoreCase = true) || hasDrmKeys
        val isHls  = !isDash && channel.url.contains(".m3u8", ignoreCase = true)

        when {
            isDash -> itemBuilder.setMimeType(MimeTypes.APPLICATION_MPD)
            isHls  -> itemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }

        val mediaItem = itemBuilder.build()

        // Build per-channel HTTP data source with correct User-Agent + headers
        val ua = channel.userAgent?.takeIf { it.isNotBlank() }
            ?: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"

        val headers = mutableMapOf<String, String>()
        channel.cookie?.takeIf { it.isNotBlank() }?.let { headers["Cookie"] = it }
        channel.referer?.takeIf { it.isNotBlank() }?.let { headers["Referer"] = it }
        channel.origin?.takeIf { it.isNotBlank() }?.let { headers["Origin"] = it }
        
        // Fallback for some hardcoded URLs if they didn't have explicit headers in M3U
        if (channel.url.contains("jiotv", ignoreCase = true) && !headers.containsKey("Referer")) {
            headers["Referer"] = "https://www.jiotv.com/"
            headers["Origin"]  = "https://www.jiotv.com"
        }

        val dsFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(ua)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(20_000)
            .setDefaultRequestProperties(headers)

        // Build the MediaSourceFactory — inject DRM session manager only when keys are present
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dsFactory)

        if (hasDrmKeys) {
            try {
                val drmCallback = ClearKeyMediaDrmCallback(
                    keyIdHex = channel.drmKeyId!!,
                    keyHex   = channel.drmKey!!
                )
                val drmSessionManager = DefaultDrmSessionManager.Builder()
                    .setMultiSession(false)
                    .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                    .build(drmCallback)

                mediaSourceFactory.setDrmSessionManagerProvider(
                    DrmSessionManagerProvider { drmSessionManager }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)

        // Reset track selector to Auto for each new channel
        trackSelector.setParameters(
            trackSelector.buildUponParameters()
                .clearVideoSizeConstraints()
                .setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
                .setMaxVideoBitrate(Int.MAX_VALUE)
                .setForceHighestSupportedBitrate(false)
        )

        player.setMediaSource(mediaSource)
        player.prepare()
    }

    fun release() {
        player.release()
    }
}
