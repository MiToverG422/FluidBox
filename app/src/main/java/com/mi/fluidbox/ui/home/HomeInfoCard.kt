package com.mi.fluidbox.ui.home

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mi.fluidbox.R
import com.mi.fluidbox.ui.settings.SettingsDivider
import com.mi.fluidbox.ui.settings.SettingsRowTextContent
import com.mi.fluidbox.ui.settings.SettingsTokens
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.Card
import java.util.Locale

@Composable
fun HomeInfoCard(
    oneChinaPrincipleEnabled: Boolean,
) {
    val context = LocalContext.current
    val systemVersion = Build.DISPLAY.ifBlank { Build.VERSION.INCREMENTAL }
    val kernelVersion = remember {
        System.getProperty("os.version")
            ?.lineSequence()
            ?.firstOrNull()
            ?.trim()
            .orEmpty()
    }
    val regionText = remember(context, oneChinaPrincipleEnabled) {
        detectHomeRegionText(context, oneChinaPrincipleEnabled)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            HomeInfoBasicRow(
                label = stringResource(R.string.home_info_region),
                value = regionText,
            )
            SettingsDivider()
            HomeInfoBasicRow(
                label = stringResource(R.string.home_info_android_api),
                value = stringResource(
                    R.string.home_dash_android_api_format,
                    Build.VERSION.RELEASE,
                    Build.VERSION.SDK_INT,
                ),
            )
            SettingsDivider()
            HomeInfoBasicRow(
                label = stringResource(R.string.info_device_model),
                value = Build.MODEL,
            )
            SettingsDivider()
            HomeInfoBasicRow(
                label = stringResource(R.string.home_info_system_version),
                value = systemVersion,
            )
            SettingsDivider()
            HomeInfoBasicRow(
                label = stringResource(R.string.home_info_system_architecture),
                value = Build.SUPPORTED_ABIS.joinToString(" / ")
                    .ifBlank { stringResource(R.string.home_info_unknown) },
            )
            SettingsDivider()
            HomeInfoBasicRow(
                label = stringResource(R.string.home_info_system_fingerprint),
                value = Build.FINGERPRINT.ifBlank { stringResource(R.string.home_info_unknown) },
            )
            SettingsDivider()
            HomeInfoBasicRow(
                label = stringResource(R.string.home_info_kernel_version),
                value = kernelVersion.ifBlank { stringResource(R.string.home_info_unknown) },
            )
        }
    }
}

private fun detectHomeRegionText(
    context: Context,
    oneChinaPrincipleEnabled: Boolean,
): String {
    val nvid = readHomeSystemProperty("ro.build.oplus_nv_id")?.trim()
    val nvidRegion = nvid?.let { mapOplusNvidRegion(it, oneChinaPrincipleEnabled) }
    if (nvidRegion != null) return nvidRegion

    val propertyRegion = listOf(
        "ro.oplus.regionmark",
        "ro.oplus.region",
        "ro.vendor.oplus.regionmark",
        "persist.sys.oplus.region",
        "ro.product.locale.region",
    ).asSequence()
        .mapNotNull(::readHomeSystemProperty)
        .map(String::trim)
        .firstOrNull(String::isNotEmpty)
        ?.let(::normalizeHomeRegionCode)

    if (propertyRegion != null) {
        return formatHomeRegionWithOneChinaPolicy(propertyRegion, oneChinaPrincipleEnabled)
    }

    if (!nvid.isNullOrBlank() && !nvid.equals("null", ignoreCase = true)) {
        return "NV $nvid"
    }

    val localeRegion = context.resources.configuration.locales[0].country
        .ifBlank { "XX" }
        .uppercase(Locale.ROOT)
    return formatHomeRegionWithOneChinaPolicy(localeRegion, oneChinaPrincipleEnabled)
}

private fun mapOplusNvidRegion(
    nvid: String,
    oneChinaPrincipleEnabled: Boolean,
): String? = when (nvid) {
    "10010111" -> formatHomeRegion("CN")
    "00011010" -> formatHomeRegion("TW", oneChinaPrincipleEnabled)
    "00110111" -> formatHomeRegion("RU")
    "01000100" -> formatHomeRegion("GDPR_EU")
    "10001101" -> formatHomeRegion("GDPR_EUROPE")
    "00011011" -> formatHomeRegion("IN")
    "00110011" -> formatHomeRegion("ID")
    "00111000" -> formatHomeRegion("MY")
    "00111001" -> formatHomeRegion("TH")
    "00111110" -> formatHomeRegion("PH")
    "10000011" -> formatHomeRegion("SA")
    "10011010" -> formatHomeRegion("LATAM")
    "10011110" -> formatHomeRegion("BR")
    "10100110" -> formatHomeRegion("MEA")
    else -> null
}

private fun normalizeHomeRegionCode(raw: String): String? {
    val normalized = raw
        .replace('-', '_')
        .substringAfterLast('_')
        .uppercase(Locale.ROOT)
    val mapped = when (normalized) {
        "INDIA" -> "IN"
        "CHINA" -> "CN"
        "HONGKONG", "HONG_KONG" -> "HK"
        "TAIWAN" -> "TW"
        "GLOBAL", "ROW", "WW", "EUEX" -> "GLO"
        else -> normalized
    }
    return mapped.takeIf { code ->
        code.length in 2..3 && code.all { it in 'A'..'Z' }
    }
}

private fun formatHomeRegionWithOneChinaPolicy(
    code: String,
    oneChinaPrincipleEnabled: Boolean,
): String {
    return if (code.uppercase(Locale.ROOT) == "TW") {
        formatHomeRegion("TW", oneChinaPrincipleEnabled)
    } else {
        formatHomeRegion(code)
    }
}

private fun formatHomeRegion(
    code: String,
    oneChinaPrincipleEnabled: Boolean = false,
): String {
    val normalized = code.uppercase(Locale.ROOT)
    return when (normalized) {
        "CN" -> "CN 中国 中國 China 🇨🇳"
        "TW" -> if (oneChinaPrincipleEnabled) {
            "CNTW 中国台湾 中國臺灣 Taiwan, China 🇨🇳"
        } else {
            "TW 中华民国台湾 中華民國臺灣 Taiwan 🇹🇼"
        }
        "HK" -> "HK 香港 Hong Kong 🇭🇰"
        "RU" -> "RU 俄罗斯 俄羅斯 Russia 🇷🇺"
        "EU", "GDPR_EU" -> "GDPR 欧盟 歐盟 EU 🇪🇺"
        "GDPR_EUROPE" -> "GDPR 欧洲 歐洲 Europe 🇪🇺"
        "IN" -> "IN 印度 India 🇮🇳"
        "ID" -> "ID 印度尼西亚 印度尼西亞 Indonesia 🇮🇩"
        "MY" -> "MY 马来西亚 馬來西亞 Malaysia 🇲🇾"
        "TH" -> "TH 泰国 泰國 Thailand 🇹🇭"
        "PH" -> "PH 菲律宾 菲律賓 Philippines 🇵🇭"
        "SA" -> "SA 沙特阿拉伯 沙烏地阿拉伯 Saudi Arabia 🇸🇦"
        "LATAM" -> "LATAM 拉丁美洲 Latin America"
        "BR" -> "BR 巴西 Brazil 🇧🇷"
        "MEA", "ME" -> "MEA 中东和非洲 中東和非洲 The Middle East and Africa"
        "GLO", "GLOBAL" -> "GLO 全球 Global 🌐"
        else -> {
            val flag = regionalFlag(normalized)
            flag?.let { "$normalized $it" } ?: normalized
        }
    }
}

private fun regionalFlag(code: String): String? {
    if (code.length != 2 || code.any { it !in 'A'..'Z' }) return null
    val builder = StringBuilder()
    code.forEach { letter ->
        builder.appendCodePoint(0x1F1E6 + (letter - 'A'))
    }
    return builder.toString()
}

fun readHomeSystemProperty(key: String): String? {
    return runCatching {
        Class.forName("android.os.SystemProperties")
            .getMethod("get", String::class.java, String::class.java)
            .invoke(null, key, "") as? String
    }.getOrNull()?.takeIf(String::isNotBlank)
}

@Composable
private fun HomeInfoBasicRow(
    label: String,
    value: String,
) {
    BasicComponent(
        insideMargin = SettingsTokens.BasicComponentInsideMargin,
    ) {
        SettingsRowTextContent(
            title = label,
            summary = value,
        )
    }
}
