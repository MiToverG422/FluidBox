package com.mi.fluidbox.ui.common

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

val LocalHapticFeedbackEnabled = compositionLocalOf { true }
val LocalColorOsHapticFeedbackEnabled = compositionLocalOf { false }

@Composable
fun rememberHapticClick(): () -> Unit {
    val view = LocalView.current
    val enabled = LocalHapticFeedbackEnabled.current
    return remember(view, enabled) {
        {
            if (enabled) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }
    }
}

@Composable
fun rememberHapticToggle(): () -> Unit {
    val view = LocalView.current
    val enabled = LocalHapticFeedbackEnabled.current
    val colorOsEnabled = LocalColorOsHapticFeedbackEnabled.current
    return remember(view, enabled, colorOsEnabled) {
        {
            when {
                colorOsEnabled -> view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                enabled -> view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }
    }
}

@Composable
fun rememberColorOsHapticTick(): () -> Unit {
    val view = LocalView.current
    val colorOsEnabled = LocalColorOsHapticFeedbackEnabled.current
    return remember(view, colorOsEnabled) {
        {
            if (colorOsEnabled) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }
}

@Composable
fun rememberHapticLongPress(): () -> Unit {
    val view = LocalView.current
    val enabled = LocalHapticFeedbackEnabled.current
    return remember(view, enabled) {
        {
            if (enabled) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }
}
