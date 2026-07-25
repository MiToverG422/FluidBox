package com.mi.mibox.battery

import android.content.Context

object BatteryPreferences {
    private const val PREFS_NAME = "mibox_prefs"
    private const val KEY_LIVE_NOTIFICATION_ENABLED = "battery_live_notification_enabled"
    private const val KEY_KEEP_BACKGROUND_RUNNING = "battery_keep_background_running"
    private const val KEY_DUAL_CELL_MODE = "battery_dual_cell_mode"
    private const val KEY_SERIES_BATTERY_MODE = "battery_series_battery_mode"
    private const val KEY_INFO_REFRESH_INTERVAL_MS = "battery_info_refresh_interval_ms"
    private const val DEFAULT_INFO_REFRESH_INTERVAL_MS = 2_000
    private val INFO_REFRESH_INTERVAL_OPTIONS_MS = setOf(
        500,
        1_000,
        2_000,
        3_000,
        4_000,
        5_000,
        6_000,
        7_000,
        8_000,
        9_000,
        10_000
    )

    fun isLiveNotificationEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_LIVE_NOTIFICATION_ENABLED, true)
    }

    fun setLiveNotificationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_LIVE_NOTIFICATION_ENABLED, enabled).apply()
    }

    fun keepBackgroundRunning(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_KEEP_BACKGROUND_RUNNING, false)
    }

    fun setKeepBackgroundRunning(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_KEEP_BACKGROUND_RUNNING, enabled).apply()
    }

    fun isDualCellMode(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_DUAL_CELL_MODE, false)
    }

    fun setDualCellMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DUAL_CELL_MODE, enabled).apply()
    }

    fun isSeriesBatteryMode(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_SERIES_BATTERY_MODE, false)
    }

    fun setSeriesBatteryMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SERIES_BATTERY_MODE, enabled).apply()
    }

    fun infoRefreshIntervalMs(context: Context): Int {
        val interval = prefs(context).getInt(
            KEY_INFO_REFRESH_INTERVAL_MS,
            DEFAULT_INFO_REFRESH_INTERVAL_MS
        )
        return interval.takeIf { it in INFO_REFRESH_INTERVAL_OPTIONS_MS }
            ?: DEFAULT_INFO_REFRESH_INTERVAL_MS
    }

    fun setInfoRefreshIntervalMs(context: Context, intervalMs: Int) {
        val normalized = intervalMs.takeIf { it in INFO_REFRESH_INTERVAL_OPTIONS_MS }
            ?: DEFAULT_INFO_REFRESH_INTERVAL_MS
        prefs(context).edit().putInt(KEY_INFO_REFRESH_INTERVAL_MS, normalized).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
