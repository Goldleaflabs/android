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
        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
            Log.d("AuthInterceptor", "Added header: Bearer $it")
        } ?: Log.d("AuthInterceptor", "No token available")

        return chain.proceed(requestBuilder.build())
    }
}