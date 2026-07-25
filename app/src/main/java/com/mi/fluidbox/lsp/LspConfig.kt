package com.mi.fluidbox.lsp

import android.content.Context
import android.content.SharedPreferences
import com.topjohnwu.superuser.Shell
import de.robv.android.xposed.XSharedPreferences
import java.io.File

object LspConfig {
    private const val MODULE_PACKAGE = "com.mi.fluidbox"
    private const val PREFS_NAME = "lsp_features"
    private const val KEY_NATIVE_NOTIFY_ICON = "native_notify_icon_enabled"
    private const val KEY_EXTREME_REFRESH_165 = "extreme_refresh_165_enabled"
    private const val KEY_RECENT_TASK_RADIUS = "recent_task_radius_enabled"
    private const val KEY_AOD_ENHANCE = "aod_enhance_enabled"
    private const val KEY_OOS_LOCALIZER = "oos_localizer_enabled"
    private const val KEY_DOUBLE_POWER_CUSTOM = "double_power_custom_enabled"
    private const val KEY_DOUBLE_POWER_TARGET_PACKAGE = "double_power_target_package"
    private const val KEY_DOUBLE_POWER_TARGET_ACTIVITY = "double_power_target_activity"
    private const val KEY_ASSISTANT_POWER_MODE = "assistant_power_mode"
    private const val KEY_ASSISTANT_GESTURE_CIRCLE = "assistant_gesture_circle_enabled"
    private const val KEY_RECENT_TASK_RADIUS_DP = "recent_task_radius_dp"
    private const val KEY_AOD_INIT_DARK_BRIGHTNESS = "aod_init_dark_brightness"
    private const val KEY_AOD_INIT_BRIGHT_BRIGHTNESS = "aod_init_bright_brightness"
    private const val KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = "aod_running_brightness_multiplier"
    private const val KEY_AOD_PANORAMIC_SUPPORT = "aod_panoramic_support"
    private const val KEY_AOD_SETTINGS_SWITCH = "aod_settings_switch"
    private const val KEY_AOD_SINGLE_CLICK_BLOCK = "aod_single_click_block"
    private const val KEY_NOTIFICATION_BUBBLE_BLUR = "notification_bubble_blur"
    private const val KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX = "notification_bubble_blur_radius_px"
    private const val KEY_NATIVE_NOTIFICATION_BUBBLES = "native_notification_bubbles"
    private const val KEY_LAUNCHER_REGION_MODE = "launcher_region_mode"
    private const val FLAG_FILE_PATH_NATIVE_NOTIFY_ICON = "/data/local/oost_native_notify_icon.flag"
    private const val FLAG_FILE_PATH_EXTREME_REFRESH_165 = "/data/local/oost_extreme_refresh_165.flag"
    private const val FLAG_FILE_PATH_RECENT_TASK_RADIUS = "/data/local/oost_recent_task_radius.flag"
    private const val FLAG_FILE_PATH_AOD_ENHANCE = "/data/local/oost_aod_enhance.flag"
    private const val FLAG_FILE_PATH_OOS_LOCALIZER = "/data/local/oost_oos_localizer.flag"
    private const val FLAG_FILE_PATH_DOUBLE_POWER_CUSTOM = "/data/local/oost_double_power_custom.flag"
    private const val FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES = "/data/local/oost_native_notification_bubbles.flag"
    private const val TEXT_FILE_PATH_DOUBLE_POWER_TARGET_PACKAGE = "/data/local/oost_double_power_target_package.txt"
    private const val TEXT_FILE_PATH_DOUBLE_POWER_TARGET_ACTIVITY = "/data/local/oost_double_power_target_activity.txt"
    private const val LEGACY_FLAG_FILE_PATH_NATIVE_NOTIFY_ICON = "/data/local/tmp/oost_native_notify_icon.flag"
    private const val LEGACY_FLAG_FILE_PATH_EXTREME_REFRESH_165 = "/data/local/tmp/oost_extreme_refresh_165.flag"
    private const val LEGACY_FLAG_FILE_PATH_RECENT_TASK_RADIUS = "/data/local/tmp/oost_recent_task_radius.flag"
    private const val LEGACY_FLAG_FILE_PATH_AOD_ENHANCE = "/data/local/tmp/oost_aod_enhance.flag"
    private const val LEGACY_FLAG_FILE_PATH_OOS_LOCALIZER = "/data/local/tmp/oost_oos_localizer.flag"
    private const val LEGACY_FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES = "/data/local/tmp/oost_native_notification_bubbles.flag"
    private const val PROP_KEY_NATIVE_NOTIFY_ICON = "oost.native_notify_icon"
    private const val PROP_KEY_EXTREME_REFRESH_165 = "oost.extreme_refresh_165"
    private const val PROP_KEY_RECENT_TASK_RADIUS = "oost.recent_task_radius"
    private const val PROP_KEY_AOD_ENHANCE = "oost.aod_enhance"
    private const val PROP_KEY_OOS_LOCALIZER = "oost.oos_localizer"
    private const val PROP_KEY_DOUBLE_POWER_CUSTOM = "oost.double_power_custom"
    private const val PROP_KEY_DOUBLE_POWER_TARGET_PACKAGE = "oost.double_power_target_package"
    private const val PROP_KEY_DOUBLE_POWER_TARGET_ACTIVITY = "oost.double_power_target_activity"
    private const val PROP_KEY_ASSISTANT_POWER_MODE = "oost.assistant_power_mode"
    private const val PROP_KEY_ASSISTANT_GESTURE_CIRCLE = "oost.assistant_gesture_circle"
    private const val PROP_KEY_RECENT_TASK_RADIUS_DP = "oost.recent_task_radius_dp"
    private const val PROP_KEY_AOD_INIT_DARK_BRIGHTNESS = "oost.aod_init_dark_brightness"
    private const val PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS = "oost.aod_init_bright_brightness"
    private const val PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = "oost.aod_running_brightness_multiplier"
    private const val PROP_KEY_AOD_PANORAMIC_SUPPORT = "oost.aod_panoramic_support"
    private const val PROP_KEY_AOD_SETTINGS_SWITCH = "oost.aod_settings_switch"
    private const val PROP_KEY_AOD_SINGLE_CLICK_BLOCK = "oost.aod_single_click_block"
    private const val PROP_KEY_NOTIFICATION_BUBBLE_BLUR = "oost.notification_bubble_blur"
    private const val PROP_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX = "oost.notification_bubble_blur_radius_px"
    private const val PROP_KEY_NATIVE_NOTIFICATION_BUBBLES = "oost.native_notification_bubbles"
    private const val PROP_KEY_LAUNCHER_REGION_MODE = "oost.launcher_region_mode"
    private const val PERSIST_PROP_KEY_NATIVE_NOTIFY_ICON = "persist.sys.oost.native_notify_icon"
    private const val PERSIST_PROP_KEY_EXTREME_REFRESH_165 = "persist.sys.oost.extreme_refresh_165"
    private const val PERSIST_PROP_KEY_RECENT_TASK_RADIUS = "persist.sys.oost.recent_task_radius"
    private const val PERSIST_PROP_KEY_AOD_ENHANCE = "persist.sys.oost.aod_enhance"
    private const val PERSIST_PROP_KEY_OOS_LOCALIZER = "persist.sys.oost.oos_localizer"
    private const val PERSIST_PROP_KEY_DOUBLE_POWER_CUSTOM = "persist.sys.oost.double_power_custom"
    private const val PERSIST_PROP_KEY_DOUBLE_POWER_TARGET_PACKAGE = "persist.sys.oost.double_power_target_package"
    private const val PERSIST_PROP_KEY_DOUBLE_POWER_TARGET_ACTIVITY = "persist.sys.oost.double_power_target_activity"
    private const val PERSIST_PROP_KEY_ASSISTANT_POWER_MODE = "persist.sys.oost.assistant_power_mode"
    private const val PERSIST_PROP_KEY_ASSISTANT_GESTURE_CIRCLE = "persist.sys.oost.assistant_gesture_circle"
    private const val PERSIST_PROP_KEY_RECENT_TASK_RADIUS_DP = "persist.sys.oost.recent_task_radius_dp"
    private const val PERSIST_PROP_KEY_AOD_INIT_DARK_BRIGHTNESS = "persist.sys.oost.aod_init_dark_brightness"
    private const val PERSIST_PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS = "persist.sys.oost.aod_init_bright_brightness"
    private const val PERSIST_PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = "persist.sys.oost.aod_running_brightness_multiplier"
    private const val PERSIST_PROP_KEY_AOD_PANORAMIC_SUPPORT = "persist.sys.oost.aod_panoramic_support"
    private const val PERSIST_PROP_KEY_AOD_SETTINGS_SWITCH = "persist.sys.oost.aod_settings_switch"
    private const val PERSIST_PROP_KEY_AOD_SINGLE_CLICK_BLOCK = "persist.sys.oost.aod_single_click_block"
    private const val PERSIST_PROP_KEY_NOTIFICATION_BUBBLE_BLUR = "persist.sys.oost.notification_bubble_blur"
    private const val PERSIST_PROP_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX = "persist.sys.oost.notification_bubble_blur_radius_px"
    private const val PERSIST_PROP_KEY_NATIVE_NOTIFICATION_BUBBLES = "persist.sys.oost.native_notification_bubbles"
    private const val PERSIST_PROP_KEY_LAUNCHER_REGION_MODE = "persist.sys.oost.launcher_region_mode"
    private const val SETTINGS_KEY_NATIVE_NOTIFY_ICON = "oost_native_notify_icon"
    private const val SETTINGS_KEY_EXTREME_REFRESH_165 = "oost_extreme_refresh_165"
    private const val SETTINGS_KEY_RECENT_TASK_RADIUS = "oost_recent_task_radius"
    private const val SETTINGS_KEY_AOD_ENHANCE = "oost_aod_enhance"
    private const val SETTINGS_KEY_OOS_LOCALIZER = "oost_oos_localizer"
    private const val SETTINGS_KEY_DOUBLE_POWER_CUSTOM = "oost_double_power_custom"
    private const val SETTINGS_KEY_DOUBLE_POWER_TARGET_PACKAGE = "oost_double_power_target_package"
    private const val SETTINGS_KEY_DOUBLE_POWER_TARGET_ACTIVITY = "oost_double_power_target_activity"
    private const val SETTINGS_KEY_ASSISTANT_POWER_MODE = "oost_assistant_power_mode"
    private const val SETTINGS_KEY_ASSISTANT_GESTURE_CIRCLE = "oost_assistant_gesture_circle"
    private const val SETTINGS_KEY_POWER_DW_QUICK_INFO = "power_dw_quick_info"
    private const val SETTINGS_KEY_POWER_DW_ALLOW_SHOW_PKG = "power_dw_allow_show_pkg"
    private const val SETTINGS_KEY_POWER_DW_ALLOW_SETTING_PKG = "power_dw_allow_setting_pkg"
    private const val SETTINGS_KEY_RECENT_TASK_RADIUS_DP = "oost_recent_task_radius_dp"
    private const val SETTINGS_KEY_AOD_INIT_DARK_BRIGHTNESS = "oost_aod_init_dark_brightness"
    private const val SETTINGS_KEY_AOD_INIT_BRIGHT_BRIGHTNESS = "oost_aod_init_bright_brightness"
    private const val SETTINGS_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = "oost_aod_running_brightness_multiplier"
    private const val SETTINGS_KEY_AOD_PANORAMIC_SUPPORT = "oost_aod_panoramic_support"
    private const val SETTINGS_KEY_AOD_SETTINGS_SWITCH = "oost_aod_settings_switch"
    private const val SETTINGS_KEY_AOD_SINGLE_CLICK_BLOCK = "oost_aod_single_click_block"
    private const val SETTINGS_KEY_NOTIFICATION_BUBBLE_BLUR = "oost_notification_bubble_blur"
    private const val SETTINGS_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX = "oost_notification_bubble_blur_radius_px"
    private const val SETTINGS_KEY_NATIVE_NOTIFICATION_BUBBLES = "oost_native_notification_bubbles"
    private const val SETTINGS_KEY_LAUNCHER_REGION_MODE = "oost_launcher_region_mode"

    private const val DEFAULT_RECENT_TASK_RADIUS_DP = 26
    private const val DEFAULT_AOD_INIT_DARK_BRIGHTNESS = 80
    private const val DEFAULT_AOD_INIT_BRIGHT_BRIGHTNESS = 160
    private const val DEFAULT_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = 1.6f
    private const val DEFAULT_AOD_PANORAMIC_SUPPORT = true
    private const val DEFAULT_AOD_SETTINGS_SWITCH = true
    private const val DEFAULT_AOD_SINGLE_CLICK_BLOCK = true
    private const val DEFAULT_NOTIFICATION_BUBBLE_BLUR = false
    private const val DEFAULT_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX = 400
    private const val MIN_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX = 0
    private const val MAX_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX = 800
    private const val DEFAULT_NATIVE_NOTIFICATION_BUBBLES = false
    const val LAUNCHER_REGION_MODE_OFF = 0
    const val LAUNCHER_REGION_MODE_CN = 1
    const val LAUNCHER_REGION_MODE_IN = 2
    private const val DEFAULT_LAUNCHER_REGION_MODE = LAUNCHER_REGION_MODE_OFF
    private const val DEFAULT_DOUBLE_POWER_TARGET_PACKAGE = ""
    private const val DEFAULT_DOUBLE_POWER_TARGET_ACTIVITY = ""
    const val ASSISTANT_POWER_MODE_NONE = -1
    const val ASSISTANT_POWER_MODE_GEMINI = 0
    private const val DEFAULT_ASSISTANT_POWER_MODE = ASSISTANT_POWER_MODE_NONE

    data class UiSnapshot(
        val nativeNotifyIconEnabled: Boolean,
        val notificationBubbleBlurEnabled: Boolean,
        val notificationBubbleBlurRadiusPx: Int,
        val nativeNotificationBubblesEnabled: Boolean,
        val extremeRefresh165Enabled: Boolean,
        val launcherRegionMode: Int,
        val recentTaskRadiusEnabled: Boolean,
        val recentTaskRadiusDp: Int,
        val aodEnhanceEnabled: Boolean,
        val aodInitDarkBrightness: Int,
        val aodInitBrightBrightness: Int,
        val aodRunningBrightnessMultiplier: Float,
        val aodPanoramicSupportEnabled: Boolean,
        val aodSettingsSwitchEnabled: Boolean,
        val aodSingleClickBlockEnabled: Boolean,
        val oosLocalizerEnabled: Boolean,
        val doublePowerCustomEnabled: Boolean,
        val doublePowerTargetPackage: String,
        val doublePowerTargetActivity: String,
        val assistantPowerMode: Int,
        val assistantGestureCircleEnabled: Boolean
    )

    fun readCachedUiSnapshot(context: Context): UiSnapshot {
        val prefs = prefs(context)
        return UiSnapshot(
            nativeNotifyIconEnabled = prefs.getBoolean(KEY_NATIVE_NOTIFY_ICON, true),
            notificationBubbleBlurEnabled = prefs.getBoolean(
                KEY_NOTIFICATION_BUBBLE_BLUR,
                DEFAULT_NOTIFICATION_BUBBLE_BLUR
            ),
            notificationBubbleBlurRadiusPx = prefs.getInt(
                KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
                DEFAULT_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
            ).coerceIn(
                MIN_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
                MAX_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
            ),
            nativeNotificationBubblesEnabled = prefs.getBoolean(
                KEY_NATIVE_NOTIFICATION_BUBBLES,
                DEFAULT_NATIVE_NOTIFICATION_BUBBLES
            ),
            extremeRefresh165Enabled = prefs.getBoolean(KEY_EXTREME_REFRESH_165, false),
            launcherRegionMode = prefs.getInt(
                KEY_LAUNCHER_REGION_MODE,
                DEFAULT_LAUNCHER_REGION_MODE
            ).sanitizeLauncherRegionMode(),
            recentTaskRadiusEnabled = prefs.getBoolean(KEY_RECENT_TASK_RADIUS, false),
            recentTaskRadiusDp = prefs.getInt(
                KEY_RECENT_TASK_RADIUS_DP,
                DEFAULT_RECENT_TASK_RADIUS_DP
            ).coerceIn(0, 260),
            aodEnhanceEnabled = prefs.getBoolean(KEY_AOD_ENHANCE, false),
            aodInitDarkBrightness = prefs.getInt(
                KEY_AOD_INIT_DARK_BRIGHTNESS,
                DEFAULT_AOD_INIT_DARK_BRIGHTNESS
            ).coerceIn(0, 255),
            aodInitBrightBrightness = prefs.getInt(
                KEY_AOD_INIT_BRIGHT_BRIGHTNESS,
                DEFAULT_AOD_INIT_BRIGHT_BRIGHTNESS
            ).coerceIn(0, 255),
            aodRunningBrightnessMultiplier = prefs.getFloat(
                KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER,
                DEFAULT_AOD_RUNNING_BRIGHTNESS_MULTIPLIER
            ).coerceIn(1.0f, 3.0f),
            aodPanoramicSupportEnabled = prefs.getBoolean(
                KEY_AOD_PANORAMIC_SUPPORT,
                DEFAULT_AOD_PANORAMIC_SUPPORT
            ),
            aodSettingsSwitchEnabled = prefs.getBoolean(
                KEY_AOD_SETTINGS_SWITCH,
                DEFAULT_AOD_SETTINGS_SWITCH
            ),
            aodSingleClickBlockEnabled = prefs.getBoolean(
                KEY_AOD_SINGLE_CLICK_BLOCK,
                DEFAULT_AOD_SINGLE_CLICK_BLOCK
            ),
            oosLocalizerEnabled = prefs.getBoolean(KEY_OOS_LOCALIZER, false),
            doublePowerCustomEnabled = prefs.getBoolean(KEY_DOUBLE_POWER_CUSTOM, false),
            doublePowerTargetPackage = prefs.getString(
                KEY_DOUBLE_POWER_TARGET_PACKAGE,
                DEFAULT_DOUBLE_POWER_TARGET_PACKAGE
            ).orEmpty(),
            doublePowerTargetActivity = prefs.getString(
                KEY_DOUBLE_POWER_TARGET_ACTIVITY,
                DEFAULT_DOUBLE_POWER_TARGET_ACTIVITY
            ).orEmpty(),
            assistantPowerMode = prefs.getInt(
                KEY_ASSISTANT_POWER_MODE,
                DEFAULT_ASSISTANT_POWER_MODE
            ).sanitizeAssistantPowerMode(),
            assistantGestureCircleEnabled = prefs.getBoolean(
                KEY_ASSISTANT_GESTURE_CIRCLE,
                false
            )
        )
    }

    fun readSyncedUiSnapshot(context: Context): UiSnapshot {
        return UiSnapshot(
            nativeNotifyIconEnabled = isNativeNotifyIconEnabled(context),
            notificationBubbleBlurEnabled = isNotificationBubbleBlurEnabled(context),
            notificationBubbleBlurRadiusPx = getNotificationBubbleBlurRadiusPx(context),
            nativeNotificationBubblesEnabled = isNativeNotificationBubblesEnabled(context),
            extremeRefresh165Enabled = isExtremeRefresh165Enabled(context),
            launcherRegionMode = getLauncherRegionMode(context),
            recentTaskRadiusEnabled = isRecentTaskRadiusEnabled(context),
            recentTaskRadiusDp = getRecentTaskRadiusDp(context),
            aodEnhanceEnabled = isAodEnhanceEnabled(context),
            aodInitDarkBrightness = getAodInitDarkBrightness(context),
            aodInitBrightBrightness = getAodInitBrightBrightness(context),
            aodRunningBrightnessMultiplier = getAodRunningBrightnessMultiplier(context),
            aodPanoramicSupportEnabled = isAodPanoramicSupportEnabled(context),
            aodSettingsSwitchEnabled = isAodSettingsSwitchEnabled(context),
            aodSingleClickBlockEnabled = isAodSingleClickBlockEnabled(context),
            oosLocalizerEnabled = isOosLocalizerEnabled(context),
            doublePowerCustomEnabled = isDoublePowerCustomEnabled(context),
            doublePowerTargetPackage = getDoublePowerTargetPackage(context),
            doublePowerTargetActivity = getDoublePowerTargetActivity(context),
            assistantPowerMode = getAssistantPowerMode(context),
            assistantGestureCircleEnabled = isAssistantGestureCircleEnabled(context)
        )
    }

    fun isNativeNotifyIconEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_NATIVE_NOTIFY_ICON,
            propertyKey = PROP_KEY_NATIVE_NOTIFY_ICON,
            settingsKey = SETTINGS_KEY_NATIVE_NOTIFY_ICON,
            flagFilePath = FLAG_FILE_PATH_NATIVE_NOTIFY_ICON,
            legacyFlagFilePath = LEGACY_FLAG_FILE_PATH_NATIVE_NOTIFY_ICON,
            prefsKey = KEY_NATIVE_NOTIFY_ICON,
            defaultValue = true
        )
    }

    fun setNativeNotifyIconEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NATIVE_NOTIFY_ICON, enabled).commit()
        syncReadableState(context)
        syncFlagState(
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_NATIVE_NOTIFY_ICON,
                PROP_KEY_NATIVE_NOTIFY_ICON
            ),
            settingsGlobalKey = SETTINGS_KEY_NATIVE_NOTIFY_ICON,
            flagFilePath = FLAG_FILE_PATH_NATIVE_NOTIFY_ICON
        )
    }

    fun isExtremeRefresh165Enabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_EXTREME_REFRESH_165,
            propertyKey = PROP_KEY_EXTREME_REFRESH_165,
            settingsKey = SETTINGS_KEY_EXTREME_REFRESH_165,
            flagFilePath = FLAG_FILE_PATH_EXTREME_REFRESH_165,
            legacyFlagFilePath = LEGACY_FLAG_FILE_PATH_EXTREME_REFRESH_165,
            prefsKey = KEY_EXTREME_REFRESH_165,
            defaultValue = false
        )
    }

    fun setExtremeRefresh165Enabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_EXTREME_REFRESH_165, enabled).commit()
        syncReadableState(context)
        syncFlagState(
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_EXTREME_REFRESH_165,
                PROP_KEY_EXTREME_REFRESH_165
            ),
            settingsGlobalKey = SETTINGS_KEY_EXTREME_REFRESH_165,
            flagFilePath = FLAG_FILE_PATH_EXTREME_REFRESH_165
        )
    }

    fun isRecentTaskRadiusEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_RECENT_TASK_RADIUS, false)
    }

    fun setRecentTaskRadiusEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_RECENT_TASK_RADIUS, enabled).commit()
        syncReadableState(context)
        syncFlagState(
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_RECENT_TASK_RADIUS,
                PROP_KEY_RECENT_TASK_RADIUS
            ),
            settingsGlobalKey = SETTINGS_KEY_RECENT_TASK_RADIUS,
            flagFilePath = FLAG_FILE_PATH_RECENT_TASK_RADIUS
        )
    }

    fun isAodEnhanceEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_AOD_ENHANCE,
            propertyKey = PROP_KEY_AOD_ENHANCE,
            settingsKey = SETTINGS_KEY_AOD_ENHANCE,
            flagFilePath = FLAG_FILE_PATH_AOD_ENHANCE,
            legacyFlagFilePath = LEGACY_FLAG_FILE_PATH_AOD_ENHANCE,
            prefsKey = KEY_AOD_ENHANCE,
            defaultValue = false
        )
    }

    fun setAodEnhanceEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AOD_ENHANCE, enabled).commit()
        syncReadableState(context)
        syncFlagState(
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_ENHANCE,
                PROP_KEY_AOD_ENHANCE
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_ENHANCE,
            flagFilePath = FLAG_FILE_PATH_AOD_ENHANCE
        )
    }

    fun isOosLocalizerEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_OOS_LOCALIZER,
            propertyKey = PROP_KEY_OOS_LOCALIZER,
            settingsKey = SETTINGS_KEY_OOS_LOCALIZER,
            flagFilePath = FLAG_FILE_PATH_OOS_LOCALIZER,
            legacyFlagFilePath = LEGACY_FLAG_FILE_PATH_OOS_LOCALIZER,
            prefsKey = KEY_OOS_LOCALIZER,
            defaultValue = false
        )
    }

    fun setOosLocalizerEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OOS_LOCALIZER, enabled).commit()
        syncReadableState(context)
        syncFlagState(
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER,
                PROP_KEY_OOS_LOCALIZER
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER,
            flagFilePath = FLAG_FILE_PATH_OOS_LOCALIZER
        )
    }

    fun isDoublePowerCustomEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_DOUBLE_POWER_CUSTOM,
            propertyKey = PROP_KEY_DOUBLE_POWER_CUSTOM,
            settingsKey = SETTINGS_KEY_DOUBLE_POWER_CUSTOM,
            flagFilePath = FLAG_FILE_PATH_DOUBLE_POWER_CUSTOM,
            legacyFlagFilePath = null,
            prefsKey = KEY_DOUBLE_POWER_CUSTOM,
            defaultValue = false
        )
    }

    fun setDoublePowerCustomEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DOUBLE_POWER_CUSTOM, enabled).commit()
        syncReadableState(context)
        syncFlagState(
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_DOUBLE_POWER_CUSTOM,
                PROP_KEY_DOUBLE_POWER_CUSTOM
            ),
            settingsGlobalKey = SETTINGS_KEY_DOUBLE_POWER_CUSTOM,
            flagFilePath = FLAG_FILE_PATH_DOUBLE_POWER_CUSTOM
        )
        syncDoublePowerCustomState(context)
    }

    fun getDoublePowerTargetPackage(context: Context): String {
        return prefs(context).getString(
            KEY_DOUBLE_POWER_TARGET_PACKAGE,
            DEFAULT_DOUBLE_POWER_TARGET_PACKAGE
        ).orEmpty()
    }

    fun setDoublePowerTargetPackage(context: Context, value: String) {
        val normalized = value.trim()
        prefs(context).edit().putString(KEY_DOUBLE_POWER_TARGET_PACKAGE, normalized).commit()
        syncReadableState(context)
        syncScalarState(
            value = normalized,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_DOUBLE_POWER_TARGET_PACKAGE,
                PROP_KEY_DOUBLE_POWER_TARGET_PACKAGE
            ),
            settingsGlobalKey = SETTINGS_KEY_DOUBLE_POWER_TARGET_PACKAGE,
            textFilePath = TEXT_FILE_PATH_DOUBLE_POWER_TARGET_PACKAGE
        )
        syncDoublePowerCustomState(context)
    }

    fun getDoublePowerTargetActivity(context: Context): String {
        return prefs(context).getString(
            KEY_DOUBLE_POWER_TARGET_ACTIVITY,
            DEFAULT_DOUBLE_POWER_TARGET_ACTIVITY
        ).orEmpty()
    }

    fun setDoublePowerTargetActivity(context: Context, value: String) {
        val normalized = value.trim()
        prefs(context).edit().putString(KEY_DOUBLE_POWER_TARGET_ACTIVITY, normalized).commit()
        syncReadableState(context)
        syncScalarState(
            value = normalized,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_DOUBLE_POWER_TARGET_ACTIVITY,
                PROP_KEY_DOUBLE_POWER_TARGET_ACTIVITY
            ),
            settingsGlobalKey = SETTINGS_KEY_DOUBLE_POWER_TARGET_ACTIVITY,
            textFilePath = TEXT_FILE_PATH_DOUBLE_POWER_TARGET_ACTIVITY
        )
        syncDoublePowerCustomState(context)
    }

    fun getAssistantPowerMode(context: Context): Int {
        return readSyncedInt(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_ASSISTANT_POWER_MODE,
            propertyKey = PROP_KEY_ASSISTANT_POWER_MODE,
            settingsKey = SETTINGS_KEY_ASSISTANT_POWER_MODE,
            prefsKey = KEY_ASSISTANT_POWER_MODE,
            defaultValue = DEFAULT_ASSISTANT_POWER_MODE
        ).sanitizeAssistantPowerMode()
    }

    fun setAssistantPowerMode(context: Context, mode: Int) {
        val normalized = mode.sanitizeAssistantPowerMode()
        prefs(context).edit().putInt(KEY_ASSISTANT_POWER_MODE, normalized).commit()
        syncReadableState(context)
        syncScalarState(
            value = normalized.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_ASSISTANT_POWER_MODE,
                PROP_KEY_ASSISTANT_POWER_MODE
            ),
            settingsGlobalKey = SETTINGS_KEY_ASSISTANT_POWER_MODE
        )
    }

    fun isAssistantGestureCircleEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_ASSISTANT_GESTURE_CIRCLE,
            propertyKey = PROP_KEY_ASSISTANT_GESTURE_CIRCLE,
            settingsKey = SETTINGS_KEY_ASSISTANT_GESTURE_CIRCLE,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_ASSISTANT_GESTURE_CIRCLE,
            defaultValue = false
        )
    }

    fun setAssistantGestureCircleEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ASSISTANT_GESTURE_CIRCLE, enabled).commit()
        syncReadableState(context)
        syncScalarState(
            value = if (enabled) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_ASSISTANT_GESTURE_CIRCLE,
                PROP_KEY_ASSISTANT_GESTURE_CIRCLE
            ),
            settingsGlobalKey = SETTINGS_KEY_ASSISTANT_GESTURE_CIRCLE
        )
    }

    fun getRecentTaskRadiusDp(context: Context): Int {
        return prefs(context)
            .getInt(KEY_RECENT_TASK_RADIUS_DP, DEFAULT_RECENT_TASK_RADIUS_DP)
            .coerceIn(0, 260)
    }

    fun setRecentTaskRadiusDp(context: Context, value: Int) {
        val normalized = value.coerceIn(0, 260)
        prefs(context).edit().putInt(KEY_RECENT_TASK_RADIUS_DP, normalized).commit()
        syncReadableState(context)
        syncScalarState(
            value = normalized.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_RECENT_TASK_RADIUS_DP,
                PROP_KEY_RECENT_TASK_RADIUS_DP
            ),
            settingsGlobalKey = SETTINGS_KEY_RECENT_TASK_RADIUS_DP
        )
    }

    fun getAodInitDarkBrightness(context: Context): Int {
        return prefs(context)
            .getInt(KEY_AOD_INIT_DARK_BRIGHTNESS, DEFAULT_AOD_INIT_DARK_BRIGHTNESS)
            .coerceIn(0, 255)
    }

    fun setAodInitDarkBrightness(context: Context, value: Int) {
        val normalized = value.coerceIn(0, 255)
        prefs(context).edit().putInt(KEY_AOD_INIT_DARK_BRIGHTNESS, normalized).commit()
        syncReadableState(context)
        syncScalarState(
            value = normalized.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_INIT_DARK_BRIGHTNESS,
                PROP_KEY_AOD_INIT_DARK_BRIGHTNESS
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_INIT_DARK_BRIGHTNESS
        )
    }

    fun getAodInitBrightBrightness(context: Context): Int {
        return prefs(context)
            .getInt(KEY_AOD_INIT_BRIGHT_BRIGHTNESS, DEFAULT_AOD_INIT_BRIGHT_BRIGHTNESS)
            .coerceIn(0, 255)
    }

    fun setAodInitBrightBrightness(context: Context, value: Int) {
        val normalized = value.coerceIn(0, 255)
        prefs(context).edit().putInt(KEY_AOD_INIT_BRIGHT_BRIGHTNESS, normalized).commit()
        syncReadableState(context)
        syncScalarState(
            value = normalized.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS,
                PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_INIT_BRIGHT_BRIGHTNESS
        )
    }

    fun getAodRunningBrightnessMultiplier(context: Context): Float {
        return prefs(context)
            .getFloat(
                KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER,
                DEFAULT_AOD_RUNNING_BRIGHTNESS_MULTIPLIER
            )
            .coerceIn(1.0f, 3.0f)
    }

    fun setAodRunningBrightnessMultiplier(context: Context, value: Float) {
        val normalized = value.coerceIn(1.0f, 3.0f)
        prefs(context).edit().putFloat(KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER, normalized).commit()
        syncReadableState(context)
        syncScalarState(
            value = normalized.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER,
                PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER
        )
    }

    fun isAodPanoramicSupportEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_AOD_PANORAMIC_SUPPORT, DEFAULT_AOD_PANORAMIC_SUPPORT)
    }

    fun setAodPanoramicSupportEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AOD_PANORAMIC_SUPPORT, enabled).commit()
        syncReadableState(context)
        syncScalarState(
            value = if (enabled) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_PANORAMIC_SUPPORT,
                PROP_KEY_AOD_PANORAMIC_SUPPORT
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_PANORAMIC_SUPPORT
        )
    }

    fun isAodSettingsSwitchEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_AOD_SETTINGS_SWITCH, DEFAULT_AOD_SETTINGS_SWITCH)
    }

    fun setAodSettingsSwitchEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AOD_SETTINGS_SWITCH, enabled).commit()
        syncReadableState(context)
        syncScalarState(
            value = if (enabled) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_SETTINGS_SWITCH,
                PROP_KEY_AOD_SETTINGS_SWITCH
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_SETTINGS_SWITCH
        )
    }

    fun isAodSingleClickBlockEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_AOD_SINGLE_CLICK_BLOCK, DEFAULT_AOD_SINGLE_CLICK_BLOCK)
    }

    fun setAodSingleClickBlockEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AOD_SINGLE_CLICK_BLOCK, enabled).commit()
        syncReadableState(context)
        syncScalarState(
            value = if (enabled) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_SINGLE_CLICK_BLOCK,
                PROP_KEY_AOD_SINGLE_CLICK_BLOCK
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_SINGLE_CLICK_BLOCK
        )
    }

    fun isNotificationBubbleBlurEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(
            KEY_NOTIFICATION_BUBBLE_BLUR,
            DEFAULT_NOTIFICATION_BUBBLE_BLUR
        )
    }

    fun setNotificationBubbleBlurEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATION_BUBBLE_BLUR, enabled).commit()
        syncReadableState(context)
        syncScalarState(
            value = if (enabled) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_NOTIFICATION_BUBBLE_BLUR,
                PROP_KEY_NOTIFICATION_BUBBLE_BLUR
            ),
            settingsGlobalKey = SETTINGS_KEY_NOTIFICATION_BUBBLE_BLUR
        )
    }

    fun getNotificationBubbleBlurRadiusPx(context: Context): Int {
        return prefs(context)
            .getInt(KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX, DEFAULT_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX)
            .coerceIn(MIN_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX, MAX_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX)
    }

    fun setNotificationBubbleBlurRadiusPx(context: Context, value: Int) {
        val normalized = value.coerceIn(
            MIN_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
            MAX_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
        )
        prefs(context).edit().putInt(KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX, normalized).commit()
        syncReadableState(context)
        syncScalarState(
            value = normalized.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
                PROP_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
            ),
            settingsGlobalKey = SETTINGS_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
        )
    }

    fun isNativeNotificationBubblesEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_NATIVE_NOTIFICATION_BUBBLES,
            propertyKey = PROP_KEY_NATIVE_NOTIFICATION_BUBBLES,
            settingsKey = SETTINGS_KEY_NATIVE_NOTIFICATION_BUBBLES,
            flagFilePath = FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES,
            legacyFlagFilePath = LEGACY_FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES,
            prefsKey = KEY_NATIVE_NOTIFICATION_BUBBLES,
            defaultValue = DEFAULT_NATIVE_NOTIFICATION_BUBBLES
        )
    }

    fun setNativeNotificationBubblesEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NATIVE_NOTIFICATION_BUBBLES, enabled).commit()
        syncReadableState(context)
        syncFlagState(
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_NATIVE_NOTIFICATION_BUBBLES,
                PROP_KEY_NATIVE_NOTIFICATION_BUBBLES
            ),
            settingsGlobalKey = SETTINGS_KEY_NATIVE_NOTIFICATION_BUBBLES,
            flagFilePath = FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES
        )
    }

    fun getLauncherRegionMode(context: Context): Int {
        return readSyncedInt(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_LAUNCHER_REGION_MODE,
            propertyKey = PROP_KEY_LAUNCHER_REGION_MODE,
            settingsKey = SETTINGS_KEY_LAUNCHER_REGION_MODE,
            prefsKey = KEY_LAUNCHER_REGION_MODE,
            defaultValue = DEFAULT_LAUNCHER_REGION_MODE
        ).sanitizeLauncherRegionMode()
    }

    fun setLauncherRegionMode(context: Context, mode: Int) {
        val normalized = mode.sanitizeLauncherRegionMode()
        prefs(context).edit().putInt(KEY_LAUNCHER_REGION_MODE, normalized).commit()
        syncReadableState(context)
        syncScalarState(
            value = normalized.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_LAUNCHER_REGION_MODE,
                PROP_KEY_LAUNCHER_REGION_MODE
            ),
            settingsGlobalKey = SETTINGS_KEY_LAUNCHER_REGION_MODE
        )
    }

    fun syncTogglesForBoot(context: Context) {
        val nativeEnabled = isNativeNotifyIconEnabled(context)
        val extremeRefresh165Enabled = isExtremeRefresh165Enabled(context)
        val recentTaskRadiusEnabled = isRecentTaskRadiusEnabled(context)
        val aodEnhanceEnabled = isAodEnhanceEnabled(context)
        val oosLocalizerEnabled = isOosLocalizerEnabled(context)
        val doublePowerCustomEnabled = isDoublePowerCustomEnabled(context)
        val assistantPowerMode = getAssistantPowerMode(context)
        val assistantGestureCircleEnabled = isAssistantGestureCircleEnabled(context)
        val recentTaskRadiusDp = getRecentTaskRadiusDp(context)
        val aodInitDarkBrightness = getAodInitDarkBrightness(context)
        val aodInitBrightBrightness = getAodInitBrightBrightness(context)
        val aodRunningMultiplier = getAodRunningBrightnessMultiplier(context)
        val aodPanoramicSupport = isAodPanoramicSupportEnabled(context)
        val aodSettingsSwitch = isAodSettingsSwitchEnabled(context)
        val aodSingleClickBlock = isAodSingleClickBlockEnabled(context)
        val notificationBubbleBlur = isNotificationBubbleBlurEnabled(context)
        val notificationBubbleBlurRadiusPx = getNotificationBubbleBlurRadiusPx(context)
        val nativeNotificationBubbles = isNativeNotificationBubblesEnabled(context)
        val launcherRegionMode = getLauncherRegionMode(context)
        syncReadableState(context)
        syncFlagState(
            enabled = nativeEnabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_NATIVE_NOTIFY_ICON,
                PROP_KEY_NATIVE_NOTIFY_ICON
            ),
            settingsGlobalKey = SETTINGS_KEY_NATIVE_NOTIFY_ICON,
            flagFilePath = FLAG_FILE_PATH_NATIVE_NOTIFY_ICON
        )
        syncFlagState(
            enabled = extremeRefresh165Enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_EXTREME_REFRESH_165,
                PROP_KEY_EXTREME_REFRESH_165
            ),
            settingsGlobalKey = SETTINGS_KEY_EXTREME_REFRESH_165,
            flagFilePath = FLAG_FILE_PATH_EXTREME_REFRESH_165
        )
        syncFlagState(
            enabled = recentTaskRadiusEnabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_RECENT_TASK_RADIUS,
                PROP_KEY_RECENT_TASK_RADIUS
            ),
            settingsGlobalKey = SETTINGS_KEY_RECENT_TASK_RADIUS,
            flagFilePath = FLAG_FILE_PATH_RECENT_TASK_RADIUS
        )
        syncFlagState(
            enabled = aodEnhanceEnabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_ENHANCE,
                PROP_KEY_AOD_ENHANCE
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_ENHANCE,
            flagFilePath = FLAG_FILE_PATH_AOD_ENHANCE
        )
        syncFlagState(
            enabled = oosLocalizerEnabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER,
                PROP_KEY_OOS_LOCALIZER
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER,
            flagFilePath = FLAG_FILE_PATH_OOS_LOCALIZER
        )
        syncFlagState(
            enabled = doublePowerCustomEnabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_DOUBLE_POWER_CUSTOM,
                PROP_KEY_DOUBLE_POWER_CUSTOM
            ),
            settingsGlobalKey = SETTINGS_KEY_DOUBLE_POWER_CUSTOM,
            flagFilePath = FLAG_FILE_PATH_DOUBLE_POWER_CUSTOM
        )
        syncDoublePowerCustomState(context)
        syncScalarState(
            value = assistantPowerMode.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_ASSISTANT_POWER_MODE,
                PROP_KEY_ASSISTANT_POWER_MODE
            ),
            settingsGlobalKey = SETTINGS_KEY_ASSISTANT_POWER_MODE
        )
        syncScalarState(
            value = if (assistantGestureCircleEnabled) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_ASSISTANT_GESTURE_CIRCLE,
                PROP_KEY_ASSISTANT_GESTURE_CIRCLE
            ),
            settingsGlobalKey = SETTINGS_KEY_ASSISTANT_GESTURE_CIRCLE
        )
        syncScalarState(
            value = recentTaskRadiusDp.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_RECENT_TASK_RADIUS_DP,
                PROP_KEY_RECENT_TASK_RADIUS_DP
            ),
            settingsGlobalKey = SETTINGS_KEY_RECENT_TASK_RADIUS_DP
        )
        syncScalarState(
            value = aodInitDarkBrightness.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_INIT_DARK_BRIGHTNESS,
                PROP_KEY_AOD_INIT_DARK_BRIGHTNESS
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_INIT_DARK_BRIGHTNESS
        )
        syncScalarState(
            value = aodInitBrightBrightness.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS,
                PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_INIT_BRIGHT_BRIGHTNESS
        )
        syncScalarState(
            value = aodRunningMultiplier.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER,
                PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER
        )
        syncScalarState(
            value = if (aodPanoramicSupport) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_PANORAMIC_SUPPORT,
                PROP_KEY_AOD_PANORAMIC_SUPPORT
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_PANORAMIC_SUPPORT
        )
        syncScalarState(
            value = if (aodSettingsSwitch) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_SETTINGS_SWITCH,
                PROP_KEY_AOD_SETTINGS_SWITCH
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_SETTINGS_SWITCH
        )
        syncScalarState(
            value = if (aodSingleClickBlock) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_AOD_SINGLE_CLICK_BLOCK,
                PROP_KEY_AOD_SINGLE_CLICK_BLOCK
            ),
            settingsGlobalKey = SETTINGS_KEY_AOD_SINGLE_CLICK_BLOCK
        )
        syncScalarState(
            value = if (notificationBubbleBlur) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_NOTIFICATION_BUBBLE_BLUR,
                PROP_KEY_NOTIFICATION_BUBBLE_BLUR
            ),
            settingsGlobalKey = SETTINGS_KEY_NOTIFICATION_BUBBLE_BLUR
        )
        syncScalarState(
            value = notificationBubbleBlurRadiusPx.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
                PROP_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
            ),
            settingsGlobalKey = SETTINGS_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
        )
        syncFlagState(
            enabled = nativeNotificationBubbles,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_NATIVE_NOTIFICATION_BUBBLES,
                PROP_KEY_NATIVE_NOTIFICATION_BUBBLES
            ),
            settingsGlobalKey = SETTINGS_KEY_NATIVE_NOTIFICATION_BUBBLES,
            flagFilePath = FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES
        )
        syncScalarState(
            value = launcherRegionMode.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_LAUNCHER_REGION_MODE,
                PROP_KEY_LAUNCHER_REGION_MODE
            ),
            settingsGlobalKey = SETTINGS_KEY_LAUNCHER_REGION_MODE
        )
    }

    fun syncReadableState(context: Context) {
        makePrefsReadableForXposed(context)
        runCatching {
            makePrefsReadableForXposed(prefsContext(context))
        }
    }

    fun isNativeNotifyIconEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_NATIVE_NOTIFY_ICON)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_NATIVE_NOTIFY_ICON)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_NATIVE_NOTIFY_ICON)?.let { return it }
        readFlagFile(FLAG_FILE_PATH_NATIVE_NOTIFY_ICON)?.let { return it }
        readFlagFile(LEGACY_FLAG_FILE_PATH_NATIVE_NOTIFY_ICON)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_NATIVE_NOTIFY_ICON, true)
        }.getOrDefault(true)
    }

    fun isExtremeRefresh165EnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_EXTREME_REFRESH_165)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_EXTREME_REFRESH_165)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_EXTREME_REFRESH_165)?.let { return it }
        readFlagFile(FLAG_FILE_PATH_EXTREME_REFRESH_165)?.let { return it }
        readFlagFile(LEGACY_FLAG_FILE_PATH_EXTREME_REFRESH_165)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_EXTREME_REFRESH_165, false)
        }.getOrDefault(false)
    }

    fun isRecentTaskRadiusEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_RECENT_TASK_RADIUS)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_RECENT_TASK_RADIUS)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_RECENT_TASK_RADIUS)?.let { return it }
        readFlagFile(FLAG_FILE_PATH_RECENT_TASK_RADIUS)?.let { return it }
        readFlagFile(LEGACY_FLAG_FILE_PATH_RECENT_TASK_RADIUS)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_RECENT_TASK_RADIUS, false)
        }.getOrDefault(false)
    }

    fun isAodEnhanceEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_AOD_ENHANCE)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_AOD_ENHANCE)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_AOD_ENHANCE)?.let { return it }
        readFlagFile(FLAG_FILE_PATH_AOD_ENHANCE)?.let { return it }
        readFlagFile(LEGACY_FLAG_FILE_PATH_AOD_ENHANCE)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_AOD_ENHANCE, false)
        }.getOrDefault(false)
    }

    fun isOosLocalizerEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_OOS_LOCALIZER)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_OOS_LOCALIZER)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_OOS_LOCALIZER)?.let { return it }
        readFlagFile(FLAG_FILE_PATH_OOS_LOCALIZER)?.let { return it }
        readFlagFile(LEGACY_FLAG_FILE_PATH_OOS_LOCALIZER)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_OOS_LOCALIZER, false)
        }.getOrDefault(false)
    }

    fun isDoublePowerCustomEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_DOUBLE_POWER_CUSTOM)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_DOUBLE_POWER_CUSTOM)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_DOUBLE_POWER_CUSTOM)?.let { return it }
        readFlagFile(FLAG_FILE_PATH_DOUBLE_POWER_CUSTOM)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_DOUBLE_POWER_CUSTOM, false)
        }.getOrDefault(false)
    }

    fun getDoublePowerTargetPackageXposed(): String {
        readSystemPropertyValue(PERSIST_PROP_KEY_DOUBLE_POWER_TARGET_PACKAGE)?.let { return it }
        readSystemPropertyValue(PROP_KEY_DOUBLE_POWER_TARGET_PACKAGE)?.let { return it }
        readSettingsGlobalValue(SETTINGS_KEY_DOUBLE_POWER_TARGET_PACKAGE)?.let { return it }
        readTextFileValue(TEXT_FILE_PATH_DOUBLE_POWER_TARGET_PACKAGE)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getString(KEY_DOUBLE_POWER_TARGET_PACKAGE, DEFAULT_DOUBLE_POWER_TARGET_PACKAGE).orEmpty()
        }.getOrDefault(DEFAULT_DOUBLE_POWER_TARGET_PACKAGE)
    }

    fun getDoublePowerTargetActivityXposed(): String {
        readSystemPropertyValue(PERSIST_PROP_KEY_DOUBLE_POWER_TARGET_ACTIVITY)?.let { return it }
        readSystemPropertyValue(PROP_KEY_DOUBLE_POWER_TARGET_ACTIVITY)?.let { return it }
        readSettingsGlobalValue(SETTINGS_KEY_DOUBLE_POWER_TARGET_ACTIVITY)?.let { return it }
        readTextFileValue(TEXT_FILE_PATH_DOUBLE_POWER_TARGET_ACTIVITY)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getString(KEY_DOUBLE_POWER_TARGET_ACTIVITY, DEFAULT_DOUBLE_POWER_TARGET_ACTIVITY).orEmpty()
        }.getOrDefault(DEFAULT_DOUBLE_POWER_TARGET_ACTIVITY)
    }

    fun getAssistantPowerModeXposed(): Int {
        readSystemPropertyValue(PERSIST_PROP_KEY_ASSISTANT_POWER_MODE)?.toIntOrNull()?.let {
            return it.sanitizeAssistantPowerMode()
        }
        readSystemPropertyValue(PROP_KEY_ASSISTANT_POWER_MODE)?.toIntOrNull()?.let {
            return it.sanitizeAssistantPowerMode()
        }
        readSettingsGlobalValue(SETTINGS_KEY_ASSISTANT_POWER_MODE)?.toIntOrNull()?.let {
            return it.sanitizeAssistantPowerMode()
        }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getInt(KEY_ASSISTANT_POWER_MODE, DEFAULT_ASSISTANT_POWER_MODE)
        }.getOrDefault(DEFAULT_ASSISTANT_POWER_MODE).sanitizeAssistantPowerMode()
    }

    fun isAssistantGestureCircleEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_ASSISTANT_GESTURE_CIRCLE)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_ASSISTANT_GESTURE_CIRCLE)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_ASSISTANT_GESTURE_CIRCLE)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_ASSISTANT_GESTURE_CIRCLE, false)
        }.getOrDefault(false)
    }

    fun buildDoublePowerQuickInfoJson(packageName: String, activityName: String): String {
        val normalizedPackage = packageName.trim()
        val normalizedActivity = activityName.trim()
        val hasActivity = normalizedActivity.isNotEmpty()
        val type = if (hasActivity) "jumpUrl" else "launcher"
        val link = if (hasActivity) {
            val component = "$normalizedPackage/$normalizedActivity"
            "intent:#Intent;launchFlags=0x10000000;component=$component;package=$normalizedPackage;end"
        } else {
            normalizedPackage
        }
        return "{" +
            "\"switch\":true," +
            "\"pkgName\":\"${jsonEscape(normalizedPackage)}\"," +
            "\"type\":\"$type\"," +
            "\"link\":\"${jsonEscape(link)}\"," +
            "\"tag\":\"${jsonEscape(normalizedPackage)}\"" +
            "}"
    }

    fun buildDoublePowerAllowPkgList(packageName: String): String {
        return listOf(
            packageName.trim(),
            "com.oplus.camera",
            "com.oplus.accesscard",
            "com.heytap.wallet"
        ).filter { it.isNotBlank() }.distinct().joinToString("+")
    }

    fun getRecentTaskRadiusDpXposed(): Float {
        readSystemPropertyValue(PERSIST_PROP_KEY_RECENT_TASK_RADIUS_DP)?.toFloatOrNull()?.let { return it.coerceIn(0f, 260f) }
        readSystemPropertyValue(PROP_KEY_RECENT_TASK_RADIUS_DP)?.toFloatOrNull()?.let { return it.coerceIn(0f, 260f) }
        readSettingsGlobalValue(SETTINGS_KEY_RECENT_TASK_RADIUS_DP)?.toFloatOrNull()?.let { return it.coerceIn(0f, 260f) }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getInt(KEY_RECENT_TASK_RADIUS_DP, DEFAULT_RECENT_TASK_RADIUS_DP).toFloat()
        }.getOrDefault(DEFAULT_RECENT_TASK_RADIUS_DP.toFloat()).coerceIn(0f, 260f)
    }

    fun getAodInitDarkBrightnessXposed(): Int {
        readSystemPropertyValue(PERSIST_PROP_KEY_AOD_INIT_DARK_BRIGHTNESS)?.toIntOrNull()?.let { return it.coerceIn(0, 255) }
        readSystemPropertyValue(PROP_KEY_AOD_INIT_DARK_BRIGHTNESS)?.toIntOrNull()?.let { return it.coerceIn(0, 255) }
        readSettingsGlobalValue(SETTINGS_KEY_AOD_INIT_DARK_BRIGHTNESS)?.toIntOrNull()?.let { return it.coerceIn(0, 255) }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getInt(KEY_AOD_INIT_DARK_BRIGHTNESS, DEFAULT_AOD_INIT_DARK_BRIGHTNESS)
        }.getOrDefault(DEFAULT_AOD_INIT_DARK_BRIGHTNESS).coerceIn(0, 255)
    }

    fun getAodInitBrightBrightnessXposed(): Int {
        readSystemPropertyValue(PERSIST_PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS)?.toIntOrNull()?.let { return it.coerceIn(0, 255) }
        readSystemPropertyValue(PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS)?.toIntOrNull()?.let { return it.coerceIn(0, 255) }
        readSettingsGlobalValue(SETTINGS_KEY_AOD_INIT_BRIGHT_BRIGHTNESS)?.toIntOrNull()?.let { return it.coerceIn(0, 255) }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getInt(KEY_AOD_INIT_BRIGHT_BRIGHTNESS, DEFAULT_AOD_INIT_BRIGHT_BRIGHTNESS)
        }.getOrDefault(DEFAULT_AOD_INIT_BRIGHT_BRIGHTNESS).coerceIn(0, 255)
    }

    fun getAodRunningBrightnessMultiplierXposed(): Float {
        readSystemPropertyValue(PERSIST_PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER)?.toFloatOrNull()?.let { return it.coerceIn(1.0f, 3.0f) }
        readSystemPropertyValue(PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER)?.toFloatOrNull()?.let { return it.coerceIn(1.0f, 3.0f) }
        readSettingsGlobalValue(SETTINGS_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER)?.toFloatOrNull()?.let { return it.coerceIn(1.0f, 3.0f) }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getFloat(
                KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER,
                DEFAULT_AOD_RUNNING_BRIGHTNESS_MULTIPLIER
            )
        }.getOrDefault(DEFAULT_AOD_RUNNING_BRIGHTNESS_MULTIPLIER).coerceIn(1.0f, 3.0f)
    }

    fun isAodPanoramicSupportEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_AOD_PANORAMIC_SUPPORT)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_AOD_PANORAMIC_SUPPORT)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_AOD_PANORAMIC_SUPPORT)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_AOD_PANORAMIC_SUPPORT, DEFAULT_AOD_PANORAMIC_SUPPORT)
        }.getOrDefault(DEFAULT_AOD_PANORAMIC_SUPPORT)
    }

    fun isAodSettingsSwitchEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_AOD_SETTINGS_SWITCH)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_AOD_SETTINGS_SWITCH)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_AOD_SETTINGS_SWITCH)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_AOD_SETTINGS_SWITCH, DEFAULT_AOD_SETTINGS_SWITCH)
        }.getOrDefault(DEFAULT_AOD_SETTINGS_SWITCH)
    }

    fun isAodSingleClickBlockEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_AOD_SINGLE_CLICK_BLOCK)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_AOD_SINGLE_CLICK_BLOCK)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_AOD_SINGLE_CLICK_BLOCK)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_AOD_SINGLE_CLICK_BLOCK, DEFAULT_AOD_SINGLE_CLICK_BLOCK)
        }.getOrDefault(DEFAULT_AOD_SINGLE_CLICK_BLOCK)
    }

    fun isNotificationBubbleBlurEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_NOTIFICATION_BUBBLE_BLUR)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_NOTIFICATION_BUBBLE_BLUR)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_NOTIFICATION_BUBBLE_BLUR)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_NOTIFICATION_BUBBLE_BLUR, DEFAULT_NOTIFICATION_BUBBLE_BLUR)
        }.getOrDefault(DEFAULT_NOTIFICATION_BUBBLE_BLUR)
    }

    fun getNotificationBubbleBlurRadiusPxXposed(): Int {
        readSystemPropertyValue(PERSIST_PROP_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX)?.toIntOrNull()?.let {
            return it.coerceIn(
                MIN_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
                MAX_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
            )
        }
        readSystemPropertyValue(PROP_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX)?.toIntOrNull()?.let {
            return it.coerceIn(
                MIN_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
                MAX_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
            )
        }
        readSettingsGlobalValue(SETTINGS_KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX)?.toIntOrNull()?.let {
            return it.coerceIn(
                MIN_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
                MAX_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
            )
        }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getInt(
                KEY_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
                DEFAULT_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
            )
        }.getOrDefault(DEFAULT_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX).coerceIn(
            MIN_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX,
            MAX_NOTIFICATION_BUBBLE_BLUR_RADIUS_PX
        )
    }

    fun isNativeNotificationBubblesEnabledXposed(): Boolean {
        readSystemPropertyToggle(PERSIST_PROP_KEY_NATIVE_NOTIFICATION_BUBBLES)?.let { return it }
        readSystemPropertyToggle(PROP_KEY_NATIVE_NOTIFICATION_BUBBLES)?.let { return it }
        readSettingsGlobalToggle(SETTINGS_KEY_NATIVE_NOTIFICATION_BUBBLES)?.let { return it }
        readFlagFile(FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES)?.let { return it }
        readFlagFile(LEGACY_FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(KEY_NATIVE_NOTIFICATION_BUBBLES, DEFAULT_NATIVE_NOTIFICATION_BUBBLES)
        }.getOrDefault(DEFAULT_NATIVE_NOTIFICATION_BUBBLES)
    }

    fun getLauncherRegionModeXposed(): Int {
        readSystemPropertyValue(PERSIST_PROP_KEY_LAUNCHER_REGION_MODE)?.toIntOrNull()?.let {
            return it.sanitizeLauncherRegionMode()
        }
        readSystemPropertyValue(PROP_KEY_LAUNCHER_REGION_MODE)?.toIntOrNull()?.let {
            return it.sanitizeLauncherRegionMode()
        }
        readSettingsGlobalValue(SETTINGS_KEY_LAUNCHER_REGION_MODE)?.toIntOrNull()?.let {
            return it.sanitizeLauncherRegionMode()
        }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getInt(KEY_LAUNCHER_REGION_MODE, DEFAULT_LAUNCHER_REGION_MODE)
        }.getOrDefault(DEFAULT_LAUNCHER_REGION_MODE).sanitizeLauncherRegionMode()
    }

    private fun readSystemPropertyToggle(propertyKey: String): Boolean? {
        return parseToggleValue(readSystemPropertyValue(propertyKey))
    }

    private fun readSyncedToggle(
        context: Context,
        persistPropertyKey: String,
        propertyKey: String,
        settingsKey: String,
        flagFilePath: String?,
        legacyFlagFilePath: String?,
        prefsKey: String,
        defaultValue: Boolean
    ): Boolean {
        readSystemPropertyToggle(persistPropertyKey)?.let { return it }
        readSystemPropertyToggle(propertyKey)?.let { return it }
        readSettingsGlobalToggle(settingsKey)?.let { return it }
        flagFilePath?.let { readFlagFile(it)?.let { value -> return value } }
        legacyFlagFilePath?.let { readFlagFile(it)?.let { value -> return value } }
        return prefs(context).getBoolean(prefsKey, defaultValue)
    }

    private fun readSyncedInt(
        context: Context,
        persistPropertyKey: String,
        propertyKey: String,
        settingsKey: String,
        prefsKey: String,
        defaultValue: Int
    ): Int {
        readSystemPropertyValue(persistPropertyKey)?.toIntOrNull()?.let { return it }
        readSystemPropertyValue(propertyKey)?.toIntOrNull()?.let { return it }
        readSettingsGlobalValue(settingsKey)?.toIntOrNull()?.let { return it }
        return prefs(context).getInt(prefsKey, defaultValue)
    }

    private fun readSystemPropertyValue(propertyKey: String): String? {
        return runCatching {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod = systemProperties.getMethod("get", String::class.java, String::class.java)
            (getMethod.invoke(null, propertyKey, "") as String).trim().takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    private fun readFlagFile(filePath: String): Boolean? {
        return runCatching {
            val file = File(filePath)
            if (!file.exists()) return@runCatching null
            when (file.readText().trim()) {
                "1", "true", "on", "enabled" -> true
                "0", "false", "off", "disabled" -> false
                else -> null
            }
        }.getOrNull()
    }

    private fun readTextFileValue(filePath: String): String? {
        return runCatching {
            val file = File(filePath)
            if (!file.exists()) return@runCatching null
            file.readText().trim().takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    private fun readSettingsGlobalToggle(settingsKey: String): Boolean? {
        return parseToggleValue(readSettingsGlobalValue(settingsKey))
    }

    private fun readSettingsGlobalValue(settingsKey: String): String? {
        readSettingsGlobalViaFramework(settingsKey)?.let { return it }
        readSettingsGlobalViaXml(settingsKey)?.let { return it }
        return null
    }

    private fun readSettingsGlobalViaFramework(settingsKey: String): String? {
        return runCatching {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentThread = activityThreadClass
                .getMethod("currentActivityThread")
                .invoke(null)
                ?: return@runCatching null
            val systemContext = activityThreadClass
                .getMethod("getSystemContext")
                .invoke(currentThread)
                ?: return@runCatching null

            val contentResolver = systemContext.javaClass
                .getMethod("getContentResolver")
                .invoke(systemContext)
                ?: return@runCatching null

            val settingsGlobalClass = Class.forName("android.provider.Settings\$Global")
            val getStringMethod = settingsGlobalClass.getMethod(
                "getString",
                Class.forName("android.content.ContentResolver"),
                String::class.java
            )
            getStringMethod.invoke(null, contentResolver, settingsKey) as? String
        }.getOrNull()
    }

    private fun readSettingsGlobalViaXml(settingsKey: String): String? {
        return runCatching {
            val file = File("/data/system/users/0/settings_global.xml")
            if (!file.exists()) return@runCatching null
            val text = file.readText()
            val escaped = Regex.escape(settingsKey)
            val directOrder = Regex("<setting[^>]*name=\"$escaped\"[^>]*value=\"([^\"]*)\"[^>]*/?>")
                .find(text)
                ?.groupValues
                ?.getOrNull(1)
            if (directOrder != null) return@runCatching directOrder

            val reversedOrder = Regex("<setting[^>]*value=\"([^\"]*)\"[^>]*name=\"$escaped\"[^>]*/?>")
                .find(text)
                ?.groupValues
                ?.getOrNull(1)
            reversedOrder
        }.getOrNull()
    }

    private fun parseToggleValue(raw: String?): Boolean? {
        val value = raw?.trim()?.lowercase() ?: return null
        return when (value) {
            "1", "true", "on", "enabled" -> true
            "0", "false", "off", "disabled" -> false
            else -> null
        }
    }

    private fun Int.sanitizeAssistantPowerMode(): Int {
        return when (this) {
            ASSISTANT_POWER_MODE_NONE,
            ASSISTANT_POWER_MODE_GEMINI -> this
            else -> DEFAULT_ASSISTANT_POWER_MODE
        }
    }

    private fun Int.sanitizeLauncherRegionMode(): Int {
        return when (this) {
            LAUNCHER_REGION_MODE_OFF,
            LAUNCHER_REGION_MODE_CN,
            LAUNCHER_REGION_MODE_IN -> this
            else -> DEFAULT_LAUNCHER_REGION_MODE
        }
    }

    private fun syncFlagState(
        enabled: Boolean,
        propertyKeys: List<String>,
        settingsGlobalKey: String,
        flagFilePath: String
    ) {
        val value = if (enabled) "1" else "0"
        val legacyFlagPath = when (flagFilePath) {
            FLAG_FILE_PATH_NATIVE_NOTIFY_ICON -> LEGACY_FLAG_FILE_PATH_NATIVE_NOTIFY_ICON
            FLAG_FILE_PATH_EXTREME_REFRESH_165 -> LEGACY_FLAG_FILE_PATH_EXTREME_REFRESH_165
            FLAG_FILE_PATH_RECENT_TASK_RADIUS -> LEGACY_FLAG_FILE_PATH_RECENT_TASK_RADIUS
            FLAG_FILE_PATH_AOD_ENHANCE -> LEGACY_FLAG_FILE_PATH_AOD_ENHANCE
            FLAG_FILE_PATH_OOS_LOCALIZER -> LEGACY_FLAG_FILE_PATH_OOS_LOCALIZER
            FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES -> LEGACY_FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES
            else -> null
        }
        runCatching {
            val setPropCommands = propertyKeys.joinToString("; ") { key -> "setprop $key $value" }
            val suCommand = "$setPropCommands; " +
                "settings put global $settingsGlobalKey $value; " +
                "echo $value > $flagFilePath; " +
                "chmod 644 $flagFilePath" +
                if (legacyFlagPath != null) {
                    "; echo $value > $legacyFlagPath; chmod 644 $legacyFlagPath"
                } else {
                    ""
                }

            val directCommands = propertyKeys.map { key -> "setprop $key $value" } +
                listOf(
                    "settings put global $settingsGlobalKey $value",
                    "echo $value > $flagFilePath",
                    "chmod 644 $flagFilePath"
                ) +
                if (legacyFlagPath != null) {
                    listOf(
                        "echo $value > $legacyFlagPath",
                        "chmod 644 $legacyFlagPath"
                    )
                } else {
                    emptyList()
                }
            val directResult = Shell.cmd(*directCommands.toTypedArray()).exec()
            // Some environments only persist these writes when explicitly forced through su.
            val suResult = Shell.cmd("su -c \"$suCommand\"").exec()
            if (!directResult.isSuccess && !suResult.isSuccess) {
                // No-op: best effort sync; Xposed side still has multiple fallback readers.
            }
        }
    }

    private fun syncScalarState(
        value: String,
        propertyKeys: List<String>,
        settingsGlobalKey: String,
        textFilePath: String? = null
    ) {
        runCatching {
            val textFileCommands = if (textFilePath != null) {
                listOf(
                    "printf %s ${shellQuote(value)} > $textFilePath",
                    "chmod 644 $textFilePath"
                )
            } else {
                emptyList()
            }
            val directCommands = propertyKeys.map { key -> "setprop $key $value" } +
                listOf("settings put global $settingsGlobalKey ${shellQuote(value)}") +
                textFileCommands
            val suCommand = directCommands.joinToString("; ")
            val directResult = Shell.cmd(*directCommands.toTypedArray()).exec()
            val suResult = Shell.cmd("su -c \"$suCommand\"").exec()
            if (!directResult.isSuccess && !suResult.isSuccess) {
                // No-op: best effort sync. Xposed side still falls back to prefs.
            }
        }
    }

    private fun syncDoublePowerCustomState(context: Context) {
        val packageName = getDoublePowerTargetPackage(context).trim()
        if (!isDoublePowerCustomEnabled(context) || packageName.isBlank()) {
            clearDoublePowerSystemState()
            return
        }
        val activityName = getDoublePowerTargetActivity(context)
        val quickInfoJson = buildDoublePowerQuickInfoJson(
            packageName = packageName,
            activityName = activityName
        )
        val allowPkgList = buildDoublePowerAllowPkgList(packageName)
        runCatching {
            val directCommands = listOf(
                "settings put global $SETTINGS_KEY_POWER_DW_QUICK_INFO ${shellQuote(quickInfoJson)}",
                "settings put global $SETTINGS_KEY_POWER_DW_ALLOW_SHOW_PKG ${shellQuote(allowPkgList)}",
                "settings put global $SETTINGS_KEY_POWER_DW_ALLOW_SETTING_PKG ${shellQuote(allowPkgList)}"
            )
            val directResult = Shell.cmd(*directCommands.toTypedArray()).exec()
            val suCommand = directCommands.joinToString("; ")
            val suResult = Shell.cmd("su -c ${shellQuote(suCommand)}").exec()
            if (!directResult.isSuccess && !suResult.isSuccess) {
                // Best effort. The provider hook also serves this value when scoped.
            }
        }
    }

    private fun clearDoublePowerSystemState() {
        runCatching {
            val directCommands = listOf(
                "settings delete global $SETTINGS_KEY_POWER_DW_QUICK_INFO",
                "settings delete global $SETTINGS_KEY_POWER_DW_ALLOW_SHOW_PKG",
                "settings delete global $SETTINGS_KEY_POWER_DW_ALLOW_SETTING_PKG",
                "rm -f $TEXT_FILE_PATH_DOUBLE_POWER_TARGET_PACKAGE",
                "rm -f $TEXT_FILE_PATH_DOUBLE_POWER_TARGET_ACTIVITY"
            )
            val directResult = Shell.cmd(*directCommands.toTypedArray()).exec()
            val suCommand = directCommands.joinToString("; ")
            val suResult = Shell.cmd("su -c ${shellQuote(suCommand)}").exec()
            if (!directResult.isSuccess && !suResult.isSuccess) {
                // Best effort cleanup. The Xposed hook is disabled by the module flag.
            }
        }
    }

    private fun shellQuote(value: String): String {
        return "'" + value.replace("'", "'\"'\"'") + "'"
    }

    private fun jsonEscape(value: String): String {
        val out = StringBuilder(value.length + 8)
        value.forEach { char ->
            when (char) {
                '\\' -> out.append("\\\\")
                '"' -> out.append("\\\"")
                '\b' -> out.append("\\b")
                '\u000C' -> out.append("\\f")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                else -> {
                    if (char.code < 0x20) {
                        out.append("\\u")
                        out.append(char.code.toString(16).padStart(4, '0'))
                    } else {
                        out.append(char)
                    }
                }
            }
        }
        return out.toString()
    }

    private fun prefs(context: Context): SharedPreferences {
        val storageContext = prefsContext(context)
        return storageContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun prefsContext(context: Context): Context {
        val deviceContext = context.createDeviceProtectedStorageContext()
        runCatching {
            deviceContext.moveSharedPreferencesFrom(context, PREFS_NAME)
        }
        return deviceContext
    }

    private fun makePrefsReadableForXposed(context: Context) {
        runCatching {
            val userPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            val userPrefsFile = File(userPrefsDir, "$PREFS_NAME.xml")
            if (userPrefsFile.exists()) {
                userPrefsDir.setReadable(true, false)
                userPrefsFile.setReadable(true, false)
            }
        }
        runCatching {
            val devicePrefsDir = File("/data/user_de/0/${context.packageName}/shared_prefs")
            val devicePrefsFile = File(devicePrefsDir, "$PREFS_NAME.xml")
            if (devicePrefsFile.exists()) {
                devicePrefsDir.setReadable(true, false)
                devicePrefsFile.setReadable(true, false)
            }
        }
    }
}
