package com.mi.fluidbox.lsp

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

class FluidBoxYukiFrameworkHooker : YukiBaseHooker() {
    override fun onHook() {
        FrameworkHooker.hook(
            packageName = packageName,
            classLoader = appClassLoader
        )
    }
}

class FluidBoxYukiSettingsHooker : YukiBaseHooker() {
    override fun onHook() {
        val classLoader = appClassLoader ?: return
        SettingsHooker.hook(
            packageName = packageName,
            classLoader = classLoader
        )
    }
}

class FluidBoxYukiAssistantHooker : YukiBaseHooker() {
    override fun onHook() {
        val classLoader = appClassLoader ?: return
        AssistantHooker.hook(
            packageName = packageName,
            classLoader = classLoader
        )
    }
}

class FluidBoxYukiSystemUiHooker : YukiBaseHooker() {
    override fun onHook() {
        SystemUiHooker.hook(
            packageName = packageName,
            classLoader = appClassLoader
        )
    }
}
