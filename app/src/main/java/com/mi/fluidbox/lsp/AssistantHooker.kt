package com.mi.fluidbox.lsp

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Message
import android.os.Process
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object AssistantHooker {
    private const val TAG = "FluidBox-Assistant"
    private const val SYSTEM_PACKAGE = "system"
    private const val ANDROID_PACKAGE = "android"
    private const val SYSTEM_UI_PACKAGE = "com.android.systemui"
    private const val GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox"
    private const val MSG_POWER_LONG_PRESS_FOR_SPEECH = 0x3F3
    private const val DEBOUNCE_WINDOW_MS = 1_000L
    private const val WARMUP_TIMEOUT_MS = 600L
    private const val WARMUP_TIMEOUT_AGGRESSIVE_MS = 1_000L
    private const val POST_CONNECT_SETTLE_MS = 120L
    private const val POST_CONNECT_SETTLE_AGGRESSIVE_MS = 250L
    private const val SHOW_SESSION_RETRY_DELAY_MS = 80L
    private const val FORCE_STOP_SETTLE_MS = 100L
    private const val SHOW_SOURCE_ASSIST_GESTURE = 4
    private const val INVOCATION_TYPE_POWER_BUTTON_LONG_PRESS = 6
    private const val KEYBOARD_DEVICE_ID_SYSTEM = -1

    private val installedHooks = ConcurrentHashMap.newKeySet<String>()

    @Volatile
    private var systemContext: Context? = null

    @Volatile
    private var lastPowerInterceptAt = 0L

    @Volatile
    private var contextualSearchConfigId: Int? = null

    fun hook(packageName: String, classLoader: ClassLoader) {
        when (packageName) {
            SYSTEM_PACKAGE, ANDROID_PACKAGE -> hookSystem(packageName, classLoader)
            SYSTEM_UI_PACKAGE -> hookSystemUi(classLoader)
        }
    }

    private fun hookSystem(packageName: String, classLoader: ClassLoader) {
        hookSystemContext(packageName, classLoader)
        hookContextualSearch(packageName, classLoader)
        hookPowerLongPress(packageName, classLoader)
        hookOriginalAssistantStart(packageName, classLoader)
    }

    private fun hookSystemUi(classLoader: ClassLoader) {
        if (!installedHooks.add("systemui:gesture")) return
        val cls = XposedHelpers.findClassIfExists(
            "com.oplus.systemui.navigationbar.ocrscreen.OplusOcrScreenBusiness",
            classLoader
        ) ?: return log("Gesture bar business class not found")

        runCatching {
            XposedBridge.hookAllMethods(
                cls,
                "onLongPressed",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!LspConfig.isAssistantGestureCircleEnabledXposed()) return
                        findContext(param.thisObject)?.let(::performHapticFeedback)
                        if (triggerCircleToSearch()) {
                            param.result = null
                        }
                    }
                }
            )
            log("Gesture bar long press hooked")
        }.onFailure { log("Failed to hook gesture bar long press", it) }
    }

    private fun hookSystemContext(packageName: String, classLoader: ClassLoader) {
        val key = "$packageName:system-context"
        if (!installedHooks.add(key)) return
        val cls = XposedHelpers.findClassIfExists("com.android.server.SystemService", classLoader) ?: return
        runCatching {
            XposedBridge.hookAllMethods(
                cls,
                "getContext",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        (param.result as? Context)?.let {
                            systemContext = it
                            HookLog.bindContext(it)
                        }
                    }
                }
            )
        }.onFailure { log("Failed to hook SystemService.getContext", it) }
    }

    private fun hookPowerLongPress(packageName: String, classLoader: ClassLoader) {
        val key = "$packageName:power-long-press"
        if (!installedHooks.add(key)) return

        XposedHelpers.findClassIfExists(
            "com.android.server.policy.PhoneWindowManagerExtImpl\$OplusSpeechHandler",
            classLoader
        )?.let { cls ->
            runCatching {
                XposedHelpers.findAndHookMethod(
                    cls,
                    "handleMessage",
                    Message::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val message = param.args.firstOrNull() as? Message ?: return
                            if (message.what != MSG_POWER_LONG_PRESS_FOR_SPEECH) return
                            if (interceptPowerLongPress(param.thisObject)) {
                                param.result = null
                            }
                        }
                    }
                )
                log("OPlus speech handler hooked")
            }.onFailure { log("Failed to hook OPlus speech handler", it) }
        }

        XposedHelpers.findClassIfExists("com.android.server.policy.PhoneWindowManager", classLoader)
            ?.let { cls ->
                runCatching {
                    XposedBridge.hookAllMethods(
                        cls,
                        "powerLongPress",
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                if (interceptPowerLongPress(param.thisObject)) {
                                    param.result = null
                                }
                            }
                        }
                    )
                    log("PhoneWindowManager.powerLongPress hooked")
                }.onFailure { log("Failed to hook PhoneWindowManager.powerLongPress", it) }
            }
    }

    private fun hookContextualSearch(packageName: String, classLoader: ClassLoader) {
        val key = "$packageName:contextual-search"
        if (!installedHooks.add(key)) return
        contextualSearchConfigId = findContextualSearchConfigId(classLoader)

        XposedHelpers.findClassIfExists("com.android.server.SystemServer", classLoader)?.let { cls ->
            runCatching {
                XposedHelpers.findAndHookMethod(
                    cls,
                    "deviceHasConfigString",
                    Context::class.java,
                    Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (!isCircleFeatureEnabled()) return
                            val resId = param.args.getOrNull(1) as? Int ?: return
                            if (resId == contextualSearchConfigId) {
                                param.result = true
                            }
                        }
                    }
                )
            }.onFailure { log("Failed to hook contextual search config check", it) }
        }

        XposedHelpers.findClassIfExists(
            "com.android.server.contextualsearch.ContextualSearchManagerService",
            classLoader
        )?.let { cls ->
            hookContextualSearchService(cls)
            cls.declaredClasses.forEach(::hookContextualSearchService)
        }
    }

    private fun hookContextualSearchService(cls: Class<*>) {
        if (!installedHooks.add("contextual:${cls.name}")) return
        runCatching {
            cls.declaredMethods
                .filter { it.name == "getContextualSearchPackageName" }
                .forEach { method ->
                    XposedBridge.hookMethod(
                        method,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                if (isCircleFeatureEnabled()) {
                                    param.result = GOOGLE_APP_PACKAGE
                                }
                            }
                        }
                    )
                }
            cls.declaredMethods
                .filter { it.name == "enforcePermission" }
                .forEach { method ->
                    XposedBridge.hookMethod(
                        method,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                if (isCircleFeatureEnabled() && isTrustedContextualSearchCaller()) {
                                    param.result = null
                                }
                            }
                        }
                    )
                }
            cls.declaredMethods
                .filter { it.name == "startContextualSearch" }
                .forEach { method ->
                    XposedBridge.hookMethod(method, ContextualSearchIdentityHook)
                }
        }.onFailure { log("Failed to hook contextual search service ${cls.name}", it) }
    }

    private object ContextualSearchIdentityHook : XC_MethodHook() {
        private val identity = ThreadLocal<Long?>()

        override fun beforeHookedMethod(param: MethodHookParam) {
            if (!isCircleFeatureEnabled() || !isTrustedContextualSearchCaller()) return
            identity.set(Binder.clearCallingIdentity())
        }

        override fun afterHookedMethod(param: MethodHookParam) {
            identity.get()?.let(Binder::restoreCallingIdentity)
            identity.remove()
        }
    }

    private fun hookOriginalAssistantStart(packageName: String, classLoader: ClassLoader) {
        val key = "$packageName:original-assistant-block"
        if (!installedHooks.add(key)) return
        val classNames = listOf(
            "com.oplus.voiceassistant.service.BrenoServiceProxy",
            "com.oplus.voiceassistant.BrenoService",
            "com.heytap.voiceassistant.service.VoiceAssistantService"
        )
        classNames.forEach { className ->
            val cls = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            runCatching {
                cls.declaredMethods
                    .filter { it.name.startsWith("start", ignoreCase = true) }
                    .forEach { method ->
                        XposedBridge.hookMethod(
                            method,
                            object : XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {
                                    if (LspConfig.getAssistantPowerModeXposed() != LspConfig.ASSISTANT_POWER_MODE_NONE) {
                                        param.result = null
                                    }
                                }
                            }
                        )
                    }
            }.onFailure { log("Failed to hook original assistant class $className", it) }
        }
    }

    private fun interceptPowerLongPress(source: Any?): Boolean {
        val mode = LspConfig.getAssistantPowerModeXposed()
        if (mode == LspConfig.ASSISTANT_POWER_MODE_NONE) return false

        val now = SystemClock.elapsedRealtime()
        if (now - lastPowerInterceptAt < DEBOUNCE_WINDOW_MS) return true
        lastPowerInterceptAt = now

        val context = findContext(source) ?: systemContext ?: findSystemContext()
        context?.let(::performHapticFeedback)
        return when (mode) {
            LspConfig.ASSISTANT_POWER_MODE_GEMINI -> {
                if (context != null) {
                    triggerGemini(context)
                } else {
                    triggerGeminiFallbackByShell()
                    true
                }
            }
            else -> false
        }
    }

    private fun triggerGemini(context: Context): Boolean {
        val token = Binder.clearCallingIdentity()
        return runCatching {
            warmUpGoogleApp(context, aggressive = false)
            if (tryShowSessionViaVims(attempt = 1)) return@runCatching true

            forceStopGoogleApp(context)
            sleepQuietly(SHOW_SESSION_RETRY_DELAY_MS)
            warmUpGoogleApp(context, aggressive = true)
            if (tryShowSessionViaVims(attempt = 2)) return@runCatching true

            val voiceCommand = Intent(Intent.ACTION_VOICE_COMMAND)
                .setPackage(GOOGLE_APP_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (startActivity(context, voiceCommand)) return@runCatching true

            triggerGeminiFallbackByShell()
            true
        }.onFailure {
            log("Failed to trigger Gemini", it)
        }.also {
            Binder.restoreCallingIdentity(token)
        }.getOrDefault(false)
    }

    private fun warmUpGoogleApp(context: Context, aggressive: Boolean) {
        val component = findGoogleVoiceInteractionService(context) ?: return
        val intent = Intent("android.service.voice.VoiceInteractionService").setComponent(component)
        val latch = CountDownLatch(1)
        val connected = AtomicBoolean(false)
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: android.content.ComponentName, service: IBinder) {
                connected.set(true)
                latch.countDown()
            }

            override fun onServiceDisconnected(name: android.content.ComponentName) = Unit

            override fun onBindingDied(name: android.content.ComponentName) {
                latch.countDown()
            }

            override fun onNullBinding(name: android.content.ComponentName) {
                connected.set(true)
                latch.countDown()
            }
        }

        val flags = Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT
        val bound = runCatching {
            context.bindService(intent, connection, flags)
        }.getOrDefault(false)
        if (!bound) return

        try {
            val timeoutMs = if (aggressive) WARMUP_TIMEOUT_AGGRESSIVE_MS else WARMUP_TIMEOUT_MS
            val settleMs = if (aggressive) POST_CONNECT_SETTLE_AGGRESSIVE_MS else POST_CONNECT_SETTLE_MS
            val arrived = latch.await(timeoutMs, TimeUnit.MILLISECONDS)
            if (arrived && connected.get()) {
                sleepQuietly(settleMs)
            }
        } finally {
            runCatching { context.unbindService(connection) }
        }
    }

    private fun findGoogleVoiceInteractionService(context: Context): android.content.ComponentName? {
        return runCatching {
            val intent = Intent("android.service.voice.VoiceInteractionService")
                .setPackage(GOOGLE_APP_PACKAGE)
            val services = context.packageManager.queryIntentServices(intent, 0)
            val service = services.firstOrNull { info ->
                info.serviceInfo?.permission == android.Manifest.permission.BIND_VOICE_INTERACTION
            }?.serviceInfo ?: services.firstOrNull()?.serviceInfo ?: return@runCatching null
            android.content.ComponentName(service.packageName, service.name)
        }.getOrNull()
    }

    private fun tryShowSessionViaVims(attempt: Int): Boolean {
        return runCatching {
            val binder = getService("voiceinteraction") ?: return@runCatching false
            val stubClass = Class.forName("com.android.internal.app.IVoiceInteractionManagerService\$Stub")
            val service = stubClass
                .getMethod("asInterface", IBinder::class.java)
                .invoke(null, binder) ?: return@runCatching false
            val method = service.javaClass.methods.firstOrNull { it.name == "showSessionForActiveService" }
                ?: service.javaClass.methods.firstOrNull { it.name == "showSessionFromSession" }
                ?: return@runCatching false
            method.isAccessible = true
            val bundle = newAssistantInvocationBundle()
            val args = method.parameterTypes.map { type ->
                when {
                    IBinder::class.java.isAssignableFrom(type) -> null
                    type == Bundle::class.java -> bundle
                    type == Integer.TYPE -> SHOW_SOURCE_ASSIST_GESTURE
                    type == java.lang.Boolean.TYPE -> true
                    type == String::class.java -> null
                    else -> null
                }
            }.toTypedArray()
            val result = method.invoke(service, *args)
            val ok = when {
                method.returnType == Void.TYPE -> true
                method.returnType == java.lang.Boolean.TYPE -> result == true
                else -> result != java.lang.Boolean.FALSE
            }
            if (!ok) log("VIMS showSession returned false on attempt $attempt")
            ok
        }.onFailure {
            log("Failed to show Gemini session on attempt $attempt", it)
        }.getOrDefault(false)
    }

    private fun newAssistantInvocationBundle(): Bundle {
        return Bundle().apply {
            putInt("invocation_type", INVOCATION_TYPE_POWER_BUTTON_LONG_PRESS)
            putLong("invocation_time_ms", SystemClock.uptimeMillis())
            putInt("invocation_phone_state", 0)
            putLong(Intent.EXTRA_TIME, SystemClock.uptimeMillis())
            putInt(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID, KEYBOARD_DEVICE_ID_SYSTEM)
            putBoolean("xiaobu_trigger", true)
        }
    }

    private fun forceStopGoogleApp(context: Context) {
        runCatching {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return
            val method = ActivityManager::class.java.getDeclaredMethod(
                "forceStopPackage",
                String::class.java
            )
            method.isAccessible = true
            method.invoke(am, GOOGLE_APP_PACKAGE)
            sleepQuietly(FORCE_STOP_SETTLE_MS)
        }.onFailure {
            log("Failed to force stop Google app", it)
        }
    }

    private fun triggerGeminiFallbackByShell() {
        runCatching {
            log("Gemini shell fallback command start")
            Runtime.getRuntime().exec(
                arrayOf(
                    "am",
                    "start",
                    "-a",
                    Intent.ACTION_VOICE_COMMAND,
                    "-p",
                    GOOGLE_APP_PACKAGE
                )
            )
            log("Gemini shell fallback command issued")
        }.onFailure {
            log("Gemini shell fallback failed", it)
        }
    }

    private fun startActivity(context: Context, intent: Intent): Boolean {
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun triggerCircleToSearch(): Boolean {
        val token = Binder.clearCallingIdentity()
        return runCatching {
            val binder = getService("contextual_search") ?: return@runCatching false
            val stubClass = Class.forName("android.app.contextualsearch.IContextualSearchManager\$Stub")
            val service = stubClass
                .getMethod("asInterface", IBinder::class.java)
                .invoke(null, binder)
                ?: return@runCatching false
            val method = service.javaClass.methods.firstOrNull {
                it.name == "startContextualSearch" && it.parameterTypes.size == 1
            } ?: return@runCatching false
            method.invoke(service, 1)
            true
        }.onFailure {
            log("Failed to trigger Circle to Search", it)
        }.also {
            Binder.restoreCallingIdentity(token)
        }.getOrDefault(false)
    }

    private fun getService(name: String): IBinder? {
        return runCatching {
            Class.forName("android.os.ServiceManager")
                .getMethod("getService", String::class.java)
                .invoke(null, name) as? IBinder
        }.getOrNull()
    }

    private fun findContextualSearchConfigId(classLoader: ClassLoader): Int? {
        return runCatching {
            val cls = XposedHelpers.findClass("com.android.internal.R\$string", classLoader)
            XposedHelpers.getStaticIntField(cls, "config_defaultContextualSearchPackageName")
        }.getOrNull()
    }

    private fun isCircleFeatureEnabled(): Boolean {
        return LspConfig.isAssistantGestureCircleEnabledXposed()
    }

    private fun isTrustedContextualSearchCaller(): Boolean {
        val uid = Binder.getCallingUid()
        if (uid == Process.SYSTEM_UID) return true
        return runCatching {
            val context = systemContext ?: findSystemContext() ?: return@runCatching false
            context.packageManager.getPackagesForUid(uid)
                ?.contains(SYSTEM_UI_PACKAGE) == true
        }.getOrDefault(false)
    }

    private fun findContext(source: Any?): Context? {
        val direct = source as? Context
        if (direct != null) return direct
        return findFieldValue(source, Context::class.java) ?: findSystemContext()
    }

    private fun <T> findFieldValue(source: Any?, type: Class<T>, depth: Int = 0): T? {
        if (source == null || depth > 2) return null
        val cls = source.javaClass
        var current: Class<*>? = cls
        while (current != null) {
            for (field in current.declaredFields) {
                val value = runCatching {
                    field.isAccessible = true
                    field.get(source)
                }.getOrNull()
                if (type.isInstance(value)) return type.cast(value)
                if (field.name == "this$0") {
                    findFieldValue(value, type, depth + 1)?.let { return it }
                }
            }
            current = current.superclass
        }
        return null
    }

    private fun findSystemContext(): Context? {
        systemContext?.let { return it }
        return runCatching {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val thread = activityThreadClass
                .getMethod("currentActivityThread")
                .invoke(null) ?: return@runCatching null
            activityThreadClass
                .getMethod("getSystemContext")
                .invoke(thread) as? Context
        }.onSuccess { context ->
            if (context != null) systemContext = context
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun performHapticFeedback(context: Context) {
        runCatching {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                vibrator.vibrate(20L)
            }
        }
    }

    private fun performHapticFeedback(source: Any) {
        runCatching {
            (source as? View)?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                ?: findContext(source)?.let(::performHapticFeedback)
        }
    }

    private fun sleepQuietly(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun log(message: String, throwable: Throwable? = null) {
        HookLog.i(TAG, message, throwable)
    }
}
