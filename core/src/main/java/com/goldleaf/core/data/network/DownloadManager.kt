// =====================================================
// File: DownloadManager.kt
// Location: core/src/main/java/com/goldleaf/core/data/network/DownloadManager.kt
// =====================================================
package com.goldleaf.core.data.network

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class DownloadManager(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun downloadFile(
        url: String,
        destination: File,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(ioDispatcher) {
        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Download failed: ${response.code}")
                )
            }

            val body = response.body ?: return@withContext Result.failure(
                Exception("Empty response body")
            )
            val contentLength = body.contentLength()

            body.byteStream().use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (contentLength > 0) {
                            onProgress(totalBytesRead.toFloat() / contentLength)
                        }
                    }
                }
            }

            Result.success(destination)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
