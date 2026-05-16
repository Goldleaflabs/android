// =====================================================
// File: Exceptions.kt
// Location: core/src/main/java/com/goldleaf/core/data/network/Exceptions.kt
// =====================================================
package com.goldleaf.core.data.network

class NoNetworkException(message: String) : Exception(message)
class AuthenticationException(message: String) : Exception(message)
class ServerException(message: String) : Exception(message)
class ApiException(
    val code: Int,
    override val message: String,
    val errors: Map<String, List<String>>? = null
) : Exception(message)