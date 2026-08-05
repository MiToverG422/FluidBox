package com.mi.fluidbox.lsp

import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TransportInfo
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

object SystemUiHooker {
    private const val TAG = "FluidBox-LSP"
    private const val SMALL_ICON_MAX_DP = 64
    private const val AOD_INIT_DARK_THRESHOLD = 40
    private const val AOD_GESTURE_SINGLE_CLICK = 16
    private const val AOD_BRIGHTNESS_MIN = 0
    private const val AOD_BRIGHTNESS_MAX = 255
    private val installedHookKeys = ConcurrentHashMap.newKeySet<String>()
    private val mobileTypeViews = ConcurrentHashMap<Int, MobileTypeViewState>()
    private val mobileTypeLayoutParams = ConcurrentHashMap<Int, FrameLayout.LayoutParams>()
    private val mobileTypeLayoutListeners = ConcurrentHashMap<Int, WeakReference<ViewTreeObserver.OnGlobalLayoutListener>>()
    private val mobileTypePendingLayout = ConcurrentHashMap.newKeySet<Int>()
    private val missingWifiNetworks = ConcurrentHashMap.newKeySet<Network>()
    private val mobileRebindHandler = Handler(Looper.getMainLooper())
    private val mobileRebindToken = Any()
    @Volatile private var mobileStatusMonitorStarted = false
    @Volatile private var mobileSubIdsHooked = false
    @Volatile private var mobileSimState = MobileSimState()
    @Volatile private var mobileIconController: WeakReference<Any>? = null
    @Volatile private var mobileIconSubIds: List<Int> = emptyList()
    @Volatile private var mobileDataStateSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID
    @Volatile private var mobileDataStateCallback: TelephonyCallback? = null
    @Volatile private var mobileDataStateTelephonyManager: TelephonyManager? = null
    @Volatile private var mobileStatusNetworkState = MobileStatusNetworkState()

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
    private val mobileViewBinderClassNames = listOf(
        "com.oplus.systemui.statusbar.pipeline.mobile.ui.view.OplusStatusBarMobileViewBinder",
        "com.oplus.systemui.statusbar.pipeline.mobile.ui.view.BigTypeStatusBarMobileViewBinder",
        "com.oplus.systemui.statusbar.pipeline.mobile.ui.view.SprintStatusBarMobileViewBinder",
        "com.oplus.systemui.statusbar.pipeline.mobile.ui.view.TMOStatusBarMobileViewBinder"
    )
    private val mobileTypeViewNames = setOf(
        "mobile_type",
        "mobile_type_alone",
        "mobile_type_container"
    )
    private val mobileRootViewNames = setOf("mobile_combo_real", "mobile_group")
    private val mobileSignalViewNames = setOf("mobile_signal")
    private val mobileDataActivityViewNames = setOf(
        "data_inout",
        "data_inout_alone",
        "inout_container"
    )

    private data class MobileStatusNetworkState(
        val mobileDataEnabled: Boolean = true,
        val mobileDataStateKnown: Boolean = false,
        val wifiConnected: Boolean = false,
        val activeDataSubId: Int = SubscriptionManager.INVALID_SUBSCRIPTION_ID
    )

    private data class MobileSimState(
        val sim1SubId: Int = SubscriptionManager.INVALID_SUBSCRIPTION_ID,
        val sim2SubId: Int = SubscriptionManager.INVALID_SUBSCRIPTION_ID,
        val dataSubId: Int = SubscriptionManager.INVALID_SUBSCRIPTION_ID
    ) {
        val dualSim: Boolean
            get() = sim1SubId > 0 && sim2SubId > 0
    }

    private data class MobileTypeViewState(
        val root: WeakReference<ViewGroup>,
        val signal: WeakReference<View>,
        val type: WeakReference<View>,
        val dataActivity: WeakReference<View>?,
        val subscriptionId: Int?,
        val baseRootMinimumWidth: Int,
        val baseSignalTranslationY: Float,
        val baseTypeAlpha: Float,
        val baseDataActivityVisibility: Int?,
        val baseTypeScaleType: ImageView.ScaleType?,
        var measuredTypeWidth: Int,
        var signalContainer: WeakReference<View>?
    )

    fun hook(packageName: String, classLoader: ClassLoader?) {
        LspRuntimeStatus.markSystemUiScopeActive()
        hookMobileTypeVisibility(classLoader)
        hookMobileIconSubIds(classLoader)
        hookMobileTypeStateRefresh(classLoader)
        if (LspConfig.isNativeNotifyIconEnabledXposed()) {
            hookSmallIconDecisions(classLoader)
            hookNotificationUtilsGrayscaleChecks(classLoader)
            hookIconDescriptorReplacement(classLoader)
        }
        hookAodEnhanceInSystemUi(classLoader, packageName)
        log("SystemUI hooked in $packageName")
    }

    private fun hookMobileTypeVisibility(classLoader: ClassLoader?) {
        mobileViewBinderClassNames.forEach { className ->
            val hookClass = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            val methods = hookClass.declaredMethods.filter { method ->
                method.name == "bindCustEx"
            }
            methods.forEach { method ->
                val key = "mobile_type_visibility|${method.declaringClass.name}|${method.name}|${method.parameterTypes.joinToString { it.name }}"
                if (!addHookKeyIfAbsent(key)) return@forEach
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val argCandidates = param.args.toList()
                        val candidates = buildList {
                            add(param.result)
                            add(param.thisObject)
                            argCandidates.forEach { add(it) }
                        }
                        val state = findMobileTypeViewState(argCandidates)
                            ?: findMobileTypeViewState(candidates)
                            ?: return
                        registerMobileTypeView(state)
                    }
                })
            }
            log("SystemUI mobile type visibility hook: $className (${methods.size})")
        }
    }

    private fun hookMobileIconSubIds(classLoader: ClassLoader?) {
        val hookClass = XposedHelpers.findClassIfExists(
            "com.android.systemui.statusbar.phone.ui.StatusBarIconControllerImpl",
            classLoader
        ) ?: return
        val methods = hookClass.declaredMethods.filter { method ->
            method.name == "setNewMobileIconSubIds" &&
                method.parameterTypes.size == 1 &&
                List::class.java.isAssignableFrom(method.parameterTypes[0])
        }
        methods.forEach { method ->
            val key = "mobile_type_sub_ids|${method.declaringClass.name}|${method.name}"
            if (!addHookKeyIfAbsent(key)) return@forEach
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val subIds = (param.args.firstOrNull() as? List<*>)
                        .orEmpty()
                        .mapNotNull { it as? Int }
                        .filter { SubscriptionManager.isValidSubscriptionId(it) }
                    mobileIconController = param.thisObject?.let(::WeakReference)
                    mobileIconSubIds = subIds
                    mobileSimState = buildMobileSimState(param.thisObject, subIds)
                    mobileStatusNetworkState = mobileStatusNetworkState.copy(
                        activeDataSubId = mobileSimState.dataSubId
                    )
                    refreshMobileTypeViews()
                }
            })
        }
        log("SystemUI mobile sub-id hook: ${methods.size}")
    }

    private fun hookMobileTypeStateRefresh(classLoader: ClassLoader?) {
        val hookClass = XposedHelpers.findClassIfExists(
            "com.oplus.systemui.statusbar.pipeline.OplusMobileSignalExImpl",
            classLoader
        ) ?: return
        val methods = hookClass.declaredMethods.filter { method ->
            method.name == "getIconKeyEx"
        }
        methods.forEach { method ->
            val key = "mobile_type_state_refresh|${method.declaringClass.name}|${method.name}|${method.parameterTypes.joinToString { it.name }}"
            if (!addHookKeyIfAbsent(key)) return@forEach
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    refreshMobileTypeViews()
                }
            })
        }
        log("SystemUI mobile type state refresh hook: ${methods.size}")
    }

    private fun registerMobileTypeView(state: MobileTypeViewState) {
        val root = state.root.get() ?: return
        mobileTypeViews[System.identityHashCode(root)] = state
        startMobileStatusMonitor(root.context.applicationContext ?: root.context)
        registerMobileTypeLayoutListener(root)
        applyMobileTypeVisibility(state)
    }

    private fun registerMobileTypeLayoutListener(root: ViewGroup) {
        val key = System.identityHashCode(root)
        if (mobileTypeLayoutListeners[key]?.get() != null) return
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rootView = mobileTypeViews[key]?.root?.get() ?: return@OnGlobalLayoutListener
            if (!mobileTypePendingLayout.add(key)) return@OnGlobalLayoutListener
            rootView.postOnAnimation {
                mobileTypePendingLayout.remove(key)
                val state = mobileTypeViews[key] ?: return@postOnAnimation
                applyMobileTypeVisibility(state)
            }
        }
        mobileTypeLayoutListeners[key] = WeakReference(listener)
        runCatching {
            root.viewTreeObserver.addOnGlobalLayoutListener(listener)
        }
        root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit

            override fun onViewDetachedFromWindow(v: View) {
                mobileTypeViews.remove(key)
                mobileTypeLayoutListeners.remove(key)?.get()?.let { oldListener ->
                    runCatching {
                        if (root.viewTreeObserver.isAlive) {
                            root.viewTreeObserver.removeOnGlobalLayoutListener(oldListener)
                        }
                    }
                }
            }
        })
    }

    private fun startMobileStatusMonitor(context: Context) {
        if (mobileStatusMonitorStarted) return
        synchronized(this) {
            if (mobileStatusMonitorStarted) return
            mobileStatusMonitorStarted = true
            val appContext = context.applicationContext ?: context
            val handler = Handler(Looper.getMainLooper())
            mobileStatusNetworkState = mobileStatusNetworkState.copy(
                mobileDataStateKnown = true,
                mobileDataEnabled = readMobileDataEnabled(appContext),
                wifiConnected = readWifiConnected(appContext),
                activeDataSubId = SubscriptionManager.getDefaultDataSubscriptionId()
            )

            runCatching {
                appContext.contentResolver.registerContentObserver(
                    Settings.Global.getUriFor("mobile_data"),
                    false,
                    object : ContentObserver(handler) {
                        override fun onChange(selfChange: Boolean) {
                            mobileStatusNetworkState = mobileStatusNetworkState.copy(
                                mobileDataStateKnown = true,
                                mobileDataEnabled = readMobileDataEnabled(appContext),
                                activeDataSubId = SubscriptionManager.getDefaultDataSubscriptionId()
                            )
                            refreshMobileTypeViews()
                        }
                    }
                )
            }
            listOf("multi_sim_data_call", "oplus_multi_sim_data_call").forEach { key ->
                runCatching {
                    appContext.contentResolver.registerContentObserver(
                        Settings.Global.getUriFor(key),
                        false,
                        object : ContentObserver(handler) {
                            override fun onChange(selfChange: Boolean) {
                                mobileStatusNetworkState = mobileStatusNetworkState.copy(
                                    activeDataSubId = SubscriptionManager.getDefaultDataSubscriptionId()
                                )
                                refreshMobileTypeViews()
                            }
                        }
                    )
                }
            }
            listOf(
                "oost_status_mobile_type",
                "oost_status_mobile_type_hide_data_off",
                "oost_status_mobile_type_hide_wifi"
            ).forEach { key ->
                runCatching {
                    appContext.contentResolver.registerContentObserver(
                        Settings.Global.getUriFor(key),
                        false,
                        object : ContentObserver(handler) {
                            override fun onChange(selfChange: Boolean) {
                                refreshMobileTypeViews()
                            }
                        }
                    )
                }
            }
            runCatching {
                val connectivityManager =
                    appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                        ?: return@runCatching
                val request = NetworkRequest.Builder()
                    .clearCapabilities()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()
                connectivityManager.registerNetworkCallback(
                    request,
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            missingWifiNetworks.remove(network)
                            mobileStatusNetworkState = mobileStatusNetworkState.copy(
                                wifiConnected = readWifiConnected(appContext)
                            )
                            refreshMobileTypeViews()
                        }

                        override fun onLost(network: Network) {
                            missingWifiNetworks.add(network)
                            mobileStatusNetworkState = mobileStatusNetworkState.copy(
                                wifiConnected = readWifiConnected(appContext)
                            )
                            refreshMobileTypeViews()
                        }

                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities
                        ) {
                            missingWifiNetworks.remove(network)
                            mobileStatusNetworkState = mobileStatusNetworkState.copy(
                                wifiConnected = readWifiConnected(appContext)
                            )
                            refreshMobileTypeViews()
                        }
                    },
                    handler
                )
            }
            startActiveDataSubscriptionMonitor(appContext)
            log("SystemUI mobile type status monitor started")
        }
    }

    private fun startActiveDataSubscriptionMonitor(context: Context) {
        if (mobileSubIdsHooked) return
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return
        val callback = object : TelephonyCallback(), TelephonyCallback.ActiveDataSubscriptionIdListener {
            override fun onActiveDataSubscriptionIdChanged(subId: Int) {
                mobileSimState = mobileSimState.copy(dataSubId = subId)
                mobileStatusNetworkState = mobileStatusNetworkState.copy(activeDataSubId = subId)
                scheduleMobileIconRebind()
                refreshMobileTypeViews()
            }
        }
        runCatching {
            telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
            mobileSubIdsHooked = true
        }
    }

    private fun buildMobileSimState(controller: Any?, subIds: List<Int>): MobileSimState {
        val context = findContext(controller)
        val subscriptionManager = context?.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        var sim1SubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID
        var sim2SubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID
        subIds.forEach { subId ->
            val slotIndex = runCatching {
                subscriptionManager?.getActiveSubscriptionInfo(subId)?.simSlotIndex
            }.getOrNull()
            when (slotIndex) {
                0 -> sim1SubId = subId
                1 -> sim2SubId = subId
                null -> {
                    val oldState = mobileSimState
                    when (subId) {
                        oldState.sim1SubId -> sim1SubId = subId
                        oldState.sim2SubId -> sim2SubId = subId
                    }
                }
            }
        }
        return MobileSimState(
            sim1SubId = sim1SubId,
            sim2SubId = sim2SubId,
            dataSubId = SubscriptionManager.getDefaultDataSubscriptionId()
        )
    }

    private fun findContext(source: Any?): Context? {
        if (source == null) return null
        return (source as? Context)
            ?: runCatching { XposedHelpers.getObjectField(source, "mContext") as? Context }.getOrNull()
            ?: runCatching { XposedHelpers.callMethod(source, "getContext") as? Context }.getOrNull()
    }

    private fun scheduleMobileIconRebind() {
        val controller = mobileIconController?.get() ?: return
        val subIds = mobileIconSubIds
        if (subIds.isEmpty()) return
        mobileRebindHandler.removeCallbacksAndMessages(mobileRebindToken)
        mobileRebindHandler.postAtTime(
            {
                runCatching {
                    XposedHelpers.callMethod(controller, "setNewMobileIconSubIds", subIds)
                }.onFailure { throwable ->
                    log("SystemUI mobile type rebind failed: ${throwable.message}")
                }
            },
            mobileRebindToken,
            android.os.SystemClock.uptimeMillis() + 100L
        )
    }

    private fun updateActiveMobileDataStateMonitor(context: Context, subId: Int) {
        if (subId == mobileDataStateSubId && mobileDataStateCallback != null) return
        runCatching {
            mobileDataStateCallback?.let { callback ->
                mobileDataStateTelephonyManager?.unregisterTelephonyCallback(callback)
            }
        }
        mobileDataStateSubId = subId
        mobileDataStateCallback = null
        mobileDataStateTelephonyManager = null
        if (!SubscriptionManager.isValidSubscriptionId(subId)) return
        val baseManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return
        val manager = runCatching { baseManager.createForSubscriptionId(subId) }.getOrNull() ?: return
        val callback = object : TelephonyCallback(), TelephonyCallback.UserMobileDataStateListener {
            override fun onUserMobileDataStateChanged(enabled: Boolean) {
                if (mobileDataStateSubId != subId) return
                mobileStatusNetworkState = mobileStatusNetworkState.copy(
                    mobileDataEnabled = enabled,
                    mobileDataStateKnown = true
                )
                refreshMobileTypeViews()
            }
        }
        runCatching {
            manager.registerTelephonyCallback(context.mainExecutor, callback)
            mobileDataStateTelephonyManager = manager
            mobileDataStateCallback = callback
            mobileStatusNetworkState = mobileStatusNetworkState.copy(
                mobileDataEnabled = readMobileDataEnabled(context, subId),
                mobileDataStateKnown = true
            )
        }
    }

    private fun refreshMobileTypeViews() {
        mobileTypeViews.entries.removeIf { (_, state) ->
            val root = state.root.get()
            val type = state.type.get()
            if (root == null || type == null) {
                true
            } else {
                root.post { applyMobileTypeVisibility(state) }
                false
            }
        }
    }

    private fun applyMobileTypeVisibility(state: MobileTypeViewState) {
        val root = state.root.get() ?: return
        val signal = state.signal.get() ?: return
        val type = state.type.get() ?: return
        val dataActivity = state.dataActivity?.get()
        val enabled = LspConfig.isStatusMobileTypeEnabledXposed()
        val networkState = mobileStatusNetworkState
        val activeDataSubId = currentActiveDataSubId()
        updateActiveMobileDataStateMonitor(root.context.applicationContext ?: root.context, activeDataSubId)
        val hideByInactiveSub = enabled &&
            state.subscriptionId != null &&
            mobileSimState.dualSim &&
            SubscriptionManager.isValidSubscriptionId(activeDataSubId) &&
            state.subscriptionId != activeDataSubId
        val hideByData = enabled &&
            LspConfig.isStatusMobileTypeHideDataOffEnabledXposed() &&
            networkState.mobileDataStateKnown &&
            !networkState.mobileDataEnabled
        val hideByWifi = enabled &&
            LspConfig.isStatusMobileTypeHideWifiEnabledXposed() &&
            networkState.wifiConnected
        val targetVisibility = if (enabled && (hideByInactiveSub || hideByData || hideByWifi)) {
            View.GONE
        } else {
            View.VISIBLE
        }
        if (type.visibility != targetVisibility) {
            type.visibility = targetVisibility
            if (targetVisibility == View.VISIBLE) {
                type.requestLayout()
            }
        }
        if (targetVisibility == View.VISIBLE && type.alpha != 1f) {
            type.alpha = 1f
        }
        if (enabled && dataActivity != null && dataActivity.visibility != targetVisibility) {
            dataActivity.visibility = targetVisibility
            if (targetVisibility == View.VISIBLE) {
                dataActivity.requestLayout()
            }
        }
        applyMobileTypeLayout(false, state, root, signal, type)
        (root.parent as? View)?.requestLayout()
    }

    private fun findMobileTypeViewState(candidates: List<Any?>): MobileTypeViewState? {
        val candidateRoots = buildList {
            candidates.forEach { candidate ->
                when (candidate) {
                    is ViewGroup -> add(candidate)
                    is View -> (findNamedView(candidate, mobileRootViewNames) as? ViewGroup)?.let(::add)
                }
            }
        }
        val mobileRoot = candidateRoots.firstNotNullOfOrNull { root ->
            val namedRoot = findNamedView(root, mobileRootViewNames) as? ViewGroup
            namedRoot?.takeIf(::isMobileTypeRoot)
                ?: root.takeIf(::isMobileTypeRoot)
        } ?: return null
        val signal = findNamedView(mobileRoot, mobileSignalViewNames) ?: return null
        val type = findNamedView(mobileRoot, mobileTypeViewNames) ?: return null
        val dataActivity = findNamedView(mobileRoot, mobileDataActivityViewNames)
        val imageView = type as? ImageView
        val measuredTypeWidth = measureViewWidth(type).coerceAtLeast(0)
        return MobileTypeViewState(
            root = WeakReference(mobileRoot),
            signal = WeakReference(signal),
            type = WeakReference(type),
            dataActivity = dataActivity?.let(::WeakReference),
            subscriptionId = readSubscriptionId(candidates, mobileRoot),
            baseRootMinimumWidth = mobileRoot.minimumWidth,
            baseSignalTranslationY = signal.translationY,
            baseTypeAlpha = type.alpha,
            baseDataActivityVisibility = dataActivity?.visibility,
            baseTypeScaleType = imageView?.scaleType,
            measuredTypeWidth = measuredTypeWidth,
            signalContainer = null
        )
    }

    private fun isMobileTypeRoot(root: ViewGroup): Boolean {
        return findNamedView(root, mobileSignalViewNames) != null &&
            findNamedView(root, mobileTypeViewNames) != null
    }

    private fun currentActiveDataSubId(): Int {
        val stateSubId = mobileStatusNetworkState.activeDataSubId
        if (SubscriptionManager.isValidSubscriptionId(stateSubId)) return stateSubId
        return SubscriptionManager.getDefaultDataSubscriptionId()
    }

    private fun hasMultipleMobileSubIds(): Boolean {
        return mobileSimState.dualSim
    }

    private fun findNamedView(root: View, entryNames: Set<String>): View? {
        if (resourceEntryName(root) in entryNames) return root
        val group = root as? ViewGroup ?: return null
        for (index in 0 until group.childCount) {
            findNamedView(group.getChildAt(index), entryNames)?.let { return it }
        }
        return null
    }

    private fun resourceEntryName(view: View): String? {
        val id = view.id
        if (id == View.NO_ID) return null
        return runCatching {
            view.resources.getResourceEntryName(id)
        }.getOrNull()
    }

    private fun readSubscriptionId(candidates: List<Any?>, root: ViewGroup): Int? {
        (candidates + root).forEach { source ->
            if (source == null) return@forEach
            val subId = runCatching {
                XposedHelpers.callMethod(source, "getSubscriptionId") as? Int
            }.getOrNull() ?: runCatching {
                XposedHelpers.callMethod(source, "getSubId") as? Int
            }.getOrNull()
            if (subId != null && SubscriptionManager.isValidSubscriptionId(subId)) {
                return subId
            }
        }
        return null
    }

    private fun measureViewWidth(view: View): Int {
        val layoutWidth = view.layoutParams?.width?.takeIf { it > 0 } ?: 0
        val drawableWidth = ((view as? ImageView)?.drawable?.intrinsicWidth ?: 0).coerceAtLeast(0)
        return maxOf(view.width, view.measuredWidth, layoutWidth, view.minimumWidth, drawableWidth)
    }

    private fun applyMobileTypeLayout(enabled: Boolean, state: MobileTypeViewState, root: ViewGroup, signal: View, type: View) {
        if (!enabled) {
            if (root.minimumWidth != state.baseRootMinimumWidth) {
                root.minimumWidth = state.baseRootMinimumWidth
                requestLayoutChain(root)
            }
            if (signal.translationY != state.baseSignalTranslationY) {
                signal.translationY = state.baseSignalTranslationY
            }
            if (type.alpha != state.baseTypeAlpha) {
                type.alpha = state.baseTypeAlpha
            }
            val imageView = type as? ImageView
            if (imageView != null && state.baseTypeScaleType != null && imageView.scaleType != state.baseTypeScaleType) {
                imageView.scaleType = state.baseTypeScaleType
            }
            state.dataActivity?.get()?.let { dataActivity ->
                val baseVisibility = state.baseDataActivityVisibility ?: View.VISIBLE
                if (dataActivity.visibility != baseVisibility) {
                    dataActivity.visibility = baseVisibility
                    dataActivity.requestLayout()
                }
            }
            state.signalContainer?.get()?.let(::restoreFrameLayoutParams)
            restoreFrameLayoutParams(signal)
            restoreFrameLayoutParams(type)
            return
        }

        val typeWidth = measureViewWidth(type).takeIf { it > 0 } ?: state.measuredTypeWidth
        if (typeWidth > 0) {
            state.measuredTypeWidth = typeWidth
        }

        val frameParent = type.parent as? FrameLayout ?: return
        var signalContainer: View? = signal
        while (signalContainer?.parent is View && signalContainer.parent != frameParent) {
            signalContainer = signalContainer.parent as? View
        }
        if (signalContainer?.parent != frameParent || signalContainer == type) return
        val signalWidth = measureViewWidth(signalContainer).takeIf { it > 0 } ?: return
        normalizeMobileSlotWidth(root, signalWidth, state.measuredTypeWidth, state.dataActivity?.get())
        val signalParams = signalContainer.layoutParams as? FrameLayout.LayoutParams ?: return
        val typeParams = type.layoutParams as? FrameLayout.LayoutParams ?: return
        state.signalContainer = WeakReference(signalContainer)
        rememberFrameLayoutParams(signalContainer)
        rememberFrameLayoutParams(type)
        var changed = false
        if (signalParams.gravity != android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL ||
            signalParams.marginStart != 0 ||
            signalParams.marginEnd != 0
        ) {
            signalParams.gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
            signalParams.marginStart = 0
            signalParams.marginEnd = 0
            signalContainer.layoutParams = signalParams
            changed = true
        }
        if (typeParams.gravity != android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL ||
            typeParams.marginStart != signalWidth ||
            typeParams.marginEnd != 0
        ) {
            typeParams.gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
            typeParams.marginStart = signalWidth
            typeParams.marginEnd = 0
            type.layoutParams = typeParams
            changed = true
        }
        (type as? ImageView)?.scaleType = ImageView.ScaleType.FIT_CENTER
        if (changed) {
            var parent: ViewGroup? = frameParent
            repeat(6) {
                val group = parent ?: return@repeat
                group.clipChildren = false
                group.clipToPadding = false
                parent = if (group == root) {
                    null
                } else {
                    group.parent as? ViewGroup
                }
            }
            frameParent.clipChildren = false
            frameParent.clipToPadding = false
            frameParent.requestLayout()
        }
    }

    private fun normalizeMobileSlotWidth(
        root: ViewGroup,
        signalWidth: Int,
        typeWidth: Int,
        dataActivity: View?
    ) {
        val dataActivityWidth = dataActivity
            ?.takeIf { it.visibility == View.VISIBLE }
            ?.let(::measureViewWidth)
            ?: 0
        val targetWidth = maxOf(
            root.minimumWidth,
            signalWidth + typeWidth,
            dataActivityWidth
        )
        val rootWidth = maxOf(root.rootView?.width ?: 0, root.width)
        if (targetWidth <= 0 || (rootWidth > 0 && targetWidth > rootWidth) || root.minimumWidth == targetWidth) {
            return
        }
        root.minimumWidth = targetWidth
        requestLayoutChain(root)
    }

    private fun requestLayoutChain(view: View) {
        var current: View? = view
        repeat(8) {
            current?.requestLayout()
            current = current?.parent as? View
        }
    }

    private fun rememberFrameLayoutParams(view: View) {
        if (mobileTypeLayoutParams.containsKey(System.identityHashCode(view))) return
        val params = view.layoutParams as? FrameLayout.LayoutParams ?: return
        mobileTypeLayoutParams[System.identityHashCode(view)] = FrameLayout.LayoutParams(params).apply {
            marginStart = params.marginStart
            marginEnd = params.marginEnd
        }
    }

    private fun restoreFrameLayoutParams(view: View) {
        val original = mobileTypeLayoutParams[System.identityHashCode(view)] ?: return
        val current = view.layoutParams as? FrameLayout.LayoutParams ?: return
        if (
            current.gravity == original.gravity &&
            current.marginStart == original.marginStart &&
            current.marginEnd == original.marginEnd
        ) {
            return
        }
        view.layoutParams = FrameLayout.LayoutParams(original).apply {
            marginStart = original.marginStart
            marginEnd = original.marginEnd
        }
    }

    private fun readMobileDataEnabled(context: Context, subId: Int = currentActiveDataSubId()): Boolean {
        runCatching {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return@runCatching
            val scopedManager = if (SubscriptionManager.isValidSubscriptionId(subId)) {
                runCatching { telephonyManager.createForSubscriptionId(subId) }.getOrNull()
            } else {
                null
            } ?: telephonyManager
            return scopedManager.isDataEnabled
        }
        return runCatching {
            Settings.Global.getInt(context.contentResolver, "mobile_data", 1) == 1
        }.getOrDefault(true)
    }

    private fun readWifiConnected(context: Context): Boolean {
        return runCatching {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return@runCatching false
            connectivityManager.allNetworks.any { network ->
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@any false
                if (!isPrimaryValidatedWifi(capabilities)) return@any false
                !missingWifiNetworks.contains(network)
            }
        }.getOrDefault(false)
    }

    private fun isPrimaryValidatedWifi(capabilities: NetworkCapabilities): Boolean {
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            return false
        }
        val transportInfo: TransportInfo? = capabilities.transportInfo
        if (transportInfo == null || transportInfo.javaClass.name != "android.net.wifi.WifiInfo") {
            return true
        }
        return runCatching {
            transportInfo.javaClass.getMethod("isPrimary").invoke(transportInfo) as? Boolean
        }.getOrNull() != false
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

    private fun log(message: String) {
        HookLog.i(TAG, message)
    }

    private fun addHookKeyIfAbsent(key: String): Boolean {
        return installedHookKeys.add(key)
    }
}
