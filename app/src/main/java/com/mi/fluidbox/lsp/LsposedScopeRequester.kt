package com.mi.fluidbox.lsp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Process
import android.os.SystemClock
import com.mi.fluidbox.ui.common.AppLogStore
import com.mi.fluidbox.ui.common.ShellLogger
import java.io.File
import java.util.Locale

object LsposedScopeRequester {
    private const val LSPOSED_API_VERSION = 102
    private const val CACHE_PREFS = "lsposed_status_cache"
    private const val CACHE_KEY_MODULE_ENABLED = "module_enabled"
    private const val CACHE_KEY_HAS_SYSTEM = "has_system_scope"
    private const val CACHE_KEY_HAS_ANDROID = "has_android_scope"
    private const val CACHE_KEY_HAS_SYSTEMUI = "has_systemui_scope"
    private const val CACHE_KEY_HAS_SETTINGS = "has_settings_scope"
    private const val CACHE_KEY_HAS_LAUNCHER = "has_launcher_scope"
    private const val CACHE_KEY_HAS_AOD = "has_aod_scope"
    private const val CACHE_KEY_HAS_LOCALIZER = "has_localizer_scope"
    private const val CACHE_KEY_FRAMEWORK_VERSION = "framework_version"
    private const val DB_MODULE_ENABLED_CACHE_MS = 2_000L

    private val LSPOSED_CONFIG_DB_PATHS = listOf(
        "/data/adb/lspd/config/modules_config.db",
        "/data/adb/lspd/modules_config.db"
    )
    private val LSPOSED_MANAGER_PACKAGES = listOf(
        "org.lsposed.manager",
        "io.github.libxposed.manager"
    )

    private val packageColumnCandidates = listOf(
        "module_pkg_name",
        "modulePackageName",
        "package_name",
        "packageName",
        "pkg_name",
        "pkg",
        "name",
        "module"
    )

    private val enabledColumnCandidates = listOf(
        "enabled",
        "enable",
        "is_enabled",
        "isEnabled"
    )

    data class StatusSnapshot(
        val serviceConnected: Boolean,
        val moduleEnabled: Boolean,
        val hasSystemScope: Boolean,
        val hasAndroidScope: Boolean,
        val hasSystemUiScope: Boolean,
        val hasSettingsScope: Boolean,
        val hasLauncherScope: Boolean,
        val hasAodScope: Boolean,
        val hasLocalizerScopes: Boolean,
        val frameworkVersionText: String?
    ) {
        val hasRequiredScopes: Boolean
            get() = hasSystemScope && hasSystemUiScope
    }

    @Volatile
    private var appContext: Context? = null
    private val dbReadLock = Any()
    @Volatile
    private var cachedDbModuleEnabled: CachedDbModuleEnabled? = null

    fun initialize(context: Context? = null) {
        context?.applicationContext?.let { appContext = it }
    }

    fun requestRequiredScopes(): Boolean {
        AppLogStore.i("LSPosed", "Static scope mode; opening LSPosed manager instead of dynamic scope request")
        return false
    }

    fun removeOosLocalizerScopes(context: Context? = null): Boolean {
        initialize(context)
        AppLogStore.i("LSPosed", "Static scope mode; dynamic scope removal is disabled")
        return false
    }

    fun hasRequiredScopes(context: Context? = null): Boolean {
        return snapshot(context).hasRequiredScopes
    }

    fun cachedSnapshot(context: Context? = null): StatusSnapshot {
        context?.applicationContext?.let { appContext = it }
        return readCachedStatus()?.toSnapshot(serviceConnected = false) ?: emptySnapshot()
    }

    fun snapshot(context: Context? = null): StatusSnapshot {
        initialize(context)
        val cached = readCachedStatus()
        val dbModuleEnabled = readModuleEnabledFromLsposedDb()
        val runtimeSystemScopeActive = LspRuntimeStatus.isSystemScopeActive()
        val runtimeSystemUiScopeActive = LspRuntimeStatus.isSystemUiScopeActive()

        val moduleEnabled = when (dbModuleEnabled) {
            false -> false
            true -> true
            null -> runtimeSystemScopeActive || runtimeSystemUiScopeActive || cached?.moduleEnabled == true
        }

        val frameworkVersionText = readLsposedManagerVersionText()
            ?: cached?.frameworkVersionText?.takeIf { it.contains(" / API ") }
            ?: "LSPosed"
        val snapshot = StatusSnapshot(
            serviceConnected = false,
            moduleEnabled = moduleEnabled,
            hasSystemScope = moduleEnabled,
            hasAndroidScope = moduleEnabled,
            hasSystemUiScope = moduleEnabled,
            hasSettingsScope = moduleEnabled,
            hasLauncherScope = moduleEnabled,
            hasAodScope = moduleEnabled,
            hasLocalizerScopes = moduleEnabled,
            frameworkVersionText = frameworkVersionText
        )
        cacheStatus(snapshot)
        return snapshot
    }

    private fun emptySnapshot(): StatusSnapshot {
        return StatusSnapshot(
            serviceConnected = false,
            moduleEnabled = false,
            hasSystemScope = false,
            hasAndroidScope = false,
            hasSystemUiScope = false,
            hasSettingsScope = false,
            hasLauncherScope = false,
            hasAodScope = false,
            hasLocalizerScopes = false,
            frameworkVersionText = null
        )
    }

    private fun readModuleEnabledFromLsposedDb(): Boolean? {
        val now = SystemClock.elapsedRealtime()
        cachedDbModuleEnabled
            ?.takeIf { now - it.timestampMs < DB_MODULE_ENABLED_CACHE_MS }
            ?.let { return it.value }

        return synchronized(dbReadLock) {
            val lockedNow = SystemClock.elapsedRealtime()
            cachedDbModuleEnabled
                ?.takeIf { lockedNow - it.timestampMs < DB_MODULE_ENABLED_CACHE_MS }
                ?.let { return@synchronized it.value }

            val value = readModuleEnabledFromLsposedDbUncached()
            cachedDbModuleEnabled = CachedDbModuleEnabled(
                timestampMs = SystemClock.elapsedRealtime(),
                value = value
            )
            value
        }
    }

    private fun readModuleEnabledFromLsposedDbUncached(): Boolean? {
        val context = appContext ?: return null
        val packageName = context.packageName.takeIf { it.isNotBlank() } ?: return null
        val dbCopy = File(context.cacheDir, "lsposed_modules_config.db")
        LSPOSED_CONFIG_DB_PATHS.forEach { sourcePath ->
            val enabled = runCatching {
                copyLsposedDb(sourcePath, dbCopy)
                readModuleEnabledFromDbCopy(dbCopy, packageName)
            }.onFailure { throwable ->
                AppLogStore.w(
                    "LSPosed",
                    "Read module enabled from $sourcePath failed: ${throwable.message.orEmpty()}"
                )
            }.getOrNull()
            deleteLsposedDbCopy(dbCopy)
            if (enabled != null) return enabled
        }
        return null
    }

    private fun readLsposedManagerVersionText(): String? {
        val context = appContext ?: return null
        return LSPOSED_MANAGER_PACKAGES.firstNotNullOfOrNull { packageName ->
            runCatching {
                val info = context.packageManager.getPackageInfo(packageName, 0)
                val versionName = info.versionName
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: return@runCatching null
                val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    info.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    info.versionCode.toLong()
                }.takeIf { it > 0L }

                if (versionCode != null) {
                    "LSPosed $versionName ($versionCode) / API $LSPOSED_API_VERSION"
                } else {
                    "LSPosed $versionName / API $LSPOSED_API_VERSION"
                }
            }.getOrNull()
        }
    }

    private fun copyLsposedDb(sourcePath: String, target: File) {
        val targetPath = target.absolutePath
        val uid = Process.myUid()
        val copyCommands = mutableListOf(
            "rm -f ${shellQuote(targetPath)} ${shellQuote("$targetPath-wal")} ${shellQuote("$targetPath-shm")} ${shellQuote("$targetPath-journal")}",
            "cp -f ${shellQuote(sourcePath)} ${shellQuote(targetPath)}",
            "[ ! -f ${shellQuote("$sourcePath-wal")} ] || cp -f ${shellQuote("$sourcePath-wal")} ${shellQuote("$targetPath-wal")}",
            "[ ! -f ${shellQuote("$sourcePath-shm")} ] || cp -f ${shellQuote("$sourcePath-shm")} ${shellQuote("$targetPath-shm")}",
            "[ ! -f ${shellQuote("$sourcePath-journal")} ] || cp -f ${shellQuote("$sourcePath-journal")} ${shellQuote("$targetPath-journal")}",
            "chown $uid:$uid ${shellQuote(targetPath)}",
            "chmod 600 ${shellQuote(targetPath)}"
        )
        copyCommands += "if [ -f ${shellQuote("$targetPath-wal")} ]; then chown $uid:$uid ${shellQuote("$targetPath-wal")}; chmod 600 ${shellQuote("$targetPath-wal")}; fi"
        copyCommands += "if [ -f ${shellQuote("$targetPath-shm")} ]; then chown $uid:$uid ${shellQuote("$targetPath-shm")}; chmod 600 ${shellQuote("$targetPath-shm")}; fi"
        copyCommands += "if [ -f ${shellQuote("$targetPath-journal")} ]; then chown $uid:$uid ${shellQuote("$targetPath-journal")}; chmod 600 ${shellQuote("$targetPath-journal")}; fi"
        val command = copyCommands.joinToString("; ")

        val directResult = ShellLogger.exec("LSPosed copy db direct", command)
        if (directResult.isSuccess && target.exists()) return

        val suResult = ShellLogger.exec("LSPosed copy db su", "su -c ${shellQuote(command)}")
        if (!suResult.isSuccess || !target.exists()) {
            error("Unable to copy LSPosed config database from $sourcePath")
        }
    }

    private fun deleteLsposedDbCopy(dbCopy: File) {
        runCatching { dbCopy.delete() }
        runCatching { File("${dbCopy.absolutePath}-wal").delete() }
        runCatching { File("${dbCopy.absolutePath}-shm").delete() }
        runCatching { File("${dbCopy.absolutePath}-journal").delete() }
    }

    private fun readModuleEnabledFromDbCopy(dbFile: File, packageName: String): Boolean? {
        if (!dbFile.exists() || dbFile.length() <= 0L) return null
        return runCatching {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            ).use { db ->
                readModuleEnabledFromKnownSchema(db, packageName)
                    ?: readModuleEnabledFromDiscoveredSchema(db, packageName)
            }
        }.onFailure { throwable ->
            AppLogStore.w("LSPosed", "Read LSPosed database failed: ${throwable.message.orEmpty()}")
        }.getOrNull()
    }

    private fun readModuleEnabledFromKnownSchema(db: SQLiteDatabase, packageName: String): Boolean? {
        return runCatching {
            db.rawQuery(
                "SELECT enabled FROM modules WHERE module_pkg_name = ? LIMIT 1",
                arrayOf(packageName)
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    parseEnabledValue(cursor.getString(0))
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    private fun readModuleEnabledFromDiscoveredSchema(db: SQLiteDatabase, packageName: String): Boolean? {
        val tables = readUserTables(db)
        for (table in tables) {
            val columns = readColumns(db, table)
            val packageColumn = findCandidateColumn(columns, packageColumnCandidates) ?: continue
            val enabledColumn = findCandidateColumn(columns, enabledColumnCandidates) ?: continue
            val enabled = runCatching {
                db.rawQuery(
                    "SELECT ${sqlIdent(enabledColumn)} FROM ${sqlIdent(table)} WHERE ${sqlIdent(packageColumn)} = ? LIMIT 1",
                    arrayOf(packageName)
                ).use { cursor ->
                    if (cursor.moveToFirst()) parseEnabledValue(cursor.getString(0)) else null
                }
            }.getOrNull()
            if (enabled != null) return enabled
        }
        return null
    }

    private fun readUserTables(db: SQLiteDatabase): List<String> {
        return db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'",
            emptyArray()
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    cursor.getString(0)?.takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }
    }

    private fun readColumns(db: SQLiteDatabase, table: String): List<String> {
        return db.rawQuery("PRAGMA table_info(${sqlIdent(table)})", emptyArray()).use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            buildList {
                while (cursor.moveToNext()) {
                    cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }
    }

    private fun findCandidateColumn(columns: List<String>, candidates: List<String>): String? {
        val byLowerName = columns.associateBy { it.lowercase(Locale.ROOT) }
        return candidates.firstNotNullOfOrNull { candidate ->
            byLowerName[candidate.lowercase(Locale.ROOT)]
        }
    }

    private fun parseEnabledValue(raw: String?): Boolean? {
        val value = raw?.trim()?.lowercase(Locale.ROOT) ?: return null
        return when (value) {
            "1", "true", "t", "yes", "y", "on", "enabled" -> true
            "0", "false", "f", "no", "n", "off", "disabled" -> false
            else -> value.toIntOrNull()?.let { it != 0 }
        }
    }

    private fun sqlIdent(value: String): String {
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    private fun shellQuote(value: String): String {
        return "'" + value.replace("'", "'\"'\"'") + "'"
    }

    private data class CachedDbModuleEnabled(
        val timestampMs: Long,
        val value: Boolean?
    )

    private data class CachedStatus(
        val moduleEnabled: Boolean,
        val hasSystemScope: Boolean,
        val hasAndroidScope: Boolean,
        val hasSystemUiScope: Boolean,
        val hasSettingsScope: Boolean,
        val hasLauncherScope: Boolean,
        val hasAodScope: Boolean,
        val hasLocalizerScopes: Boolean,
        val frameworkVersionText: String?
    ) {
        fun toSnapshot(serviceConnected: Boolean): StatusSnapshot {
            val normalizedHasScopes = moduleEnabled
            return StatusSnapshot(
                serviceConnected = serviceConnected,
                moduleEnabled = moduleEnabled,
                hasSystemScope = normalizedHasScopes,
                hasAndroidScope = normalizedHasScopes,
                hasSystemUiScope = normalizedHasScopes,
                hasSettingsScope = normalizedHasScopes,
                hasLauncherScope = normalizedHasScopes,
                hasAodScope = normalizedHasScopes,
                hasLocalizerScopes = normalizedHasScopes,
                frameworkVersionText = frameworkVersionText?.takeIf { it.contains(" / API ") }
            )
        }
    }

    private fun cacheStatus(snapshot: StatusSnapshot) {
        val context = appContext ?: return
        runCatching {
            val prefs = context
                .createDeviceProtectedStorageContext()
                .getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(CACHE_KEY_MODULE_ENABLED, snapshot.moduleEnabled)
                .putBoolean(CACHE_KEY_HAS_SYSTEM, snapshot.hasSystemScope)
                .putBoolean(CACHE_KEY_HAS_ANDROID, snapshot.hasAndroidScope)
                .putBoolean(CACHE_KEY_HAS_SYSTEMUI, snapshot.hasSystemUiScope)
                .putBoolean(CACHE_KEY_HAS_SETTINGS, snapshot.hasSettingsScope)
                .putBoolean(CACHE_KEY_HAS_LAUNCHER, snapshot.hasLauncherScope)
                .putBoolean(CACHE_KEY_HAS_AOD, snapshot.hasAodScope)
                .putBoolean(CACHE_KEY_HAS_LOCALIZER, snapshot.hasLocalizerScopes)
                .putString(CACHE_KEY_FRAMEWORK_VERSION, snapshot.frameworkVersionText)
                .apply()
        }
    }

    private fun readCachedStatus(): CachedStatus? {
        val context = appContext ?: return null
        return runCatching {
            val prefs = context
                .createDeviceProtectedStorageContext()
                .getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
            val hasAny = prefs.contains(CACHE_KEY_MODULE_ENABLED) ||
                prefs.contains(CACHE_KEY_HAS_SYSTEM) ||
                prefs.contains(CACHE_KEY_HAS_ANDROID) ||
                prefs.contains(CACHE_KEY_HAS_SYSTEMUI) ||
                prefs.contains(CACHE_KEY_HAS_SETTINGS) ||
                prefs.contains(CACHE_KEY_HAS_LAUNCHER) ||
                prefs.contains(CACHE_KEY_HAS_AOD) ||
                prefs.contains(CACHE_KEY_HAS_LOCALIZER) ||
                prefs.contains(CACHE_KEY_FRAMEWORK_VERSION)
            if (!hasAny) {
                null
            } else {
                CachedStatus(
                    moduleEnabled = prefs.getBoolean(CACHE_KEY_MODULE_ENABLED, false),
                    hasSystemScope = prefs.getBoolean(CACHE_KEY_HAS_SYSTEM, false),
                    hasAndroidScope = prefs.getBoolean(CACHE_KEY_HAS_ANDROID, false),
                    hasSystemUiScope = prefs.getBoolean(CACHE_KEY_HAS_SYSTEMUI, false),
                    hasSettingsScope = prefs.getBoolean(CACHE_KEY_HAS_SETTINGS, false),
                    hasLauncherScope = prefs.getBoolean(CACHE_KEY_HAS_LAUNCHER, false),
                    hasAodScope = prefs.getBoolean(CACHE_KEY_HAS_AOD, false),
                    hasLocalizerScopes = prefs.getBoolean(CACHE_KEY_HAS_LOCALIZER, false),
                    frameworkVersionText = prefs.getString(CACHE_KEY_FRAMEWORK_VERSION, null)
                )
            }
        }.getOrNull()
    }
}
