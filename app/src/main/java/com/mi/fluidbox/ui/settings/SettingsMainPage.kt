package com.mi.fluidbox.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.fluidbox.BuildConfig
import com.mi.fluidbox.R
import com.mi.fluidbox.ui.common.AppIcons
import com.mi.fluidbox.ui.common.AppThemeMode
import com.mi.fluidbox.ui.common.ConfigBackup
import com.mi.fluidbox.ui.common.FluidBoxLogo
import com.mi.fluidbox.ui.common.FluidBoxLogoFontFamily
import com.mi.fluidbox.ui.common.isMonet
import com.mi.fluidbox.ui.home.rememberDeviceMarketName
import com.mi.fluidbox.ui.home.rememberAppVersionName
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.ListPopup
import io.github.suqi8.coui.kmp.basic.PopupPositionProvider
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.ChevronForward
import io.github.suqi8.coui.kmp.icon.extended.Ok
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.ThemeColorSpec
import io.github.suqi8.coui.kmp.theme.ThemePaletteStyle
import io.github.suqi8.coui.kmp.blur.isRuntimeShaderSupported
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.Locale

private const val COOLAPK_PROFILE_URL = "https://www.coolapk.com/u/29184225"
private const val GITHUB_PROFILE_URL = "https://github.com/MiToverG422"
private const val CODEX_PROFILE_URL = "https://github.com/codex"
private const val SUQI8_PROFILE_URL = "https://github.com/suqi8"
private const val COLORLARIS_PROFILE_URL = "https://github.com/Colorlaris"
private const val GITHUB_REPOSITORY_URL = "https://github.com/MiToverG422/FluidBox"
private const val TELEGRAM_CHANNEL_URL = "https://t.me/FluidBox_Chat"
private const val SOFTWARE_UPDATE_CHECK_DELAY_MS = 2_000L
private const val RELEASE_NOTES_ASSET = "release_notes.json"
private val SoftwareUpdateContentHorizontalPadding = 16.dp
private val SoftwareUpdateVersionInfoExtraHorizontalPadding = 20.dp
private const val MITOVERG_AVATAR_URL = "https://github.com/MiToverG422.png?size=160"
private const val CODEX_AVATAR_URL = "https://github.com/codex.png?size=160"
private const val COLORLARIS_AVATAR_URL = "https://github.com/Colorlaris.png?size=160"
private const val COUI_REPOSITORY_URL = "https://github.com/suqi8/coui"
private const val MIUIX_REPOSITORY_URL = "https://github.com/compose-miuix-ui/miuix"
private const val OSHIN_REPOSITORY_URL = "https://github.com/suqi8/OShin"
private const val YUKIHOOKAPI_REPOSITORY_URL = "https://github.com/HighCapable/YukiHookAPI"
private const val KERNELSU_REPOSITORY_URL = "https://github.com/tiann/KernelSU"
private const val LIBSU_REPOSITORY_URL = "https://github.com/topjohnwu/libsu"
private const val SHIZUKU_REPOSITORY_URL = "https://github.com/RikkaApps/Shizuku"
private const val HIDDEN_API_BYPASS_REPOSITORY_URL = "https://github.com/LSPosed/AndroidHiddenApiBypass"
private const val LSPOSED_REPOSITORY_URL = "https://github.com/LSPosed/LSPosed"
private const val JETPACK_COMPOSE_URL = "https://developer.android.com/jetpack/compose"
private const val ANDROIDX_URL = "https://developer.android.com/jetpack/androidx"
private const val ANDROIDX_PALETTE_URL = "https://developer.android.com/develop/ui/views/graphics/palette-colors"
private const val MATERIAL_ICONS_URL = "https://developer.android.com/reference/kotlin/androidx/compose/material/icons/package-summary"
private const val XPOSED_API_URL = "https://github.com/rovo89/XposedBridge"
private const val GAZE_CAPSULE_URL = "https://github.com/Mocha-Realm/gaze"
private const val KOTLIN_URL = "https://kotlinlang.org/"
private const val KSP_URL = "https://github.com/google/ksp"
private const val ANDROID_GRADLE_PLUGIN_URL = "https://developer.android.com/build/releases/gradle-plugin"
private const val JUNIT_URL = "https://junit.org/junit4/"
private const val ESPRESSO_URL = "https://developer.android.com/training/testing/espresso"

@DrawableRes
private fun fallbackAvatarRes(avatarUrl: String): Int = when (avatarUrl) {
    MITOVERG_AVATAR_URL -> R.drawable.avatar_mitoverg
    CODEX_AVATAR_URL -> R.drawable.avatar_codex
    COLORLARIS_AVATAR_URL -> R.drawable.avatar_colorlaris
    else -> R.drawable.ic_github
}

private data class AboutLinkItem(
    @param:StringRes val titleRes: Int,
    @param:StringRes val summaryRes: Int,
    val url: String,
    val leadingContent: (@Composable () -> Unit)? = null,
)

private data class AvatarImageState(
    val bitmap: Bitmap?,
)

private object GitHubAvatarCache {
    private val bitmaps = mutableMapOf<String, Bitmap>()
    private val failedUrls = mutableSetOf<String>()

    @Synchronized
    fun get(url: String): Bitmap? = bitmaps[url]

    @Synchronized
    fun put(url: String, bitmap: Bitmap) {
        bitmaps[url] = bitmap
        failedUrls.remove(url)
    }

    @Synchronized
    private fun markFailed(url: String) {
        failedUrls += url
    }

    @Synchronized
    private fun hasFailed(url: String): Boolean = url in failedUrls

    suspend fun load(context: Context, url: String): AvatarImageState = withContext(Dispatchers.IO) {
        get(url)?.let { bitmap ->
            return@withContext AvatarImageState(bitmap = bitmap)
        }
        val cachedBitmap = readFromDisk(context, url)
        if (cachedBitmap != null) {
            put(url, cachedBitmap)
            return@withContext AvatarImageState(bitmap = cachedBitmap)
        }
        if (hasFailed(url)) {
            return@withContext AvatarImageState(bitmap = null)
        }
        runCatching {
            URL(url).openConnection().apply {
                connectTimeout = 3_000
                readTimeout = 5_000
            }.getInputStream().use { stream ->
                BitmapFactory.decodeStream(stream)
            }?.also { bitmap ->
                put(url, bitmap)
                writeToDisk(context, url, bitmap)
            }
        }.fold(
            onSuccess = { bitmap ->
                AvatarImageState(bitmap = bitmap).also {
                    if (bitmap == null) markFailed(url)
                }
            },
            onFailure = {
                markFailed(url)
                AvatarImageState(bitmap = null)
            },
        )
    }

    private fun readFromDisk(context: Context, url: String): Bitmap? {
        val file = cacheFile(context, url)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    private fun writeToDisk(context: Context, url: String, bitmap: Bitmap) {
        val file = cacheFile(context, url)
        file.parentFile?.mkdirs()
        file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }

    private fun cacheFile(context: Context, url: String): File {
        val name = url.hashCode().toUInt().toString(16)
        return File(context.cacheDir, "github_avatars/$name.png")
    }
}

suspend fun prefetchAboutAuthorAvatars(context: Context) {
    val appContext = context.applicationContext
    GitHubAvatarCache.load(appContext, MITOVERG_AVATAR_URL)
    GitHubAvatarCache.load(appContext, CODEX_AVATAR_URL)
}

@Composable
fun AboutMainPage(
    onOpenAppSettings: () -> Unit,
    onOpenSoftwareUpdate: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenContributors: () -> Unit,
    onOpenReferences: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            prefetchAboutAuthorAvatars(context.applicationContext)
        }
    }
    AboutAppCard()
    AboutGroupSpacer()
    SettingsGroup {
        SettingsCardRow(
            title = stringResource(R.string.setting_software_update),
            summary = "",
            showArrow = true,
            hasDividerBelow = true,
            onClick = onOpenSoftwareUpdate,
        )
        SettingsDivider()
        SettingsCardRow(
            title = stringResource(R.string.tab_events),
            summary = "",
            showArrow = true,
            hasDividerAbove = true,
            hasDividerBelow = true,
            onClick = onOpenLogs,
        )
        SettingsDivider()
        SettingsCardRow(
            title = stringResource(R.string.setting_theme_settings),
            summary = "",
            showArrow = true,
            hasDividerAbove = true,
            onClick = onOpenAppSettings,
        )
    }
    AboutGroupSpacer()
    AboutAuthorGroup(
        onOpenContributors = onOpenContributors,
        onOpenReferences = onOpenReferences,
    )
}

@Composable
private fun AboutGroupSpacer() {
    Spacer(modifier = Modifier.height(0.dp))
}

@Composable
private fun AboutAppCard() {
    val versionName = rememberAppVersionName()
    val versionCode = rememberAppVersionCode()
    var versionTapCount by remember { mutableStateOf(0) }
    var lastVersionTapAt by remember { mutableStateOf(0L) }
    var showDetails by rememberSaveable { mutableStateOf(false) }

    SettingsGroup {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground_art),
                contentDescription = null,
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(COUITheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            FluidBoxLogo(
                color = COUITheme.colorScheme.onSurface,
                fontSize = 24.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.about_version, versionName),
                style = COUITheme.textStyles.body1,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    val now = SystemClock.elapsedRealtime()
                    versionTapCount = if (now - lastVersionTapAt <= 1_500L) {
                        versionTapCount + 1
                    } else {
                        1
                    }
                    lastVersionTapAt = now
                    if (versionTapCount >= 5) {
                        showDetails = !showDetails
                        versionTapCount = 0
                    }
                },
            )
            Spacer(modifier = Modifier.height(6.dp))
            BuildTypeBadge()
            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(durationMillis = 180)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                ) + fadeOut(animationSpec = tween(durationMillis = 140)),
            ) {
                AboutVersionDetails(
                    versionName = versionName,
                    versionCode = versionCode,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.about_description),
                style = COUITheme.textStyles.body1,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AboutVersionDetails(
    versionName: String,
    versionCode: Long,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(COUITheme.colorScheme.dividerLine),
        )
        Spacer(modifier = Modifier.height(8.dp))
        AboutDetailText("${stringResource(R.string.info_version)}: $versionName ($versionCode)")
        AboutDetailText("Build: ${BuildConfig.BUILD_TYPE}")
        AboutDetailText("${stringResource(R.string.info_package_name)}: ${BuildConfig.APPLICATION_ID}")
        AboutDetailText("${stringResource(R.string.about_detail_build_time)}: ${BuildConfig.APP_BUILD_TIME}")
        AboutDetailText(
            "${stringResource(R.string.about_detail_build_timestamp)}: ${BuildConfig.APP_BUILD_TIMESTAMP}",
        )
        AboutDetailText(
            "${stringResource(R.string.about_detail_yukihookapi_version)}: ${BuildConfig.YUKIHOOKAPI_VERSION}",
        )
    }
}

@Composable
private fun AboutDetailText(
    text: String,
) {
    Text(
        text = text,
        style = COUITheme.textStyles.body1,
        color = COUITheme.colorScheme.onSurfaceVariantSummary,
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
private fun rememberAppVersionCode(): Long {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
        }.getOrDefault(0L)
    }
}

@Composable
private fun BuildTypeBadge() {
    val isDebug = BuildConfig.DEBUG
    val badgeText = if (isDebug) "Debug" else "Release"
    val badgeColor = if (isDebug) {
        Color(0xFFFFC928)
    } else {
        Color(0xFF34C759)
    }
    val textColor = if (isDebug) {
        Color(0xFF3B2A00)
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(badgeColor)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badgeText,
            style = COUITheme.textStyles.body1,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
        )
    }
}

data class SoftwareUpdateUiState(
    val checkingUpdates: Boolean,
    val automaticSilentUpdate: Boolean,
    val selectedChannel: UpdateChannel,
    val statusText: String,
    val onCheckUpdates: () -> Unit,
    val onAutomaticSilentUpdateChange: (Boolean) -> Unit,
    val onUpdateChannelChange: (UpdateChannel) -> Unit,
)

@Composable
fun rememberSoftwareUpdateUiState(
    autoCheckUpdateRequest: Int,
): SoftwareUpdateUiState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checkingUpdates by remember { mutableStateOf(false) }
    var automaticSilentUpdate by remember {
        mutableStateOf(UpdateChannelPreference.getAutomaticSilentUpdate(context))
    }
    var selectedChannel by remember {
        mutableStateOf(UpdateChannelPreference.get(context))
    }
    var statusText by remember { mutableStateOf<String?>(null) }
    val checkingText = stringResource(R.string.update_checking)
    val noUpdateText = stringResource(R.string.update_no_update)
    val installStartedText = stringResource(R.string.update_install_started)
    val installFinishedText = stringResource(R.string.update_silent_install_finished)
    val installPermissionText = stringResource(R.string.update_install_permission_required)
    val failedText = stringResource(R.string.update_failed)
    val checkUpdates: () -> Unit = {
        if (!checkingUpdates) {
            scope.launch {
                checkingUpdates = true
                statusText = checkingText
                delay(SOFTWARE_UPDATE_CHECK_DELAY_MS)
                val result = AppUpdater.checkAndInstall(
                    context = context,
                    channel = selectedChannel,
                )
                val message = when (result) {
                    UpdateResult.NoUpdate -> noUpdateText
                    is UpdateResult.InstallStarted -> installStartedText.format(result.versionName)
                    is UpdateResult.InstallFinished -> installFinishedText.format(result.versionName)
                    is UpdateResult.Failed -> {
                        if (result.reason == AppUpdater.INSTALL_PERMISSION_REQUIRED) {
                            installPermissionText
                        } else {
                            failedText
                        }
                    }
                }
                statusText = message
                checkingUpdates = false
            }
        }
    }

    LaunchedEffect(autoCheckUpdateRequest) {
        if (autoCheckUpdateRequest > 0) {
            checkUpdates()
        }
    }

    return SoftwareUpdateUiState(
        checkingUpdates = checkingUpdates,
        automaticSilentUpdate = automaticSilentUpdate,
        selectedChannel = selectedChannel,
        statusText = statusText ?: noUpdateText,
        onCheckUpdates = checkUpdates,
        onAutomaticSilentUpdateChange = { enabled ->
            automaticSilentUpdate = enabled
            UpdateChannelPreference.setAutomaticSilentUpdate(context, enabled)
        },
        onUpdateChannelChange = { channel ->
            selectedChannel = channel
            UpdateChannelPreference.set(context, channel)
        },
    )
}

@Composable
fun SoftwareUpdatePage(
    state: SoftwareUpdateUiState,
    onOpenReleaseNotes: () -> Unit,
) {
    val versionName = rememberAppVersionName()
    val versionCode = rememberAppVersionCode()
    val deviceMarketName = rememberDeviceMarketName()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SoftwareUpdateContentHorizontalPadding)
                .aspectRatio(0.65f)
                .clip(RoundedCornerShape(28.dp)),
        ) {
            Image(
                painter = painterResource(R.drawable.software_update_wave_bg),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1f
                        scaleY = 1f
                    },
                contentScale = ContentScale.Crop,
            )
            Row(
                modifier = Modifier.padding(start = 26.dp, top = 28.dp),
                verticalAlignment = Alignment.Top,
            ) {
                SoftwareUpdateCardNumberText(
                    text = "1",
                    color = Color(0xFFFF625D),
                    fontSize = 125.sp,
                    lineHeight = 125.sp)
                SoftwareUpdateCardNumberText(
                    text = "6",
                    color = Color(0xFFE8E8E8),
                    fontSize = 125.sp,
                    lineHeight = 125.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 26.dp, end = 36.dp, bottom = 114.dp),
            ) {
                Text(
                    text = state.statusText,
                    style = COUITheme.textStyles.title2,
                    color = Color(0xFFECECEC),
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                )
                if (state.checkingUpdates) {
                    Spacer(modifier = Modifier.width(6.dp))
                    SoftwareUpdateLoadingIndicator(
                        color = Color(0xFFECECEC),
                        size = 16.dp,
                        strokeWidth = 0.5.dp,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }
            FluidBoxLogo(
                color = Color(0xFFECECEC),
                fontSize = 32.sp,
                lineHeight = 38.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 26.dp, end = 36.dp, bottom = 58.dp),
            )
            Text(
                text = deviceMarketName,
                style = COUITheme.textStyles.title3,
                color = Color(0xFFECECEC),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 26.dp, end = 36.dp, bottom = 40.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SoftwareUpdateContentHorizontalPadding +
                            SoftwareUpdateVersionInfoExtraHorizontalPadding,
                ),
        ) {
            Text(
                text = stringResource(R.string.software_update_version_title),
                style = COUITheme.textStyles.title3,
                color = COUITheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$versionName($versionCode)",
                style = COUITheme.textStyles.body1,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = onOpenReleaseNotes,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.software_update_release_notes),
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 13.sp,
                )
                Text(
                    text = ">",
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

@Composable
fun SoftwareUpdateReleaseNotesPage() {
    val context = LocalContext.current
    val versionName = rememberAppVersionName()
    val versionCode = rememberAppVersionCode()
    val releaseNotesText = rememberReleaseNotesForVersion(versionName, versionCode)
        ?: stringResource(R.string.software_update_release_notes_empty)
    var releaseNotesExpanded by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        SettingsGroup {
            SettingsCardRow(
                title = stringResource(R.string.software_update_version_title),
                summary = "$versionName($versionCode)",
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsGroup {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsCardRow(
                    title = stringResource(R.string.software_update_release_notes_title),
                    summary = "",
                    onClick = { releaseNotesExpanded = !releaseNotesExpanded },
                    showExpandArrow = true,
                    expandArrowExpanded = releaseNotesExpanded,
                    hasDividerBelow = releaseNotesExpanded,
                )
                AnimatedVisibility(visible = releaseNotesExpanded) {
                    Column {
                        SettingsDivider()
                        Text(
                            text = releaseNotesText,
                            style = COUITheme.textStyles.body1,
                            color = COUITheme.colorScheme.onSurfaceVariantSummary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SoftwareUpdateContentHorizontalPadding*2),
        ) {
            Text(
                text = stringResource(R.string.software_update_notice_title),
                style = COUITheme.textStyles.title3,
                color = COUITheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(14.dp))
            val noticeItems = stringResource(R.string.software_update_notice_body)
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toList()
            noticeItems.forEachIndexed { index, notice ->
                Text(
                    text = notice,
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                )
                if (index < noticeItems.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.software_update_notice_more),
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                )
                Text(
                    text = stringResource(R.string.software_update_notice_community),
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            onClick = { openUrl(context, TELEGRAM_CHANNEL_URL) },
                        ),
                )
            }
        }
    }
}

@Composable
private fun rememberReleaseNotesForVersion(
    versionName: String,
    versionCode: Long,
): String? {
    val context = LocalContext.current
    return remember(context, versionName, versionCode) {
        runCatching {
            val jsonText = context.assets.open(RELEASE_NOTES_ASSET)
                .bufferedReader()
                .use { it.readText() }
            val releaseNotesRoot = JSONObject(jsonText)
            val exactVersionKey = "$versionName($versionCode)"
            val releaseNotes = releaseNotesRoot
                .optJSONObject(exactVersionKey)
                ?: releaseNotesRoot.optJSONObject(versionName)
                ?: return@runCatching null
            val languageTag = Locale.getDefault().toLanguageTag()
            val localizedNotes = releaseNotes.optString(languageTag).takeIf { it.isNotBlank() }
                ?: if (languageTag.startsWith("zh", ignoreCase = true)) {
                    releaseNotes.optString("zh-CN").takeIf { it.isNotBlank() }
                } else {
                    null
                }
                ?: releaseNotes.optString("default").takeIf { it.isNotBlank() }
            localizedNotes
        }.getOrNull()
    }
}

@Composable
private fun SoftwareUpdateCardNumberText(
    text: String,
    color: Color,
    fontSize: TextUnit,
    lineHeight: TextUnit,
) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 5.dp.toPx() }
    val numberStyle = COUITheme.textStyles.title1.copy(
        fontFamily = FluidBoxLogoFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = fontSize,
        lineHeight = lineHeight,
    )

    Box {
        Text(
            text = text,
            style = numberStyle.copy(drawStyle = Stroke(width = strokeWidth)),
            color = color,
        )
        Text(
            text = text,
            style = numberStyle,
            color = color,
        )
    }
}

@Composable
private fun SoftwareUpdateLoadingIndicator(
    color: Color,
    size: Dp,
    strokeWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "softwareUpdateLoading")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "softwareUpdateLoadingRotation",
    )

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = rotation
            },
    ) {
        val strokeWidthPx = strokeWidth.toPx()
        val diameter = this.size.minDimension - strokeWidthPx
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun SoftwareUpdateTopBarActions(
    onOpenAutoUpdateSettings: () -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val popupWidth = 180.dp
    val popupPositionProvider = remember(density) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowBounds: IntRect,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
                popupMargin: IntRect,
                alignment: PopupPositionProvider.Align,
            ): IntOffset {
                val verticalOffset = with(density) { 8.dp.roundToPx() }
                val x = anchorBounds.right - popupContentSize.width
                val y = anchorBounds.bottom - verticalOffset
                return IntOffset(
                    x = x.coerceIn(windowBounds.left, windowBounds.right - popupContentSize.width),
                    y = y.coerceIn(windowBounds.top, windowBounds.bottom - popupContentSize.height),
                )
            }

            override fun getMargins(): PaddingValues {
                return PaddingValues(horizontal = 0.dp, vertical = 0.dp)
            }
        }
    }

    Box {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    onClick = { expanded.value = true },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(COUITheme.colorScheme.onBackground),
                    )
                }
            }
        }

        val popupOverlayColor = if (COUITheme.colorScheme.background.luminance() < 0.5f) {
            COUITheme.colorScheme.onSurface.copy(alpha = 0.08f)
        } else {
            Color.Transparent
        }
        ListPopup(
            show = expanded,
            popupPositionProvider = popupPositionProvider,
            alignment = PopupPositionProvider.Align.End,
            onDismissRequest = { expanded.value = false },
            minWidth = popupWidth,
            cornerRadius = 16.dp,
            shadowElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier
                    .width(popupWidth)
                    .background(popupOverlayColor),
            ) {
                SoftwareUpdateMenuRow(
                    title = stringResource(R.string.software_update_auto_settings),
                    onClick = {
                        expanded.value = false
                        onOpenAutoUpdateSettings()
                    },
                )
            }
        }
    }
}

@Composable
fun SoftwareUpdateAutoSettingsPage(
    state: SoftwareUpdateUiState,
) {
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.setting_auto_silent_update),
            summary = stringResource(R.string.setting_auto_silent_update_summary),
            checked = state.automaticSilentUpdate,
            onCheckedChange = state.onAutomaticSilentUpdateChange,
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsUpdateChannelDropdown(
            title = stringResource(R.string.about_update_channel_title),
            hasDividerAbove = true,
            hasDividerBelow = true,
            selectedChannel = state.selectedChannel,
            onChannelChange = state.onUpdateChannelChange,
        )
    }
}

@Composable
private fun SoftwareUpdateMenuRow(
    title: String,
    trailing: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = COUITheme.textStyles.title3,
            color = when {
                selected -> COUITheme.colorScheme.primary
                enabled -> COUITheme.colorScheme.onSurface
                else -> COUITheme.colorScheme.onSurfaceVariantSummary
            },
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = COUITheme.textStyles.body1,
                color = COUITheme.colorScheme.onSurfaceVariantActions,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
            )
        }
        if (selected) {
            Icon(
                imageVector = COUIIcons.Ok,
                contentDescription = null,
                tint = COUITheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun SoftwareUpdateMenuDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(COUITheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    )
}

@Composable
private fun AboutAuthorGroup(
    onOpenContributors: () -> Unit,
    onOpenReferences: () -> Unit,
) {
    val context = LocalContext.current
    var authorInfoExpanded by rememberSaveable { mutableStateOf(false) }

    SettingsGroup {
        SettingsCardRow(
            title = stringResource(R.string.about_author_info_title),
            summary = stringResource(R.string.about_author_info_summary),
            hasDividerBelow = true,
            onClick = { authorInfoExpanded = !authorInfoExpanded },
            leadingContent = { GitHubAuthorAvatar(MITOVERG_AVATAR_URL) },
            showExpandArrow = true,
            expandArrowExpanded = authorInfoExpanded,
        )
        AnimatedVisibility(
            visible = authorInfoExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(durationMillis = 140)),
        ) {
            Column {
                SettingsDivider()
                SettingsCardRow(
                    title = stringResource(R.string.about_author_coolapk_title),
                    summary = stringResource(R.string.about_author_coolapk_summary),
                    showArrow = true,
                    hasDividerAbove = true,
                    hasDividerBelow = true,
                    onClick = { openUrl(context, COOLAPK_PROFILE_URL) },
                    leadingContent = { AboutRowIcon(R.drawable.ic_coolapk) },
                )
                SettingsDivider()
                SettingsCardRow(
                    title = stringResource(R.string.about_author_github_profile_title),
                    summary = stringResource(R.string.about_author_github_profile_summary),
                    showArrow = true,
                    hasDividerAbove = true,
                    hasDividerBelow = true,
                    onClick = { openUrl(context, GITHUB_PROFILE_URL) },
                    leadingContent = { AboutRowIcon(R.drawable.ic_github) },
                )
            }
        }
        SettingsDivider()
        SettingsCardRow(
            title = stringResource(R.string.about_codex_author_title),
            summary = stringResource(R.string.about_codex_author_summary),
            showArrow = true,
            hasDividerAbove = true,
            hasDividerBelow = true,
            onClick = { openUrl(context, CODEX_PROFILE_URL) },
            leadingContent = { GitHubAuthorAvatar(CODEX_AVATAR_URL) },
        )
        SettingsDivider()
        SettingsCardRow(
            title = stringResource(R.string.about_contributors_title),
            summary = "",
            showArrow = true,
            hasDividerAbove = true,
            hasDividerBelow = true,
            onClick = onOpenContributors,
            leadingContent = { AboutVectorIcon(AppIcons.Heart) },
        )
        SettingsDivider()
        SettingsCardRow(
            title = stringResource(R.string.about_references_title),
            summary = "",
            showArrow = true,
            hasDividerAbove = true,
            hasDividerBelow = true,
            onClick = onOpenReferences,
            leadingContent = { AboutVectorIcon(AppIcons.BookOpen) },
        )
        SettingsDivider()
        SettingsCardRow(
            title = stringResource(R.string.about_github_title),
            summary = stringResource(R.string.about_github_summary),
            showArrow = true,
            hasDividerAbove = true,
            hasDividerBelow = true,
            onClick = { openUrl(context, GITHUB_REPOSITORY_URL) },
            leadingContent = { AboutRowIcon(R.drawable.ic_github) },
        )
        SettingsDivider()
        SettingsCardRow(
            title = stringResource(R.string.about_telegram_title),
            summary = stringResource(R.string.about_telegram_summary),
            showArrow = true,
            hasDividerAbove = true,
            onClick = { openUrl(context, TELEGRAM_CHANNEL_URL) },
            leadingContent = { AboutRowIcon(R.drawable.ic_telegram) },
        )
    }
}

@Composable
fun AboutContributorsPage() {
    val items = listOf(
        AboutLinkItem(
            titleRes = R.string.about_contributor_colorlaris_title,
            summaryRes = R.string.about_contributor_colorlaris_summary,
            url = COLORLARIS_PROFILE_URL,
            leadingContent = { GitHubAuthorAvatar(COLORLARIS_AVATAR_URL) },
        ),
    )
    AboutLinkGroup(items = items)
}

@Composable
fun AboutReferencesPage() {
    val items = listOf(
        AboutLinkItem(
            titleRes = R.string.about_reference_coui_title,
            summaryRes = R.string.about_reference_coui_summary,
            url = COUI_REPOSITORY_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_miuix_title,
            summaryRes = R.string.about_reference_miuix_summary,
            url = MIUIX_REPOSITORY_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_oshin_title,
            summaryRes = R.string.about_reference_oshin_summary,
            url = OSHIN_REPOSITORY_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_yukihookapi_title,
            summaryRes = R.string.about_reference_yukihookapi_summary,
            url = YUKIHOOKAPI_REPOSITORY_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_kernelsu_title,
            summaryRes = R.string.about_reference_kernelsu_summary,
            url = KERNELSU_REPOSITORY_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_libsu_title,
            summaryRes = R.string.about_reference_libsu_summary,
            url = LIBSU_REPOSITORY_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_shizuku_title,
            summaryRes = R.string.about_reference_shizuku_summary,
            url = SHIZUKU_REPOSITORY_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_hidden_api_bypass_title,
            summaryRes = R.string.about_reference_hidden_api_bypass_summary,
            url = HIDDEN_API_BYPASS_REPOSITORY_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_lsposed_title,
            summaryRes = R.string.about_reference_lsposed_summary,
            url = LSPOSED_REPOSITORY_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_jetpack_compose_title,
            summaryRes = R.string.about_reference_jetpack_compose_summary,
            url = JETPACK_COMPOSE_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_kotlin_title,
            summaryRes = R.string.about_reference_kotlin_summary,
            url = KOTLIN_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_android_gradle_plugin_title,
            summaryRes = R.string.about_reference_android_gradle_plugin_summary,
            url = ANDROID_GRADLE_PLUGIN_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_ksp_title,
            summaryRes = R.string.about_reference_ksp_summary,
            url = KSP_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_androidx_title,
            summaryRes = R.string.about_reference_androidx_summary,
            url = ANDROIDX_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_androidx_palette_title,
            summaryRes = R.string.about_reference_androidx_palette_summary,
            url = ANDROIDX_PALETTE_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_material_icons_title,
            summaryRes = R.string.about_reference_material_icons_summary,
            url = MATERIAL_ICONS_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_xposed_api_title,
            summaryRes = R.string.about_reference_xposed_api_summary,
            url = XPOSED_API_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_gaze_capsule_title,
            summaryRes = R.string.about_reference_gaze_capsule_summary,
            url = GAZE_CAPSULE_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_junit_title,
            summaryRes = R.string.about_reference_junit_summary,
            url = JUNIT_URL,
        ),
        AboutLinkItem(
            titleRes = R.string.about_reference_espresso_title,
            summaryRes = R.string.about_reference_espresso_summary,
            url = ESPRESSO_URL,
        ),
    )
    SettingsSection(title = stringResource(R.string.about_references_open_source_section))
    AboutLinkGroup(items = items)
}

@Composable
private fun AboutNoticeCard(
    text: String,
) {
    var visible by rememberSaveable { mutableStateOf(true) }
    if (!visible) return

    SettingsGroup {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = COUITheme.textStyles.body1,
                color = COUITheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(COUITheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    .clickable { visible = false },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "脳",
                    style = COUITheme.textStyles.body1,
                    color = COUITheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AboutLinkGroup(
    items: List<AboutLinkItem>,
) {
    val context = LocalContext.current
    SettingsGroup {
        items.forEachIndexed { index, item ->
            SettingsCardRow(
                title = stringResource(item.titleRes),
                summary = stringResource(item.summaryRes),
                showArrow = true,
                hasDividerAbove = index > 0,
                hasDividerBelow = index < items.lastIndex,
                onClick = { openUrl(context, item.url) },
                leadingContent = item.leadingContent,
            )
            if (index < items.lastIndex) {
                SettingsDivider()
            }
        }
    }
}

@Composable
private fun GitHubAuthorAvatar(
    avatarUrl: String,
) {
    val context = LocalContext.current
    val resolvedAvatarUrl = remember(avatarUrl) { avatarUrl }
    val avatarState by produceState(
        initialValue = AvatarImageState(bitmap = GitHubAvatarCache.get(resolvedAvatarUrl)),
        key1 = resolvedAvatarUrl,
        key2 = context,
    ) {
        if (value.bitmap != null) return@produceState
        value = GitHubAvatarCache.load(context.applicationContext, resolvedAvatarUrl)
    }
    val iconModifier = Modifier
        .size(32.dp)
        .clip(RoundedCornerShape(9.dp))

    val bitmap = avatarState.bitmap
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = iconModifier,
        )
    } else {
        Image(
            painter = painterResource(fallbackAvatarRes(resolvedAvatarUrl)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = iconModifier,
        )
    }
}

@Composable
private fun AboutRowIcon(
    @DrawableRes iconRes: Int,
) {
    Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        colorFilter = ColorFilter.tint(COUITheme.colorScheme.onSurface),
        modifier = Modifier.size(24.dp),
    )
}

@Composable
private fun AboutVectorIcon(
    imageVector: ImageVector,
) {
    Image(
        imageVector = imageVector,
        contentDescription = null,
        colorFilter = ColorFilter.tint(COUITheme.colorScheme.onSurface),
        modifier = Modifier.size(24.dp),
    )
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}

@Composable
fun AppSettingsPage(
    showChinaSpecialFeatures: Boolean,
    onShowChinaSpecialFeaturesChange: (Boolean) -> Unit,
    showGlobalSpecialFeatures: Boolean,
    onShowGlobalSpecialFeaturesChange: (Boolean) -> Unit,
    hapticFeedbackEnabled: Boolean,
    onHapticFeedbackEnabledChange: (Boolean) -> Unit,
    hapticFeedbackPlusEnabled: Boolean,
    onHapticFeedbackPlusEnabledChange: (Boolean) -> Unit,
    blurEffectEnabled: Boolean,
    onBlurEffectEnabledChange: (Boolean) -> Unit,
    appLanguageTag: String,
    onAppLanguageChange: (String) -> Unit,
    appThemeMode: AppThemeMode,
    onAppThemeModeChange: (AppThemeMode) -> Unit,
    appThemeKeyColor: Long?,
    onAppThemeKeyColorChange: (Long?) -> Unit,
    appThemePaletteStyle: Int,
    onAppThemePaletteStyleChange: (Int) -> Unit,
    appThemeColorSpec: Int,
    onAppThemeColorSpecChange: (Int) -> Unit,
    liquidGlassBottomBarEnabled: Boolean,
    onLiquidGlassBottomBarEnabledChange: (Boolean) -> Unit,
    oneChinaPrincipleEnabled: Boolean,
    onOneChinaPrincipleEnabledChange: (Boolean) -> Unit,
    onOpenDeveloperOptions: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val liquidGlassBottomBarSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val exportConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(ConfigBackup.MIME_TYPE),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    ConfigBackup.exportToUri(context, uri)
                }
            }.fold(
                onSuccess = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.config_export_success, uri.lastPathSegment.orEmpty()),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                onFailure = { error ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.config_export_failed, error.localizedMessage.orEmpty()),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }
    val importConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    ConfigBackup.importFromUri(context, uri)
                }
            }.fold(
                onSuccess = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.config_import_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                onFailure = { error ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.config_import_failed, error.localizedMessage.orEmpty()),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }

    SettingsSection(title = stringResource(R.string.settings_section_display))
    SettingsGroup {
        SettingsThemeModeDropdown(
            title = stringResource(R.string.setting_theme_mode),
            selectedMode = appThemeMode,
            onModeChange = onAppThemeModeChange,
            hasDividerBelow = true,
        )
        SettingsDivider()
        AnimatedVisibility(
            visible = appThemeMode.isMonet,
            enter = expandVertically(
                animationSpec = tween(260, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(180)),
            exit = shrinkVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(160)),
        ) {
            Column {
                SettingsThemeKeyColorDropdown(
                    title = stringResource(R.string.setting_theme_key_color),
                    selectedKeyColor = appThemeKeyColor,
                    onKeyColorChange = onAppThemeKeyColorChange,
                    hasDividerAbove = true,
                    hasDividerBelow = true,
                )
                SettingsDivider()
            }
        }
        AnimatedVisibility(
            visible = appThemeMode.isMonet && appThemeKeyColor != null,
            enter = expandVertically(
                animationSpec = tween(260, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(180)),
            exit = shrinkVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(160)),
        ) {
            Column {
                SettingsThemeTextDropdown(
                    title = stringResource(R.string.setting_theme_palette_style),
                    labels = ThemePaletteStyle.entries.map { it.name },
                    selectedIndex = appThemePaletteStyle,
                    onSelectedIndexChange = onAppThemePaletteStyleChange,
                    hasDividerAbove = true,
                    hasDividerBelow = true,
                )
                SettingsDivider()
                SettingsThemeTextDropdown(
                    title = stringResource(R.string.setting_theme_color_spec),
                    labels = ThemeColorSpec.entries.map { it.name },
                    selectedIndex = appThemeColorSpec,
                    onSelectedIndexChange = onAppThemeColorSpecChange,
                    hasDividerAbove = true,
                    hasDividerBelow = true,
                )
                SettingsDivider()
            }
        }
        SettingsCardRow(
            title = stringResource(R.string.setting_app_language),
            summary = stringResource(R.string.setting_app_language_summary),
            hasDividerAbove = true,
            hasDividerBelow = true,
            trailing = when (appLanguageTag) {
                "zh-CN" -> stringResource(R.string.language_simplified_chinese)
                "zh-TW" -> stringResource(R.string.language_traditional_chinese_tw)
                "zh-HK" -> stringResource(R.string.language_traditional_chinese_hk)
                "zh-MO" -> stringResource(R.string.language_traditional_chinese_mo)
                "yue-Hant" -> stringResource(R.string.language_traditional_chinese_cantonese)
                "en" -> "English"
                else -> stringResource(R.string.language_system)
            },
        )
        SettingsDivider()
        AnimatedVisibility(
            visible = isRuntimeShaderSupported(),
            enter = expandVertically(
                animationSpec = tween(260, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(180)),
            exit = shrinkVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing),
            ) + fadeOut(animationSpec = tween(160)),
        ) {
            Column {
                SettingsToggleRow(
                    title = stringResource(R.string.setting_blur_effect),
                    summary = "",
                    checked = blurEffectEnabled,
                    onCheckedChange = onBlurEffectEnabledChange,
                    hasDividerAbove = true,
                    hasDividerBelow = true,
                )
                SettingsDivider()
            }
        }
        SettingsToggleRow(
            title = stringResource(R.string.setting_liquid_glass_bottom_bar),
            summary = stringResource(R.string.setting_liquid_glass_bottom_bar_summary),
            checked = liquidGlassBottomBarEnabled && liquidGlassBottomBarSupported,
            onCheckedChange = {
                if (liquidGlassBottomBarSupported) {
                    onLiquidGlassBottomBarEnabledChange(it)
                }
            },
            hasDividerAbove = true,
            hasDividerBelow = true,
            enabled = liquidGlassBottomBarSupported,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.setting_one_china_principle),
            summary = stringResource(R.string.setting_one_china_principle_summary),
            checked = oneChinaPrincipleEnabled,
            onCheckedChange = onOneChinaPrincipleEnabledChange,
            hasDividerAbove = true,
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.setting_show_cn_special_features),
            summary = stringResource(R.string.setting_show_cn_special_features_summary),
            checked = showChinaSpecialFeatures,
            onCheckedChange = onShowChinaSpecialFeaturesChange,
            hasDividerAbove = true,
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.setting_show_global_special_features),
            summary = stringResource(R.string.setting_show_global_special_features_summary),
            checked = showGlobalSpecialFeatures,
            onCheckedChange = onShowGlobalSpecialFeaturesChange,
            hasDividerAbove = true,
        )
    }

    SettingsSection(title = stringResource(R.string.settings_section_haptics))
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.setting_haptic_feedback),
            summary = "",
            checked = hapticFeedbackEnabled,
            onCheckedChange = onHapticFeedbackEnabledChange,
            hasDividerAbove = false,
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsToggleRow(
            title = stringResource(R.string.setting_haptic_feedback_plus),
            summary = stringResource(R.string.setting_haptic_feedback_plus_summary),
            checked = hapticFeedbackPlusEnabled,
            onCheckedChange = onHapticFeedbackPlusEnabledChange,
            hasDividerAbove = true,
            hasDividerBelow = false,
        )
    }

    SettingsSection(title = stringResource(R.string.section_restore_backup))
    SettingsGroup {
        SettingsCardRow(
            title = stringResource(R.string.setting_export_config),
            summary = stringResource(R.string.setting_export_config_summary),
            showArrow = true,
            onClick = { exportConfigLauncher.launch(ConfigBackup.defaultFileName()) },
            hasDividerBelow = true,
        )
        SettingsDivider()
        SettingsCardRow(
            title = stringResource(R.string.setting_import_config),
            summary = stringResource(R.string.setting_import_config_summary),
            showArrow = true,
            onClick = { importConfigLauncher.launch(arrayOf(ConfigBackup.MIME_TYPE, "text/*")) },
            hasDividerAbove = true,
        )
    }

    SettingsSection(title = stringResource(R.string.settings_section_other))
    SettingsGroup {
        SettingsCardRow(
            title = stringResource(R.string.setting_developer_options),
            summary = "",
            showArrow = true,
            onClick = onOpenDeveloperOptions,
        )
    }
}

@Composable
fun DeveloperOptionsPage(
    showFpsMonitor: Boolean,
    onShowFpsMonitorChange: (Boolean) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_section_display))
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.setting_show_fps_monitor),
            summary = "",
            checked = showFpsMonitor,
            onCheckedChange = onShowFpsMonitorChange,
            hasDividerAbove = false,
            hasDividerBelow = false,
        )
    }

    SettingsSection(title = stringResource(R.string.settings_section_other))
    SettingsGroup {
        SettingsToggleRow(
            title = stringResource(R.string.setting_pop_follows_swipe_edge),
            summary = stringResource(R.string.setting_pop_follows_swipe_edge_summary),
            checked = false,
            onCheckedChange = {},
            hasDividerAbove = false,
            hasDividerBelow = false,
            enabled = false,
        )
    }
}
