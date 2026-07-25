package com.mi.fluidbox.lsp

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.widget.ImageView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

object SystemUiHooker {
    private const val TAG = "FluidBox-LSP"
    private const val SMALL_ICON_MAX_DP = 64
    private const val AOD_INIT_DARK_THRESHOLD = 40
    private const val AOD_GESTURE_SINGLE_CLICK = 16
    private const val AOD_BRIGHTNESS_MIN = 0
    private const val AOD_BRIGHTNESS_MAX = 255
    private const val DIMEN_MAX_WINDOW_BLUR_RADIUS = "max_window_blur_radius"
    private val installedHookKeys = ConcurrentHashMap.newKeySet<String>()

    private val grayscaleBitmapCache = ConcurrentHashMap<Int, Boolean>()
    private val contrastClassNames = listOf(
        "com.android.internal.util.ContrastColorUtil",
        "com.android.internal.util.NotificationColorUtil",
        "com.oplusos.util.OplusContrastColorUtil",
        "com.oplus.util.OplusContrastColorUtil",
        "com.oplusos.util.OplusNotificationColorUtil",
        "com.oplus.util.OplusNotificationColorUtil"
    )
    private val notificationUtilsClassNames = listOf(
        "com.android.systemui.statusbar.notification.NotificationUtils",
        "com.oplus.systemui.statusbar.notification.NotificationUtils",
        "com.oplusos.systemui.statusbar.notification.NotificationUtils"
    )
    private val iconUtilClassNames = listOf(
        "com.oplus.systemui.statusbar.notification.util.OplusNotificationSmallIconUtil",
        "com.oplusos.systemui.statusbar.notification.util.OplusNotificationSmallIconUtil"
    )
    private val iconManagerClassNames = listOf(
        "com.android.systemui.statusbar.notification.icon.IconManager",
        "com.oplus.systemui.statusbar.notification.icon.IconManager",
        "com.oplusos.systemui.statusbar.notification.icon.IconManager"
    )
    private val aodSingleClickCallbackClassNames = listOf(
        "com.oplus.systemui.aod.scene.AodViewSingleClickWakeUpHolder\$AodSingleClickWakeUpCallback",
        "com.oplus.systemui.aod.scene.PanoramicAodSingleClickWakeUpController\$PanoramicAodSingleClickWakeUpCallback"
    )

    fun hook(packageName: String, classLoader: ClassLoader?) {
        LspRuntimeStatus.markSystemUiScopeActive()
        if (LspConfig.isNativeNotifyIconEnabledXposed()) {
            hookSmallIconDecisions(classLoader)
            hookNotificationUtilsGrayscaleChecks(classLoader)
            hookIconDescriptorReplacement(classLoader)
        }
        hookAodEnhanceInSystemUi(classLoader, packageName)
        hookNotificationBubbleBlurRadius(classLoader, packageName)
        log("SystemUI hooked in $packageName")
    }

    private fun hookNotificationBubbleBlurRadius(classLoader: ClassLoader?, packageName: String) {
        val resourcesClass = XposedHelpers.findClassIfExists("android.content.res.Resources", classLoader)
            ?: Resources::class.java
        val methods = resourcesClass.declaredMethods.filter { method ->
            method.parameterTypes.size == 1 &&
                method.parameterTypes[0] == Integer.TYPE &&
                (
                    (method.name == "getDimensionPixelSize" && method.returnType == Integer.TYPE) ||
                        (method.name == "getDimensionPixelOffset" && method.returnType == Integer.TYPE) ||
                        (method.name == "getDimension" && method.returnType == java.lang.Float.TYPE)
                    )
        }
        var hooked = 0
        methods.forEach { method ->
            val key = "notification_bubble_blur|${method.declaringClass.name}|${method.name}"
            if (!addHookKeyIfAbsent(key)) return@forEach
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!LspConfig.isNotificationBubbleBlurEnabledXposed()) return
                    val requestId = param.args.firstOrNull() as? Int ?: return
                    val resources = param.thisObject as? Resources ?: return
                    if (!isMaxWindowBlurRadius(resources, requestId)) return
                    val blurRadiusPx = LspConfig.getNotificationBubbleBlurRadiusPxXposed()
                    param.result = if (method.returnType == java.lang.Float.TYPE) {
                        blurRadiusPx.toFloat()
                    } else {
                        blurRadiusPx
                    }
                }
            })
            hooked++
        }
        if (hooked > 0) {
            log("SystemUI notification bubble blur hooks installed in $packageName: methods=$hooked")
        }
    }

    private fun hookNotificationUtilsGrayscaleChecks(classLoader: ClassLoader?) {
        notificationUtilsClassNames.forEach { className ->
            val hookClass = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            val methods = hookClass.declaredMethods.filter { method ->
                method.returnType == java.lang.Boolean.TYPE &&
                    method.parameterTypes.isNotEmpty() &&
                    method.parameterTypes.firstOrNull()?.let { ImageView::class.java.isAssignableFrom(it) } == true &&
                    (method.name.equals("isGrayscale", ignoreCase = true) ||
                        method.name.equals("isGrayscaleOplus", ignoreCase = true))
            }
            methods.forEach { method ->
                hookNotificationUtilsGrayscaleDetector(method, classLoader)
            }
            log("SystemUI notification grayscale hook: $className (${methods.size})")
        }
    }

    private fun hookSmallIconDecisions(classLoader: ClassLoader?) {
        iconUtilClassNames.forEach { className ->
            val hookClass = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            val methods = hookClass.declaredMethods.filter { method ->
                val name = method.name.lowercase()
                method.returnType == java.lang.Boolean.TYPE &&
                    name == "useappiconforsmallicon"
            }
            methods.forEach { method ->
                hookReturnFalse(method)
            }
            log("SystemUI small-icon decision hook: $className (${methods.size})")
        }
    }

    private fun hookIconDescriptorReplacement(classLoader: ClassLoader?) {
        iconManagerClassNames.forEach { className ->
            val hookClass = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            val methods = hookClass.declaredMethods.filter { method ->
                !method.returnType.isPrimitive &&
                    method.name.equals("getIconDescriptor", ignoreCase = true) &&
                    method.parameterTypes.isNotEmpty() &&
                    method.parameterTypes.any { type ->
                        val typeName = type.name
                        typeName.contains("Notification") || typeName.contains("Entry")
                    }
            }
            methods.forEach { method ->
                hookReplaceIconDescriptor(method)
            }
            log("SystemUI icon-descriptor hook: $className (${methods.size})")
        }
    }

    private fun hookAodEnhanceInSystemUi(classLoader: ClassLoader?, packageName: String) {
        val dozeServiceClass =
            XposedHelpers.findClassIfExists("com.oplus.systemui.aod.OplusDozeServiceExImpl", classLoader)
        val dozeServiceMethods = dozeServiceClass
            ?.declaredMethods
            ?.filter { method ->
                method.name == "setBrightnessBeforeDozing" &&
                    method.parameterTypes.isEmpty() &&
                    method.returnType == Integer.TYPE
            }
            .orEmpty()
        dozeServiceMethods.forEach { method ->
            val key = "aod_enhance|${method.declaringClass.name}|${method.name}|init"
            if (!addHookKeyIfAbsent(key)) return@forEach
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!LspConfig.isAodEnhanceEnabledXposed()) return
                    val originalResult = param.result as? Int ?: return
                    val darkTarget = LspConfig.getAodInitDarkBrightnessXposed()
                    val brightTarget = LspConfig.getAodInitBrightBrightnessXposed()
                    param.result = if (originalResult < AOD_INIT_DARK_THRESHOLD) {
                        darkTarget
                    } else {
                        brightTarget
                    }
                }
            })
        }

        val baseDisplayUtilClass =
            XposedHelpers.findClassIfExists("com.oplus.systemui.aod.display.BaseDisplayUtil", classLoader)
        val runningMethods = baseDisplayUtilClass
            ?.declaredMethods
            ?.filter { method ->
                method.name == "setDozeScreenBrightness" &&
                    method.parameterTypes.size == 2 &&
                    method.parameterTypes[0] == java.lang.Float.TYPE &&
                    method.parameterTypes[1] == Integer.TYPE
            }
            .orEmpty()
        runningMethods.forEach { method ->
            val key = "aod_enhance|${method.declaringClass.name}|${method.name}|running"
            if (!addHookKeyIfAbsent(key)) return@forEach
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!LspConfig.isAodEnhanceEnabledXposed()) return
                    val originalNit = param.args.getOrNull(0) as? Float ?: return
                    val originalBrightness = param.args.getOrNull(1) as? Int ?: return
                    val multiplier = LspConfig.getAodRunningBrightnessMultiplierXposed()

                    val boostedNit = originalNit * multiplier
                    val boostedBrightness = (originalBrightness * multiplier)
                        .toInt()
                        .coerceIn(AOD_BRIGHTNESS_MIN, AOD_BRIGHTNESS_MAX)
                    param.args[0] = boostedNit
                    param.args[1] = boostedBrightness
                }
            })
        }

        val smoothControllerCompanion =
            XposedHelpers.findClassIfExists("com.oplus.systemui.aod.display.SmoothTransitionController\$Companion", classLoader)
        val panoramicMethods = smoothControllerCompanion
            ?.declaredMethods
            ?.filter { method ->
                method.name == "getInstance"
            }
            .orEmpty()
        panoramicMethods.forEach { method ->
            val key = "aod_enhance|${method.declaringClass.name}|${method.name}|panoramic"
            if (!addHookKeyIfAbsent(key)) return@forEach
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!LspConfig.isAodEnhanceEnabledXposed()) return
                    if (!LspConfig.isAodPanoramicSupportEnabledXposed()) return
                    val instance = param.result ?: return
                    runCatching {
                        val field = instance.javaClass.getDeclaredField("isSupportPanoramicAllDay")
                        field.isAccessible = true
                        field.setBoolean(instance, true)
                    }
                    runCatching {
                        val field = instance.javaClass.getDeclaredField("isSupportPanoramicAllDayByPanelFeature")
                        field.isAccessible = true
                        field.setBoolean(instance, true)
                    }
                }
            })
        }

        aodSingleClickCallbackClassNames.forEach { className ->
            val callbackClass = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            val methods = callbackClass.declaredMethods.filter { method ->
                method.name == "isSupportGesture" &&
                    method.parameterTypes.size == 1 &&
                    method.parameterTypes[0] == Integer.TYPE &&
                    method.returnType == java.lang.Boolean.TYPE
            }
            methods.forEach { method ->
                val key = "aod_enhance|${method.declaringClass.name}|${method.name}|single_click"
                if (!addHookKeyIfAbsent(key)) return@forEach
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!LspConfig.isAodEnhanceEnabledXposed()) return
                        if (!LspConfig.isAodSingleClickBlockEnabledXposed()) return
                        val gesture = param.args.firstOrNull() as? Int ?: return
                        if (gesture == AOD_GESTURE_SINGLE_CLICK) {
                            param.result = false
                        }
                    }
                })
            }
        }

        if (
            dozeServiceMethods.isNotEmpty() ||
            runningMethods.isNotEmpty() ||
            panoramicMethods.isNotEmpty()
        ) {
            log(
                "SystemUI AOD enhance hooks installed in $packageName: " +
                    "init=${dozeServiceMethods.size}, running=${runningMethods.size}, panoramic=${panoramicMethods.size}"
            )
        }
    }

    private fun hookReturnFalse(method: Method) {
        XposedBridge.hookMethod(method, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!LspConfig.isNativeNotifyIconEnabledXposed()) return
                param.result = false
            }
        })
    }

    private fun hookNotificationUtilsGrayscaleDetector(method: Method, classLoader: ClassLoader?) {
        XposedBridge.hookMethod(method, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!LspConfig.isNativeNotifyIconEnabledXposed()) return
                val imageView = param.args.firstOrNull() as? ImageView ?: return
                val drawable = imageView.drawable ?: return
                if (isLargeIconCandidate(imageView, drawable)) {
                    // Large content icons should keep original color and must not be forced into grayscale flow.
                    param.result = false
                    return
                }

                val grayscale = resolveIsGrayscaleFromSystem(
                    classLoader = classLoader,
                    context = imageView.context,
                    drawable = drawable
                ) ?: isGrayscaleDrawable(drawable, imageView.context)
                param.result = grayscale
            }
        })
    }

    private fun hookReplaceIconDescriptor(method: Method) {
        XposedBridge.hookMethod(method, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!LspConfig.isNativeNotifyIconEnabledXposed()) return
                val supplementIcon = NativeNotifyIconRules.buildSupplementResultForArgsXposed(
                    args = param.args,
                    returnType = Icon::class.java
                ) as? Icon ?: return

                val descriptor = param.result ?: return
                if (setIconField(descriptor, supplementIcon)) {
                    return
                }
                if (method.returnType.isAssignableFrom(Icon::class.java)) {
                    param.result = supplementIcon
                }
            }
        })
    }

    private fun setIconField(target: Any, icon: Icon): Boolean {
        var current: Class<*>? = target.javaClass
        while (current != null) {
            val field = current.declaredFields.firstOrNull {
                Icon::class.java.isAssignableFrom(it.type) &&
                    (it.name == "icon" || it.name == "mIcon")
            }
            if (field != null) {
                return runCatching {
                    field.isAccessible = true
                    field.set(target, icon)
                    true
                }.getOrDefault(false)
            }
            current = current.superclass
        }
        return false
    }

    private fun resolveIsGrayscaleFromSystem(
        classLoader: ClassLoader?,
        context: Context,
        drawable: Drawable
    ): Boolean? {
        contrastClassNames.forEach { className ->
            val hookClass = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            val method = hookClass.declaredMethods.firstOrNull {
                it.returnType == java.lang.Boolean.TYPE &&
                    (it.name.equals("isGrayscaleIcon", ignoreCase = true) ||
                        it.name.equals("isGrayscale", ignoreCase = true)) &&
                    it.parameterTypes.size == 1 &&
                    Drawable::class.java.isAssignableFrom(it.parameterTypes[0])
            } ?: return@forEach

            val target = if (Modifier.isStatic(method.modifiers)) {
                null
            } else {
                hookClass.declaredMethods.firstOrNull { candidate ->
                    candidate.name == "getInstance" &&
                        candidate.parameterTypes.size == 1 &&
                        Context::class.java.isAssignableFrom(candidate.parameterTypes[0]) &&
                        Modifier.isStatic(candidate.modifiers)
                }?.let { getInstance ->
                    runCatching {
                        getInstance.isAccessible = true
                        getInstance.invoke(null, context)
                    }.getOrNull()
                } ?: return@forEach
            }

            val value = runCatching {
                method.isAccessible = true
                method.invoke(target, drawable) as? Boolean
            }.getOrNull()
            if (value != null) return value
        }
        return null
    }

    private fun isGrayscaleDrawable(drawable: Drawable, context: Context): Boolean {
        if (isDrawableTooLargeForSmallIcon(drawable, context)) return false
        val probe = drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate()
        val width = probe.intrinsicWidth.takeIf { it > 0 } ?: 64
        val height = probe.intrinsicHeight.takeIf { it > 0 } ?: 64
        val safeWidth = width.coerceIn(16, 256)
        val safeHeight = height.coerceIn(16, 256)

        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        probe.setBounds(0, 0, safeWidth, safeHeight)
        probe.draw(canvas)
        return isGrayscaleBitmap(bitmap)
    }

    private fun isGrayscaleBitmap(bitmap: Bitmap): Boolean {
        grayscaleBitmapCache[bitmap.generationId]?.let { return it }

        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return true

        val xStep = (width / 32).coerceAtLeast(1)
        val yStep = (height / 32).coerceAtLeast(1)
        var isGray = true

        loop@ for (y in 0 until height step yStep) {
            for (x in 0 until width step xStep) {
                val color = bitmap.getPixel(x, y)
                val a = Color.alpha(color)
                if (a == 0) continue

                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)
                if (kotlin.math.abs(r - g) > 10 || kotlin.math.abs(r - b) > 10 || kotlin.math.abs(g - b) > 10) {
                    isGray = false
                    break@loop
                }
            }
        }

        grayscaleBitmapCache[bitmap.generationId] = isGray
        return isGray
    }

    private fun isLargeIconCandidate(imageView: ImageView, drawable: Drawable): Boolean {
        val maxSizePx = resolveSmallIconMaxPx(imageView.context)
        val viewWidth = imageView.width.takeIf { it > 0 } ?: imageView.measuredWidth.takeIf { it > 0 } ?: 0
        val viewHeight = imageView.height.takeIf { it > 0 } ?: imageView.measuredHeight.takeIf { it > 0 } ?: 0
        if (viewWidth > maxSizePx || viewHeight > maxSizePx) return true
        return isDrawableTooLargeForSmallIcon(drawable, imageView.context)
    }

    private fun isDrawableTooLargeForSmallIcon(drawable: Drawable, context: Context): Boolean {
        val maxSizePx = resolveSmallIconMaxPx(context)
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: return false
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: return false
        return width > maxSizePx || height > maxSizePx
    }

    private fun resolveSmallIconMaxPx(context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (SMALL_ICON_MAX_DP * density).toInt().coerceAtLeast(64)
    }

    private fun isMaxWindowBlurRadius(resources: Resources, resourceId: Int): Boolean {
        return runCatching {
            resources.getResourceEntryName(resourceId) == DIMEN_MAX_WINDOW_BLUR_RADIUS
        }.getOrDefault(false)
    }

    private fun log(message: String) {
        XposedBridge.log("$TAG: $message")
    }

    private fun addHookKeyIfAbsent(key: String): Boolean {
        return installedHookKeys.add(key)
    }
}
