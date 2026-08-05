package com.mi.fluidbox.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mi.fluidbox.ui.layout.topBarColors
import com.mi.fluidbox.ui.layout.BlurredChromeBar
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.TopAppBarState
import io.github.suqi8.coui.kmp.basic.COUIScrollBehavior
import io.github.suqi8.coui.kmp.blur.LayerBackdrop
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Back
import io.github.suqi8.coui.kmp.theme.COUITheme

@Composable
fun SettingsPageSurface(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    blurBackdrop: LayerBackdrop? = null,
    bottomContentPadding: Dp = 0.dp,
    bottomChrome: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val topAppBarState = remember(title) { TopAppBarState(-Float.MAX_VALUE, 0f, 0f) }
    LaunchedEffect(title) {
        topAppBarState.heightOffset = 0f
        topAppBarState.contentOffset = 0f
    }
    val scrollBehavior = COUIScrollBehavior(state = topAppBarState)

    Scaffold(
        modifier = modifier
            .background(COUITheme.colorScheme.surface)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = COUITheme.colorScheme.surface,
        popupHost = {},
        topBar = {
        BlurredChromeBar(
            backdrop = blurBackdrop,
        ) {
            TopAppBar(
                title = title,
                largeTitle = title,
                scrollBehavior = scrollBehavior,
                color = if (blurBackdrop != null) Color.Transparent else topBarColors(),
                showDivider = blurBackdrop == null,
                navigationIcon = {
                    if (showBack) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onBack?.invoke() },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = COUIIcons.Back,
                                contentDescription = null,
                                tint = COUITheme.colorScheme.onBackground,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                },
                actions = actions,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        },
        bottomBar = {
            bottomChrome?.invoke()
        },
    ) { innerPadding ->
        val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (blurBackdrop != null) Modifier.layerBackdrop(blurBackdrop) else Modifier)
                .verticalScroll(scrollState)
                .padding(
                    start = 0.dp,
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    end = 0.dp,
                    bottom = maxOf(innerPadding.calculateBottomPadding(), bottomContentPadding) + imeBottomPadding + 12.dp,
                ),
            content = content,
        )
        }
}
