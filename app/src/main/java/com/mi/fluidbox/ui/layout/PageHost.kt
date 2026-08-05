package com.mi.fluidbox.ui.layout

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.fluidbox.R
import com.mi.fluidbox.ui.home.HomePage
import com.mi.fluidbox.ui.screens.RefreshRatePage
import com.mi.fluidbox.ui.layout.BlurredChromeBar
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.ListPopup
import io.github.suqi8.coui.kmp.basic.NavigationBar
import io.github.suqi8.coui.kmp.basic.NavigationItem
import io.github.suqi8.coui.kmp.basic.PopupPositionProvider
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TopAppBarState
import io.github.suqi8.coui.kmp.basic.COUIScrollBehavior
import io.github.suqi8.coui.kmp.blur.LayerBackdrop
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronForward
import io.github.suqi8.coui.kmp.icon.extended.Ok
import io.github.suqi8.coui.kmp.icon.extended.Back
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.darkColorScheme
import io.github.suqi8.coui.kmp.theme.lightColorScheme
import io.github.suqi8.coui.kmp.utils.COUIPopupUtils.Companion.COUIPopupHost
import io.github.suqi8.coui.kmp.utils.PressFeedbackType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

@Composable
fun Page(
    modifier: Modifier,
    currentTab: Int,
    rootGranted: Boolean,
    oneChinaPrincipleEnabled: Boolean,
    extremeRefresh165Enabled: Boolean,
    onExtremeRefresh165EnabledChange: (Boolean) -> Unit,
    bottomChromePadding: Dp,
    onHomeHeroLongPress: () -> Unit,
    blurBackdrop: LayerBackdrop?,
) {
    val scrollState = rememberScrollState()
    val topAppBarState = remember(currentTab) { TopAppBarState(-Float.MAX_VALUE, 0f, 0f) }
    LaunchedEffect(currentTab) {
        topAppBarState.heightOffset = 0f
        topAppBarState.contentOffset = 0f
    }
    val scrollBehavior = COUIScrollBehavior(state = topAppBarState)
    val pageTitle = when (currentTab) {
        0 -> stringResource(R.string.tab_home)
        4 -> stringResource(R.string.tab_refresh_rate)
        else -> ""
    }
    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = COUITheme.colorScheme.surface,
        popupHost = {},
        topBar = {
        BlurredChromeBar(
            backdrop = blurBackdrop,
        ) {
            TopAppBar(
                title = pageTitle,
                largeTitle = pageTitle,
                scrollBehavior = scrollBehavior,
                color = if (blurBackdrop != null) Color.Transparent else topBarColors(),
                showDivider = blurBackdrop == null,
                actions = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
        },
    ) { innerPadding ->
        val isRefreshRateTab = currentTab == 4
        val horizontalContentPadding = if (isRefreshRateTab) 0.dp else 16.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (blurBackdrop != null) Modifier.layerBackdrop(blurBackdrop) else Modifier)
                .verticalScroll(scrollState)
                .padding(
                    start = horizontalContentPadding,
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    end = horizontalContentPadding,
                    bottom = maxOf(innerPadding.calculateBottomPadding(), bottomChromePadding) + 12.dp,
                ),
            verticalArrangement = if (isRefreshRateTab) Arrangement.Top else Arrangement.spacedBy(12.dp),
        ) {
            when (currentTab) {
                0 -> HomePage(
                    rootGranted = rootGranted,
                    oneChinaPrincipleEnabled = oneChinaPrincipleEnabled,
                    onHeroLongPress = onHomeHeroLongPress,
                )
                4 -> RefreshRatePage(
                    extremeRefresh165Enabled = extremeRefresh165Enabled,
                    onExtremeRefresh165EnabledChange = onExtremeRefresh165EnabledChange,
                )
            }
        }
    }
}

@Composable
internal fun stableTopBarInsets(): WindowInsets {
    val statusBarHeight = stableStatusBarHeight()
    return WindowInsets(left = 0.dp, top = statusBarHeight, right = 0.dp, bottom = 0.dp)
}

@Composable
internal fun stableStatusBarHeight(): Dp {
    val context = LocalContext.current
    val density = LocalDensity.current
    return remember(context, density) {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            with(density) { context.resources.getDimensionPixelSize(resourceId).toDp() }
        } else {
            24.dp
        }
    }
}
