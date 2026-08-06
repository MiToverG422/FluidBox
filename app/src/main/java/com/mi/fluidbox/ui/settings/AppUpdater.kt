package com.mi.fluidbox.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.mi.fluidbox.BuildConfig
import com.mi.fluidbox.ui.common.AppLogStore
import com.mi.fluidbox.ui.common.ShellLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.zip.ZipInputStream

object AppUpdater {
    const val INSTALL_PERMISSION_REQUIRED = "Install permission required"

    private const val REPO = "MiToverG422/FluidBox"
    private const val API_BASE = "https://api.github.com/repos/$REPO"
    private const val USER_AGENT = "FluidBox/${BuildConfig.VERSION_NAME}"

    suspend fun checkAndInstall(
        context: Context,
        channel: UpdateChannel,
        installMode: UpdateInstallMode = UpdateInstallMode.Interactive,
    ): UpdateResult =
        withContext(Dispatchers.IO) {
            runCatching {
                val update = when (channel) {
                    UpdateChannel.GitHubReleases -> findLatestRelease()
                    UpdateChannel.GitHubCi -> findLatestCiArtifact()
                } ?: return@withContext UpdateResult.NoUpdate

                if (!isNewerUpdate(update)) {
                    return@withContext UpdateResult.NoUpdate
                }

                val updateDir = File(context.cacheDir, "updates")
                val apkFile = when (update.kind) {
                    UpdatePackageKind.Apk -> downloadApk(updateDir, update.downloadUrl, update.fileName)
                    UpdatePackageKind.ArtifactZip -> downloadArtifactApk(updateDir, update.downloadUrl, update.fileName)
                }
                when (installMode) {
                    UpdateInstallMode.Interactive -> {
                        installApk(context, apkFile)
                        UpdateResult.InstallStarted(update.versionName)
                    }
                    UpdateInstallMode.Silent -> {
                        if (silentInstallApk(apkFile)) {
                            UpdateResult.InstallFinished(update.versionName)
                        } else {
                            UpdateResult.Failed("Silent install failed")
                        }
                    }
                }
            }.getOrElse { error ->
                UpdateResult.Failed(error.message ?: error::class.java.simpleName)
            }
        }

    suspend fun runAutomaticSilentUpdate(context: Context): UpdateResult? {
        val appContext = context.applicationContext
        if (!UpdateChannelPreference.getAutomaticSilentUpdate(appContext)) return null
        if (!UpdateChannelPreference.shouldRunAutomaticSilentUpdate(appContext)) return null
        UpdateChannelPreference.markAutomaticSilentUpdateChecked(appContext)
        AppLogStore.i("Updater", "Automatic silent update check started")
        return checkAndInstall(
            context = appContext,
            channel = UpdateChannelPreference.get(appContext),
            installMode = UpdateInstallMode.Silent,
        ).also { result ->
            AppLogStore.i("Updater", "Automatic silent update result: $result")
        }
    }

    private fun findLatestRelease(): UpdatePackage? {
        return findLatestReleaseAsset("$API_BASE/releases/latest")
    }

    private fun findLatestCiRelease(): UpdatePackage? {
        return runCatching {
            findLatestReleaseAsset("$API_BASE/releases/tags/ci-latest")
        }.getOrNull()
    }

    private fun findLatestReleaseAsset(url: String): UpdatePackage? {
        val json = JSONObject(getText(url))
        val assets = json.optJSONArray("assets") ?: JSONArray()
        val asset = firstApkAsset(assets) ?: return null
        val assetName = asset.optString("name", "")
        val versionName = extractReleaseVersionName(
            tagName = json.optString("tag_name"),
            releaseName = json.optString("name"),
            assetName = assetName,
        ) ?: return null
        return UpdatePackage(
            versionName = versionName,
            downloadUrl = asset.getString("browser_download_url"),
            fileName = assetName.ifBlank { "fluidbox-$versionName-release.apk" },
            kind = UpdatePackageKind.Apk,
        )
    }

    private fun findLatestCiArtifact(): UpdatePackage? {
        findLatestCiRelease()?.let { return it }
        val runsJson = getLatestCiRunsJson()
        val runs = runsJson.optJSONArray("workflow_runs") ?: return null
        for (runIndex in 0 until runs.length()) {
            val run = runs.optJSONObject(runIndex) ?: continue
            val artifactsUrl = run.optString("artifacts_url").takeIf { it.isNotBlank() } ?: continue
            val artifactsJson = JSONObject(getText(artifactsUrl))
            val artifacts = artifactsJson.optJSONArray("artifacts") ?: continue
            val artifact = firstUsableArtifact(artifacts) ?: continue
            val artifactName = artifact.optString("name")
            val versionName = artifactName.removePrefix("fluidbox-").ifBlank {
                run.optString("head_sha").takeIf { it.length >= 8 }
                    ?.let { "16.0-CI-${it.take(8)}" }
                    .orEmpty()
            }
            if (versionName.isBlank()) continue
            if (run.optLong("run_number") > BuildConfig.VERSION_CODE.toLong()) {
                error("CI APK is not published to ci-latest release yet")
            }
            return null
        }
        return null
    }

    private fun getLatestCiRunsJson(): JSONObject {
        val requests = listOf(
            "$API_BASE/actions/runs?branch=main&status=success&per_page=10",
            "$API_BASE/actions/runs?branch=master&status=success&per_page=10",
            "$API_BASE/actions/runs?status=success&per_page=10",
        )
        var lastError: Throwable? = null
        for (request in requests) {
            val result = runCatching { JSONObject(getText(request)) }
            val json = result.getOrNull()
            if (json?.optJSONArray("workflow_runs")?.length()?.let { it > 0 } == true) {
                return json
            }
            lastError = result.exceptionOrNull()
        }
        lastError?.let { throw it }
        return JSONObject("""{"workflow_runs":[]}""")
    }

    private fun firstApkAsset(assets: JSONArray): JSONObject? {
        for (index in 0 until assets.length()) {
            val asset = assets.optJSONObject(index) ?: continue
            val name = asset.optString("name").lowercase(Locale.ROOT)
            if (name.endsWith(".apk") && !name.contains("debug")) {
                return asset
            }
        }
        for (index in 0 until assets.length()) {
            val asset = assets.optJSONObject(index) ?: continue
            if (asset.optString("name").lowercase(Locale.ROOT).endsWith(".apk")) {
                return asset
            }
        }
        return null
    }

    private fun firstUsableArtifact(artifacts: JSONArray): JSONObject? {
        for (index in 0 until artifacts.length()) {
            val artifact = artifacts.optJSONObject(index) ?: continue
            val name = artifact.optString("name").lowercase(Locale.ROOT)
            if (
                name.startsWith("fluidbox-") &&
                artifact.optBoolean("expired").not() &&
                artifact.optString("archive_download_url").isNotBlank()
            ) {
                return artifact
            }
        }
        return null
    }

    private fun extractReleaseVersionName(
        tagName: String,
        releaseName: String,
        assetName: String,
    ): String? {
        val candidates = listOf(
            tagName.removeVersionPrefix(),
            releaseName
                .removePrefix("FluidBox CI Latest ")
                .removePrefix("FluidBox ")
                .removeVersionPrefix(),
            assetName
                .removePrefix("fluidbox-")
                .removeSuffix(".apk")
                .removeSuffix("-release")
                .removeSuffix("-debug")
                .removeVersionPrefix(),
        )
        return candidates
            .map { it.trim() }
            .firstOrNull { candidate -> candidate.isComparableVersionName() }
            ?: candidates
                .asSequence()
                .mapNotNull { candidate -> VERSION_NAME_REGEX.find(candidate)?.value }
                .firstOrNull()
    }

    private fun downloadApk(
        updateDir: File,
        downloadUrl: String,
        fileName: String,
    ): File {
        val output = updateFile(updateDir, fileName.ensureApkSuffix())
        downloadToFile(downloadUrl, output)
        return output
    }

    private fun downloadArtifactApk(
        updateDir: File,
        downloadUrl: String,
        fileName: String,
    ): File {
        val zipFile = updateFile(updateDir, fileName.ensureZipSuffix())
        downloadToFile(downloadUrl, zipFile)
        ZipInputStream(zipFile.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory && entry.name.lowercase(Locale.ROOT).endsWith(".apk")) {
                    val output = updateFile(updateDir, File(entry.name).name.ensureApkSuffix())
                    FileOutputStream(output).use { stream ->
                        zip.copyTo(stream)
                    }
                    return output
                }
            }
        }
        error("No APK found in CI artifact")
    }

    private fun downloadToFile(downloadUrl: String, output: File) {
        output.parentFile?.mkdirs()
        val connection = openConnection(downloadUrl)
        try {
            connection.inputStream.use { input ->
                FileOutputStream(output).use { outputStream ->
                    input.copyTo(outputStream)
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun getText(url: String): String {
        val connection = openConnection(url)
        try {
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(url: String): HttpURLConnection {
        var requestUrl = url
        repeat(6) {
            val connection = URL(requestUrl).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "application/vnd.github+json, application/octet-stream")
            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                if (location.isNullOrBlank()) error("GitHub request redirected without Location")
                requestUrl = URL(URL(requestUrl), location).toString()
                return@repeat
            }
            if (code !in 200..299) {
                val message = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()
                error("GitHub request failed: HTTP $code ${message.take(120)}")
            }
            return connection
        }
        error("GitHub request redirected too many times")
    }

    private fun installApk(context: Context, apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settingsIntent)
            error(INSTALL_PERMISSION_REQUIRED)
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )
        val installIntent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(apkUri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(installIntent)
        } catch (_: ActivityNotFoundException) {
            error("No installer available")
        }
    }

    private fun silentInstallApk(apkFile: File): Boolean {
        val result = ShellLogger.exec(
            tag = "Updater",
            "pm install -r ${apkFile.absolutePath.shellQuote()}",
        )
        return result.isSuccess
    }

    private fun updateFile(updateDir: File, fileName: String): File {
        return File(updateDir, fileName)
    }

    private fun isNewerUpdate(update: UpdatePackage): Boolean {
        if (isNewerVersion(update.versionName, BuildConfig.VERSION_NAME)) return true
        return update.versionCode?.let { it > BuildConfig.VERSION_CODE.toLong() } == true
    }

    private fun isNewerVersion(remote: String, current: String): Boolean {
        if (remote.isBlank()) return false
        if (remote == current) return false
        val remoteVersion = ParsedVersion.parse(remote) ?: return false
        val currentVersion = ParsedVersion.parse(current) ?: return true
        val remoteParts = remoteVersion.parts
        val currentParts = currentVersion.parts
        for (index in 0 until maxOf(remoteParts.size, currentParts.size)) {
            val remotePart = remoteParts.getOrElse(index) { 0 }
            val currentPart = currentParts.getOrElse(index) { 0 }
            if (remotePart != currentPart) return remotePart > currentPart
        }
        for (index in 0 until maxOf(remoteVersion.qualifierParts.size, currentVersion.qualifierParts.size)) {
            val remotePart = remoteVersion.qualifierParts.getOrElse(index) { 0 }
            val currentPart = currentVersion.qualifierParts.getOrElse(index) { 0 }
            if (remotePart != currentPart) return remotePart > currentPart
        }
        if (remoteVersion.qualifierRank != currentVersion.qualifierRank) {
            return remoteVersion.qualifierRank > currentVersion.qualifierRank
        }
        return remoteVersion.qualifierText != currentVersion.qualifierText
    }

    private fun String.isComparableVersionName(): Boolean {
        return matches(VERSION_NAME_REGEX)
    }

    private fun String.removeVersionPrefix(): String {
        return trim().replaceFirst(Regex("^v", RegexOption.IGNORE_CASE), "")
    }

    private fun String.ensureApkSuffix() =
        if (lowercase(Locale.ROOT).endsWith(".apk")) this else "$this.apk"

    private fun String.ensureZipSuffix() =
        if (lowercase(Locale.ROOT).endsWith(".zip")) this else "$this.zip"

    private fun String.shellQuote(): String {
        return "'${replace("'", "'\\''")}'"
    }
}

private val VERSION_NAME_REGEX =
    Regex("""\d+(?:\.\d+)+(?:-(?:BETA(?:\d+(?:\.\d+)*)?|CI-[A-Za-z0-9]+))?""", RegexOption.IGNORE_CASE)

private data class ParsedVersion(
    val parts: List<Int>,
    val qualifierRank: Int,
    val qualifierParts: List<Int>,
    val qualifierText: String,
) {
    companion object {
        fun parse(version: String): ParsedVersion? {
            val normalized = version.trim().replaceFirst(Regex("^v", RegexOption.IGNORE_CASE), "")
            val match = VERSION_NAME_REGEX.find(normalized) ?: return null
            val comparable = match.value
            val base = comparable.substringBefore("-")
            val qualifier = comparable.substringAfter("-", missingDelimiterValue = "")
            val parts = base.split('.').mapNotNull { it.toIntOrNull() }
            if (parts.isEmpty()) return null
            val upperQualifier = qualifier.uppercase(Locale.ROOT)
            val qualifierRank = when {
                upperQualifier.startsWith("CI-") -> 1
                upperQualifier.startsWith("BETA") -> 2
                upperQualifier.isBlank() -> 3
                else -> 0
            }
            val qualifierParts = when {
                upperQualifier.startsWith("BETA") ->
                    upperQualifier
                        .removePrefix("BETA")
                        .takeIf { it.isNotBlank() }
                        ?.split('.')
                        ?.mapNotNull { it.toIntOrNull() }
                        .orEmpty()
                else -> emptyList()
            }
            return ParsedVersion(
                parts = parts,
                qualifierRank = qualifierRank,
                qualifierParts = qualifierParts,
                qualifierText = upperQualifier,
            )
        }
    }
}

enum class UpdateInstallMode {
    Interactive,
    Silent,
}

sealed interface UpdateResult {
    data object NoUpdate : UpdateResult
    data class InstallStarted(val versionName: String) : UpdateResult
    data class InstallFinished(val versionName: String) : UpdateResult
    data class Failed(val reason: String) : UpdateResult
}

private enum class UpdatePackageKind {
    Apk,
    ArtifactZip,
}

private data class UpdatePackage(
    val versionName: String,
    val versionCode: Long? = null,
    val downloadUrl: String,
    val fileName: String,
    val kind: UpdatePackageKind,
)
