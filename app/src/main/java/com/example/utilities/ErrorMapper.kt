package com.example.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.OpenableColumns
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {
    const val MAX_UPLOAD_BYTES = 20L * 1024L * 1024L // 20 MB

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun validatePickedImageUri(context: Context, uri: Uri): String? {
        val mime = context.contentResolver.getType(uri).orEmpty().lowercase()
        if (mime.isNotBlank() && !mime.startsWith("image/")) {
            return "Unsupported file format ($mime). Please select a PNG, JPEG, or WebP image."
        }
        val sizeBytes = queryFileSize(context, uri)
        if (sizeBytes > MAX_UPLOAD_BYTES) {
            return "Selected image exceeds the 20 MB studio upload limit (${Formatters.formatFileSize(sizeBytes)})."
        }
        return null
    }

    fun queryFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getLong(idx) else -1L
            } ?: -1L
        } catch (_: Exception) {
            -1L
        }
    }

    fun toUserMessage(error: Throwable): String {
        return when (error) {
            is SocketTimeoutException -> "Request timed out. Please retry in a moment."
            is UnknownHostException -> "Network connection unavailable. Check your internet connection and retry."
            is IOException -> error.localizedMessage ?: "Storage or network I/O error occurred."
            is SecurityException -> "Permission denied while accessing the selected file."
            is IllegalArgumentException -> error.message ?: "Invalid input provided."
            else -> error.localizedMessage ?: "An unexpected studio error occurred. Please retry."
        }
    }
}
