package com.mi.fluidbox

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mi.fluidbox.lsp.LspConfig
import com.mi.fluidbox.lsp.LsposedScopeRequester
import com.mi.fluidbox.ui.common.AppLocale
import com.mi.fluidbox.ui.common.AppLogStore
import com.mi.fluidbox.ui.common.AppThemeColorSpec
import com.mi.fluidbox.ui.common.AppThemeKeyColor
import com.mi.fluidbox.ui.common.AppThemeMode
import com.mi.fluidbox.ui.common.AppThemePaletteStyle
import com.mi.fluidbox.ui.common.AssistantScreenOption
import com.mi.fluidbox.ui.common.RootAccessState
import com.mi.fluidbox.ui.common.applyAssistantScreenOption
import com.mi.fluidbox.ui.common.applyLauncherLayoutUnlocked
import com.mi.fluidbox.ui.common.applyPermissionMonitorVisibility
import com.mi.fluidbox.ui.common.queryAssistantScreenOption
import com.mi.fluidbox.ui.common.queryLauncherLayoutUnlocked
import com.mi.fluidbox.ui.common.queryPermissionMonitorVisibility
import com.mi.fluidbox.ui.common.queryRootAccess
import com.mi.fluidbox.ui.Root
import com.mi.fluidbox.ui.settings.AppUpdater
import java.util.Locale

private data class StartupRefreshState(
    val lspSnapshot: LspConfig.UiSnapshot,
    val rootGranted: Boolean,
    val permissionMonitorVisible: Boolean,
    val launcherLayoutUnlocked: Boolean,
    val assistantScreenOption: AssistantScreenOption
)

class MainActivity : ComponentActivity() {
    private companion object {
        const val PREF_SPECIAL_FEATURE_VISIBILITY_INITIALIZED = "special_feature_visibility_initialized"
        const val PREF_SHOW_CN_SPECIAL_FEATURES = "show_cn_special_features"
        const val PREF_SHOW_GLOBAL_SPECIAL_FEATURES = "show_global_special_features"
        const val PREF_HAPTIC_FEEDBACK_ENABLED = "haptic_feedback_enabled"
        const val PREF_HAPTIC_FEEDBACK_PLUS_ENABLED = "haptic_feedback_plus_enabled"
        const val PREF_BLUR_EFFECT_ENABLED = "blur_effect_enabled"
        const val PREF_POP_DIRECTION_FOLLOWS_SWIPE_EDGE = "pop_direction_follows_swipe_edge"
        const val PREF_SHOW_FPS_MONITOR = "show_fps_monitor"
        const val PREF_LIQUID_GLASS_BOTTOM_BAR = "liquid_glass_bottom_bar"
        const val PREF_ONE_CHINA_PRINCIPLE = "one_china_principle"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrapContext(newBase))
    }

    private fun recreateForLocaleChange() {
        window.decorView.post {
            recreate()
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogStore.i("App", "MainActivity onCreate")
        setContent {
            val prefs = remember {
                getSharedPreferences("fluidbox_prefs", MODE_PRIVATE)
            }
            val rootCheckScope = rememberCoroutineScope()
            var firstLaunchChecksPassed by rememberSaveable {
                mutableStateOf(prefs.getBoolean("first_launch_root_granted", false))
            }
            var showChinaSpecialFeatures by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_SHOW_CN_SPECIAL_FEATURES, false))
            }
            var showGlobalSpecialFeatures by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_SHOW_GLOBAL_SPECIAL_FEATURES, false))
            }
            var hapticFeedbackEnabled by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_HAPTIC_FEEDBACK_ENABLED, false))
            }
            var hapticFeedbackPlusEnabled by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_HAPTIC_FEEDBACK_PLUS_ENABLED, true))
            }
            var blurEffectEnabled by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_BLUR_EFFECT_ENABLED, false))
            }
            var popDirectionFollowsSwipeEdge by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_POP_DIRECTION_FOLLOWS_SWIPE_EDGE, false))
            }
            var showFpsMonitor by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_SHOW_FPS_MONITOR, false))
            }
            var liquidGlassBottomBarEnabled by rememberSaveable {
                mutableStateOf(
                    prefs.getBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR, false) &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                )
            }
            var oneChinaPrincipleEnabled by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_ONE_CHINA_PRINCIPLE, true))
            }
            var currentTab by rememberSaveable {
                mutableIntStateOf(0)
            }
            var appLanguageTag by rememberSaveable {
                mutableStateOf(AppLocale.getSelectedLanguageTag(this@MainActivity))
            }
            var appThemeMode by rememberSaveable {
                mutableStateOf(AppThemeMode.get(this@MainActivity))
            }
            var appThemeKeyColor by rememberSaveable {
                mutableStateOf(AppThemeKeyColor.get(this@MainActivity))
            }
            var appThemePaletteStyle by rememberSaveable {
                mutableIntStateOf(AppThemePaletteStyle.get(this@MainActivity))
            }
            var appThemeColorSpec by rememberSaveable {
                mutableIntStateOf(AppThemeColorSpec.get(this@MainActivity))
            }
            val initialLspConfig = remember {
                LspConfig.readCachedUiSnapshot(this@MainActivity)
            }
            var nativeNotifyIconEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.nativeNotifyIconEnabled)
            }
            var nativeNotificationBubblesEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.nativeNotificationBubblesEnabled)
            }
            var statusMobileTypeEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.statusMobileTypeEnabled)
            }
            var statusMobileTypeHideDataOffEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.statusMobileTypeHideDataOffEnabled)
            }
            var statusMobileTypeHideWifiEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.statusMobileTypeHideWifiEnabled)
            }
            var systemUiHideQsEditEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.systemUiHideQsEditEnabled)
            }
            var systemUiHideQsSettingsEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.systemUiHideQsSettingsEnabled)
            }
            var systemUiHideQsTopCarrierEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.systemUiHideQsTopCarrierEnabled)
            }
            var systemUiHideQsMoreEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.systemUiHideQsMoreEnabled)
            }
            var systemUiForceNativeClipboardOverlayEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.systemUiForceNativeClipboardOverlayEnabled)
            }
            var settingsForceGoogleEntryEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.settingsForceGoogleEntryEnabled)
            }
            var extremeRefresh165Enabled by rememberSaveable {
                mutableStateOf(initialLspConfig.extremeRefresh165Enabled)
            }
            var permissionMonitorVisible by rememberSaveable {
                mutableStateOf(false)
            }
            var launcherLayoutUnlocked by rememberSaveable {
                mutableStateOf(false)
            }
            var assistantScreenOption by rememberSaveable {
                mutableStateOf(AssistantScreenOption.Default)
            }
            var launcherRegionMode by rememberSaveable {
                mutableIntStateOf(initialLspConfig.launcherRegionMode)
            }
            var recentTaskRadiusEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.recentTaskRadiusEnabled)
            }
            var recentTaskRadiusDp by rememberSaveable {
                mutableIntStateOf(initialLspConfig.recentTaskRadiusDp)
            }
            var aodEnhanceEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.aodEnhanceEnabled)
            }
            var aodInitDarkBrightness by rememberSaveable {
                mutableIntStateOf(initialLspConfig.aodInitDarkBrightness)
            }
            var aodInitBrightBrightness by rememberSaveable {
                mutableIntStateOf(initialLspConfig.aodInitBrightBrightness)
            }
            var aodRunningBrightnessMultiplier by rememberSaveable {
                mutableStateOf(initialLspConfig.aodRunningBrightnessMultiplier)
            }
            var aodPanoramicSupportEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.aodPanoramicSupportEnabled)
            }
            var aodSettingsSwitchEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.aodSettingsSwitchEnabled)
            }
            var aodSingleClickBlockEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.aodSingleClickBlockEnabled)
            }
            var oosLocalizerEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.oosLocalizerEnabled)
            }
            var oosLocalizerConfigMode by rememberSaveable {
                mutableIntStateOf(initialLspConfig.oosLocalizerConfigMode)
            }
            var oosLocalizerRegion by rememberSaveable {
                mutableStateOf(initialLspConfig.oosLocalizerRegion)
            }
            var oosLocalizerLocale by rememberSaveable {
                mutableStateOf(initialLspConfig.oosLocalizerLocale)
            }
            var oosLocalizerModel by rememberSaveable {
                mutableStateOf(initialLspConfig.oosLocalizerModel)
            }
            var assistantPowerMode by rememberSaveable {
                mutableIntStateOf(initialLspConfig.assistantPowerMode)
            }
            var assistantGestureCircleEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.assistantGestureCircleEnabled)
            }
            var settingsEffectsReady by remember {
                mutableStateOf(false)
            }
            var resumeRefreshEnabled by remember {
                mutableStateOf(false)
            }
            fun applyLspConfigSnapshot(snapshot: LspConfig.UiSnapshot) {
                nativeNotifyIconEnabled = snapshot.nativeNotifyIconEnabled
                nativeNotificationBubblesEnabled = snapshot.nativeNotificationBubblesEnabled
                extremeRefresh165Enabled = snapshot.extremeRefresh165Enabled
                launcherRegionMode = snapshot.launcherRegionMode
                recentTaskRadiusEnabled = snapshot.recentTaskRadiusEnabled
                recentTaskRadiusDp = snapshot.recentTaskRadiusDp
                aodEnhanceEnabled = snapshot.aodEnhanceEnabled
                aodInitDarkBrightness = snapshot.aodInitDarkBrightness
                aodInitBrightBrightness = snapshot.aodInitBrightBrightness
                aodRunningBrightnessMultiplier = snapshot.aodRunningBrightnessMultiplier
                aodPanoramicSupportEnabled = snapshot.aodPanoramicSupportEnabled
                aodSettingsSwitchEnabled = snapshot.aodSettingsSwitchEnabled
                aodSingleClickBlockEnabled = snapshot.aodSingleClickBlockEnabled
                statusMobileTypeEnabled = snapshot.statusMobileTypeEnabled
                statusMobileTypeHideDataOffEnabled = snapshot.statusMobileTypeHideDataOffEnabled
                statusMobileTypeHideWifiEnabled = snapshot.statusMobileTypeHideWifiEnabled
                systemUiHideQsEditEnabled = snapshot.systemUiHideQsEditEnabled
                systemUiHideQsSettingsEnabled = snapshot.systemUiHideQsSettingsEnabled
                systemUiHideQsTopCarrierEnabled = snapshot.systemUiHideQsTopCarrierEnabled
                systemUiHideQsMoreEnabled = snapshot.systemUiHideQsMoreEnabled
                systemUiForceNativeClipboardOverlayEnabled = snapshot.systemUiForceNativeClipboardOverlayEnabled
                settingsForceGoogleEntryEnabled = snapshot.settingsForceGoogleEntryEnabled
                oosLocalizerEnabled = snapshot.oosLocalizerEnabled
                oosLocalizerConfigMode = snapshot.oosLocalizerConfigMode
                oosLocalizerRegion = snapshot.oosLocalizerRegion
                oosLocalizerLocale = snapshot.oosLocalizerLocale
                oosLocalizerModel = snapshot.oosLocalizerModel
                assistantPowerMode = snapshot.assistantPowerMode
                assistantGestureCircleEnabled = snapshot.assistantGestureCircleEnabled
            }
            LaunchedEffect(Unit) {
                withFrameNanos { }
                withContext(Dispatchers.IO) {
                    AppLogStore.initialize(applicationContext)
                }
                if (!prefs.getBoolean(PREF_SPECIAL_FEATURE_VISIBILITY_INITIALIZED, false)) {
                    val isChinaMainland = withContext(Dispatchers.IO) {
                        detectRegionCode() == "CN"
                    }
                    prefs.edit()
                        .putBoolean(PREF_SHOW_CN_SPECIAL_FEATURES, isChinaMainland)
                        .putBoolean(PREF_SHOW_GLOBAL_SPECIAL_FEATURES, !isChinaMainland)
                        .putBoolean(PREF_SPECIAL_FEATURE_VISIBILITY_INITIALIZED, true)
                        .apply()
                    showChinaSpecialFeatures = isChinaMainland
                    showGlobalSpecialFeatures = !isChinaMainland
                }
                AppLogStore.i("App", "MainActivity started")
                delay(1_200)
                val startupState = withContext(Dispatchers.IO) {
                    LsposedScopeRequester.initialize(this@MainActivity)
                    val currentRootAccess = queryRootAccess(this@MainActivity)
                    StartupRefreshState(
                        lspSnapshot = LspConfig.readSyncedUiSnapshot(this@MainActivity),
                        rootGranted = currentRootAccess.state == RootAccessState.Granted,
                        permissionMonitorVisible = queryPermissionMonitorVisibility(),
                        launcherLayoutUnlocked = queryLauncherLayoutUnlocked(),
                        assistantScreenOption = queryAssistantScreenOption()
                    )
                }
                applyLspConfigSnapshot(startupState.lspSnapshot)
                if (firstLaunchChecksPassed != startupState.rootGranted) {
                    firstLaunchChecksPassed = startupState.rootGranted
                }
                permissionMonitorVisible = startupState.permissionMonitorVisible
                launcherLayoutUnlocked = startupState.launcherLayoutUnlocked
                assistantScreenOption = startupState.assistantScreenOption
                resumeRefreshEnabled = true
                settingsEffectsReady = true
                rootCheckScope.launch {
                    delay(2_000)
                    withContext(Dispatchers.IO) {
                        LspConfig.syncTogglesForBoot(this@MainActivity)
                    }
                }
                rootCheckScope.launch {
                    delay(5_000)
                    withContext(Dispatchers.IO) {
                        AppUpdater.runAutomaticSilentUpdate(this@MainActivity)
                    }
                }
            }
            DisposableEffect(resumeRefreshEnabled) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME && resumeRefreshEnabled) {
                        rootCheckScope.launch {
                            val refreshState = withContext(Dispatchers.IO) {
                                val currentRootAccess = queryRootAccess(this@MainActivity)
                                StartupRefreshState(
                                    lspSnapshot = LspConfig.readSyncedUiSnapshot(this@MainActivity),
                                    rootGranted = currentRootAccess.state == RootAccessState.Granted,
                                    permissionMonitorVisible = queryPermissionMonitorVisibility(),
                                    launcherLayoutUnlocked = queryLauncherLayoutUnlocked(),
                                    assistantScreenOption = queryAssistantScreenOption()
                                )
                            }
                            applyLspConfigSnapshot(refreshState.lspSnapshot)
                            firstLaunchChecksPassed = refreshState.rootGranted
                            permissionMonitorVisible = refreshState.permissionMonitorVisible
                            launcherLayoutUnlocked = refreshState.launcherLayoutUnlocked
                            assistantScreenOption = refreshState.assistantScreenOption
                        }
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }
            LaunchedEffect(showChinaSpecialFeatures) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit()
                    .putBoolean(PREF_SHOW_CN_SPECIAL_FEATURES, showChinaSpecialFeatures)
                    .apply()
                AppLogStore.i("Settings", "Show China special features: $showChinaSpecialFeatures")
            }
            LaunchedEffect(showGlobalSpecialFeatures) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit()
                    .putBoolean(PREF_SHOW_GLOBAL_SPECIAL_FEATURES, showGlobalSpecialFeatures)
                    .apply()
                AppLogStore.i("Settings", "Show global special features: $showGlobalSpecialFeatures")
            }
            LaunchedEffect(hapticFeedbackEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit()
                    .putBoolean(PREF_HAPTIC_FEEDBACK_ENABLED, hapticFeedbackEnabled)
                    .apply()
                AppLogStore.i("Settings", "Haptic feedback: $hapticFeedbackEnabled")
            }
            LaunchedEffect(hapticFeedbackPlusEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit()
                    .putBoolean(PREF_HAPTIC_FEEDBACK_PLUS_ENABLED, hapticFeedbackPlusEnabled)
                    .apply()
                AppLogStore.i("Settings", "ColorOS-style vibration: $hapticFeedbackPlusEnabled")
            }
            LaunchedEffect(blurEffectEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit()
                    .putBoolean(PREF_BLUR_EFFECT_ENABLED, blurEffectEnabled)
                    .apply()
                AppLogStore.i("Settings", "Blur effect: $blurEffectEnabled")
            }
            LaunchedEffect(popDirectionFollowsSwipeEdge) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit()
                    .putBoolean(PREF_POP_DIRECTION_FOLLOWS_SWIPE_EDGE, popDirectionFollowsSwipeEdge)
                    .apply()
                AppLogStore.i("Settings", "Pop follows swipe edge: $popDirectionFollowsSwipeEdge")
            }
            LaunchedEffect(showFpsMonitor) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit()
                    .putBoolean(PREF_SHOW_FPS_MONITOR, showFpsMonitor)
                    .apply()
                AppLogStore.i("Settings", "Show FPS monitor: $showFpsMonitor")
            }
            LaunchedEffect(liquidGlassBottomBarEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit()
                    .putBoolean(PREF_LIQUID_GLASS_BOTTOM_BAR, liquidGlassBottomBarEnabled)
                    .apply()
                AppLogStore.i("Settings", "LiquidGlass bottom bar: $liquidGlassBottomBarEnabled")
            }
            LaunchedEffect(oneChinaPrincipleEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit()
                    .putBoolean(PREF_ONE_CHINA_PRINCIPLE, oneChinaPrincipleEnabled)
                    .apply()
                AppLogStore.i("Settings", "One China principle: $oneChinaPrincipleEnabled")
            }
            LaunchedEffect(firstLaunchChecksPassed) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit().putBoolean("first_launch_root_granted", firstLaunchChecksPassed).apply()
                AppLogStore.i("FirstLaunch", "First launch checks passed: $firstLaunchChecksPassed")
            }
            LaunchedEffect(nativeNotifyIconEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setNativeNotifyIconEnabled(this@MainActivity, nativeNotifyIconEnabled)
                }
                AppLogStore.i("NativeNotifyIcon", "Native notify icon toggle: $nativeNotifyIconEnabled")
            }
            LaunchedEffect(nativeNotificationBubblesEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setNativeNotificationBubblesEnabled(
                        this@MainActivity,
                        nativeNotificationBubblesEnabled
                    )
                }
                AppLogStore.i(
                    "NativeNotificationBubbles",
                    "Native notification bubbles toggle: $nativeNotificationBubblesEnabled"
                )
            }
            LaunchedEffect(statusMobileTypeEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setStatusMobileTypeEnabled(this@MainActivity, statusMobileTypeEnabled)
                }
                AppLogStore.i("StatusMobileType", "Mobile type toggle: $statusMobileTypeEnabled")
            }
            LaunchedEffect(statusMobileTypeHideDataOffEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setStatusMobileTypeHideDataOffEnabled(
                        this@MainActivity,
                        statusMobileTypeHideDataOffEnabled
                    )
                }
                AppLogStore.i(
                    "StatusMobileType",
                    "Mobile type hide when data off: $statusMobileTypeHideDataOffEnabled"
                )
            }
            LaunchedEffect(statusMobileTypeHideWifiEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setStatusMobileTypeHideWifiEnabled(
                        this@MainActivity,
                        statusMobileTypeHideWifiEnabled
                    )
                }
                AppLogStore.i(
                    "StatusMobileType",
                    "Mobile type hide on Wi-Fi: $statusMobileTypeHideWifiEnabled"
                )
            }
            LaunchedEffect(systemUiHideQsEditEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setSystemUiHideQsEditEnabled(this@MainActivity, systemUiHideQsEditEnabled)
                }
                AppLogStore.i("SystemUI", "Hide QS edit entry: $systemUiHideQsEditEnabled")
            }
            LaunchedEffect(systemUiHideQsSettingsEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setSystemUiHideQsSettingsEnabled(this@MainActivity, systemUiHideQsSettingsEnabled)
                }
                AppLogStore.i("SystemUI", "Hide QS settings button: $systemUiHideQsSettingsEnabled")
            }
            LaunchedEffect(systemUiHideQsTopCarrierEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setSystemUiHideQsTopCarrierEnabled(this@MainActivity, systemUiHideQsTopCarrierEnabled)
                }
                AppLogStore.i("SystemUI", "Hide QS top carrier: $systemUiHideQsTopCarrierEnabled")
            }
            LaunchedEffect(systemUiHideQsMoreEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setSystemUiHideQsMoreEnabled(this@MainActivity, systemUiHideQsMoreEnabled)
                }
                AppLogStore.i("SystemUI", "Hide QS more entry: $systemUiHideQsMoreEnabled")
            }
            LaunchedEffect(systemUiForceNativeClipboardOverlayEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setSystemUiForceNativeClipboardOverlayEnabled(
                        this@MainActivity,
                        systemUiForceNativeClipboardOverlayEnabled
                    )
                }
                AppLogStore.i(
                    "SystemUI",
                    "Force native clipboard overlay: $systemUiForceNativeClipboardOverlayEnabled"
                )
            }
            LaunchedEffect(settingsForceGoogleEntryEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setSettingsForceGoogleEntryEnabled(
                        this@MainActivity,
                        settingsForceGoogleEntryEnabled
                    )
                }
                AppLogStore.i("SettingsHook", "Force Google entry: $settingsForceGoogleEntryEnabled")
            }
            LaunchedEffect(extremeRefresh165Enabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setExtremeRefresh165Enabled(this@MainActivity, extremeRefresh165Enabled)
                }
                AppLogStore.i("ExtremeRefresh165", "165Hz extreme refresh toggle: $extremeRefresh165Enabled")
            }
            LaunchedEffect(launcherRegionMode) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setLauncherRegionMode(this@MainActivity, launcherRegionMode)
                }
                AppLogStore.i("LauncherRegion", "Launcher region mode: $launcherRegionMode")
            }
            LaunchedEffect(recentTaskRadiusEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setRecentTaskRadiusEnabled(this@MainActivity, recentTaskRadiusEnabled)
                }
                AppLogStore.i("RecentTaskRadius", "Recent task radius toggle: $recentTaskRadiusEnabled")
            }
            LaunchedEffect(recentTaskRadiusDp) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setRecentTaskRadiusDp(this@MainActivity, recentTaskRadiusDp)
                }
                AppLogStore.i("RecentTaskRadius", "Recent task radius dp: $recentTaskRadiusDp")
            }
            LaunchedEffect(aodEnhanceEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setAodEnhanceEnabled(this@MainActivity, aodEnhanceEnabled)
                }
                AppLogStore.i("AodEnhance", "AOD enhance toggle: $aodEnhanceEnabled")
            }
            LaunchedEffect(aodInitDarkBrightness) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setAodInitDarkBrightness(this@MainActivity, aodInitDarkBrightness)
                }
                AppLogStore.i("AodEnhance", "AOD init dark brightness: $aodInitDarkBrightness")
            }
            LaunchedEffect(aodInitBrightBrightness) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setAodInitBrightBrightness(this@MainActivity, aodInitBrightBrightness)
                }
                AppLogStore.i("AodEnhance", "AOD init bright brightness: $aodInitBrightBrightness")
            }
            LaunchedEffect(aodRunningBrightnessMultiplier) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setAodRunningBrightnessMultiplier(
                        this@MainActivity,
                        aodRunningBrightnessMultiplier
                    )
                }
                AppLogStore.i(
                    "AodEnhance",
                    "AOD running brightness multiplier: $aodRunningBrightnessMultiplier"
                )
            }
            LaunchedEffect(aodPanoramicSupportEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setAodPanoramicSupportEnabled(
                        this@MainActivity,
                        aodPanoramicSupportEnabled
                    )
                }
                AppLogStore.i(
                    "AodEnhance",
                    "AOD panoramic support: $aodPanoramicSupportEnabled"
                )
            }
            LaunchedEffect(aodSettingsSwitchEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setAodSettingsSwitchEnabled(
                        this@MainActivity,
                        aodSettingsSwitchEnabled
                    )
                }
                AppLogStore.i(
                    "AodEnhance",
                    "AOD settings switch support: $aodSettingsSwitchEnabled"
                )
            }
            LaunchedEffect(aodSingleClickBlockEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setAodSingleClickBlockEnabled(
                        this@MainActivity,
                        aodSingleClickBlockEnabled
                    )
                }
                AppLogStore.i(
                    "AodEnhance",
                    "AOD single-click wake block: $aodSingleClickBlockEnabled"
                )
            }
            LaunchedEffect(oosLocalizerEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setOosLocalizerEnabled(this@MainActivity, oosLocalizerEnabled)
                }
                if (oosLocalizerEnabled) {
                    LsposedScopeRequester.requestRequiredScopes()
                } else {
                    LsposedScopeRequester.removeOosLocalizerScopes(this@MainActivity)
                }
                AppLogStore.i("OosLocalizer", "OOS localizer toggle: $oosLocalizerEnabled")
            }
            LaunchedEffect(oosLocalizerConfigMode) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setOosLocalizerConfigMode(this@MainActivity, oosLocalizerConfigMode)
                }
                AppLogStore.i("OosLocalizer", "Global localizer config mode: $oosLocalizerConfigMode")
            }
            LaunchedEffect(oosLocalizerRegion) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setOosLocalizerRegion(this@MainActivity, oosLocalizerRegion)
                }
                AppLogStore.i("OosLocalizer", "Global localizer region: $oosLocalizerRegion")
            }
            LaunchedEffect(oosLocalizerLocale) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setOosLocalizerLocale(this@MainActivity, oosLocalizerLocale)
                }
                AppLogStore.i("OosLocalizer", "Global localizer locale: $oosLocalizerLocale")
            }
            LaunchedEffect(oosLocalizerModel) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setOosLocalizerModel(this@MainActivity, oosLocalizerModel)
                }
                AppLogStore.i("OosLocalizer", "Global localizer model: $oosLocalizerModel")
            }
            LaunchedEffect(assistantPowerMode) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setAssistantPowerMode(this@MainActivity, assistantPowerMode)
                }
                AppLogStore.i("Assistant", "Power long press mode: $assistantPowerMode")
            }
            LaunchedEffect(assistantGestureCircleEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setAssistantGestureCircleEnabled(
                        this@MainActivity,
                        assistantGestureCircleEnabled
                    )
                }
                AppLogStore.i("Assistant", "Gesture Circle to Search: $assistantGestureCircleEnabled")
            }

            Root(
                currentTab = currentTab,
                onTabChange = { currentTab = it },
                rootGranted = firstLaunchChecksPassed,
                showChinaSpecialFeatures = showChinaSpecialFeatures,
                onShowChinaSpecialFeaturesChange = { showChinaSpecialFeatures = it },
                showGlobalSpecialFeatures = showGlobalSpecialFeatures,
                onShowGlobalSpecialFeaturesChange = { showGlobalSpecialFeatures = it },
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                onHapticFeedbackEnabledChange = { enabled ->
                    hapticFeedbackEnabled = enabled
                    if (enabled) {
                        hapticFeedbackPlusEnabled = false
                    }
                },
                hapticFeedbackPlusEnabled = hapticFeedbackPlusEnabled,
                onHapticFeedbackPlusEnabledChange = { enabled ->
                    hapticFeedbackPlusEnabled = enabled
                    if (enabled) {
                        hapticFeedbackEnabled = false
                    }
                },
                blurEffectEnabled = blurEffectEnabled,
                onBlurEffectEnabledChange = { blurEffectEnabled = it },
                popDirectionFollowsSwipeEdge = popDirectionFollowsSwipeEdge,
                onPopDirectionFollowsSwipeEdgeChange = { popDirectionFollowsSwipeEdge = it },
                showFpsMonitor = showFpsMonitor,
                onShowFpsMonitorChange = { showFpsMonitor = it },
                liquidGlassBottomBarEnabled = liquidGlassBottomBarEnabled,
                onLiquidGlassBottomBarEnabledChange = { liquidGlassBottomBarEnabled = it },
                oneChinaPrincipleEnabled = oneChinaPrincipleEnabled,
                onOneChinaPrincipleEnabledChange = { oneChinaPrincipleEnabled = it },
                appLanguageTag = appLanguageTag,
                onAppLanguageChange = { languageTag ->
                    if (appLanguageTag != languageTag) {
                        appLanguageTag = languageTag
                        AppLocale.setSelectedLanguageTag(this@MainActivity, languageTag)
                        recreateForLocaleChange()
                    }
                },
                appThemeMode = appThemeMode,
                onAppThemeModeChange = { mode ->
                    appThemeMode = mode
                    AppThemeMode.set(this@MainActivity, mode)
                },
                appThemeKeyColor = appThemeKeyColor,
                onAppThemeKeyColorChange = { color ->
                    appThemeKeyColor = color
                    AppThemeKeyColor.set(this@MainActivity, color)
                },
                appThemePaletteStyle = appThemePaletteStyle,
                onAppThemePaletteStyleChange = { style ->
                    appThemePaletteStyle = style
                    AppThemePaletteStyle.set(this@MainActivity, style)
                },
                appThemeColorSpec = appThemeColorSpec,
                onAppThemeColorSpecChange = { spec ->
                    appThemeColorSpec = spec
                    AppThemeColorSpec.set(this@MainActivity, spec)
                },
                permissionMonitorVisible = permissionMonitorVisible,
                onPermissionMonitorVisibleChange = { enabled ->
                    permissionMonitorVisible = enabled
                    rootCheckScope.launch {
                        val result = applyPermissionMonitorVisibility(enabled)
                        permissionMonitorVisible = queryPermissionMonitorVisibility()
                        if (!result.success) {
                            AppLogStore.w(
                                "PermissionMonitor",
                                "Toggle apply failed: ${result.detail.orEmpty()}"
                            )
                        }
                    }
                },
                nativeNotifyIconEnabled = nativeNotifyIconEnabled,
                onNativeNotifyIconEnabledChange = { nativeNotifyIconEnabled = it },
                nativeNotificationBubblesEnabled = nativeNotificationBubblesEnabled,
                onNativeNotificationBubblesEnabledChange = { nativeNotificationBubblesEnabled = it },
                statusMobileTypeEnabled = statusMobileTypeEnabled,
                onStatusMobileTypeEnabledChange = { statusMobileTypeEnabled = it },
                statusMobileTypeHideDataOffEnabled = statusMobileTypeHideDataOffEnabled,
                onStatusMobileTypeHideDataOffEnabledChange = {
                    statusMobileTypeHideDataOffEnabled = it
                },
                statusMobileTypeHideWifiEnabled = statusMobileTypeHideWifiEnabled,
                onStatusMobileTypeHideWifiEnabledChange = { statusMobileTypeHideWifiEnabled = it },
                systemUiHideQsEditEnabled = systemUiHideQsEditEnabled,
                onSystemUiHideQsEditEnabledChange = { systemUiHideQsEditEnabled = it },
                systemUiHideQsSettingsEnabled = systemUiHideQsSettingsEnabled,
                onSystemUiHideQsSettingsEnabledChange = { systemUiHideQsSettingsEnabled = it },
                systemUiHideQsTopCarrierEnabled = systemUiHideQsTopCarrierEnabled,
                onSystemUiHideQsTopCarrierEnabledChange = { systemUiHideQsTopCarrierEnabled = it },
                systemUiHideQsMoreEnabled = systemUiHideQsMoreEnabled,
                onSystemUiHideQsMoreEnabledChange = { systemUiHideQsMoreEnabled = it },
                systemUiForceNativeClipboardOverlayEnabled = systemUiForceNativeClipboardOverlayEnabled,
                onSystemUiForceNativeClipboardOverlayEnabledChange = {
                    systemUiForceNativeClipboardOverlayEnabled = it
                },
                settingsForceGoogleEntryEnabled = settingsForceGoogleEntryEnabled,
                onSettingsForceGoogleEntryEnabledChange = { settingsForceGoogleEntryEnabled = it },
                extremeRefresh165Enabled = extremeRefresh165Enabled,
                onExtremeRefresh165EnabledChange = { extremeRefresh165Enabled = it },
                launcherLayoutUnlocked = launcherLayoutUnlocked,
                onLauncherLayoutUnlockedChange = { enabled ->
                    launcherLayoutUnlocked = enabled
                    rootCheckScope.launch {
                        val result = applyLauncherLayoutUnlocked(enabled)
                        launcherLayoutUnlocked = queryLauncherLayoutUnlocked()
                        if (!result.success) {
                            AppLogStore.w(
                                "LauncherLayout",
                                "Toggle apply failed: ${result.detail.orEmpty()}"
                            )
                        }
                    }
                },
                assistantScreenOption = assistantScreenOption,
                onAssistantScreenOptionChange = { option ->
                    assistantScreenOption = option
                    rootCheckScope.launch {
                        val result = applyAssistantScreenOption(option)
                        assistantScreenOption = queryAssistantScreenOption()
                        if (!result.success) {
                            AppLogStore.w(
                                "DesktopAssistant",
                                "Apply failed: ${result.detail.orEmpty()}"
                            )
                        }
                    }
                },
                launcherRegionMode = launcherRegionMode,
                onLauncherRegionModeChange = { launcherRegionMode = it },
                recentTaskRadiusEnabled = recentTaskRadiusEnabled,
                onRecentTaskRadiusEnabledChange = { recentTaskRadiusEnabled = it },
                recentTaskRadiusDp = recentTaskRadiusDp,
                onRecentTaskRadiusDpChange = { recentTaskRadiusDp = it },
                aodEnhanceEnabled = aodEnhanceEnabled,
                onAodEnhanceEnabledChange = { aodEnhanceEnabled = it },
                aodInitDarkBrightness = aodInitDarkBrightness,
                onAodInitDarkBrightnessChange = { aodInitDarkBrightness = it },
                aodInitBrightBrightness = aodInitBrightBrightness,
                onAodInitBrightBrightnessChange = { aodInitBrightBrightness = it },
                aodRunningBrightnessMultiplier = aodRunningBrightnessMultiplier,
                onAodRunningBrightnessMultiplierChange = { aodRunningBrightnessMultiplier = it },
                aodPanoramicSupportEnabled = aodPanoramicSupportEnabled,
                onAodPanoramicSupportEnabledChange = { aodPanoramicSupportEnabled = it },
                aodSettingsSwitchEnabled = aodSettingsSwitchEnabled,
                onAodSettingsSwitchEnabledChange = { aodSettingsSwitchEnabled = it },
                aodSingleClickBlockEnabled = aodSingleClickBlockEnabled,
                onAodSingleClickBlockEnabledChange = { aodSingleClickBlockEnabled = it },
                oosLocalizerEnabled = oosLocalizerEnabled,
                onOosLocalizerEnabledChange = { oosLocalizerEnabled = it },
                oosLocalizerConfigMode = oosLocalizerConfigMode,
                onOosLocalizerConfigModeChange = { oosLocalizerConfigMode = it },
                oosLocalizerRegion = oosLocalizerRegion,
                onOosLocalizerRegionChange = { oosLocalizerRegion = it },
                oosLocalizerLocale = oosLocalizerLocale,
                onOosLocalizerLocaleChange = { oosLocalizerLocale = it },
                oosLocalizerModel = oosLocalizerModel,
                onOosLocalizerModelChange = { oosLocalizerModel = it },
                assistantPowerMode = assistantPowerMode,
                onAssistantPowerModeChange = { assistantPowerMode = it },
                assistantGestureCircleEnabled = assistantGestureCircleEnabled,
                onAssistantGestureCircleEnabledChange = { assistantGestureCircleEnabled = it },
            )
        }
    }

    private fun detectRegionCode(): String {
        val propertyRegion = listOf(
            "ro.oplus.regionmark",
            "ro.oplus.region",
            "ro.vendor.oplus.regionmark",
            "persist.sys.oplus.region",
            "ro.product.locale.region"
        ).asSequence()
            .mapNotNull(::readSystemProperty)
            .map(String::trim)
            .firstOrNull { it.isNotEmpty() }
            ?.let(::normalizeRegionCode)

        if (propertyRegion != null) {
            return propertyRegion
        }

        return resources.configuration.locales[0]
            .country
            .ifBlank { "XX" }
            .uppercase(Locale.ROOT)
    }

    private fun normalizeRegionCode(raw: String): String? {
        val normalized = raw
            .replace('-', '_')
            .substringAfterLast('_')
            .uppercase(Locale.ROOT)

        val mapped = when (normalized) {
            "INDIA" -> "IN"
            "CHINA" -> "CN"
            "HONGKONG", "HONG_KONG" -> "HK"
            "TAIWAN" -> "TW"
            "GLOBAL", "ROW", "WW", "EUEX" -> "GLO"
            else -> normalized
        }

        return mapped.takeIf { it.length in 2..3 && it.all { ch -> ch in 'A'..'Z' } }
    }

    private fun readSystemProperty(key: String): String? {
        return runCatching {
            val cls = Class.forName("android.os.SystemProperties")
            val getMethod = cls.getMethod("get", String::class.java, String::class.java)
            (getMethod.invoke(null, key, "") as? String)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }
}
