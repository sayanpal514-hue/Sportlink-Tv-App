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
    
    fun parseLiveSportEvent(content: String, defaultGroup: String = "Live Sports"): List<Channel> {
        val channels = mutableListOf<Channel>()
        try {
            val arr = JSONArray(content)
            for (i in 0 until arr.length()) {
                val eventObj = arr.getJSONObject(i)
                val eventName = eventObj.optString("title", "Unknown")
                val eventLogo = eventObj.optString("image", "")
                val eventCat = eventObj.optString("cat", defaultGroup)
                
                val streamsArr = eventObj.optJSONArray("resolved_streams") ?: continue
                for (j in 0 until streamsArr.length()) {
                    val streamObj = streamsArr.getJSONObject(j)
                    val title = streamObj.optString("title", eventName)
                    var link = streamObj.optString("link", "")
                    val api = streamObj.optString("api", "")
                    
                    if (link.isBlank()) continue
                    
                    var userAgent: String? = null
                    var cookie: String? = null
                    var referer: String? = null
                    var origin: String? = null
                    
                    if (link.contains("|")) {
                        val parts = link.split("|", limit = 2)
                        link = parts[0]
                        val headers = parts[1].split("&")
                        for (header in headers) {
                            val kv = header.split("=", limit = 2)
                            if (kv.size == 2) {
                                val key = kv[0].trim().lowercase()
                                val value = kv[1].trim()
                                when (key) {
                                    "user-agent" -> userAgent = value
                                    "cookie" -> cookie = value
                                    "referer" -> referer = value
                                    "origin" -> origin = value
                                }
                            }
                        }
                    }
                    
                    var drmKeyId: String? = null
                    var drmKey: String? = null
                    if (api.contains(":")) {
                        val pairs = api.split(",")
                        val kids = mutableListOf<String>()
                        val keys = mutableListOf<String>()
                        for (pair in pairs) {
                            val keyParts = pair.split(":")
                            if (keyParts.size >= 2) {
                                kids.add(keyParts[0].trim())
                                keys.add(keyParts[1].trim())
                            }
                        }
                        if (kids.isNotEmpty()) {
                            drmKeyId = kids.joinToString(",")
                            drmKey = keys.joinToString(",")
                        }
                    }
                    
                    channels.add(
                        Channel(
                            name = "$eventName - $title",
                            url = link,
                            logo = eventLogo,
                            group = eventCat,
                            tvgId = "",
                            drmKeyId = drmKeyId,
                            drmKey = drmKey,
                            userAgent = userAgent,
                            cookie = cookie,
                            referer = referer,
                            origin = origin
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return channels
    }
}
