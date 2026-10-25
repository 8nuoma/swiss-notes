package com.swissnotes.app.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages media attachments. Copies images and videos selected from the
 * gallery into the app's private storage so they remain tied to the note
 * and survive across sessions.
 */
object AttachmentManager {

    private val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US)

    /**
     * Copies a media [uri] into the app storage and returns a stable
     * `file://` URI string that can be embedded in Markdown as
     * `![alt](file://...)`.
     */
    suspend fun copyToStorage(context: Context, uri: Uri, kind: Kind): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, "attachments")
                if (!dir.exists()) dir.mkdirs()
                val ext = when (kind) {
                    Kind.Image -> ".jpg"
                    Kind.Video -> ".mp4"
                }
                val file = File(dir, "${timestamp.format(Date())}$ext")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                file.toURI().toString()
            }.getOrNull()
        }

    enum class Kind { Image, Video }
}
