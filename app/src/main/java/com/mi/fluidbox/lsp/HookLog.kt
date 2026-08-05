package com.mi.fluidbox.lsp

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.highcapable.yukihookapi.hook.log.YLog
import com.mi.fluidbox.logging.HookLogContract
import de.robv.android.xposed.XposedBridge
import java.util.ArrayDeque

internal object HookLog {
    private const val MODULE_PACKAGE = "com.mi.fluidbox"
    private const val LOGCAT_TAG = "FluidBox-LSP"
    private const val FLUSH_DELAY_MS = 350L
    private const val MAX_PENDING_LINES = 120

    private val lock = Any()
    private val pendingLines = ArrayDeque<String>()
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    @Volatile
    private var flushScheduled = false

    @Volatile
    private var boundContext: Context? = null

    fun bindContext(context: Context?) {
        boundContext = context?.applicationContext
    }

    fun d(tag: String, message: String, throwable: Throwable? = null) {
        write("D", tag, message, throwable)
    }

    fun i(tag: String, message: String, throwable: Throwable? = null) {
        write("I", tag, message, throwable)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        write("W", tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        write("E", tag, message, throwable)
    }

    private fun write(level: String, tag: String, message: String, throwable: Throwable?) {
        val scopedTag = tag
            .removePrefix("FluidBox-")
            .removePrefix("FluidBox ")
            .trim()
            .ifBlank { "Hook" }
        enqueueAppLog(
            level = level,
            feature = scopedTag,
            event = "hook",
            message = if (throwable == null) message else "$message\n${throwable.stackTraceToString()}",
        )
        val line = "[$scopedTag] $message"
        val xposedLine = "$LOGCAT_TAG/$level $line${
            throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
        }"
        val yukiLogged = runCatching {
            when (level) {
                "D" -> YLog.debug(line, throwable, LOGCAT_TAG)
                "W" -> YLog.warn(line, throwable, LOGCAT_TAG)
                "E" -> YLog.error(line, throwable, LOGCAT_TAG)
                else -> YLog.info(line, throwable, LOGCAT_TAG)
            }
        }.isSuccess
        if (yukiLogged) return

        runCatching { XposedBridge.log(xposedLine) }
        when (level) {
            "D" -> Log.d(LOGCAT_TAG, line, throwable)
            "W" -> Log.w(LOGCAT_TAG, line, throwable)
            "E" -> Log.e(LOGCAT_TAG, line, throwable)
            else -> Log.i(LOGCAT_TAG, line, throwable)
        }
    }

    private fun enqueueAppLog(
        level: String,
        feature: String,
        event: String,
        message: String,
    ) {
        val line = HookLogContract.encode(
            timestampMs = System.currentTimeMillis(),
            priority = HookLogContract.priorityFromLetter(level),
            process = currentProcessName(),
            feature = feature,
            event = event,
            message = message,
        )
        var shouldSchedule = false
        synchronized(lock) {
            pendingLines.addLast(line)
            while (pendingLines.size > MAX_PENDING_LINES) pendingLines.removeFirst()
            if (!flushScheduled) {
                flushScheduled = true
                shouldSchedule = true
            }
        }
        if (shouldSchedule) {
            runCatching { handler.postDelayed(::flushAppLogs, FLUSH_DELAY_MS) }
                .onFailure { flushAppLogs() }
        }
    }

    private fun flushAppLogs() {
        val context = currentContext()
        if (context == null) {
            synchronized(lock) { flushScheduled = false }
            return
        }
        val lines = synchronized(lock) {
            flushScheduled = false
            if (pendingLines.isEmpty()) return
            buildList {
                while (pendingLines.isNotEmpty()) add(pendingLines.removeFirst())
            }
        }
        val intent = Intent(HookLogContract.ACTION_APPEND_HOOK_LOG)
            .setPackage(MODULE_PACKAGE)
            .setComponent(ComponentName(MODULE_PACKAGE, HookLogContract.RECEIVER_CLASS))
            .putStringArrayListExtra(HookLogContract.EXTRA_LINES, ArrayList(lines))
        runCatching { context.sendBroadcast(intent) }
    }

    private fun currentContext(): Context? {
        boundContext?.let { return it }
        return runCatching {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentApplication = activityThreadClass.getDeclaredMethod("currentApplication")
            (currentApplication.invoke(null) as? Context)?.applicationContext
        }.getOrNull()
    }

    private fun currentProcessName(): String {
        return runCatching { Application.getProcessName() }
            .getOrDefault("")
    }
}
