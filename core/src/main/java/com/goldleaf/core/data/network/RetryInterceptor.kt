package com.goldleaf.core.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class RetryInterceptor(
    private val maxRetries: Int = 2,
    private val initialDelayMs: Long = 1000,
    private val maxDelayMs: Long = 10000,
    private val backoffMultiplier: Double = 2.0,
    private val retryableStatusCodes: Set<Int> = setOf(408, 429, 502, 503, 504)
) : Interceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // ✅ NEW: Don't retry POST, PUT, or PATCH requests
        if (request.method == "POST" || request.method == "PUT" || request.method == "PATCH") {
            return chain.proceed(request)
        }

        var currentDelay = initialDelayMs

        for (attempt in 0 until maxRetries) { // Using for-loop for clearer exit logic
            try {
                val response = chain.proceed(request)
              // 1. If successful or not worth retrying, return it immediately.
                // This response is OPEN and safe for the Logging Interceptor.
                if (response.isSuccessful || !retryableStatusCodes.contains(response.code)) {
                    return response
                }
            // 2. If this is the ABSOLUTE last attempt, return the error response.
                // DO NOT close it, or the Logging Interceptor will crash.
                if (attempt == maxRetries - 1) {
                    Log.e(TAG, "Max retries exceeded, returning error code ${response.code}")
                    return response
                }
         // 3. Otherwise, we ARE going to retry.
                // We must close THIS specific response before the next proceed() call.
                Log.w(TAG, "Retrying code ${response.code}, attempt ${attempt + 1}")
                response.close()
                performDelay(currentDelay, attempt)
                currentDelay = calculateNextDelay(currentDelay)

            } catch (e: Exception) {
                if (!isRetryableException(e) || attempt == maxRetries - 1) throw e

                performDelay(currentDelay, attempt)
                currentDelay = calculateNextDelay(currentDelay)
            }
        }

        throw IOException("Retries exhausted")
    }


    private fun isRetryableException(exception: Exception): Boolean {
        return when (exception) {
            is SocketTimeoutException -> true
            is IOException -> {
                // Retry most IOExceptions except connection issues that won't resolve
                exception !is UnknownHostException && exception !is SSLException
            }
            else -> false
        }
    }

    private fun performDelay(delayMs: Long, attempt: Int) {
        try {
            Log.d(TAG, "Waiting ${delayMs}ms before retry attempt ${attempt + 2}")
            // Use background thread to avoid blocking the calling thread (which may be Main)
            val delayThread = Thread {
                try {
                    Thread.sleep(delayMs)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    Log.w(TAG, "Retry delay interrupted")
                }
            }
            delayThread.isDaemon = true
            delayThread.start()
            delayThread.join()  // Wait for delay to complete without blocking Main thread
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            Log.w(TAG, "Retry delay interrupted")
        }
    }

    private fun calculateNextDelay(currentDelay: Long): Long {
        return (currentDelay * backoffMultiplier)
            .toLong()
            .coerceAtMost(maxDelayMs)
    }
}