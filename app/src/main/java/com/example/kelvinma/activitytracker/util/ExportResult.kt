package com.example.kelvinma.activitytracker.util

import android.content.Intent

data class ExportResult(
    val intent: Intent,
    val fileSizeBytes: Long,
    val fileName: String
) {
    fun getFormattedFileSize(): String {
        return when {
            fileSizeBytes < 1024 -> "${fileSizeBytes} B"
            fileSizeBytes < 1024 * 1024 -> "${fileSizeBytes / 1024} KB"
            fileSizeBytes < 1024 * 1024 * 1024 -> "${"%.1f".format(fileSizeBytes / (1024.0 * 1024.0))} MB"
            else -> "${"%.1f".format(fileSizeBytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }
}