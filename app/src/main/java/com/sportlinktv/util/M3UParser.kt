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
                        val parts = keyVal.split(":")
                        if (parts.size >= 2) {
                            current?.put("drmKeyId", parts[0].trim())
                            current?.put("drmKey", parts[1].trim())
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
                        channels.add(Channel(
                            name = attrs["tvg-name"]?.takeIf { it.isNotBlank() } ?: attrs["name"] ?: "Unknown",
                            url = trimmed,
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
