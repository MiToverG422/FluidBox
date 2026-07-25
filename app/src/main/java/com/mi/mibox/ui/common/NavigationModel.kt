package com.mi.mibox.ui.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.mi.mibox.R

data class BottomTab(
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
    val screenIndex: Int
)

val bottomTabs = listOf(
    BottomTab(R.string.tab_features, AppIcons.Widgets, screenIndex = 1),
    BottomTab(R.string.tab_home, Icons.Rounded.Home, screenIndex = 0),
    BottomTab(R.string.tab_battery, AppIcons.Battery, screenIndex = 4),
    BottomTab(R.string.tab_events, AppIcons.Event, screenIndex = 2),
    BottomTab(R.string.tab_settings, Icons.Rounded.Settings, screenIndex = 3)
)
