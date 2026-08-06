package com.mi.fluidbox.ui.screens

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.fluidbox.R
import com.mi.fluidbox.ui.common.ShellLogger
import com.mi.fluidbox.ui.common.rememberHapticClick
import com.mi.fluidbox.ui.settings.SettingsGroup
import com.mi.fluidbox.ui.settings.SettingsSection
import com.mi.fluidbox.ui.settings.SettingsDivider
import com.mi.fluidbox.ui.settings.SettingsTokens
import com.mi.fluidbox.ui.settings.SettingsToggleRow
import com.mi.fluidbox.ui.settings.settingsInteractiveRowHighlight
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.ButtonDefaults
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.RadioButton
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextButton
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronForward
import io.github.suqi8.coui.kmp.theme.COUITheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private data class DisplayModeInfo(
    val id: Int,
    val surfaceFlingerModeIndex: Int,
    val width: Int,
    val height: Int,
    val refreshRate: Float,
    val isCurrent: Boolean,
)

private sealed interface RefreshRateAction {
    data class Apply(val mode: DisplayModeInfo) : RefreshRateAction
    data object RestoreDefault : RefreshRateAction
}

private const val REFRESH_RATE_PREFS = "refresh_rate_page"
private const val KEY_SELECTED_MODE_ID = "selected_mode_id"
private const val KEY_AUTO_START_ENABLED = "auto_start_enabled"
private const val KEY_CACHED_DISPLAY_MODES = "cached_display_modes"
private val RefreshRateTableContentPaddingStart = 16.dp
private val RefreshRateTableContentPaddingEnd = 8.dp
private val RefreshRateTableRowMinHeight = 48.dp
private val RefreshRateTableRowVerticalPadding = 6.dp

@Composable
fun RefreshRatePage(
    extremeRefresh165Enabled: Boolean,
    onExtremeRefresh165EnabledChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshKey by remember { mutableIntStateOf(0) }
    val cachedModes = remember(context) { readCachedDisplayModes(context) }
    var modes by remember(context) { mutableStateOf(cachedModes) }
    var modesLoaded by remember(context) { mutableStateOf(cachedModes.isNotEmpty()) }
    LaunchedEffect(context, refreshKey) {
        // SurfaceFlinger's dumpsys can take around one second. Keep it off the
        // composition thread so applying a mode never freezes the page.
        val refreshedModes = withContext(Dispatchers.IO) { readDisplayModes(context) }
        if (refreshedModes.isNotEmpty()) {
            modes = refreshedModes
            saveCachedDisplayModes(context, refreshedModes)
        }
        modesLoaded = true
    }
    var autoStartEnabled by remember(context) {
        mutableStateOf(readAutoStartEnabled(context))
    }
    var showRefreshRate by remember { mutableStateOf(false) }
    LaunchedEffect(context) {
        showRefreshRate = withContext(Dispatchers.IO) {
            readForceRefreshEnabled(context)
        }
    }
    var selectedModeId by remember(context, modes) {
        val savedModeId = readSavedModeId(context)
            ?.takeIf { savedId -> modes.any { mode -> mode.id == savedId } }
        mutableStateOf(savedModeId)
    }
    val selectedMode = modes.firstOrNull { it.id == selectedModeId }
    var applying by remember { mutableStateOf(false) }
    val applyFailedText = stringResource(R.string.refresh_rate_apply_failed)
    val restoreSuccessText = stringResource(R.string.refresh_rate_restore_success)
    val restoreFailedText = stringResource(R.string.refresh_rate_restore_failed)
    var queuedAction by remember { mutableStateOf<RefreshRateAction?>(null) }
    var modesExpanded by remember { mutableStateOf(false) }

    fun enqueueRefreshAction(action: RefreshRateAction) {
        when (action) {
            is RefreshRateAction.Apply -> selectedModeId = action.mode.id
            RefreshRateAction.RestoreDefault -> Unit
        }
        if (applying) {
            // Keep only the user's latest intent while SurfaceFlinger is applying.
            queuedAction = action
            return
        }
        applying = true
        scope.launch {
            var nextAction: RefreshRateAction? = action
            while (nextAction != null) {
                when (nextAction) {
                    is RefreshRateAction.Apply -> {
                        val mode = nextAction.mode
                        val success = withContext(Dispatchers.IO) {
                            applyPreferredMode(context, mode)
                        }
                        if (success) {
                            saveSelectedModeId(context, mode.id)
                        } else {
                            Toast.makeText(context, applyFailedText, Toast.LENGTH_SHORT).show()
                        }
                    }

                    RefreshRateAction.RestoreDefault -> {
                        val success = withContext(Dispatchers.IO) { clearPreferredMode(context) }
                        Toast.makeText(
                            context,
                            if (success) restoreSuccessText else restoreFailedText,
                            Toast.LENGTH_SHORT,
                        ).show()
                        selectedModeId = null
                        clearSavedModeId(context)
                    }
                }
                refreshKey++
                nextAction = queuedAction
                queuedAction = null
            }
            applying = false
        }
    }

    if (modes.isEmpty() && modesLoaded) {
        SettingsGroup {
            Text(
                text = stringResource(R.string.refresh_rate_no_modes),
                style = COUITheme.textStyles.body1,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        return
    }

    if (modes.isEmpty()) return

    SettingsGroup {
        RefreshRateModeHeader(
            expanded = modesExpanded,
            hasDividerBelow = modesExpanded,
            onClick = { modesExpanded = !modesExpanded },
        )
        AnimatedVisibility(
            visible = modesExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                expandFrom = Alignment.Top,
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Top,
            ),
        ) {
            Column {
                SettingsDivider()
                modes.forEachIndexed { index, mode ->
                    if (index > 0) SettingsDivider()
                    RefreshRateModeRow(
                        mode = mode,
                        selected = mode.id == selectedModeId,
                        hasDividerAbove = true,
                        hasDividerBelow = index < modes.lastIndex,
                        onClick = {
                            enqueueRefreshAction(RefreshRateAction.Apply(mode))
                        },
                    )
                }
            }
        }
    }

    TextButton(
        text = stringResource(R.string.refresh_rate_restore_default),
        onClick = {
            enqueueRefreshAction(RefreshRateAction.RestoreDefault)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        minHeight = 42.dp,
        colors = ButtonDefaults.textButtonColorsPrimary(),
    )

    SettingsSection(title = stringResource(R.string.feature_group_refresh_rate))
    SettingsGroup {
        RefreshRateToggleRow(
            title = stringResource(R.string.refresh_rate_auto_start),
            checked = autoStartEnabled,
            hasDividerBelow = true,
            onCheckedChange = { enabled ->
                autoStartEnabled = enabled
                setAutoStartEnabled(context, enabled)
            },
        )
        SettingsDivider()
        RefreshRateToggleRow(
            title = stringResource(R.string.refresh_rate_show_refresh_rate),
            checked = showRefreshRate,
            hasDividerAbove = true,
            onCheckedChange = { enabled ->
                showRefreshRate = enabled
                scope.launch {
                    if (!withContext(Dispatchers.IO) { setForceRefreshEnabled(context, enabled) }) {
                        showRefreshRate = !enabled
                        Toast.makeText(context, applyFailedText, Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
    }

    SettingsSection(title = stringResource(R.string.section_experimental))
    SettingsGroup {
        RefreshRateToggleRow(
            title = stringResource(R.string.feature_extreme_refresh_165_title),
            summary = stringResource(R.string.feature_extreme_refresh_165_summary),
            checked = extremeRefresh165Enabled,
            onCheckedChange = onExtremeRefresh165EnabledChange,
        )
    }

}

@Composable
private fun RefreshRateModeHeader(
    expanded: Boolean,
    hasDividerBelow: Boolean,
    onClick: () -> Unit,
) {
    val hapticClick = rememberHapticClick()
    BasicComponent(
        insideMargin = PaddingValues(
            start = RefreshRateTableContentPaddingStart,
            end = RefreshRateTableContentPaddingEnd,
        ),
        endActions = {
            RefreshRateExpandArrow(expanded = expanded)
        },
        onClick = {
            hapticClick()
            onClick()
        },
        role = Role.Button,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RefreshRateHeaderText(
                text = stringResource(R.string.refresh_rate_table_id),
                modifier = Modifier.width(42.dp),
            )
            RefreshRateHeaderText(
                text = stringResource(R.string.refresh_rate_table_resolution),
                modifier = Modifier.width(124.dp),
            )
            RefreshRateHeaderText(
                text = stringResource(R.string.refresh_rate_table_refresh_rate),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RefreshRateExpandArrow(
    expanded: Boolean,
) {
    val rotationZ by animateFloatAsState(
        targetValue = if (expanded) -90f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "refreshRateExpandArrowRotation",
    )
    Icon(
        imageVector = COUIIcons.ChevronForward,
        contentDescription = null,
        tint = COUITheme.colorScheme.onSurfaceVariantActions,
        modifier = Modifier
            .size(width = 12.dp, height = 24.dp)
            .graphicsLayer(rotationZ = rotationZ),
    )
}

@Composable
private fun RefreshRateHeaderText(
    text: String,
    modifier: Modifier,
) {
    Text(
        text = text,
        fontSize = COUITheme.textStyles.headline1.fontSize,
        color = COUITheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RefreshRateModeRow(
    mode: DisplayModeInfo,
    selected: Boolean,
    hasDividerAbove: Boolean,
    hasDividerBelow: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticClick = rememberHapticClick()
    val rowHighlightColor = COUITheme.colorScheme.onSurface.copy(alpha = 0.08f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .settingsInteractiveRowHighlight(
                interactionSource = interactionSource,
                color = rowHighlightColor,
                hasDividerAbove = hasDividerAbove,
                hasDividerBelow = hasDividerBelow,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = {
                    hapticClick()
                    onClick()
                },
            )
            .heightIn(min = RefreshRateTableRowMinHeight)
            .padding(
                start = RefreshRateTableContentPaddingStart,
                end = RefreshRateTableContentPaddingEnd,
                top = RefreshRateTableRowVerticalPadding,
                bottom = RefreshRateTableRowVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mode.surfaceFlingerModeIndex.toString(),
            fontSize = COUITheme.textStyles.headline1.fontSize,
            color = COUITheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(42.dp),
            maxLines = 1,
        )
        Text(
            text = stringResource(R.string.refresh_rate_resolution_format, mode.width, mode.height),
            fontSize = COUITheme.textStyles.headline1.fontSize,
            color = COUITheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(124.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = formatRefreshRate(mode.refreshRate),
            fontSize = COUITheme.textStyles.headline1.fontSize,
            color = COUITheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        RadioButton(
            selected = selected,
            onClick = null,
        )
    }
}

@Composable
private fun RefreshRateToggleRow(
    title: String,
    summary: String? = null,
    checked: Boolean,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsToggleRow(
        title = title,
        summary = summary.orEmpty(),
        checked = checked,
        onCheckedChange = onCheckedChange,
        hasDividerAbove = hasDividerAbove,
        hasDividerBelow = hasDividerBelow,
    )
}

private fun readDisplayModes(context: Context): List<DisplayModeInfo> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return emptyList()
    val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY) ?: return emptyList()
    val modes = readSurfaceFlingerDisplayModes(context, display)
        .ifEmpty { readHiddenDisplayModes(display) ?: readPublicDisplayModes(display) }
    // Transaction 1035 uses this complete native mode list. Do not filter,
    // deduplicate, or reorder it, otherwise the configuration index changes.
    return modes
}

private fun readCachedDisplayModes(context: Context): List<DisplayModeInfo> {
    val serialized = context
        .getSharedPreferences(REFRESH_RATE_PREFS, Context.MODE_PRIVATE)
        .getString(KEY_CACHED_DISPLAY_MODES, null)
        .orEmpty()
    return serialized
        .split(';')
        .mapNotNull { entry ->
            val values = entry.split('|')
            if (values.size != 5) return@mapNotNull null
            runCatching {
                DisplayModeInfo(
                    id = values[0].toInt(),
                    surfaceFlingerModeIndex = values[1].toInt(),
                    width = values[2].toInt(),
                    height = values[3].toInt(),
                    refreshRate = values[4].toFloat(),
                    isCurrent = false,
                )
            }.getOrNull()
        }
}

private fun saveCachedDisplayModes(context: Context, modes: List<DisplayModeInfo>) {
    val serialized = modes.joinToString(separator = ";") { mode ->
        listOf(
            mode.id,
            mode.surfaceFlingerModeIndex,
            mode.width,
            mode.height,
            mode.refreshRate,
        ).joinToString(separator = "|")
    }
    context
        .getSharedPreferences(REFRESH_RATE_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_CACHED_DISPLAY_MODES, serialized)
        .apply()
}

private fun readSurfaceFlingerDisplayModes(
    context: Context,
    display: Display,
): List<DisplayModeInfo> {
    val currentMode = display.mode
    val output = ShellLogger.exec(
        "RefreshRate list modes",
        surfaceFlingerListCommand(context),
    ).out
    val modePattern = Regex(
        """id=(-?\d+), config=(-?\d+), (\d+)x(\d+)@([0-9.]+)""",
    )
    return output.mapNotNull { line ->
        val match = modePattern.find(line) ?: return@mapNotNull null
        val (id, config, width, height, refreshRate) = match.destructured
        DisplayModeInfo(
            id = id.toInt(),
            surfaceFlingerModeIndex = config.toInt(),
            width = width.toInt(),
            height = height.toInt(),
            refreshRate = refreshRate.toFloat(),
            isCurrent = width.toInt() == currentMode?.physicalWidth &&
                height.toInt() == currentMode?.physicalHeight &&
                abs(refreshRate.toFloat() - (currentMode?.refreshRate ?: 0f)) < 0.01f,
        )
    }
}

private fun readHiddenDisplayModes(display: Display): List<DisplayModeInfo>? {
    val currentMode = display.mode
    val currentModeId = currentMode?.modeId
    return runCatching {
        val displayInfoClass = Class.forName("android.view.DisplayInfo")
        val displayInfo = displayInfoClass.getDeclaredConstructor().newInstance()
        val getDisplayInfo = Display::class.java.getDeclaredMethod("getDisplayInfo", displayInfoClass)
        getDisplayInfo.isAccessible = true
        val hasInfo = getDisplayInfo.invoke(display, displayInfo) as? Boolean ?: false
        if (!hasInfo) return@runCatching null

        val supportedModes = listOf("supportedDisplayModes", "supportedModes")
            .firstNotNullOfOrNull { fieldName ->
                runCatching {
                    displayInfoClass.getDeclaredField(fieldName).apply { isAccessible = true }.get(displayInfo)
                }.getOrNull()
            }
            ?.let { modes ->
                when (modes) {
                    is Array<*> -> modes.filterNotNull()
                    else -> emptyList()
                }
            }
            .orEmpty()

        val displayModes = supportedModes
            .mapIndexedNotNull { index, mode ->
                val id = readIntValue(mode, "id", "modeId", "mModeId")
                    ?: callIntValue(mode, "getModeId")
                    ?: return@mapIndexedNotNull null
                val width = readIntValue(mode, "width", "physicalWidth", "mWidth")
                    ?: callIntValue(mode, "getPhysicalWidth")
                    ?: return@mapIndexedNotNull null
                val height = readIntValue(mode, "height", "physicalHeight", "mHeight")
                    ?: callIntValue(mode, "getPhysicalHeight")
                    ?: return@mapIndexedNotNull null
                val refreshRate = readFloatValue(mode, "refreshRate", "mRefreshRate")
                    ?: callFloatValue(mode, "getRefreshRate")
                    ?: return@mapIndexedNotNull null
                DisplayModeInfo(
                    id = id,
                    surfaceFlingerModeIndex = index,
                    width = width,
                    height = height,
                    refreshRate = refreshRate,
                    isCurrent = id == currentModeId ||
                        (width == currentMode?.physicalWidth &&
                            height == currentMode.physicalHeight &&
                            abs(refreshRate - currentMode.refreshRate) < 0.01f),
                )
            }

        displayModes.takeIf { it.isNotEmpty() }
    }.getOrNull()
}

private fun readPublicDisplayModes(display: Display): List<DisplayModeInfo> {
    val currentModeId = display.mode?.modeId
    return display.supportedModes
        .mapIndexed { index, mode ->
            DisplayModeInfo(
                id = mode.modeId,
                surfaceFlingerModeIndex = index,
                width = mode.physicalWidth,
                height = mode.physicalHeight,
                refreshRate = mode.refreshRate,
                isCurrent = mode.modeId == currentModeId,
            )
        }
}

private fun readIntValue(instance: Any, vararg names: String): Int? {
    return names.firstNotNullOfOrNull { name ->
        runCatching {
            instance.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(instance) as? Int
        }.getOrNull()
    }
}

private fun readFloatValue(instance: Any, vararg names: String): Float? {
    return names.firstNotNullOfOrNull { name ->
        runCatching {
            when (val value = instance.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(instance)) {
                is Float -> value
                is Double -> value.toFloat()
                is Number -> value.toFloat()
                else -> null
            }
        }.getOrNull()
    }
}

private fun callIntValue(instance: Any, name: String): Int? {
    return runCatching {
        instance.javaClass.getDeclaredMethod(name).apply { isAccessible = true }.invoke(instance) as? Int
    }.getOrNull()
}

private fun callFloatValue(instance: Any, name: String): Float? {
    return runCatching {
        when (val value = instance.javaClass.getDeclaredMethod(name).apply { isAccessible = true }.invoke(instance)) {
            is Float -> value
            is Double -> value.toFloat()
            is Number -> value.toFloat()
            else -> null
        }
    }.getOrNull()
}

private fun readSavedModeId(context: Context): Int? {
    val prefs = context.getSharedPreferences(REFRESH_RATE_PREFS, Context.MODE_PRIVATE)
    return prefs
        .getInt(KEY_SELECTED_MODE_ID, -1)
        .takeIf { it >= 0 }
}

private fun saveSelectedModeId(context: Context, modeId: Int) {
    context
        .getSharedPreferences(REFRESH_RATE_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_SELECTED_MODE_ID, modeId)
        .apply()
}

private fun clearSavedModeId(context: Context) {
    context
        .getSharedPreferences(REFRESH_RATE_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_SELECTED_MODE_ID, -1)
        .apply()
}

private fun applyPreferredMode(context: Context, mode: DisplayModeInfo): Boolean {
    return ShellLogger.exec(
        "RefreshRate apply ${formatRefreshRate(mode.refreshRate)}",
        surfaceFlingerModeCommand(context, mode.surfaceFlingerModeIndex),
    ).isSuccess
}

private fun clearPreferredMode(context: Context): Boolean {
    return ShellLogger.exec(
        "RefreshRate clear",
        surfaceFlingerResetCommand(context),
    ).isSuccess
}

private fun readAutoStartEnabled(context: Context): Boolean = context
    .getSharedPreferences(REFRESH_RATE_PREFS, Context.MODE_PRIVATE)
    .getBoolean(KEY_AUTO_START_ENABLED, false)

private fun setAutoStartEnabled(context: Context, enabled: Boolean) {
    context
        .getSharedPreferences(REFRESH_RATE_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_AUTO_START_ENABLED, enabled)
        .apply()
}

private fun readForceRefreshEnabled(context: Context): Boolean {
    val result = ShellLogger.exec(
        "RefreshRate force status",
        surfaceFlingerForceStatusCommand(context),
    )
    return result.isSuccess && result.out.lastOrNull()?.trim() == "1"
}

private fun setForceRefreshEnabled(context: Context, enabled: Boolean): Boolean {
    val result = ShellLogger.exec(
        "RefreshRate force enabled=$enabled",
        surfaceFlingerForceEnabledCommand(context, enabled),
    )
    return result.isSuccess
}

private fun surfaceFlingerModeCommand(context: Context, modeIndex: Int): String {
    val apkPath = context.applicationInfo.sourceDir
    return "CLASSPATH=${shellQuote(apkPath)} app_process /system/bin com.mi.fluidbox.tools.SurfaceFlingerModeTool set $modeIndex"
}

private fun surfaceFlingerForceEnabledCommand(context: Context, enabled: Boolean): String {
    val apkPath = context.applicationInfo.sourceDir
    val value = if (enabled) 1 else 0
    return "CLASSPATH=${shellQuote(apkPath)} app_process /system/bin com.mi.fluidbox.tools.SurfaceFlingerModeTool force $value"
}

private fun surfaceFlingerForceStatusCommand(context: Context): String {
    val apkPath = context.applicationInfo.sourceDir
    return "CLASSPATH=${shellQuote(apkPath)} app_process /system/bin com.mi.fluidbox.tools.SurfaceFlingerModeTool force-status"
}

private fun surfaceFlingerListCommand(context: Context): String {
    val apkPath = context.applicationInfo.sourceDir
    return "CLASSPATH=${shellQuote(apkPath)} app_process /system/bin com.mi.fluidbox.tools.SurfaceFlingerModeTool list"
}

private fun surfaceFlingerResetCommand(context: Context): String {
    val apkPath = context.applicationInfo.sourceDir
    return "CLASSPATH=${shellQuote(apkPath)} app_process /system/bin com.mi.fluidbox.tools.SurfaceFlingerModeTool reset"
}

private fun shellQuote(value: String): String {
    return "'${value.replace("'", "'\"'\"'")}'"
}

private fun formatRefreshRate(refreshRate: Float): String {
    val rounded = refreshRate.roundToInt()
    val text = if (abs(refreshRate - rounded) < 0.01f) {
        rounded.toString()
    } else {
        String.format(Locale.US, "%.2f", refreshRate).trimEnd('0').trimEnd('.')
    }
    return "$text Hz"
}
