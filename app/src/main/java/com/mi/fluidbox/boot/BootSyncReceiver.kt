package com.mi.fluidbox.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mi.fluidbox.lsp.LspConfig
import com.mi.fluidbox.ui.common.AppLogStore

class BootSyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        runCatching {
            LspConfig.syncTogglesForBoot(context.applicationContext)
            AppLogStore.i("BootSync", "LSP toggles synced on $action")
        }.onFailure { error ->
            AppLogStore.w("BootSync", "Sync failed on $action: ${error.javaClass.simpleName}")
        }
    }
}

