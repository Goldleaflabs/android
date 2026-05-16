package com.goldleaf.core.data.network

// =====================================================
// File: ApiResponse.kt
// Location: core/src/main/java/com/goldleaf/core/data/network/ApiResponse.kt
// =====================================================


sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(
        val code: Int,
        val message: String,
        val error: Throwable? = null
    ) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}