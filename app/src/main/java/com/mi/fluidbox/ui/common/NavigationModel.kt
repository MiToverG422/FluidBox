package com.mi.fluidbox.ui.common

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.mi.fluidbox.R

data class BottomTab(
    @param:StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screenIndex: Int
)

val bottomTabs = listOf(
    BottomTab(R.string.tab_home, AppIcons.HomeFilled, AppIcons.HomeOutline, screenIndex = 0),
    BottomTab(R.string.tab_features, AppIcons.Widgets, AppIcons.WidgetsOutline, screenIndex = 1),
    BottomTab(R.string.tab_refresh_rate, AppIcons.Refresh, AppIcons.Refresh, screenIndex = 4),
    BottomTab(R.string.section_about, AppIcons.InfoFilled, AppIcons.InfoOutline, screenIndex = 3)
)
