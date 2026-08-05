package com.mi.fluidbox.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.theme.COUITheme

@Composable
fun SettingsToggleRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
    enabled: Boolean = true,
) {
    SettingsSwitchPreference(
        checked = checked,
        onCheckedChange = onCheckedChange,
        title = title,
        summary = summary.takeIf { it.isNotBlank() },
        startAction = icon?.let { imageVector ->
            {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = COUITheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp),
            )
            }
        },
        insideMargin = SettingsTokens.BasicComponentInsideMargin,
        enabled = enabled,
    )
}
