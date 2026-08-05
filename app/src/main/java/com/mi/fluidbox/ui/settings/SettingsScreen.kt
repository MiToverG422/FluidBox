package com.mi.fluidbox.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
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
import com.mi.fluidbox.ui.common.AppLocale
import com.mi.fluidbox.ui.common.AppThemeMode
import com.mi.fluidbox.ui.common.AppIcons
import com.mi.fluidbox.ui.common.bottomTabs
import com.mi.fluidbox.ui.common.readCachedRootAccessInfo
import com.mi.fluidbox.ui.screens.LogsPage
import com.mi.fluidbox.ui.screens.LogsTopBarActions
import com.mi.fluidbox.ui.screens.rememberLogsPageState
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.ListPopup
import io.github.suqi8.coui.kmp.basic.NavigationBar
import io.github.suqi8.coui.kmp.basic.NavigationItem
import io.github.suqi8.coui.kmp.basic.PopupPositionProvider
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.COUIScrollBehavior
import io.github.suqi8.coui.kmp.blur.LayerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronForward
import io.github.suqi8.coui.kmp.icon.extended.Ok
import io.github.suqi8.coui.kmp.icon.extended.Back
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.darkColorScheme
import io.github.suqi8.coui.kmp.theme.lightColorScheme
import io.github.suqi8.coui.kmp.utils.COUIPopupUtils.Companion.COUIPopupHost
import io.github.suqi8.coui.kmp.utils.PressFeedbackType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

enum class AboutPageMode {
    Main,
    AppSettings,
    DeveloperOptions,
    Update,
    UpdateSettings,
    UpdateReleaseNotes,
    Logs,
    Contributors,
    References,
}

@Composable
fun AboutMainRoute(
    modifier: Modifier,
    blurBackdrop: LayerBackdrop?,
    bottomContentPadding: Dp,
    onOpenAppSettings: () -> Unit,
    onOpenSoftwareUpdate: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenContributors: () -> Unit,
    onOpenReferences: () -> Unit,
) {
    SettingsPageSurface(
        title = stringResource(R.string.section_about),
        blurBackdrop = blurBackdrop,
        bottomContentPadding = bottomContentPadding,
        modifier = modifier.fillMaxSize(),
    ) {
        AboutMainPage(
            onOpenAppSettings = onOpenAppSettings,
            onOpenSoftwareUpdate = onOpenSoftwareUpdate,
            onOpenLogs = onOpenLogs,
            onOpenContributors = onOpenContributors,
            onOpenReferences = onOpenReferences,
        )
    }
}

@Composable
fun AboutSubRoute(
    modifier: Modifier,
    pageMode: AboutPageMode,
    softwareUpdateState: SoftwareUpdateUiState,
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
    bottomContentPadding: Dp,
    subPageBottomExtension: Dp,
    blurBackdrop: LayerBackdrop?,
    onBack: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    onOpenUpdateSettings: () -> Unit,
    onOpenUpdateReleaseNotes: () -> Unit,
) {
    val logsPageState = if (pageMode == AboutPageMode.Logs) {
        rememberLogsPageState()
    } else {
        null
    }
    SettingsPageSurface(
        title = when (pageMode) {
            AboutPageMode.AppSettings -> stringResource(R.string.setting_theme_settings)
            AboutPageMode.DeveloperOptions -> stringResource(R.string.setting_developer_options)
            AboutPageMode.Update -> stringResource(R.string.setting_software_update)
            AboutPageMode.UpdateSettings -> stringResource(R.string.software_update_auto_settings)
            AboutPageMode.UpdateReleaseNotes -> stringResource(R.string.software_update_release_notes_title)
            AboutPageMode.Logs -> stringResource(R.string.tab_events)
            AboutPageMode.Contributors -> stringResource(R.string.about_contributors_title)
            AboutPageMode.References -> stringResource(R.string.about_references_title)
            AboutPageMode.Main -> stringResource(R.string.section_about)
        },
        showBack = true,
        onBack = onBack,
        actions = {
            if (pageMode == AboutPageMode.Update) {
                SoftwareUpdateTopBarActions(
                    onOpenAutoUpdateSettings = onOpenUpdateSettings,
                )
            }
            logsPageState?.let { state ->
                LogsTopBarActions(state)
            }
        },
        blurBackdrop = blurBackdrop,
        bottomContentPadding = bottomContentPadding,
        modifier = modifier
            .fillMaxSize()
            .extendPastBottom(subPageBottomExtension),
    ) {
        when (pageMode) {
            AboutPageMode.AppSettings -> AppSettingsPage(
                showChinaSpecialFeatures = showChinaSpecialFeatures,
                onShowChinaSpecialFeaturesChange = onShowChinaSpecialFeaturesChange,
                showGlobalSpecialFeatures = showGlobalSpecialFeatures,
                onShowGlobalSpecialFeaturesChange = onShowGlobalSpecialFeaturesChange,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                onHapticFeedbackEnabledChange = onHapticFeedbackEnabledChange,
                hapticFeedbackPlusEnabled = hapticFeedbackPlusEnabled,
                onHapticFeedbackPlusEnabledChange = onHapticFeedbackPlusEnabledChange,
                blurEffectEnabled = blurEffectEnabled,
                onBlurEffectEnabledChange = onBlurEffectEnabledChange,
                appLanguageTag = appLanguageTag,
                onAppLanguageChange = onAppLanguageChange,
                appThemeMode = appThemeMode,
                onAppThemeModeChange = onAppThemeModeChange,
                appThemeKeyColor = appThemeKeyColor,
                onAppThemeKeyColorChange = onAppThemeKeyColorChange,
                appThemePaletteStyle = appThemePaletteStyle,
                onAppThemePaletteStyleChange = onAppThemePaletteStyleChange,
                appThemeColorSpec = appThemeColorSpec,
                onAppThemeColorSpecChange = onAppThemeColorSpecChange,
                liquidGlassBottomBarEnabled = liquidGlassBottomBarEnabled,
                onLiquidGlassBottomBarEnabledChange = onLiquidGlassBottomBarEnabledChange,
                oneChinaPrincipleEnabled = oneChinaPrincipleEnabled,
                onOneChinaPrincipleEnabledChange = onOneChinaPrincipleEnabledChange,
                onOpenDeveloperOptions = onOpenDeveloperOptions,
            )
            AboutPageMode.DeveloperOptions -> DeveloperOptionsPage(
                showFpsMonitor = showFpsMonitor,
                onShowFpsMonitorChange = onShowFpsMonitorChange,
            )
            AboutPageMode.Update -> SoftwareUpdatePage(
                state = softwareUpdateState,
                onOpenReleaseNotes = onOpenUpdateReleaseNotes,
            )
            AboutPageMode.UpdateSettings -> SoftwareUpdateAutoSettingsPage(softwareUpdateState)
            AboutPageMode.UpdateReleaseNotes -> SoftwareUpdateReleaseNotesPage()
            AboutPageMode.Logs -> logsPageState?.let { state -> LogsPage(state) }
            AboutPageMode.Contributors -> AboutContributorsPage()
            AboutPageMode.References -> AboutReferencesPage()
            AboutPageMode.Main -> Unit
        }
    }
}


private fun Modifier.extendPastBottom(extra: Dp): Modifier = layout { measurable, constraints ->
    val extraPx = extra.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            minHeight = constraints.minHeight + extraPx,
            maxHeight = constraints.maxHeight + extraPx,
        )
    )
    layout(placeable.width, constraints.maxHeight) {
        placeable.place(0, 0)
    }
}
