package com.goldleaf.feature.advisoryservices.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64
import java.io.File
import java.io.FileOutputStream

/**
 * Utility functions for image handling in advisory services
 * Handles conversion between Uri, Bitmap, and Base64 for API submission
 */
object ImageUtils {
    
    /**
     * Convert image URI to Base64 string for API submission
     * @param context Android Context
     * @param uri Image URI from gallery or camera
     * @return Base64 encoded string of image data
     */
    suspend fun convertUriToBase64(context: Context, uri: Uri?): String {
        if (uri == null) return ""
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@withContext ""
                inputStream?.close()
                Base64.getEncoder().encodeToString(bytes)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
    
    /**
     * Convert image URI to ByteArray for direct processing
     * @param context Android Context
     * @param uri Image URI
     * @return ByteArray of image data
     */
    suspend fun convertUriToByteArray(context: Context, uri: Uri?): ByteArray {
        if (uri == null) return byteArrayOf()
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: byteArrayOf()
                inputStream?.close()
                bytes
            } catch (e: Exception) {
                e.printStackTrace()
                byteArrayOf()
            }
        }
    }
    
    /**
     * Get the file size of image at URI
     * @param context Android Context
     * @param uri Image URI
     * @return File size in bytes, or -1 if error
     */
    suspend fun getImageFileSize(context: Context, uri: Uri?): Long {
        if (uri == null) return -1
        return withContext(Dispatchers.IO) {
            try {
                val bytes = convertUriToByteArray(context, uri)
                bytes.size.toLong()
            } catch (e: Exception) {
                -1
            }
        }
    }
    
    /**
     * Check if image at URI is valid (readable and not too large)
     * @param context Android Context
     * @param uri Image URI
     * @param maxSizeMB Maximum size in MB (default 10MB)
     * @return true if valid, false otherwise
     */
    suspend fun isValidImage(context: Context, uri: Uri?, maxSizeMB: Int = 10): Boolean {
        val fileSize = getImageFileSize(context, uri)
        return fileSize > 0 && fileSize <= (maxSizeMB * 1024 * 1024)
    }
    
    /**
     * Save URI image to app's cache directory
     * Useful for processing before sending to API
     * @param context Android Context
     * @param uri Source image URI
     * @return File path if successful, empty string if failed
     */
    suspend fun saveImageToCache(context: Context, uri: Uri?): String {
        if (uri == null) return ""
        return withContext(Dispatchers.IO) {
            try {
                val bytes = convertUriToByteArray(context, uri)
                if (bytes.isEmpty()) return@withContext ""
                
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val file = File(context.cacheDir, fileName)
                
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(bytes)
                }
                
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

    

    suspend fun getImageMimeType(context: Context, uri: Uri?): String {
        if (uri == null) return "image/*"
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.getType(uri) ?: "image/*"
            } catch (e: Exception) {
                "image/*"
            }
        }
    }
    
    /**
     * Clean up old cached images
     * Call periodically to prevent cache bloat
     * @param context Android Context
     * @param maxAgeHours Delete images older than this many hours (default 24)
     **/
    suspend fun cleanupOldCachedImages(context: Context, maxAgeHours: Int = 24) {
        return withContext(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir
                val cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000)
                
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("image_") && file.lastModified() < cutoffTime) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
