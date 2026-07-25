package com.mi.mibox.lsp

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.util.concurrent.ConcurrentHashMap

class OosLspModuleEntry : XposedModule() {
    private val systemUiReadyHooked = ConcurrentHashMap.newKeySet<String>()

    override fun onSystemServerStarting(param: XposedModuleInterface.SystemServerStartingParam) {
        FrameworkHooker.hook(
            packageName = "system",
            classLoader = param.getClassLoader()
        )
        AssistantHooker.hook(
            packageName = "system",
            classLoader = param.getClassLoader()
        )
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        val packageName = param.getPackageName()
        when (packageName) {
            "android",
            "system" -> {
                FrameworkHooker.hook(
                    packageName = packageName,
                    classLoader = param.getDefaultClassLoader()
                )
            }

            "com.android.launcher",
            "com.oplus.aod" -> {
                FrameworkHooker.hook(
                    packageName = packageName,
                    classLoader = param.getDefaultClassLoader()
                )
            }

            "com.android.settings" -> {
                SettingsHooker.hook(
                    packageName = packageName,
                    classLoader = param.getDefaultClassLoader()
                )
            }
        }
        if (OosLocalizerHooker.isSupportedPackage(packageName)) {
            OosLocalizerHooker.hook(
                packageName = packageName,
                classLoader = param.getDefaultClassLoader()
            )
        }
        DoublePowerHooker.hook(
            packageName = packageName,
            classLoader = param.getDefaultClassLoader()
        )
        AssistantHooker.hook(
            packageName = packageName,
            classLoader = param.getDefaultClassLoader()
        )
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        val packageName = param.getPackageName()
        when (packageName) {
            "android",
            "system" -> {
                FrameworkHooker.hook(
                    packageName = packageName,
                    classLoader = param.getClassLoader()
                )
            }

            "com.android.launcher",
            "com.oplus.aod" -> {
                FrameworkHooker.hook(
                    packageName = packageName,
                    classLoader = param.getClassLoader()
                )
            }

            "com.android.systemui" -> {
                if (systemUiReadyHooked.add(packageName)) {
                    SystemUiHooker.hook(
                        packageName = packageName,
                        classLoader = param.getClassLoader()
                    )
                }
            }

            "com.android.settings" -> {
                SettingsHooker.hook(
                    packageName = packageName,
                    classLoader = param.getClassLoader()
                )
            }
        }
        if (OosLocalizerHooker.isSupportedPackage(packageName)) {
            OosLocalizerHooker.hook(
                packageName = packageName,
                classLoader = param.getClassLoader()
            )
        }
        DoublePowerHooker.hook(
            packageName = packageName,
            classLoader = param.getClassLoader()
        )
        AssistantHooker.hook(
            packageName = packageName,
            classLoader = param.getClassLoader()
        )
    }
}
