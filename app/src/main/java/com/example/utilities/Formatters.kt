package com.example.utilities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    private val fileTimestampFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    fun formatTimestamp(millis: Long): String {
        return dateFormatter.format(Date(millis))
    }

    fun formatFileTimestamp(millis: Long = System.currentTimeMillis()): String {
        return fileTimestampFormatter.format(Date(millis))
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 KB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.2f MB", mb)
            else -> String.format(Locale.US, "%.0f KB", kb.coerceAtLeast(1.0))
        }
    }

    fun slugify(input: String, maxLen: Int = 24): String {
        val cleaned = input.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(maxLen)
            .trim('_')
        return cleaned.ifBlank { "creation" }
    }
}
