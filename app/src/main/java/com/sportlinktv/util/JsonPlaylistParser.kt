package com.sportlinktv.util

import com.sportlinktv.data.local.entity.Channel
import org.json.JSONArray

object JsonPlaylistParser {

    /**
     * Parse Zee5 JSON format:
     * { "name", "logo", "url", "KeyId", "Key" }
     */
    fun parseZee5(content: String, defaultGroup: String = "ZEE5"): List<Channel> {
        val channels = mutableListOf<Channel>()
        try {
            val arr = JSONArray(content)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val name = obj.optString("name", "Unknown")
                val logo = obj.optString("logo", "")
                val url = obj.optString("url", "")
                val keyId = obj.optString("KeyId", "").ifBlank { null }
                val key = obj.optString("Key", "").ifBlank { null }
                // Skip entries with null Key (e.g. Zee News plain HLS)
                if (url.isBlank()) continue
                channels.add(
                    Channel(
                        name = name,
                        url = url,
                        logo = logo,
                        group = defaultGroup,
                        tvgId = "",
                        drmKeyId = keyId,
                        drmKey = key,
                        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return channels
    }

    /**
     * Parse Jio Hotstar JSON format:
     * { "id", "name", "logo", "group", "mpd_url", "keyId", "key" }
     */
    fun parseHotstar(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        try {
            val arr = JSONArray(content)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val name = obj.optString("name", "Unknown")
                val logo = obj.optString("logo", "")
                val url = obj.optString("mpd_url", "")
                val group = obj.optString("group", "Hotstar")
                val keyId = obj.optString("keyId", "").ifBlank { null }
                val key = obj.optString("key", "").ifBlank { null }
                if (url.isBlank()) continue
                channels.add(
                    Channel(
                        name = name,
                        url = url,
                        logo = logo,
                        group = group,
                        tvgId = "",
                        drmKeyId = keyId,
                        drmKey = key,
                        userAgent = "Mozilla/5.0 (Linux; Android 11; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return channels
    }
}
