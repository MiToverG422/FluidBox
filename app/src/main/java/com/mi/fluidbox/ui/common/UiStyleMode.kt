package com.mi.fluidbox.ui.common

enum class UiStyleMode(val prefValue: Int) {
    Md3e(prefValue = 0),
    Cosx(prefValue = 1);

    companion object {
        fun fromPrefValue(value: Int): UiStyleMode {
            return entries.firstOrNull { it.prefValue == value } ?: Md3e
        }
    }
}
