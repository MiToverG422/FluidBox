package com.mi.fluidbox.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.DropdownColors
import io.github.suqi8.coui.kmp.basic.DropdownDefaults
import io.github.suqi8.coui.kmp.basic.DropdownEntry
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.SwitchColors
import io.github.suqi8.coui.kmp.basic.SwitchDefaults
import io.github.suqi8.coui.kmp.preference.WindowDropdownPreference as CouiWindowDropdownPreference

@Composable
internal fun SettingsSwitchPreference(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    startAction: @Composable (() -> Unit)? = null,
    endActions: @Composable RowScope.() -> Unit = {},
    bottomAction: (@Composable () -> Unit)? = null,
    switchColors: SwitchColors = SwitchDefaults.switchColors(),
    holdDownState: Boolean = false,
    enabled: Boolean = true,
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    BasicComponent(
        modifier = modifier,
        title = title,
        summary = summary,
        startAction = startAction,
        endActions = {
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically)
                    .weight(1f, fill = false),
            ) {
                endActions()
            }
            Switch(
                checked = checked,
                onCheckedChange = currentOnCheckedChange.takeIf { enabled },
                enabled = enabled,
                colors = switchColors,
            )
        },
        bottomAction = bottomAction,
        onClick = {
            currentOnCheckedChange.takeIf { enabled }?.invoke(!checked)
        },
        role = Role.Switch,
        holdDownState = holdDownState,
        enabled = enabled,
    )
}

@Composable
internal fun SettingsWindowDropdownPreference(
    items: List<String>,
    selectedIndex: Int,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    startAction: @Composable (() -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    maxHeight: Dp? = null,
    enabled: Boolean = true,
    showValue: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    onSelectedIndexChange: ((Int) -> Unit)? = null,
) {
    CouiWindowDropdownPreference(
        items = items,
        selectedIndex = selectedIndex,
        title = title,
        modifier = modifier,
        summary = summary,
        dropdownColors = dropdownColors,
        startAction = startAction,
        bottomAction = bottomAction,
        maxHeight = maxHeight,
        enabled = enabled,
        showValue = showValue,
        onExpandedChange = onExpandedChange,
        onSelectedIndexChange = onSelectedIndexChange,
    )
}

@Composable
internal fun SettingsWindowDropdownPreference(
    entry: DropdownEntry,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    startAction: @Composable (() -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    maxHeight: Dp? = null,
    enabled: Boolean = true,
    showValue: Boolean = true,
    collapseOnSelection: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    CouiWindowDropdownPreference(
        entry = entry,
        title = title,
        modifier = modifier,
        summary = summary,
        dropdownColors = dropdownColors,
        startAction = startAction,
        bottomAction = bottomAction,
        maxHeight = maxHeight,
        enabled = enabled,
        showValue = showValue,
        collapseOnSelection = collapseOnSelection,
        onExpandedChange = onExpandedChange,
    )
}
