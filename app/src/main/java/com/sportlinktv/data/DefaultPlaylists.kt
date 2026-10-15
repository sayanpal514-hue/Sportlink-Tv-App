package com.sportlinktv.data

import androidx.annotation.DrawableRes
import com.sportlinktv.R

enum class PlaylistFormat { M3U, JSON_ZEE5, JSON_HOTSTAR, JSON_LIVE_EVENT }

data class DefaultPlaylist(
    val name: String,
    @DrawableRes val icon: Int,
    val url: String,
    val format: PlaylistFormat = PlaylistFormat.M3U
)

object DefaultPlaylists {
    val list = listOf(
        DefaultPlaylist("FANCODE", R.drawable.logo_fancode, "https://raw.githubusercontent.com/drmlive/fancode-live-events/refs/heads/main/fancode.m3u"),
        DefaultPlaylist("SONYLIV", R.drawable.logo_sonyliv, "https://raw.githubusercontent.com/drmlive/sliv-live-events/refs/heads/main/sonyliv.m3u"),
        DefaultPlaylist("WILLOW", R.drawable.logo_willow, "https://raw.githubusercontent.com/srhady/willow-event/refs/heads/main/live_sports.m3u"),
        DefaultPlaylist("PRIMEVIDEO", R.drawable.logo_primevideo, "https://raw.githubusercontent.com/srhady/willow-event/refs/heads/main/primevideo_sports.m3u"),
        DefaultPlaylist("JIO-TV", R.drawable.logo_jiotv, "https://sportlink-playlist.pages.dev/zio.m3u"),
        DefaultPlaylist("ZEE5", R.drawable.logo_zee5, "https://vk-playlist.pages.dev/zee3.m3u"),
        DefaultPlaylist("SONY", R.drawable.logo_sony, "https://vk-playlist.pages.dev/sony.m3u"),
        DefaultPlaylist("SUN", R.drawable.logo_sun, "https://vk-playlist.pages.dev/sun.m3u"),
        DefaultPlaylist("JIO TV+", R.drawable.logo_jiotv, "https://premiumplugx.top/jiostb/mjelo.php?view=raw"),
        DefaultPlaylist("Hotstar", R.drawable.logo_hotstar, "https://sportlink-playlist.pages.dev/tstar.m3u")
    )
}
