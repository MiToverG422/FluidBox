package com.mi.fluidbox.ui.settings

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.DropdownArrowEndAction
import io.github.suqi8.coui.kmp.basic.DropdownColors
import io.github.suqi8.coui.kmp.basic.DropdownDefaults
import io.github.suqi8.coui.kmp.basic.DropdownEntry
import io.github.suqi8.coui.kmp.basic.DropdownItem
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.SwitchColors
import io.github.suqi8.coui.kmp.basic.SwitchDefaults
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.popup.WindowDropdownPopup
import io.github.suqi8.coui.kmp.theme.COUITheme

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
    insideMargin: PaddingValues = SettingsTokens.BasicComponentInsideMargin,
    holdDownState: Boolean = false,
    enabled: Boolean = true,
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    BasicComponent(
        modifier = modifier,
        insideMargin = insideMargin,
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
    ) {
        SettingsRowTextContent(
            title = title,
            summary = summary,
            enabled = enabled,
        )
    }
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
    insideMargin: PaddingValues = SettingsTokens.BasicComponentInsideMargin,
    maxHeight: Dp? = null,
    enabled: Boolean = true,
    showValue: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    onSelectedIndexChange: ((Int) -> Unit)? = null,
) {
    val entry = remember(
        items,
        selectedIndex,
        onSelectedIndexChange,
    ) {
        DropdownEntry(
            items.mapIndexed { index, item ->
                DropdownItem(
                    text = item,
                    selected = index == selectedIndex,
                    onClick = { onSelectedIndexChange?.invoke(index) },
                )
            },
        )
    }
    SettingsWindowDropdownPreference(
        entry = entry,
        title = title,
        modifier = modifier,
        summary = summary,
        dropdownColors = dropdownColors,
        startAction = startAction,
        bottomAction = bottomAction,
        insideMargin = insideMargin,
        maxHeight = maxHeight,
        enabled = enabled,
        showValue = showValue,
        collapseOnSelection = true,
        onExpandedChange = onExpandedChange,
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
    insideMargin: PaddingValues = SettingsTokens.BasicComponentInsideMargin,
    maxHeight: Dp? = null,
    enabled: Boolean = true,
    showValue: Boolean = true,
    collapseOnSelection: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDropdownExpanded = remember { mutableStateOf(false) }
    val isHoldDown = remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val currentHapticFeedback by rememberUpdatedState(hapticFeedback)
    val currentOnExpandedChange = rememberUpdatedState(onExpandedChange)
    val setExpanded: (Boolean) -> Unit = remember {
        { expanded ->
            if (isDropdownExpanded.value != expanded) {
                isDropdownExpanded.value = expanded
                currentOnExpandedChange.value?.invoke(expanded)
            }
        }
    }

    val itemsNotEmpty = entry.items.isNotEmpty()
    val actualEnabled = enabled && itemsNotEmpty
    val actionColor = if (actualEnabled) {
        COUITheme.colorScheme.onSurfaceVariantActions
    } else {
        COUITheme.colorScheme.disabledOnSecondaryVariant
    }
    val valueColor = if (actualEnabled) {
        COUITheme.colorScheme.onSurfaceSecondary
    } else {
        COUITheme.colorScheme.disabledOnSecondaryVariant
    }
    val handleClick = remember(actualEnabled) {
        {
            if (actualEnabled) {
                setExpanded(!isDropdownExpanded.value)
                if (isDropdownExpanded.value) {
                    isHoldDown.value = true
                    currentHapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            }
        }
    }

    BasicComponent(
        modifier = modifier,
        interactionSource = interactionSource,
        insideMargin = insideMargin,
        startAction = startAction,
        endActions = {
            if (showValue && itemsNotEmpty) {
                val text = entry.items.firstOrNull { it.selected }?.text
                if (!text.isNullOrEmpty()) {
                    Text(
                        text = text,
                        modifier = Modifier
                            .widthIn(max = 162.dp)
                            .padding(start = 8.dp, end = 4.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1f, fill = false),
                        fontSize = SettingsTokens.RowSummaryFontSize,
                        lineHeight = SettingsTokens.RowSummaryLineHeight,
                        color = valueColor,
                        textAlign = TextAlign.End,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            DropdownArrowEndAction(actionColor = actionColor)
            if (itemsNotEmpty) {
                WindowDropdownPopup(
                    entry = entry,
                    show = isDropdownExpanded.value,
                    onDismiss = { setExpanded(false) },
                    onDismissFinished = { isHoldDown.value = false },
                    maxHeight = maxHeight,
                    dropdownColors = dropdownColors,
                    collapseOnSelection = collapseOnSelection,
                )
            }
        },
        bottomAction = bottomAction,
        onClick = handleClick,
        role = Role.DropdownList,
        holdDownState = isHoldDown.value,
        enabled = actualEnabled,
    ) {
        SettingsRowTextContent(
            title = title,
            summary = summary,
            enabled = actualEnabled,
        )
    }
}
