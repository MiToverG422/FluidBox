package com.mi.fluidbox.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mi.fluidbox.ui.common.AppLocale
import com.mi.fluidbox.ui.platform.findActivity
import com.mi.fluidbox.ui.platform.recreateWithoutAnimation

@Composable
fun SettingsLanguageDropdown(
    title: String,
    summary: String,
    hasDividerAbove: Boolean,
    hasDividerBelow: Boolean,
) {
    val context = LocalContext.current
    val languageTags = listOf(
        AppLocale.LANGUAGE_SYSTEM,
        AppLocale.LANGUAGE_EN,
        AppLocale.LANGUAGE_ZH_CN,
        AppLocale.LANGUAGE_ZH_TW,
    )
    val languageLabels = listOf(
        "Follow system",
        "English",
        "简体中文",
        "正體中文",
    )
    val selectedIndex = languageTags
        .indexOf(AppLocale.getSelectedLanguageTag(context))
        .takeIf { it >= 0 }
        ?: 0

    SettingsWindowDropdownPreference(
        items = languageLabels,
        selectedIndex = selectedIndex,
        title = title,
        summary = summary.takeIf { it.isNotBlank() },
        onSelectedIndexChange = { index ->
            val tag = languageTags.getOrNull(index) ?: AppLocale.LANGUAGE_SYSTEM
            AppLocale.setSelectedLanguageTag(context, tag)
            context.findActivity()?.recreateWithoutAnimation()
        },
    )
}
