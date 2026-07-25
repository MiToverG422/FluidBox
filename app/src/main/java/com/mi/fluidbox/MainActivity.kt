package com.mi.fluidbox

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mi.fluidbox.lsp.LspConfig
import com.mi.fluidbox.lsp.LsposedScopeRequester
import com.mi.fluidbox.ui.common.AppLocale
import com.mi.fluidbox.ui.common.AppLogStore
import com.mi.fluidbox.ui.common.RootAccessState
import com.mi.fluidbox.ui.common.UiStyleMode
import com.mi.fluidbox.ui.common.applyLauncherLayoutUnlocked
import com.mi.fluidbox.ui.common.applyPermissionMonitorVisibility
import com.mi.fluidbox.ui.common.queryLauncherLayoutUnlocked
import com.mi.fluidbox.ui.common.queryPermissionMonitorVisibility
import com.mi.fluidbox.ui.common.queryRootAccess
import com.mi.fluidbox.ui.md3e.Md3eRoot
import com.mi.fluidbox.ui.md3e.resolveMd3eColorScheme
import java.util.Locale

private data class StartupRefreshState(
    val lspSnapshot: LspConfig.UiSnapshot,
    val rootGranted: Boolean,
    val permissionMonitorVisible: Boolean,
    val launcherLayoutUnlocked: Boolean
)

class MainActivity : ComponentActivity() {
    private companion object {
        const val THEME_MODE_SYSTEM = 0
        const val THEME_MODE_LIGHT = 1
        const val THEME_MODE_DARK = 2
        const val PREF_SPECIAL_FEATURE_VISIBILITY_INITIALIZED = "special_feature_visibility_initialized"
        const val PREF_SHOW_CN_SPECIAL_FEATURES = "show_cn_special_features"
        const val PREF_SHOW_GLOBAL_SPECIAL_FEATURES = "show_global_special_features"
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
        setContent {
            val prefs = remember {
                getSharedPreferences("fluidbox_prefs", MODE_PRIVATE)
            }
            val rootCheckScope = rememberCoroutineScope()
            var firstLaunchChecksPassed by rememberSaveable {
                mutableStateOf(prefs.getBoolean("first_launch_root_granted", false))
            }
            var predictiveBackEnabled by rememberSaveable {
                mutableStateOf(prefs.getBoolean("predictive_back_enabled", true))
            }
            var showLogsTab by rememberSaveable {
                mutableStateOf(prefs.getBoolean("show_logs_tab", false))
            }
            var showBatteryTab by rememberSaveable {
                mutableStateOf(prefs.getBoolean("show_battery_tab", false))
            }
            var showChinaSpecialFeatures by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_SHOW_CN_SPECIAL_FEATURES, false))
            }
            var showGlobalSpecialFeatures by rememberSaveable {
                mutableStateOf(prefs.getBoolean(PREF_SHOW_GLOBAL_SPECIAL_FEATURES, false))
            }
            var showSwitchIcons by rememberSaveable {
                mutableStateOf(prefs.getBoolean("show_switch_icons", true))
            }
            var customMonetEnabled by rememberSaveable {
                mutableStateOf(prefs.getBoolean("custom_monet_enabled", false))
            }
            var customMonetSeedColor by rememberSaveable {
                mutableIntStateOf(prefs.getInt("custom_monet_seed", 0xFF4F6BED.toInt()))
            }
            var currentTab by rememberSaveable {
                mutableIntStateOf(0)
            }
            var themeMode by rememberSaveable {
                mutableIntStateOf(prefs.getInt("theme_mode", THEME_MODE_SYSTEM))
            }
            var appLanguageTag by rememberSaveable {
                mutableStateOf(AppLocale.getSelectedLanguageTag(this@MainActivity))
            }
            val initialLspConfig = remember {
                LspConfig.readCachedUiSnapshot(this@MainActivity)
            }
            var nativeNotifyIconEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.nativeNotifyIconEnabled)
            }
            var notificationBubbleBlurEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.notificationBubbleBlurEnabled)
            }
            var notificationBubbleBlurRadiusPx by rememberSaveable {
                mutableIntStateOf(initialLspConfig.notificationBubbleBlurRadiusPx)
            }
            var nativeNotificationBubblesEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.nativeNotificationBubblesEnabled)
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
            var doublePowerCustomEnabled by rememberSaveable {
                mutableStateOf(initialLspConfig.doublePowerCustomEnabled)
            }
            var doublePowerTargetPackage by rememberSaveable {
                mutableStateOf(initialLspConfig.doublePowerTargetPackage)
            }
            var doublePowerTargetActivity by rememberSaveable {
                mutableStateOf(initialLspConfig.doublePowerTargetActivity)
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
                notificationBubbleBlurEnabled = snapshot.notificationBubbleBlurEnabled
                notificationBubbleBlurRadiusPx = snapshot.notificationBubbleBlurRadiusPx
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
                oosLocalizerEnabled = snapshot.oosLocalizerEnabled
                doublePowerCustomEnabled = snapshot.doublePowerCustomEnabled
                doublePowerTargetPackage = snapshot.doublePowerTargetPackage
                doublePowerTargetActivity = snapshot.doublePowerTargetActivity
                assistantPowerMode = snapshot.assistantPowerMode
                assistantGestureCircleEnabled = snapshot.assistantGestureCircleEnabled
            }
            val reloadConfigurationFromPrefs = {
                predictiveBackEnabled = prefs.getBoolean("predictive_back_enabled", true)
                showLogsTab = prefs.getBoolean("show_logs_tab", false)
                showBatteryTab = prefs.getBoolean("show_battery_tab", false)
                showChinaSpecialFeatures = prefs.getBoolean(PREF_SHOW_CN_SPECIAL_FEATURES, false)
                showGlobalSpecialFeatures = prefs.getBoolean(PREF_SHOW_GLOBAL_SPECIAL_FEATURES, false)
                showSwitchIcons = prefs.getBoolean("show_switch_icons", true)
                customMonetEnabled = prefs.getBoolean("custom_monet_enabled", false)
                customMonetSeedColor = prefs.getInt("custom_monet_seed", 0xFF4F6BED.toInt())
                themeMode = prefs.getInt("theme_mode", THEME_MODE_SYSTEM)
                appLanguageTag = AppLocale.getSelectedLanguageTag(this@MainActivity)
                rootCheckScope.launch {
                    val refreshState = withContext(Dispatchers.IO) {
                        StartupRefreshState(
                            lspSnapshot = LspConfig.readSyncedUiSnapshot(this@MainActivity),
                            rootGranted = firstLaunchChecksPassed,
                            permissionMonitorVisible = queryPermissionMonitorVisibility(),
                            launcherLayoutUnlocked = queryLauncherLayoutUnlocked()
                        )
                    }
                    applyLspConfigSnapshot(refreshState.lspSnapshot)
                    permissionMonitorVisible = refreshState.permissionMonitorVisible
                    launcherLayoutUnlocked = refreshState.launcherLayoutUnlocked
                }
                Unit
            }
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                THEME_MODE_LIGHT -> false
                THEME_MODE_DARK -> true
                else -> systemDarkTheme
            }

            LaunchedEffect(Unit) {
                withFrameNanos { }
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
                val startupState = withContext(Dispatchers.IO) {
                    LsposedScopeRequester.initialize(this@MainActivity)
                    LspConfig.syncTogglesForBoot(this@MainActivity)
                    val currentRootAccess = queryRootAccess(this@MainActivity)
                    StartupRefreshState(
                        lspSnapshot = LspConfig.readSyncedUiSnapshot(this@MainActivity),
                        rootGranted = currentRootAccess.state == RootAccessState.Granted,
                        permissionMonitorVisible = queryPermissionMonitorVisibility(),
                        launcherLayoutUnlocked = queryLauncherLayoutUnlocked()
                    )
                }
                applyLspConfigSnapshot(startupState.lspSnapshot)
                if (firstLaunchChecksPassed != startupState.rootGranted) {
                    firstLaunchChecksPassed = startupState.rootGranted
                }
                permissionMonitorVisible = startupState.permissionMonitorVisible
                launcherLayoutUnlocked = startupState.launcherLayoutUnlocked
                resumeRefreshEnabled = true
                settingsEffectsReady = true
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
                                    launcherLayoutUnlocked = queryLauncherLayoutUnlocked()
                                )
                            }
                            applyLspConfigSnapshot(refreshState.lspSnapshot)
                            firstLaunchChecksPassed = refreshState.rootGranted
                            permissionMonitorVisible = refreshState.permissionMonitorVisible
                            launcherLayoutUnlocked = refreshState.launcherLayoutUnlocked
                        }
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }
            LaunchedEffect(predictiveBackEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit().putBoolean("predictive_back_enabled", predictiveBackEnabled).apply()
                AppLogStore.i("Settings", "Predictive back: $predictiveBackEnabled")
            }
            LaunchedEffect(showLogsTab) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit().putBoolean("show_logs_tab", showLogsTab).apply()
                AppLogStore.i("Settings", "Show logs tab: $showLogsTab")
                if (!showLogsTab && currentTab == 2) {
                    currentTab = 0
                }
            }
            LaunchedEffect(showBatteryTab) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit().putBoolean("show_battery_tab", showBatteryTab).apply()
                AppLogStore.i("Settings", "Show battery tab: $showBatteryTab")
                if (!showBatteryTab && currentTab == 4) {
                    currentTab = 0
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
            LaunchedEffect(showSwitchIcons) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit().putBoolean("show_switch_icons", showSwitchIcons).apply()
                AppLogStore.i("Settings", "Show switch icons: $showSwitchIcons")
            }
            LaunchedEffect(customMonetEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit().putBoolean("custom_monet_enabled", customMonetEnabled).apply()
                AppLogStore.i("Settings", "Custom Monet: $customMonetEnabled")
            }
            LaunchedEffect(customMonetSeedColor) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit().putInt("custom_monet_seed", customMonetSeedColor).apply()
                AppLogStore.i("Settings", "Monet seed changed: 0x${customMonetSeedColor.toUInt().toString(16)}")
            }
            LaunchedEffect(themeMode) {
                if (!settingsEffectsReady) return@LaunchedEffect
                prefs.edit().putInt("theme_mode", themeMode).apply()
                AppLogStore.i("Settings", "Theme mode: $themeMode")
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
            LaunchedEffect(notificationBubbleBlurEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setNotificationBubbleBlurEnabled(
                        this@MainActivity,
                        notificationBubbleBlurEnabled
                    )
                }
                AppLogStore.i(
                    "NotificationBubbleBlur",
                    "Notification bubble blur toggle: $notificationBubbleBlurEnabled"
                )
            }
            LaunchedEffect(notificationBubbleBlurRadiusPx) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setNotificationBubbleBlurRadiusPx(
                        this@MainActivity,
                        notificationBubbleBlurRadiusPx
                    )
                }
                AppLogStore.i(
                    "NotificationBubbleBlur",
                    "Notification bubble blur radius: ${notificationBubbleBlurRadiusPx}px"
                )
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
            LaunchedEffect(doublePowerCustomEnabled) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setDoublePowerCustomEnabled(this@MainActivity, doublePowerCustomEnabled)
                }
                AppLogStore.i("DoublePower", "Double power custom toggle: $doublePowerCustomEnabled")
            }
            LaunchedEffect(doublePowerTargetPackage) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setDoublePowerTargetPackage(this@MainActivity, doublePowerTargetPackage)
                }
                AppLogStore.i("DoublePower", "Double power target package: $doublePowerTargetPackage")
            }
            LaunchedEffect(doublePowerTargetActivity) {
                if (!settingsEffectsReady) return@LaunchedEffect
                withContext(Dispatchers.IO) {
                    LspConfig.setDoublePowerTargetActivity(this@MainActivity, doublePowerTargetActivity)
                }
                AppLogStore.i("DoublePower", "Double power target activity: $doublePowerTargetActivity")
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

            Md3eRoot(
                darkTheme = darkTheme,
                currentTab = currentTab,
                onTabChange = { currentTab = it },
                showLogsTab = showLogsTab,
                onShowLogsTabChange = { showLogsTab = it },
                showBatteryTab = showBatteryTab,
                onShowBatteryTabChange = { showBatteryTab = it },
                showChinaSpecialFeatures = showChinaSpecialFeatures,
                onShowChinaSpecialFeaturesChange = { showChinaSpecialFeatures = it },
                showGlobalSpecialFeatures = showGlobalSpecialFeatures,
                onShowGlobalSpecialFeaturesChange = { showGlobalSpecialFeatures = it },
                predictiveBackEnabled = predictiveBackEnabled,
                onPredictiveBackEnabledChange = { predictiveBackEnabled = it },
                customMonetEnabled = customMonetEnabled,
                onCustomMonetEnabledChange = { customMonetEnabled = it },
                customMonetSeedColor = customMonetSeedColor,
                onCustomMonetSeedColorChange = { customMonetSeedColor = it },
                themeMode = themeMode,
                onThemeModeChange = { themeMode = it },
                appLanguageTag = appLanguageTag,
                onAppLanguageChange = { languageTag ->
                    if (appLanguageTag != languageTag) {
                        appLanguageTag = languageTag
                        AppLocale.setSelectedLanguageTag(this@MainActivity, languageTag)
                        recreateForLocaleChange()
                    }
                },
                showSwitchIcons = showSwitchIcons,
                onShowSwitchIconsChange = { showSwitchIcons = it },
                nativeNotifyIconEnabled = nativeNotifyIconEnabled,
                onNativeNotifyIconEnabledChange = { nativeNotifyIconEnabled = it },
                notificationBubbleBlurEnabled = notificationBubbleBlurEnabled,
                onNotificationBubbleBlurEnabledChange = { notificationBubbleBlurEnabled = it },
                notificationBubbleBlurRadiusPx = notificationBubbleBlurRadiusPx,
                onNotificationBubbleBlurRadiusPxChange = { notificationBubbleBlurRadiusPx = it },
                nativeNotificationBubblesEnabled = nativeNotificationBubblesEnabled,
                onNativeNotificationBubblesEnabledChange = { nativeNotificationBubblesEnabled = it },
                extremeRefresh165Enabled = extremeRefresh165Enabled,
                onExtremeRefresh165EnabledChange = { extremeRefresh165Enabled = it },
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
                doublePowerCustomEnabled = doublePowerCustomEnabled,
                onDoublePowerCustomEnabledChange = { doublePowerCustomEnabled = it },
                doublePowerTargetPackage = doublePowerTargetPackage,
                onDoublePowerTargetPackageChange = { doublePowerTargetPackage = it },
                doublePowerTargetActivity = doublePowerTargetActivity,
                onDoublePowerTargetActivityChange = { doublePowerTargetActivity = it },
                assistantPowerMode = assistantPowerMode,
                onAssistantPowerModeChange = { assistantPowerMode = it },
                assistantGestureCircleEnabled = assistantGestureCircleEnabled,
                onAssistantGestureCircleEnabledChange = { assistantGestureCircleEnabled = it },
                currentUiStyle = UiStyleMode.Md3e.prefValue,
                onUiStyleChange = {},
                uiStyleMode = UiStyleMode.Md3e,
                onConfigImported = reloadConfigurationFromPrefs
            )

            if (!firstLaunchChecksPassed) {
                MaterialTheme(
                    colorScheme = resolveMd3eColorScheme(
                        context = this@MainActivity,
                        darkTheme = darkTheme,
                        customMonetEnabled = customMonetEnabled,
                        customMonetSeedColor = customMonetSeedColor
                    )
                ) {
                    FirstLaunchRootDialog(
                        onGranted = { firstLaunchChecksPassed = true },
                        onExit = { finish() }
                    )
                }
            }
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

@Composable
private fun FirstLaunchRootDialog(
    onGranted: () -> Unit,
    onExit: () -> Unit
) {
    var requesting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val deniedText = stringResource(R.string.first_launch_root_dialog_denied)

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = stringResource(R.string.first_launch_root_dialog_title))
        },
        text = {
            Text(
                text = errorText ?: stringResource(R.string.first_launch_root_dialog_message)
            )
        },
        confirmButton = {
            TextButton(
                enabled = !requesting,
                onClick = {
                    requesting = true
                    errorText = null
                    scope.launch {
                        val rootResult = queryRootAccess(context)
                        val hasRoot = rootResult.state == RootAccessState.Granted
                        AppLogStore.i(
                            "FirstLaunch",
                            "Checks result: root=$hasRoot"
                        )
                        requesting = false
                        if (hasRoot) {
                            onGranted()
                        } else {
                            errorText = deniedText
                        }
                    }
                }
            ) {
                Text(
                    text = if (requesting) {
                        stringResource(R.string.first_launch_root_dialog_requesting)
                    } else {
                        stringResource(R.string.first_launch_root_dialog_confirm)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !requesting,
                onClick = onExit
            ) {
                Text(text = stringResource(R.string.first_launch_root_dialog_exit))
            }
        }
    )
}
