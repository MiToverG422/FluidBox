package com.mi.fluidbox.lsp

import android.content.Context
import com.mi.fluidbox.ui.common.AppLogStore
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.atomic.AtomicBoolean

object LsposedScopeRequester {
    private const val CACHE_PREFS = "lsposed_status_cache"
    private const val CACHE_KEY_MODULE_ENABLED = "module_enabled"
    private const val CACHE_KEY_HAS_SYSTEM = "has_system_scope"
    private const val CACHE_KEY_HAS_ANDROID = "has_android_scope"
    private const val CACHE_KEY_HAS_SYSTEMUI = "has_systemui_scope"
    private const val CACHE_KEY_HAS_SETTINGS = "has_settings_scope"
    private const val CACHE_KEY_HAS_LAUNCHER = "has_launcher_scope"
    private const val CACHE_KEY_HAS_AOD = "has_aod_scope"
    private const val CACHE_KEY_HAS_LOCALIZER = "has_localizer_scope"
    private const val CACHE_KEY_HAS_DOUBLE_POWER = "has_double_power_scope"
    private const val CACHE_KEY_FRAMEWORK_VERSION = "framework_version"

    private const val SCOPE_SYSTEM = "system"
    private const val SCOPE_ANDROID = "android"
    private const val SCOPE_SYSTEMUI = "com.android.systemui"
    private const val SCOPE_SETTINGS = "com.android.settings"
    private const val SCOPE_LAUNCHER = "com.android.launcher"
    private const val SCOPE_AOD = "com.oplus.aod"

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
        val hasDoublePowerScope: Boolean,
        val frameworkVersionText: String?
    ) {
        val hasRequiredScopes: Boolean
            get() = hasSystemScope &&
                hasAndroidScope &&
                hasSystemUiScope &&
                hasSettingsScope &&
                (!isRecentTaskRadiusEnabled() || hasLauncherScope) &&
                (!isLauncherRegionEnabled() || hasLauncherScope) &&
                (!isAodEnhanceEnabled() || hasAodScope) &&
                (!isLocalizerEnabled() || hasLocalizerScopes) &&
                (!isDoublePowerEnabled() || hasDoublePowerScope)
    }

    private val systemUiScopeAliases = setOf(
        SCOPE_SYSTEMUI,
        "systemui"
    )
    private val listenerRegistered = AtomicBoolean(false)

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var boundService: XposedService? = null

    @Volatile
    private var pendingRequest = false

    @Volatile
    private var pendingLocalizerScopeRemoval = false

    fun initialize(context: Context? = null) {
        context?.applicationContext?.let { appContext = it }
        ensureListenerRegistered()
    }

    fun requestRequiredScopes(): Boolean {
        ensureListenerRegistered()
        pendingLocalizerScopeRemoval = false
        val service = boundService
        if (service != null) {
            dispatchScopeRequest(service)
            return true
        }
        pendingRequest = true
        AppLogStore.i("LSPosed", "Queued direct scope request (service not connected yet)")
        return false
    }

    fun removeOosLocalizerScopes(context: Context? = null): Boolean {
        initialize(context)
        val service = boundService
        if (service != null) {
            dispatchLocalizerScopeRemoval(service)
            return true
        }
        pendingLocalizerScopeRemoval = true
        AppLogStore.i("LSPosed", "Queued OPlus localizer scope removal (service not connected yet)")
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
        val service = boundService
        val cached = readCachedStatus()

        if (service == null) {
            return cached?.toSnapshot(serviceConnected = false) ?: emptySnapshot()
        }

        return runCatching {
            val granted = service.getScope().toSet()
            val hasSystem = SCOPE_SYSTEM in granted
            val hasAndroid = SCOPE_ANDROID in granted
            val hasSystemUi = granted.any { systemUiScopeAliases.contains(it) }
            val hasSettings = SCOPE_SETTINGS in granted
            val hasLauncher = SCOPE_LAUNCHER in granted
            val hasAod = SCOPE_AOD in granted
            val hasLocalizer = hasLocalizerScopes(granted)
            val hasDoublePower = hasDoublePowerScope(granted)
            val moduleEnabled = readModuleEnabled(service) ?: true
            val frameworkVersionText = buildFrameworkVersionText(service)

            val snapshot = StatusSnapshot(
                serviceConnected = true,
                moduleEnabled = moduleEnabled,
                hasSystemScope = hasSystem,
                hasAndroidScope = hasAndroid,
                hasSystemUiScope = hasSystemUi,
                hasSettingsScope = hasSettings,
                hasLauncherScope = hasLauncher,
                hasAodScope = hasAod,
                hasLocalizerScopes = hasLocalizer,
                hasDoublePowerScope = hasDoublePower,
                frameworkVersionText = frameworkVersionText
            )
            cacheStatus(snapshot)
            snapshot
        }.getOrElse { throwable ->
            AppLogStore.w("LSPosed", "Read status snapshot failed: ${throwable.message.orEmpty()}")
            cached?.toSnapshot(serviceConnected = false) ?: emptySnapshot()
        }
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
            hasDoublePowerScope = false,
            frameworkVersionText = null
        )
    }

    private fun ensureListenerRegistered() {
        if (!listenerRegistered.compareAndSet(false, true)) return
        runCatching {
            XposedServiceHelper.registerListener(
                object : XposedServiceHelper.OnServiceListener {
                    override fun onServiceBind(service: XposedService) {
                        boundService = service
                        AppLogStore.i(
                            "LSPosed",
                            "Xposed service connected: ${service.getFrameworkName()} ${service.getFrameworkVersion()}"
                        )
                        if (pendingRequest) {
                            pendingRequest = false
                            dispatchScopeRequest(service)
                        }
                        if (pendingLocalizerScopeRemoval && !isLocalizerEnabled()) {
                            pendingLocalizerScopeRemoval = false
                            dispatchLocalizerScopeRemoval(service)
                        }
                    }

                    override fun onServiceDied(service: XposedService) {
                        if (boundService === service) {
                            boundService = null
                        }
                        AppLogStore.w("LSPosed", "Xposed service disconnected")
                    }
                }
            )
        }.onFailure { throwable ->
            listenerRegistered.set(false)
            AppLogStore.w("LSPosed", "Register service listener failed: ${throwable.message.orEmpty()}")
        }
    }

    private fun dispatchScopeRequest(service: XposedService) {
        val requestList = getScopesToRequest(service)
        if (requestList.isEmpty()) {
            AppLogStore.i("LSPosed", "Scope already granted, no request needed")
            return
        }
        runCatching {
            service.requestScope(
                requestList,
                object : XposedService.OnScopeEventListener {
                    override fun onScopeRequestApproved(approved: List<String>) {
                        AppLogStore.i("LSPosed", "Scope request approved: ${approved.joinToString()}")
                    }

                    override fun onScopeRequestFailed(message: String) {
                        AppLogStore.w("LSPosed", "Scope request failed: $message")
                    }
                }
            )
            AppLogStore.i("LSPosed", "Requested scope directly: ${requestList.joinToString()}")
        }.onFailure { throwable ->
            AppLogStore.w("LSPosed", "Direct scope request failed: ${throwable.message.orEmpty()}")
        }
    }

    private fun dispatchLocalizerScopeRemoval(service: XposedService) {
        val currentScopes = runCatching { service.getScope().toSet() }.getOrDefault(emptySet())
        val removeList = localizerScopes()
            .filter { it in currentScopes }
            .distinct()
        if (removeList.isEmpty()) {
            AppLogStore.i("LSPosed", "OPlus localizer scope already removed")
            return
        }
        runCatching {
            service.removeScope(removeList)
            AppLogStore.i("LSPosed", "Removed OPlus localizer scopes: ${removeList.joinToString()}")
            snapshot()
        }.onFailure { throwable ->
            AppLogStore.w("LSPosed", "Remove OPlus localizer scopes failed: ${throwable.message.orEmpty()}")
        }
    }

    private fun getScopesToRequest(service: XposedService): List<String> {
        val currentScopes = runCatching { service.getScope().toSet() }.getOrDefault(emptySet())
        val requestScopes = mutableListOf<String>()
        val hasSystemUiScope = currentScopes.any { systemUiScopeAliases.contains(it) }
        if (SCOPE_SYSTEM !in currentScopes) {
            requestScopes += SCOPE_SYSTEM
        }
        if (SCOPE_ANDROID !in currentScopes) {
            requestScopes += SCOPE_ANDROID
        }
        if (!hasSystemUiScope) {
            requestScopes += SCOPE_SYSTEMUI
        }
        if (SCOPE_SETTINGS !in currentScopes) {
            requestScopes += SCOPE_SETTINGS
        }
        if (isRecentTaskRadiusEnabled() || isLauncherRegionEnabled()) {
            requestScopes += installedPackageScopes(SCOPE_LAUNCHER)
                .filterNot { it in currentScopes }
        }
        if (isAodEnhanceEnabled()) {
            requestScopes += installedPackageScopes(SCOPE_AOD)
                .filterNot { it in currentScopes }
        }
        if (isLocalizerEnabled()) {
            requestScopes += localizerScopes()
                .filterNot { it in currentScopes }
        }
        if (isDoublePowerEnabled()) {
            requestScopes += installedDoublePowerScopes()
                .filterNot { it in currentScopes }
        }
        return requestScopes
            .distinct()
    }

    private fun hasLocalizerScopes(currentScopes: Set<String>): Boolean {
        if (!isLocalizerEnabled()) return true
        val requiredScopes = localizerScopes()
        return requiredScopes.all { it in currentScopes }
    }

    private fun hasDoublePowerScope(currentScopes: Set<String>): Boolean {
        if (!isDoublePowerEnabled()) return true
        val installedScopes = installedDoublePowerScopes()
        return installedScopes.isEmpty() || installedScopes.all { it in currentScopes }
    }

    private fun isLocalizerEnabled(): Boolean {
        val context = appContext ?: return false
        return LspConfig.isOosLocalizerEnabled(context)
    }

    private fun isDoublePowerEnabled(): Boolean {
        val context = appContext ?: return false
        return LspConfig.isDoublePowerCustomEnabled(context)
    }

    private fun isRecentTaskRadiusEnabled(): Boolean {
        val context = appContext ?: return false
        return LspConfig.isRecentTaskRadiusEnabled(context)
    }

    private fun isLauncherRegionEnabled(): Boolean {
        val context = appContext ?: return false
        return LspConfig.getLauncherRegionMode(context) != LspConfig.LAUNCHER_REGION_MODE_OFF
    }

    private fun isAodEnhanceEnabled(): Boolean {
        val context = appContext ?: return false
        return LspConfig.isAodEnhanceEnabled(context)
    }

    private fun localizerScopes(): Set<String> {
        return OosLocalizerHooker.supportedPackageNames
    }

    private fun installedDoublePowerScopes(): Set<String> {
        return installedPackageScopes(DoublePowerHooker.TARGET_PACKAGE)
    }

    private fun installedPackageScopes(vararg packageNames: String): Set<String> {
        val context = appContext ?: return emptySet()
        val packageManager = context.packageManager
        return packageNames
            .filter { packageName ->
                runCatching {
                    packageManager.getPackageInfo(packageName, 0)
                }.isSuccess
            }
            .toSet()
    }

    private fun readModuleEnabled(service: XposedService): Boolean? {
        val methodNames = listOf("isModuleEnabled", "isEnabled", "isActivated")
        val methods = service.javaClass.methods
        methodNames.forEach { name ->
            val method = methods.firstOrNull {
                it.name == name &&
                    it.parameterCount == 0 &&
                    (it.returnType == java.lang.Boolean.TYPE || it.returnType == java.lang.Boolean::class.java)
            } ?: return@forEach

            val value = runCatching {
                method.isAccessible = true
                method.invoke(service)
            }.getOrNull() as? Boolean
            if (value != null) return value
        }
        return null
    }

    private fun buildFrameworkVersionText(service: XposedService): String? {
        val frameworkName = runCatching { service.getFrameworkName() }
            .getOrNull()
            ?.trim()
            ?.ifBlank { null }
            ?: "LSPosed"

        val versionName = runCatching { service.getFrameworkVersion() }
            .getOrNull()
            ?.trim()
            ?.ifBlank { null }
            ?: readOptionalString(service, "getFrameworkVersionName")
            ?: readOptionalString(service, "getFrameworkVersionString")

        val versionCode = runCatching { service.getFrameworkVersionCode() }
            .getOrNull()
            ?.takeIf { it > 0L }
            ?.toString()

        val apiVersion = runCatching { service.getApiVersion() }
            .getOrNull()
            ?.takeIf { it > 0 }
            ?.toString()

        val frameworkText = when {
            !versionName.isNullOrBlank() && !versionCode.isNullOrBlank() ->
                "$frameworkName $versionName ($versionCode)"
            !versionName.isNullOrBlank() ->
                "$frameworkName $versionName"
            !versionCode.isNullOrBlank() ->
                "$frameworkName ($versionCode)"
            else -> frameworkName
        }

        return if (!apiVersion.isNullOrBlank()) {
            "$frameworkText / API $apiVersion"
        } else {
            frameworkText
        }
    }

    private fun readOptionalString(target: Any, methodName: String): String? {
        val method = target.javaClass.methods.firstOrNull {
            it.name == methodName &&
                it.parameterCount == 0 &&
                it.returnType == String::class.java
        } ?: return null

        return runCatching {
            method.isAccessible = true
            method.invoke(target) as? String
        }.getOrNull()?.trim().orEmpty().ifBlank { null }
    }

    private data class CachedStatus(
        val moduleEnabled: Boolean,
        val hasSystemScope: Boolean,
        val hasAndroidScope: Boolean,
        val hasSystemUiScope: Boolean,
        val hasSettingsScope: Boolean,
        val hasLauncherScope: Boolean,
        val hasAodScope: Boolean,
        val hasLocalizerScopes: Boolean,
        val hasDoublePowerScope: Boolean,
        val frameworkVersionText: String?
    ) {
        fun toSnapshot(serviceConnected: Boolean): StatusSnapshot {
            return StatusSnapshot(
                serviceConnected = serviceConnected,
                moduleEnabled = moduleEnabled,
                hasSystemScope = hasSystemScope,
                hasAndroidScope = hasAndroidScope,
                hasSystemUiScope = hasSystemUiScope,
                hasSettingsScope = hasSettingsScope,
                hasLauncherScope = hasLauncherScope,
                hasAodScope = hasAodScope,
                hasLocalizerScopes = hasLocalizerScopes,
                hasDoublePowerScope = hasDoublePowerScope,
                frameworkVersionText = frameworkVersionText
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
                .putBoolean(CACHE_KEY_HAS_DOUBLE_POWER, snapshot.hasDoublePowerScope)
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
                prefs.contains(CACHE_KEY_HAS_DOUBLE_POWER) ||
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
                    hasDoublePowerScope = prefs.getBoolean(CACHE_KEY_HAS_DOUBLE_POWER, false),
                    frameworkVersionText = prefs.getString(CACHE_KEY_FRAMEWORK_VERSION, null)
                )
            }
        }.getOrNull()
    }
}
