package com.mi.mibox.ui.md3e

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.BatteryManager
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Slider
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import com.mi.mibox.R
import com.mi.mibox.battery.BatteryLiveNotificationService
import com.mi.mibox.battery.BatteryMonitor
import com.mi.mibox.battery.BatteryPreferences
import com.mi.mibox.battery.BatterySnapshot
import com.mi.mibox.battery.OplusChargingProtocolInfo
import com.mi.mibox.battery.OplusBatteryInfo
import com.mi.mibox.lsp.LspConfig
import com.mi.mibox.lsp.LsposedScopeRequester
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import com.mi.mibox.ui.common.AppIcons
import com.mi.mibox.ui.common.ConfigBackup
import com.mi.mibox.ui.common.AppLocale
import com.mi.mibox.ui.common.AppLogLevel
import com.mi.mibox.ui.common.AppLogStore
import com.mi.mibox.ui.common.bottomTabs
import com.mi.mibox.ui.common.AssistantScreenOption
import com.mi.mibox.ui.common.UiStyleMode
import com.mi.mibox.ui.common.appendLspDiagnosticsForFeedback
import com.mi.mibox.ui.common.applyAssistantScreenOption
import com.mi.mibox.ui.common.queryAssistantScreenOption
import com.mi.mibox.ui.common.RootAccessState
import com.mi.mibox.ui.common.RootAccessInfo
import com.mi.mibox.ui.common.queryRootAccess
import com.mi.mibox.ui.common.readCachedRootAccessInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val pageHorizontalPadding = 16.dp
private val personalizationHeaderOffset = 12.dp
private const val COOLAPK_PROFILE_URL = "https://www.coolapk.com/u/29184225"
private const val GITHUB_PROFILE_URL = "https://github.com/MiToverG422"
private const val GITHUB_REPO_URL = "https://github.com/MiToverG422/MiBox"
private const val TELEGRAM_CHANNEL_URL = "https://t.me/mibox_ci"
private const val THEME_MODE_SYSTEM = 0
private const val THEME_MODE_LIGHT = 1
private const val THEME_MODE_DARK = 2
private val batteryRefreshIntervalOptionsMs = listOf(
    500,
    1_000,
    2_000,
    3_000,
    4_000,
    5_000,
    6_000,
    7_000,
    8_000,
    9_000,
    10_000
)
private val LocalSwitchIconVisibility = staticCompositionLocalOf { true }

private fun Context.isIgnoringBatteryOptimizations(): Boolean {
    return getSystemService(PowerManager::class.java)
        ?.isIgnoringBatteryOptimizations(packageName) == true
}

private fun batteryOptimizationSettingsIntent(context: Context): Intent {
    val requestIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    return if (requestIntent.resolveActivity(context.packageManager) != null) {
        requestIntent
    } else {
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    }
}

private data class AssistantActionUi(
    val option: AssistantScreenOption,
    @param:StringRes val titleRes: Int,
    @param:StringRes val successMessageRes: Int
)

private val assistantActionOptions = listOf(
    AssistantActionUi(
        option = AssistantScreenOption.Shelf,
        titleRes = R.string.event_shelf_option,
        successMessageRes = R.string.event_apply_success_shelf
    ),
    AssistantActionUi(
        option = AssistantScreenOption.Disabled,
        titleRes = R.string.event_disable_option,
        successMessageRes = R.string.event_apply_success_disabled
    ),
    AssistantActionUi(
        option = AssistantScreenOption.Default,
        titleRes = R.string.event_default_option,
        successMessageRes = R.string.event_apply_success_default
    )
)

private data class LauncherRegionUi(
    val mode: Int,
    @param:StringRes val titleRes: Int
)

private val launcherRegionOptions = listOf(
    LauncherRegionUi(
        mode = LspConfig.LAUNCHER_REGION_MODE_OFF,
        titleRes = R.string.feature_launcher_region_off
    ),
    LauncherRegionUi(
        mode = LspConfig.LAUNCHER_REGION_MODE_CN,
        titleRes = R.string.feature_launcher_region_cn
    ),
    LauncherRegionUi(
        mode = LspConfig.LAUNCHER_REGION_MODE_IN,
        titleRes = R.string.feature_launcher_region_in
    )
)

private data class MonetPreset(
    @param:StringRes val nameRes: Int,
    val color: Int
)

private val customMonetPresets = listOf(
    MonetPreset(nameRes = R.string.monet_name_sakura, color = 0xFFFFB7C5.toInt()),
    MonetPreset(nameRes = R.string.monet_name_red, color = 0xFFC62828.toInt()),
    MonetPreset(nameRes = R.string.monet_name_pink, color = 0xFFE91E63.toInt()),
    MonetPreset(nameRes = R.string.monet_name_purple, color = 0xFF9C27B0.toInt()),
    MonetPreset(nameRes = R.string.monet_name_deep_purple, color = 0xFF5E35B1.toInt()),
    MonetPreset(nameRes = R.string.monet_name_indigo, color = 0xFF3F51B5.toInt()),
    MonetPreset(nameRes = R.string.monet_name_blue, color = 0xFF1E88E5.toInt()),
    MonetPreset(nameRes = R.string.monet_name_light_blue, color = 0xFF4FC3F7.toInt()),
    MonetPreset(nameRes = R.string.monet_name_cyan, color = 0xFF00ACC1.toInt()),
    MonetPreset(nameRes = R.string.monet_name_teal, color = 0xFF009688.toInt()),
    MonetPreset(nameRes = R.string.monet_name_green, color = 0xFF43A047.toInt()),
    MonetPreset(nameRes = R.string.monet_name_light_green, color = 0xFF8BC34A.toInt()),
    MonetPreset(nameRes = R.string.monet_name_lime, color = 0xFFCDDC39.toInt()),
    MonetPreset(nameRes = R.string.monet_name_yellow, color = 0xFFFDD835.toInt()),
    MonetPreset(nameRes = R.string.monet_name_amber, color = 0xFFFFB300.toInt()),
    MonetPreset(nameRes = R.string.monet_name_orange, color = 0xFFFB8C00.toInt()),
    MonetPreset(nameRes = R.string.monet_name_deep_orange, color = 0xFFF4511E.toInt()),
    MonetPreset(nameRes = R.string.monet_name_brown, color = 0xFF795548.toInt()),
    MonetPreset(nameRes = R.string.monet_name_blue_grey, color = 0xFF607D8B.toInt())
)

private fun blendColor(color: Int, withColor: Int, ratio: Float): Color {
    return Color(ColorUtils.blendARGB(color, withColor, ratio.coerceIn(0f, 1f)))
}

private data class Md3eCustomUiColors(
    val navContainer: Color,
    val navIndicator: Color,
    val navSelected: Color,
    val settingsCard: Color,
    val infoCard: Color,
    val statusCard: Color,
    val statusOnCard: Color
)

private fun customUiColors(
    darkTheme: Boolean,
    seedColor: Int
): Md3eCustomUiColors {
    val seed = seedColor or 0xFF000000.toInt()
    val white = 0xFFFFFFFF.toInt()
    val black = 0xFF000000.toInt()

    return if (darkTheme) {
        Md3eCustomUiColors(
            navContainer = blendColor(seed, black, 0.80f),
            navIndicator = blendColor(seed, white, 0.28f),
            navSelected = blendColor(seed, white, 0.88f),
            settingsCard = blendColor(seed, black, 0.82f),
            infoCard = blendColor(seed, black, 0.88f),
            statusCard = blendColor(seed, black, 0.58f),
            statusOnCard = blendColor(seed, white, 0.72f)
        )
    } else {
        Md3eCustomUiColors(
            navContainer = blendColor(seed, white, 0.89f),
            navIndicator = blendColor(seed, white, 0.70f),
            navSelected = blendColor(seed, black, 0.18f),
            settingsCard = blendColor(seed, white, 0.84f),
            infoCard = blendColor(seed, white, 0.90f),
            statusCard = blendColor(seed, white, 0.66f),
            statusOnCard = blendColor(seed, black, 0.70f)
        )
    }
}

private enum class GroupPosition {
    Top,
    Middle,
    Bottom,
    Single
}

private fun groupedCardShape(position: GroupPosition): RoundedCornerShape {
    return when (position) {
        GroupPosition.Top -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        GroupPosition.Middle -> RoundedCornerShape(4.dp)
        GroupPosition.Bottom -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        GroupPosition.Single -> RoundedCornerShape(20.dp)
    }
}

private fun cosxGroupedCardShape(position: GroupPosition): RoundedCornerShape {
    return when (position) {
        GroupPosition.Top -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
        GroupPosition.Middle -> RoundedCornerShape(10.dp)
        GroupPosition.Bottom -> RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        GroupPosition.Single -> RoundedCornerShape(18.dp)
    }
}

@Composable
fun Md3eRoot(
    darkTheme: Boolean,
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    showLogsTab: Boolean,
    onShowLogsTabChange: (Boolean) -> Unit,
    showBatteryTab: Boolean,
    onShowBatteryTabChange: (Boolean) -> Unit,
    showChinaSpecialFeatures: Boolean,
    onShowChinaSpecialFeaturesChange: (Boolean) -> Unit,
    showGlobalSpecialFeatures: Boolean,
    onShowGlobalSpecialFeaturesChange: (Boolean) -> Unit,
    predictiveBackEnabled: Boolean,
    onPredictiveBackEnabledChange: (Boolean) -> Unit,
    customMonetEnabled: Boolean,
    onCustomMonetEnabledChange: (Boolean) -> Unit,
    customMonetSeedColor: Int,
    onCustomMonetSeedColorChange: (Int) -> Unit,
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    appLanguageTag: String,
    onAppLanguageChange: (String) -> Unit,
    showSwitchIcons: Boolean,
    onShowSwitchIconsChange: (Boolean) -> Unit,
    nativeNotifyIconEnabled: Boolean,
    onNativeNotifyIconEnabledChange: (Boolean) -> Unit,
    notificationBubbleBlurEnabled: Boolean,
    onNotificationBubbleBlurEnabledChange: (Boolean) -> Unit,
    notificationBubbleBlurRadiusPx: Int,
    onNotificationBubbleBlurRadiusPxChange: (Int) -> Unit,
    nativeNotificationBubblesEnabled: Boolean,
    onNativeNotificationBubblesEnabledChange: (Boolean) -> Unit,
    extremeRefresh165Enabled: Boolean,
    onExtremeRefresh165EnabledChange: (Boolean) -> Unit,
    permissionMonitorVisible: Boolean,
    onPermissionMonitorVisibleChange: (Boolean) -> Unit,
    launcherLayoutUnlocked: Boolean,
    onLauncherLayoutUnlockedChange: (Boolean) -> Unit,
    launcherRegionMode: Int,
    onLauncherRegionModeChange: (Int) -> Unit,
    recentTaskRadiusEnabled: Boolean,
    onRecentTaskRadiusEnabledChange: (Boolean) -> Unit,
    recentTaskRadiusDp: Int,
    onRecentTaskRadiusDpChange: (Int) -> Unit,
    aodEnhanceEnabled: Boolean,
    onAodEnhanceEnabledChange: (Boolean) -> Unit,
    aodInitDarkBrightness: Int,
    onAodInitDarkBrightnessChange: (Int) -> Unit,
    aodInitBrightBrightness: Int,
    onAodInitBrightBrightnessChange: (Int) -> Unit,
    aodRunningBrightnessMultiplier: Float,
    onAodRunningBrightnessMultiplierChange: (Float) -> Unit,
    aodPanoramicSupportEnabled: Boolean,
    onAodPanoramicSupportEnabledChange: (Boolean) -> Unit,
    aodSettingsSwitchEnabled: Boolean,
    onAodSettingsSwitchEnabledChange: (Boolean) -> Unit,
    aodSingleClickBlockEnabled: Boolean,
    onAodSingleClickBlockEnabledChange: (Boolean) -> Unit,
    oosLocalizerEnabled: Boolean,
    onOosLocalizerEnabledChange: (Boolean) -> Unit,
    doublePowerCustomEnabled: Boolean,
    onDoublePowerCustomEnabledChange: (Boolean) -> Unit,
    doublePowerTargetPackage: String,
    onDoublePowerTargetPackageChange: (String) -> Unit,
    doublePowerTargetActivity: String,
    onDoublePowerTargetActivityChange: (String) -> Unit,
    assistantPowerMode: Int,
    onAssistantPowerModeChange: (Int) -> Unit,
    assistantGestureCircleEnabled: Boolean,
    onAssistantGestureCircleEnabledChange: (Boolean) -> Unit,
    currentUiStyle: Int,
    onUiStyleChange: (Int) -> Unit,
    uiStyleMode: UiStyleMode = UiStyleMode.Md3e,
    onConfigImported: () -> Unit
) {
    val context = LocalContext.current
    val visibleBottomTabs = remember(showLogsTab, showBatteryTab) {
        bottomTabs.filter { tab ->
            (showLogsTab || tab.screenIndex != 2) &&
                (showBatteryTab || tab.screenIndex != 4)
        }
    }
    val tabScreenOrder = remember(visibleBottomTabs) {
        visibleBottomTabs
            .mapIndexed { index, tab -> tab.screenIndex to index }
            .toMap()
    }

    LaunchedEffect(showLogsTab, currentTab) {
        if (!showLogsTab && currentTab == 2) {
            onTabChange(0)
        }
    }

    LaunchedEffect(showBatteryTab, currentTab) {
        if (!showBatteryTab && currentTab == 4) {
            onTabChange(0)
        }
    }

    LaunchedEffect(visibleBottomTabs, currentTab) {
        if (visibleBottomTabs.none { it.screenIndex == currentTab }) {
            onTabChange(0)
        }
    }

    val monetEnabledForStyle = uiStyleMode == UiStyleMode.Md3e && customMonetEnabled
    val customColors = if (monetEnabledForStyle) {
        customUiColors(darkTheme = darkTheme, seedColor = customMonetSeedColor)
    } else {
        null
    }

    val colors = when (uiStyleMode) {
        UiStyleMode.Cosx -> resolveCosxColorScheme(darkTheme = darkTheme)
        UiStyleMode.Md3e -> resolveMd3eColorScheme(
            context = context,
            darkTheme = darkTheme,
            customMonetEnabled = monetEnabledForStyle,
            customMonetSeedColor = customMonetSeedColor
        )
    }

    val shapes = if (uiStyleMode == UiStyleMode.Cosx) {
        Shapes(
            extraSmall = RoundedCornerShape(6.dp),
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(14.dp),
            extraLarge = RoundedCornerShape(18.dp)
        )
    } else {
        Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(16.dp),
            extraLarge = RoundedCornerShape(20.dp)
        )
    }

    MaterialTheme(
        colorScheme = colors,
        shapes = shapes
    ) {
        CompositionLocalProvider(LocalSwitchIconVisibility provides showSwitchIcons) {
        BackGestureHandler(
            enabled = predictiveBackEnabled,
            currentTab = currentTab,
            onTabChange = onTabChange
        )
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = when (uiStyleMode) {
                            UiStyleMode.Cosx -> MaterialTheme.colorScheme.surfaceContainerLow
                            UiStyleMode.Md3e -> customColors?.navContainer ?: MaterialTheme.colorScheme.surfaceContainer
                        },
                        tonalElevation = if (uiStyleMode == UiStyleMode.Cosx) 2.dp else 6.dp
                    ) {
                        visibleBottomTabs.forEach { tab ->
                            val tabTitle = stringResource(tab.titleRes)
                            NavigationBarItem(
                                selected = currentTab == tab.screenIndex,
                                onClick = { onTabChange(tab.screenIndex) },
                                icon = { Icon(tab.icon, contentDescription = tabTitle) },
                                label = {
                                    Text(
                                        text = tabTitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (uiStyleMode == UiStyleMode.Cosx) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        customColors?.navSelected ?: MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                    selectedTextColor = if (uiStyleMode == UiStyleMode.Cosx) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        customColors?.navSelected ?: MaterialTheme.colorScheme.onSurface
                                    },
                                    indicatorColor = if (uiStyleMode == UiStyleMode.Cosx) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        customColors?.navIndicator ?: MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (uiStyleMode == UiStyleMode.Cosx) 0.92f else 1f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            val initialOrder = tabScreenOrder[initialState] ?: initialState
                            val targetOrder = tabScreenOrder[targetState] ?: targetState
                            val isForward = targetOrder > initialOrder
                            fadeIn(animationSpec = tween(220)) +
                                slideInHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 280,
                                        easing = FastOutSlowInEasing
                                    ),
                                    initialOffsetX = { full -> if (isForward) full / 5 else -full / 5 }
                                ) togetherWith
                                fadeOut(animationSpec = tween(180)) +
                                slideOutHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 240,
                                        easing = FastOutSlowInEasing
                                    ),
                                    targetOffsetX = { full -> if (isForward) -full / 7 else full / 7 }
                                )
                        },
                        label = "md3e_tab_switch"
                    ) { tab ->
                        when (tab) {
                            0 -> Md3eHomeScreen(
                                customColors = customColors,
                                uiStyleMode = uiStyleMode
                            )
                            1 -> Md3eFeaturesScreen(
                                showChinaSpecialFeatures = showChinaSpecialFeatures,
                                showGlobalSpecialFeatures = showGlobalSpecialFeatures,
                                nativeNotifyIconEnabled = nativeNotifyIconEnabled,
                                onNativeNotifyIconEnabledChange = onNativeNotifyIconEnabledChange,
                                notificationBubbleBlurEnabled = notificationBubbleBlurEnabled,
                                onNotificationBubbleBlurEnabledChange = onNotificationBubbleBlurEnabledChange,
                                notificationBubbleBlurRadiusPx = notificationBubbleBlurRadiusPx,
                                onNotificationBubbleBlurRadiusPxChange = onNotificationBubbleBlurRadiusPxChange,
                                nativeNotificationBubblesEnabled = nativeNotificationBubblesEnabled,
                                onNativeNotificationBubblesEnabledChange = onNativeNotificationBubblesEnabledChange,
                                extremeRefresh165Enabled = extremeRefresh165Enabled,
                                onExtremeRefresh165EnabledChange = onExtremeRefresh165EnabledChange,
                                permissionMonitorVisible = permissionMonitorVisible,
                                onPermissionMonitorVisibleChange = onPermissionMonitorVisibleChange,
                                launcherLayoutUnlocked = launcherLayoutUnlocked,
                                onLauncherLayoutUnlockedChange = onLauncherLayoutUnlockedChange,
                                launcherRegionMode = launcherRegionMode,
                                onLauncherRegionModeChange = onLauncherRegionModeChange,
                                recentTaskRadiusEnabled = recentTaskRadiusEnabled,
                                onRecentTaskRadiusEnabledChange = onRecentTaskRadiusEnabledChange,
                                recentTaskRadiusDp = recentTaskRadiusDp,
                                onRecentTaskRadiusDpChange = onRecentTaskRadiusDpChange,
                                aodEnhanceEnabled = aodEnhanceEnabled,
                                onAodEnhanceEnabledChange = onAodEnhanceEnabledChange,
                                aodInitDarkBrightness = aodInitDarkBrightness,
                                onAodInitDarkBrightnessChange = onAodInitDarkBrightnessChange,
                                aodInitBrightBrightness = aodInitBrightBrightness,
                                onAodInitBrightBrightnessChange = onAodInitBrightBrightnessChange,
                                aodRunningBrightnessMultiplier = aodRunningBrightnessMultiplier,
                                onAodRunningBrightnessMultiplierChange = onAodRunningBrightnessMultiplierChange,
                                aodPanoramicSupportEnabled = aodPanoramicSupportEnabled,
                                onAodPanoramicSupportEnabledChange = onAodPanoramicSupportEnabledChange,
                                aodSettingsSwitchEnabled = aodSettingsSwitchEnabled,
                                onAodSettingsSwitchEnabledChange = onAodSettingsSwitchEnabledChange,
                                aodSingleClickBlockEnabled = aodSingleClickBlockEnabled,
                                onAodSingleClickBlockEnabledChange = onAodSingleClickBlockEnabledChange,
                                oosLocalizerEnabled = oosLocalizerEnabled,
                                onOosLocalizerEnabledChange = onOosLocalizerEnabledChange,
                                doublePowerCustomEnabled = doublePowerCustomEnabled,
                                onDoublePowerCustomEnabledChange = onDoublePowerCustomEnabledChange,
                                doublePowerTargetPackage = doublePowerTargetPackage,
                                onDoublePowerTargetPackageChange = onDoublePowerTargetPackageChange,
                                doublePowerTargetActivity = doublePowerTargetActivity,
                                onDoublePowerTargetActivityChange = onDoublePowerTargetActivityChange,
                                assistantPowerMode = assistantPowerMode,
                                onAssistantPowerModeChange = onAssistantPowerModeChange,
                                assistantGestureCircleEnabled = assistantGestureCircleEnabled,
                                onAssistantGestureCircleEnabledChange = onAssistantGestureCircleEnabledChange,
                                customColors = customColors,
                                uiStyleMode = uiStyleMode
                            )
                            2 -> Md3eLogsScreen(
                                customColors = customColors,
                                uiStyleMode = uiStyleMode
                            )
                            3 -> Md3eSettingsScreen(
                                predictiveBackEnabled = predictiveBackEnabled,
                                onPredictiveBackEnabledChange = onPredictiveBackEnabledChange,
                                showLogsTab = showLogsTab,
                                onShowLogsTabChange = onShowLogsTabChange,
                                showBatteryTab = showBatteryTab,
                                onShowBatteryTabChange = onShowBatteryTabChange,
                                showChinaSpecialFeatures = showChinaSpecialFeatures,
                                onShowChinaSpecialFeaturesChange = onShowChinaSpecialFeaturesChange,
                                showGlobalSpecialFeatures = showGlobalSpecialFeatures,
                                onShowGlobalSpecialFeaturesChange = onShowGlobalSpecialFeaturesChange,
                                customMonetEnabled = customMonetEnabled,
                                onCustomMonetEnabledChange = onCustomMonetEnabledChange,
                                customMonetSeedColor = customMonetSeedColor,
                                onCustomMonetSeedColorChange = onCustomMonetSeedColorChange,
                                themeMode = themeMode,
                                onThemeModeChange = onThemeModeChange,
                                appLanguageTag = appLanguageTag,
                                onAppLanguageChange = onAppLanguageChange,
                                showSwitchIcons = showSwitchIcons,
                                onShowSwitchIconsChange = onShowSwitchIconsChange,
                                customColors = customColors,
                                currentUiStyle = currentUiStyle,
                                onUiStyleChange = onUiStyleChange,
                                uiStyleMode = uiStyleMode,
                                onConfigImported = onConfigImported
                            )
                            4 -> Md3eBatteryScreen(
                                customColors = customColors,
                                uiStyleMode = uiStyleMode
                            )
                            else -> PlaceholderScreen(tab)
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun BackGestureHandler(
    enabled: Boolean,
    currentTab: Int,
    onTabChange: (Int) -> Unit
) {
    // When predictive back is enabled, we do not intercept so system predictive animation works.
    // When disabled, we fallback to non-predictive app back behavior.
    val activity = LocalContext.current as? Activity ?: return

    BackHandler(enabled = !enabled) {
        if (currentTab != 0) onTabChange(0) else activity.moveTaskToBack(false)
    }
}

@Composable
private fun PageTopHeader(
    title: String,
    showHomeIcons: Boolean,
    titleOffsetY: androidx.compose.ui.unit.Dp = 0.dp,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionContent: (@Composable () -> Unit)? = null
) {
    val hasNavigation = navigationIcon != null && onNavigationClick != null
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showHomeIcons) {
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Extension,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(modifier = Modifier.size(48.dp))
                Box(modifier = Modifier.size(48.dp))
            }
        }
        if (actionContent != null || (actionText != null && onActionClick != null)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp)
                    .offset(y = titleOffsetY),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasNavigation) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigationClick ?: {},
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = navigationIcon ?: Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (actionContent != null) {
                    actionContent()
                } else {
                    TextButton(
                        onClick = onActionClick ?: {},
                        modifier = Modifier.heightIn(min = 32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = actionText.orEmpty(),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        } else {
            if (hasNavigation) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 6.dp)
                        .offset(y = titleOffsetY),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigationClick ?: {},
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = navigationIcon ?: Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 6.dp)
                        .offset(y = titleOffsetY)
                )
            }
        }
    }
}

@Composable
private fun FeatureDetailStickyTopBar(
    title: String,
    titleAlpha: Float,
    onBack: () -> Unit
) {
    val safeAlpha = titleAlpha.coerceIn(0f, 1f)
    val containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.86f + safeAlpha * 0.12f)
    val iconBgColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        tonalElevation = if (safeAlpha > 0.05f) 2.dp else 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .offset(y = (-8).dp)
                .padding(
                    start = 0.dp,
                    end = pageHorizontalPadding,
                    top = 0.dp,
                    bottom = 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(iconBgColor)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = safeAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .offset(y = (-1).dp)
            )
        }
    }
}

@Composable
private fun HeaderActionIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    var showHint by remember { mutableStateOf(false) }
    var hintSignal by remember { mutableStateOf(0) }

    LaunchedEffect(hintSignal) {
        if (showHint) {
            delay(1000)
            showHint = false
        }
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .zIndex(2f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = {
                            showHint = true
                            hintSignal += 1
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp)
            )
        }

        AnimatedVisibility(
            visible = showHint,
            enter = fadeIn(animationSpec = tween(140)) + scaleIn(animationSpec = tween(180), initialScale = 0.92f),
            exit = fadeOut(animationSpec = tween(180)) + scaleOut(animationSpec = tween(180), targetScale = 0.95f),
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .align(Alignment.TopCenter)
                .offset(y = (-44).dp)
                .zIndex(100f)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = contentDescription,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(tab: Int) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = pageHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PageTopHeader(
                title = stringResource(bottomTabs[tab].titleRes),
                showHomeIcons = false
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = stringResource(
                        R.string.tab_page_placeholder,
                        stringResource(bottomTabs[tab].titleRes)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}

private enum class LogFilterMode(val label: String) {
    ALL("ALL"),
    INFO_PLUS("INFO+"),
    WARN_PLUS("WARN+"),
    ERROR_ONLY("ERROR");

    fun matches(level: AppLogLevel): Boolean {
        return when (this) {
            ALL -> true
            INFO_PLUS -> level == AppLogLevel.INFO
            WARN_PLUS -> level == AppLogLevel.WARN || level == AppLogLevel.ERROR
            ERROR_ONLY -> level == AppLogLevel.ERROR
        }
    }
}

private enum class LspUiState {
    READY,
    MISSING_SCOPE,
    NOT_READY
}

@Composable
private fun Md3eLogsScreen(
    customColors: Md3eCustomUiColors?,
    uiStyleMode: UiStyleMode
) {
    val isCosxStyle = uiStyleMode == UiStyleMode.Cosx
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logs by AppLogStore.entries.collectAsState()
    var filterMode by rememberSaveable { mutableStateOf(LogFilterMode.ALL) }
    var showFilterMenu by rememberSaveable { mutableStateOf(false) }
    var refreshNonce by rememberSaveable { mutableStateOf(0L) }
    var diagnosticsLoading by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching {
            appendLspDiagnosticsForFeedback(
                context = context,
                reason = "log_page_open"
            )
        }.onFailure { throwable ->
            AppLogStore.w("LSP", "Diagnostics collect failed: ${throwable.message.orEmpty()}")
        }
        refreshNonce = System.currentTimeMillis()
    }

    val saveLogLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = runCatching {
                val bytes = AppLogStore.exportText().toByteArray(Charsets.UTF_8)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(bytes)
                    out.flush()
                } ?: error("Cannot open output stream")
                uri
            }
            if (result.isSuccess) {
                AppLogStore.i("Logs", "Log file saved: ${result.getOrNull()}")
            } else {
                AppLogStore.w(
                    "Logs",
                    "Save log failed: ${result.exceptionOrNull()?.message ?: context.getString(R.string.common_unknown)}"
                )
            }
        }
    }

    val filteredLogs = remember(logs, filterMode, refreshNonce) {
        logs.asReversed().filter { entry ->
            filterMode.matches(entry.level)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = pageHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            PageTopHeader(
                title = stringResource(R.string.tab_events),
                showHomeIcons = false,
                actionContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderActionIconButton(
                            icon = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.action_refresh),
                            onClick = {
                                if (diagnosticsLoading) return@HeaderActionIconButton
                                diagnosticsLoading = true
                                scope.launch {
                                    runCatching {
                                        appendLspDiagnosticsForFeedback(
                                            context = context,
                                            reason = "manual_refresh"
                                        )
                                    }.onFailure { throwable ->
                                        AppLogStore.w(
                                            "LSP",
                                            "Diagnostics collect failed: ${throwable.message.orEmpty()}"
                                        )
                                    }
                                    diagnosticsLoading = false
                                    refreshNonce = System.currentTimeMillis()
                                }
                            }
                        )

                        Box {
                            HeaderActionIconButton(
                                icon = AppIcons.Filter,
                                contentDescription = stringResource(R.string.log_action_filter),
                                onClick = { showFilterMenu = true }
                            )

                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                LogFilterMode.values().forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(text = mode.label) },
                                        onClick = {
                                            filterMode = mode
                                            showFilterMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        HeaderActionIconButton(
                            icon = AppIcons.Save,
                            contentDescription = stringResource(R.string.log_action_save),
                            onClick = {
                                saveLogLauncher.launch("mibox_log_${System.currentTimeMillis()}.txt")
                            }
                        )

                        HeaderActionIconButton(
                            icon = Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.log_action_share),
                            onClick = {
                                scope.launch {
                                    val result = AppLogStore.createShareUri(context)
                                    if (result.isSuccess) {
                                        val uri = result.getOrNull()
                                        if (uri != null) {
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                putExtra(
                                                    Intent.EXTRA_SUBJECT,
                                                    context.getString(R.string.log_share_subject)
                                                )
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                clipData = ClipData.newUri(
                                                    context.contentResolver,
                                                    "mibox_log",
                                                    uri
                                                )
                                            }
                                            val chooserIntent = Intent.createChooser(
                                                sendIntent,
                                                context.getString(R.string.log_action_share)
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(chooserIntent)
                                            AppLogStore.i("Logs", "Share panel opened")
                                        }
                                    } else {
                                        AppLogStore.w(
                                            "Logs",
                                            "Share failed: ${
                                                result.exceptionOrNull()?.message
                                                    ?: context.getString(R.string.common_unknown)
                                            }"
                                        )
                                    }
                                }
                            }
                        )

                        HeaderActionIconButton(
                            icon = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.log_action_clear),
                            onClick = {
                                AppLogStore.clear()
                            }
                        )
                    }
                }
            )
        }

        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCosxStyle) {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        } else {
                            customColors?.infoCard ?: MaterialTheme.colorScheme.surfaceContainerLow
                        }
                    ),
                    shape = if (isCosxStyle) RoundedCornerShape(18.dp) else MaterialTheme.shapes.large
                ) {
                    Text(
                        text = stringResource(R.string.log_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                    )
                }
            }
        } else {
            items(
                items = filteredLogs,
                key = { it.id }
            ) { entry ->
                LogEntryCard(
                    line = entry.toLine(),
                    customColors = customColors,
                    isCosxStyle = isCosxStyle
                )
            }
        }
    }
}

@Composable
private fun LogEntryCard(
    line: String,
    customColors: Md3eCustomUiColors?,
    isCosxStyle: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCosxStyle) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                customColors?.infoCard ?: MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        shape = if (isCosxStyle) RoundedCornerShape(14.dp) else MaterialTheme.shapes.medium
    ) {
        Text(
            text = line,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Md3eFeaturesScreen(
    showChinaSpecialFeatures: Boolean,
    showGlobalSpecialFeatures: Boolean,
    nativeNotifyIconEnabled: Boolean,
    onNativeNotifyIconEnabledChange: (Boolean) -> Unit,
    notificationBubbleBlurEnabled: Boolean,
    onNotificationBubbleBlurEnabledChange: (Boolean) -> Unit,
    notificationBubbleBlurRadiusPx: Int,
    onNotificationBubbleBlurRadiusPxChange: (Int) -> Unit,
    nativeNotificationBubblesEnabled: Boolean,
    onNativeNotificationBubblesEnabledChange: (Boolean) -> Unit,
    extremeRefresh165Enabled: Boolean,
    onExtremeRefresh165EnabledChange: (Boolean) -> Unit,
    permissionMonitorVisible: Boolean,
    onPermissionMonitorVisibleChange: (Boolean) -> Unit,
    launcherLayoutUnlocked: Boolean,
    onLauncherLayoutUnlockedChange: (Boolean) -> Unit,
    launcherRegionMode: Int,
    onLauncherRegionModeChange: (Int) -> Unit,
    recentTaskRadiusEnabled: Boolean,
    onRecentTaskRadiusEnabledChange: (Boolean) -> Unit,
    recentTaskRadiusDp: Int,
    onRecentTaskRadiusDpChange: (Int) -> Unit,
    aodEnhanceEnabled: Boolean,
    onAodEnhanceEnabledChange: (Boolean) -> Unit,
    aodInitDarkBrightness: Int,
    onAodInitDarkBrightnessChange: (Int) -> Unit,
    aodInitBrightBrightness: Int,
    onAodInitBrightBrightnessChange: (Int) -> Unit,
    aodRunningBrightnessMultiplier: Float,
    onAodRunningBrightnessMultiplierChange: (Float) -> Unit,
    aodPanoramicSupportEnabled: Boolean,
    onAodPanoramicSupportEnabledChange: (Boolean) -> Unit,
    aodSettingsSwitchEnabled: Boolean,
    onAodSettingsSwitchEnabledChange: (Boolean) -> Unit,
    aodSingleClickBlockEnabled: Boolean,
    onAodSingleClickBlockEnabledChange: (Boolean) -> Unit,
    oosLocalizerEnabled: Boolean,
    onOosLocalizerEnabledChange: (Boolean) -> Unit,
    doublePowerCustomEnabled: Boolean,
    onDoublePowerCustomEnabledChange: (Boolean) -> Unit,
    doublePowerTargetPackage: String,
    onDoublePowerTargetPackageChange: (String) -> Unit,
    doublePowerTargetActivity: String,
    onDoublePowerTargetActivityChange: (String) -> Unit,
    assistantPowerMode: Int,
    onAssistantPowerModeChange: (Int) -> Unit,
    assistantGestureCircleEnabled: Boolean,
    onAssistantGestureCircleEnabledChange: (Boolean) -> Unit,
    customColors: Md3eCustomUiColors?,
    uiStyleMode: UiStyleMode
) {
    val isCosxStyle = uiStyleMode == UiStyleMode.Cosx
    var recentTaskRadiusInput by rememberSaveable { mutableStateOf(recentTaskRadiusDp.toString()) }
    var aodDarkInput by rememberSaveable { mutableStateOf(aodInitDarkBrightness.toString()) }
    var aodBrightInput by rememberSaveable { mutableStateOf(aodInitBrightBrightness.toString()) }
    var aodMultiplierInput by rememberSaveable {
        mutableStateOf(String.format("%.1f", aodRunningBrightnessMultiplier))
    }
    var notificationBubbleBlurInput by rememberSaveable {
        mutableStateOf(notificationBubbleBlurRadiusPx.toString())
    }

    LaunchedEffect(recentTaskRadiusDp) {
        recentTaskRadiusInput = recentTaskRadiusDp.toString()
    }
    LaunchedEffect(aodInitDarkBrightness) {
        aodDarkInput = aodInitDarkBrightness.toString()
    }
    LaunchedEffect(aodInitBrightBrightness) {
        aodBrightInput = aodInitBrightBrightness.toString()
    }
    LaunchedEffect(aodRunningBrightnessMultiplier) {
        aodMultiplierInput = String.format("%.1f", aodRunningBrightnessMultiplier)
    }
    LaunchedEffect(notificationBubbleBlurRadiusPx) {
        notificationBubbleBlurInput = notificationBubbleBlurRadiusPx.toString()
    }
    var selectedCategory by rememberSaveable { mutableStateOf<FeatureCategory?>(null) }
    var openedCategory by rememberSaveable { mutableStateOf<FeatureCategory?>(null) }
    val visibleFeatureCategories = remember(showChinaSpecialFeatures, showGlobalSpecialFeatures) {
        FeatureCategory.entries.filter { category ->
            when (category) {
                FeatureCategory.DoublePower,
                FeatureCategory.Localizer -> showGlobalSpecialFeatures
                FeatureCategory.Assistant -> showChinaSpecialFeatures
                else -> true
            }
        }
    }

    BackHandler(enabled = openedCategory != null) {
        openedCategory = null
        selectedCategory = null
    }
    LaunchedEffect(visibleFeatureCategories, openedCategory) {
        if (openedCategory != null && openedCategory !in visibleFeatureCategories) {
            openedCategory = null
            selectedCategory = null
        }
    }

    AnimatedContent(
        targetState = openedCategory,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            val openDetail = initialState == null && targetState != null
            if (openDetail) {
                (slideInHorizontally(
                    animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> (fullWidth * 0.18f).toInt() }
                ) + fadeIn(animationSpec = tween(durationMillis = 260))) togetherWith
                    (slideOutHorizontally(
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> (-fullWidth * 0.08f).toInt() }
                    ) + fadeOut(animationSpec = tween(durationMillis = 220)))
            } else {
                (slideInHorizontally(
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> (-fullWidth * 0.10f).toInt() }
                ) + fadeIn(animationSpec = tween(durationMillis = 240))) togetherWith
                    (slideOutHorizontally(
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> (fullWidth * 0.16f).toInt() }
                    ) + fadeOut(animationSpec = tween(durationMillis = 200)))
            }
        },
        label = "feature_category_page_switch"
    ) { targetCategory ->
        if (targetCategory == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = pageHorizontalPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    PageTopHeader(
                        title = stringResource(R.string.tab_features),
                        showHomeIcons = false
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(personalizationHeaderOffset))
                }
                item {
                    FeatureCategorySelectorCard(
                        categories = visibleFeatureCategories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = {
                            selectedCategory = it
                            openedCategory = it
                        },
                        customColors = customColors,
                        isCosxStyle = isCosxStyle
                    )
                }
            }
        } else {
            val currentCategory = targetCategory
            val detailListState = rememberLazyListState()
            val collapseTarget by remember(detailListState) {
                androidx.compose.runtime.derivedStateOf {
                    when {
                        detailListState.firstVisibleItemIndex > 0 -> 1f
                        else -> (detailListState.firstVisibleItemScrollOffset / 140f).coerceIn(0f, 1f)
                    }
                }
            }
            val collapseProgress by animateFloatAsState(
                targetValue = collapseTarget,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "feature_detail_collapse_title"
            )

            LazyColumn(
                state = detailListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = pageHorizontalPadding,
                    end = pageHorizontalPadding,
                    top = 0.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                stickyHeader {
                    FeatureDetailStickyTopBar(
                        title = stringResource(currentCategory.titleRes),
                        titleAlpha = collapseProgress,
                        onBack = {
                            openedCategory = null
                            selectedCategory = null
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Text(
                        text = stringResource(currentCategory.titleRes),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(personalizationHeaderOffset))
                }
                when (currentCategory) {
                    FeatureCategory.Desktop -> {
                        if (showGlobalSpecialFeatures) {
                            item {
                                Text(
                                    text = stringResource(R.string.feature_group_minus_one),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                                )
                            }
                            item {
                                DesktopAssistantExpandableCard(
                                    customColors = customColors,
                                    isCosxStyle = isCosxStyle
                                )
                            }
                        }
                        item {
                            Text(
                                text = stringResource(R.string.feature_group_region),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 10.dp,
                                    bottom = 8.dp
                                )
                            )
                        }
                        item {
                            DesktopRegionExpandableCard(
                                regionMode = launcherRegionMode,
                                onRegionModeChange = onLauncherRegionModeChange,
                                customColors = customColors,
                                isCosxStyle = isCosxStyle
                            )
                        }
                        item {
                            Text(
                                text = stringResource(R.string.feature_group_layout),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 10.dp,
                                    bottom = 8.dp
                                )
                            )
                        }
                        item {
                            SettingSwitchCard(
                                icon = AppIcons.Widgets,
                                title = stringResource(R.string.feature_launcher_layout_unlock_title),
                                description = stringResource(R.string.feature_launcher_layout_unlock_summary),
                                checked = launcherLayoutUnlocked,
                                onCheckedChange = onLauncherLayoutUnlockedChange,
                                customColors = customColors,
                                groupPosition = GroupPosition.Single,
                                isCosxStyle = isCosxStyle
                            )
                        }
                        item {
                            Text(
                                text = stringResource(R.string.feature_group_recent_tasks),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 8.dp)
                            )
                        }
                        item {
                            FeatureNumericSliderCard(
                                icon = AppIcons.Widgets,
                                title = stringResource(R.string.feature_recent_task_radius_title),
                                description = stringResource(R.string.feature_recent_task_radius_summary),
                                checked = recentTaskRadiusEnabled,
                                onCheckedChange = onRecentTaskRadiusEnabledChange,
                                currentValueText = stringResource(
                                    R.string.feature_slider_current_dp,
                                    recentTaskRadiusDp
                                ),
                                sliderValue = recentTaskRadiusDp.toFloat(),
                                sliderRange = 0f..260f,
                                onSliderValueChange = { onRecentTaskRadiusDpChange(it.roundToInt().coerceIn(0, 260)) },
                                inputValue = recentTaskRadiusInput,
                                onInputValueChange = { raw ->
                                    val digitsOnly = raw.filter { it.isDigit() }.take(3)
                                    recentTaskRadiusInput = digitsOnly
                                    digitsOnly.toIntOrNull()?.let { onRecentTaskRadiusDpChange(it.coerceIn(0, 260)) }
                                },
                                inputPlaceholder = stringResource(R.string.feature_slider_input_dp_hint),
                                hint = stringResource(R.string.feature_recent_task_radius_hint),
                                customColors = customColors,
                                groupPosition = GroupPosition.Single,
                                isCosxStyle = isCosxStyle
                            )
                        }
                    }

                    FeatureCategory.SystemUi -> {
                        item {
                            SettingSwitchCard(
                                icon = Icons.Rounded.Notifications,
                                title = stringResource(R.string.feature_native_notify_icon_title),
                                description = stringResource(R.string.feature_native_notify_icon_summary),
                                checked = nativeNotifyIconEnabled,
                                onCheckedChange = onNativeNotifyIconEnabledChange,
                                customColors = customColors,
                                groupPosition = GroupPosition.Single,
                                isCosxStyle = isCosxStyle
                            )
                        }
                        if (showChinaSpecialFeatures) {
                            item {
                                SettingSwitchCard(
                                    icon = Icons.Rounded.Notifications,
                                    title = stringResource(R.string.feature_native_notification_bubbles_title),
                                    description = stringResource(R.string.feature_native_notification_bubbles_summary),
                                    checked = nativeNotificationBubblesEnabled,
                                    onCheckedChange = onNativeNotificationBubblesEnabledChange,
                                    customColors = customColors,
                                    groupPosition = GroupPosition.Single,
                                    isCosxStyle = isCosxStyle
                                )
                            }
                        }
                        item {
                            FeatureNumericSliderCard(
                                icon = AppIcons.Tune,
                                title = stringResource(R.string.feature_notification_bubble_blur_title),
                                description = stringResource(R.string.feature_notification_bubble_blur_summary),
                                checked = notificationBubbleBlurEnabled,
                                onCheckedChange = onNotificationBubbleBlurEnabledChange,
                                currentValueText = stringResource(
                                    R.string.feature_slider_current_px,
                                    notificationBubbleBlurRadiusPx
                                ),
                                sliderValue = notificationBubbleBlurRadiusPx.toFloat(),
                                sliderRange = 0f..800f,
                                onSliderValueChange = {
                                    onNotificationBubbleBlurRadiusPxChange(it.roundToInt().coerceIn(0, 800))
                                },
                                inputValue = notificationBubbleBlurInput,
                                onInputValueChange = { raw ->
                                    val digitsOnly = raw.filter { it.isDigit() }.take(3)
                                    notificationBubbleBlurInput = digitsOnly
                                    digitsOnly.toIntOrNull()?.let {
                                        onNotificationBubbleBlurRadiusPxChange(it.coerceIn(0, 800))
                                    }
                                },
                                inputPlaceholder = stringResource(R.string.feature_slider_input_px_hint),
                                hint = stringResource(R.string.feature_notification_bubble_blur_hint),
                                customColors = customColors,
                                groupPosition = GroupPosition.Single,
                                isCosxStyle = isCosxStyle
                            )
                        }
                    }

                    FeatureCategory.Settings -> {
                        item {
                            SettingSwitchCard(
                                icon = AppIcons.Tune,
                                title = stringResource(R.string.feature_permission_monitor_title),
                                description = stringResource(R.string.feature_permission_monitor_summary),
                                checked = permissionMonitorVisible,
                                onCheckedChange = onPermissionMonitorVisibleChange,
                                customColors = customColors,
                                groupPosition = GroupPosition.Single,
                                isCosxStyle = isCosxStyle
                            )
                        }
                    }

                    FeatureCategory.Aod -> {
                        item {
                            FeatureAodEnhanceCard(
                                icon = AppIcons.LightMode,
                                title = stringResource(R.string.feature_aod_enhance_title),
                                description = stringResource(R.string.feature_aod_enhance_summary),
                                checked = aodEnhanceEnabled,
                                onCheckedChange = onAodEnhanceEnabledChange,
                                darkBrightness = aodInitDarkBrightness,
                                onDarkBrightnessChange = onAodInitDarkBrightnessChange,
                                brightBrightness = aodInitBrightBrightness,
                                onBrightBrightnessChange = onAodInitBrightBrightnessChange,
                                runningMultiplier = aodRunningBrightnessMultiplier,
                                onRunningMultiplierChange = onAodRunningBrightnessMultiplierChange,
                                panoramicSupportEnabled = aodPanoramicSupportEnabled,
                                onPanoramicSupportChange = onAodPanoramicSupportEnabledChange,
                                settingsSwitchEnabled = aodSettingsSwitchEnabled,
                                onSettingsSwitchChange = onAodSettingsSwitchEnabledChange,
                                singleClickBlockEnabled = aodSingleClickBlockEnabled,
                                onSingleClickBlockChange = onAodSingleClickBlockEnabledChange,
                                darkInputValue = aodDarkInput,
                                onDarkInputValueChange = { raw ->
                                    val digitsOnly = raw.filter { it.isDigit() }.take(3)
                                    aodDarkInput = digitsOnly
                                    digitsOnly.toIntOrNull()?.let { onAodInitDarkBrightnessChange(it.coerceIn(0, 255)) }
                                },
                                brightInputValue = aodBrightInput,
                                onBrightInputValueChange = { raw ->
                                    val digitsOnly = raw.filter { it.isDigit() }.take(3)
                                    aodBrightInput = digitsOnly
                                    digitsOnly.toIntOrNull()?.let { onAodInitBrightBrightnessChange(it.coerceIn(0, 255)) }
                                },
                                multiplierInputValue = aodMultiplierInput,
                                onMultiplierInputValueChange = { raw ->
                                    val sanitized = raw
                                        .replace(',', '.')
                                        .filterIndexed { index, c -> c.isDigit() || (c == '.' && index != 0) }
                                        .let { value ->
                                            val dotIndex = value.indexOf('.')
                                            if (dotIndex >= 0) {
                                                val head = value.substring(0, dotIndex + 1)
                                                val tail = value.substring(dotIndex + 1).replace(".", "")
                                                head + tail.take(2)
                                            } else {
                                                value.take(4)
                                            }
                                        }
                                    aodMultiplierInput = sanitized
                                    sanitized.toFloatOrNull()?.let {
                                        onAodRunningBrightnessMultiplierChange(it.coerceIn(1.0f, 3.0f))
                                    }
                                },
                                customColors = customColors,
                                groupPosition = GroupPosition.Single,
                                isCosxStyle = isCosxStyle
                            )
                        }
                    }

                    FeatureCategory.DoublePower -> {
                        item {
                            FeatureDoublePowerCard(
                                icon = AppIcons.Phone,
                                title = stringResource(R.string.feature_double_power_custom_launch_title),
                                description = stringResource(R.string.feature_double_power_custom_launch_summary),
                                checked = doublePowerCustomEnabled,
                                onCheckedChange = onDoublePowerCustomEnabledChange,
                                packageValue = doublePowerTargetPackage,
                                onPackageValueChange = onDoublePowerTargetPackageChange,
                                activityValue = doublePowerTargetActivity,
                                onActivityValueChange = onDoublePowerTargetActivityChange,
                                customColors = customColors,
                                groupPosition = GroupPosition.Single,
                                isCosxStyle = isCosxStyle
                            )
                        }
                    }

                    FeatureCategory.Assistant -> {
                        item {
                            SettingSwitchCard(
                                icon = AppIcons.Tune,
                                title = stringResource(R.string.feature_assistant_power_title),
                                description = stringResource(R.string.feature_assistant_power_summary),
                                checked = assistantPowerMode == LspConfig.ASSISTANT_POWER_MODE_GEMINI,
                                onCheckedChange = { enabled ->
                                    onAssistantPowerModeChange(
                                        if (enabled) {
                                            LspConfig.ASSISTANT_POWER_MODE_GEMINI
                                        } else {
                                            LspConfig.ASSISTANT_POWER_MODE_NONE
                                        }
                                    )
                                },
                                customColors = customColors,
                                groupPosition = GroupPosition.Top,
                                isCosxStyle = isCosxStyle
                            )
                        }
                        item {
                            SettingSwitchCard(
                                icon = Icons.Rounded.Search,
                                title = stringResource(R.string.feature_assistant_gesture_title),
                                description = stringResource(R.string.feature_assistant_gesture_summary),
                                checked = assistantGestureCircleEnabled,
                                onCheckedChange = onAssistantGestureCircleEnabledChange,
                                customColors = customColors,
                                groupPosition = GroupPosition.Bottom,
                                isCosxStyle = isCosxStyle
                            )
                        }
                    }

                    FeatureCategory.Localizer -> {
                        item {
                            FeatureOosLocalizerCard(
                                icon = AppIcons.Tune,
                                title = stringResource(R.string.feature_oos_localizer_title),
                                description = stringResource(R.string.feature_oos_localizer_summary),
                                checked = oosLocalizerEnabled,
                                onCheckedChange = onOosLocalizerEnabledChange,
                                customColors = customColors,
                                groupPosition = GroupPosition.Single,
                                isCosxStyle = isCosxStyle
                            )
                        }
                    }

                    FeatureCategory.Experimental -> {
                        item {
                            SettingSwitchCard(
                                icon = Icons.Rounded.Refresh,
                                iconResId = R.drawable.ic_extreme_refresh_165,
                                iconModifier = Modifier
                                    .width(32.dp)
                                    .height(20.dp)
                                    .padding(top = 1.dp),
                                title = stringResource(R.string.feature_extreme_refresh_165_title),
                                description = stringResource(R.string.feature_extreme_refresh_165_summary),
                                checked = extremeRefresh165Enabled,
                                onCheckedChange = onExtremeRefresh165EnabledChange,
                                customColors = customColors,
                                groupPosition = GroupPosition.Single,
                                isCosxStyle = isCosxStyle
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class FeatureCategory(
    @param:StringRes val titleRes: Int,
    @param:StringRes val summaryRes: Int,
    val fallbackIcon: ImageVector,
    val iconPackages: List<String>,
    val iconScale: Float = 1f
) {
    Desktop(
        titleRes = R.string.section_system_desktop,
        summaryRes = R.string.feature_category_desktop_summary,
        fallbackIcon = AppIcons.Widgets,
        iconPackages = listOf(
            "com.android.launcher",
            "com.android.launcher3",
            "com.oplus.launcher",
            "com.oneplus.launcher",
            "com.android.systemui"
        )
    ),
    SystemUi(
        titleRes = R.string.section_lsp,
        summaryRes = R.string.feature_category_system_ui_summary,
        fallbackIcon = Icons.Rounded.Notifications,
        iconPackages = listOf("com.android.systemui")
    ),
    Settings(
        titleRes = R.string.tab_settings,
        summaryRes = R.string.feature_category_settings_summary,
        fallbackIcon = AppIcons.Tune,
        iconPackages = listOf("com.android.settings")
    ),
    Aod(
        titleRes = R.string.feature_aod_enhance_title,
        summaryRes = R.string.feature_category_aod_summary,
        fallbackIcon = AppIcons.LightMode,
        iconPackages = listOf(
            "com.oplus.aod",
            "com.oneplus.aod",
            "com.coloros.aod",
            "com.android.systemui"
        )
    ),
    DoublePower(
        titleRes = R.string.feature_double_power_title,
        summaryRes = R.string.feature_double_power_summary,
        fallbackIcon = AppIcons.Phone,
        iconPackages = listOf(
            "com.oplus.doublewake.settings",
            "com.android.systemui",
            "android"
        )
    ),
    Assistant(
        titleRes = R.string.feature_assistant_title,
        summaryRes = R.string.feature_assistant_summary,
        fallbackIcon = AppIcons.Tune,
        iconPackages = listOf(
            "com.heytap.speechassist",
            "com.google.android.googlequicksearchbox",
            "com.android.systemui",
            "android"
        )
    ),
    Localizer(
        titleRes = R.string.feature_oos_localizer_title,
        summaryRes = R.string.feature_oos_localizer_home_summary,
        fallbackIcon = AppIcons.Tune,
        iconPackages = listOf(
            "com.oplus.aimemory",
            "com.heytap.speechassist",
            "com.coloros.shortcuts",
            "android"
        )
    ),
    Experimental(
        titleRes = R.string.section_experimental,
        summaryRes = R.string.feature_category_experimental_summary,
        fallbackIcon = AppIcons.Extension,
        iconPackages = listOf("android")
    )
}

private fun resolveSystemCategoryIcon(
    context: Context,
    packageCandidates: List<String>
): Drawable? {
    val pm = context.packageManager
    for (pkg in packageCandidates) {
        val icon = runCatching {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                val resolveInfo = pm.resolveActivity(
                    launchIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                resolveInfo?.loadIcon(pm)
            } else {
                null
            } ?: pm.getApplicationIcon(pkg)
        }.getOrNull()
        if (icon != null) return icon
    }
    return null
}

@Composable
private fun FeatureCategorySelectorCard(
    categories: List<FeatureCategory>,
    selectedCategory: FeatureCategory?,
    onCategorySelected: (FeatureCategory) -> Unit,
    customColors: Md3eCustomUiColors?,
    isCosxStyle: Boolean
) {
    val containerColor = if (isCosxStyle) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        categories.forEachIndexed { index, category ->
            val position = when {
                categories.size == 1 -> GroupPosition.Single
                index == 0 -> GroupPosition.Top
                index == categories.lastIndex -> GroupPosition.Bottom
                else -> GroupPosition.Middle
            }
            FeatureCategoryWideTile(
                category = category,
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                containerColor = containerColor,
                shape = if (isCosxStyle) cosxGroupedCardShape(position) else groupedCardShape(position)
            )
        }
    }
}

@Composable
private fun FeatureCategoryWideTile(
    category: FeatureCategory,
    selected: Boolean,
    onClick: () -> Unit,
    containerColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) {
        Color.White.copy(alpha = 0.88f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val backgroundColor = if (selected) {
        Color.White.copy(alpha = 0.10f)
    } else {
        containerColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.28f)),
                onClick = onClick
        ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = shape,
        border = null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeatureCategoryIcon(
                category = category,
                tint = tint,
                modifier = Modifier.size(42.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 10.dp)
            ) {
                Text(
                    text = stringResource(category.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(category.summaryRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FeatureCategoryIcon(
    category: FeatureCategory,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appIconDrawable = remember(category, context) {
        resolveSystemCategoryIcon(context, category.iconPackages)
    }
    val iconMaskShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(iconMaskShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        if (appIconDrawable != null) {
            val iconBitmap = remember(appIconDrawable) {
                appIconDrawable.toBitmap(width = 126, height = 126)
            }
            Image(
                bitmap = iconBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(iconMaskShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = category.fallbackIcon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun FeatureCategoryPageHeader(
    title: String,
    summary: String,
    onBack: () -> Unit,
    customColors: Md3eCustomUiColors?,
    isCosxStyle: Boolean
) {
    val containerColor = if (isCosxStyle) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val cardShape = if (isCosxStyle) {
        cosxGroupedCardShape(GroupPosition.Single)
    } else {
        groupedCardShape(GroupPosition.Single)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.tab_features),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureNumericSliderCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    currentValueText: String,
    sliderValue: Float,
    sliderRange: ClosedFloatingPointRange<Float>,
    onSliderValueChange: (Float) -> Unit,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    inputPlaceholder: String,
    hint: String,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    val containerColor = if (isCosxStyle) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp, end = 10.dp)
                ) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = description,
                        style = if (isCosxStyle) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                StyledFeatureSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    isCosxStyle = isCosxStyle
                )
            }

            Text(
                text = currentValueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp)
            )
            Slider(
                value = sliderValue.coerceIn(sliderRange.start, sliderRange.endInclusive),
                onValueChange = onSliderValueChange,
                valueRange = sliderRange,
                enabled = checked,
                modifier = Modifier.padding(top = 2.dp)
            )
            OutlinedTextField(
                value = inputValue,
                onValueChange = onInputValueChange,
                enabled = checked,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text(inputPlaceholder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun FeatureAodEnhanceCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    darkBrightness: Int,
    onDarkBrightnessChange: (Int) -> Unit,
    brightBrightness: Int,
    onBrightBrightnessChange: (Int) -> Unit,
    runningMultiplier: Float,
    onRunningMultiplierChange: (Float) -> Unit,
    panoramicSupportEnabled: Boolean,
    onPanoramicSupportChange: (Boolean) -> Unit,
    settingsSwitchEnabled: Boolean,
    onSettingsSwitchChange: (Boolean) -> Unit,
    singleClickBlockEnabled: Boolean,
    onSingleClickBlockChange: (Boolean) -> Unit,
    darkInputValue: String,
    onDarkInputValueChange: (String) -> Unit,
    brightInputValue: String,
    onBrightInputValueChange: (String) -> Unit,
    multiplierInputValue: String,
    onMultiplierInputValueChange: (String) -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    val containerColor = if (isCosxStyle) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp, end = 10.dp)
                ) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = description,
                        style = if (isCosxStyle) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                StyledFeatureSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    isCosxStyle = isCosxStyle
                )
            }

            Text(
                text = stringResource(R.string.feature_aod_brightness_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = stringResource(R.string.feature_aod_dark_brightness_current, darkBrightness),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Slider(
                value = darkBrightness.toFloat(),
                onValueChange = { onDarkBrightnessChange(it.roundToInt().coerceIn(0, 255)) },
                valueRange = 0f..255f,
                enabled = checked,
                modifier = Modifier.padding(top = 2.dp)
            )
            OutlinedTextField(
                value = darkInputValue,
                onValueChange = onDarkInputValueChange,
                enabled = checked,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text(stringResource(R.string.feature_aod_dark_brightness_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Text(
                text = stringResource(R.string.feature_aod_bright_brightness_current, brightBrightness),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 10.dp)
            )
            Slider(
                value = brightBrightness.toFloat(),
                onValueChange = { onBrightBrightnessChange(it.roundToInt().coerceIn(0, 255)) },
                valueRange = 0f..255f,
                enabled = checked,
                modifier = Modifier.padding(top = 2.dp)
            )
            OutlinedTextField(
                value = brightInputValue,
                onValueChange = onBrightInputValueChange,
                enabled = checked,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text(stringResource(R.string.feature_aod_bright_brightness_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Text(
                text = stringResource(
                    R.string.feature_aod_multiplier_current,
                    String.format("%.1f", runningMultiplier)
                ),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 10.dp)
            )
            Slider(
                value = runningMultiplier.coerceIn(1.0f, 3.0f),
                onValueChange = { onRunningMultiplierChange(it.coerceIn(1.0f, 3.0f)) },
                valueRange = 1.0f..3.0f,
                steps = 19,
                enabled = checked,
                modifier = Modifier.padding(top = 2.dp)
            )
            OutlinedTextField(
                value = multiplierInputValue,
                onValueChange = onMultiplierInputValueChange,
                enabled = checked,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text(stringResource(R.string.feature_aod_multiplier_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Text(
                text = stringResource(R.string.feature_aod_function_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
            FeatureSubToggleRow(
                title = stringResource(R.string.feature_aod_panoramic_title),
                checked = panoramicSupportEnabled,
                onCheckedChange = onPanoramicSupportChange,
                enabled = checked,
                isCosxStyle = isCosxStyle
            )
            FeatureSubToggleRow(
                title = stringResource(R.string.feature_aod_settings_switch_title),
                checked = settingsSwitchEnabled,
                onCheckedChange = onSettingsSwitchChange,
                enabled = checked,
                isCosxStyle = isCosxStyle
            )
            FeatureSubToggleRow(
                title = stringResource(R.string.feature_aod_single_click_block_title),
                checked = singleClickBlockEnabled,
                onCheckedChange = onSingleClickBlockChange,
                enabled = checked,
                isCosxStyle = isCosxStyle
            )
        }
    }
}

@Composable
private fun FeatureDoublePowerCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    packageValue: String,
    onPackageValueChange: (String) -> Unit,
    activityValue: String,
    onActivityValueChange: (String) -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCosxStyle -> MaterialTheme.colorScheme.surfaceContainerLow
                else -> customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCosxStyle) 14.dp else 16.dp, vertical = if (isCosxStyle) 14.dp else 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 1.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp, end = 12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = if (isCosxStyle) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                StyledFeatureSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    isCosxStyle = isCosxStyle
                )
            }
            AnimatedVisibility(
                visible = checked,
                enter = expandVertically(animationSpec = tween(durationMillis = 220)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 180)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = packageValue,
                        onValueChange = { onPackageValueChange(it.trim()) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.feature_double_power_package_label)) },
                        placeholder = { Text(stringResource(R.string.feature_double_power_package_hint)) }
                    )
                    OutlinedTextField(
                        value = activityValue,
                        onValueChange = { onActivityValueChange(it.trim()) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.feature_double_power_activity_label)) },
                        placeholder = { Text(stringResource(R.string.feature_double_power_activity_hint)) }
                    )
                    Text(
                        text = stringResource(R.string.feature_double_power_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureAssistantPowerCard(
    icon: ImageVector,
    title: String,
    description: String,
    selectedMode: Int,
    onModeChange: (Int) -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    val modes = listOf(
        LspConfig.ASSISTANT_POWER_MODE_NONE to R.string.feature_assistant_power_mode_none,
        LspConfig.ASSISTANT_POWER_MODE_GEMINI to R.string.feature_assistant_power_mode_default
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCosxStyle -> MaterialTheme.colorScheme.surfaceContainerLow
                else -> customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = if (isCosxStyle) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCosxStyle) {
                    CosxSegmentedButtons(
                        options = modes,
                        selectedValue = selectedMode,
                        onValueChange = onModeChange
                    )
                } else {
                    modes.forEachIndexed { index, (value, labelRes) ->
                        FilterChip(
                            selected = selectedMode == value,
                            onClick = { onModeChange(value) },
                            label = { Text(text = stringResource(labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        if (index != modes.lastIndex) {
                            Box(modifier = Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureOosLocalizerCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCosxStyle -> MaterialTheme.colorScheme.surfaceContainerLow
                else -> customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCosxStyle) 14.dp else 16.dp, vertical = if (isCosxStyle) 14.dp else 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp, end = 12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = if (isCosxStyle) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                StyledFeatureSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    isCosxStyle = isCosxStyle
                )
            }
        }
    }
}

@Composable
private fun FeatureSubToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    isCosxStyle: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        StyledFeatureSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            isCosxStyle = isCosxStyle
        )
    }
}

@Composable
private fun StyledFeatureSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    isCosxStyle: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val showSwitchIcons = LocalSwitchIconVisibility.current
    if (isCosxStyle) {
        CosxSwitch(
            checked = checked,
            onCheckedChange = { if (enabled) onCheckedChange(it) }
        )
    } else {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            thumbContent = if (showSwitchIcons) {
                {
                    Icon(
                        imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            } else {
                null
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = scheme.primaryContainer,
                checkedTrackColor = scheme.primary,
                checkedIconColor = scheme.onPrimaryContainer,
                uncheckedThumbColor = scheme.outline,
                uncheckedTrackColor = scheme.surfaceVariant,
                uncheckedBorderColor = scheme.outline,
                uncheckedIconColor = scheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun DesktopAssistantExpandableCard(
    customColors: Md3eCustomUiColors?,
    isCosxStyle: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedOption by rememberSaveable { mutableStateOf(AssistantScreenOption.Default) }
    var applying by rememberSaveable { mutableStateOf(false) }
    val selectedAction = assistantActionOptions.firstOrNull { it.option == selectedOption }

    LaunchedEffect(Unit) {
        val currentOption = queryAssistantScreenOption()
        selectedOption = currentOption
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCosxStyle) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(GroupPosition.Single) else groupedCardShape(GroupPosition.Single),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !applying) { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .padding(start = 14.dp, end = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.event_page_tool_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.event_page_tool_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier.widthIn(min = 52.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = selectedAction?.let { stringResource(it.titleRes) } ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        offset = DpOffset(x = 8.dp, y = 2.dp)
                    ) {
                        assistantActionOptions.forEach { action ->
                            val isSelected = selectedOption == action.option
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(action.titleRes),
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    if (applying) return@DropdownMenuItem
                                    expanded = false
                                    selectedOption = action.option
                                    scope.launch {
                                        applying = true
                                        val result = applyAssistantScreenOption(action.option)
                                        applying = false
                                        if (!result.success) {
                                            AppLogStore.w(
                                                "DesktopAssistant",
                                                "Apply failed in UI: ${
                                                    result.detail ?: context.getString(R.string.common_unknown)
                                                }"
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopRegionExpandableCard(
    regionMode: Int,
    onRegionModeChange: (Int) -> Unit,
    customColors: Md3eCustomUiColors?,
    isCosxStyle: Boolean = false
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedAction = launcherRegionOptions.firstOrNull { it.mode == regionMode } ?: launcherRegionOptions.first()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCosxStyle) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(GroupPosition.Single) else groupedCardShape(GroupPosition.Single),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.74f)
                        .padding(start = 14.dp, end = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.feature_launcher_region_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.feature_launcher_region_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier.widthIn(min = 52.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(selectedAction.titleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        offset = DpOffset(x = 8.dp, y = 2.dp)
                    ) {
                        launcherRegionOptions.forEach { action ->
                            val isSelected = regionMode == action.mode
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(action.titleRes),
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    onRegionModeChange(action.mode)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Md3eHomeScreen(
    customColors: Md3eCustomUiColors?,
    uiStyleMode: UiStyleMode
) {
    val isCosxStyle = uiStyleMode == UiStyleMode.Cosx
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val info = rememberSystemInfo()
    val unknownText = stringResource(R.string.common_unknown)
    val dashboardInfo by produceState(
        initialValue = defaultHomeDashboardInfo(context),
        key1 = context
    ) {
        while (true) {
            value = queryHomeDashboardInfo(context)
            delay(15_000)
        }
    }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            LsposedScopeRequester.initialize(context)
        }
    }
    val rootAccessInfo by produceState(
        initialValue = readCachedRootAccessInfo(context)
            ?: RootAccessInfo(state = RootAccessState.Checking),
        key1 = context
    ) {
        while (true) {
            value = withContext(Dispatchers.IO) {
                queryRootAccess(context)
            }
            delay(15_000)
        }
    }
    val lspSnapshot by produceState(
        initialValue = LsposedScopeRequester.cachedSnapshot(context),
        key1 = context
    ) {
        while (true) {
            value = withContext(Dispatchers.IO) {
                LsposedScopeRequester.snapshot(context)
            }
            delay(900)
        }
    }
    val lspVersionText by produceState(
        initialValue = lspSnapshot.frameworkVersionText ?: unknownText,
        key1 = context,
        key2 = lspSnapshot.frameworkVersionText
    ) {
        value = lspSnapshot.frameworkVersionText ?: withContext(Dispatchers.IO) {
            queryLsposedManagerVersion(context)
        }
    }

    val lspUiState = when {
        lspSnapshot.moduleEnabled && lspSnapshot.hasRequiredScopes -> LspUiState.READY
        lspSnapshot.moduleEnabled -> LspUiState.MISSING_SCOPE
        else -> LspUiState.NOT_READY
    }
    val allActivated = rootAccessInfo.state == RootAccessState.Granted && lspUiState == LspUiState.READY
    val inactiveContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.62f)
    val inactiveOnColor = MaterialTheme.colorScheme.onErrorContainer
    val homeCardShape = if (isCosxStyle) RoundedCornerShape(18.dp) else MaterialTheme.shapes.large
    val infoCardColor = if (isCosxStyle) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        customColors?.infoCard ?: MaterialTheme.colorScheme.surfaceContainerLow
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = pageHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PageTopHeader(
                title = stringResource(R.string.md3e_home_title),
                showHomeIcons = true,
                titleOffsetY = 2.dp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (allActivated) {
                        if (isCosxStyle) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            customColors?.statusCard ?: MaterialTheme.colorScheme.secondaryContainer
                        }
                    } else {
                        inactiveContainerColor
                    }
                ),
                shape = homeCardShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp)
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (allActivated) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                        contentDescription = null,
                        tint = if (allActivated) {
                            customColors?.statusOnCard ?: MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            inactiveOnColor
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = if (allActivated) {
                                stringResource(R.string.status_activated)
                            } else {
                                stringResource(R.string.status_inactive)
                            },
                            color = if (allActivated) {
                                customColors?.statusOnCard ?: MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                inactiveOnColor
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val blockSpacing = 8.dp
                val panelWidth = ((maxWidth - blockSpacing).coerceAtLeast(0.dp)) / 2

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(blockSpacing),
                    verticalAlignment = Alignment.Top
                ) {
                    Card(
                        modifier = Modifier
                            .width(panelWidth)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = infoCardColor
                        ),
                        shape = homeCardShape
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 96.dp)
                                .padding(horizontal = 10.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = stringResource(R.string.root_access_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = when (rootAccessInfo.state) {
                                    RootAccessState.Checking -> stringResource(R.string.root_access_status_checking)
                                    RootAccessState.Granted -> stringResource(R.string.root_access_status_granted)
                                    RootAccessState.NotGranted -> stringResource(R.string.root_access_status_denied)
                                    RootAccessState.Error -> stringResource(R.string.root_access_status_error)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (rootAccessInfo.state == RootAccessState.Granted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(top = 6.dp),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = rootAccessInfo.managerVersion
                                    ?: stringResource(R.string.common_unknown),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .width(panelWidth)
                            .fillMaxHeight()
                            .clip(homeCardShape)
                            .clickable {
                                scope.launch {
                                    requestLsposedScopeFromHome(context)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = infoCardColor
                        ),
                        shape = homeCardShape
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 96.dp)
                                .padding(horizontal = 10.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = stringResource(R.string.lsp_status_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = when (lspUiState) {
                                    LspUiState.READY -> stringResource(R.string.lsp_status_ready)
                                    LspUiState.MISSING_SCOPE -> stringResource(R.string.lsp_status_missing_scope)
                                    LspUiState.NOT_READY -> stringResource(R.string.lsp_status_not_ready)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (lspUiState == LspUiState.READY) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(top = 6.dp),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = lspVersionText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }

        item {
            HomeDashboardCard(
                info = dashboardInfo,
                systemInfo = info,
                cardShape = homeCardShape,
                infoCardColor = infoCardColor
            )
        }
    }
}

@Composable
private fun Md3eBatteryScreen(
    customColors: Md3eCustomUiColors?,
    uiStyleMode: UiStyleMode
) {
    val context = LocalContext.current
    val isCosxStyle = uiStyleMode == UiStyleMode.Cosx
    val infoCardColor = if (isCosxStyle) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        customColors?.infoCard ?: MaterialTheme.colorScheme.surfaceContainerLow
    }
    val cardShape = if (isCosxStyle) RoundedCornerShape(18.dp) else MaterialTheme.shapes.large
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        AppLogStore.i("Battery", "Notification permission granted: $granted")
        BatteryLiveNotificationService.sync(context)
    }
    var liveNotificationEnabled by remember {
        mutableStateOf(BatteryPreferences.isLiveNotificationEnabled(context))
    }
    var keepBackgroundRunning by remember {
        mutableStateOf(BatteryPreferences.keepBackgroundRunning(context))
    }
    var dualCellMode by remember {
        mutableStateOf(BatteryPreferences.isDualCellMode(context))
    }
    var seriesBatteryMode by remember {
        mutableStateOf(BatteryPreferences.isSeriesBatteryMode(context))
    }
    var refreshIntervalMs by remember {
        mutableStateOf(BatteryPreferences.infoRefreshIntervalMs(context))
    }
    val backgroundRunningPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val allowed = context.isIgnoringBatteryOptimizations()
        keepBackgroundRunning = allowed
        BatteryPreferences.setKeepBackgroundRunning(context, allowed)
        BatteryLiveNotificationService.sync(context)
    }
    val batteryListState = rememberLazyListState()
    val batterySnapshot by produceState(
        initialValue = BatteryMonitor.query(context, includeOplus = false),
        key1 = context,
        key2 = refreshIntervalMs
    ) {
        var lastChargingState = value.isCharging
        while (true) {
            if (batteryListState.isScrollInProgress) {
                delay(300)
                continue
            }
            val snapshot = withContext(Dispatchers.IO) {
                BatteryMonitor.query(context)
            }
            value = snapshot
            if (snapshot.isCharging != lastChargingState) {
                BatteryLiveNotificationService.sync(context)
                lastChargingState = snapshot.isCharging
            }
            delay(refreshIntervalMs.toLong())
        }
    }

    LaunchedEffect(Unit) {
        if (keepBackgroundRunning && !context.isIgnoringBatteryOptimizations()) {
            keepBackgroundRunning = false
            BatteryPreferences.setKeepBackgroundRunning(context, false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            BatteryLiveNotificationService.sync(context)
        }
    }

    LazyColumn(
        state = batteryListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = pageHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PageTopHeader(
                title = stringResource(R.string.tab_battery),
                showHomeIcons = false
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (batterySnapshot.isCharging) {
                        customColors?.statusCard ?: MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        infoCardColor
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = batterySnapshot.chargeIconResId()),
                            contentDescription = null,
                            tint = if (batterySnapshot.isCharging) {
                                customColors?.statusOnCard ?: MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(28.dp)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.battery_level_percent, batterySnapshot.percent),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (batterySnapshot.isCharging) {
                                    customColors?.statusOnCard ?: MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = batterySnapshot.statusText(context),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (batterySnapshot.isCharging) {
                                    (customColors?.statusOnCard ?: MaterialTheme.colorScheme.onSecondaryContainer)
                                        .copy(alpha = 0.82f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(batterySnapshot.percent / 100f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (batterySnapshot.isCharging) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    }
                                )
                        )
                    }

                }
            }
        }

        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val spacing = 8.dp
                val panelWidth = ((maxWidth - spacing).coerceAtLeast(0.dp)) / 2
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        BatteryMetricCard(
                            title = stringResource(R.string.battery_metric_power),
                            value = batterySnapshot.powerWatts?.let {
                                stringResource(R.string.battery_power_watts, it)
                            } ?: stringResource(R.string.common_unknown),
                            modifier = Modifier.width(panelWidth),
                            cardColor = infoCardColor,
                            cardShape = cardShape
                        )
                        BatteryMetricCard(
                            title = stringResource(R.string.battery_metric_temperature),
                            value = batterySnapshot.temperatureTenthsC?.let {
                                stringResource(R.string.battery_temperature_celsius, it / 10f)
                            } ?: stringResource(R.string.common_unknown),
                            modifier = Modifier.width(panelWidth),
                            cardColor = infoCardColor,
                            cardShape = cardShape
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        BatteryMetricCard(
                            title = stringResource(R.string.battery_metric_current),
                            value = batterySnapshot.displayCurrentMa?.let {
                                stringResource(R.string.battery_current_amps, kotlin.math.abs(it) / 1000f)
                            } ?: stringResource(R.string.common_unknown),
                            modifier = Modifier.width(panelWidth),
                            cardColor = infoCardColor,
                            cardShape = cardShape
                        )
                        BatteryMetricCard(
                            title = stringResource(R.string.battery_metric_voltage),
                            value = batterySnapshot.displayVoltageMv?.let {
                                stringResource(R.string.battery_voltage_volts, it / 1000f)
                            } ?: stringResource(R.string.common_unknown),
                            modifier = Modifier.width(panelWidth),
                            cardColor = infoCardColor,
                            cardShape = cardShape
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = infoCardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BatteryDetailInfoRows(
                        batterySnapshot = batterySnapshot,
                        context = context
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = infoCardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BatterySettingToggleRow(
                        title = stringResource(R.string.battery_setting_live_notification_title),
                        summary = stringResource(R.string.battery_setting_live_notification_summary),
                        checked = liveNotificationEnabled,
                        onCheckedChange = { enabled ->
                            liveNotificationEnabled = enabled
                            BatteryPreferences.setLiveNotificationEnabled(context, enabled)
                            BatteryLiveNotificationService.sync(context)
                        },
                        isCosxStyle = isCosxStyle
                    )
                    BatterySettingToggleRow(
                        title = stringResource(R.string.battery_setting_keep_background_title),
                        summary = stringResource(R.string.battery_setting_keep_background_summary),
                        checked = keepBackgroundRunning,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                keepBackgroundRunning = false
                                BatteryPreferences.setKeepBackgroundRunning(context, false)
                                BatteryLiveNotificationService.sync(context)
                                return@BatterySettingToggleRow
                            }
                            if (context.isIgnoringBatteryOptimizations()) {
                                keepBackgroundRunning = true
                                BatteryPreferences.setKeepBackgroundRunning(context, true)
                                BatteryLiveNotificationService.sync(context)
                                return@BatterySettingToggleRow
                            }
                            keepBackgroundRunning = false
                            runCatching {
                                backgroundRunningPermissionLauncher.launch(
                                    batteryOptimizationSettingsIntent(context)
                                )
                            }.onFailure {
                                BatteryPreferences.setKeepBackgroundRunning(context, false)
                                BatteryLiveNotificationService.sync(context)
                            }
                        },
                        isCosxStyle = isCosxStyle
                    )
                    BatterySettingToggleRow(
                        title = stringResource(R.string.battery_setting_dual_cell_title),
                        summary = stringResource(R.string.battery_setting_dual_cell_summary),
                        checked = dualCellMode,
                        onCheckedChange = { enabled ->
                            dualCellMode = enabled
                            BatteryPreferences.setDualCellMode(context, enabled)
                            BatteryLiveNotificationService.sync(context)
                        },
                        isCosxStyle = isCosxStyle
                    )
                    BatterySettingToggleRow(
                        title = stringResource(R.string.battery_setting_series_battery_title),
                        summary = stringResource(R.string.battery_setting_series_battery_summary),
                        checked = seriesBatteryMode,
                        onCheckedChange = { enabled ->
                            seriesBatteryMode = enabled
                            BatteryPreferences.setSeriesBatteryMode(context, enabled)
                            BatteryLiveNotificationService.sync(context)
                        },
                        isCosxStyle = isCosxStyle
                    )
                    BatterySettingRefreshIntervalRow(
                        title = stringResource(R.string.battery_setting_refresh_interval_title),
                        summary = stringResource(R.string.battery_setting_refresh_interval_summary),
                        selectedIntervalMs = refreshIntervalMs,
                        onIntervalSelected = { intervalMs ->
                            refreshIntervalMs = intervalMs
                            BatteryPreferences.setInfoRefreshIntervalMs(context, intervalMs)
                            BatteryLiveNotificationService.sync(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BatterySettingToggleRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    isCosxStyle: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                }
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.55f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        StyledFeatureSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            isCosxStyle = isCosxStyle
        )
    }
}

@Composable
private fun BatterySettingRefreshIntervalRow(
    title: String,
    summary: String,
    selectedIntervalMs: Int,
    onIntervalSelected: (Int) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Box(
            modifier = Modifier.width(92.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextButton(
                onClick = { expanded = true },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(
                        R.string.battery_refresh_interval_seconds,
                        selectedIntervalMs.refreshIntervalLabel()
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                batteryRefreshIntervalOptionsMs.forEach { intervalMs ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    R.string.battery_refresh_interval_seconds,
                                    intervalMs.refreshIntervalLabel()
                                )
                            )
                        },
                        onClick = {
                            expanded = false
                            onIntervalSelected(intervalMs)
                        },
                        trailingIcon = if (intervalMs == selectedIntervalMs) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

private fun Int.refreshIntervalLabel(): String {
    return if (this == 500) "0.5" else (this / 1000).toString()
}

@Composable
private fun BatteryMetricCard(
    title: String,
    value: String,
    modifier: Modifier,
    cardColor: Color,
    cardShape: Shape
) {
    Card(
        modifier = modifier.heightIn(min = 92.dp),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BatteryInfoGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (title, value) ->
            BatteryInfoRow(
                title = title,
                value = value
            )
        }
    }
}

@Composable
private fun BatteryInfoRow(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BatteryDetailInfoRows(
    batterySnapshot: BatterySnapshot,
    context: Context
) {
    val info = batterySnapshot.oplus
    val unknown = stringResource(R.string.common_unknown)
    val items = mutableListOf<Pair<String, String>>(
        stringResource(R.string.battery_metric_plugged) to batterySnapshot.pluggedText(context)
    )

    if (info != null) {
        items += stringResource(R.string.battery_metric_charging_protocol) to (
            info.chargingProtocol?.displayText(context) ?: unknown
            )
    }

    items += stringResource(R.string.battery_metric_health) to batterySnapshot.healthText(context)

    if (info != null) {
        items += stringResource(R.string.battery_metric_soh) to (
            info.stateOfHealthPercent?.let { stringResource(R.string.battery_percent_value, it) }
                ?: unknown
            )
    }

    items += listOf(
        stringResource(R.string.battery_metric_cycle_count) to (
            batterySnapshot.cycleCount?.toString() ?: unknown
            ),
        stringResource(R.string.battery_metric_technology) to (
            batterySnapshot.technology ?: unknown
            )
    )

    if (info != null) {
        items += stringResource(R.string.battery_metric_battery_type) to (
            info.batteryType ?: unknown
            )
        items += listOf(
            stringResource(R.string.battery_metric_remaining_capacity) to (
                info.remainingCapacityMah?.let { stringResource(R.string.battery_capacity_mah, it) }
                    ?: unknown
                ),
            stringResource(R.string.battery_metric_full_charge_capacity) to (
                info.fullChargeCapacityMah?.let { stringResource(R.string.battery_capacity_mah, it) }
                    ?: unknown
                ),
            stringResource(R.string.battery_metric_qmax) to (
                info.qmaxMah?.let { stringResource(R.string.battery_capacity_mah, it) }
                    ?: unknown
                )
        )
    }

    items += listOf(
        stringResource(R.string.battery_metric_charge_counter) to (
            batterySnapshot.chargeCounterUah?.let {
                stringResource(R.string.battery_charge_counter_mah, it / 1000f)
            } ?: unknown
            ),
        stringResource(R.string.battery_metric_energy_counter) to (
            batterySnapshot.energyCounterNwh?.let {
                stringResource(R.string.battery_energy_counter_wh, it / 1_000_000f)
            } ?: unknown
            )
    )

    if (info != null) {
        items += listOf(
            stringResource(R.string.battery_metric_bcc_current) to (
                info.bccCurrentMa?.let { stringResource(R.string.battery_current_ma, it) }
                    ?: unknown
                ),
            stringResource(R.string.battery_metric_bcc_voltage) to (
                when {
                    info.voltage0Mv != null && info.voltage1Mv != null ->
                        stringResource(R.string.battery_dual_voltage_mv, info.voltage0Mv, info.voltage1Mv)
                    info.voltage0Mv != null -> stringResource(R.string.battery_voltage_mv, info.voltage0Mv)
                    else -> unknown
                }
                ),
            stringResource(R.string.battery_metric_under_voltage) to (
                info.underVoltageThresholdMv?.let { stringResource(R.string.battery_voltage_mv, it) }
                    ?: unknown
                ),
            stringResource(R.string.battery_metric_manufacture_date) to (
                info.manufactureDate ?: unknown
                ),
            stringResource(R.string.battery_metric_serial_number) to (
                info.serialNumber ?: unknown
                )
        )
    }

    BatteryInfoGrid(items = items)

    if (info == null) {
        Text(
            text = stringResource(R.string.battery_oplus_root_required),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun BatterySnapshot.chargeIconResId(): Int {
    return if (oplus?.chargingProtocol?.isFastCharging == true) {
        R.drawable.stat_charge_super_vooc
    } else {
        R.drawable.stat_charge_normal
    }
}

private fun OplusChargingProtocolInfo.displayText(context: Context): String {
    val protocolName = when {
        isPpsCharging || protocolCode in setOf(4, 5) || fastChargeCode in setOf(4, 5) ->
            context.getString(R.string.battery_protocol_pps)
        isVoocCharging || protocolCode == 6 || fastChargeCode == 6 ->
            context.getString(R.string.battery_protocol_supervooc)
        protocolCode == 1 || fastChargeCode == 1 || chargeTechnologyCode == 6 ->
            context.getString(R.string.battery_protocol_vooc)
        protocolCode == 2 || fastChargeCode == 2 ->
            context.getString(R.string.battery_protocol_qc)
        protocolCode == 3 || fastChargeCode == 3 || chargeTechnologyCode == 7 ->
            context.getString(R.string.battery_protocol_pd)
        protocolCode == 0 && fastChargeCode == 0 -> context.getString(R.string.battery_protocol_standard)
        protocolCode != null || fastChargeCode != null || chargeTechnologyCode != null ->
            context.getString(R.string.battery_protocol_unknown_code, firstKnownProtocolCode())
        else -> null
    } ?: return context.getString(R.string.common_unknown)

    return ppsPower
        ?.takeIf { isPpsCharging || protocolCode in setOf(4, 5) || fastChargeCode in setOf(4, 5) }
        ?.let { context.getString(R.string.battery_protocol_with_power, protocolName, it) }
        ?: protocolName
}

private fun OplusChargingProtocolInfo.firstKnownProtocolCode(): Int {
    return protocolCode ?: fastChargeCode ?: chargeTechnologyCode ?: 0
}

private fun BatterySnapshot.statusText(context: Context): String {
    return when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.battery_status_charging)
        BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.battery_status_discharging)
        BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.battery_status_full)
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.battery_status_not_charging)
        else -> context.getString(R.string.common_unknown)
    }
}

private fun BatterySnapshot.healthText(context: Context): String {
    return when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.battery_health_good)
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.battery_health_overheat)
        BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.battery_health_dead)
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.battery_health_over_voltage)
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.battery_health_failure)
        BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.battery_health_cold)
        else -> context.getString(R.string.common_unknown)
    }
}

private fun BatterySnapshot.pluggedText(context: Context): String {
    return when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.battery_plugged_ac)
        BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.battery_plugged_usb)
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.battery_plugged_wireless)
        BatteryManager.BATTERY_PLUGGED_DOCK -> context.getString(R.string.battery_plugged_dock)
        else -> context.getString(R.string.battery_plugged_unknown)
    }
}

private data class SystemInfo(
    val version: String,
    val packageName: String,
    val systemVersion: String,
    val deviceModel: String,
    val architecture: String
)

private data class HomeDashboardInfo(
    val androidApiText: String,
    val buildText: String,
    val regionText: String
)

private fun defaultHomeDashboardInfo(context: Context): HomeDashboardInfo {
    val locale = runCatching {
        context.resources.configuration.locales[0]
    }.getOrDefault(Locale.getDefault())
    val localeRegionCode = locale.country.ifBlank { "XX" }.uppercase(Locale.ROOT)
    val regionCode = queryRegionCodeFromSystemProperties() ?: localeRegionCode
    return HomeDashboardInfo(
        androidApiText = context.getString(
            R.string.home_dash_android_api_format,
            Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT
        ),
        buildText = Build.DISPLAY.ifBlank { "${Build.MODEL}_${Build.ID}" },
        regionText = regionCode
    )
}

@Composable
private fun HomeDashboardCard(
    info: HomeDashboardInfo,
    systemInfo: SystemInfo,
    cardShape: Shape,
    infoCardColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = infoCardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .widthIn(min = 0.dp)
                ) {
                    Text(
                        text = systemInfo.deviceModel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = info.buildText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = info.regionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .width(56.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(vertical = 6.dp),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoRow(stringResource(R.string.info_version), systemInfo.version)
                    InfoRow(stringResource(R.string.info_package_name), systemInfo.packageName)
                    InfoRow(stringResource(R.string.info_system_version), info.androidApiText)
                    InfoRow(stringResource(R.string.info_architecture), systemInfo.architecture)
                }
            }
        }
    }
}

private suspend fun queryHomeDashboardInfo(context: Context): HomeDashboardInfo = withContext(Dispatchers.IO) {
    val locale = runCatching {
        context.resources.configuration.locales[0]
    }.getOrDefault(Locale.getDefault())
    val propertyRegionCode = queryRegionCodeFromSystemProperties()
    val localeRegionCode = locale.country.ifBlank { "XX" }.uppercase(Locale.ROOT)
    val countryCode = propertyRegionCode ?: localeRegionCode
    val regionText = countryCode

    HomeDashboardInfo(
        androidApiText = context.getString(
            R.string.home_dash_android_api_format,
            Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT
        ),
        buildText = Build.DISPLAY.ifBlank { "${Build.MODEL}_${Build.ID}" },
        regionText = regionText
    )
}

private fun queryRegionCodeFromSystemProperties(): String? {
    val keys = listOf(
        "ro.oplus.regionmark",
        "ro.oplus.region",
        "ro.vendor.oplus.regionmark",
        "persist.sys.oplus.region",
        "ro.product.locale.region"
    )
    val raw = keys.asSequence()
        .mapNotNull { readSystemProperty(it) }
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }
        ?: return null

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

    return mapped?.takeIf { it.length in 2..3 && it.all { ch -> ch in 'A'..'Z' } }
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

private val lsposedManagerPackages = listOf(
    "org.lsposed.manager",
    "io.github.libxposed.manager"
)

private fun requestLsposedScopeFromHome(context: android.content.Context): Boolean {
    if (LsposedScopeRequester.requestRequiredScopes()) {
        AppLogStore.i("LSPosed", "Requested required scopes from Home card")
        return true
    }

    return openLsposedModulePage(context)
}

private fun openLsposedModulePage(context: android.content.Context): Boolean {
    val managerPackage = "org.lsposed.manager"
    val modulePageIntents = listOf(
        "lsposed://module/${context.packageName}",
        "lsp://module/${context.packageName}"
    ).map { uri ->
        Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            .setPackage(managerPackage)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    modulePageIntents.forEach { intent ->
        if (runCatching { context.startActivity(intent) }.isSuccess) {
            AppLogStore.i("LSPosed", "Opened LSPosed module page")
            return true
        }
    }

    val launchIntent = context.packageManager
        .getLaunchIntentForPackage(managerPackage)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    if (launchIntent != null && runCatching { context.startActivity(launchIntent) }.isSuccess) {
        AppLogStore.i("LSPosed", "Opened LSPosed Manager fallback")
        return true
    }

    AppLogStore.w("LSPosed", "Scope request/deep link failed from Home card")
    return false
}

private fun queryLsposedManagerVersion(context: android.content.Context): String {
    val unknownText = context.getString(R.string.common_unknown)
    lsposedManagerPackages.forEach { pkg ->
        val value = runCatching {
            val packageInfo = context.packageManager.getPackageInfo(pkg, 0)
            val versionName = packageInfo.versionName?.trim().orEmpty()
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }

            when {
                versionName.isNotBlank() && versionCode.isNotBlank() -> "$versionName ($versionCode)"
                versionName.isNotBlank() -> versionName
                versionCode.isNotBlank() -> versionCode
                else -> null
            }
        }.getOrNull()
        if (!value.isNullOrBlank()) {
            return value
        }
    }
    return unknownText
}

private fun isLsposedManagerInstalled(context: android.content.Context): Boolean {
    val pm = context.packageManager
    val hasKnownPackage = lsposedManagerPackages.any { pkg ->
        runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
    }
    if (hasKnownPackage) return true

    val deepLink = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("lsposed://module/${context.packageName}")
    ).setPackage("org.lsposed.manager")
    return runCatching {
        pm.resolveActivity(deepLink, 0) != null
    }.getOrDefault(false)
}

@Composable
private fun rememberSystemInfo(): SystemInfo {
    val context = LocalContext.current
    return remember(context) {
        val unknownText = context.getString(R.string.common_unknown)
        val packageName = context.packageName
        val version = runCatching {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName?.takeIf { it.isNotBlank() } ?: unknownText
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }

            context.getString(R.string.app_version_with_code, versionName, versionCode)
        }.getOrDefault(unknownText)

        val systemVersion = context.getString(
            R.string.system_version_format,
            Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT
        )
        val brand = Build.BRAND.orEmpty()
        val model = Build.MODEL.orEmpty()
        val deviceModel = if (brand.isNotBlank()) "$brand $model" else model.ifBlank { unknownText }
        val architecture = Build.SUPPORTED_ABIS.firstOrNull().orEmpty().ifBlank { unknownText }

        SystemInfo(
            version = version,
            packageName = packageName,
            systemVersion = systemVersion,
            deviceModel = deviceModel,
            architecture = architecture
        )
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Md3eSettingsScreen(
    predictiveBackEnabled: Boolean,
    onPredictiveBackEnabledChange: (Boolean) -> Unit,
    showLogsTab: Boolean,
    onShowLogsTabChange: (Boolean) -> Unit,
    showBatteryTab: Boolean,
    onShowBatteryTabChange: (Boolean) -> Unit,
    showChinaSpecialFeatures: Boolean,
    onShowChinaSpecialFeaturesChange: (Boolean) -> Unit,
    showGlobalSpecialFeatures: Boolean,
    onShowGlobalSpecialFeaturesChange: (Boolean) -> Unit,
    customMonetEnabled: Boolean,
    onCustomMonetEnabledChange: (Boolean) -> Unit,
    customMonetSeedColor: Int,
    onCustomMonetSeedColorChange: (Int) -> Unit,
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    appLanguageTag: String,
    onAppLanguageChange: (String) -> Unit,
    showSwitchIcons: Boolean,
    onShowSwitchIconsChange: (Boolean) -> Unit,
    customColors: Md3eCustomUiColors?,
    currentUiStyle: Int,
    onUiStyleChange: (Int) -> Unit,
    uiStyleMode: UiStyleMode,
    onConfigImported: () -> Unit
) {
    val context = LocalContext.current
    val isCosxStyle = uiStyleMode == UiStyleMode.Cosx
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var authorInfoExpanded by rememberSaveable { mutableStateOf(false) }
    var themeSettingsOpen by rememberSaveable { mutableStateOf(false) }
    val exportSuccessText = stringResource(R.string.config_export_success)
    val exportFailedText = stringResource(R.string.config_export_failed)
    val importSuccessText = stringResource(R.string.config_import_success)
    val importFailedText = stringResource(R.string.config_import_failed)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    val json = ConfigBackup.exportToJson(context)
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error("Cannot open config output stream")
                }.exceptionOrNull()
            }
            if (error == null) {
                AppLogStore.i("Settings", "Config exported")
                Toast.makeText(context, exportSuccessText, Toast.LENGTH_SHORT).show()
            } else {
                AppLogStore.w("Settings", "Export config failed: ${error.message.orEmpty()}")
                Toast.makeText(context, exportFailedText, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.readBytes().toString(Charsets.UTF_8)
                    } ?: error("Cannot open config input stream")
                    ConfigBackup.importFromJson(context, json)
                    LspConfig.syncTogglesForBoot(context)
                }.exceptionOrNull()
            }
            if (error == null) {
                onConfigImported()
                AppLogStore.i("Settings", "Config imported")
                Toast.makeText(context, importSuccessText, Toast.LENGTH_SHORT).show()
            } else {
                AppLogStore.w("Settings", "Import config failed: ${error.message.orEmpty()}")
                Toast.makeText(context, importFailedText, Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler(enabled = themeSettingsOpen) {
        themeSettingsOpen = false
    }

    AnimatedContent(
        targetState = themeSettingsOpen,
        transitionSpec = {
            val opening = targetState && !initialState
            fadeIn(animationSpec = tween(220)) +
                slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = 280,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetX = { full -> if (opening) full / 5 else -full / 5 }
                ) togetherWith
                fadeOut(animationSpec = tween(180)) +
                slideOutHorizontally(
                    animationSpec = tween(
                        durationMillis = 240,
                        easing = FastOutSlowInEasing
                    ),
                    targetOffsetX = { full -> if (opening) -full / 7 else full / 7 }
                )
        },
        label = "settings_theme_page_switch"
    ) { showThemeSettings ->
        if (showThemeSettings) {
            Md3eThemeSettingsScreen(
                predictiveBackEnabled = predictiveBackEnabled,
                onPredictiveBackEnabledChange = onPredictiveBackEnabledChange,
                customMonetEnabled = customMonetEnabled,
                onCustomMonetEnabledChange = onCustomMonetEnabledChange,
                customMonetSeedColor = customMonetSeedColor,
                onCustomMonetSeedColorChange = onCustomMonetSeedColorChange,
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                showSwitchIcons = showSwitchIcons,
                onShowSwitchIconsChange = onShowSwitchIconsChange,
                customColors = customColors,
                isCosxStyle = isCosxStyle,
                onBack = { themeSettingsOpen = false }
            )
        } else {
            Md3eSettingsMainContent(
                showLogsTab = showLogsTab,
                onShowLogsTabChange = onShowLogsTabChange,
                showBatteryTab = showBatteryTab,
                onShowBatteryTabChange = onShowBatteryTabChange,
                showChinaSpecialFeatures = showChinaSpecialFeatures,
                onShowChinaSpecialFeaturesChange = onShowChinaSpecialFeaturesChange,
                showGlobalSpecialFeatures = showGlobalSpecialFeatures,
                onShowGlobalSpecialFeaturesChange = onShowGlobalSpecialFeaturesChange,
                appLanguageTag = appLanguageTag,
                onAppLanguageChange = onAppLanguageChange,
                customColors = customColors,
                isCosxStyle = isCosxStyle,
                uriHandler = uriHandler,
                authorInfoExpanded = authorInfoExpanded,
                onAuthorInfoExpandedChange = { authorInfoExpanded = it },
                onThemeSettingsOpen = { themeSettingsOpen = true },
                onExportConfig = { exportLauncher.launch("mibox_config.json") },
                onImportConfig = { importLauncher.launch(arrayOf("application/json", "text/*", "*/*")) }
            )
        }
    }
}

@Composable
private fun Md3eSettingsMainContent(
    showLogsTab: Boolean,
    onShowLogsTabChange: (Boolean) -> Unit,
    showBatteryTab: Boolean,
    onShowBatteryTabChange: (Boolean) -> Unit,
    showChinaSpecialFeatures: Boolean,
    onShowChinaSpecialFeaturesChange: (Boolean) -> Unit,
    showGlobalSpecialFeatures: Boolean,
    onShowGlobalSpecialFeaturesChange: (Boolean) -> Unit,
    appLanguageTag: String,
    onAppLanguageChange: (String) -> Unit,
    customColors: Md3eCustomUiColors?,
    isCosxStyle: Boolean,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    authorInfoExpanded: Boolean,
    onAuthorInfoExpandedChange: (Boolean) -> Unit,
    onThemeSettingsOpen: () -> Unit,
    onExportConfig: () -> Unit,
    onImportConfig: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = pageHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            PageTopHeader(
                title = stringResource(R.string.tab_settings),
                showHomeIcons = false
            )
        }
        item {
            Spacer(modifier = Modifier.height(personalizationHeaderOffset))
        }
        if (isCosxStyle) {
            item {
                CosxSearchPlaceholderCard()
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        item {
            Text(
                text = stringResource(R.string.section_personalization),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        item {
            SettingLinkCard(
                icon = AppIcons.Palette,
                iconResId = null,
                title = stringResource(R.string.setting_theme_settings),
                description = stringResource(R.string.setting_theme_settings_summary),
                onClick = onThemeSettingsOpen,
                customColors = customColors,
                groupPosition = GroupPosition.Top,
                isCosxStyle = isCosxStyle
            )
        }

        item {
            SettingLanguageCard(
                icon = AppIcons.Tune,
                title = stringResource(R.string.setting_app_language),
                selectedLanguageTag = appLanguageTag,
                onLanguageChange = onAppLanguageChange,
                customColors = customColors,
                groupPosition = GroupPosition.Bottom,
                isCosxStyle = isCosxStyle
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Text(
                text = stringResource(R.string.section_restore_backup),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        item {
            SettingLinkCard(
                icon = AppIcons.Save,
                iconResId = null,
                title = stringResource(R.string.setting_export_config),
                description = stringResource(R.string.setting_export_config_summary),
                onClick = onExportConfig,
                customColors = customColors,
                groupPosition = GroupPosition.Top,
                isCosxStyle = isCosxStyle
            )
        }
        item {
            SettingLinkCard(
                icon = Icons.Rounded.Refresh,
                iconResId = null,
                title = stringResource(R.string.setting_import_config),
                description = stringResource(R.string.setting_import_config_summary),
                onClick = onImportConfig,
                customColors = customColors,
                groupPosition = GroupPosition.Bottom,
                isCosxStyle = isCosxStyle
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Text(
                text = stringResource(R.string.section_other_settings),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        item {
            SettingSwitchCard(
                icon = AppIcons.Event,
                title = stringResource(R.string.setting_show_logs),
                description = stringResource(R.string.setting_show_logs_summary),
                checked = showLogsTab,
                onCheckedChange = onShowLogsTabChange,
                customColors = customColors,
                groupPosition = GroupPosition.Top,
                isCosxStyle = isCosxStyle
            )
        }
        item {
            SettingSwitchCard(
                icon = AppIcons.Battery,
                title = stringResource(R.string.setting_show_battery),
                description = stringResource(R.string.setting_show_battery_summary),
                checked = showBatteryTab,
                onCheckedChange = onShowBatteryTabChange,
                customColors = customColors,
                groupPosition = GroupPosition.Middle,
                isCosxStyle = isCosxStyle
            )
        }
        item {
            SettingSwitchCard(
                icon = AppIcons.Extension,
                title = stringResource(R.string.setting_show_cn_special_features),
                description = stringResource(R.string.setting_show_cn_special_features_summary),
                checked = showChinaSpecialFeatures,
                onCheckedChange = onShowChinaSpecialFeaturesChange,
                customColors = customColors,
                groupPosition = GroupPosition.Middle,
                isCosxStyle = isCosxStyle
            )
        }
        item {
            SettingSwitchCard(
                icon = AppIcons.Tune,
                title = stringResource(R.string.setting_show_global_special_features),
                description = stringResource(R.string.setting_show_global_special_features_summary),
                checked = showGlobalSpecialFeatures,
                onCheckedChange = onShowGlobalSpecialFeaturesChange,
                customColors = customColors,
                groupPosition = GroupPosition.Bottom,
                isCosxStyle = isCosxStyle
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Text(
                text = stringResource(R.string.section_about),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SettingExpandableCard(
                    icon = AppIcons.Extension,
                    iconResId = R.drawable.author_avatar,
                    iconAsAvatar = true,
                    title = stringResource(R.string.about_author_info_title),
                    description = stringResource(R.string.about_author_info_summary),
                    expanded = authorInfoExpanded,
                    onExpandedChange = { onAuthorInfoExpandedChange(!authorInfoExpanded) },
                    customColors = customColors,
                    groupPosition = GroupPosition.Top,
                    isCosxStyle = isCosxStyle
                )
                AnimatedVisibility(
                    visible = authorInfoExpanded,
                    enter = expandVertically(
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 180)),
                    exit = shrinkVertically(
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 140))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        SettingLinkCard(
                            icon = null,
                            iconResId = R.drawable.ic_coolapk,
                            iconScale = 0.9f,
                            title = stringResource(R.string.about_author_coolapk_title),
                            description = null,
                            onClick = {
                                runCatching {
                                    uriHandler.openUri(COOLAPK_PROFILE_URL)
                                }.onFailure { throwable ->
                                    AppLogStore.w(
                                        "Settings",
                                        "Open Coolapk profile failed: ${throwable.message.orEmpty()}"
                                    )
                                }
                            },
                            customColors = customColors,
                            groupPosition = GroupPosition.Middle,
                            isCosxStyle = isCosxStyle
                        )
                        SettingLinkCard(
                            icon = null,
                            iconResId = R.drawable.ic_github,
                            title = stringResource(R.string.about_author_github_profile_title),
                            description = null,
                            onClick = {
                                runCatching {
                                    uriHandler.openUri(GITHUB_PROFILE_URL)
                                }.onFailure { throwable ->
                                    AppLogStore.w(
                                        "Settings",
                                        "Open GitHub profile failed: ${throwable.message.orEmpty()}"
                                    )
                                }
                            },
                            customColors = customColors,
                            groupPosition = GroupPosition.Middle,
                            isCosxStyle = isCosxStyle
                        )
                    }
                }
            }
        }
        item {
            SettingLinkCard(
                icon = null,
                iconResId = R.drawable.ic_github,
                title = stringResource(R.string.about_github_title),
                description = stringResource(R.string.about_github_summary),
                onClick = {
                    runCatching {
                        uriHandler.openUri(GITHUB_REPO_URL)
                    }.onFailure { throwable ->
                        AppLogStore.w(
                            "Settings",
                            "Open GitHub link failed: ${throwable.message.orEmpty()}"
                        )
                    }
                },
                customColors = customColors,
                groupPosition = GroupPosition.Middle,
                isCosxStyle = isCosxStyle
            )
        }
        item {
            SettingLinkCard(
                icon = null,
                iconResId = R.drawable.ic_telegram,
                title = stringResource(R.string.about_telegram_title),
                description = stringResource(R.string.about_telegram_summary),
                onClick = {
                    runCatching {
                        uriHandler.openUri(TELEGRAM_CHANNEL_URL)
                    }.onFailure { throwable ->
                        AppLogStore.w(
                            "Settings",
                            "Open Telegram link failed: ${throwable.message.orEmpty()}"
                        )
                    }
                },
                customColors = customColors,
                groupPosition = GroupPosition.Bottom,
                isCosxStyle = isCosxStyle
            )
        }
    }
}

@Composable
private fun Md3eThemeSettingsScreen(
    predictiveBackEnabled: Boolean,
    onPredictiveBackEnabledChange: (Boolean) -> Unit,
    customMonetEnabled: Boolean,
    onCustomMonetEnabledChange: (Boolean) -> Unit,
    customMonetSeedColor: Int,
    onCustomMonetSeedColorChange: (Int) -> Unit,
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    showSwitchIcons: Boolean,
    onShowSwitchIconsChange: (Boolean) -> Unit,
    customColors: Md3eCustomUiColors?,
    isCosxStyle: Boolean,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = pageHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            PageTopHeader(
                title = stringResource(R.string.setting_theme_settings),
                showHomeIcons = false,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavigationClick = onBack
            )
        }
        item {
            Spacer(modifier = Modifier.height(personalizationHeaderOffset))
        }
        item {
            SettingThemeModeCard(
                icon = AppIcons.LightMode,
                title = stringResource(R.string.setting_theme_mode),
                selectedMode = themeMode,
                onModeChange = onThemeModeChange,
                customColors = customColors,
                groupPosition = GroupPosition.Top,
                isCosxStyle = isCosxStyle
            )
        }
        item {
            SettingSwitchCard(
                icon = Icons.Rounded.Check,
                title = stringResource(R.string.setting_switch_icons),
                description = stringResource(R.string.setting_switch_icons_summary),
                checked = showSwitchIcons,
                onCheckedChange = onShowSwitchIconsChange,
                customColors = customColors,
                groupPosition = if (isCosxStyle) GroupPosition.Bottom else GroupPosition.Middle,
                isCosxStyle = isCosxStyle
            )
        }
        item {
            SettingSwitchCard(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                title = stringResource(R.string.setting_predictive_back),
                description = stringResource(R.string.setting_predictive_back_summary),
                checked = predictiveBackEnabled,
                onCheckedChange = onPredictiveBackEnabledChange,
                customColors = customColors,
                groupPosition = if (isCosxStyle) GroupPosition.Bottom else GroupPosition.Middle,
                isCosxStyle = isCosxStyle
            )
        }
        if (!isCosxStyle) {
            item {
                SettingSwitchCard(
                    icon = AppIcons.Palette,
                    title = stringResource(R.string.setting_custom_monet),
                    description = stringResource(R.string.setting_custom_monet_summary),
                    checked = customMonetEnabled,
                    onCheckedChange = onCustomMonetEnabledChange,
                    customColors = customColors,
                    groupPosition = if (customMonetEnabled) GroupPosition.Middle else GroupPosition.Bottom,
                    isCosxStyle = false
                )
            }
            item {
                AnimatedVisibility(
                    visible = customMonetEnabled,
                    enter = expandVertically(
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 180)),
                    exit = shrinkVertically(
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 140))
                ) {
                    val selectedPresetName = customMonetPresets.firstOrNull {
                        it.color == customMonetSeedColor
                    }?.let { stringResource(it.nameRes) } ?: stringResource(R.string.monet_name_custom)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = groupedCardShape(GroupPosition.Bottom),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.setting_theme_color_with_name, selectedPresetName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 10.dp)
                        )

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(customMonetPresets.size) { index ->
                                val preset = customMonetPresets[index]
                                val isSelected = customMonetSeedColor == preset.color
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color(preset.color), CircleShape)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                            },
                                            shape = CircleShape
                                        )
                                        .clickable { onCustomMonetSeedColorChange(preset.color) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CosxSearchPlaceholderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.setting_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitchCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false,
    iconResId: Int? = null,
    iconModifier: Modifier = Modifier
        .size(24.dp)
        .padding(top = 1.dp)
) {
    val scheme = MaterialTheme.colorScheme
    val checkedTrack = scheme.primary
    val checkedThumb = scheme.primaryContainer
    val checkedIcon = scheme.onPrimaryContainer
    val uncheckedTrack = scheme.surfaceVariant
    val uncheckedBorder = scheme.outline
    val uncheckedThumb = uncheckedBorder
    val uncheckedIcon = uncheckedTrack
    val showSwitchIcons = LocalSwitchIconVisibility.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCosxStyle -> MaterialTheme.colorScheme.surfaceContainerLow
                else -> customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCosxStyle) 14.dp else 16.dp, vertical = if (isCosxStyle) 14.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = iconModifier
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = iconModifier
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .padding(start = 14.dp, end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = if (isCosxStyle) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (isCosxStyle) {
                CosxSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            } else {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    thumbContent = if (showSwitchIcons) {
                        {
                            Icon(
                                imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = checkedThumb,
                        checkedTrackColor = checkedTrack,
                        checkedIconColor = checkedIcon,
                        uncheckedThumbColor = uncheckedThumb,
                        uncheckedTrackColor = uncheckedTrack,
                        uncheckedBorderColor = uncheckedBorder,
                        uncheckedIconColor = uncheckedIcon
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingLinkCard(
    icon: ImageVector?,
    iconResId: Int?,
    iconScale: Float = 1f,
    title: String,
    description: String?,
    onClick: () -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    val iconTint = if (isCosxStyle) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCosxStyle -> MaterialTheme.colorScheme.surfaceContainerLow
                else -> customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onClick() }
                .padding(horizontal = if (isCosxStyle) 14.dp else 16.dp, vertical = if (isCosxStyle) 14.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (iconResId != null) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale)
                            .padding(top = 1.dp)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale)
                            .padding(top = 1.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = if (isCosxStyle) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Box(
                modifier = Modifier.width(58.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingExpandableCard(
    icon: ImageVector?,
    iconResId: Int?,
    iconAsAvatar: Boolean = false,
    title: String,
    description: String,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    val iconTint = if (isCosxStyle) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCosxStyle -> MaterialTheme.colorScheme.surfaceContainerLow
                else -> customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onExpandedChange() }
                .padding(horizontal = if (isCosxStyle) 14.dp else 16.dp, vertical = if (isCosxStyle) 14.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconResId != null) {
                if (iconAsAvatar) {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Top)
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(top = 1.dp)
                    )
                }
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 1.dp)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Top)
                    .weight(1f)
                    .padding(start = 14.dp, end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = if (isCosxStyle) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Box(
                modifier = Modifier.width(58.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun CosxSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val showSwitchIcons = LocalSwitchIconVisibility.current
    val thumbSize by animateDpAsState(
        targetValue = if (!showSwitchIcons && !checked) 24.dp else 28.dp,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "cosx_switch_thumb_size"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 58.dp - thumbSize - 2.dp else 2.dp,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "cosx_switch_thumb_offset"
    )
    val thumbVerticalOffset by animateDpAsState(
        targetValue = (34.dp - thumbSize) / 2,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "cosx_switch_thumb_vertical_offset"
    )
    val trackColor = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = if (checked) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
    }
    val thumbColor = if (checked) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val iconTint = if (checked) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .width(58.dp)
            .height(34.dp)
            .clip(CircleShape)
            .background(trackColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = thumbVerticalOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor),
            contentAlignment = Alignment.Center
        ) {
            if (showSwitchIcons) {
                Icon(
                    imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingUiStyleCard(
    icon: ImageVector,
    title: String,
    selectedStyle: Int,
    onStyleChange: (Int) -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    val styles = listOf(
        UiStyleMode.Md3e.prefValue to R.string.ui_style_md3e,
        UiStyleMode.Cosx.prefValue to R.string.ui_style_cosx
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCosxStyle) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.setting_ui_style_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCosxStyle) {
                    CosxSegmentedButtons(
                        options = styles,
                        selectedValue = selectedStyle,
                        onValueChange = onStyleChange
                    )
                } else {
                    styles.forEachIndexed { index, (value, labelRes) ->
                        FilterChip(
                            selected = selectedStyle == value,
                            onClick = { onStyleChange(value) },
                            label = { Text(text = stringResource(labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        if (index != styles.lastIndex) {
                            Box(modifier = Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingThemeModeCard(
    icon: ImageVector,
    title: String,
    selectedMode: Int,
    onModeChange: (Int) -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    val modes = listOf(
        THEME_MODE_SYSTEM to R.string.theme_mode_system,
        THEME_MODE_LIGHT to R.string.theme_mode_light,
        THEME_MODE_DARK to R.string.theme_mode_dark
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCosxStyle) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.setting_theme_mode_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCosxStyle) {
                    CosxSegmentedButtons(
                        options = modes,
                        selectedValue = selectedMode,
                        onValueChange = onModeChange
                    )
                } else {
                    modes.forEachIndexed { index, (value, labelRes) ->
                        FilterChip(
                            selected = selectedMode == value,
                            onClick = { onModeChange(value) },
                            label = { Text(text = stringResource(labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        if (index != modes.lastIndex) {
                            Box(modifier = Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingLanguageCard(
    icon: ImageVector,
    title: String,
    selectedLanguageTag: String,
    onLanguageChange: (String) -> Unit,
    customColors: Md3eCustomUiColors?,
    groupPosition: GroupPosition = GroupPosition.Single,
    isCosxStyle: Boolean = false
) {
    val languages = listOf(
        AppLocale.LANGUAGE_SYSTEM to R.string.language_system,
        AppLocale.LANGUAGE_EN to R.string.language_english,
        AppLocale.LANGUAGE_ZH_CN to R.string.language_simplified_chinese,
        AppLocale.LANGUAGE_ZH_HK to R.string.language_traditional_chinese_hk,
        AppLocale.LANGUAGE_ZH_TW to R.string.language_traditional_chinese_tw
    )
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedLabelRes = languages.firstOrNull { it.first == selectedLanguageTag }?.second
        ?: R.string.language_system

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCosxStyle) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                customColors?.settingsCard ?: MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        shape = if (isCosxStyle) cosxGroupedCardShape(groupPosition) else groupedCardShape(groupPosition),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCosxStyle) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isCosxStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.setting_app_language_summary),
                    style = if (isCosxStyle) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Box(
                modifier = Modifier.widthIn(min = 96.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = stringResource(selectedLabelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset(x = 8.dp, y = 2.dp)
                ) {
                    languages.forEach { (languageTag, labelRes) ->
                        val isSelected = selectedLanguageTag == languageTag
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(labelRes),
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            onClick = {
                                expanded = false
                                onLanguageChange(languageTag)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CosxSegmentedButtons(
    options: List<Pair<Int, Int>>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit
) {
    val itemGap = 8.dp
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val eachWidth = ((maxWidth - itemGap * (options.size - 1)).coerceAtLeast(0.dp)) / options.size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(itemGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { (value, labelRes) ->
                val selected = selectedValue == value
                Surface(
                    modifier = Modifier
                        .width(eachWidth)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onValueChange(value) },
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}
