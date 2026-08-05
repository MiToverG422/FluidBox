package com.mi.fluidbox.logging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mi.fluidbox.ui.common.AppLogStore
import java.util.ArrayDeque
import java.util.concurrent.Executors

class HookLogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != HookLogContract.ACTION_APPEND_HOOK_LOG) return
        val lines = intent.readHookLines()
        if (lines.isEmpty()) return
        val pendingResult = goAsync()
        enqueue(context.applicationContext, lines, pendingResult)
    }

    private fun Intent.readHookLines(): List<String> {
        @Suppress("DEPRECATION")
        val lines = getStringArrayListExtra(HookLogContract.EXTRA_LINES).orEmpty()
        if (lines.isNotEmpty()) return lines
        return getStringExtra(HookLogContract.EXTRA_LINE)
            ?.let(::listOf)
            .orEmpty()
    }

    private companion object {
        private const val MAX_RAW_QUEUE = 480
        private const val MAX_PENDING_QUEUE = 16

        private val executor = Executors.newSingleThreadExecutor()
        private val lock = Any()
        private val rawQueue = ArrayDeque<String>()
        private val pendingQueue = ArrayDeque<BroadcastReceiver.PendingResult>()
        private var running = false

        private fun enqueue(
            context: Context,
            lines: List<String>,
            pendingResult: BroadcastReceiver.PendingResult,
        ) {
            var shouldStart = false
            synchronized(lock) {
                lines
                    .asSequence()
                    .map { line -> line.take(HookLogContract.MAX_LINE_LENGTH) }
                    .filter { line -> line.isNotBlank() }
                    .forEach { line ->
                        rawQueue.addLast(line)
                        while (rawQueue.size > MAX_RAW_QUEUE) rawQueue.removeFirst()
                    }
                pendingQueue.addLast(pendingResult)
                while (pendingQueue.size > MAX_PENDING_QUEUE) {
                    runCatching { pendingQueue.removeFirst().finish() }
                }
                if (!running) {
                    running = true
                    shouldStart = true
                }
            }
            if (shouldStart) {
                executor.execute { drain(context) }
            }
        }

        private fun drain(context: Context) {
            while (true) {
                val lines: List<String>
                val pendingResults: List<BroadcastReceiver.PendingResult>
                synchronized(lock) {
                    if (rawQueue.isEmpty()) {
                        running = false
                        return
                    }
                    lines = buildList {
                        while (rawQueue.isNotEmpty()) add(rawQueue.removeFirst())
                    }
                    pendingResults = buildList {
                        while (pendingQueue.isNotEmpty()) add(pendingQueue.removeFirst())
                    }
                }

                runCatching {
                    AppLogStore.appendHookRawLines(context, lines)
                }
                pendingResults.forEach { pending ->
                    runCatching { pending.finish() }
                }
            }
        }
    }
}
