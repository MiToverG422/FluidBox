package com.mi.fluidbox.lsp

import android.database.MatrixCursor
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.concurrent.ConcurrentHashMap

object DoublePowerHooker {
    private const val TAG = "FluidBox-DoublePower"
    const val TARGET_PACKAGE = "com.oplus.doublewake.settings"

    private val installedHookKeys = ConcurrentHashMap.newKeySet<String>()

    fun hook(packageName: String, classLoader: ClassLoader?) {
        if (packageName != TARGET_PACKAGE) return
        hookQuickInfoConfig(classLoader)
    }

    private fun hookQuickInfoConfig(classLoader: ClassLoader?) {
        val hookKey = "quick_info_config"
        if (!installedHookKeys.add(hookKey)) return

        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.oplus.doublewake.settings.provider.QuickStartContentProvider",
                classLoader,
                "a",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val packageName = LspConfig.getDoublePowerTargetPackageXposed().trim()
                        if (!LspConfig.isDoublePowerCustomEnabledXposed() || packageName.isBlank()) return
                        val activityName = LspConfig.getDoublePowerTargetActivityXposed().trim()
                        log("Provide quick app cursor: pkg=$packageName activity=${activityName.ifBlank { "<launcher>" }}")
                        param.result = buildProviderCursor(packageName, activityName)
                    }
                }
            )
            XposedHelpers.findAndHookMethod(
                "com.oplus.doublewake.settings.config.a",
                classLoader,
                "b",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val packageName = LspConfig.getDoublePowerTargetPackageXposed().trim()
                        if (!LspConfig.isDoublePowerCustomEnabledXposed() || packageName.isBlank()) return
                        val activityName = LspConfig.getDoublePowerTargetActivityXposed().trim()
                        log("Provide QuickInfo: pkg=$packageName activity=${activityName.ifBlank { "<launcher>" }}")
                        param.result = buildQuickInfo(classLoader, packageName, activityName)
                    }
                }
            )
            XposedHelpers.findAndHookMethod(
                "com.oplus.doublewake.settings.config.a",
                classLoader,
                "c",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val packageName = LspConfig.getDoublePowerTargetPackageXposed().trim()
                        if (LspConfig.isDoublePowerCustomEnabledXposed() && packageName.isNotBlank()) {
                            param.result = true
                        }
                    }
                }
            )
            log("QuickInfo config hook installed")
        }.onFailure { error ->
            installedHookKeys.remove(hookKey)
            log("QuickInfo config hook failed: ${error.javaClass.simpleName}")
        }
    }

    private fun buildProviderCursor(packageName: String, activityName: String): MatrixCursor {
        val hasActivity = activityName.isNotBlank()
        val type = if (hasActivity) "jumpUrl" else "launcher"
        val jumpUrl = if (hasActivity) buildActivityIntentUri(packageName, activityName) else null
        val operation = if (hasActivity) null else packageName
        val cursor = MatrixCursor(
            arrayOf(
                "action",
                "jumpUrl",
                "operation",
                "period",
                "pkgName",
                "secLink",
                "secPkgName",
                "secType",
                "supportQuickStart",
                "tag",
                "type",
                "uniqueTag"
            )
        )
        cursor.addRow(
            arrayOf<Any?>(
                null,
                jumpUrl,
                operation,
                72,
                packageName,
                null,
                null,
                null,
                true,
                packageName,
                type,
                "OPEN_APP_FLAG"
            )
        )
        return cursor
    }

    private fun buildQuickInfo(
        classLoader: ClassLoader?,
        packageName: String,
        activityName: String
    ): Any {
        val hasActivity = activityName.isNotBlank()
        val quickInfoClass = XposedHelpers.findClass("com.oplus.data.QuickInfo", classLoader)
        val typeClass = XposedHelpers.findClass("com.oplus.data.QuickInfo\$Type", classLoader)
        val type = XposedHelpers.getStaticObjectField(
            typeClass,
            if (hasActivity) "JUMPURL" else "LAUNCHER"
        )
        val jumpUrl = if (hasActivity) buildActivityIntentUri(packageName, activityName) else null
        val operation = if (hasActivity) null else packageName
        return XposedHelpers.newInstance(
            quickInfoClass,
            packageName,
            packageName,
            type,
            null,
            jumpUrl,
            operation
        )
    }

    private fun buildActivityIntentUri(packageName: String, activityName: String): String {
        val component = "$packageName/$activityName"
        return "intent:#Intent;launchFlags=0x10000000;component=$component;package=$packageName;end"
    }

    private fun log(message: String) {
        XposedBridge.log("$TAG: $message")
    }
}
