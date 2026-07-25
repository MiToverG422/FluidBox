package com.mi.mibox.battery

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import com.mi.mibox.MainActivity
import com.mi.mibox.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BatteryLiveNotificationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var updateJob: Job? = null

    private fun startUpdateLoop() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                if (!BatteryPreferences.isLiveNotificationEnabled(this@BatteryLiveNotificationService)) {
                    stopSelf()
                    return@launch
                }
                val snapshot = BatteryMonitor.query(this@BatteryLiveNotificationService)
                if (!snapshot.isCharging &&
                    !BatteryPreferences.keepBackgroundRunning(this@BatteryLiveNotificationService)
                ) {
                    stopSelf()
                    return@launch
                }
                notificationManager.notify(NOTIFICATION_ID, buildNotification(snapshot))
                delay(BatteryPreferences.infoRefreshIntervalMs(this@BatteryLiveNotificationService).toLong())
            }
        }
    }

    private fun stopUpdateLoop() {
        updateJob?.cancel()
        updateJob = null
    }

    private val notificationManager: NotificationManager
        get() = getSystemService(NotificationManager::class.java)

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!BatteryPreferences.isLiveNotificationEnabled(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val snapshot = BatteryMonitor.query(this, includeOplus = false)
        if (!snapshot.isCharging && !BatteryPreferences.keepBackgroundRunning(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification(snapshot))
        startUpdateLoop()
        return START_STICKY
    }

    override fun onDestroy() {
        stopUpdateLoop()
        serviceScope.cancel()
        notificationManager.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.battery_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.battery_notification_channel_summary)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("NewApi")
    private fun buildNotification(snapshot: BatterySnapshot): Notification {
        val progress = snapshot.percent.coerceIn(0, 100)
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val iconResId = snapshot.chargeIconResId()
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(iconResId)
            .setContentTitle(snapshot.notificationTitle(this))
            .setContentText(snapshot.notificationSummary(this))
            .setSubText(snapshot.pluggedText(this))
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_STATUS)
            .setProgress(100, progress, false)

        if (Build.VERSION.SDK_INT >= 36) {
            val title = snapshot.notificationTitle(this)
            val progressColor = when {
                progress >= 80 -> Color.rgb(70, 170, 95)
                progress >= 40 -> Color.rgb(88, 142, 255)
                else -> Color.rgb(255, 172, 64)
            }
            builder
                .setShortCriticalText(title)
                .setRequestPromotedOngoing(true)
                .setStyle(
                    Notification.ProgressStyle()
                        .setProgress(progress)
                        .setStyledByProgress(true)
                        .setProgressTrackerIcon(
                            Icon.createWithResource(
                                this,
                                if (progress >= 100) {
                                    R.drawable.stat_charge_progress_transparent
                                } else {
                                    R.drawable.stat_charge_progress_dot
                                }
                            )
                        )
                        .addProgressSegment(
                            Notification.ProgressStyle.Segment(100)
                                .setColor(progressColor)
                        )
                )
        }

        return builder.build()
    }

    companion object {
        private const val CHANNEL_ID = "battery_live"
        private const val NOTIFICATION_ID = 0xB417

        fun sync(context: Context) {
            if (!BatteryPreferences.isLiveNotificationEnabled(context)) {
                context.stopService(Intent(context, BatteryLiveNotificationService::class.java))
                context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
                return
            }
            val snapshot = BatteryMonitor.query(context, includeOplus = false)
            val intent = Intent(context, BatteryLiveNotificationService::class.java)
            if (snapshot.isCharging || BatteryPreferences.keepBackgroundRunning(context)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
                context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
            }
        }
    }
}

private fun BatterySnapshot.notificationTitle(context: Context): String {
    if (!isCharging) return statusText(context)
    return powerWatts?.let { context.getString(R.string.battery_power_watts, it) }
        ?: context.getString(R.string.common_unknown)
}

private fun BatterySnapshot.chargeIconResId(): Int {
    return if (oplus?.chargingProtocol?.isFastCharging == true) {
        R.drawable.stat_charge_super_vooc
    } else {
        R.drawable.stat_charge_normal
    }
}

private fun BatterySnapshot.notificationSummary(context: Context): String {
    val parts = listOfNotNull(
        temperatureTenthsC?.let { context.getString(R.string.battery_temperature_celsius, it / 10f) },
        displayCurrentMa?.let { context.getString(R.string.battery_current_amps, kotlin.math.abs(it) / 1000f) },
        context.getString(R.string.battery_percent_value, percent)
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
        ?: context.getString(R.string.battery_notification_summary_fallback)
}

private fun BatterySnapshot.pluggedText(context: Context): String {
    return when (plugged) {
        android.os.BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.battery_plugged_ac)
        android.os.BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.battery_plugged_usb)
        android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.battery_plugged_wireless)
        android.os.BatteryManager.BATTERY_PLUGGED_DOCK -> context.getString(R.string.battery_plugged_dock)
        else -> context.getString(R.string.battery_plugged_unknown)
    }
}

private fun BatterySnapshot.statusText(context: Context): String {
    return when (status) {
        android.os.BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.battery_status_charging)
        android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.battery_status_discharging)
        android.os.BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.battery_status_full)
        android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.battery_status_not_charging)
        else -> context.getString(R.string.common_unknown)
    }
}
