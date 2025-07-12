package com.example.kelvinma.activitytracker.util

/**
 * A sealed class representing the result of an operation that can succeed or fail.
 * Used for consistent error handling throughout the app.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> defaultValue
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }
    
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }
    
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> try {
            Success(transform(data))
        } catch (e: Exception) {
            Error(e)
        }
        is Error -> this
    }
}

/**
 * Executes a block of code and wraps the result in a Result object.
 */
inline fun <T> safeCall(action: () -> T): Result<T> = try {
    Result.Success(action())
} catch (e: Exception) {
    Result.Error(e)
}

/**
 * Executes a suspend block of code and wraps the result in a Result object.
 */
suspend inline fun <T> safeSuspendCall(crossinline action: suspend () -> T): Result<T> = try {
    Result.Success(action())
} catch (e: Exception) {
    Result.Error(e)
}