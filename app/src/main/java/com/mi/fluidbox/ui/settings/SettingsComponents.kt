package com.mi.fluidbox.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.fluidbox.R
import com.mi.fluidbox.lsp.LsposedScopeRequester
import com.mi.fluidbox.ui.common.AppLocale
import com.mi.fluidbox.ui.common.AppIcons
import com.mi.fluidbox.ui.common.rememberHapticClick
import com.mi.fluidbox.ui.common.bottomTabs
import com.mi.fluidbox.ui.common.readCachedRootAccessInfo
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.CardDefaults
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.HorizontalDivider
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.SmallTitle
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.ListPopup
import io.github.suqi8.coui.kmp.basic.NavigationBar
import io.github.suqi8.coui.kmp.basic.NavigationItem
import io.github.suqi8.coui.kmp.basic.PopupPositionProvider
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.COUIScrollBehavior
import io.github.suqi8.coui.kmp.icon.extended.Ok
import io.github.suqi8.coui.kmp.icon.extended.Back
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronForward
import io.github.suqi8.coui.kmp.preference.ArrowPreference
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.darkColorScheme
import io.github.suqi8.coui.kmp.theme.lightColorScheme
import io.github.suqi8.coui.kmp.utils.COUIPopupUtils.Companion.COUIPopupHost
import io.github.suqi8.coui.kmp.utils.PressFeedbackType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

@Composable
fun SettingsSection(title: String) {
    SmallTitle(text = title)
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
    ) {
        content()
    }
}

internal object SettingsTokens {
    // Press highlight inset around dividers.
    val DividerVerticalPadding = 6.dp
    // Settings divider thickness.
    val DividerThickness = 0.5.dp
    // Horizontal overflow for custom pressed-row highlight backgrounds.
    val RowHighlightHorizontalOverflow = 16.dp
    // Extra pressed-highlight overflow at card group top/bottom edges.
    val RowHighlightGroupEdgeOverflow = 28.dp
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
}

private fun Modifier.settingsRowHighlight(
    progress: Float,
    color: Color,
    hasDividerAbove: Boolean,
    hasDividerBelow: Boolean,
): Modifier = drawBehind {
    val alpha = progress.coerceIn(0f, 1f)
    if (alpha <= 0f) return@drawBehind

    val horizontalOverflow = SettingsTokens.RowHighlightHorizontalOverflow.toPx()
    val dividerLimit = SettingsTokens.DividerVerticalPadding.toPx()
    val groupEdgeOverflow = SettingsTokens.RowHighlightGroupEdgeOverflow.toPx()
    val topOverflow = if (hasDividerAbove) dividerLimit else groupEdgeOverflow
    val bottomOverflow = if (hasDividerBelow) dividerLimit else groupEdgeOverflow

    drawRect(
        color = color.copy(alpha = color.alpha * alpha),
        topLeft = Offset(-horizontalOverflow, -topOverflow),
        size = Size(
            width = size.width + horizontalOverflow * 2f,
            height = size.height + topOverflow + bottomOverflow,
        ),
    )
}

@Composable
fun Modifier.settingsInteractiveRowHighlight(
    interactionSource: MutableInteractionSource,
    color: Color,
    hasDividerAbove: Boolean,
    hasDividerBelow: Boolean,
    persistentProgress: Float = 0f,
): Modifier {
    val clickProgress = rememberSettingsRowClickHighlightProgress(interactionSource)
    return settingsRowHighlight(
        progress = max(clickProgress, persistentProgress),
        color = color,
        hasDividerAbove = hasDividerAbove,
        hasDividerBelow = hasDividerBelow,
    )
}

@Composable
private fun rememberSettingsRowClickHighlightProgress(
    interactionSource: MutableInteractionSource,
): Float {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(interactionSource) {
        var animationJob: Job? = null
        interactionSource.interactions.collect { interaction ->
            animationJob?.cancel()
            when (interaction) {
                is PressInteraction.Press -> {
                    animationJob = launch {
                        progress.stop()
                        progress.snapTo(0f)
                        progress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 80),
                        )
                    }
                }
                is PressInteraction.Release,
                is PressInteraction.Cancel -> {
                    animationJob = launch {
                        progress.stop()
                        progress.animateTo(
                            targetValue = 0.55f,
                            animationSpec = tween(durationMillis = 60),
                        )
                        progress.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 180),
                        )
                    }
                }
            }
        }
    }
    return progress.value
}

@Composable
fun SettingsCardRow(
    title: String,
    summary: String,
    trailing: String? = null,
    onClick: (() -> Unit)? = null,
    showArrow: Boolean = false,
    showExpandArrow: Boolean = false,
    expandArrowExpanded: Boolean = false,
    hasDividerAbove: Boolean = false,
    hasDividerBelow: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    if (trailing != null) {
        SettingsLanguageDropdown(
            title = title,
            summary = summary,
            hasDividerAbove = hasDividerAbove,
            hasDividerBelow = hasDividerBelow,
        )
        return
    }

    val rowSummary = summary.takeIf { it.isNotBlank() }
    val startAction = leadingContent?.let { content: @Composable () -> Unit ->
        @Composable { content() }
    }

    when {
        showArrow -> ArrowPreference(
            title = title,
            summary = rowSummary,
            startAction = startAction,
            onClick = onClick,
        )
        showExpandArrow -> BasicComponent(
            title = title,
            summary = rowSummary,
            startAction = startAction,
            endActions = {
                SettingsExpandArrow(expanded = expandArrowExpanded)
            },
            onClick = onClick,
        )
        else -> BasicComponent(
            title = title,
            summary = rowSummary,
            startAction = startAction,
            onClick = onClick,
        )
    }
}

@Composable
private fun SettingsExpandArrow(
    expanded: Boolean,
) {
    val rotationZ by animateFloatAsState(
        targetValue = if (expanded) -90f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "settingsExpandArrowRotation",
    )
    Icon(
        imageVector = COUIIcons.ChevronForward,
        contentDescription = null,
        tint = COUITheme.colorScheme.onSurfaceVariantActions,
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer(rotationZ = rotationZ),
    )
}

@Composable
internal fun SettingsRowTextContent(
    title: String,
    summary: String?,
    enabled: Boolean = true,
) {
    Text(
        text = title,
        fontSize = COUITheme.textStyles.headline1.fontSize,
        fontWeight = FontWeight.Medium,
        color = if (enabled) {
            COUITheme.colorScheme.onBackground
        } else {
            COUITheme.colorScheme.disabledOnSecondaryVariant
        },
    )
    if (summary != null) {
        Text(
            text = summary,
            modifier = Modifier.padding(top = 2.dp),
            fontSize = COUITheme.textStyles.body2.fontSize,
            color = if (enabled) {
                COUITheme.colorScheme.onSurfaceVariantSummary
            } else {
                COUITheme.colorScheme.disabledOnSecondaryVariant
            },
        )
    }
}
