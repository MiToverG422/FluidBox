package com.mi.fluidbox.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.mi.fluidbox.R
import com.mi.fluidbox.lsp.LspConfig
import com.mi.fluidbox.lsp.OosLocalizerHooker
import com.mi.fluidbox.ui.common.AppIcons
import com.mi.fluidbox.ui.common.AssistantScreenOption
import com.mi.fluidbox.ui.common.CouiConfirmDialog
import com.mi.fluidbox.ui.common.rememberColorOsHapticTick
import com.mi.fluidbox.ui.common.rememberHapticClick
import com.mi.fluidbox.ui.common.rememberHapticToggle
import com.mi.fluidbox.ui.common.restartScopePackages
import com.mi.fluidbox.ui.settings.SettingsCardRow
import com.mi.fluidbox.ui.settings.SettingsDivider
import com.mi.fluidbox.ui.settings.SettingsGroup
import com.mi.fluidbox.ui.settings.SettingsPageSurface
import com.mi.fluidbox.ui.settings.SettingsSection
import com.mi.fluidbox.ui.settings.SettingsTokens
import com.mi.fluidbox.ui.settings.SettingsToggleRow
import com.mi.fluidbox.ui.settings.SettingsRowTextContent
import com.mi.fluidbox.ui.settings.SettingsWindowDropdownPreference
import com.mi.fluidbox.ui.settings.settingsInteractiveRowHighlight
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.Button
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.RadioButton
import io.github.suqi8.coui.kmp.basic.Slider
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.basic.TextFieldMode
import io.github.suqi8.coui.kmp.blur.LayerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Ok
import io.github.suqi8.coui.kmp.icon.extended.Refresh
import io.github.suqi8.coui.kmp.theme.COUITheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

enum class FeaturePageMode {
    Main,
    Desktop,
    SystemUi,
    Settings,
    Aod,
    Assistant,
    OPlusLocalizer,
    OPlusLocalizerProperties,
    OPlusLocalizerScope,
    Experimental,
}

@Composable
fun FeatureMainRoute(
    modifier: Modifier,
    showChinaSpecialFeatures: Boolean,
    showGlobalSpecialFeatures: Boolean,
    subPageBottomExtension: Dp,
    blurBackdrop: LayerBackdrop?,
    onOpen: (FeaturePageMode) -> Unit,
) {
    SettingsPageSurface(
        title = stringResource(R.string.tab_features),
        blurBackdrop = blurBackdrop,
        bottomContentPadding = subPageBottomExtension,
        modifier = modifier.fillMaxSize(),
    ) {
        FeatureMainPage(
            showChinaSpecialFeatures = showChinaSpecialFeatures,
            showGlobalSpecialFeatures = showGlobalSpecialFeatures,
            onOpen = onOpen,
        )
    }
}

@Composable
fun FeatureSubRoute(
    modifier: Modifier,
    pageMode: FeaturePageMode,
    showChinaSpecialFeatures: Boolean,
    showGlobalSpecialFeatures: Boolean,
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
    assistantPowerMode: Int,
    onAssistantPowerModeChange: (Int) -> Unit,
    assistantGestureCircleEnabled: Boolean,
    onAssistantGestureCircleEnabledChange: (Boolean) -> Unit,
    subPageBottomExtension: Dp,
    blurBackdrop: LayerBackdrop?,
    onBack: () -> Unit,
    onOpenSubPage: (FeaturePageMode) -> Unit,
) {
    val context = LocalContext.current
    val subPageStateHolder = rememberSaveableStateHolder()
    var oosLocalizerPropertiesDraft by remember { mutableStateOf<OosLocalizerPropertiesDraft?>(null) }
    var oosLocalizerScopeDraft by remember { mutableStateOf<OosLocalizerScopeDraft?>(null) }
    val restartScope = rememberCoroutineScope()
    var restartConfirmMode by remember { mutableStateOf<FeaturePageMode?>(null) }
    val restartTargets = featureRestartPackages(pageMode)

    fun performRestartScope(targets: List<String>) {
        restartScope.launch {
            val result = restartScopePackages(targets)
            val message = if (result.success) {
                context.getString(R.string.feature_restart_scope_done)
            } else {
                result.detail?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.feature_restart_scope_failed)
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun RenderRestartScopeConfirmDialog() {
        val modeToRestart = restartConfirmMode
        val targets = modeToRestart?.let { featureRestartPackages(it) }.orEmpty()
        CouiConfirmDialog(
            show = modeToRestart != null,
            title = modeToRestart?.let {
                stringResource(
                    R.string.feature_restart_scope_confirm_title,
                    featurePageTitle(it),
                )
            },
            summary = stringResource(R.string.feature_restart_scope_confirm_summary),
            negativeText = stringResource(R.string.feature_restart_scope_confirm_cancel),
            positiveText = stringResource(R.string.feature_restart_scope_confirm_action),
            onDismissRequest = { restartConfirmMode = null },
            onPositive = {
                restartConfirmMode = null
                performRestartScope(targets)
            },
        )
    }

    SettingsPageSurface(
        title = featurePageTitle(pageMode),
        showBack = true,
        onBack = onBack,
        blurBackdrop = blurBackdrop,
        bottomContentPadding = subPageBottomExtension,
        actions = {
            FeatureRestartActionButton(
                onClick = {
                    if (restartTargets.isEmpty()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.feature_restart_scope_unsupported),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        restartConfirmMode = pageMode
                    }
                },
            )
        },
        modifier = modifier
            .fillMaxSize()
            .extendPastBottom(subPageBottomExtension),
    ) {
        subPageStateHolder.SaveableStateProvider(pageMode) {
            val propertiesDraft = if (pageMode == FeaturePageMode.OPlusLocalizerProperties) {
                oosLocalizerPropertiesDraft ?: createOosLocalizerPropertiesDraft(
                    context = context,
                    region = oosLocalizerRegion,
                    locale = oosLocalizerLocale,
                    model = oosLocalizerModel,
                )
            } else {
                null
            }
            val scopePackages = OosLocalizerHooker.supportedPackageNames.sorted()
            val scopeDraft = if (pageMode == FeaturePageMode.OPlusLocalizerScope) {
                oosLocalizerScopeDraft ?: createOosLocalizerScopeDraft(
                    context = context,
                    scopePackages = scopePackages,
                )
            } else {
                null
            }
            FeatureSubPage(
                mode = pageMode,
                showChinaSpecialFeatures = showChinaSpecialFeatures,
                showGlobalSpecialFeatures = showGlobalSpecialFeatures,
                oosLocalizerEnabled = oosLocalizerEnabled,
                onOosLocalizerEnabledChange = onOosLocalizerEnabledChange,
                oosLocalizerConfigMode = oosLocalizerConfigMode,
                onOosLocalizerConfigModeChange = onOosLocalizerConfigModeChange,
                oosLocalizerRegion = oosLocalizerRegion,
                onOosLocalizerRegionChange = onOosLocalizerRegionChange,
                oosLocalizerLocale = oosLocalizerLocale,
                onOosLocalizerLocaleChange = onOosLocalizerLocaleChange,
                oosLocalizerModel = oosLocalizerModel,
                onOosLocalizerModelChange = onOosLocalizerModelChange,
                permissionMonitorVisible = permissionMonitorVisible,
                onPermissionMonitorVisibleChange = onPermissionMonitorVisibleChange,
                nativeNotifyIconEnabled = nativeNotifyIconEnabled,
                onNativeNotifyIconEnabledChange = onNativeNotifyIconEnabledChange,
                nativeNotificationBubblesEnabled = nativeNotificationBubblesEnabled,
                onNativeNotificationBubblesEnabledChange = onNativeNotificationBubblesEnabledChange,
                statusMobileTypeEnabled = statusMobileTypeEnabled,
                onStatusMobileTypeEnabledChange = onStatusMobileTypeEnabledChange,
                statusMobileTypeHideDataOffEnabled = statusMobileTypeHideDataOffEnabled,
                onStatusMobileTypeHideDataOffEnabledChange = onStatusMobileTypeHideDataOffEnabledChange,
                statusMobileTypeHideWifiEnabled = statusMobileTypeHideWifiEnabled,
                onStatusMobileTypeHideWifiEnabledChange = onStatusMobileTypeHideWifiEnabledChange,
                settingsForceGoogleEntryEnabled = settingsForceGoogleEntryEnabled,
                onSettingsForceGoogleEntryEnabledChange = onSettingsForceGoogleEntryEnabledChange,
                extremeRefresh165Enabled = extremeRefresh165Enabled,
                onExtremeRefresh165EnabledChange = onExtremeRefresh165EnabledChange,
                launcherLayoutUnlocked = launcherLayoutUnlocked,
                onLauncherLayoutUnlockedChange = onLauncherLayoutUnlockedChange,
                assistantScreenOption = assistantScreenOption,
                onAssistantScreenOptionChange = onAssistantScreenOptionChange,
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
                assistantPowerMode = assistantPowerMode,
                onAssistantPowerModeChange = onAssistantPowerModeChange,
                assistantGestureCircleEnabled = assistantGestureCircleEnabled,
                onAssistantGestureCircleEnabledChange = onAssistantGestureCircleEnabledChange,
                oosLocalizerPropertiesDraft = propertiesDraft,
                onOosLocalizerPropertiesDraftChange = { oosLocalizerPropertiesDraft = it },
                oosLocalizerScopePackages = scopePackages,
                oosLocalizerScopeDraft = scopeDraft,
                onOosLocalizerScopeDraftChange = { oosLocalizerScopeDraft = it },
                onOpenSubPage = onOpenSubPage,
            )
        }
    }

    RenderRestartScopeConfirmDialog()
}

private data class OosLocalizerPropertiesDraft(
    val region: String,
    val locale: String,
    val model: String,
    val propertyValues: Map<String, String>,
    val appFeatureValues: Map<String, String>,
    val featureEnabledStates: Map<String, Boolean>,
)

private data class OosLocalizerScopeDraft(
    val packageEnabledStates: Map<String, Boolean>,
)

private fun createOosLocalizerPropertiesDraft(
    context: Context,
    region: String,
    locale: String,
    model: String,
): OosLocalizerPropertiesDraft {
    return OosLocalizerPropertiesDraft(
        region = region,
        locale = locale,
        model = model,
        propertyValues = LspConfig.OOS_LOCALIZER_PROPERTY_DEFAULTS.mapValues { (key, _) ->
            LspConfig.getOosLocalizerProperty(context, key)
        },
        appFeatureValues = LspConfig.OOS_LOCALIZER_APP_FEATURE_DEFAULTS.mapValues { (key, _) ->
            LspConfig.getOosLocalizerAppFeature(context, key)
        },
        featureEnabledStates = LspConfig.OOS_LOCALIZER_FEATURE_DEFAULTS.keys.associateWith { feature ->
            LspConfig.isOosLocalizerFeatureEnabled(context, feature)
        },
    )
}

private fun createOosLocalizerScopeDraft(
    context: Context,
    scopePackages: List<String>,
): OosLocalizerScopeDraft {
    return OosLocalizerScopeDraft(
        packageEnabledStates = scopePackages.associateWith { packageName ->
            LspConfig.isOosLocalizerPackageEnabled(context, packageName)
        },
    )
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

@Composable
private fun featurePageTitle(mode: FeaturePageMode): String = when (mode) {
    FeaturePageMode.Main -> stringResource(R.string.tab_features)
    FeaturePageMode.Desktop -> stringResource(R.string.section_system_desktop)
    FeaturePageMode.SystemUi -> stringResource(R.string.section_lsp)
    FeaturePageMode.Settings -> stringResource(R.string.tab_settings)
    FeaturePageMode.Aod -> stringResource(R.string.feature_aod_enhance_title)
    FeaturePageMode.Assistant -> stringResource(R.string.feature_assistant_title)
    FeaturePageMode.OPlusLocalizer -> stringResource(R.string.feature_oos_localizer_title)
    FeaturePageMode.OPlusLocalizerProperties -> stringResource(R.string.feature_oos_localizer_group_properties)
    FeaturePageMode.OPlusLocalizerScope -> stringResource(R.string.feature_oos_localizer_group_scope)
    FeaturePageMode.Experimental -> stringResource(R.string.section_experimental)
}

private fun featureRestartPackages(mode: FeaturePageMode): List<String> = when (mode) {
    FeaturePageMode.Desktop -> listOf("com.android.launcher", "com.oplus.launcher", "com.coloros.launcher")
    FeaturePageMode.SystemUi -> listOf("com.android.systemui")
    FeaturePageMode.Settings -> listOf("com.android.settings")
    FeaturePageMode.Aod -> listOf("com.oplus.aod", "com.coloros.aod", "com.oplus.aodservice")
    FeaturePageMode.Assistant -> listOf("android", "system", "com.android.systemui")
    FeaturePageMode.OPlusLocalizer,
    FeaturePageMode.OPlusLocalizerProperties,
    FeaturePageMode.OPlusLocalizerScope -> OosLocalizerHooker.supportedPackageNames.sorted()
    FeaturePageMode.Main,
    FeaturePageMode.Experimental -> emptyList()
}

@Composable
private fun FeatureRestartActionButton(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = COUIIcons.Refresh,
            contentDescription = stringResource(R.string.feature_restart_scope_confirm_action),
            tint = COUITheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun FeatureMainPage(
    showChinaSpecialFeatures: Boolean,
    showGlobalSpecialFeatures: Boolean,
    onOpen: (FeaturePageMode) -> Unit,
) {
    val entries = buildList {
        add(
            FeatureMainEntry(
                titleRes = R.string.section_system_desktop,
                summaryRes = R.string.feature_category_desktop_summary,
                icon = AppIcons.Widgets,
                iconPackages = listOf("com.android.launcher", "com.oplus.launcher", "com.coloros.launcher"),
                pageMode = FeaturePageMode.Desktop,
            )
        )
        add(
            FeatureMainEntry(
                titleRes = R.string.section_lsp,
                summaryRes = R.string.feature_category_system_ui_summary,
                icon = AppIcons.Tune,
                iconPackages = listOf("com.android.systemui"),
                pageMode = FeaturePageMode.SystemUi,
            )
        )
        add(
            FeatureMainEntry(
                titleRes = R.string.tab_settings,
                summaryRes = R.string.feature_category_settings_summary,
                icon = AppIcons.Phone,
                iconPackages = listOf("com.android.settings"),
                pageMode = FeaturePageMode.Settings,
            )
        )
        add(
            FeatureMainEntry(
                titleRes = R.string.feature_aod_enhance_title,
                summaryRes = R.string.feature_category_aod_summary,
                icon = AppIcons.LightMode,
                iconPackages = listOf("com.oplus.aod", "com.coloros.aod", "com.oplus.aodservice"),
                pageMode = FeaturePageMode.Aod,
            )
        )
        if (showChinaSpecialFeatures) {
            add(
                FeatureMainEntry(
                    titleRes = R.string.feature_assistant_title,
                    summaryRes = R.string.feature_assistant_summary,
                    icon = AppIcons.Extension,
                    iconPackages = listOf(
                        "com.heytap.speechassist",
                        "com.coloros.speechassist",
                        "com.oplus.speechassist",
                        "com.google.android.googlequicksearchbox",
                    ),
                    pageMode = FeaturePageMode.Assistant,
                )
            )
        }
        if (showGlobalSpecialFeatures) {
            add(
                FeatureMainEntry(
                    titleRes = R.string.feature_oos_localizer_title,
                    summaryRes = R.string.feature_oos_localizer_home_summary,
                    icon = AppIcons.Extension,
                    iconPackages = listOf(
                        "com.oplus.aimemory",
                        "com.oplus.appplatform",
                        "com.oplus.exsystemservice",
                        "com.android.settings",
                    ),
                    pageMode = FeaturePageMode.OPlusLocalizer,
                )
            )
        }
    }

    SettingsGroup {
        entries.forEachIndexed { index, entry ->
            if (index > 0) {
                SettingsDivider()
            }
            FeatureEntryRow(
                title = stringResource(entry.titleRes),
                summary = stringResource(entry.summaryRes),
                icon = entry.icon,
                iconPackages = entry.iconPackages,
                onClick = { onOpen(entry.pageMode) },
                hasDividerAbove = index > 0,
                hasDividerBelow = index < entries.lastIndex,
            )
        }
    }
}

private data class FeatureMainEntry(
    @param:StringRes val titleRes: Int,
    @param:StringRes val summaryRes: Int,
    val icon: ImageVector,
    val iconPackages: List<String>,
    val pageMode: FeaturePageMode,
)

@Composable
private fun FeatureSubPage(
    mode: FeaturePageMode,
    showChinaSpecialFeatures: Boolean,
    showGlobalSpecialFeatures: Boolean,
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
    assistantPowerMode: Int,
    onAssistantPowerModeChange: (Int) -> Unit,
    assistantGestureCircleEnabled: Boolean,
    onAssistantGestureCircleEnabledChange: (Boolean) -> Unit,
    oosLocalizerPropertiesDraft: OosLocalizerPropertiesDraft?,
    onOosLocalizerPropertiesDraftChange: (OosLocalizerPropertiesDraft) -> Unit,
    oosLocalizerScopePackages: List<String>,
    oosLocalizerScopeDraft: OosLocalizerScopeDraft?,
    onOosLocalizerScopeDraftChange: (OosLocalizerScopeDraft) -> Unit,
    onOpenSubPage: (FeaturePageMode) -> Unit,
) {
    when (mode) {
        FeaturePageMode.Desktop -> DesktopFeaturesPage(
            showGlobalSpecialFeatures = showGlobalSpecialFeatures,
            launcherLayoutUnlocked = launcherLayoutUnlocked,
            onLauncherLayoutUnlockedChange = onLauncherLayoutUnlockedChange,
            assistantScreenOption = assistantScreenOption,
            onAssistantScreenOptionChange = onAssistantScreenOptionChange,
            launcherRegionMode = launcherRegionMode,
            onLauncherRegionModeChange = onLauncherRegionModeChange,
            recentTaskRadiusEnabled = recentTaskRadiusEnabled,
            onRecentTaskRadiusEnabledChange = onRecentTaskRadiusEnabledChange,
            recentTaskRadiusDp = recentTaskRadiusDp,
            onRecentTaskRadiusDpChange = onRecentTaskRadiusDpChange,
        )
        FeaturePageMode.SystemUi -> SystemUiFeaturesPage(
            showChinaSpecialFeatures = showChinaSpecialFeatures,
            nativeNotifyIconEnabled = nativeNotifyIconEnabled,
            onNativeNotifyIconEnabledChange = onNativeNotifyIconEnabledChange,
            nativeNotificationBubblesEnabled = nativeNotificationBubblesEnabled,
            onNativeNotificationBubblesEnabledChange = onNativeNotificationBubblesEnabledChange,
            statusMobileTypeEnabled = statusMobileTypeEnabled,
            onStatusMobileTypeEnabledChange = onStatusMobileTypeEnabledChange,
            statusMobileTypeHideDataOffEnabled = statusMobileTypeHideDataOffEnabled,
            onStatusMobileTypeHideDataOffEnabledChange = onStatusMobileTypeHideDataOffEnabledChange,
            statusMobileTypeHideWifiEnabled = statusMobileTypeHideWifiEnabled,
            onStatusMobileTypeHideWifiEnabledChange = onStatusMobileTypeHideWifiEnabledChange,
        )
        FeaturePageMode.Settings -> SettingsFeaturesPage(
            permissionMonitorVisible = permissionMonitorVisible,
            onPermissionMonitorVisibleChange = onPermissionMonitorVisibleChange,
            settingsForceGoogleEntryEnabled = settingsForceGoogleEntryEnabled,
            onSettingsForceGoogleEntryEnabledChange = onSettingsForceGoogleEntryEnabledChange,
        )
        FeaturePageMode.Aod -> AodFeaturesPage(
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
        )
        FeaturePageMode.Assistant -> AssistantFeaturesPage(
            assistantPowerMode = assistantPowerMode,
            onAssistantPowerModeChange = onAssistantPowerModeChange,
            assistantGestureCircleEnabled = assistantGestureCircleEnabled,
            onAssistantGestureCircleEnabledChange = onAssistantGestureCircleEnabledChange,
        )
        FeaturePageMode.OPlusLocalizer -> OPlusLocalizerFeaturesPage(
            oosLocalizerEnabled = oosLocalizerEnabled,
            onOosLocalizerEnabledChange = onOosLocalizerEnabledChange,
            oosLocalizerConfigMode = oosLocalizerConfigMode,
            onOosLocalizerConfigModeChange = onOosLocalizerConfigModeChange,
            onOpenSubPage = onOpenSubPage,
        )
        FeaturePageMode.OPlusLocalizerProperties -> OPlusLocalizerPropertiesPage(
            oosLocalizerRegion = oosLocalizerRegion,
            onOosLocalizerRegionChange = onOosLocalizerRegionChange,
            oosLocalizerLocale = oosLocalizerLocale,
            onOosLocalizerLocaleChange = onOosLocalizerLocaleChange,
            oosLocalizerModel = oosLocalizerModel,
            onOosLocalizerModelChange = onOosLocalizerModelChange,
            draft = oosLocalizerPropertiesDraft,
            onDraftChange = onOosLocalizerPropertiesDraftChange,
        )
        FeaturePageMode.OPlusLocalizerScope -> OPlusLocalizerScopePage(
            scopePackages = oosLocalizerScopePackages,
            draft = oosLocalizerScopeDraft,
            onDraftChange = onOosLocalizerScopeDraftChange,
        )
        FeaturePageMode.Experimental -> ExperimentalFeaturesPage(
            extremeRefresh165Enabled = extremeRefresh165Enabled,
            onExtremeRefresh165EnabledChange = onExtremeRefresh165EnabledChange,
        )
        FeaturePageMode.Main -> Unit
    }
}

@Composable
private fun DesktopFeaturesPage(
    showGlobalSpecialFeatures: Boolean,
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
) {
    if (showGlobalSpecialFeatures) {
        SettingsSection(title = stringResource(R.string.feature_group_minus_one))
        SettingsGroup {
            AssistantScreenRow(
                title = stringResource(R.string.event_page_tool_title),
                summary = stringResource(R.string.event_page_tool_summary),
                selectedOption = assistantScreenOption,
                onOptionChange = onAssistantScreenOptionChange,
                hasDividerAbove = false,
                hasDividerBelow = false,
            )
        }
    }

    SettingsSection(title = stringResource(R.string.feature_group_region))
    SettingsGroup {
        LauncherRegionRow(
            title = stringResource(R.string.feature_launcher_region_title),
            summary = "",
            selectedMode = launcherRegionMode,
            onModeChange = onLauncherRegionModeChange,
            hasDividerAbove = false,
            hasDividerBelow = false,
        )
    }

    SettingsSection(title = stringResource(R.string.feature_group_layout))
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.feature_launcher_layout_unlock_title),
            summary = stringResource(R.string.feature_launcher_layout_unlock_summary),
            checked = launcherLayoutUnlocked,
            onCheckedChange = onLauncherLayoutUnlockedChange,
            hasDividerAbove = false,
            hasDividerBelow = false,
        )
    }

    SettingsSection(title = stringResource(R.string.feature_group_recent_tasks))
    SettingsGroup {
        RecentTaskRadiusRow(
            title = stringResource(R.string.feature_recent_task_radius_title),
            summary = stringResource(R.string.feature_recent_task_radius_summary),
            checked = recentTaskRadiusEnabled,
            onCheckedChange = onRecentTaskRadiusEnabledChange,
            valueDp = recentTaskRadiusDp,
            onValueDpChange = onRecentTaskRadiusDpChange,
            hasDividerAbove = false,
        )
    }
}

@Composable
private fun AssistantScreenRow(
    title: String,
    summary: String,
    selectedOption: AssistantScreenOption,
    onOptionChange: (AssistantScreenOption) -> Unit,
    hasDividerAbove: Boolean,
    hasDividerBelow: Boolean,
) {
    val options = listOf(
        AssistantScreenOption.Shelf to stringResource(R.string.event_shelf_option),
        AssistantScreenOption.Disabled to stringResource(R.string.event_disable_option),
        AssistantScreenOption.Default to stringResource(R.string.event_default_option),
    )
    val selectedLabel = options.firstOrNull { it.first == selectedOption }?.second ?: options.last().second
    OptionDropdownRow(
        title = title,
        summary = summary,
        selectedLabel = selectedLabel,
        options = options,
        selectedValue = selectedOption,
        onValueChange = onOptionChange,
        hasDividerAbove = hasDividerAbove,
        hasDividerBelow = hasDividerBelow,
    )
}

@Composable
private fun LauncherRegionRow(
    title: String,
    summary: String,
    selectedMode: Int,
    onModeChange: (Int) -> Unit,
    hasDividerAbove: Boolean,
    hasDividerBelow: Boolean,
) {
    val options = listOf(
        LspConfig.LAUNCHER_REGION_MODE_OFF to stringResource(R.string.feature_launcher_region_off),
        LspConfig.LAUNCHER_REGION_MODE_CN to stringResource(R.string.feature_launcher_region_cn),
        LspConfig.LAUNCHER_REGION_MODE_IN to stringResource(R.string.feature_launcher_region_in),
    )
    val selectedLabel = options.firstOrNull { it.first == selectedMode }?.second ?: options.first().second
    OptionDropdownRow(
        title = title,
        summary = summary,
        selectedLabel = selectedLabel,
        options = options,
        selectedValue = selectedMode,
        onValueChange = onModeChange,
        hasDividerAbove = hasDividerAbove,
        hasDividerBelow = hasDividerBelow,
    )
}

@Composable
private fun <T> OptionDropdownRow(
    title: String,
    summary: String,
    selectedLabel: String,
    options: List<Pair<T, String>>,
    selectedValue: T,
    onValueChange: (T) -> Unit,
    hasDividerAbove: Boolean,
    hasDividerBelow: Boolean,
) {
    val selectedIndex = options
        .indexOfFirst { it.first == selectedValue }
        .takeIf { it >= 0 }
        ?: 0
    SettingsWindowDropdownPreference(
        title = title,
        summary = summary.takeIf { it.isNotBlank() },
        items = options.map { it.second },
        selectedIndex = selectedIndex,
        insideMargin = SettingsTokens.BasicComponentInsideMargin,
        onSelectedIndexChange = { index ->
            options.getOrNull(index)?.first?.let(onValueChange)
        },
    )
}

@Composable
private fun RecentTaskRadiusRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    valueDp: Int,
    onValueDpChange: (Int) -> Unit,
    hasDividerAbove: Boolean,
) {
    val hapticTick = rememberColorOsHapticTick()
    Column {
        SettingsToggleRow(
            title = title,
            summary = summary,
            checked = checked,
            onCheckedChange = onCheckedChange,
            hasDividerAbove = hasDividerAbove,
            hasDividerBelow = checked,
        )
        AnimatedVisibility(
            visible = checked,
            enter = expandVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)),
            exit = shrinkVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            ) {
                SettingsDivider()
                Text(
                    text = stringResource(R.string.feature_slider_current_dp, valueDp),
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
                Slider(
                    value = valueDp.toFloat(),
                    onValueChange = { next ->
                        val nextValue = next.toInt()
                        if (nextValue != valueDp) {
                            hapticTick()
                            onValueDpChange(nextValue)
                        }
                    },
                    valueRange = 0f..260f,
                    steps = 259,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun SystemUiFeaturesPage(
    showChinaSpecialFeatures: Boolean,
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
) {
    SettingsSection(title = stringResource(R.string.feature_group_native))
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.feature_native_notify_icon_title),
            summary = stringResource(R.string.feature_native_notify_icon_summary),
            checked = nativeNotifyIconEnabled,
            onCheckedChange = onNativeNotifyIconEnabledChange,
            hasDividerBelow = showChinaSpecialFeatures,
        )
        if (showChinaSpecialFeatures) {
            SettingsDivider()
            SettingsToggleRow(
                title = stringResource(R.string.feature_native_notification_bubbles_title),
                summary = stringResource(R.string.feature_native_notification_bubbles_summary),
                checked = nativeNotificationBubblesEnabled,
                onCheckedChange = onNativeNotificationBubblesEnabledChange,
                hasDividerAbove = true,
            )
        }
    }

    SettingsSection(title = stringResource(R.string.feature_group_beautify))
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.feature_status_mobile_type_title),
            summary = stringResource(R.string.feature_status_mobile_type_summary),
            checked = statusMobileTypeEnabled,
            onCheckedChange = onStatusMobileTypeEnabledChange,
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.feature_status_mobile_type_hide_data_off_title),
            summary = stringResource(R.string.feature_status_mobile_type_hide_data_off_summary),
            checked = statusMobileTypeHideDataOffEnabled,
            onCheckedChange = onStatusMobileTypeHideDataOffEnabledChange,
            hasDividerAbove = true,
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.feature_status_mobile_type_hide_wifi_title),
            summary = stringResource(R.string.feature_status_mobile_type_hide_wifi_summary),
            checked = statusMobileTypeHideWifiEnabled,
            onCheckedChange = onStatusMobileTypeHideWifiEnabledChange,
            hasDividerAbove = true,
        )
    }
}

@Composable
private fun SettingsFeaturesPage(
    permissionMonitorVisible: Boolean,
    onPermissionMonitorVisibleChange: (Boolean) -> Unit,
    settingsForceGoogleEntryEnabled: Boolean,
    onSettingsForceGoogleEntryEnabledChange: (Boolean) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.feature_group_developer_options))
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.feature_permission_monitor_title),
            summary = stringResource(R.string.feature_permission_monitor_summary),
            checked = permissionMonitorVisible,
            onCheckedChange = onPermissionMonitorVisibleChange,
        )
    }

    SettingsSection(title = stringResource(R.string.feature_group_settings_hidden_features))
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.feature_settings_force_google_title),
            summary = stringResource(R.string.feature_settings_force_google_summary),
            checked = settingsForceGoogleEntryEnabled,
            onCheckedChange = onSettingsForceGoogleEntryEnabledChange,
        )
    }
}

@Composable
private fun AodFeaturesPage(
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
) {
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.feature_aod_enhance_toggle_title),
            summary = "",
            checked = aodEnhanceEnabled,
            onCheckedChange = onAodEnhanceEnabledChange,
            hasDividerBelow = true,
        )
        SettingsDivider()
        AodIntSliderRow(
            title = stringResource(R.string.feature_aod_dark_brightness_current, aodInitDarkBrightness),
            value = aodInitDarkBrightness,
            onValueChange = onAodInitDarkBrightnessChange,
            hasDividerAbove = true,
            hasDividerBelow = true,
        )
        SettingsDivider()
        AodIntSliderRow(
            title = stringResource(R.string.feature_aod_bright_brightness_current, aodInitBrightBrightness),
            value = aodInitBrightBrightness,
            onValueChange = onAodInitBrightBrightnessChange,
            hasDividerAbove = true,
            hasDividerBelow = true,
        )
        SettingsDivider()
        AodFloatSliderRow(
            title = stringResource(
                R.string.feature_aod_multiplier_current,
                formatAodMultiplier(aodRunningBrightnessMultiplier),
            ),
            value = aodRunningBrightnessMultiplier,
            onValueChange = onAodRunningBrightnessMultiplierChange,
            hasDividerAbove = true,
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.feature_aod_panoramic_title),
            summary = "",
            checked = aodPanoramicSupportEnabled,
            onCheckedChange = onAodPanoramicSupportEnabledChange,
            hasDividerAbove = true,
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.feature_aod_settings_switch_title),
            summary = "",
            checked = aodSettingsSwitchEnabled,
            onCheckedChange = onAodSettingsSwitchEnabledChange,
            hasDividerAbove = true,
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.feature_aod_single_click_block_title),
            summary = "",
            checked = aodSingleClickBlockEnabled,
            onCheckedChange = onAodSingleClickBlockEnabledChange,
            hasDividerAbove = true,
        )
    }
}

@Composable
private fun AodIntSliderRow(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
) {
    val hapticTick = rememberColorOsHapticTick()
    AodSliderContainer(
        title = title,
        hasDividerAbove = hasDividerAbove,
        hasDividerBelow = hasDividerBelow,
    ) {
        Slider(
            value = value.toFloat(),
            onValueChange = { next ->
                val nextValue = next.roundToInt().coerceIn(0, 255)
                if (nextValue != value) {
                    hapticTick()
                    onValueChange(nextValue)
                }
            },
            valueRange = 0f..255f,
            steps = 254,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AodFloatSliderRow(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
) {
    val hapticTick = rememberColorOsHapticTick()
    AodSliderContainer(
        title = title,
        hasDividerAbove = hasDividerAbove,
        hasDividerBelow = hasDividerBelow,
    ) {
        Slider(
            value = value,
            onValueChange = { next ->
                val nextValue = (next * 10f).roundToInt().div(10f).coerceIn(1.0f, 3.0f)
                if (nextValue != value) {
                    hapticTick()
                    onValueChange(nextValue)
                }
            },
            valueRange = 1.0f..3.0f,
            steps = 19,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AodSliderContainer(
    title: String,
    hasDividerAbove: Boolean,
    hasDividerBelow: Boolean,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .settingsInteractiveRowHighlight(
                interactionSource = interactionSource,
                color = Color.Transparent,
                hasDividerAbove = hasDividerAbove,
                hasDividerBelow = hasDividerBelow,
            )
            .padding(vertical = 10.dp),
    ) {
        Text(
            text = title,
            style = COUITheme.textStyles.title3,
            color = COUITheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

private fun formatAodMultiplier(value: Float): String {
    val rounded = (value * 10f).roundToInt() / 10f
    return if (rounded % 1f == 0f) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

@Composable
private fun AssistantFeaturesPage(
    assistantPowerMode: Int,
    onAssistantPowerModeChange: (Int) -> Unit,
    assistantGestureCircleEnabled: Boolean,
    onAssistantGestureCircleEnabledChange: (Boolean) -> Unit,
) {
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.feature_assistant_power_title),
            summary = stringResource(R.string.feature_assistant_power_summary),
            checked = assistantPowerMode != LspConfig.ASSISTANT_POWER_MODE_NONE,
            onCheckedChange = { enabled ->
                onAssistantPowerModeChange(
                    if (enabled) {
                        LspConfig.ASSISTANT_POWER_MODE_GEMINI
                    } else {
                        LspConfig.ASSISTANT_POWER_MODE_NONE
                    },
                )
            },
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.feature_assistant_gesture_title),
            summary = stringResource(R.string.feature_assistant_gesture_summary),
            checked = assistantGestureCircleEnabled,
            onCheckedChange = onAssistantGestureCircleEnabledChange,
            hasDividerAbove = true,
        )
    }
}

@Composable
private fun OPlusLocalizerFeaturesPage(
    oosLocalizerEnabled: Boolean,
    onOosLocalizerEnabledChange: (Boolean) -> Unit,
    oosLocalizerConfigMode: Int,
    onOosLocalizerConfigModeChange: (Int) -> Unit,
    onOpenSubPage: (FeaturePageMode) -> Unit,
) {
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.feature_oos_localizer_master_switch),
            summary = "",
            checked = oosLocalizerEnabled,
            onCheckedChange = onOosLocalizerEnabledChange,
            hasDividerBelow = false,
        )
    }

    LocalizerExpandableContent(visible = oosLocalizerEnabled) {
        SettingsSection(title = stringResource(R.string.feature_oos_localizer_config_title))
        SettingsGroup {
            OosLocalizerConfigModeCard(
                title = stringResource(R.string.feature_oos_localizer_config_default),
                summary = stringResource(R.string.feature_oos_localizer_config_default_summary),
                selected = oosLocalizerConfigMode == LspConfig.OOS_LOCALIZER_CONFIG_DEFAULT,
                onClick = { onOosLocalizerConfigModeChange(LspConfig.OOS_LOCALIZER_CONFIG_DEFAULT) },
                hasDividerBelow = true,
            )
            SettingsDivider()
            OosLocalizerConfigModeCard(
                title = stringResource(R.string.feature_oos_localizer_config_custom),
                summary = stringResource(R.string.feature_oos_localizer_config_custom_summary),
                selected = oosLocalizerConfigMode == LspConfig.OOS_LOCALIZER_CONFIG_CUSTOM,
                onClick = { onOosLocalizerConfigModeChange(LspConfig.OOS_LOCALIZER_CONFIG_CUSTOM) },
                hasDividerAbove = true,
            )
        }
    }

    LocalizerExpandableContent(
        visible = oosLocalizerEnabled && oosLocalizerConfigMode == LspConfig.OOS_LOCALIZER_CONFIG_CUSTOM,
    ) {
        SettingsGroup {
            SettingsCardRow(
                title = stringResource(R.string.feature_oos_localizer_group_properties),
                summary = "",
                onClick = { onOpenSubPage(FeaturePageMode.OPlusLocalizerProperties) },
                showArrow = true,
                hasDividerBelow = true,
            )
            SettingsDivider()
            SettingsCardRow(
                title = stringResource(R.string.feature_oos_localizer_group_scope),
                summary = "",
                onClick = { onOpenSubPage(FeaturePageMode.OPlusLocalizerScope) },
                showArrow = true,
                hasDividerAbove = true,
            )
        }
    }
}

@Composable
private fun OosLocalizerConfigModeCard(
    title: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
) {
    BasicComponent(
        insideMargin = SettingsTokens.BasicComponentInsideMargin,
        onClick = onClick,
        endActions = {
            RadioButton(
                selected = selected,
                onClick = null,
            )
        },
    ) {
        SettingsRowTextContent(
            title = title,
            summary = summary.takeIf { it.isNotBlank() },
        )
    }
}

@Composable
private fun OPlusLocalizerPropertiesPage(
    oosLocalizerRegion: String,
    onOosLocalizerRegionChange: (String) -> Unit,
    oosLocalizerLocale: String,
    onOosLocalizerLocaleChange: (String) -> Unit,
    oosLocalizerModel: String,
    onOosLocalizerModelChange: (String) -> Unit,
    draft: OosLocalizerPropertiesDraft?,
    onDraftChange: (OosLocalizerPropertiesDraft) -> Unit,
) {
    val context = LocalContext.current
    val currentDraft = draft ?: createOosLocalizerPropertiesDraft(
        context = context,
        region = oosLocalizerRegion,
        locale = oosLocalizerLocale,
        model = oosLocalizerModel,
    )
    fun updateDraft(transform: (OosLocalizerPropertiesDraft) -> OosLocalizerPropertiesDraft) {
        onDraftChange(transform(currentDraft))
    }
    val propertyValues = currentDraft.propertyValues
    val appFeatureValues = currentDraft.appFeatureValues
    val featureEnabledStates = currentDraft.featureEnabledStates
    val regionEnabled = featureEnabledStates[LspConfig.OOS_LOCALIZER_FEATURE_REGION] ?: true
    val localeEnabled = featureEnabledStates[LspConfig.OOS_LOCALIZER_FEATURE_LOCALE] ?: true
    val modelEnabled = featureEnabledStates[LspConfig.OOS_LOCALIZER_FEATURE_BUILD_MODEL] ?: true
    val propertiesEnabled = featureEnabledStates[LspConfig.OOS_LOCALIZER_FEATURE_PROPERTIES] ?: true
    val appFeaturesEnabled = featureEnabledStates[LspConfig.OOS_LOCALIZER_FEATURE_APP_FEATURES] ?: true
    fun saveCustomContent() {
        onOosLocalizerRegionChange(currentDraft.region)
        onOosLocalizerLocaleChange(currentDraft.locale)
        onOosLocalizerModelChange(currentDraft.model)
        LspConfig.setOosLocalizerRegion(context, currentDraft.region)
        LspConfig.setOosLocalizerLocale(context, currentDraft.locale)
        LspConfig.setOosLocalizerModel(context, currentDraft.model)
        propertyValues.forEach { (key, value) ->
            LspConfig.setOosLocalizerProperty(context, key, value)
        }
        appFeatureValues.forEach { (key, value) ->
            LspConfig.setOosLocalizerAppFeature(context, key, value)
        }
        featureEnabledStates.forEach { (feature, enabled) ->
            LspConfig.setOosLocalizerFeatureEnabled(context, feature, enabled)
        }
        Toast.makeText(context, R.string.feature_oos_localizer_save_success, Toast.LENGTH_SHORT).show()
    }

    fun resetCustomContent() {
        val defaultProperties = LspConfig.OOS_LOCALIZER_PROPERTY_DEFAULTS.toMap()
        val defaultAppFeatures = LspConfig.OOS_LOCALIZER_APP_FEATURE_DEFAULTS.toMap()
        val defaultFeatures = LspConfig.OOS_LOCALIZER_FEATURE_DEFAULTS.toMap()
        onDraftChange(
            OosLocalizerPropertiesDraft(
                region = LspConfig.DEFAULT_OOS_LOCALIZER_REGION,
                locale = LspConfig.DEFAULT_OOS_LOCALIZER_LOCALE,
                model = LspConfig.DEFAULT_OOS_LOCALIZER_MODEL,
                propertyValues = defaultProperties,
                appFeatureValues = defaultAppFeatures,
                featureEnabledStates = defaultFeatures,
            )
        )
        onOosLocalizerRegionChange(LspConfig.DEFAULT_OOS_LOCALIZER_REGION)
        onOosLocalizerLocaleChange(LspConfig.DEFAULT_OOS_LOCALIZER_LOCALE)
        onOosLocalizerModelChange(LspConfig.DEFAULT_OOS_LOCALIZER_MODEL)
        LspConfig.setOosLocalizerRegion(context, LspConfig.DEFAULT_OOS_LOCALIZER_REGION)
        LspConfig.setOosLocalizerLocale(context, LspConfig.DEFAULT_OOS_LOCALIZER_LOCALE)
        LspConfig.setOosLocalizerModel(context, LspConfig.DEFAULT_OOS_LOCALIZER_MODEL)
        defaultProperties.forEach { (key, value) ->
            LspConfig.setOosLocalizerProperty(context, key, value)
        }
        defaultAppFeatures.forEach { (key, value) ->
            LspConfig.setOosLocalizerAppFeature(context, key, value)
        }
        defaultFeatures.forEach { (feature, enabled) ->
            LspConfig.setOosLocalizerFeatureEnabled(context, feature, enabled)
        }
        Toast.makeText(context, R.string.feature_oos_localizer_reset_success, Toast.LENGTH_SHORT).show()
    }

    LocalizerActionButtons(
        onReset = ::resetCustomContent,
        onSave = ::saveCustomContent,
    )

    SettingsGroup {
        LocalizerFeatureToggleRow(
            feature = LspConfig.OOS_LOCALIZER_FEATURE_REGION,
            enabledStates = featureEnabledStates,
            onEnabledStatesChange = { states ->
                updateDraft { it.copy(featureEnabledStates = states) }
            },
            hasDividerBelow = regionEnabled,
        )
        LocalizerExpandableContent(visible = regionEnabled) {
            SettingsDivider()
            LocalizerTextFieldRow(
                title = stringResource(R.string.feature_oos_localizer_region_title),
                value = currentDraft.region,
                placeholder = LspConfig.DEFAULT_OOS_LOCALIZER_REGION,
                onValueChange = { value ->
                    updateDraft { it.copy(region = value) }
                },
                hasDividerAbove = true,
                hasDividerBelow = true,
            )
        }
    }

    SettingsGroup {
        LocalizerFeatureToggleRow(
            feature = LspConfig.OOS_LOCALIZER_FEATURE_LOCALE,
            enabledStates = featureEnabledStates,
            onEnabledStatesChange = { states ->
                updateDraft { it.copy(featureEnabledStates = states) }
            },
            hasDividerBelow = localeEnabled,
        )
        LocalizerExpandableContent(visible = localeEnabled) {
            SettingsDivider()
            LocalizerTextFieldRow(
                title = stringResource(R.string.feature_oos_localizer_locale_title),
                value = currentDraft.locale,
                placeholder = LspConfig.DEFAULT_OOS_LOCALIZER_LOCALE,
                onValueChange = { value ->
                    updateDraft { it.copy(locale = value) }
                },
                hasDividerAbove = true,
                hasDividerBelow = true,
            )
        }
    }

    SettingsGroup {
        LocalizerFeatureToggleRow(
            feature = LspConfig.OOS_LOCALIZER_FEATURE_BUILD_MODEL,
            enabledStates = featureEnabledStates,
            onEnabledStatesChange = { states ->
                updateDraft { it.copy(featureEnabledStates = states) }
            },
            hasDividerBelow = modelEnabled,
        )
        LocalizerExpandableContent(visible = modelEnabled) {
            SettingsDivider()
            LocalizerTextFieldRow(
                title = stringResource(R.string.feature_oos_localizer_model_title),
                value = currentDraft.model,
                placeholder = LspConfig.DEFAULT_OOS_LOCALIZER_MODEL,
                onValueChange = { value ->
                    updateDraft { it.copy(model = value) }
                },
                hasDividerAbove = true,
            )
        }
    }

    SettingsGroup {
        LocalizerFeatureToggleRow(
            feature = LspConfig.OOS_LOCALIZER_FEATURE_PROPERTIES,
            enabledStates = featureEnabledStates,
            onEnabledStatesChange = { states ->
                updateDraft { it.copy(featureEnabledStates = states) }
            },
            hasDividerBelow = propertiesEnabled,
        )
        LocalizerExpandableContent(visible = propertiesEnabled) {
            SettingsDivider()
            LspConfig.OOS_LOCALIZER_PROPERTY_DEFAULTS.entries.forEachIndexed { index, entry ->
                LocalizerTextFieldRow(
                    title = entry.key,
                    value = propertyValues[entry.key].orEmpty(),
                    placeholder = entry.value,
                    onValueChange = { value ->
                        updateDraft {
                            it.copy(propertyValues = it.propertyValues + (entry.key to value))
                        }
                    },
                    hasDividerAbove = true,
                    hasDividerBelow = index != LspConfig.OOS_LOCALIZER_PROPERTY_DEFAULTS.size - 1,
                )
                if (index != LspConfig.OOS_LOCALIZER_PROPERTY_DEFAULTS.size - 1) SettingsDivider()
            }
        }
    }

    SettingsGroup {
        LocalizerFeatureToggleRow(
            feature = LspConfig.OOS_LOCALIZER_FEATURE_APP_FEATURES,
            enabledStates = featureEnabledStates,
            onEnabledStatesChange = { states ->
                updateDraft { it.copy(featureEnabledStates = states) }
            },
            hasDividerBelow = appFeaturesEnabled,
        )
        LocalizerExpandableContent(visible = appFeaturesEnabled) {
            SettingsDivider()
            LspConfig.OOS_LOCALIZER_APP_FEATURE_DEFAULTS.entries.forEachIndexed { index, entry ->
                LocalizerTextFieldRow(
                    title = entry.key,
                    value = appFeatureValues[entry.key].orEmpty(),
                    placeholder = entry.value,
                    onValueChange = { value ->
                        updateDraft {
                            it.copy(appFeatureValues = it.appFeatureValues + (entry.key to value))
                        }
                    },
                    hasDividerAbove = true,
                    hasDividerBelow = index != LspConfig.OOS_LOCALIZER_APP_FEATURE_DEFAULTS.size - 1,
                )
                if (index != LspConfig.OOS_LOCALIZER_APP_FEATURE_DEFAULTS.size - 1) SettingsDivider()
            }
        }
    }
}

@Composable
private fun OPlusLocalizerScopePage(
    scopePackages: List<String>,
    draft: OosLocalizerScopeDraft?,
    onDraftChange: (OosLocalizerScopeDraft) -> Unit,
) {
    val context = LocalContext.current
    val currentDraft = draft ?: createOosLocalizerScopeDraft(
        context = context,
        scopePackages = scopePackages,
    )
    val packageEnabledStates = currentDraft.packageEnabledStates

    fun saveScopeContent() {
        packageEnabledStates.forEach { (packageName, enabled) ->
            LspConfig.setOosLocalizerPackageEnabled(context, packageName, enabled)
        }
        Toast.makeText(context, R.string.feature_oos_localizer_save_success, Toast.LENGTH_SHORT).show()
    }

    fun resetScopeContent() {
        val defaultStates = scopePackages.associateWith { true }
        onDraftChange(OosLocalizerScopeDraft(defaultStates))
        defaultStates.forEach { (packageName, enabled) ->
            LspConfig.setOosLocalizerPackageEnabled(context, packageName, enabled)
        }
        Toast.makeText(context, R.string.feature_oos_localizer_reset_success, Toast.LENGTH_SHORT).show()
    }

    LocalizerActionButtons(
        onReset = ::resetScopeContent,
        onSave = ::saveScopeContent,
        title = stringResource(R.string.feature_oos_localizer_scope_notice_title),
        summary = stringResource(R.string.feature_oos_localizer_scope_notice_summary),
        titleColor = COUITheme.colorScheme.onSurface,
    )

    SettingsGroup {
        scopePackages.forEachIndexed { index, packageName ->
            if (index > 0) SettingsDivider()
            SettingsToggleRow(
                title = packageName,
                summary = "",
                checked = packageEnabledStates[packageName] ?: true,
                onCheckedChange = { enabled ->
                    onDraftChange(
                        currentDraft.copy(
                            packageEnabledStates = currentDraft.packageEnabledStates + (packageName to enabled),
                        )
                    )
                },
                hasDividerAbove = index > 0,
                hasDividerBelow = index != scopePackages.lastIndex,
            )
        }
    }
}

@Composable
private fun localizerFeatureTitle(feature: String): String {
    return when (feature) {
        LspConfig.OOS_LOCALIZER_FEATURE_PROPERTIES ->
            stringResource(R.string.feature_oos_localizer_feature_properties)
        LspConfig.OOS_LOCALIZER_FEATURE_REGION ->
            stringResource(R.string.feature_oos_localizer_feature_region)
        LspConfig.OOS_LOCALIZER_FEATURE_LOCALE ->
            stringResource(R.string.feature_oos_localizer_feature_locale)
        LspConfig.OOS_LOCALIZER_FEATURE_BUILD_MODEL ->
            stringResource(R.string.feature_oos_localizer_feature_build_model)
        LspConfig.OOS_LOCALIZER_FEATURE_APP_FEATURES ->
            stringResource(R.string.feature_oos_localizer_feature_app_features)
        else -> feature
    }
}
@Composable
private fun LocalizerActionButtons(
    onReset: () -> Unit,
    onSave: () -> Unit,
    title: String = stringResource(R.string.feature_oos_localizer_risk_title),
    summary: String = stringResource(R.string.feature_oos_localizer_risk_summary),
    titleColor: Color = Color(0xFFFF4D5E),
) {
    val hapticClick = rememberHapticClick()
    SettingsGroup {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = title,
                style = COUITheme.textStyles.body1,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = summary,
                style = COUITheme.textStyles.body1,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = {
                        hapticClick()
                        onReset()
                    },
                    modifier = Modifier.weight(1f),
                    cornerRadius = 20.dp,
                    minHeight = 38.dp,
                ) {
                    Text(
                        text = stringResource(R.string.feature_oos_localizer_action_reset),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Button(
                    onClick = {
                        hapticClick()
                        onSave()
                    },
                    modifier = Modifier.weight(1f),
                    cornerRadius = 20.dp,
                    minHeight = 38.dp,
                ) {
                    Text(
                        text = stringResource(R.string.feature_oos_localizer_action_save),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalizerExpandableContent(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        ) + fadeIn(animationSpec = tween(durationMillis = 180)),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        ) + fadeOut(animationSpec = tween(durationMillis = 140)),
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun LocalizerFeatureToggleRow(
    feature: String,
    enabledStates: Map<String, Boolean>,
    onEnabledStatesChange: (Map<String, Boolean>) -> Unit,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
) {
    SettingsToggleRow(
        title = localizerFeatureTitle(feature),
        summary = "",
        checked = enabledStates[feature] ?: true,
        onCheckedChange = { enabled ->
            onEnabledStatesChange(enabledStates + (feature to enabled))
        },
        hasDividerAbove = hasDividerAbove,
        hasDividerBelow = hasDividerBelow,
    )
}

@Composable
private fun LocalizerTextFieldRow(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = SettingsTokens.RowHeight)
            .settingsInteractiveRowHighlight(
                interactionSource = remember { MutableInteractionSource() },
                color = COUITheme.colorScheme.onSurface.copy(alpha = 0.08f),
                hasDividerAbove = hasDividerAbove,
                hasDividerBelow = hasDividerBelow,
            )
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = title,
            style = COUITheme.textStyles.body1,
            color = COUITheme.colorScheme.onSurfaceVariantSummary,
            fontSize = SettingsTokens.RowSummaryFontSize,
            lineHeight = SettingsTokens.RowSummaryLineHeight,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = placeholder,
            backgroundMode = TextFieldMode.None,
            singleLine = false,
            minLines = 1,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
    }
}

@Composable
private fun ExperimentalFeaturesPage(
    extremeRefresh165Enabled: Boolean,
    onExtremeRefresh165EnabledChange: (Boolean) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.feature_group_refresh_rate))
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.feature_extreme_refresh_165_title),
            summary = stringResource(R.string.feature_extreme_refresh_165_summary),
            checked = extremeRefresh165Enabled,
            onCheckedChange = onExtremeRefresh165EnabledChange,
            hasDividerBelow = false,
        )
    }
}

@Composable
private fun FeatureEntryRow(
    title: String,
    summary: String,
    icon: ImageVector,
    iconPackages: List<String>,
    onClick: () -> Unit,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
) {
    SettingsCardRow(
        title = title,
        summary = summary,
        onClick = onClick,
        showArrow = true,
        hasDividerAbove = hasDividerAbove,
        hasDividerBelow = hasDividerBelow,
        leadingContent = { FeatureIcon(icon = icon, packageNames = iconPackages) },
    )
}

@Composable
private fun FeatureIcon(
    icon: ImageVector,
    packageNames: List<String> = emptyList(),
) {
    val context = LocalContext.current
    val fallbackColor = COUITheme.colorScheme.primary
    val appIconInfo by produceState<AppIconInfo?>(
        initialValue = null,
        key1 = context,
        key2 = packageNames.joinToString(),
        key3 = fallbackColor,
    ) {
        value = withContext(Dispatchers.Default) {
            packageNames.firstNotNullOfOrNull { packageName ->
                loadPackageIconInfo(context, packageName, fallbackColor)
            }
        }
    }
    val iconModifier = Modifier
        .size(52.dp)
        .clip(RoundedCornerShape(14.dp))

    if (appIconInfo != null) {
        val info = appIconInfo!!
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
        ) {
            if (info.systemFrameworkIcon) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(45.dp)
                        .drawColoredShadow(
                            color = Color.White,
                            alpha = 0.9f,
                            borderRadius = 13.dp,
                            shadowRadius = 7.dp,
                            roundedRect = false,
                        )
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White),
                ) {
                    Image(
                        bitmap = info.icon.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(45.dp)
                        .drawColoredShadow(
                            color = info.dominantColor,
                            alpha = 1f,
                            borderRadius = 13.dp,
                            shadowRadius = 7.dp,
                            roundedRect = false,
                        )
                        .clip(RoundedCornerShape(13.dp))
                        .background(info.dominantColor),
                ) {
                    Image(
                        bitmap = info.icon.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(45.dp),
                    )
                }
            }
        }
        return
    }

    Box(modifier = iconModifier)
}

private data class AppIconInfo(
    val icon: Bitmap,
    val dominantColor: Color,
    val systemFrameworkIcon: Boolean = false,
)

private fun loadPackageIconInfo(
    context: Context,
    packageName: String,
    defaultColor: Color,
): AppIconInfo? {
    return runCatching {
        val packageManager = context.packageManager
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        val icon = appInfo.loadIcon(packageManager)
        val bitmap = icon.toBitmap()
        AppIconInfo(
            icon = bitmap,
            dominantColor = bitmap.extractPlateColor(defaultColor),
            systemFrameworkIcon = packageName == "android",
        )
    }.getOrNull()
}

private fun Bitmap.extractPlateColor(fallback: Color): Color {
    return runCatching {
        Palette.from(this)
            .generate()
            .dominantSwatch
            ?.rgb
            ?.let(::Color)
    }.getOrNull() ?: fallback
}

private fun Modifier.drawColoredShadow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    roundedRect: Boolean = true,
): Modifier = drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = color.copy(alpha = 0f).toArgb()
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            color.copy(alpha = alpha).toArgb(),
        )
        canvas.save()
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = if (roundedRect) size.height / 2f else borderRadius.toPx(),
            radiusY = if (roundedRect) size.height / 2f else borderRadius.toPx(),
            paint = paint,
        )
        canvas.restore()
    }
}
