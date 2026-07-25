package com.mi.fluidbox.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BatteryPowerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> BatteryLiveNotificationService.sync(context.applicationContext)
        }
    }
}
