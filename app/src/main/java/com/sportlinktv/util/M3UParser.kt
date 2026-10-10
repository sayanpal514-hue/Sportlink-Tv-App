package com.sportlinktv.util

import com.sportlinktv.data.local.entity.Channel

object M3UParser {

    fun parse(content: String, defaultGroup: String = "General"): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()
        var current: MutableMap<String, String>? = null

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("#EXTINF") -> {
                    current = mutableMapOf()
                    current["name"] = trimmed.substringAfterLast(",").trim()
                    current["tvg-id"] = extractAttr(trimmed, "tvg-id")
                    current["tvg-name"] = extractAttr(trimmed, "tvg-name")
                    current["tvg-logo"] = extractAttr(trimmed, "tvg-logo")
                    current["group-title"] = extractAttr(trimmed, "group-title")
                }
                trimmed.startsWith("#KODIPROP:inputstream.adaptive.license_key=") -> {
                    val keyVal = trimmed.substringAfter("=")
                    if (keyVal.contains(":") && !keyVal.startsWith("{")) {
                        val pairs = keyVal.split(",")
                        val kids = mutableListOf<String>()
                        val keys = mutableListOf<String>()
                        for (pair in pairs) {
                            val parts = pair.split(":")
                            if (parts.size >= 2) {
                                kids.add(parts[0].trim())
                                keys.add(parts[1].trim())
                            }
                        }
                        if (kids.isNotEmpty()) {
                            current?.put("drmKeyId", kids.joinToString(","))
                            current?.put("drmKey", keys.joinToString(","))
                        }
                    }
                }
                trimmed.startsWith("#KODIPROP:inputstream.adaptive.stream_headers=") -> {
                    val headers = trimmed.substringAfter("=")
                    val cookieMatch = Regex("Cookie=([^;]+)").find(headers)
                    if (cookieMatch != null) {
                        current?.put("cookie", cookieMatch.groupValues[1].trim())
                    }
                    val hdneaMatch = Regex("__hdnea__=[^;&]+").find(headers)
                    if (hdneaMatch != null && current?.get("cookie") == null) {
                        current?.put("cookie", "__hdnea__=" + hdneaMatch.value.replace("__hdnea__=", ""))
                    }
                    val uaMatch = Regex("User-Agent=([^;]+)").find(headers)
                    if (uaMatch != null) {
                        current?.put("userAgent", uaMatch.groupValues[1].trim())
                    }
                }
                trimmed.startsWith("#EXTVLCOPT:http-user-agent=") -> {
                    current?.put("userAgent", trimmed.substringAfter("=").trim())
                }
                trimmed.startsWith("#EXTHTTP:") -> {
                    try {
                        val jsonStr = trimmed.substringAfter(":")
                        val jsonObj = org.json.JSONObject(jsonStr)
                        if (jsonObj.has("cookie")) current?.put("cookie", jsonObj.optString("cookie"))
                        if (jsonObj.has("Origin")) current?.put("origin", jsonObj.optString("Origin"))
                        if (jsonObj.has("Referer")) current?.put("referer", jsonObj.optString("Referer"))
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }
                trimmed.startsWith("http") || trimmed.startsWith("rtmp") || trimmed.startsWith("rtsp") -> {
                    current?.let { attrs ->
                        var streamUrl = trimmed
                        // Handle pipe-separated inline headers: url|cookie=...&referer=...&user-agent=...
                        // Also handle the ?| variant: url?|cookie=...
                        val pipeIdx = trimmed.indexOfFirst { it == '|' }
                        if (pipeIdx != -1) {
                            // Remove the pipe and everything after from URL
                            streamUrl = trimmed.substring(0, pipeIdx).trimEnd('?')
                            val headerStr = trimmed.substring(pipeIdx + 1)
                            headerStr.split("&").forEach { kv ->
                                val eqIdx = kv.indexOf("=")
                                if (eqIdx != -1) {
                                    val k = kv.substring(0, eqIdx).trim().lowercase()
                                    val v = kv.substring(eqIdx + 1).trim()
                                    when (k) {
                                        "cookie" -> if (attrs["cookie"].isNullOrBlank()) attrs["cookie"] = v
                                        "referer" -> if (attrs["referer"].isNullOrBlank()) attrs["referer"] = v
                                        "origin" -> if (attrs["origin"].isNullOrBlank()) attrs["origin"] = v
                                        "user-agent" -> if (attrs["userAgent"].isNullOrBlank()) attrs["userAgent"] = v
                                    }
                                }
                            }
                        }
                        channels.add(Channel(
                            name = attrs["tvg-name"]?.takeIf { it.isNotBlank() } ?: attrs["name"] ?: "Unknown",
                            url = streamUrl,
                            logo = attrs["tvg-logo"] ?: "",
                            group = attrs["group-title"]?.takeIf { it.isNotBlank() } ?: defaultGroup,
                            tvgId = attrs["tvg-id"] ?: "",
                            drmKeyId = attrs["drmKeyId"],
                            drmKey = attrs["drmKey"],
                            cookie = attrs["cookie"],
                            userAgent = attrs["userAgent"],
                            referer = attrs["referer"],
                            origin = attrs["origin"]
                        ))
                        current = null
                    }
                }
            }
        }
        return channels
    }

    private fun extractAttr(line: String, attr: String): String {
        val regex = Regex("$attr=\"([^\"]*)\"")
        return regex.find(line)?.groupValues?.getOrNull(1) ?: ""
    }
}
