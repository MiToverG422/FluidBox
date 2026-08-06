package com.mi.fluidbox.ui.common

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object AppLocale {
    const val LANGUAGE_SYSTEM = ""
    const val LANGUAGE_EN = "en"
    const val LANGUAGE_ZH_CN = "zh-CN"
    const val LANGUAGE_ZH_TW = "zh-TW"

    private const val PREFS_NAME = "fluidbox_prefs"
    private const val KEY_APP_LANGUAGE = "app_language"

    fun getSelectedLanguageTag(context: Context): String {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_LANGUAGE, LANGUAGE_SYSTEM)
            .orEmpty()
            .normalizeLanguageTag()
            .takeIf { it in supportedLanguageTags }
            ?: LANGUAGE_SYSTEM
    }

    fun setSelectedLanguageTag(context: Context, languageTag: String) {
        val normalizedTag = languageTag
            .normalizeLanguageTag()
            .takeIf { it in supportedLanguageTags }
            ?: LANGUAGE_SYSTEM
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_LANGUAGE, normalizedTag)
            .commit()
    }

    fun wrapContext(context: Context): Context {
        return wrapContext(context, getSelectedLanguageTag(context))
    }

    fun wrapContext(context: Context, languageTag: String): Context {
        if (languageTag.isBlank()) return context

        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList(locale))
        return context.createConfigurationContext(configuration)
    }

    private val supportedLanguageTags = setOf(
        LANGUAGE_SYSTEM,
        LANGUAGE_EN,
        LANGUAGE_ZH_CN,
        LANGUAGE_ZH_TW,
    )

    private fun String.normalizeLanguageTag(): String {
        return when (this) {
            "zh-HK", "zh-MO", "yue-Hant" -> LANGUAGE_ZH_TW
            else -> this
        }
    }
}
