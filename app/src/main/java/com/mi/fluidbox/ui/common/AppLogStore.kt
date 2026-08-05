package com.mi.fluidbox.ui.common

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.mi.fluidbox.logging.HookLogContract
import com.mi.fluidbox.logging.HookLogRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
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
    private const val MAX_PERSISTED_BYTES = 512 * 1024L
    private const val LOG_DIR_NAME = "logs"
    private const val RUNTIME_LOG_FILE = "fluidbox-runtime.log"

    private val lock = Any()
    @Volatile
    private var appContext: Context? = null
    @Volatile
    private var initialized = false

    internal val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private val nextId = AtomicLong(1L)
    private val _entries = MutableStateFlow<List<AppLogEntry>>(emptyList())
    val entries: StateFlow<List<AppLogEntry>> = _entries.asStateFlow()

    fun initialize(context: Context) {
        val applicationContext = context.applicationContext
        synchronized(lock) {
            appContext = applicationContext
            if (initialized) return
            initialized = true

            val loaded = loadPersistentEntries(applicationContext)
            if (loaded.isNotEmpty()) {
                _entries.value = loaded.takeLast(MAX_ENTRIES)
                nextId.set((loaded.maxOfOrNull { it.id } ?: 0L) + 1L)
            }
        }
    }

    fun d(tag: String, message: String) = append(AppLogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = append(AppLogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = append(AppLogLevel.WARN, tag, message)
    fun e(tag: String, message: String) = append(AppLogLevel.ERROR, tag, message)

    fun clear() {
        _entries.value = emptyList()
        appContext?.let { context ->
            runCatching { runtimeLogFile(context).delete() }
        }
    }

    fun clearHookLogs() {
        _entries.update { list ->
            list.filterNot(AppLogEntry::isHookLog)
        }
        rewritePersistentFile()
    }

    fun removeByTagPrefix(prefix: String) {
        _entries.update { list ->
            list.filterNot { entry -> entry.tag.startsWith(prefix) }
        }
        rewritePersistentFile()
    }

    suspend fun reload(context: Context): Int = withContext(Dispatchers.IO) {
        val applicationContext = context.applicationContext
        synchronized(lock) {
            appContext = applicationContext
            initialized = true
            val loaded = loadPersistentEntries(applicationContext)
            _entries.value = loaded.takeLast(MAX_ENTRIES)
            nextId.set((loaded.maxOfOrNull { it.id } ?: 0L) + 1L)
            loaded.size
        }
    }

    fun appendHookRawLines(context: Context, rawLines: List<String>): Int {
        initialize(context)
        var count = 0
        rawLines
            .asSequence()
            .mapNotNull(HookLogContract::decode)
            .forEach { record ->
                appendHookRecord(record)
                count += 1
            }
        return count
    }

    fun exportText(zoneId: ZoneId = ZoneId.systemDefault()): String {
        return exportEntriesText(_entries.value, zoneId)
    }

    fun exportHookText(zoneId: ZoneId = ZoneId.systemDefault()): String {
        return exportEntriesText(_entries.value.filter(AppLogEntry::isHookLog), zoneId)
    }

    fun hookEntriesSnapshot(): List<AppLogEntry> {
        return _entries.value.filter(AppLogEntry::isHookLog)
    }

    private fun exportEntriesText(entries: List<AppLogEntry>, zoneId: ZoneId): String {
        val lines = entries.map { it.toLine(zoneId) }
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

    suspend fun saveHookToLocal(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val content = exportHookText()
            val fileName = "fluidbox_hook_log_${System.currentTimeMillis()}.txt"
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

    suspend fun createHookShareUri(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val content = exportHookText()
            val shareDir = File(context.cacheDir, "shared_logs")
            if (!shareDir.exists()) shareDir.mkdirs()
            val file = File(shareDir, "fluidbox_hook_log_${System.currentTimeMillis()}.txt")
            file.writeText(content, StandardCharsets.UTF_8)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
    }

    private fun append(
        level: AppLogLevel,
        tag: String,
        message: String,
        mirrorToLogcat: Boolean = true,
        timestampMs: Long = System.currentTimeMillis(),
    ) {
        val entry = AppLogEntry(
            id = nextId.getAndIncrement(),
            timestampMs = timestampMs,
            level = level,
            tag = tag,
            message = sanitizeMessage(message)
        )
        _entries.update { list ->
            val merged = list + entry
            if (merged.size <= MAX_ENTRIES) merged else merged.takeLast(MAX_ENTRIES)
        }
        persistEntry(entry)
        if (mirrorToLogcat) {
            writeAndroidLog(entry)
        }
    }

    private fun appendHookRecord(record: HookLogRecord) {
        val feature = record.feature.ifBlank { "Hook" }
        val event = record.event.ifBlank { "hook" }
        val message = buildString {
            append("[$feature] ")
            append(event)
            if (record.message.isNotBlank()) {
                append('\n')
                append(record.message)
            }
            if (record.process.isNotBlank()) {
                append('\n')
                append(record.process)
            }
        }
        append(
            level = hookLevelFromPriority(record.priority),
            tag = "Hook/$feature",
            message = message,
            mirrorToLogcat = false,
            timestampMs = record.timestampMs,
        )
    }

    private fun hookLevelFromPriority(priority: Int): AppLogLevel {
        return when (priority) {
            2, 3 -> AppLogLevel.DEBUG
            5 -> AppLogLevel.WARN
            6, 7 -> AppLogLevel.ERROR
            else -> AppLogLevel.INFO
        }
    }

    private fun writeAndroidLog(entry: AppLogEntry) {
        val line = "[${entry.tag}] ${entry.message}"
        when (entry.level) {
            AppLogLevel.DEBUG -> Log.d("FluidBox", line)
            AppLogLevel.INFO -> Log.i("FluidBox", line)
            AppLogLevel.WARN -> Log.w("FluidBox", line)
            AppLogLevel.ERROR -> Log.e("FluidBox", line)
        }
    }

    private fun sanitizeMessage(message: String): String {
        return message
            .replace("\r", "")
            .replace("\n", "\\n")
            .trim()
            .ifBlank { "(empty)" }
    }

    private fun persistEntry(entry: AppLogEntry) {
        val context = appContext ?: return
        runCatching {
            val file = runtimeLogFile(context)
            file.parentFile?.mkdirs()
            file.appendText(entry.toLine() + "\n", StandardCharsets.UTF_8)
            if (file.length() > MAX_PERSISTED_BYTES) {
                trimPersistentFile(file)
            }
        }
    }

    private fun rewritePersistentFile() {
        val context = appContext ?: return
        runCatching {
            val file = runtimeLogFile(context)
            file.parentFile?.mkdirs()
            file.writeText(exportText(), StandardCharsets.UTF_8)
            file.appendText("\n", StandardCharsets.UTF_8)
        }
    }

    private fun trimPersistentFile(file: File) {
        val lines = file.readLines(StandardCharsets.UTF_8).takeLast(MAX_ENTRIES)
        file.writeText(lines.joinToString(separator = "\n"), StandardCharsets.UTF_8)
        file.appendText("\n", StandardCharsets.UTF_8)
    }

    private fun runtimeLogFile(context: Context): File {
        return File(File(context.filesDir, LOG_DIR_NAME), RUNTIME_LOG_FILE)
    }

    private fun loadPersistentEntries(context: Context): List<AppLogEntry> {
        val file = runtimeLogFile(context)
        if (!file.exists()) return emptyList()
        return runCatching {
            file.readLines(StandardCharsets.UTF_8)
                .takeLast(MAX_ENTRIES)
                .mapIndexedNotNull { index, line -> parseLine(line, index + 1L) }
        }.getOrDefault(emptyList())
    }

    private fun parseLine(line: String, fallbackId: Long): AppLogEntry? {
        val match = logLineRegex.matchEntire(line.trim()) ?: return null
        val timestamp = runCatching {
            LocalDateTime
                .parse(match.groupValues[1], timeFormatter)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrDefault(System.currentTimeMillis())
        val level = runCatching { AppLogLevel.valueOf(match.groupValues[2]) }
            .getOrDefault(AppLogLevel.INFO)
        return AppLogEntry(
            id = fallbackId,
            timestampMs = timestamp,
            level = level,
            tag = match.groupValues[3],
            message = match.groupValues[4]
        )
    }

    private val logLineRegex =
        Regex("""^\[(.+)] \[(DEBUG|INFO|WARN|ERROR)] \[(.+)] (.*)$""")
}

fun AppLogEntry.isHookLog(): Boolean = tag.startsWith("Hook/")
