package com.mi.fluidbox.ui.screens

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.fluidbox.R
import com.mi.fluidbox.ui.common.AppIcons
import com.mi.fluidbox.ui.common.AppLogEntry
import com.mi.fluidbox.ui.common.AppLogLevel
import com.mi.fluidbox.ui.common.AppLogStore
import com.mi.fluidbox.ui.common.isHookLog
import com.mi.fluidbox.ui.common.rememberColorOsHapticTick
import com.mi.fluidbox.ui.common.rememberHapticClick
import com.mi.fluidbox.ui.common.rememberHapticLongPress
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.TextField
import io.github.suqi8.coui.kmp.basic.TextFieldMode
import io.github.suqi8.coui.kmp.theme.COUITheme
import com.mi.fluidbox.ui.settings.SettingsGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun rememberLogsPageState(): LogsPageState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entries by AppLogStore.entries.collectAsState()
    var searchVisible by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val saveLogLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val message = runCatching {
                val content = AppLogStore.exportHookText()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(content.toByteArray(StandardCharsets.UTF_8))
                        output.flush()
                    } ?: error("Cannot open output stream")
                }
                context.getString(R.string.log_save_success, uri.toString())
            }.getOrElse { throwable ->
                context.getString(
                    R.string.log_save_failed,
                    throwable.message ?: throwable::class.java.simpleName
                )
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        AppLogStore.initialize(context)
        AppLogStore.reload(context)
    }

    val hookEntries = remember(entries) {
        entries.filter(AppLogEntry::isHookLog)
    }
    val visibleEntries = remember(hookEntries, searchText) {
        val keyword = searchText.trim()
        if (keyword.isBlank()) {
            hookEntries
        } else {
            hookEntries.filter { entry ->
                val display = entry.toLogDisplay()
                entry.tag.contains(keyword, ignoreCase = true) ||
                    display.title.contains(keyword, ignoreCase = true) ||
                    display.source.contains(keyword, ignoreCase = true) ||
                    display.body.contains(keyword, ignoreCase = true)
            }
        }
    }

    return LogsPageState(
        visibleEntries = visibleEntries,
        searchVisible = searchVisible,
        searchText = searchText,
        onSearchTextChange = { searchText = it },
        onToggleSearch = {
            val willShow = !searchVisible
            searchVisible = willShow
            if (!willShow) {
                scope.launch {
                    delay(LogTokens.SearchTextClearDelayMillis.toLong())
                    if (!searchVisible) searchText = ""
                }
            }
        },
        onRefresh = {
            scope.launch {
                AppLogStore.reload(context)
                Toast.makeText(context, R.string.action_refresh, Toast.LENGTH_SHORT).show()
            }
        },
        onSave = {
            saveLogLauncher.launch("fluidbox_hook_log_${System.currentTimeMillis()}.txt")
        },
        onShare = {
            scope.launch {
                AppLogStore.createHookShareUri(context).onSuccess { uri ->
                    val intent = Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_STREAM, uri)
                        .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.log_share_subject))
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(
                        Intent.createChooser(intent, context.getString(R.string.log_action_share))
                    )
                }.onFailure { throwable ->
                    Toast.makeText(context, throwable.message.orEmpty(), Toast.LENGTH_SHORT).show()
                }
            }
        },
        onClear = {
            AppLogStore.clearHookLogs()
            Toast.makeText(context, R.string.log_clear_success, Toast.LENGTH_SHORT).show()
        },
    )
}

data class LogsPageState(
    val visibleEntries: List<AppLogEntry>,
    val searchVisible: Boolean,
    val searchText: String,
    val onSearchTextChange: (String) -> Unit,
    val onToggleSearch: () -> Unit,
    val onRefresh: () -> Unit,
    val onSave: () -> Unit,
    val onShare: () -> Unit,
    val onClear: () -> Unit,
)

@Composable
fun LogsPage(state: LogsPageState) {
    val reversedEntries = remember(state.visibleEntries) {
        state.visibleEntries.asReversed()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LogSearchVisibility(state)

        if (reversedEntries.isEmpty()) {
            LogEmptyCard()
        } else {
            LogEntryCard(reversedEntries.first())
        }
    }

    reversedEntries.drop(1).forEach { entry ->
        LogEntryCard(entry)
    }
}

@Composable
private fun LogSearchVisibility(state: LogsPageState) {
    AnimatedVisibility(
        visible = state.searchVisible,
        enter = expandVertically(
            animationSpec = tween(
                durationMillis = LogTokens.SearchEnterDurationMillis,
                easing = FastOutSlowInEasing,
            ),
            expandFrom = Alignment.Top,
        ) + fadeIn(
            animationSpec = tween(durationMillis = 100),
            initialAlpha = 0.65f,
        ),
        exit = shrinkVertically(
            animationSpec = tween(
                durationMillis = LogTokens.SearchExitDurationMillis,
                easing = LinearOutSlowInEasing,
            ),
            shrinkTowards = Alignment.Top,
        ) + fadeOut(
            animationSpec = tween(durationMillis = 130),
        ),
        label = "LogSearchVisibility",
    ) {
        Column {
            LogSearchField(
                value = state.searchText,
                onValueChange = state.onSearchTextChange,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LogEmptyCard() {
    SettingsGroup {
        Text(
            text = stringResource(R.string.log_empty),
            style = COUITheme.textStyles.body1,
            color = COUITheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
        )
    }
}

@Composable
fun LogsTopBarActions(state: LogsPageState) {
    LogToolbar(
        searchVisible = state.searchVisible,
        onToggleSearch = state.onToggleSearch,
        onRefresh = state.onRefresh,
        onSave = state.onSave,
        onShare = state.onShare,
        onClear = state.onClear,
    )
}

@Composable
private fun LogToolbar(
    searchVisible: Boolean,
    onToggleSearch: () -> Unit,
    onRefresh: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogIconButton(
            icon = AppIcons.Search,
            label = stringResource(R.string.log_action_filter),
            selected = searchVisible,
            onClick = onToggleSearch,
        )
        LogIconButton(
            icon = AppIcons.Refresh,
            label = stringResource(R.string.action_refresh),
            onClick = onRefresh,
        )
        LogIconButton(
            icon = AppIcons.Save,
            label = stringResource(R.string.log_action_save),
            onClick = onSave,
        )
        LogIconButton(
            icon = AppIcons.Share,
            label = stringResource(R.string.log_action_share),
            onClick = onShare,
        )
        LogIconButton(
            icon = AppIcons.Trash,
            label = stringResource(R.string.log_action_clear),
            onClick = onClear,
        )
    }
}

@Composable
private fun LogIconButton(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val hapticClick = rememberHapticClick()

    Box(
        modifier = Modifier
            .size(LogTokens.ToolbarButtonSize)
            .clickable {
                hapticClick()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) COUITheme.colorScheme.primary else COUITheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun LogSearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    SettingsGroup {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = AppIcons.Search,
                contentDescription = null,
                tint = COUITheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                label = stringResource(R.string.log_search_placeholder),
                backgroundMode = TextFieldMode.None,
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LogEntryCard(entry: AppLogEntry) {
    val context = LocalContext.current
    val display = remember(entry) { entry.toLogDisplay() }
    val copyText = remember(entry) { entry.toLine() }
    val sourceLabel = display.source.ifBlank { entry.tag }
    val hapticClick = rememberHapticClick()
    val hapticLongPress = rememberHapticLongPress()
    val hapticExpand = rememberColorOsHapticTick()
    var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }
    var canExpand by remember(display.body) { mutableStateOf(false) }
    LaunchedEffect(canExpand) {
        if (!canExpand && expanded) {
            expanded = false
        }
    }
    SettingsGroup {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(canExpand, expanded, hapticClick, hapticLongPress, hapticExpand, display.title, copyText) {
                    detectTapGestures(
                        onTap = {
                            if (canExpand) {
                                hapticClick()
                                hapticExpand()
                                expanded = !expanded
                            } else {
                                hapticClick()
                            }
                        },
                        onLongPress = {
                            hapticLongPress()
                            context
                                .getSystemService(android.content.ClipboardManager::class.java)
                                ?.setPrimaryClip(ClipData.newPlainText(display.title, copyText))
                            Toast.makeText(context, R.string.log_copy_success, Toast.LENGTH_SHORT).show()
                        },
                    )
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LogLevelBadge(display.level)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = display.title,
                    modifier = Modifier
                        .weight(1f)
                        .alignBy(FirstBaseline),
                    style = COUITheme.textStyles.title3,
                    color = COUITheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = display.time,
                    modifier = Modifier.alignBy(FirstBaseline),
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                )
            }
            Text(
                text = display.body,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = if (expanded) 340 else 280,
                            easing = FastOutSlowInEasing,
                        ),
                    ),
                style = COUITheme.textStyles.body1,
                color = COUITheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                maxLines = if (expanded) Int.MAX_VALUE else LogTokens.CollapsedBodyMaxLines,
                overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    if (!expanded) {
                        canExpand = textLayoutResult.hasVisualOverflow
                    }
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val hasTrailingPackage =
                    display.trailingPackage.isNotBlank() && display.trailingPackage != sourceLabel
                if (hasTrailingPackage) {
                    Text(
                        text = display.trailingPackage,
                        modifier = Modifier.weight(1f),
                        style = COUITheme.textStyles.body1,
                        color = COUITheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = sourceLabel,
                    modifier = Modifier.weight(1f),
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = if (hasTrailingPackage) TextAlign.End else TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun LogLevelBadge(level: AppLogLevel) {
    val color = when (level) {
        AppLogLevel.DEBUG -> COUITheme.colorScheme.onSurfaceVariantSummary
        AppLogLevel.INFO -> COUITheme.colorScheme.primary
        AppLogLevel.WARN -> Color(0xFFFFB020)
        AppLogLevel.ERROR -> Color(0xFFFF4D55)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.86f), RoundedCornerShape(13.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(level.labelRes),
            style = COUITheme.textStyles.body1,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private data class LogDisplay(
    val level: AppLogLevel,
    val title: String,
    val source: String,
    val body: String,
    val trailingPackage: String,
    val time: String,
)

private object LogTokens {
    const val CollapsedBodyMaxLines = 6
    const val SearchEnterDurationMillis = 180
    const val SearchExitDurationMillis = 220
    const val SearchTextClearDelayMillis = 300
    val ToolbarButtonSize = 34.dp
}

private val logTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")

private val bracketTitleRegex = Regex("""^\[(.+?)]\s*(.*)$""")
private val hookTitleRegex = Regex("""^\[?(?i:hook)/([^]\s]+)]?\s*(.*)$""")
private val logcatPrefixRegex =
    Regex("""^\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d+\s+(?:(?:\d+\s+\d+\s+)?[VDIWEF][/\s][^:]+:\s*)""")
private val logPriorityPrefixRegex = Regex("""^[VDIWEF][/\s][^:]+:\s*""")
private val packageNameRegex = Regex("""\b[a-zA-Z][\w]*(?:\.[\w:]+){2,}\b""")

private fun AppLogEntry.toLogDisplay(): LogDisplay {
    val cleaned = message
        .replace("\\n", "\n")
        .replace(logcatPrefixRegex, "")
        .replace(logPriorityPrefixRegex, "")
        .trim()
    val parsedHeader = cleaned.parseLogHeader(tag)
    val parsedTitle = parsedHeader.title
    val parsedBody = parsedHeader.body
    val title = parsedTitle ?: tag.withoutHookTitlePrefix()
    val rawBody = (parsedBody ?: cleaned.ifBlank { toLine() })
        .withoutDuplicatedLeadingBracketLabel(title)
        .withoutLeadingHookLine()
    val (body, trailingSource) = rawBody.withoutTrailingSourcePackage()
    val source = packageNameRegex.find(cleaned)?.value ?: tag
    val displayLevel = level.withInferredSeverity("$cleaned\n$body")
    return LogDisplay(
        level = displayLevel,
        title = title,
        source = source.takeIf { it != title }.orEmpty(),
        body = body,
        trailingPackage = trailingSource.orEmpty(),
        time = Instant.ofEpochMilli(timestampMs)
            .atZone(ZoneId.systemDefault())
            .format(logTimeFormatter),
    )
}

private fun AppLogLevel.withInferredSeverity(text: String): AppLogLevel {
    if (this == AppLogLevel.ERROR) return this

    val normalized = text.lowercase()
    val inferredError = errorLogTokens.any { token -> normalized.contains(token) }
    if (inferredError) return AppLogLevel.ERROR

    if (this == AppLogLevel.WARN) return this
    val inferredWarn = warnLogTokens.any { token -> normalized.contains(token) }
    if (inferredWarn) return AppLogLevel.WARN

    return this
}

private data class ParsedLogHeader(
    val title: String?,
    val body: String?,
)

private fun String.parseLogHeader(tag: String): ParsedLogHeader {
    val normalized = trimStart()
    val firstLineEnd = normalized.indexOf('\n')
    val firstLine = if (firstLineEnd >= 0) normalized.substring(0, firstLineEnd) else normalized
    val rest = if (firstLineEnd >= 0) normalized.substring(firstLineEnd + 1) else ""

    hookTitleRegex.find(firstLine)?.let { match ->
        val title = match.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() }
        val body = mergeHeaderBody(
            inlineBody = match.groupValues.getOrNull(2).orEmpty(),
            rest = rest,
        )
        return ParsedLogHeader(title = title, body = body)
    }

    bracketTitleRegex.find(firstLine)?.let { match ->
        val rawTitle = match.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() }
        val title = rawTitle?.withoutHookTitlePrefix()
        val body = mergeHeaderBody(
            inlineBody = match.groupValues.getOrNull(2).orEmpty(),
            rest = rest,
        )
        return ParsedLogHeader(title = title, body = body)
    }

    return ParsedLogHeader(title = tag.withoutHookTitlePrefix(), body = null)
}

private fun mergeHeaderBody(inlineBody: String, rest: String): String? {
    return listOf(inlineBody, rest)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n")
        .ifBlank { null }
}

private fun String.withoutHookTitlePrefix(): String =
    removePrefix("[")
        .replace(Regex("""^hook/""", RegexOption.IGNORE_CASE), "")
        .substringBefore(']')
        .trim()

private fun String.withoutDuplicatedLeadingBracketLabel(title: String): String {
    val trimmedTitle = title.trim()
    if (trimmedTitle.isBlank()) return this

    val leadingLabelRegex = Regex("""^\[([^]]+)]\s*""")
    val match = leadingLabelRegex.find(this) ?: return this
    val label = match.groupValues.getOrNull(1)?.trim().orEmpty()
    if (!label.equals(trimmedTitle, ignoreCase = true)) return this

    return removeRange(match.range).trimStart()
}

private fun String.withoutLeadingHookLine(): String {
    val lines = lines()
    val firstContentIndex = lines.indexOfFirst { it.isNotBlank() }
    if (firstContentIndex < 0) return this
    if (!lines[firstContentIndex].trim().equals("hook", ignoreCase = true)) return this

    return lines
        .filterIndexed { index, _ -> index != firstContentIndex }
        .joinToString("\n")
        .trimStart()
}

private fun String.withoutTrailingSourcePackage(): Pair<String, String?> {
    val lines = lines()
    val lastContentIndex = lines.indexOfLast { it.isNotBlank() }
    if (lastContentIndex < 0) return this to null

    val trailingLine = lines[lastContentIndex].trim()
    if (!packageNameRegex.matches(trailingLine)) return this to null

    val body = lines
        .filterIndexed { index, _ -> index != lastContentIndex }
        .joinToString("\n")
        .trimEnd()
    return body to trailingLine
}

private fun AppLogEntry.isModuleLogEntry(): Boolean {
    val value = "$tag $message"
    return moduleLogTokens.any { token -> value.contains(token, ignoreCase = true) }
}

private val moduleLogTokens = listOf(
    "fluidbox",
    "hook",
    "xposed",
    "lsposed",
    "yukihook",
    "root",
    "shell",
    "scope",
    "kernelsu",
    "ksu",
    "systemui",
)

private val errorLogTokens = listOf(
    "exception",
    "error",
    "failed",
    "failure",
    "cannot run",
    "no such file",
    "permission denied",
    "fatal",
    "crash",
    "throwable",
)

private val warnLogTokens = listOf(
    "warn",
    "warning",
    "missing",
    "not found",
    "timeout",
    "fallback",
)

private val AppLogLevel.labelRes: Int
    get() = when (this) {
        AppLogLevel.DEBUG -> R.string.log_level_debug
        AppLogLevel.INFO -> R.string.log_level_info
        AppLogLevel.WARN -> R.string.log_level_warn
        AppLogLevel.ERROR -> R.string.log_level_error
    }
