package com.mi.mibox.ui.common

import android.content.Context
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private const val ROOT_STATUS_CACHE_PREFS = "root_status_cache"
private const val ROOT_STATUS_CACHE_STATE = "state"
private const val ROOT_STATUS_CACHE_UID = "uid"
private const val ROOT_STATUS_CACHE_MANAGER_VERSION = "manager_version"
private const val ROOT_STATUS_CACHE_DETAIL = "detail"

enum class RootAccessState {
    Checking,
    Granted,
    NotGranted,
    Error
}

data class RootAccessInfo(
    val state: RootAccessState,
    val uid: String? = null,
    val managerVersion: String? = null,
    val detail: String? = null
)

suspend fun queryRootAccess(context: Context? = null): RootAccessInfo = withContext(Dispatchers.IO) {
    AppLogStore.i("RootAccess", "Start checking root access")
    val info = runCatching {
        refreshCachedShellIfNeeded()
        val result = runFreshSu("id -u")
        if (!result.isSuccess) {
            closeCachedShell("root check failed")
            AppLogStore.w(
                "RootAccess",
                "Root check failed: ${result.err.firstOrNull().orEmpty().ifBlank { "unknown" }}"
            )
            RootAccessInfo(
                state = RootAccessState.NotGranted,
                detail = result.err.firstOrNull()
            )
        } else {
            val uid = result.out.firstOrNull()?.trim().orEmpty()
            if (uid == "0") {
                AppLogStore.i("RootAccess", "Root granted (uid=0)")
                RootAccessInfo(
                    state = RootAccessState.Granted,
                    uid = uid,
                    managerVersion = detectRootManagerVersion()
                )
            } else {
                closeCachedShell("root uid is not 0")
                AppLogStore.w("RootAccess", "Root denied (uid=$uid)")
                RootAccessInfo(
                    state = RootAccessState.NotGranted,
                    uid = uid
                )
            }
        }
    }.getOrElse { throwable ->
        AppLogStore.e("RootAccess", "Root check exception: ${throwable.message.orEmpty()}")
        RootAccessInfo(
            state = RootAccessState.Error,
            detail = throwable.message
        )
    }

    context?.applicationContext?.let { cacheRootAccessInfo(it, info) }
    info
}

fun readCachedRootAccessInfo(context: Context): RootAccessInfo? {
    return runCatching {
        val prefs = context
            .applicationContext
            .getSharedPreferences(ROOT_STATUS_CACHE_PREFS, Context.MODE_PRIVATE)
        val stateName = prefs.getString(ROOT_STATUS_CACHE_STATE, null) ?: return@runCatching null
        val state = RootAccessState.entries.firstOrNull { it.name == stateName } ?: return@runCatching null
        RootAccessInfo(
            state = state,
            uid = prefs.getString(ROOT_STATUS_CACHE_UID, null),
            managerVersion = prefs.getString(ROOT_STATUS_CACHE_MANAGER_VERSION, null),
            detail = prefs.getString(ROOT_STATUS_CACHE_DETAIL, null)
        )
    }.getOrNull()
}

private fun cacheRootAccessInfo(context: Context, info: RootAccessInfo) {
    runCatching {
        context
            .getSharedPreferences(ROOT_STATUS_CACHE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(ROOT_STATUS_CACHE_STATE, info.state.name)
            .putString(ROOT_STATUS_CACHE_UID, info.uid)
            .putString(ROOT_STATUS_CACHE_MANAGER_VERSION, info.managerVersion)
            .putString(ROOT_STATUS_CACHE_DETAIL, info.detail)
            .apply()
    }.onFailure { throwable ->
        AppLogStore.w("RootAccess", "Cache root status failed: ${throwable.message.orEmpty()}")
    }
}

enum class AssistantScreenOption {
    Shelf,
    Disabled,
    Default
}

data class AssistantScreenApplyResult(
    val success: Boolean,
    val detail: String? = null
)

data class SecureSettingApplyResult(
    val success: Boolean,
    val detail: String? = null
)

private data class FreshSuResult(
    val isSuccess: Boolean,
    val out: List<String> = emptyList(),
    val err: List<String> = emptyList()
)

private fun runFreshSu(command: String, timeoutSeconds: Long = 15): FreshSuResult {
    return runCatching {
        val process = ProcessBuilder("su", "-c", command).start()
        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            return FreshSuResult(
                isSuccess = false,
                err = listOf("su timed out")
            )
        }
        val stdout = process.inputStream.bufferedReader().readText().lines().filter { it.isNotBlank() }
        val stderr = process.errorStream.bufferedReader().readText().lines().filter { it.isNotBlank() }
        FreshSuResult(
            isSuccess = process.exitValue() == 0,
            out = stdout,
            err = stderr
        )
    }.getOrElse { throwable ->
        FreshSuResult(
            isSuccess = false,
            err = listOfNotNull(throwable.message)
        )
    }
}

private fun refreshCachedShellIfNeeded() {
    runCatching {
        val cachedShell = Shell.getCachedShell() ?: return
        if (!cachedShell.isRoot) {
            AppLogStore.i("RootAccess", "Close cached non-root shell before root check")
            cachedShell.close()
        }
    }.onFailure { throwable ->
        AppLogStore.w("RootAccess", "Refresh cached shell failed: ${throwable.message.orEmpty()}")
    }
}

private fun closeCachedShell(reason: String) {
    runCatching {
        val cachedShell = Shell.getCachedShell() ?: return
        AppLogStore.i("RootAccess", "Close cached shell: $reason")
        cachedShell.close()
    }.onFailure { throwable ->
        AppLogStore.w("RootAccess", "Close cached shell failed: ${throwable.message.orEmpty()}")
    }
}

private fun detectRootManagerVersion(): String? {
    fun firstLineOf(command: String): String? {
        val result = Shell.cmd(command).exec()
        return (result.out.firstOrNull { it.isNotBlank() }
            ?: result.err.firstOrNull { it.isNotBlank() })
            ?.trim()
            ?.takeIf { result.isSuccess && it.isNotBlank() }
    }

    val magiskVersionName = firstLineOf("magisk -v")
    val magiskVersionCode = firstLineOf("magisk -V")
    if (!magiskVersionName.isNullOrBlank()) {
        return if (magiskVersionCode.isNullOrBlank()) {
            magiskVersionName
        } else {
            "$magiskVersionName ($magiskVersionCode)"
        }
    }

    val suVersionName = firstLineOf("su -v")
    val suVersionCode = firstLineOf("su -V")
    if (!suVersionName.isNullOrBlank()) {
        return if (suVersionCode.isNullOrBlank()) {
            suVersionName
        } else {
            "$suVersionName ($suVersionCode)"
        }
    }

    return null
}

suspend fun applyAssistantScreenOption(option: AssistantScreenOption): AssistantScreenApplyResult =
    withContext(Dispatchers.IO) {
        AppLogStore.i("DesktopAssistant", "Apply option: $option")
        val (assistantType, leftEnable) = when (option) {
            AssistantScreenOption.Shelf -> 1 to 1
            AssistantScreenOption.Disabled -> 0 to 0
            AssistantScreenOption.Default -> 2 to 1
        }

        runCatching {
            val writeResult = Shell.cmd(
                "settings put secure assistant_screen_type $assistantType",
                "settings put secure assistant_screen_type_left_enable $leftEnable"
            ).exec()

            if (!writeResult.isSuccess) {
                val reason = writeResult.err.firstOrNull { it.isNotBlank() }
                    ?: writeResult.out.firstOrNull { it.isNotBlank() }
                    ?: "settings command failed"
                AppLogStore.e("DesktopAssistant", "Apply failed: $reason")
                return@runCatching AssistantScreenApplyResult(
                    success = false,
                    detail = reason
                )
            }

            // Refresh launcher to apply changes. Ignore force-stop failure to keep the main action successful.
            Shell.cmd("am force-stop com.android.launcher").exec()
            AppLogStore.i("DesktopAssistant", "Apply succeeded")

            AssistantScreenApplyResult(success = true)
        }.getOrElse { throwable ->
            AppLogStore.e("DesktopAssistant", "Apply exception: ${throwable.message.orEmpty()}")
            AssistantScreenApplyResult(
                success = false,
                detail = throwable.message
            )
        }
    }

suspend fun queryAssistantScreenOption(): AssistantScreenOption =
    withContext(Dispatchers.IO) {
        runCatching {
            val typeResult = Shell.cmd("settings get secure assistant_screen_type").exec()
            val leftResult = Shell.cmd("settings get secure assistant_screen_type_left_enable").exec()

            val rawType = typeResult.out.firstOrNull()?.trim().orEmpty()
            val rawLeft = leftResult.out.firstOrNull()?.trim().orEmpty()

            val type = rawType.toIntOrNull()
            val left = rawLeft.toIntOrNull()

            // Mapping follows applyAssistantScreenOption():
            // Shelf -> (1,1), Disabled -> (0,0), Default -> (2,1)
            when {
                left == 0 || type == 0 -> AssistantScreenOption.Disabled
                type == 1 && left == 1 -> AssistantScreenOption.Shelf
                type == 2 && left == 1 -> AssistantScreenOption.Default
                type == 1 -> AssistantScreenOption.Shelf
                else -> AssistantScreenOption.Default
            }
        }.getOrElse {
            AssistantScreenOption.Default
        }
    }

suspend fun queryPermissionMonitorVisibility(): Boolean =
    withContext(Dispatchers.IO) {
        runCatching {
            val result = Shell.cmd("settings get secure system_opt_enable").exec()
            result.out.firstOrNull()?.trim() == "1"
        }.getOrElse { throwable ->
            AppLogStore.w("PermissionMonitor", "Read failed: ${throwable.message.orEmpty()}")
            false
        }
    }

suspend fun applyPermissionMonitorVisibility(enabled: Boolean): SecureSettingApplyResult =
    withContext(Dispatchers.IO) {
        val command = if (enabled) {
            "settings put secure system_opt_enable 1"
        } else {
            "settings delete secure system_opt_enable"
        }
        AppLogStore.i("PermissionMonitor", "Apply visibility: $enabled")

        runCatching {
            val result = Shell.cmd(command).exec()
            if (result.isSuccess) {
                SecureSettingApplyResult(success = true)
            } else {
                val reason = result.err.firstOrNull { it.isNotBlank() }
                    ?: result.out.firstOrNull { it.isNotBlank() }
                    ?: "settings command failed"
                AppLogStore.e("PermissionMonitor", "Apply failed: $reason")
                SecureSettingApplyResult(success = false, detail = reason)
            }
        }.getOrElse { throwable ->
            AppLogStore.e("PermissionMonitor", "Apply exception: ${throwable.message.orEmpty()}")
            SecureSettingApplyResult(success = false, detail = throwable.message)
        }
    }

suspend fun queryLauncherLayoutUnlocked(): Boolean =
    withContext(Dispatchers.IO) {
        runCatching {
            val result = Shell.cmd("settings get global useOldLayout").exec()
            result.out.firstOrNull()?.trim() == "1"
        }.getOrElse { throwable ->
            AppLogStore.w("LauncherLayout", "Read failed: ${throwable.message.orEmpty()}")
            false
        }
    }

suspend fun applyLauncherLayoutUnlocked(enabled: Boolean): SecureSettingApplyResult =
    withContext(Dispatchers.IO) {
        val command = if (enabled) {
            "settings put global useOldLayout 1"
        } else {
            "settings put global useOldLayout 3"
        }
        AppLogStore.i("LauncherLayout", "Apply unlocked layout: $enabled")

        runCatching {
            val result = Shell.cmd(command).exec()
            if (result.isSuccess) {
                SecureSettingApplyResult(success = true)
            } else {
                val reason = result.err.firstOrNull { it.isNotBlank() }
                    ?: result.out.firstOrNull { it.isNotBlank() }
                    ?: "settings command failed"
                AppLogStore.e("LauncherLayout", "Apply failed: $reason")
                SecureSettingApplyResult(success = false, detail = reason)
            }
        }.getOrElse { throwable ->
            AppLogStore.e("LauncherLayout", "Apply exception: ${throwable.message.orEmpty()}")
            SecureSettingApplyResult(success = false, detail = throwable.message)
        }
    }
