package com.goldleaf.core.util

sealed class Result<out T> {

    /**
     * Extension function to execute a block on failure
     */
    fun onFailure(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) {
            action(exception ?: Exception(message))
        }
        return this
    }

    /**
     * Success state with data
     * @param data The successful result data
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Error state with message
     * @param message The error message describing what went wrong
     * @param exception Optional exception that caused the error
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : Result<Nothing>()
}

/**
 * Extension function to execute a block on success
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Extension function to execute a block on error
 */
inline fun <T> Result<T>.onError(action: (String) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(message)
    }
    return this
}

/**
 * Extension function to map success data to another type
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(message, exception)
    }
}

/**
 * Extension function to get data or null
 */
fun <T> Result<T>.getOrNull(): T? {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }
}

/**
 * Extension function to get data or default value
 */
fun <T> Result<T>.getOrDefault(defaultValue: T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> defaultValue
    }
}

/**
 * Extension function to get data or throw exception
 */
fun <T> Result<T>.getOrThrow(): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> throw exception ?: Exception(message)
    }
}

