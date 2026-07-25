package com.mi.mibox.lsp

import android.app.Notification
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method

object FrameworkHooker {
    private const val TAG = "MiBox-LSP"
    private const val EXTREME_RATE_ID = 7
    private const val FORCED_DISPLAY_MODE_ID = 5
    private const val CLS_REFRESH_RATE_CONFIGS = "com.android.server.wm.OplusRefreshRateConfigs"
    private const val CLS_REFRESH_RATE_CORE = "com.android.server.wm.OplusRefreshRateCore"
    private const val CLS_REFRESH_RATE_CORE_VOTE = "com.android.server.wm.OplusRefreshRateCore\$Vote"
    private const val CLS_REFRESH_RATE_POLICY_IMPL = "com.android.server.wm.OplusRefreshRatePolicyImpl"
    private const val CLS_REFRESH_RATE_POLICY_PICK_DATA = "com.android.server.wm.OplusRefreshRatePolicyImpl\$PickRefreshRateData"
    private const val CLS_REFRESH_RATE_SETTINGS_OBSERVER = "com.android.server.wm.OplusRefreshRatePolicyImpl\$SettingsObserver"
    private const val CLS_ANDROID_DISPLAY_INFO = "android.view.DisplayInfo"
    private const val CLS_ANDROID_POINT = "android.graphics.Point"
    private const val CLS_BUBBLE_EXTRACTOR = "com.android.server.notification.BubbleExtractor"
    private const val CLS_NOTIFICATION_RECORD = "com.android.server.notification.NotificationRecord"
    private const val CLS_PREFERENCES_HELPER = "com.android.server.notification.PreferencesHelper"
    private const val CLS_NOTIFICATION_CHANNEL = "android.app.NotificationChannel"

    private const val M_IS_EXTREME_HIGH_ENABLE = "isExtremeHighEnable"
    private const val M_IS_EXTREME_HIGH_SETTING_ON = "isExtremeHighSettingOn"
    private const val M_GET_FINAL_DISPLAY_MODE_ID_LOCKED = "getFinalDisplayModeIdLocked"
    private const val M_IS_KEYGUARD_SHOWN = "isKeyguardShown"
    private const val M_UPDATE_VOTE_LOCKED = "updateVoteLocked"
    private const val M_GET_PREFERRED_REFRESH_RATE_DATA = "getPreferredRefreshRateData"
    private const val M_GET_OR_CREATE_PREFERRED_REFRESH_RATE_DATA = "getOrCreatePreferredRefreshRateData"
    private const val M_SET_APP_REQ_FIRST = "setAppReqFirst"
    private const val M_ENABLE_WIN_OVERRIDE = "enableWinOverride"
    private const val M_SET_DISABLE_VIEW_OVERRIDE = "setDisableViewOverride"
    private const val M_SET_LOW_FEQ_MODE = "setLowFeqMode"
    private const val M_SET_BRIGHTNESS_BLOCK = "setBrightnessBlock"
    private const val M_SET_APP_LOW_REFRESH_RATE = "setAppLowRefreshRate"
    private const val M_PUT_USR_OVERRIDE_REFRESH_RATE_ID = "putUsrOverrideRefreshRateId"
    private const val M_FOR_REFRESH_RATE = "forRefreshRate"
    private const val M_GET_DIMENSION_PIXEL_SIZE = "getDimensionPixelSize"
    private const val M_GET_KEY_AOD_ALL_DAY_SUPPORT_SETTINGS = "getKeyAodAllDaySupportSettings"
    private const val M_PROCESS = "process"
    private const val M_CAN_PRESENT_AS_BUBBLE = "canPresentAsBubble"
    private const val M_SET_ALLOW_BUBBLE = "setAllowBubble"
    private const val M_CAN_BUBBLE = "canBubble"
    private const val M_IS_CONVERSATION = "isConversation"
    private const val M_BUBBLES_ENABLED = "bubblesEnabled"
    private const val M_GET_BUBBLE_PREFERENCE = "getBubblePreference"
    private const val M_GET_ALLOW_BUBBLES = "getAllowBubbles"

    private const val PACKAGE_SYSTEM = "system"
    private const val PACKAGE_ANDROID = "android"
    private const val PACKAGE_LAUNCHER = "com.android.launcher"
    private const val PACKAGE_OPLUS_AOD = "com.oplus.aod"

    private val recentTaskDimenNames = listOf(
        "recent_task_view_radius",
        "task_view_radius_20",
        "task_view_radius_22"
    )
    @Volatile
    private var cachedRecentTaskDimenIds: Set<Int> = emptySet()

    private val helperClassNames = listOf(
        "com.android.server.notification.OplusNotificationFixHelper",
        "com.android.server.notification.OplusNotificationManagerExtImpl",
        "com.android.server.notification.NotificationManagerServiceExtImpl",
        "com.android.server.notification.OplusNotificationManagerServiceExtImpl"
    )

    private val installedHookKeys = HashSet<String>()
    private val isInternalPreferredDataRewrite = ThreadLocal.withInitial { false }

    @Volatile
    private var lastLoggedToggle: Boolean? = null

    fun hook(packageName: String, classLoader: ClassLoader?): Boolean {
        var totalHooks = 0
        when (packageName) {
            PACKAGE_ANDROID,
            PACKAGE_SYSTEM -> {
                LspRuntimeStatus.markSystemScopeActive()
                totalHooks += hookNativeNotifyIcon(classLoader = classLoader, packageName = packageName)
                totalHooks += hookNativeNotificationBubbles(classLoader = classLoader, packageName = packageName)
                totalHooks += hookExtremeRefresh165FromJadx(classLoader = classLoader, packageName = packageName)
            }

            PACKAGE_LAUNCHER -> {
                totalHooks += hookRecentTaskViewRadius(
                    classLoader = classLoader,
                    packageName = packageName
                )
            }

            PACKAGE_OPLUS_AOD -> {
                totalHooks += hookAodSettingsSupport(
                    classLoader = classLoader,
                    packageName = packageName
                )
            }
        }
        return totalHooks > 0
    }

    private fun isFrameworkPackage(packageName: String): Boolean {
        return packageName == "android" || packageName == "system"
    }

    private fun hookNativeNotifyIcon(classLoader: ClassLoader?, packageName: String): Int {
        var hookedMethods = 0
        helperClassNames.forEach { className ->
            val hookClass = findClassAnyLoader(className, classLoader) ?: return@forEach
            val targetMethods = hookClass.declaredMethods.filter { method ->
                method.name.equals("fixSmallIcon", ignoreCase = true) &&
                    method.parameterTypes.size == 4 &&
                    method.parameterTypes[0] == Notification::class.java
            }
            targetMethods.forEach { method ->
                if (hookFixSmallIcon(method)) {
                    hookedMethods++
                }
            }
            if (targetMethods.isNotEmpty()) {
                log("Framework native-icon hook: $className (${targetMethods.size}) in $packageName")
            }
        }
        return hookedMethods
    }

    private fun hookRecentTaskViewRadius(classLoader: ClassLoader?, packageName: String): Int {
        val hookKey = "recent_task_radius|resources|getDimensionPixelSize"
        if (!addHookKeyIfAbsent(hookKey)) return 1

        val resourcesClass = findClassAnyLoader("android.content.res.Resources", classLoader)
        if (resourcesClass == null) {
            removeHookKey(hookKey)
            log("Recent task radius hook miss: Resources class not found in $packageName")
            return 0
        }

        val targetMethods = resourcesClass.declaredMethods.filter { method ->
            method.name == M_GET_DIMENSION_PIXEL_SIZE &&
                method.parameterTypes.size == 1 &&
                method.parameterTypes[0] == Integer.TYPE &&
                method.returnType == Integer.TYPE
        }
        if (targetMethods.isEmpty()) {
            removeHookKey(hookKey)
            log("Recent task radius hook miss: Resources#getDimensionPixelSize not found in $packageName")
            return 0
        }

        targetMethods.forEach { method ->
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!LspConfig.isRecentTaskRadiusEnabledXposed()) return

                    val requestId = param.args.firstOrNull() as? Int ?: return
                    val resources = param.thisObject as? Resources ?: return
                    val matchedIds = resolveRecentTaskDimenIds(resources)
                    if (matchedIds.isEmpty() || requestId !in matchedIds) return
                    val recentTaskRadiusDp = LspConfig.getRecentTaskRadiusDpXposed()

                    val modifiedPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        recentTaskRadiusDp,
                        resources.displayMetrics
                    ).toInt()
                    param.result = modifiedPx
                }
            })
        }
        log("Recent task radius hooks installed in $packageName: methods=${targetMethods.size}")
        return targetMethods.size
    }

    private fun hookAodSettingsSupport(classLoader: ClassLoader?, packageName: String): Int {
        val installed = if (
            tryHookFromJadx(
                className = "com.oplus.aod.util.SettingsUtils",
                classLoader = classLoader,
                methodName = M_GET_KEY_AOD_ALL_DAY_SUPPORT_SETTINGS,
                parameters = arrayOf(
                    Context::class.java,
                    Integer.TYPE
                ),
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!LspConfig.isAodEnhanceEnabledXposed()) return
                        if (!LspConfig.isAodSettingsSwitchEnabledXposed()) return
                        param.result = 1
                    }
                }
            )
        ) {
            1
        } else {
            0
        }
        if (installed > 0) {
            log("AOD settings support hook installed in $packageName")
        }
        return installed
    }

    private fun hookNativeNotificationBubbles(classLoader: ClassLoader?, packageName: String): Int {
        var installed = 0

        if (
            tryHookFromJadx(
                className = CLS_PREFERENCES_HELPER,
                classLoader = classLoader,
                methodName = M_BUBBLES_ENABLED,
                parameters = arrayOf("android.os.UserHandle"),
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!isNativeNotificationBubblesEnabledForHook()) return
                        param.result = true
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_PREFERENCES_HELPER,
                classLoader = classLoader,
                methodName = M_GET_BUBBLE_PREFERENCE,
                parameters = arrayOf(String::class.java, Integer.TYPE),
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!isNativeNotificationBubblesEnabledForHook()) return
                        if ((param.result as? Int ?: 0) == 0) {
                            param.result = 1
                        }
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_BUBBLE_EXTRACTOR,
                classLoader = classLoader,
                methodName = M_CAN_PRESENT_AS_BUBBLE,
                parameters = arrayOf(CLS_NOTIFICATION_RECORD),
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val record = param.args.firstOrNull()
                        if (!shouldForceBubbleRecord(record)) return
                        param.result = true
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_BUBBLE_EXTRACTOR,
                classLoader = classLoader,
                methodName = M_PROCESS,
                parameters = arrayOf(CLS_NOTIFICATION_RECORD),
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        forceBubbleRecordIfEligible(param.args.firstOrNull())
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_NOTIFICATION_RECORD,
                classLoader = classLoader,
                methodName = M_SET_ALLOW_BUBBLE,
                parameters = arrayOf(java.lang.Boolean.TYPE),
                callback = object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!shouldForceBubbleRecord(param.thisObject)) return
                        param.args[0] = true
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_NOTIFICATION_RECORD,
                classLoader = classLoader,
                methodName = M_CAN_BUBBLE,
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!shouldForceBubbleRecord(param.thisObject)) return
                        param.result = true
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_NOTIFICATION_RECORD,
                classLoader = classLoader,
                methodName = M_IS_CONVERSATION,
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!shouldForceBubbleRecord(param.thisObject)) return
                        param.result = true
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_NOTIFICATION_CHANNEL,
                classLoader = classLoader,
                methodName = M_CAN_BUBBLE,
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!isNativeNotificationBubblesEnabledForHook()) return
                        param.result = true
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_NOTIFICATION_CHANNEL,
                classLoader = classLoader,
                methodName = M_GET_ALLOW_BUBBLES,
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!isNativeNotificationBubblesEnabledForHook()) return
                        if ((param.result as? Int ?: 0) == 0) {
                            param.result = 1
                        }
                    }
                }
            )
        ) installed++

        if (installed > 0) {
            log("Framework native notification bubbles hooks installed in $packageName: methods=$installed")
        } else {
            log("Framework native notification bubbles hooks not matched in $packageName")
        }
        return installed
    }

    private fun hookExtremeRefresh165FromJadx(classLoader: ClassLoader?, packageName: String): Int {
        var installed = 0

        if (
            tryHookFromJadx(
                className = CLS_REFRESH_RATE_CONFIGS,
                classLoader = classLoader,
                methodName = M_IS_EXTREME_HIGH_ENABLE,
                callback = object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!isExtremeRefreshEnabledForHook()) return
                        param.result = true
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_REFRESH_RATE_SETTINGS_OBSERVER,
                classLoader = classLoader,
                methodName = M_IS_EXTREME_HIGH_SETTING_ON,
                callback = object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!isExtremeRefreshEnabledForHook()) return
                        param.result = true
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_REFRESH_RATE_POLICY_IMPL,
                classLoader = classLoader,
                methodName = M_GET_FINAL_DISPLAY_MODE_ID_LOCKED,
                parameters = arrayOf(
                    CLS_ANDROID_DISPLAY_INFO,
                    CLS_ANDROID_POINT
                ),
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!isExtremeRefreshEnabledForHook()) return
                        param.result = FORCED_DISPLAY_MODE_ID
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_REFRESH_RATE_POLICY_PICK_DATA,
                classLoader = classLoader,
                methodName = M_IS_KEYGUARD_SHOWN,
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!isExtremeRefreshEnabledForHook()) return
                        param.result = false
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_REFRESH_RATE_CORE,
                classLoader = classLoader,
                methodName = M_UPDATE_VOTE_LOCKED,
                parameters = arrayOf(
                    Integer.TYPE,
                    CLS_REFRESH_RATE_CORE_VOTE
                ),
                callback = object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!isExtremeRefreshEnabledForHook()) return
                        if (param.args.size < 2) return

                        runCatching {
                            val effectiveLoader = param.thisObject?.javaClass?.classLoader ?: classLoader
                            val voteClass = findClassAnyLoader(CLS_REFRESH_RATE_CORE_VOTE, effectiveLoader) ?: return
                            val vote = XposedHelpers.callStaticMethod(
                                voteClass,
                                M_FOR_REFRESH_RATE,
                                EXTREME_RATE_ID
                            )
                            param.args[1] = vote
                        }.onFailure { error ->
                            log("Framework 165Hz vote rewrite failed: ${error.javaClass.simpleName}")
                        }
                    }
                }
            )
        ) installed++

        if (
            tryHookFromJadx(
                className = CLS_REFRESH_RATE_CONFIGS,
                classLoader = classLoader,
                methodName = M_GET_PREFERRED_REFRESH_RATE_DATA,
                parameters = arrayOf(
                    String::class.java,
                    String::class.java
                ),
                callback = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!isExtremeRefreshEnabledForHook()) return
                        if (isInternalPreferredDataRewrite.get() == true) return

                        isInternalPreferredDataRewrite.set(true)
                        try {
                            val thisObj = param.thisObject ?: return
                            val key = param.args.firstOrNull() as? String ?: return
                            val data = XposedHelpers.callMethod(
                                thisObj,
                                M_GET_OR_CREATE_PREFERRED_REFRESH_RATE_DATA,
                                key
                            ) ?: return

                            XposedHelpers.callMethod(data, M_SET_APP_REQ_FIRST, false)
                            XposedHelpers.callMethod(data, M_ENABLE_WIN_OVERRIDE, false)
                            XposedHelpers.callMethod(data, M_SET_DISABLE_VIEW_OVERRIDE, true)
                            XposedHelpers.callMethod(data, M_SET_LOW_FEQ_MODE, false)
                            XposedHelpers.callMethod(data, M_SET_BRIGHTNESS_BLOCK, false)
                            XposedHelpers.callMethod(data, M_SET_APP_LOW_REFRESH_RATE, 0)

                            for (i in 0..3) {
                                XposedHelpers.callMethod(
                                    data,
                                    M_PUT_USR_OVERRIDE_REFRESH_RATE_ID,
                                    i,
                                    EXTREME_RATE_ID
                                )
                            }

                            param.result = data
                        } catch (error: Throwable) {
                            log("Framework 165Hz preferred-data rewrite failed: ${error.javaClass.simpleName}")
                        } finally {
                            isInternalPreferredDataRewrite.set(false)
                        }
                    }
                }
            )
        ) installed++

        if (installed > 0) {
            log("Framework 165Hz (jadx-aligned) hooks installed in $packageName: methods=$installed")
        } else {
            log("Framework 165Hz (jadx-aligned) hooks not matched in $packageName")
        }

        return installed
    }

    private fun tryHookFromJadx(
        className: String,
        classLoader: ClassLoader?,
        methodName: String,
        parameters: Array<Any> = emptyArray(),
        callback: XC_MethodHook
    ): Boolean {
        val paramsKey = parameters.joinToString(",") { param ->
            when (param) {
                is Class<*> -> param.name
                is String -> param
                else -> param.toString()
            }
        }
        val hookKey = "method|$className|$methodName|$paramsKey"
        if (!addHookKeyIfAbsent(hookKey)) return true

        val loaders = candidateLoaders(classLoader)
        var lastError: Throwable? = null
        loaders.forEach { loader ->
            val success = runCatching {
                val args = arrayOfNulls<Any>(parameters.size + 1)
                parameters.forEachIndexed { index, value -> args[index] = value }
                args[parameters.size] = callback
                XposedHelpers.findAndHookMethod(className, loader, methodName, *args)
            }.onFailure { error ->
                lastError = error
            }.isSuccess
            if (success) return true
        }

        removeHookKey(hookKey)
        log("Framework hook miss: $className#$methodName (${lastError?.javaClass?.simpleName ?: "Unknown"})")
        return false
    }

    private fun hookFixSmallIcon(method: Method): Boolean {
        if (!markMethodInstalled(method, "native_notify")) return false
        XposedBridge.hookMethod(method, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!LspConfig.isNativeNotifyIconEnabledXposed()) return
                NativeNotifyIconRules.buildSupplementResultForArgsXposed(
                    args = param.args,
                    returnType = method.returnType
                )?.let { supplement ->
                    param.result = supplement
                    return
                }
                // Keep bypassing OEM fix path so framework won't rewrite app-provided native small icons.
                param.result = defaultResult(method.returnType)
            }
        })
        return true
    }

    private fun shouldForceBubbleRecord(record: Any?): Boolean {
        if (!isNativeNotificationBubblesEnabledForHook()) return false
        if (record == null) return false
        if (isForegroundServiceOrUserInitiatedJob(record)) return false
        return getBubbleMetadataFromRecord(record) != null
    }

    private fun forceBubbleRecordIfEligible(record: Any?) {
        if (!shouldForceBubbleRecord(record)) return
        runCatching {
            XposedHelpers.callMethod(record, M_SET_ALLOW_BUBBLE, true)
        }.onFailure { error ->
            log("Force native bubble setAllowBubble failed: ${error.javaClass.simpleName}")
        }
        setNotificationBubbleFlag(record)
    }

    private fun getNotificationFromRecord(record: Any?): Any? {
        if (record == null) return null
        return runCatching {
            XposedHelpers.callMethod(record, "getNotification")
        }.getOrNull()
    }

    private fun getBubbleMetadataFromRecord(record: Any?): Any? {
        val notification = getNotificationFromRecord(record) ?: return null
        return runCatching {
            XposedHelpers.callMethod(notification, "getBubbleMetadata")
        }.getOrNull()
    }

    private fun isForegroundServiceOrUserInitiatedJob(record: Any?): Boolean {
        val notification = getNotificationFromRecord(record) ?: return false
        return runCatching {
            XposedHelpers.callMethod(notification, "isFgsOrUij") as? Boolean == true
        }.getOrDefault(false)
    }

    private fun setNotificationBubbleFlag(record: Any?) {
        val notification = getNotificationFromRecord(record) as? Notification ?: return
        notification.flags = notification.flags or Notification.FLAG_BUBBLE
    }

    private fun isNativeNotificationBubblesEnabledForHook(): Boolean {
        return runCatching {
            LspConfig.isNativeNotificationBubblesEnabledXposed()
        }.getOrDefault(false)
    }

    private fun isExtremeRefreshEnabledForHook(): Boolean {
        val value = runCatching { LspConfig.isExtremeRefresh165EnabledXposed() }.getOrDefault(false)
        if (lastLoggedToggle != value) {
            lastLoggedToggle = value
            log("Framework 165Hz toggle readable: $value")
        }
        return value
    }

    private fun markMethodInstalled(method: Method, feature: String): Boolean {
        val key = "$feature|${method.declaringClass.name}|${method.name}|${method.parameterTypes.joinToString { it.name }}"
        return addHookKeyIfAbsent(key)
    }

    private fun addHookKeyIfAbsent(key: String): Boolean {
        synchronized(installedHookKeys) {
            if (installedHookKeys.contains(key)) return false
            installedHookKeys.add(key)
            return true
        }
    }

    private fun removeHookKey(key: String) {
        synchronized(installedHookKeys) {
            installedHookKeys.remove(key)
        }
    }

    private fun findClassAnyLoader(className: String, classLoader: ClassLoader?): Class<*>? {
        candidateLoaders(classLoader).forEach { loader ->
            val klass = XposedHelpers.findClassIfExists(className, loader)
            if (klass != null) return klass
        }
        return null
    }

    private fun resolveRecentTaskDimenIds(resources: Resources): Set<Int> {
        val cached = cachedRecentTaskDimenIds
        if (cached.isNotEmpty()) return cached

        val ids = recentTaskDimenNames.mapNotNull { name ->
            val id = resources.getIdentifier(name, "dimen", PACKAGE_LAUNCHER)
            if (id != 0) id else null
        }.toSet()
        if (ids.isNotEmpty()) {
            cachedRecentTaskDimenIds = ids
        }
        return ids
    }

    private fun candidateLoaders(primary: ClassLoader?): List<ClassLoader?> {
        val list = ArrayList<ClassLoader?>()
        list.add(primary)
        list.add(null)
        list.add(ClassLoader.getSystemClassLoader())
        return list.distinct()
    }

    private fun defaultResult(returnType: Class<*>): Any? {
        return when (returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0f
            java.lang.Double.TYPE -> 0.0
            java.lang.Short.TYPE -> 0.toShort()
            java.lang.Byte.TYPE -> 0.toByte()
            java.lang.Character.TYPE -> 0.toChar()
            java.lang.Void.TYPE -> null
            else -> null
        }
    }

    private fun log(message: String) {
        XposedBridge.log("$TAG: $message")
    }
}
