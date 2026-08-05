package com.mi.fluidbox.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mi.fluidbox.R
import com.mi.fluidbox.ui.common.AppThemeMode

@Composable
fun SettingsThemeModeDropdown(
    title: String,
    selectedMode: AppThemeMode,
    onModeChange: (AppThemeMode) -> Unit,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
) {
    val modes = listOf(
        AppThemeMode.System,
        AppThemeMode.Light,
        AppThemeMode.Dark,
        AppThemeMode.MonetSystem,
        AppThemeMode.MonetLight,
        AppThemeMode.MonetDark,
    )
    val labels = listOf(
        stringResource(R.string.theme_mode_system),
        stringResource(R.string.theme_mode_light),
        stringResource(R.string.theme_mode_dark),
        stringResource(R.string.theme_mode_monet_system),
        stringResource(R.string.theme_mode_monet_light),
        stringResource(R.string.theme_mode_monet_dark),
    )
    val selectedIndex = modes.indexOf(selectedMode).takeIf { it >= 0 } ?: 0

    SettingsWindowDropdownPreference(
        items = labels,
        selectedIndex = selectedIndex,
        title = title,
        insideMargin = SettingsTokens.BasicComponentInsideMargin,
        onSelectedIndexChange = { index ->
            modes.getOrNull(index)?.let(onModeChange)
        },
    )
}
