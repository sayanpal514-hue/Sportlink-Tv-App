package com.sportlinktv.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String
)

object UpdateManager {

    private const val UPDATE_JSON_URL = "https://raw.githubusercontent.com/sayanpal514-hue/Sportlink-Tv-App/main/update.json"
    private val client = OkHttpClient()

    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(UPDATE_JSON_URL)
                .header("Cache-Control", "no-cache")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val remoteCode = json.optInt("versionCode", 0)
            if (remoteCode > currentVersionCode) {
                UpdateInfo(
                    versionCode = remoteCode,
                    versionName = json.optString("versionName", ""),
                    apkUrl = json.optString("apkUrl", ""),
                    changelog = json.optString("changelog", "New update available!")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(apkUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false
            val body = response.body ?: return@withContext false

            val apkFile = File(context.externalCacheDir ?: context.cacheDir, "update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            onProgress(downloadedBytes.toFloat() / totalBytes.toFloat())
                        }
                    }
                    output.flush()
                }
            }

            // Install APK
            withContext(Dispatchers.Main) {
                installApk(context, apkFile)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun installApk(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}
