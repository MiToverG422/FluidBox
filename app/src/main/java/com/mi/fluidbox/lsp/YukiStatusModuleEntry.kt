package com.mi.fluidbox.lsp

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed(entryClassName = "FluidBoxYukiStatusEntry", isUsingResourcesHook = true)
class YukiStatusModuleEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugLog {
            tag = "FluidBox"
        }
        isDebug = false
    }

    override fun onHook() = encase {
        loadSystem {
            FrameworkHooker.hook(
                packageName = packageName,
                classLoader = appClassLoader
            )
        }
        loadApp(name = "com.android.launcher") {
            FrameworkHooker.hook(
                packageName = packageName,
                classLoader = appClassLoader
            )
        }
        loadApp(name = "com.oplus.aod") {
            FrameworkHooker.hook(
                packageName = packageName,
                classLoader = appClassLoader
            )
        }
        loadApp(name = "com.android.settings", hooker = FluidBoxYukiSettingsHooker())
        loadApp(*OosLocalizerHooker.supportedPackageNames.toTypedArray()) {
            OosLocalizerHooker.hook(
                packageName = packageName,
                classLoader = appClassLoader
            )
        }
        loadSystem(hooker = FluidBoxYukiAssistantHooker())
        loadApp(name = "com.android.systemui", hooker = FluidBoxYukiAssistantHooker())
        loadApp(name = "com.android.systemui", hooker = FluidBoxYukiSystemUiHooker())
    }
}
