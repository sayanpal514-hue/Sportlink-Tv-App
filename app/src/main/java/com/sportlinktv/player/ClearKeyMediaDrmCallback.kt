package com.sportlinktv.player

import android.util.Base64
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.exoplayer.drm.MediaDrmCallback
import java.util.UUID

/**
 * A ClearKey DRM callback that directly returns the JWK key set
 * without making any network request.
 *
 * Android's FrameworkMediaDrm for ClearKey sends a JSON key request
 * and expects a JSON key response. This callback intercepts that and
 * returns the pre-known key/keyId pair without hitting a license server.
 */
class ClearKeyMediaDrmCallback(
    private val keyIdHex: String,
    private val keyHex: String
) : MediaDrmCallback {

    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray = ByteArray(0)

    override fun executeKeyRequest(
        uuid: UUID,
        request: ExoMediaDrm.KeyRequest
    ): ByteArray {
        val kidB64List = keyIdHex.split(",").map { hexToBase64Url(it) }
        val keyB64List = keyHex.split(",").map { hexToBase64Url(it) }
        val keysJsonArray = kidB64List.zip(keyB64List).joinToString(",") { (kid, key) ->
            """{"kty":"oct","k":"$key","kid":"$kid"}"""
        }
        val jwkResponse = """{"keys":[$keysJsonArray],"type":"temporary"}"""
        return jwkResponse.toByteArray(Charsets.UTF_8)
    }

    private fun hexToBase64Url(hex: String): String {
        val clean = hex.trim().replace(" ", "")
        val bytes = ByteArray(clean.length / 2)
        for (i in bytes.indices) {
            bytes[i] = ((Character.digit(clean[i * 2], 16) shl 4) +
                         Character.digit(clean[i * 2 + 1], 16)).toByte()
        }
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
