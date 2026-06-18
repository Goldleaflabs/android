package com.goldleaf.core.data.network

import android.util.Log
import com.goldleaf.core.auth.UserSessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userSessionManager: UserSessionManager  // Direct injection!
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = userSessionManager.getAuthTokenSync()
        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
            // Mask token in logs: show first/last 6 characters only
            val masked = if (token.length > 12) token.take(6) + "..." + token.takeLast(6) else "[redacted]"
            Log.d("AuthInterceptor", "Added Authorization header: Bearer $masked")
        } else {
            Log.d("AuthInterceptor", "No token available")
        }

        return chain.proceed(requestBuilder.build())
    }
}