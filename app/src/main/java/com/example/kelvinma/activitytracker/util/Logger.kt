package com.example.kelvinma.activitytracker.util

import android.util.Log

/**
 * Centralized logging utility for the Activity Tracker app.
 * Provides consistent logging with appropriate tags and levels.
 */
object Logger {
    private const val TAG_PREFIX = "ActivityTracker"
    
    // Component-specific tags
    const val TAG_DATABASE = "${TAG_PREFIX}_DB"
    const val TAG_REPOSITORY = "${TAG_PREFIX}_Repo"
    const val TAG_TIMER = "${TAG_PREFIX}_Timer"
    const val TAG_ANALYTICS = "${TAG_PREFIX}_Analytics"
    const val TAG_AUDIO = "${TAG_PREFIX}_Audio"
    const val TAG_NAVIGATION = "${TAG_PREFIX}_Nav"
    const val TAG_YAML = "${TAG_PREFIX}_YAML"
    const val TAG_MIGRATION = "${TAG_PREFIX}_Migration"
    
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        Log.d(tag, message, throwable)
    }
    
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        Log.i(tag, message, throwable)
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
    
    // Convenience methods for common operations
    fun logDatabaseOperation(operation: String, success: Boolean, throwable: Throwable? = null) {
        if (success) {
            d(TAG_DATABASE, "Database operation successful: $operation")
        } else {
            e(TAG_DATABASE, "Database operation failed: $operation", throwable)
        }
    }
    
    fun logTimerEvent(event: String, details: String = "") {
        i(TAG_TIMER, "Timer event: $event $details".trim())
    }
    
    fun logAudioEvent(event: String, resourceId: Int? = null, throwable: Throwable? = null) {
        val message = if (resourceId != null) {
            "Audio event: $event (resource: $resourceId)"
        } else {
            "Audio event: $event"
        }
        
        if (throwable != null) {
            e(TAG_AUDIO, message, throwable)
        } else {
            d(TAG_AUDIO, message)
        }
    }
    
    fun logYamlParsing(fileName: String, success: Boolean, throwable: Throwable? = null) {
        if (success) {
            d(TAG_YAML, "Successfully parsed YAML file: $fileName")
        } else {
            e(TAG_YAML, "Failed to parse YAML file: $fileName", throwable)
        }
    }
    
    fun logNavigation(destination: String, success: Boolean, throwable: Throwable? = null) {
        if (success) {
            d(TAG_NAVIGATION, "Navigation successful to: $destination")
        } else {
            e(TAG_NAVIGATION, "Navigation failed to: $destination", throwable)
        }
    }
}