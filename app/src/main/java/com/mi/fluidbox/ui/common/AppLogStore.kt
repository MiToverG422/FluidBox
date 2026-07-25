package com.mi.fluidbox.ui.common

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

enum class AppLogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

data class AppLogEntry(
    val id: Long,
    val timestampMs: Long,
    val level: AppLogLevel,
    val tag: String,
    val message: String
) {
    fun toLine(zoneId: ZoneId = ZoneId.systemDefault()): String {
        val instant = Instant.ofEpochMilli(timestampMs)
        val time = AppLogStore.timeFormatter.format(instant.atZone(zoneId))
        return "[$time] [${level.name}] [$tag] $message"
    }
}

object AppLogStore {
    private const val MAX_ENTRIES = 500
    private const val LOG_DIR_NAME = "logs"

    internal val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private val nextId = AtomicLong(1L)
    private val _entries = MutableStateFlow<List<AppLogEntry>>(emptyList())
    val entries: StateFlow<List<AppLogEntry>> = _entries.asStateFlow()

    fun d(tag: String, message: String) = append(AppLogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = append(AppLogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = append(AppLogLevel.WARN, tag, message)
    fun e(tag: String, message: String) = append(AppLogLevel.ERROR, tag, message)

    fun clear() {
        _entries.value = emptyList()
    }

    fun removeByTagPrefix(prefix: String) {
        _entries.update { list ->
            list.filterNot { entry -> entry.tag.startsWith(prefix) }
        }
    }

    fun exportText(zoneId: ZoneId = ZoneId.systemDefault()): String {
        val lines = _entries.value.map { it.toLine(zoneId) }
        return if (lines.isEmpty()) "No logs." else lines.joinToString(separator = "\n")
    }

    suspend fun saveToLocal(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val content = exportText()
            val fileName = "fluidbox_log_${System.currentTimeMillis()}.txt"
            val bytes = content.toByteArray(StandardCharsets.UTF_8)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/FluidBox")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("Cannot create log file in Downloads")
                resolver.openOutputStream(uri)?.use { out ->
                    out.write(bytes)
                    out.flush()
                } ?: error("Cannot open Downloads output stream")

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } else {
                val dir = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    LOG_DIR_NAME
                )
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                file.writeBytes(bytes)
                Uri.fromFile(file)
            }
        }
    }

    suspend fun createShareUri(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val content = exportText()
            val shareDir = File(context.cacheDir, "shared_logs")
            if (!shareDir.exists()) shareDir.mkdirs()
            val file = File(shareDir, "fluidbox_log_${System.currentTimeMillis()}.txt")
            file.writeText(content, StandardCharsets.UTF_8)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
    }

    private fun append(level: AppLogLevel, tag: String, message: String) {
        val entry = AppLogEntry(
            id = nextId.getAndIncrement(),
            timestampMs = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message
        )
        _entries.update { list ->
            val merged = list + entry
            if (merged.size <= MAX_ENTRIES) merged else merged.takeLast(MAX_ENTRIES)
        }
    }
}
