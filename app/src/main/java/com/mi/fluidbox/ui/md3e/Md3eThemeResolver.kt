package com.mi.fluidbox.ui.md3e

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils

private fun blendColor(color: Int, withColor: Int, ratio: Float): Color {
    return Color(ColorUtils.blendARGB(color, withColor, ratio.coerceIn(0f, 1f)))
}

private fun customMonetColorScheme(
    darkTheme: Boolean,
    seedColor: Int
): ColorScheme {
    val seed = seedColor or 0xFF000000.toInt()
    val white = 0xFFFFFFFF.toInt()
    val black = 0xFF000000.toInt()

    return if (darkTheme) {
        darkColorScheme(
            primary = blendColor(seed, white, 0.24f),
            onPrimary = blendColor(seed, black, 0.88f),
            primaryContainer = blendColor(seed, black, 0.52f),
            onPrimaryContainer = blendColor(seed, white, 0.40f),
            secondary = blendColor(seed, white, 0.30f),
            onSecondary = blendColor(seed, black, 0.86f),
            secondaryContainer = blendColor(seed, black, 0.56f),
            onSecondaryContainer = blendColor(seed, white, 0.45f),
            tertiary = blendColor(seed, white, 0.38f),
            onTertiary = blendColor(seed, black, 0.86f),
            tertiaryContainer = blendColor(seed, black, 0.62f),
            onTertiaryContainer = blendColor(seed, white, 0.52f),
            background = blendColor(seed, black, 0.90f),
            onBackground = blendColor(seed, white, 0.72f),
            surface = blendColor(seed, black, 0.88f),
            onSurface = blendColor(seed, white, 0.72f),
            surfaceVariant = blendColor(seed, black, 0.80f),
            onSurfaceVariant = blendColor(seed, white, 0.56f),
            surfaceContainerLowest = blendColor(seed, black, 0.94f),
            surfaceContainerLow = blendColor(seed, black, 0.91f),
            surfaceContainer = blendColor(seed, black, 0.88f),
            surfaceContainerHigh = blendColor(seed, black, 0.84f),
            surfaceContainerHighest = blendColor(seed, black, 0.80f),
            outline = blendColor(seed, white, 0.40f)
        )
    } else {
        lightColorScheme(
            primary = blendColor(seed, black, 0.16f),
            onPrimary = Color.White,
            primaryContainer = blendColor(seed, white, 0.66f),
            onPrimaryContainer = blendColor(seed, black, 0.76f),
            secondary = blendColor(seed, black, 0.24f),
            onSecondary = Color.White,
            secondaryContainer = blendColor(seed, white, 0.72f),
            onSecondaryContainer = blendColor(seed, black, 0.76f),
            tertiary = blendColor(seed, black, 0.32f),
            onTertiary = Color.White,
            tertiaryContainer = blendColor(seed, white, 0.76f),
            onTertiaryContainer = blendColor(seed, black, 0.78f),
            background = blendColor(seed, white, 0.96f),
            onBackground = blendColor(seed, black, 0.78f),
            surface = blendColor(seed, white, 0.94f),
            onSurface = blendColor(seed, black, 0.78f),
            surfaceVariant = blendColor(seed, white, 0.84f),
            onSurfaceVariant = blendColor(seed, black, 0.52f),
            surfaceContainerLowest = blendColor(seed, white, 0.99f),
            surfaceContainerLow = blendColor(seed, white, 0.96f),
            surfaceContainer = blendColor(seed, white, 0.93f),
            surfaceContainerHigh = blendColor(seed, white, 0.90f),
            surfaceContainerHighest = blendColor(seed, white, 0.87f),
            outline = blendColor(seed, black, 0.45f)
        )
    }
}

private fun ColorScheme.withMd3eDarkContainers(): ColorScheme {
    return copy(
        background = Color(0xFF080C14),
        surface = Color(0xFF10141D),
        surfaceVariant = Color(0xFF252A35),
        surfaceContainerLowest = Color(0xFF090D15),
        surfaceContainerLow = Color(0xFF121722),
        surfaceContainer = Color(0xFF171C27),
        surfaceContainerHigh = Color(0xFF1C222E),
        surfaceContainerHighest = Color(0xFF242A36),
        outlineVariant = Color(0xFF2B3140)
    )
}

internal fun resolveMd3eColorScheme(
    context: Context,
    darkTheme: Boolean,
    customMonetEnabled: Boolean,
    customMonetSeedColor: Int
): ColorScheme {
    return when {
        customMonetEnabled -> customMonetColorScheme(
            darkTheme = darkTheme,
            seedColor = customMonetSeedColor
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }.let { colorScheme ->
        if (darkTheme && !customMonetEnabled) colorScheme.withMd3eDarkContainers() else colorScheme
    }
}

internal fun resolveCosxColorScheme(
    darkTheme: Boolean
): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF4F7CFF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF26314F),
            onPrimaryContainer = Color(0xFFD8E2FF),
            secondary = Color(0xFF6E7485),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFF1A1D26),
            onSecondaryContainer = Color(0xFFDDE2F2),
            tertiary = Color(0xFF8EA4FF),
            onTertiary = Color(0xFF12151D),
            tertiaryContainer = Color(0xFF1E2436),
            onTertiaryContainer = Color(0xFFDDE2FF),
            background = Color(0xFF070A12),
            onBackground = Color(0xFFE8EBF5),
            surface = Color(0xFF0F121A),
            onSurface = Color(0xFFE8EBF5),
            surfaceVariant = Color(0xFF2A2E3A),
            onSurfaceVariant = Color(0xFFB8BECE),
            surfaceContainerLowest = Color(0xFF090C14),
            surfaceContainerLow = Color(0xFF131723),
            surfaceContainer = Color(0xFF1A1E2A),
            surfaceContainerHigh = Color(0xFF202535),
            surfaceContainerHighest = Color(0xFF252B3D),
            outline = Color(0xFF3A4050),
            outlineVariant = Color(0xFF2C3140)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF325EE6),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDCE3FF),
            onPrimaryContainer = Color(0xFF00174A),
            secondary = Color(0xFF4E566B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE0E5F4),
            onSecondaryContainer = Color(0xFF0B1426),
            tertiary = Color(0xFF2A63A3),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD2E4FF),
            onTertiaryContainer = Color(0xFF001D36),
            background = Color(0xFFF4F6FC),
            onBackground = Color(0xFF12141B),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF12141B),
            surfaceVariant = Color(0xFFE0E4F0),
            onSurfaceVariant = Color(0xFF454A59),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF2F5FD),
            surfaceContainer = Color(0xFFEBEEF8),
            surfaceContainerHigh = Color(0xFFE5E9F5),
            surfaceContainerHighest = Color(0xFFDFE4F2),
            outline = Color(0xFF73798B),
            outlineVariant = Color(0xFFC4CAD9)
        )
    }
}
