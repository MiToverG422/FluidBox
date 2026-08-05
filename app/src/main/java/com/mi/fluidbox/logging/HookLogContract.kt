package com.mi.fluidbox.logging

object HookLogContract {
    const val ACTION_APPEND_HOOK_LOG = "com.mi.fluidbox.action.APPEND_HOOK_LOG"
    const val EXTRA_LINES = "hook_log_lines"
    const val EXTRA_LINE = "hook_log_line"
    const val RECEIVER_CLASS = "com.mi.fluidbox.logging.HookLogReceiver"
    const val MAX_LINE_LENGTH = 8192

    fun encode(
        timestampMs: Long,
        priority: Int,
        process: String,
        feature: String,
        event: String,
        message: String,
    ): String {
        return listOf(
            timestampMs.toString(),
            priority.toString(),
            escape(process),
            escape(feature),
            escape(event),
            escape(message),
        ).joinToString(separator = "\t")
            .take(MAX_LINE_LENGTH)
    }

    fun decode(line: String): HookLogRecord? {
        val parts = line.split('\t', limit = 6)
        if (parts.size < 6) return null
        return HookLogRecord(
            timestampMs = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
            priority = parts[1].toIntOrNull() ?: 4,
            process = unescape(parts[2]),
            feature = unescape(parts[3]),
            event = unescape(parts[4]),
            message = unescape(parts[5]),
        )
    }

    fun priorityFromLetter(level: String): Int {
        return when (level) {
            "D" -> 3
            "W" -> 5
            "E" -> 6
            else -> 4
        }
    }

    private fun escape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\t", "\\t")
            .replace("\r", "")
            .replace("\n", "\\n")
    }

    private fun unescape(value: String): String {
        val builder = StringBuilder(value.length)
        var escaped = false
        value.forEach { char ->
            if (escaped) {
                builder.append(
                    when (char) {
                        't' -> '\t'
                        'n' -> '\n'
                        else -> char
                    }
                )
                escaped = false
            } else if (char == '\\') {
                escaped = true
            } else {
                builder.append(char)
            }
        }
        if (escaped) builder.append('\\')
        return builder.toString()
    }
}

data class HookLogRecord(
    val timestampMs: Long,
    val priority: Int,
    val process: String,
    val feature: String,
    val event: String,
    val message: String,
)
