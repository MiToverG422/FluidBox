package com.mi.fluidbox.lsp

import android.content.Context
import android.content.SharedPreferences
import com.mi.fluidbox.ui.common.ShellLogger
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
    private const val KEY_ASSISTANT_POWER_MODE = "assistant_power_mode"
    private const val KEY_ASSISTANT_GESTURE_CIRCLE = "assistant_gesture_circle_enabled"
    private const val KEY_RECENT_TASK_RADIUS_DP = "recent_task_radius_dp"
    private const val KEY_AOD_INIT_DARK_BRIGHTNESS = "aod_init_dark_brightness"
    private const val KEY_AOD_INIT_BRIGHT_BRIGHTNESS = "aod_init_bright_brightness"
    private const val KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = "aod_running_brightness_multiplier"
    private const val KEY_AOD_PANORAMIC_SUPPORT = "aod_panoramic_support"
    private const val KEY_AOD_SETTINGS_SWITCH = "aod_settings_switch"
    private const val KEY_AOD_SINGLE_CLICK_BLOCK = "aod_single_click_block"
    private const val KEY_NATIVE_NOTIFICATION_BUBBLES = "native_notification_bubbles"
    private const val KEY_STATUS_MOBILE_TYPE_ENABLED = "status_mobile_type_enabled"
    private const val KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF = "status_mobile_type_hide_data_off"
    private const val KEY_STATUS_MOBILE_TYPE_HIDE_WIFI = "status_mobile_type_hide_wifi"
    private const val KEY_SYSTEMUI_HIDE_QS_EDIT = "systemui_hide_qs_edit"
    private const val KEY_SYSTEMUI_HIDE_QS_SETTINGS = "systemui_hide_qs_settings"
    private const val KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER = "systemui_hide_qs_top_carrier"
    private const val KEY_SYSTEMUI_HIDE_QS_MORE = "systemui_hide_qs_more"
    private const val KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY = "systemui_force_native_clipboard_overlay"
    private const val KEY_SETTINGS_FORCE_GOOGLE_ENTRY = "settings_force_google_entry"
    private const val KEY_LAUNCHER_REGION_MODE = "launcher_region_mode"
    private const val KEY_OOS_LOCALIZER_CONFIG_MODE = "oos_localizer_config_mode"
    private const val KEY_OOS_LOCALIZER_REGION = "oos_localizer_region"
    private const val KEY_OOS_LOCALIZER_LOCALE = "oos_localizer_locale"
    private const val KEY_OOS_LOCALIZER_MODEL = "oos_localizer_model"
    private const val KEY_OOS_LOCALIZER_DISABLED_PACKAGES = "oos_localizer_disabled_packages"
    private const val KEY_OOS_LOCALIZER_DISABLED_FEATURES = "oos_localizer_disabled_features"
    private const val KEY_OOS_LOCALIZER_PROPERTY_PREFIX = "oos_localizer_property_"
    private const val KEY_OOS_LOCALIZER_APP_FEATURE_PREFIX = "oos_localizer_app_feature_"
    private const val FLAG_FILE_PATH_NATIVE_NOTIFY_ICON = "/data/local/oost_native_notify_icon.flag"
    private const val FLAG_FILE_PATH_EXTREME_REFRESH_165 = "/data/local/oost_extreme_refresh_165.flag"
    private const val FLAG_FILE_PATH_RECENT_TASK_RADIUS = "/data/local/oost_recent_task_radius.flag"
    private const val FLAG_FILE_PATH_AOD_ENHANCE = "/data/local/oost_aod_enhance.flag"
    private const val FLAG_FILE_PATH_OOS_LOCALIZER = "/data/local/oost_oos_localizer.flag"
    private const val FLAG_FILE_PATH_NATIVE_NOTIFICATION_BUBBLES = "/data/local/oost_native_notification_bubbles.flag"
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
    private const val PROP_KEY_ASSISTANT_POWER_MODE = "oost.assistant_power_mode"
    private const val PROP_KEY_ASSISTANT_GESTURE_CIRCLE = "oost.assistant_gesture_circle"
    private const val PROP_KEY_RECENT_TASK_RADIUS_DP = "oost.recent_task_radius_dp"
    private const val PROP_KEY_AOD_INIT_DARK_BRIGHTNESS = "oost.aod_init_dark_brightness"
    private const val PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS = "oost.aod_init_bright_brightness"
    private const val PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = "oost.aod_running_brightness_multiplier"
    private const val PROP_KEY_AOD_PANORAMIC_SUPPORT = "oost.aod_panoramic_support"
    private const val PROP_KEY_AOD_SETTINGS_SWITCH = "oost.aod_settings_switch"
    private const val PROP_KEY_AOD_SINGLE_CLICK_BLOCK = "oost.aod_single_click_block"
    private const val PROP_KEY_NATIVE_NOTIFICATION_BUBBLES = "oost.native_notification_bubbles"
    private const val PROP_KEY_STATUS_MOBILE_TYPE_ENABLED = "oost.status_mobile_type"
    private const val PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF = "oost.status_mobile_type_hide_data_off"
    private const val PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI = "oost.status_mobile_type_hide_wifi"
    private const val PROP_KEY_SYSTEMUI_HIDE_QS_EDIT = "oost.systemui_hide_qs_edit"
    private const val PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS = "oost.systemui_hide_qs_settings"
    private const val PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER = "oost.systemui_hide_qs_top_carrier"
    private const val PROP_KEY_SYSTEMUI_HIDE_QS_MORE = "oost.systemui_hide_qs_more"
    private const val PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY = "oost.systemui_force_native_clipboard_overlay"
    private const val PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY = "oost.settings_force_google_entry"
    private const val PROP_KEY_LAUNCHER_REGION_MODE = "oost.launcher_region_mode"
    private const val PROP_KEY_OOS_LOCALIZER_CONFIG_MODE = "oost.oos_localizer_config_mode"
    private const val PROP_KEY_OOS_LOCALIZER_REGION = "oost.oos_localizer_region"
    private const val PROP_KEY_OOS_LOCALIZER_LOCALE = "oost.oos_localizer_locale"
    private const val PROP_KEY_OOS_LOCALIZER_MODEL = "oost.oos_localizer_model"
    private const val PERSIST_PROP_KEY_NATIVE_NOTIFY_ICON = "persist.sys.oost.native_notify_icon"
    private const val PERSIST_PROP_KEY_EXTREME_REFRESH_165 = "persist.sys.oost.extreme_refresh_165"
    private const val PERSIST_PROP_KEY_RECENT_TASK_RADIUS = "persist.sys.oost.recent_task_radius"
    private const val PERSIST_PROP_KEY_AOD_ENHANCE = "persist.sys.oost.aod_enhance"
    private const val PERSIST_PROP_KEY_OOS_LOCALIZER = "persist.sys.oost.oos_localizer"
    private const val PERSIST_PROP_KEY_ASSISTANT_POWER_MODE = "persist.sys.oost.assistant_power_mode"
    private const val PERSIST_PROP_KEY_ASSISTANT_GESTURE_CIRCLE = "persist.sys.oost.assistant_gesture_circle"
    private const val PERSIST_PROP_KEY_RECENT_TASK_RADIUS_DP = "persist.sys.oost.recent_task_radius_dp"
    private const val PERSIST_PROP_KEY_AOD_INIT_DARK_BRIGHTNESS = "persist.sys.oost.aod_init_dark_brightness"
    private const val PERSIST_PROP_KEY_AOD_INIT_BRIGHT_BRIGHTNESS = "persist.sys.oost.aod_init_bright_brightness"
    private const val PERSIST_PROP_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = "persist.sys.oost.aod_running_brightness_multiplier"
    private const val PERSIST_PROP_KEY_AOD_PANORAMIC_SUPPORT = "persist.sys.oost.aod_panoramic_support"
    private const val PERSIST_PROP_KEY_AOD_SETTINGS_SWITCH = "persist.sys.oost.aod_settings_switch"
    private const val PERSIST_PROP_KEY_AOD_SINGLE_CLICK_BLOCK = "persist.sys.oost.aod_single_click_block"
    private const val PERSIST_PROP_KEY_NATIVE_NOTIFICATION_BUBBLES = "persist.sys.oost.native_notification_bubbles"
    private const val PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_ENABLED = "persist.sys.oost.status_mobile_type"
    private const val PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF = "persist.sys.oost.status_mobile_type_hide_data_off"
    private const val PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI = "persist.sys.oost.status_mobile_type_hide_wifi"
    private const val PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_EDIT = "persist.sys.oost.systemui_hide_qs_edit"
    private const val PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS = "persist.sys.oost.systemui_hide_qs_settings"
    private const val PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER = "persist.sys.oost.systemui_hide_qs_top_carrier"
    private const val PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_MORE = "persist.sys.oost.systemui_hide_qs_more"
    private const val PERSIST_PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY = "persist.sys.oost.systemui_force_native_clipboard_overlay"
    private const val PERSIST_PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY = "persist.sys.oost.settings_force_google_entry"
    private const val PERSIST_PROP_KEY_LAUNCHER_REGION_MODE = "persist.sys.oost.launcher_region_mode"
    private const val PERSIST_PROP_KEY_OOS_LOCALIZER_CONFIG_MODE = "persist.sys.oost.oos_localizer_config_mode"
    private const val PERSIST_PROP_KEY_OOS_LOCALIZER_REGION = "persist.sys.oost.oos_localizer_region"
    private const val PERSIST_PROP_KEY_OOS_LOCALIZER_LOCALE = "persist.sys.oost.oos_localizer_locale"
    private const val PERSIST_PROP_KEY_OOS_LOCALIZER_MODEL = "persist.sys.oost.oos_localizer_model"
    private const val SETTINGS_KEY_NATIVE_NOTIFY_ICON = "oost_native_notify_icon"
    private const val SETTINGS_KEY_EXTREME_REFRESH_165 = "oost_extreme_refresh_165"
    private const val SETTINGS_KEY_RECENT_TASK_RADIUS = "oost_recent_task_radius"
    private const val SETTINGS_KEY_AOD_ENHANCE = "oost_aod_enhance"
    private const val SETTINGS_KEY_OOS_LOCALIZER = "oost_oos_localizer"
    private const val SETTINGS_KEY_ASSISTANT_POWER_MODE = "oost_assistant_power_mode"
    private const val SETTINGS_KEY_ASSISTANT_GESTURE_CIRCLE = "oost_assistant_gesture_circle"
    private const val SETTINGS_KEY_RECENT_TASK_RADIUS_DP = "oost_recent_task_radius_dp"
    private const val SETTINGS_KEY_AOD_INIT_DARK_BRIGHTNESS = "oost_aod_init_dark_brightness"
    private const val SETTINGS_KEY_AOD_INIT_BRIGHT_BRIGHTNESS = "oost_aod_init_bright_brightness"
    private const val SETTINGS_KEY_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = "oost_aod_running_brightness_multiplier"
    private const val SETTINGS_KEY_AOD_PANORAMIC_SUPPORT = "oost_aod_panoramic_support"
    private const val SETTINGS_KEY_AOD_SETTINGS_SWITCH = "oost_aod_settings_switch"
    private const val SETTINGS_KEY_AOD_SINGLE_CLICK_BLOCK = "oost_aod_single_click_block"
    private const val SETTINGS_KEY_NATIVE_NOTIFICATION_BUBBLES = "oost_native_notification_bubbles"
    private const val SETTINGS_KEY_STATUS_MOBILE_TYPE_ENABLED = "oost_status_mobile_type"
    private const val SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF = "oost_status_mobile_type_hide_data_off"
    private const val SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI = "oost_status_mobile_type_hide_wifi"
    private const val SETTINGS_KEY_SYSTEMUI_HIDE_QS_EDIT = "oost_systemui_hide_qs_edit"
    private const val SETTINGS_KEY_SYSTEMUI_HIDE_QS_SETTINGS = "oost_systemui_hide_qs_settings"
    private const val SETTINGS_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER = "oost_systemui_hide_qs_top_carrier"
    private const val SETTINGS_KEY_SYSTEMUI_HIDE_QS_MORE = "oost_systemui_hide_qs_more"
    private const val SETTINGS_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY = "oost_systemui_force_native_clipboard_overlay"
    private const val SETTINGS_KEY_SETTINGS_FORCE_GOOGLE_ENTRY = "oost_settings_force_google_entry"
    private const val SETTINGS_KEY_LAUNCHER_REGION_MODE = "oost_launcher_region_mode"
    private const val SETTINGS_KEY_OOS_LOCALIZER_CONFIG_MODE = "oost_oos_localizer_config_mode"
    private const val SETTINGS_KEY_OOS_LOCALIZER_REGION = "oost_oos_localizer_region"
    private const val SETTINGS_KEY_OOS_LOCALIZER_LOCALE = "oost_oos_localizer_locale"
    private const val SETTINGS_KEY_OOS_LOCALIZER_MODEL = "oost_oos_localizer_model"

    private const val DEFAULT_RECENT_TASK_RADIUS_DP = 26
    private const val DEFAULT_AOD_INIT_DARK_BRIGHTNESS = 80
    private const val DEFAULT_AOD_INIT_BRIGHT_BRIGHTNESS = 160
    private const val DEFAULT_AOD_RUNNING_BRIGHTNESS_MULTIPLIER = 1.6f
    private const val DEFAULT_AOD_PANORAMIC_SUPPORT = true
    private const val DEFAULT_AOD_SETTINGS_SWITCH = true
    private const val DEFAULT_AOD_SINGLE_CLICK_BLOCK = true
    private const val DEFAULT_NATIVE_NOTIFICATION_BUBBLES = false
    private const val DEFAULT_STATUS_MOBILE_TYPE_ENABLED = false
    private const val DEFAULT_STATUS_MOBILE_TYPE_HIDE_DATA_OFF = false
    private const val DEFAULT_STATUS_MOBILE_TYPE_HIDE_WIFI = false
    private const val DEFAULT_SYSTEMUI_HIDE_QS_EDIT = false
    private const val DEFAULT_SYSTEMUI_HIDE_QS_SETTINGS = false
    private const val DEFAULT_SYSTEMUI_HIDE_QS_TOP_CARRIER = false
    private const val DEFAULT_SYSTEMUI_HIDE_QS_MORE = false
    private const val DEFAULT_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY = false
    private const val DEFAULT_SETTINGS_FORCE_GOOGLE_ENTRY = false
    const val LAUNCHER_REGION_MODE_OFF = 0
    const val LAUNCHER_REGION_MODE_CN = 1
    const val LAUNCHER_REGION_MODE_IN = 2
    private const val DEFAULT_LAUNCHER_REGION_MODE = LAUNCHER_REGION_MODE_OFF
    const val ASSISTANT_POWER_MODE_NONE = -1
    const val ASSISTANT_POWER_MODE_GEMINI = 0
    private const val DEFAULT_ASSISTANT_POWER_MODE = ASSISTANT_POWER_MODE_NONE
    const val DEFAULT_OOS_LOCALIZER_REGION = "CN"
    const val DEFAULT_OOS_LOCALIZER_LOCALE = "zh-CN"
    const val DEFAULT_OOS_LOCALIZER_MODEL = "PMA120"
    const val OOS_LOCALIZER_CONFIG_DEFAULT = 0
    const val OOS_LOCALIZER_CONFIG_CUSTOM = 1
    private const val DEFAULT_OOS_LOCALIZER_CONFIG_MODE = OOS_LOCALIZER_CONFIG_DEFAULT
    const val OOS_LOCALIZER_FEATURE_PROPERTIES = "properties"
    const val OOS_LOCALIZER_FEATURE_REGION = "region"
    const val OOS_LOCALIZER_FEATURE_LOCALE = "locale"
    const val OOS_LOCALIZER_FEATURE_BUILD_MODEL = "build_model"
    const val OOS_LOCALIZER_FEATURE_APP_FEATURES = "app_features"

    val OOS_LOCALIZER_PROPERTY_DEFAULTS = linkedMapOf(
        "ro.oplus.image.system_ext.area" to "domestic",
        "ro.oplus.image.my_stock.type" to "domestic_OPPO",
        "ro.build.display.id" to "PMA120_16.0.7.210(CN01)",
        "ro.build.display.full_id" to "PMA120domestic_11_16.0.7.210(CN01)_2026051318470000",
        "ro.build.version.ota" to "PMA120_11.A.45_0450_202605131847",
        "ro.oplus.image.my_manifest.version" to "PMA120_11.A.45_0450_202605131847.97.41d84fe6",
        "ro.build.display.ota" to "PMA120_11_A.45",
        "ro.product.authentication" to "26C44PC2V997",
        "persist.bluetooth.airpods_support" to "true"
    )

    val OOS_LOCALIZER_APP_FEATURE_DEFAULTS = linkedMapOf(
        "com.android.incallui.region_cn" to "true",
        "com.android.launcher.CN_VERSION" to "true",
        "com.android.settings.cn_version" to "true",
        "com.oplusos.deepthinker.cn.enable" to "true",
        "com.oplus.aiwriter.main_host_address" to "String:aitool-infer-cn.heytapmobi.com",
        "com.oplus.smartanalysis.rule_server_host" to "String:https://iwisdom.apps.coloros.com"
    )

    val OOS_LOCALIZER_FEATURE_DEFAULTS = linkedMapOf(
        OOS_LOCALIZER_FEATURE_PROPERTIES to true,
        OOS_LOCALIZER_FEATURE_REGION to true,
        OOS_LOCALIZER_FEATURE_LOCALE to true,
        OOS_LOCALIZER_FEATURE_BUILD_MODEL to true,
        OOS_LOCALIZER_FEATURE_APP_FEATURES to true
    )

    data class UiSnapshot(
        val nativeNotifyIconEnabled: Boolean,
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
        val statusMobileTypeEnabled: Boolean,
        val statusMobileTypeHideDataOffEnabled: Boolean,
        val statusMobileTypeHideWifiEnabled: Boolean,
        val systemUiHideQsEditEnabled: Boolean,
        val systemUiHideQsSettingsEnabled: Boolean,
        val systemUiHideQsTopCarrierEnabled: Boolean,
        val systemUiHideQsMoreEnabled: Boolean,
        val systemUiForceNativeClipboardOverlayEnabled: Boolean,
        val settingsForceGoogleEntryEnabled: Boolean,
        val oosLocalizerEnabled: Boolean,
        val oosLocalizerConfigMode: Int,
        val oosLocalizerRegion: String,
        val oosLocalizerLocale: String,
        val oosLocalizerModel: String,
        val assistantPowerMode: Int,
        val assistantGestureCircleEnabled: Boolean
    )

    fun readCachedUiSnapshot(context: Context): UiSnapshot {
        val prefs = prefs(context)
        return UiSnapshot(
            nativeNotifyIconEnabled = prefs.getBoolean(KEY_NATIVE_NOTIFY_ICON, true),
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
            statusMobileTypeEnabled = prefs.getBoolean(
                KEY_STATUS_MOBILE_TYPE_ENABLED,
                DEFAULT_STATUS_MOBILE_TYPE_ENABLED
            ),
            statusMobileTypeHideDataOffEnabled = prefs.getBoolean(
                KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
                DEFAULT_STATUS_MOBILE_TYPE_HIDE_DATA_OFF
            ),
            statusMobileTypeHideWifiEnabled = prefs.getBoolean(
                KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
                DEFAULT_STATUS_MOBILE_TYPE_HIDE_WIFI
            ),
            systemUiHideQsEditEnabled = prefs.getBoolean(
                KEY_SYSTEMUI_HIDE_QS_EDIT,
                DEFAULT_SYSTEMUI_HIDE_QS_EDIT
            ),
            systemUiHideQsSettingsEnabled = prefs.getBoolean(
                KEY_SYSTEMUI_HIDE_QS_SETTINGS,
                DEFAULT_SYSTEMUI_HIDE_QS_SETTINGS
            ),
            systemUiHideQsTopCarrierEnabled = prefs.getBoolean(
                KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
                DEFAULT_SYSTEMUI_HIDE_QS_TOP_CARRIER
            ),
            systemUiHideQsMoreEnabled = prefs.getBoolean(
                KEY_SYSTEMUI_HIDE_QS_MORE,
                DEFAULT_SYSTEMUI_HIDE_QS_MORE
            ),
            systemUiForceNativeClipboardOverlayEnabled = prefs.getBoolean(
                KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
                DEFAULT_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY
            ),
            settingsForceGoogleEntryEnabled = prefs.getBoolean(
                KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
                DEFAULT_SETTINGS_FORCE_GOOGLE_ENTRY
            ),
            oosLocalizerEnabled = prefs.getBoolean(KEY_OOS_LOCALIZER, false),
            oosLocalizerConfigMode = prefs.getInt(
                KEY_OOS_LOCALIZER_CONFIG_MODE,
                DEFAULT_OOS_LOCALIZER_CONFIG_MODE
            ).sanitizeOosLocalizerConfigMode(),
            oosLocalizerRegion = prefs.getString(
                KEY_OOS_LOCALIZER_REGION,
                DEFAULT_OOS_LOCALIZER_REGION
            ).sanitizeOosLocalizerRegion(),
            oosLocalizerLocale = prefs.getString(
                KEY_OOS_LOCALIZER_LOCALE,
                DEFAULT_OOS_LOCALIZER_LOCALE
            ).sanitizeOosLocalizerLocale(),
            oosLocalizerModel = prefs.getString(
                KEY_OOS_LOCALIZER_MODEL,
                DEFAULT_OOS_LOCALIZER_MODEL
            ).sanitizeOosLocalizerModel(),
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
            statusMobileTypeEnabled = isStatusMobileTypeEnabled(context),
            statusMobileTypeHideDataOffEnabled = isStatusMobileTypeHideDataOffEnabled(context),
            statusMobileTypeHideWifiEnabled = isStatusMobileTypeHideWifiEnabled(context),
            systemUiHideQsEditEnabled = isSystemUiHideQsEditEnabled(context),
            systemUiHideQsSettingsEnabled = isSystemUiHideQsSettingsEnabled(context),
            systemUiHideQsTopCarrierEnabled = isSystemUiHideQsTopCarrierEnabled(context),
            systemUiHideQsMoreEnabled = isSystemUiHideQsMoreEnabled(context),
            systemUiForceNativeClipboardOverlayEnabled = isSystemUiForceNativeClipboardOverlayEnabled(context),
            settingsForceGoogleEntryEnabled = isSettingsForceGoogleEntryEnabled(context),
            oosLocalizerEnabled = isOosLocalizerEnabled(context),
            oosLocalizerConfigMode = getOosLocalizerConfigMode(context),
            oosLocalizerRegion = getOosLocalizerRegion(context),
            oosLocalizerLocale = getOosLocalizerLocale(context),
            oosLocalizerModel = getOosLocalizerModel(context),
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

    fun getOosLocalizerConfigMode(context: Context): Int {
        return readSyncedInt(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_OOS_LOCALIZER_CONFIG_MODE,
            propertyKey = PROP_KEY_OOS_LOCALIZER_CONFIG_MODE,
            settingsKey = SETTINGS_KEY_OOS_LOCALIZER_CONFIG_MODE,
            prefsKey = KEY_OOS_LOCALIZER_CONFIG_MODE,
            defaultValue = DEFAULT_OOS_LOCALIZER_CONFIG_MODE
        ).sanitizeOosLocalizerConfigMode()
    }

    fun setOosLocalizerConfigMode(context: Context, mode: Int) {
        val sanitized = mode.sanitizeOosLocalizerConfigMode()
        prefs(context).edit().putInt(KEY_OOS_LOCALIZER_CONFIG_MODE, sanitized).commit()
        syncReadableState(context)
        syncScalarState(
            value = sanitized.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER_CONFIG_MODE,
                PROP_KEY_OOS_LOCALIZER_CONFIG_MODE
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER_CONFIG_MODE
        )
    }

    fun getOosLocalizerRegion(context: Context): String {
        return readSyncedString(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_OOS_LOCALIZER_REGION,
            propertyKey = PROP_KEY_OOS_LOCALIZER_REGION,
            settingsKey = SETTINGS_KEY_OOS_LOCALIZER_REGION,
            prefsKey = KEY_OOS_LOCALIZER_REGION,
            defaultValue = DEFAULT_OOS_LOCALIZER_REGION
        ).sanitizeOosLocalizerRegion()
    }

    fun setOosLocalizerRegion(context: Context, value: String) {
        val sanitized = value.sanitizeOosLocalizerRegion()
        prefs(context).edit().putString(KEY_OOS_LOCALIZER_REGION, sanitized).commit()
        syncReadableState(context)
        syncScalarState(
            value = sanitized,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER_REGION,
                PROP_KEY_OOS_LOCALIZER_REGION
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER_REGION
        )
    }

    fun getOosLocalizerLocale(context: Context): String {
        return readSyncedString(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_OOS_LOCALIZER_LOCALE,
            propertyKey = PROP_KEY_OOS_LOCALIZER_LOCALE,
            settingsKey = SETTINGS_KEY_OOS_LOCALIZER_LOCALE,
            prefsKey = KEY_OOS_LOCALIZER_LOCALE,
            defaultValue = DEFAULT_OOS_LOCALIZER_LOCALE
        ).sanitizeOosLocalizerLocale()
    }

    fun setOosLocalizerLocale(context: Context, value: String) {
        val sanitized = value.sanitizeOosLocalizerLocale()
        prefs(context).edit().putString(KEY_OOS_LOCALIZER_LOCALE, sanitized).commit()
        syncReadableState(context)
        syncScalarState(
            value = sanitized,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER_LOCALE,
                PROP_KEY_OOS_LOCALIZER_LOCALE
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER_LOCALE
        )
    }

    fun getOosLocalizerModel(context: Context): String {
        return readSyncedString(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_OOS_LOCALIZER_MODEL,
            propertyKey = PROP_KEY_OOS_LOCALIZER_MODEL,
            settingsKey = SETTINGS_KEY_OOS_LOCALIZER_MODEL,
            prefsKey = KEY_OOS_LOCALIZER_MODEL,
            defaultValue = DEFAULT_OOS_LOCALIZER_MODEL
        ).sanitizeOosLocalizerModel()
    }

    fun setOosLocalizerModel(context: Context, value: String) {
        val sanitized = value.sanitizeOosLocalizerModel()
        prefs(context).edit().putString(KEY_OOS_LOCALIZER_MODEL, sanitized).commit()
        syncReadableState(context)
        syncScalarState(
            value = sanitized,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER_MODEL,
                PROP_KEY_OOS_LOCALIZER_MODEL
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER_MODEL
        )
    }

    fun getOosLocalizerProperty(context: Context, key: String): String {
        val defaultValue = OOS_LOCALIZER_PROPERTY_DEFAULTS[key].orEmpty()
        return prefs(context).getString(KEY_OOS_LOCALIZER_PROPERTY_PREFIX + key, defaultValue)
            ?: defaultValue
    }

    fun setOosLocalizerProperty(context: Context, key: String, value: String) {
        if (key !in OOS_LOCALIZER_PROPERTY_DEFAULTS) return
        prefs(context).edit()
            .putString(KEY_OOS_LOCALIZER_PROPERTY_PREFIX + key, value.trim())
            .commit()
        syncReadableState(context)
    }

    fun getOosLocalizerAppFeature(context: Context, key: String): String {
        val defaultValue = OOS_LOCALIZER_APP_FEATURE_DEFAULTS[key].orEmpty()
        return prefs(context).getString(KEY_OOS_LOCALIZER_APP_FEATURE_PREFIX + key, defaultValue)
            ?: defaultValue
    }

    fun setOosLocalizerAppFeature(context: Context, key: String, value: String) {
        if (key !in OOS_LOCALIZER_APP_FEATURE_DEFAULTS) return
        prefs(context).edit()
            .putString(KEY_OOS_LOCALIZER_APP_FEATURE_PREFIX + key, value.trim())
            .commit()
        syncReadableState(context)
    }

    fun isOosLocalizerPackageEnabled(context: Context, packageName: String): Boolean {
        return packageName !in getStringSet(context, KEY_OOS_LOCALIZER_DISABLED_PACKAGES)
    }

    fun setOosLocalizerPackageEnabled(context: Context, packageName: String, enabled: Boolean) {
        val disabled = getStringSet(context, KEY_OOS_LOCALIZER_DISABLED_PACKAGES).toMutableSet()
        if (enabled) {
            disabled.remove(packageName)
        } else {
            disabled.add(packageName)
        }
        prefs(context).edit().putStringSet(KEY_OOS_LOCALIZER_DISABLED_PACKAGES, disabled.toSet()).commit()
        syncReadableState(context)
    }

    fun isOosLocalizerFeatureEnabled(context: Context, feature: String): Boolean {
        val defaultValue = OOS_LOCALIZER_FEATURE_DEFAULTS[feature] ?: true
        if (defaultValue) {
            return feature !in getStringSet(context, KEY_OOS_LOCALIZER_DISABLED_FEATURES)
        }
        return feature in getStringSet(context, KEY_OOS_LOCALIZER_DISABLED_FEATURES)
    }

    fun setOosLocalizerFeatureEnabled(context: Context, feature: String, enabled: Boolean) {
        if (feature !in OOS_LOCALIZER_FEATURE_DEFAULTS) return
        val disabled = getStringSet(context, KEY_OOS_LOCALIZER_DISABLED_FEATURES).toMutableSet()
        if (enabled) {
            disabled.remove(feature)
        } else {
            disabled.add(feature)
        }
        prefs(context).edit().putStringSet(KEY_OOS_LOCALIZER_DISABLED_FEATURES, disabled.toSet()).commit()
        syncReadableState(context)
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

    fun isStatusMobileTypeEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_ENABLED,
            propertyKey = PROP_KEY_STATUS_MOBILE_TYPE_ENABLED,
            settingsKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_ENABLED,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_STATUS_MOBILE_TYPE_ENABLED,
            defaultValue = DEFAULT_STATUS_MOBILE_TYPE_ENABLED
        )
    }

    fun setStatusMobileTypeEnabled(context: Context, enabled: Boolean) {
        setSyncedBooleanPreference(
            context = context,
            prefsKey = KEY_STATUS_MOBILE_TYPE_ENABLED,
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_ENABLED,
                PROP_KEY_STATUS_MOBILE_TYPE_ENABLED
            ),
            settingsGlobalKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_ENABLED
        )
    }

    fun isStatusMobileTypeHideDataOffEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
            propertyKey = PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
            settingsKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
            defaultValue = DEFAULT_STATUS_MOBILE_TYPE_HIDE_DATA_OFF
        )
    }

    fun setStatusMobileTypeHideDataOffEnabled(context: Context, enabled: Boolean) {
        setSyncedBooleanPreference(
            context = context,
            prefsKey = KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
                PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF
            ),
            settingsGlobalKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF
        )
    }

    fun isStatusMobileTypeHideWifiEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
            propertyKey = PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
            settingsKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
            defaultValue = DEFAULT_STATUS_MOBILE_TYPE_HIDE_WIFI
        )
    }

    fun setStatusMobileTypeHideWifiEnabled(context: Context, enabled: Boolean) {
        setSyncedBooleanPreference(
            context = context,
            prefsKey = KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
                PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI
            ),
            settingsGlobalKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI
        )
    }

    fun isSystemUiHideQsEditEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_EDIT,
            propertyKey = PROP_KEY_SYSTEMUI_HIDE_QS_EDIT,
            settingsKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_EDIT,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_EDIT,
            defaultValue = DEFAULT_SYSTEMUI_HIDE_QS_EDIT
        )
    }

    fun setSystemUiHideQsEditEnabled(context: Context, enabled: Boolean) {
        setSyncedBooleanPreference(
            context = context,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_EDIT,
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_EDIT,
                PROP_KEY_SYSTEMUI_HIDE_QS_EDIT
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_EDIT
        )
    }

    fun isSystemUiHideQsSettingsEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS,
            propertyKey = PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS,
            settingsKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_SETTINGS,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_SETTINGS,
            defaultValue = DEFAULT_SYSTEMUI_HIDE_QS_SETTINGS
        )
    }

    fun setSystemUiHideQsSettingsEnabled(context: Context, enabled: Boolean) {
        setSyncedBooleanPreference(
            context = context,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_SETTINGS,
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS,
                PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_SETTINGS
        )
    }

    fun isSystemUiHideQsTopCarrierEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
            propertyKey = PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
            settingsKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
            defaultValue = DEFAULT_SYSTEMUI_HIDE_QS_TOP_CARRIER
        )
    }

    fun setSystemUiHideQsTopCarrierEnabled(context: Context, enabled: Boolean) {
        setSyncedBooleanPreference(
            context = context,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
                PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER
        )
    }

    fun isSystemUiHideQsMoreEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_MORE,
            propertyKey = PROP_KEY_SYSTEMUI_HIDE_QS_MORE,
            settingsKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_MORE,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_MORE,
            defaultValue = DEFAULT_SYSTEMUI_HIDE_QS_MORE
        )
    }

    fun setSystemUiHideQsMoreEnabled(context: Context, enabled: Boolean) {
        setSyncedBooleanPreference(
            context = context,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_MORE,
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_MORE,
                PROP_KEY_SYSTEMUI_HIDE_QS_MORE
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_MORE
        )
    }

    fun isSystemUiForceNativeClipboardOverlayEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
            propertyKey = PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
            settingsKey = SETTINGS_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
            defaultValue = DEFAULT_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY
        )
    }

    fun setSystemUiForceNativeClipboardOverlayEnabled(context: Context, enabled: Boolean) {
        setSyncedBooleanPreference(
            context = context,
            prefsKey = KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
                PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY
        )
    }

    fun isSettingsForceGoogleEntryEnabled(context: Context): Boolean {
        return readSyncedToggle(
            context = context,
            persistPropertyKey = PERSIST_PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
            propertyKey = PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
            settingsKey = SETTINGS_KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
            flagFilePath = null,
            legacyFlagFilePath = null,
            prefsKey = KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
            defaultValue = DEFAULT_SETTINGS_FORCE_GOOGLE_ENTRY
        )
    }

    fun setSettingsForceGoogleEntryEnabled(context: Context, enabled: Boolean) {
        setSyncedBooleanPreference(
            context = context,
            prefsKey = KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
            enabled = enabled,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
                PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY
            ),
            settingsGlobalKey = SETTINGS_KEY_SETTINGS_FORCE_GOOGLE_ENTRY
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
        val assistantPowerMode = getAssistantPowerMode(context)
        val assistantGestureCircleEnabled = isAssistantGestureCircleEnabled(context)
        val recentTaskRadiusDp = getRecentTaskRadiusDp(context)
        val aodInitDarkBrightness = getAodInitDarkBrightness(context)
        val aodInitBrightBrightness = getAodInitBrightBrightness(context)
        val aodRunningMultiplier = getAodRunningBrightnessMultiplier(context)
        val aodPanoramicSupport = isAodPanoramicSupportEnabled(context)
        val aodSettingsSwitch = isAodSettingsSwitchEnabled(context)
        val aodSingleClickBlock = isAodSingleClickBlockEnabled(context)
        val nativeNotificationBubbles = isNativeNotificationBubblesEnabled(context)
        val statusMobileType = isStatusMobileTypeEnabled(context)
        val statusMobileTypeHideDataOff = isStatusMobileTypeHideDataOffEnabled(context)
        val statusMobileTypeHideWifi = isStatusMobileTypeHideWifiEnabled(context)
        val systemUiHideQsEdit = isSystemUiHideQsEditEnabled(context)
        val systemUiHideQsSettings = isSystemUiHideQsSettingsEnabled(context)
        val systemUiHideQsTopCarrier = isSystemUiHideQsTopCarrierEnabled(context)
        val systemUiHideQsMore = isSystemUiHideQsMoreEnabled(context)
        val systemUiForceNativeClipboardOverlay = isSystemUiForceNativeClipboardOverlayEnabled(context)
        val settingsForceGoogleEntry = isSettingsForceGoogleEntryEnabled(context)
        val launcherRegionMode = getLauncherRegionMode(context)
        val oosLocalizerConfigMode = getOosLocalizerConfigMode(context)
        val oosLocalizerRegion = getOosLocalizerRegion(context)
        val oosLocalizerLocale = getOosLocalizerLocale(context)
        val oosLocalizerModel = getOosLocalizerModel(context)
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
            value = if (statusMobileType) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_ENABLED,
                PROP_KEY_STATUS_MOBILE_TYPE_ENABLED
            ),
            settingsGlobalKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_ENABLED
        )
        syncScalarState(
            value = if (statusMobileTypeHideDataOff) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
                PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF
            ),
            settingsGlobalKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF
        )
        syncScalarState(
            value = if (statusMobileTypeHideWifi) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
                PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI
            ),
            settingsGlobalKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI
        )
        syncScalarState(
            value = if (systemUiHideQsEdit) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_EDIT,
                PROP_KEY_SYSTEMUI_HIDE_QS_EDIT
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_EDIT
        )
        syncScalarState(
            value = if (systemUiHideQsSettings) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS,
                PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_SETTINGS
        )
        syncScalarState(
            value = if (systemUiHideQsTopCarrier) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
                PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER
        )
        syncScalarState(
            value = if (systemUiHideQsMore) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_MORE,
                PROP_KEY_SYSTEMUI_HIDE_QS_MORE
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_MORE
        )
        syncScalarState(
            value = if (systemUiForceNativeClipboardOverlay) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
                PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY
            ),
            settingsGlobalKey = SETTINGS_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY
        )
        syncScalarState(
            value = if (settingsForceGoogleEntry) "1" else "0",
            propertyKeys = listOf(
                PERSIST_PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
                PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY
            ),
            settingsGlobalKey = SETTINGS_KEY_SETTINGS_FORCE_GOOGLE_ENTRY
        )
        syncScalarState(
            value = launcherRegionMode.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_LAUNCHER_REGION_MODE,
                PROP_KEY_LAUNCHER_REGION_MODE
            ),
            settingsGlobalKey = SETTINGS_KEY_LAUNCHER_REGION_MODE
        )
        syncScalarState(
            value = oosLocalizerConfigMode.toString(),
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER_CONFIG_MODE,
                PROP_KEY_OOS_LOCALIZER_CONFIG_MODE
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER_CONFIG_MODE
        )
        syncScalarState(
            value = oosLocalizerRegion,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER_REGION,
                PROP_KEY_OOS_LOCALIZER_REGION
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER_REGION
        )
        syncScalarState(
            value = oosLocalizerLocale,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER_LOCALE,
                PROP_KEY_OOS_LOCALIZER_LOCALE
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER_LOCALE
        )
        syncScalarState(
            value = oosLocalizerModel,
            propertyKeys = listOf(
                PERSIST_PROP_KEY_OOS_LOCALIZER_MODEL,
                PROP_KEY_OOS_LOCALIZER_MODEL
            ),
            settingsGlobalKey = SETTINGS_KEY_OOS_LOCALIZER_MODEL
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

    fun getOosLocalizerRegionXposed(): String {
        readSystemPropertyValue(PERSIST_PROP_KEY_OOS_LOCALIZER_REGION)?.let {
            return it.sanitizeOosLocalizerRegion()
        }
        readSystemPropertyValue(PROP_KEY_OOS_LOCALIZER_REGION)?.let {
            return it.sanitizeOosLocalizerRegion()
        }
        readSettingsGlobalValue(SETTINGS_KEY_OOS_LOCALIZER_REGION)?.let {
            return it.sanitizeOosLocalizerRegion()
        }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getString(KEY_OOS_LOCALIZER_REGION, DEFAULT_OOS_LOCALIZER_REGION)
        }.getOrDefault(DEFAULT_OOS_LOCALIZER_REGION).sanitizeOosLocalizerRegion()
    }

    fun getOosLocalizerConfigModeXposed(): Int {
        readSystemPropertyValue(PERSIST_PROP_KEY_OOS_LOCALIZER_CONFIG_MODE)?.toIntOrNull()?.let {
            return it.sanitizeOosLocalizerConfigMode()
        }
        readSystemPropertyValue(PROP_KEY_OOS_LOCALIZER_CONFIG_MODE)?.toIntOrNull()?.let {
            return it.sanitizeOosLocalizerConfigMode()
        }
        readSettingsGlobalValue(SETTINGS_KEY_OOS_LOCALIZER_CONFIG_MODE)?.toIntOrNull()?.let {
            return it.sanitizeOosLocalizerConfigMode()
        }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getInt(KEY_OOS_LOCALIZER_CONFIG_MODE, DEFAULT_OOS_LOCALIZER_CONFIG_MODE)
        }.getOrDefault(DEFAULT_OOS_LOCALIZER_CONFIG_MODE).sanitizeOosLocalizerConfigMode()
    }

    fun getOosLocalizerLocaleXposed(): String {
        readSystemPropertyValue(PERSIST_PROP_KEY_OOS_LOCALIZER_LOCALE)?.let {
            return it.sanitizeOosLocalizerLocale()
        }
        readSystemPropertyValue(PROP_KEY_OOS_LOCALIZER_LOCALE)?.let {
            return it.sanitizeOosLocalizerLocale()
        }
        readSettingsGlobalValue(SETTINGS_KEY_OOS_LOCALIZER_LOCALE)?.let {
            return it.sanitizeOosLocalizerLocale()
        }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getString(KEY_OOS_LOCALIZER_LOCALE, DEFAULT_OOS_LOCALIZER_LOCALE)
        }.getOrDefault(DEFAULT_OOS_LOCALIZER_LOCALE).sanitizeOosLocalizerLocale()
    }

    fun getOosLocalizerModelXposed(): String {
        readSystemPropertyValue(PERSIST_PROP_KEY_OOS_LOCALIZER_MODEL)?.let {
            return it.sanitizeOosLocalizerModel()
        }
        readSystemPropertyValue(PROP_KEY_OOS_LOCALIZER_MODEL)?.let {
            return it.sanitizeOosLocalizerModel()
        }
        readSettingsGlobalValue(SETTINGS_KEY_OOS_LOCALIZER_MODEL)?.let {
            return it.sanitizeOosLocalizerModel()
        }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getString(KEY_OOS_LOCALIZER_MODEL, DEFAULT_OOS_LOCALIZER_MODEL)
        }.getOrDefault(DEFAULT_OOS_LOCALIZER_MODEL).sanitizeOosLocalizerModel()
    }

    fun getOosLocalizerPropertyXposed(key: String): String? {
        val defaultValue = OOS_LOCALIZER_PROPERTY_DEFAULTS[key] ?: return null
        return readXposedString(KEY_OOS_LOCALIZER_PROPERTY_PREFIX + key, defaultValue)
            ?.takeIf { it.isNotBlank() }
    }

    fun getOosLocalizerAppFeatureXposed(key: String): String? {
        val defaultValue = OOS_LOCALIZER_APP_FEATURE_DEFAULTS[key] ?: return null
        return readXposedString(KEY_OOS_LOCALIZER_APP_FEATURE_PREFIX + key, defaultValue)
            ?.takeIf { it.isNotBlank() }
    }

    fun isOosLocalizerPackageEnabledXposed(packageName: String): Boolean {
        return packageName !in readXposedStringSet(KEY_OOS_LOCALIZER_DISABLED_PACKAGES)
    }

    fun isOosLocalizerFeatureEnabledXposed(feature: String): Boolean {
        val defaultValue = OOS_LOCALIZER_FEATURE_DEFAULTS[feature] ?: true
        val disabled = readXposedStringSet(KEY_OOS_LOCALIZER_DISABLED_FEATURES)
        return if (defaultValue) feature !in disabled else feature in disabled
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

    fun isStatusMobileTypeEnabledXposed(): Boolean {
        return readXposedBoolean(
            persistPropertyKey = PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_ENABLED,
            propertyKey = PROP_KEY_STATUS_MOBILE_TYPE_ENABLED,
            settingsKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_ENABLED,
            prefsKey = KEY_STATUS_MOBILE_TYPE_ENABLED,
            defaultValue = DEFAULT_STATUS_MOBILE_TYPE_ENABLED
        )
    }

    fun isStatusMobileTypeHideDataOffEnabledXposed(): Boolean {
        return readXposedBoolean(
            persistPropertyKey = PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
            propertyKey = PROP_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
            settingsKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
            prefsKey = KEY_STATUS_MOBILE_TYPE_HIDE_DATA_OFF,
            defaultValue = DEFAULT_STATUS_MOBILE_TYPE_HIDE_DATA_OFF
        )
    }

    fun isStatusMobileTypeHideWifiEnabledXposed(): Boolean {
        return readXposedBoolean(
            persistPropertyKey = PERSIST_PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
            propertyKey = PROP_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
            settingsKey = SETTINGS_KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
            prefsKey = KEY_STATUS_MOBILE_TYPE_HIDE_WIFI,
            defaultValue = DEFAULT_STATUS_MOBILE_TYPE_HIDE_WIFI
        )
    }

    fun isSystemUiHideQsEditEnabledXposed(): Boolean {
        return readXposedBoolean(
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_EDIT,
            propertyKey = PROP_KEY_SYSTEMUI_HIDE_QS_EDIT,
            settingsKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_EDIT,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_EDIT,
            defaultValue = DEFAULT_SYSTEMUI_HIDE_QS_EDIT
        )
    }

    fun isSystemUiHideQsSettingsEnabledXposed(): Boolean {
        return readXposedBoolean(
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS,
            propertyKey = PROP_KEY_SYSTEMUI_HIDE_QS_SETTINGS,
            settingsKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_SETTINGS,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_SETTINGS,
            defaultValue = DEFAULT_SYSTEMUI_HIDE_QS_SETTINGS
        )
    }

    fun isSystemUiHideQsTopCarrierEnabledXposed(): Boolean {
        return readXposedBoolean(
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
            propertyKey = PROP_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
            settingsKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_TOP_CARRIER,
            defaultValue = DEFAULT_SYSTEMUI_HIDE_QS_TOP_CARRIER
        )
    }

    fun isSystemUiHideQsMoreEnabledXposed(): Boolean {
        return readXposedBoolean(
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_HIDE_QS_MORE,
            propertyKey = PROP_KEY_SYSTEMUI_HIDE_QS_MORE,
            settingsKey = SETTINGS_KEY_SYSTEMUI_HIDE_QS_MORE,
            prefsKey = KEY_SYSTEMUI_HIDE_QS_MORE,
            defaultValue = DEFAULT_SYSTEMUI_HIDE_QS_MORE
        )
    }

    fun isSystemUiForceNativeClipboardOverlayEnabledXposed(): Boolean {
        return readXposedBoolean(
            persistPropertyKey = PERSIST_PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
            propertyKey = PROP_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
            settingsKey = SETTINGS_KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
            prefsKey = KEY_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY,
            defaultValue = DEFAULT_SYSTEMUI_FORCE_NATIVE_CLIPBOARD_OVERLAY
        )
    }

    fun isSettingsForceGoogleEntryEnabledXposed(): Boolean {
        return readXposedBoolean(
            persistPropertyKey = PERSIST_PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
            propertyKey = PROP_KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
            settingsKey = SETTINGS_KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
            prefsKey = KEY_SETTINGS_FORCE_GOOGLE_ENTRY,
            defaultValue = DEFAULT_SETTINGS_FORCE_GOOGLE_ENTRY
        )
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

    private fun readSyncedString(
        context: Context,
        persistPropertyKey: String,
        propertyKey: String,
        settingsKey: String,
        prefsKey: String,
        defaultValue: String
    ): String {
        readSystemPropertyValue(persistPropertyKey)?.let { return it }
        readSystemPropertyValue(propertyKey)?.let { return it }
        readSettingsGlobalValue(settingsKey)?.let { return it }
        return prefs(context).getString(prefsKey, defaultValue) ?: defaultValue
    }

    private fun setSyncedBooleanPreference(
        context: Context,
        prefsKey: String,
        enabled: Boolean,
        propertyKeys: List<String>,
        settingsGlobalKey: String
    ) {
        prefs(context).edit().putBoolean(prefsKey, enabled).commit()
        syncReadableState(context)
        syncScalarState(
            value = if (enabled) "1" else "0",
            propertyKeys = propertyKeys,
            settingsGlobalKey = settingsGlobalKey
        )
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

    private fun getStringSet(context: Context, key: String): Set<String> {
        return prefs(context).getStringSet(key, emptySet()).orEmpty()
    }

    private fun readXposedString(key: String, defaultValue: String): String? {
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getString(key, defaultValue)
        }.getOrDefault(defaultValue)
    }

    private fun readXposedStringSet(key: String): Set<String> {
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getStringSet(key, emptySet()).orEmpty()
        }.getOrDefault(emptySet())
    }

    private fun readXposedBoolean(
        persistPropertyKey: String,
        propertyKey: String,
        settingsKey: String,
        prefsKey: String,
        defaultValue: Boolean
    ): Boolean {
        readSystemPropertyToggle(persistPropertyKey)?.let { return it }
        readSystemPropertyToggle(propertyKey)?.let { return it }
        readSettingsGlobalToggle(settingsKey)?.let { return it }
        return runCatching {
            val prefs = XSharedPreferences(MODULE_PACKAGE, PREFS_NAME)
            prefs.makeWorldReadable()
            prefs.reload()
            prefs.getBoolean(prefsKey, defaultValue)
        }.getOrDefault(defaultValue)
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

    private fun Int.sanitizeOosLocalizerConfigMode(): Int {
        return when (this) {
            OOS_LOCALIZER_CONFIG_DEFAULT,
            OOS_LOCALIZER_CONFIG_CUSTOM -> this
            else -> DEFAULT_OOS_LOCALIZER_CONFIG_MODE
        }
    }

    private fun String?.sanitizeOosLocalizerRegion(): String {
        return this
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.matches(Regex("[A-Z]{2}")) }
            ?: DEFAULT_OOS_LOCALIZER_REGION
    }

    private fun String?.sanitizeOosLocalizerLocale(): String {
        return this
            ?.trim()
            ?.replace('_', '-')
            ?.takeIf { it.matches(Regex("[A-Za-z]{2,3}(-[A-Za-z]{2})?")) }
            ?: DEFAULT_OOS_LOCALIZER_LOCALE
    }

    private fun String?.sanitizeOosLocalizerModel(): String {
        return this
            ?.trim()
            ?.takeIf { it.matches(Regex("[A-Za-z0-9_.-]{2,32}")) }
            ?: DEFAULT_OOS_LOCALIZER_MODEL
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
            val directResult = ShellLogger.exec("LSP sync toggle direct:$settingsGlobalKey", *directCommands.toTypedArray())
            // Some environments only persist these writes when explicitly forced through su.
            val suResult = ShellLogger.exec("LSP sync toggle su:$settingsGlobalKey", "su -c \"$suCommand\"")
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
            val directResult = ShellLogger.exec("LSP sync scalar direct:$settingsGlobalKey", *directCommands.toTypedArray())
            val suResult = ShellLogger.exec("LSP sync scalar su:$settingsGlobalKey", "su -c \"$suCommand\"")
            if (!directResult.isSuccess && !suResult.isSuccess) {
                // No-op: best effort sync. Xposed side still falls back to prefs.
            }
        }
    }

    private fun shellQuote(value: String): String {
        return "'" + value.replace("'", "'\"'\"'") + "'"
    }

    private fun prefs(context: Context): SharedPreferences {
        val storageContext = prefsContext(context)
        return storageContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun prefsContext(context: Context): Context {
        val deviceContext = context.createDeviceProtectedStorageContext()
        runCatching {
            val devicePrefsFile = File(
                "/data/user_de/0/${context.packageName}/shared_prefs",
                "$PREFS_NAME.xml"
            )
            if (!devicePrefsFile.exists()) {
                deviceContext.moveSharedPreferencesFrom(context, PREFS_NAME)
            }
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
