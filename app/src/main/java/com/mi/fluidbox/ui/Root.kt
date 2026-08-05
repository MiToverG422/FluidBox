package com.mi.fluidbox.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.RenderEffect
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplayTransitionEffects
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.fluidbox.R
import com.mi.fluidbox.lsp.LsposedScopeRequester
import com.mi.fluidbox.ui.layout.BottomNavigationBar
import com.mi.fluidbox.ui.layout.BlurredChromeBar
import com.mi.fluidbox.ui.layout.LiquidGlassBottomNavigationBar
import com.mi.fluidbox.ui.layout.Page
import com.mi.fluidbox.ui.layout.rememberChromeBlurBackdrop
import com.mi.fluidbox.ui.platform.findActivity
import com.mi.fluidbox.ui.screens.FeatureMainRoute
import com.mi.fluidbox.ui.screens.FeaturePageMode
import com.mi.fluidbox.ui.screens.FeatureSubRoute
import com.mi.fluidbox.ui.settings.AboutMainRoute
import com.mi.fluidbox.ui.settings.AboutPageMode
import com.mi.fluidbox.ui.settings.AboutSubRoute
import com.mi.fluidbox.ui.settings.SoftwareUpdateUiState
import com.mi.fluidbox.ui.settings.rememberSoftwareUpdateUiState
import com.mi.fluidbox.ui.common.AppLocale
import com.mi.fluidbox.ui.common.AppThemeMode
import com.mi.fluidbox.ui.common.isMonet
import com.mi.fluidbox.ui.common.AppIcons
import com.mi.fluidbox.ui.common.AssistantScreenOption
import com.mi.fluidbox.ui.common.BottomTab
import com.mi.fluidbox.ui.common.FpsMonitorOverlay
import com.mi.fluidbox.ui.common.LocalColorOsHapticFeedbackEnabled
import com.mi.fluidbox.ui.common.bottomTabs
import com.mi.fluidbox.ui.common.LocalHapticFeedbackEnabled
import com.mi.fluidbox.ui.common.readCachedRootAccessInfo
import com.mi.fluidbox.ui.common.rememberHapticClick
import com.kyant.backdrop.backdrops.LayerBackdrop as LiquidLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as liquidLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as rememberLiquidLayerBackdrop
import com.kyant.backdrop.drawPlainBackdrop
import com.kyant.backdrop.effects.blur as liquidBlur
import com.kyant.backdrop.effects.effect as liquidEffect
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.ListPopup
import io.github.suqi8.coui.kmp.basic.NavigationBar
import io.github.suqi8.coui.kmp.basic.NavigationBarDisplayMode
import io.github.suqi8.coui.kmp.basic.NavigationItem
import io.github.suqi8.coui.kmp.basic.PopupPositionProvider
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.COUIScrollBehavior
import io.github.suqi8.coui.kmp.blur.LayerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronForward
import io.github.suqi8.coui.kmp.icon.extended.Ok
import io.github.suqi8.coui.kmp.icon.extended.Back
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.ThemeColorSpec
import io.github.suqi8.coui.kmp.theme.ThemeController
import io.github.suqi8.coui.kmp.theme.ThemePaletteStyle
import io.github.suqi8.coui.kmp.utils.COUIPopupUtils.Companion.COUIPopupHost
import io.github.suqi8.coui.kmp.utils.PressFeedbackType
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Stable
private class RootMainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set
    private var navigationJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (
            targetIndex == selectedPage &&
            targetIndex == pagerState.currentPage &&
            abs(pagerState.currentPageOffsetFraction) < 0.001f
        ) {
            return
        }
        navigationJob?.cancel()
        selectedPage = targetIndex
        isNavigating = true
        val distance = abs(targetIndex - pagerState.currentPage).coerceAtLeast(2)
        val duration = 100 * distance + 100
        val layoutInfo = pagerState.layoutInfo
        val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
        val currentDistanceInPages =
            targetIndex - pagerState.currentPage - pagerState.currentPageOffsetFraction
        val scrollPixels = currentDistanceInPages * pageSize
        navigationJob = coroutineScope.launch {
            val myJob = coroutineContext[Job]
            try {
                if (pageSize == 0) {
                    pagerState.scrollToPage(targetIndex)
                } else {
                    pagerState.animateScrollBy(
                        value = scrollPixels,
                        animationSpec = tween(easing = EaseInOut, durationMillis = duration),
                    )
                }
            } finally {
                if (navigationJob == myJob) {
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                    isNavigating = false
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
private fun LiquidGlassTopBarBackground(
    backdrop: LiquidLayerBackdrop,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .drawPlainBackdrop(
                backdrop = backdrop,
                shape = { RectangleShape },
                effects = {
                    liquidBlur(4.dp.toPx())
                    liquidEffect(
                        RenderEffect.createRuntimeShaderEffect(
                            obtainRuntimeShader(
                                "FluidBoxLiquidTopBarAlphaMask",
                                """
uniform shader content;
uniform float2 size;
layout(color) uniform half4 tint;
uniform float tintIntensity;

half4 main(float2 coord) {
    float blurAlpha = smoothstep(size.y, size.y * 0.2, coord.y);
    float tintAlpha = smoothstep(size.y, size.y * 0.2, coord.y);
    return mix(content.eval(coord) * blurAlpha, tint * tintAlpha, tintIntensity);
}
""".trimIndent(),
                            ).apply {
                                setFloatUniform("size", size.width, size.height)
                                setColorUniform("tint", tint.value.toLong())
                                setFloatUniform("tintIntensity", 0.8f)
                            },
                            "content",
                        ),
                    )
                },
            ),
        contentAlignment = Alignment.Center,
    ) {}
}

@Composable
private fun rememberRootMainPagerState(pagerState: PagerState): RootMainPagerState {
    val coroutineScope = rememberCoroutineScope()
    return remember(pagerState, coroutineScope) {
        RootMainPagerState(pagerState, coroutineScope)
    }
}

@Composable
private fun MainScreenBackHandler(
    mainPagerState: RootMainPagerState,
    navigator: RootNavigator,
    onTabChange: (Int) -> Unit,
) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navigator.current() is RootRoute.Main &&
                    navigator.backStackSize() == 1 &&
                    mainPagerState.selectedPage != 0
        }
    }
    val mainBackEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = mainBackEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainPagerState.animateToPage(0)
            onTabChange(0)
        },
    )
}

@Serializable
private sealed interface RootRoute : NavKey {
    @Serializable
    data object Main : RootRoute

    @Serializable
    data object FeatureDesktop : RootRoute

    @Serializable
    data object FeatureSystemUi : RootRoute

    @Serializable
    data object FeatureSettings : RootRoute

    @Serializable
    data object FeatureAod : RootRoute

    @Serializable
    data object FeatureAssistant : RootRoute

    @Serializable
    data object FeatureOPlusLocalizer : RootRoute

    @Serializable
    data object FeatureOPlusLocalizerProperties : RootRoute

    @Serializable
    data object FeatureOPlusLocalizerScope : RootRoute

    @Serializable
    data object FeatureExperimental : RootRoute

    @Serializable
    data object AppSettings : RootRoute

    @Serializable
    data object DeveloperOptions : RootRoute

    @Serializable
    data object SoftwareUpdate : RootRoute

    @Serializable
    data object SoftwareUpdateSettings : RootRoute

    @Serializable
    data object SoftwareUpdateReleaseNotes : RootRoute

    @Serializable
    data object Logs : RootRoute

    @Serializable
    data object Contributors : RootRoute

    @Serializable
    data object References : RootRoute
}

@Stable
private class RootNavigator(
    private val backStack: MutableList<NavKey>,
) {
    fun push(key: NavKey) {
        backStack.add(key)
    }

    fun pop() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    fun popUntil(predicate: (NavKey) -> Boolean) {
        while (backStack.size > 1 && !predicate(backStack.last())) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun current(): NavKey? = backStack.lastOrNull()

    fun backStackSize(): Int = backStack.size
}

private fun FeaturePageMode.toRootRoute(): RootRoute? = when (this) {
    FeaturePageMode.Main -> null
    FeaturePageMode.Desktop -> RootRoute.FeatureDesktop
    FeaturePageMode.SystemUi -> RootRoute.FeatureSystemUi
    FeaturePageMode.Settings -> RootRoute.FeatureSettings
    FeaturePageMode.Aod -> RootRoute.FeatureAod
    FeaturePageMode.Assistant -> RootRoute.FeatureAssistant
    FeaturePageMode.OPlusLocalizer -> RootRoute.FeatureOPlusLocalizer
    FeaturePageMode.OPlusLocalizerProperties -> RootRoute.FeatureOPlusLocalizerProperties
    FeaturePageMode.OPlusLocalizerScope -> RootRoute.FeatureOPlusLocalizerScope
    FeaturePageMode.Experimental -> RootRoute.FeatureExperimental
}

private fun AboutPageMode.toRootRoute(): RootRoute? = when (this) {
    AboutPageMode.Main -> null
    AboutPageMode.AppSettings -> RootRoute.AppSettings
    AboutPageMode.DeveloperOptions -> RootRoute.DeveloperOptions
    AboutPageMode.Update -> RootRoute.SoftwareUpdate
    AboutPageMode.UpdateSettings -> RootRoute.SoftwareUpdateSettings
    AboutPageMode.UpdateReleaseNotes -> RootRoute.SoftwareUpdateReleaseNotes
    AboutPageMode.Logs -> RootRoute.Logs
    AboutPageMode.Contributors -> RootRoute.Contributors
    AboutPageMode.References -> RootRoute.References
}

@Stable
private data class RootUiState(
    val currentTab: Int,
    val rootGranted: Boolean,
    val showChinaSpecialFeatures: Boolean,
    val showGlobalSpecialFeatures: Boolean,
    val hapticFeedbackEnabled: Boolean,
    val hapticFeedbackPlusEnabled: Boolean,
    val blurEffectEnabled: Boolean,
    val popDirectionFollowsSwipeEdge: Boolean,
    val showFpsMonitor: Boolean,
    val liquidGlassBottomBarEnabled: Boolean,
    val oneChinaPrincipleEnabled: Boolean,
    val appLanguageTag: String,
    val appThemeMode: AppThemeMode,
    val appThemeKeyColor: Long?,
    val appThemePaletteStyle: Int,
    val appThemeColorSpec: Int,
    val permissionMonitorVisible: Boolean,
    val nativeNotifyIconEnabled: Boolean,
    val nativeNotificationBubblesEnabled: Boolean,
    val statusMobileTypeEnabled: Boolean,
    val statusMobileTypeHideDataOffEnabled: Boolean,
    val statusMobileTypeHideWifiEnabled: Boolean,
    val settingsForceGoogleEntryEnabled: Boolean,
    val extremeRefresh165Enabled: Boolean,
    val launcherLayoutUnlocked: Boolean,
    val assistantScreenOption: AssistantScreenOption,
    val launcherRegionMode: Int,
    val recentTaskRadiusEnabled: Boolean,
    val recentTaskRadiusDp: Int,
    val aodEnhanceEnabled: Boolean,
    val aodInitDarkBrightness: Int,
    val aodInitBrightBrightness: Int,
    val aodRunningBrightnessMultiplier: Float,
    val aodPanoramicSupportEnabled: Boolean,
    val aodSettingsSwitchEnabled: Boolean,
    val aodSingleClickBlockEnabled: Boolean,
    val oosLocalizerEnabled: Boolean,
    val oosLocalizerConfigMode: Int,
    val oosLocalizerRegion: String,
    val oosLocalizerLocale: String,
    val oosLocalizerModel: String,
    val assistantPowerMode: Int,
    val assistantGestureCircleEnabled: Boolean,
    val visibleTabs: List<BottomTab>,
    val selectedIndex: Int,
    val bottomNavigationHeight: Dp,
    val blurBackdrop: LayerBackdrop?,
    val liquidBackdrop: LiquidLayerBackdrop?,
    val softwareUpdateState: SoftwareUpdateUiState,
)

@Stable
private data class RootActions(
    val onTabChange: (Int) -> Unit,
    val onShowChinaSpecialFeaturesChange: (Boolean) -> Unit,
    val onShowGlobalSpecialFeaturesChange: (Boolean) -> Unit,
    val onHapticFeedbackEnabledChange: (Boolean) -> Unit,
    val onHapticFeedbackPlusEnabledChange: (Boolean) -> Unit,
    val onBlurEffectEnabledChange: (Boolean) -> Unit,
    val onPopDirectionFollowsSwipeEdgeChange: (Boolean) -> Unit,
    val onShowFpsMonitorChange: (Boolean) -> Unit,
    val onLiquidGlassBottomBarEnabledChange: (Boolean) -> Unit,
    val onOneChinaPrincipleEnabledChange: (Boolean) -> Unit,
    val onAppLanguageChange: (String) -> Unit,
    val onAppThemeModeChange: (AppThemeMode) -> Unit,
    val onAppThemeKeyColorChange: (Long?) -> Unit,
    val onAppThemePaletteStyleChange: (Int) -> Unit,
    val onAppThemeColorSpecChange: (Int) -> Unit,
    val onPermissionMonitorVisibleChange: (Boolean) -> Unit,
    val onNativeNotifyIconEnabledChange: (Boolean) -> Unit,
    val onNativeNotificationBubblesEnabledChange: (Boolean) -> Unit,
    val onStatusMobileTypeEnabledChange: (Boolean) -> Unit,
    val onStatusMobileTypeHideDataOffEnabledChange: (Boolean) -> Unit,
    val onStatusMobileTypeHideWifiEnabledChange: (Boolean) -> Unit,
    val onSettingsForceGoogleEntryEnabledChange: (Boolean) -> Unit,
    val onExtremeRefresh165EnabledChange: (Boolean) -> Unit,
    val onLauncherLayoutUnlockedChange: (Boolean) -> Unit,
    val onAssistantScreenOptionChange: (AssistantScreenOption) -> Unit,
    val onLauncherRegionModeChange: (Int) -> Unit,
    val onRecentTaskRadiusEnabledChange: (Boolean) -> Unit,
    val onRecentTaskRadiusDpChange: (Int) -> Unit,
    val onAodEnhanceEnabledChange: (Boolean) -> Unit,
    val onAodInitDarkBrightnessChange: (Int) -> Unit,
    val onAodInitBrightBrightnessChange: (Int) -> Unit,
    val onAodRunningBrightnessMultiplierChange: (Float) -> Unit,
    val onAodPanoramicSupportEnabledChange: (Boolean) -> Unit,
    val onAodSettingsSwitchEnabledChange: (Boolean) -> Unit,
    val onAodSingleClickBlockEnabledChange: (Boolean) -> Unit,
    val onOosLocalizerEnabledChange: (Boolean) -> Unit,
    val onOosLocalizerConfigModeChange: (Int) -> Unit,
    val onOosLocalizerRegionChange: (String) -> Unit,
    val onOosLocalizerLocaleChange: (String) -> Unit,
    val onOosLocalizerModelChange: (String) -> Unit,
    val onAssistantPowerModeChange: (Int) -> Unit,
    val onAssistantGestureCircleEnabledChange: (Boolean) -> Unit,
    val onBottomNavigationHeightChange: (Int) -> Unit,
    val onHapticClick: () -> Unit,
    val popRootRoute: () -> Unit,
    val popToMainRoute: () -> Unit,
    val openFeatureSubPage: (FeaturePageMode) -> Unit,
    val openAboutSubPage: (AboutPageMode) -> Unit,
    val requestAboutAutoCheckUpdate: () -> Unit,
)

private val LocalRootUiState = compositionLocalOf<RootUiState> {
    error("No RootUiState provided")
}

private val LocalRootActions = staticCompositionLocalOf<RootActions> {
    error("No RootActions provided")
}

private val LocalRootNavigator = staticCompositionLocalOf<RootNavigator> {
    error("No RootNavigator provided")
}

private val LocalMainPagerState = staticCompositionLocalOf<RootMainPagerState> {
    error("No RootMainPagerState provided")
}

@Composable
private fun RootFeatureEntry(pageMode: FeaturePageMode) {
    val ui = LocalRootUiState.current
    val actions = LocalRootActions.current
    FeatureSubRoute(
        modifier = Modifier.fillMaxSize(),
        pageMode = pageMode,
        showChinaSpecialFeatures = ui.showChinaSpecialFeatures,
        showGlobalSpecialFeatures = ui.showGlobalSpecialFeatures,
        oosLocalizerEnabled = ui.oosLocalizerEnabled,
        onOosLocalizerEnabledChange = actions.onOosLocalizerEnabledChange,
        oosLocalizerConfigMode = ui.oosLocalizerConfigMode,
        onOosLocalizerConfigModeChange = actions.onOosLocalizerConfigModeChange,
        oosLocalizerRegion = ui.oosLocalizerRegion,
        onOosLocalizerRegionChange = actions.onOosLocalizerRegionChange,
        oosLocalizerLocale = ui.oosLocalizerLocale,
        onOosLocalizerLocaleChange = actions.onOosLocalizerLocaleChange,
        oosLocalizerModel = ui.oosLocalizerModel,
        onOosLocalizerModelChange = actions.onOosLocalizerModelChange,
        permissionMonitorVisible = ui.permissionMonitorVisible,
        onPermissionMonitorVisibleChange = actions.onPermissionMonitorVisibleChange,
        nativeNotifyIconEnabled = ui.nativeNotifyIconEnabled,
        onNativeNotifyIconEnabledChange = actions.onNativeNotifyIconEnabledChange,
        nativeNotificationBubblesEnabled = ui.nativeNotificationBubblesEnabled,
        onNativeNotificationBubblesEnabledChange = actions.onNativeNotificationBubblesEnabledChange,
        statusMobileTypeEnabled = ui.statusMobileTypeEnabled,
        onStatusMobileTypeEnabledChange = actions.onStatusMobileTypeEnabledChange,
        statusMobileTypeHideDataOffEnabled = ui.statusMobileTypeHideDataOffEnabled,
        onStatusMobileTypeHideDataOffEnabledChange = actions.onStatusMobileTypeHideDataOffEnabledChange,
        statusMobileTypeHideWifiEnabled = ui.statusMobileTypeHideWifiEnabled,
        onStatusMobileTypeHideWifiEnabledChange = actions.onStatusMobileTypeHideWifiEnabledChange,
        settingsForceGoogleEntryEnabled = ui.settingsForceGoogleEntryEnabled,
        onSettingsForceGoogleEntryEnabledChange = actions.onSettingsForceGoogleEntryEnabledChange,
        extremeRefresh165Enabled = ui.extremeRefresh165Enabled,
        onExtremeRefresh165EnabledChange = actions.onExtremeRefresh165EnabledChange,
        launcherLayoutUnlocked = ui.launcherLayoutUnlocked,
        onLauncherLayoutUnlockedChange = actions.onLauncherLayoutUnlockedChange,
        assistantScreenOption = ui.assistantScreenOption,
        onAssistantScreenOptionChange = actions.onAssistantScreenOptionChange,
        launcherRegionMode = ui.launcherRegionMode,
        onLauncherRegionModeChange = actions.onLauncherRegionModeChange,
        recentTaskRadiusEnabled = ui.recentTaskRadiusEnabled,
        onRecentTaskRadiusEnabledChange = actions.onRecentTaskRadiusEnabledChange,
        recentTaskRadiusDp = ui.recentTaskRadiusDp,
        onRecentTaskRadiusDpChange = actions.onRecentTaskRadiusDpChange,
        aodEnhanceEnabled = ui.aodEnhanceEnabled,
        onAodEnhanceEnabledChange = actions.onAodEnhanceEnabledChange,
        aodInitDarkBrightness = ui.aodInitDarkBrightness,
        onAodInitDarkBrightnessChange = actions.onAodInitDarkBrightnessChange,
        aodInitBrightBrightness = ui.aodInitBrightBrightness,
        onAodInitBrightBrightnessChange = actions.onAodInitBrightBrightnessChange,
        aodRunningBrightnessMultiplier = ui.aodRunningBrightnessMultiplier,
        onAodRunningBrightnessMultiplierChange = actions.onAodRunningBrightnessMultiplierChange,
        aodPanoramicSupportEnabled = ui.aodPanoramicSupportEnabled,
        onAodPanoramicSupportEnabledChange = actions.onAodPanoramicSupportEnabledChange,
        aodSettingsSwitchEnabled = ui.aodSettingsSwitchEnabled,
        onAodSettingsSwitchEnabledChange = actions.onAodSettingsSwitchEnabledChange,
        aodSingleClickBlockEnabled = ui.aodSingleClickBlockEnabled,
        onAodSingleClickBlockEnabledChange = actions.onAodSingleClickBlockEnabledChange,
        assistantPowerMode = ui.assistantPowerMode,
        onAssistantPowerModeChange = actions.onAssistantPowerModeChange,
        assistantGestureCircleEnabled = ui.assistantGestureCircleEnabled,
        onAssistantGestureCircleEnabledChange = actions.onAssistantGestureCircleEnabledChange,
        subPageBottomExtension = ui.bottomNavigationHeight,
        blurBackdrop = ui.blurBackdrop,
        onBack = actions.popRootRoute,
        onOpenSubPage = actions.openFeatureSubPage,
    )
}

@Composable
private fun RootAboutEntry(pageMode: AboutPageMode) {
    val ui = LocalRootUiState.current
    val actions = LocalRootActions.current
    AboutSubRoute(
        modifier = Modifier.fillMaxSize(),
        pageMode = pageMode,
        softwareUpdateState = ui.softwareUpdateState,
        showChinaSpecialFeatures = ui.showChinaSpecialFeatures,
        onShowChinaSpecialFeaturesChange = actions.onShowChinaSpecialFeaturesChange,
        showGlobalSpecialFeatures = ui.showGlobalSpecialFeatures,
        onShowGlobalSpecialFeaturesChange = actions.onShowGlobalSpecialFeaturesChange,
        hapticFeedbackEnabled = ui.hapticFeedbackEnabled,
        onHapticFeedbackEnabledChange = actions.onHapticFeedbackEnabledChange,
        hapticFeedbackPlusEnabled = ui.hapticFeedbackPlusEnabled,
        onHapticFeedbackPlusEnabledChange = actions.onHapticFeedbackPlusEnabledChange,
        blurEffectEnabled = ui.blurEffectEnabled,
        onBlurEffectEnabledChange = actions.onBlurEffectEnabledChange,
        popDirectionFollowsSwipeEdge = ui.popDirectionFollowsSwipeEdge,
        onPopDirectionFollowsSwipeEdgeChange = actions.onPopDirectionFollowsSwipeEdgeChange,
        showFpsMonitor = ui.showFpsMonitor,
        onShowFpsMonitorChange = actions.onShowFpsMonitorChange,
        liquidGlassBottomBarEnabled = ui.liquidGlassBottomBarEnabled,
        onLiquidGlassBottomBarEnabledChange = actions.onLiquidGlassBottomBarEnabledChange,
        oneChinaPrincipleEnabled = ui.oneChinaPrincipleEnabled,
        onOneChinaPrincipleEnabledChange = actions.onOneChinaPrincipleEnabledChange,
        appLanguageTag = ui.appLanguageTag,
        onAppLanguageChange = actions.onAppLanguageChange,
        appThemeMode = ui.appThemeMode,
        onAppThemeModeChange = actions.onAppThemeModeChange,
        appThemeKeyColor = ui.appThemeKeyColor,
        onAppThemeKeyColorChange = actions.onAppThemeKeyColorChange,
        appThemePaletteStyle = ui.appThemePaletteStyle,
        onAppThemePaletteStyleChange = actions.onAppThemePaletteStyleChange,
        appThemeColorSpec = ui.appThemeColorSpec,
        onAppThemeColorSpecChange = actions.onAppThemeColorSpecChange,
        bottomContentPadding = ui.bottomNavigationHeight,
        subPageBottomExtension = ui.bottomNavigationHeight,
        blurBackdrop = ui.blurBackdrop,
        onBack = actions.popRootRoute,
        onOpenDeveloperOptions = { actions.openAboutSubPage(AboutPageMode.DeveloperOptions) },
        onOpenUpdateSettings = { actions.openAboutSubPage(AboutPageMode.UpdateSettings) },
        onOpenUpdateReleaseNotes = { actions.openAboutSubPage(AboutPageMode.UpdateReleaseNotes) },
    )
}

@Composable
fun Root(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    rootGranted: Boolean,
    showChinaSpecialFeatures: Boolean,
    onShowChinaSpecialFeaturesChange: (Boolean) -> Unit,
    showGlobalSpecialFeatures: Boolean,
    onShowGlobalSpecialFeaturesChange: (Boolean) -> Unit,
    hapticFeedbackEnabled: Boolean,
    onHapticFeedbackEnabledChange: (Boolean) -> Unit,
    hapticFeedbackPlusEnabled: Boolean,
    onHapticFeedbackPlusEnabledChange: (Boolean) -> Unit,
    blurEffectEnabled: Boolean,
    onBlurEffectEnabledChange: (Boolean) -> Unit,
    popDirectionFollowsSwipeEdge: Boolean,
    onPopDirectionFollowsSwipeEdgeChange: (Boolean) -> Unit,
    showFpsMonitor: Boolean,
    onShowFpsMonitorChange: (Boolean) -> Unit,
    liquidGlassBottomBarEnabled: Boolean,
    onLiquidGlassBottomBarEnabledChange: (Boolean) -> Unit,
    oneChinaPrincipleEnabled: Boolean,
    onOneChinaPrincipleEnabledChange: (Boolean) -> Unit,
    appLanguageTag: String,
    onAppLanguageChange: (String) -> Unit,
    appThemeMode: AppThemeMode,
    onAppThemeModeChange: (AppThemeMode) -> Unit,
    appThemeKeyColor: Long?,
    onAppThemeKeyColorChange: (Long?) -> Unit,
    appThemePaletteStyle: Int,
    onAppThemePaletteStyleChange: (Int) -> Unit,
    appThemeColorSpec: Int,
    onAppThemeColorSpecChange: (Int) -> Unit,
    permissionMonitorVisible: Boolean,
    onPermissionMonitorVisibleChange: (Boolean) -> Unit,
    nativeNotifyIconEnabled: Boolean,
    onNativeNotifyIconEnabledChange: (Boolean) -> Unit,
    nativeNotificationBubblesEnabled: Boolean,
    onNativeNotificationBubblesEnabledChange: (Boolean) -> Unit,
    statusMobileTypeEnabled: Boolean,
    onStatusMobileTypeEnabledChange: (Boolean) -> Unit,
    statusMobileTypeHideDataOffEnabled: Boolean,
    onStatusMobileTypeHideDataOffEnabledChange: (Boolean) -> Unit,
    statusMobileTypeHideWifiEnabled: Boolean,
    onStatusMobileTypeHideWifiEnabledChange: (Boolean) -> Unit,
    settingsForceGoogleEntryEnabled: Boolean,
    onSettingsForceGoogleEntryEnabledChange: (Boolean) -> Unit,
    extremeRefresh165Enabled: Boolean,
    onExtremeRefresh165EnabledChange: (Boolean) -> Unit,
    launcherLayoutUnlocked: Boolean,
    onLauncherLayoutUnlockedChange: (Boolean) -> Unit,
    assistantScreenOption: AssistantScreenOption,
    onAssistantScreenOptionChange: (AssistantScreenOption) -> Unit,
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
    oosLocalizerConfigMode: Int,
    onOosLocalizerConfigModeChange: (Int) -> Unit,
    oosLocalizerRegion: String,
    onOosLocalizerRegionChange: (String) -> Unit,
    oosLocalizerLocale: String,
    onOosLocalizerLocaleChange: (String) -> Unit,
    oosLocalizerModel: String,
    onOosLocalizerModelChange: (String) -> Unit,
    assistantPowerMode: Int,
    onAssistantPowerModeChange: (Int) -> Unit,
    assistantGestureCircleEnabled: Boolean,
    onAssistantGestureCircleEnabledChange: (Boolean) -> Unit,
) {
    val themeController = remember(appThemeMode, appThemeKeyColor, appThemePaletteStyle, appThemeColorSpec) {
        val paletteStyle = ThemePaletteStyle.entries.getOrNull(appThemePaletteStyle)
            ?: ThemePaletteStyle.TonalSpot
        val colorSpec = ThemeColorSpec.entries.getOrNull(appThemeColorSpec)
            ?: ThemeColorSpec.Spec2021
        ThemeController(
            colorSchemeMode = appThemeMode.toColorSchemeMode(),
            keyColor = if (appThemeMode.isMonet) appThemeKeyColor?.let { Color(it) } else null,
            paletteStyle = paletteStyle,
            colorSpec = colorSpec,
        )
    }
    COUITheme(controller = themeController) {
        CompositionLocalProvider(
            LocalHapticFeedbackEnabled provides hapticFeedbackEnabled,
            LocalColorOsHapticFeedbackEnabled provides hapticFeedbackPlusEnabled,
        ) {
        val colors = COUITheme.colorScheme
        val blurBackdrop = rememberChromeBlurBackdrop(blurEffectEnabled)
        val liquidBottomBarSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val liquidBackdrop = if (liquidBottomBarSupported) rememberLiquidLayerBackdrop() else null
        val context = LocalContext.current
        val activity = context.findActivity()
        val statusBarDarkIcons = colors.surface.luminance() > 0.5f
        val navigationBarSurface = if (blurBackdrop != null) colors.surface else colors.background
        val navigationBarDarkIcons = navigationBarSurface.luminance() > 0.5f
        DisposableEffect(activity, statusBarDarkIcons, navigationBarDarkIcons) {
            activity?.window?.let { window ->
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = statusBarDarkIcons
                    isAppearanceLightNavigationBars = navigationBarDarkIcons
                }
            }
            onDispose {}
        }
        val hapticClick = rememberHapticClick()
        val visibleTabs = bottomTabs
        val selectedPagerIndex = visibleTabs.indexOfFirst { it.screenIndex == currentTab }.coerceAtLeast(0)
        val pagerState = rememberPagerState(
            initialPage = selectedPagerIndex,
            pageCount = { visibleTabs.size },
        )
        val mainPagerState = rememberRootMainPagerState(pagerState)
        LaunchedEffect(mainPagerState.pagerState.currentPage) {
            mainPagerState.syncPage()
        }
        LaunchedEffect(selectedPagerIndex) {
            if (mainPagerState.selectedPage != selectedPagerIndex) {
                mainPagerState.animateToPage(selectedPagerIndex)
            }
        }
        val selectedIndex = mainPagerState.selectedPage
            .coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0))
        var aboutAutoCheckUpdateRequest by remember { mutableIntStateOf(0) }
        val serializersModule = remember {
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(RootRoute.Main::class)
                    subclass(RootRoute.FeatureDesktop::class)
                    subclass(RootRoute.FeatureSystemUi::class)
                    subclass(RootRoute.FeatureSettings::class)
                    subclass(RootRoute.FeatureAod::class)
                    subclass(RootRoute.FeatureAssistant::class)
                    subclass(RootRoute.FeatureOPlusLocalizer::class)
                    subclass(RootRoute.FeatureOPlusLocalizerProperties::class)
                    subclass(RootRoute.FeatureOPlusLocalizerScope::class)
                    subclass(RootRoute.FeatureExperimental::class)
                    subclass(RootRoute.AppSettings::class)
                    subclass(RootRoute.DeveloperOptions::class)
                    subclass(RootRoute.SoftwareUpdate::class)
                    subclass(RootRoute.SoftwareUpdateSettings::class)
                    subclass(RootRoute.SoftwareUpdateReleaseNotes::class)
                    subclass(RootRoute.Logs::class)
                    subclass(RootRoute.Contributors::class)
                    subclass(RootRoute.References::class)
                }
            }
        }
        val savedStateConfig = remember(serializersModule) {
            SavedStateConfiguration {
                this.serializersModule = serializersModule
            }
        }
        val rootBackStack = rememberNavBackStack(
            configuration = savedStateConfig,
            RootRoute.Main,
        )
        val navigator = remember(rootBackStack) { RootNavigator(rootBackStack) }
        val subPageActive = rootBackStack.size > 1
        val softwareUpdateState = rememberSoftwareUpdateUiState(aboutAutoCheckUpdateRequest)
        val currentRootRoute = navigator.current() as? RootRoute ?: RootRoute.Main
        var bottomNavigationHeightPx by remember { mutableIntStateOf(0) }
        val density = LocalDensity.current
        val bottomNavigationHeight = with(density) {
            if (bottomNavigationHeightPx > 0) bottomNavigationHeightPx.toDp() else 96.dp
        }

        LaunchedEffect(currentRootRoute) {
            if (
                currentRootRoute == RootRoute.SoftwareUpdate
            ) {
                softwareUpdateState.onCheckUpdates()
            }
        }

        fun popRootRoute() {
            navigator.pop()
        }

        fun popToMainRoute() {
            navigator.popUntil { it is RootRoute.Main }
        }

        fun openFeatureSubPage(mode: FeaturePageMode) {
            val route = mode.toRootRoute() ?: return
            if (navigator.current() == route) return
            navigator.push(route)
        }

        fun openAboutSubPage(mode: AboutPageMode) {
            val route = mode.toRootRoute() ?: return
            if (navigator.current() == route) return
            navigator.push(route)
        }

        val rootUiState = RootUiState(
            currentTab = currentTab,
            rootGranted = rootGranted,
            showChinaSpecialFeatures = showChinaSpecialFeatures,
            showGlobalSpecialFeatures = showGlobalSpecialFeatures,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
            hapticFeedbackPlusEnabled = hapticFeedbackPlusEnabled,
            blurEffectEnabled = blurEffectEnabled,
            popDirectionFollowsSwipeEdge = popDirectionFollowsSwipeEdge,
            showFpsMonitor = showFpsMonitor,
            liquidGlassBottomBarEnabled = liquidGlassBottomBarEnabled && liquidBackdrop != null,
            oneChinaPrincipleEnabled = oneChinaPrincipleEnabled,
            appLanguageTag = appLanguageTag,
            appThemeMode = appThemeMode,
            appThemeKeyColor = appThemeKeyColor,
            appThemePaletteStyle = appThemePaletteStyle,
            appThemeColorSpec = appThemeColorSpec,
            permissionMonitorVisible = permissionMonitorVisible,
            nativeNotifyIconEnabled = nativeNotifyIconEnabled,
            nativeNotificationBubblesEnabled = nativeNotificationBubblesEnabled,
            statusMobileTypeEnabled = statusMobileTypeEnabled,
            statusMobileTypeHideDataOffEnabled = statusMobileTypeHideDataOffEnabled,
            statusMobileTypeHideWifiEnabled = statusMobileTypeHideWifiEnabled,
            settingsForceGoogleEntryEnabled = settingsForceGoogleEntryEnabled,
            extremeRefresh165Enabled = extremeRefresh165Enabled,
            launcherLayoutUnlocked = launcherLayoutUnlocked,
            assistantScreenOption = assistantScreenOption,
            launcherRegionMode = launcherRegionMode,
            recentTaskRadiusEnabled = recentTaskRadiusEnabled,
            recentTaskRadiusDp = recentTaskRadiusDp,
            aodEnhanceEnabled = aodEnhanceEnabled,
            aodInitDarkBrightness = aodInitDarkBrightness,
            aodInitBrightBrightness = aodInitBrightBrightness,
            aodRunningBrightnessMultiplier = aodRunningBrightnessMultiplier,
            aodPanoramicSupportEnabled = aodPanoramicSupportEnabled,
            aodSettingsSwitchEnabled = aodSettingsSwitchEnabled,
            aodSingleClickBlockEnabled = aodSingleClickBlockEnabled,
            oosLocalizerEnabled = oosLocalizerEnabled,
            oosLocalizerConfigMode = oosLocalizerConfigMode,
            oosLocalizerRegion = oosLocalizerRegion,
            oosLocalizerLocale = oosLocalizerLocale,
            oosLocalizerModel = oosLocalizerModel,
            assistantPowerMode = assistantPowerMode,
            assistantGestureCircleEnabled = assistantGestureCircleEnabled,
            visibleTabs = visibleTabs,
            selectedIndex = selectedIndex,
            bottomNavigationHeight = bottomNavigationHeight,
            blurBackdrop = blurBackdrop,
            liquidBackdrop = liquidBackdrop,
            softwareUpdateState = softwareUpdateState,
        )
        val rootActions = RootActions(
            onTabChange = onTabChange,
            onShowChinaSpecialFeaturesChange = onShowChinaSpecialFeaturesChange,
            onShowGlobalSpecialFeaturesChange = onShowGlobalSpecialFeaturesChange,
            onHapticFeedbackEnabledChange = onHapticFeedbackEnabledChange,
            onHapticFeedbackPlusEnabledChange = onHapticFeedbackPlusEnabledChange,
            onBlurEffectEnabledChange = onBlurEffectEnabledChange,
            onPopDirectionFollowsSwipeEdgeChange = onPopDirectionFollowsSwipeEdgeChange,
            onShowFpsMonitorChange = onShowFpsMonitorChange,
            onLiquidGlassBottomBarEnabledChange = onLiquidGlassBottomBarEnabledChange,
            onOneChinaPrincipleEnabledChange = onOneChinaPrincipleEnabledChange,
            onAppLanguageChange = onAppLanguageChange,
            onAppThemeModeChange = onAppThemeModeChange,
            onAppThemeKeyColorChange = onAppThemeKeyColorChange,
            onAppThemePaletteStyleChange = onAppThemePaletteStyleChange,
            onAppThemeColorSpecChange = onAppThemeColorSpecChange,
            onPermissionMonitorVisibleChange = onPermissionMonitorVisibleChange,
            onNativeNotifyIconEnabledChange = onNativeNotifyIconEnabledChange,
            onNativeNotificationBubblesEnabledChange = onNativeNotificationBubblesEnabledChange,
            onStatusMobileTypeEnabledChange = onStatusMobileTypeEnabledChange,
            onStatusMobileTypeHideDataOffEnabledChange = onStatusMobileTypeHideDataOffEnabledChange,
            onStatusMobileTypeHideWifiEnabledChange = onStatusMobileTypeHideWifiEnabledChange,
            onSettingsForceGoogleEntryEnabledChange = onSettingsForceGoogleEntryEnabledChange,
            onExtremeRefresh165EnabledChange = onExtremeRefresh165EnabledChange,
            onLauncherLayoutUnlockedChange = onLauncherLayoutUnlockedChange,
            onAssistantScreenOptionChange = onAssistantScreenOptionChange,
            onLauncherRegionModeChange = onLauncherRegionModeChange,
            onRecentTaskRadiusEnabledChange = onRecentTaskRadiusEnabledChange,
            onRecentTaskRadiusDpChange = onRecentTaskRadiusDpChange,
            onAodEnhanceEnabledChange = onAodEnhanceEnabledChange,
            onAodInitDarkBrightnessChange = onAodInitDarkBrightnessChange,
            onAodInitBrightBrightnessChange = onAodInitBrightBrightnessChange,
            onAodRunningBrightnessMultiplierChange = onAodRunningBrightnessMultiplierChange,
            onAodPanoramicSupportEnabledChange = onAodPanoramicSupportEnabledChange,
            onAodSettingsSwitchEnabledChange = onAodSettingsSwitchEnabledChange,
            onAodSingleClickBlockEnabledChange = onAodSingleClickBlockEnabledChange,
            onOosLocalizerEnabledChange = onOosLocalizerEnabledChange,
            onOosLocalizerConfigModeChange = onOosLocalizerConfigModeChange,
            onOosLocalizerRegionChange = onOosLocalizerRegionChange,
            onOosLocalizerLocaleChange = onOosLocalizerLocaleChange,
            onOosLocalizerModelChange = onOosLocalizerModelChange,
            onAssistantPowerModeChange = onAssistantPowerModeChange,
            onAssistantGestureCircleEnabledChange = onAssistantGestureCircleEnabledChange,
            onBottomNavigationHeightChange = { bottomNavigationHeightPx = it },
            onHapticClick = hapticClick,
            popRootRoute = ::popRootRoute,
            popToMainRoute = ::popToMainRoute,
            openFeatureSubPage = ::openFeatureSubPage,
            openAboutSubPage = ::openAboutSubPage,
            requestAboutAutoCheckUpdate = { aboutAutoCheckUpdateRequest += 1 },
        )

        val bottomChrome: @Composable (Boolean, Boolean) -> Unit = { visible, liquidOnly ->
        val ui = LocalRootUiState.current
        val actions = LocalRootActions.current
        val mainPagerState = LocalMainPagerState.current
        val colors = COUITheme.colorScheme
            val blurActive = ui.blurBackdrop != null
            val barColor = if (blurActive) Color.Transparent else colors.background
            val liquidBackdrop = ui.liquidBackdrop
            val liquidGlassActive = ui.liquidGlassBottomBarEnabled && liquidBackdrop != null
            val liquidGlassVisible = visible && liquidOnly && liquidGlassActive
            AnimatedVisibility(
                visible = liquidGlassVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                liquidBackdrop?.let { backdrop ->
                    LiquidGlassBottomNavigationBar(
                        tabs = ui.visibleTabs,
                        selectedIndex = ui.selectedIndex,
                        pagerState = mainPagerState.pagerState,
                        backdrop = backdrop,
                        onTabSelected = { index ->
                            actions.onHapticClick()
                            actions.popToMainRoute()
                            mainPagerState.animateToPage(index)
                            actions.onTabChange(ui.visibleTabs[index].screenIndex)
                        },
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                actions.onBottomNavigationHeightChange(coordinates.size.height)
                            },
                    )
                }
            }
            AnimatedVisibility(
                visible = visible && !liquidOnly && !liquidGlassActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    BlurredChromeBar(
                        backdrop = ui.blurBackdrop,
                        backgroundColor = barColor,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                    ) {
                        BottomNavigationBar(
                            items = ui.visibleTabs.mapIndexed { index, tab ->
                                NavigationItem(
                                    label = stringResource(tab.titleRes),
                                    icon = if (index == ui.selectedIndex) tab.selectedIcon else tab.unselectedIcon,
                                )
                            },
                            selectedIndex = ui.selectedIndex,
                            onItemSelected = { index ->
                                actions.onHapticClick()
                                actions.popToMainRoute()
                                mainPagerState.animateToPage(index)
                                actions.onTabChange(ui.visibleTabs[index].screenIndex)
                            },
                            color = barColor,
                            showDivider = !blurActive,
                            mode = NavigationBarDisplayMode.IconAndText,
                            modifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    actions.onBottomNavigationHeightChange(coordinates.size.height)
                                },
                        )
                    }
                }
            }
        }
        val rootEntryProvider = remember(rootBackStack) {
            entryProvider<NavKey> {
            entry<RootRoute.Main> {
                        val ui = LocalRootUiState.current
                        val actions = LocalRootActions.current
                        val mainPagerState = LocalMainPagerState.current
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(COUITheme.colorScheme.surface),
                            containerColor = COUITheme.colorScheme.surface,
                            popupHost = {},
                            bottomBar = { bottomChrome(true, false) },
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                HorizontalPager(
                                    state = mainPagerState.pagerState,
                                    userScrollEnabled = false,
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxSize(),
                                ) { pageIndex ->
                                    val targetTab = ui.visibleTabs.getOrNull(pageIndex)?.screenIndex ?: ui.currentTab
                                    when (targetTab) {
                                        1 -> FeatureMainRoute(
                                            modifier = Modifier.fillMaxSize(),
                                            showChinaSpecialFeatures = ui.showChinaSpecialFeatures,
                                            showGlobalSpecialFeatures = ui.showGlobalSpecialFeatures,
                                            subPageBottomExtension = ui.bottomNavigationHeight,
                                            blurBackdrop = ui.blurBackdrop,
                                            onOpen = actions.openFeatureSubPage,
                                        )
                                        3 -> AboutMainRoute(
                                            modifier = Modifier.fillMaxSize(),
                                            blurBackdrop = ui.blurBackdrop,
                                            bottomContentPadding = ui.bottomNavigationHeight,
                                            onOpenAppSettings = { actions.openAboutSubPage(AboutPageMode.AppSettings) },
                                            onOpenSoftwareUpdate = { actions.openAboutSubPage(AboutPageMode.Update) },
                                            onOpenLogs = { actions.openAboutSubPage(AboutPageMode.Logs) },
                                            onOpenContributors = { actions.openAboutSubPage(AboutPageMode.Contributors) },
                                            onOpenReferences = { actions.openAboutSubPage(AboutPageMode.References) },
                                        )
                                        else -> Page(
                                            modifier = Modifier.fillMaxSize(),
                                            currentTab = targetTab,
                                            rootGranted = ui.rootGranted,
                                            oneChinaPrincipleEnabled = ui.oneChinaPrincipleEnabled,
                                            extremeRefresh165Enabled = ui.extremeRefresh165Enabled,
                                            onExtremeRefresh165EnabledChange = actions.onExtremeRefresh165EnabledChange,
                                            bottomChromePadding = ui.bottomNavigationHeight,
                                            onHomeHeroLongPress = {
                                                actions.onTabChange(3)
                                                mainPagerState.animateToPage(
                                                    ui.visibleTabs.indexOfFirst { it.screenIndex == 3 }.coerceAtLeast(0),
                                                )
                                                actions.openAboutSubPage(AboutPageMode.Update)
                                                actions.requestAboutAutoCheckUpdate()
                                            },
                                            blurBackdrop = ui.blurBackdrop,
                                        )
                                    }
                                }
                            }
                            COUIPopupHost()
                        }
            }
            entry<RootRoute.FeatureDesktop> { RootFeatureEntry(FeaturePageMode.Desktop) }
            entry<RootRoute.FeatureSystemUi> { RootFeatureEntry(FeaturePageMode.SystemUi) }
            entry<RootRoute.FeatureSettings> { RootFeatureEntry(FeaturePageMode.Settings) }
            entry<RootRoute.FeatureAod> { RootFeatureEntry(FeaturePageMode.Aod) }
            entry<RootRoute.FeatureAssistant> { RootFeatureEntry(FeaturePageMode.Assistant) }
            entry<RootRoute.FeatureOPlusLocalizer> { RootFeatureEntry(FeaturePageMode.OPlusLocalizer) }
            entry<RootRoute.FeatureOPlusLocalizerProperties> {
                RootFeatureEntry(FeaturePageMode.OPlusLocalizerProperties)
            }
            entry<RootRoute.FeatureOPlusLocalizerScope> {
                RootFeatureEntry(FeaturePageMode.OPlusLocalizerScope)
            }
            entry<RootRoute.FeatureExperimental> { RootFeatureEntry(FeaturePageMode.Experimental) }
            entry<RootRoute.AppSettings> { RootAboutEntry(AboutPageMode.AppSettings) }
            entry<RootRoute.DeveloperOptions> { RootAboutEntry(AboutPageMode.DeveloperOptions) }
            entry<RootRoute.SoftwareUpdate> { RootAboutEntry(AboutPageMode.Update) }
            entry<RootRoute.SoftwareUpdateSettings> { RootAboutEntry(AboutPageMode.UpdateSettings) }
            entry<RootRoute.SoftwareUpdateReleaseNotes> {
                RootAboutEntry(AboutPageMode.UpdateReleaseNotes)
            }
            entry<RootRoute.Logs> { RootAboutEntry(AboutPageMode.Logs) }
            entry<RootRoute.Contributors> { RootAboutEntry(AboutPageMode.Contributors) }
            entry<RootRoute.References> { RootAboutEntry(AboutPageMode.References) }
            }
        }

        val rootEntries = rememberDecoratedNavEntries(
            backStack = rootBackStack,
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider = rootEntryProvider,
        )

        val rootTransitionEffects = remember(popDirectionFollowsSwipeEdge) {
            NavDisplayTransitionEffects(
                enableCornerClip = true,
                dimAmount = 0.5f,
                blockInputDuringTransition = true,
                popDirectionFollowsSwipeEdge = false,
            )
        }

        MainScreenBackHandler(
            mainPagerState = mainPagerState,
            navigator = navigator,
            onTabChange = onTabChange,
        )

        CompositionLocalProvider(
            LocalRootNavigator provides navigator,
            LocalMainPagerState provides mainPagerState,
            LocalRootUiState provides rootUiState,
            LocalRootActions provides rootActions,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    entries = rootEntries,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.surface)
                        .imePadding()
                        .then(
                            if (rootUiState.liquidGlassBottomBarEnabled && rootUiState.liquidBackdrop != null) {
                                Modifier.liquidLayerBackdrop(rootUiState.liquidBackdrop)
                            } else {
                                Modifier
                            },
                        ),
                    onBack = { navigator.pop() },
                    transitionEffects = rootTransitionEffects,
                )
                if (rootUiState.liquidGlassBottomBarEnabled && rootUiState.liquidBackdrop != null) {
                    LiquidGlassTopBarBackground(
                        backdrop = rootUiState.liquidBackdrop,
                        tint = colors.surface,
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    bottomChrome(currentRootRoute == RootRoute.Main, true)
                }
                AnimatedVisibility(
                    visible = rootUiState.showFpsMonitor,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    FpsMonitorOverlay(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 44.dp),
                    )
                }
            }
        }
        }
    }
}
