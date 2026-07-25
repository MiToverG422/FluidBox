package com.mi.mibox.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.mi.mibox.ui.common.RootAccessState
import com.mi.mibox.ui.common.readCachedRootAccessInfo
import com.topjohnwu.superuser.Shell
import kotlin.math.abs

data class BatterySnapshot(
    val level: Int,
    val scale: Int,
    val percent: Int,
    val status: Int,
    val plugged: Int,
    val health: Int,
    val cycleCount: Int?,
    val temperatureTenthsC: Int?,
    val voltageMv: Int?,
    val currentNowUa: Long?,
    val displayCurrentMa: Double?,
    val displayVoltageMv: Int?,
    val displayPowerWatts: Double?,
    val currentAverageUa: Long?,
    val chargeCounterUah: Long?,
    val energyCounterNwh: Long?,
    val technology: String?,
    val oplus: OplusBatteryInfo?
) {
    val isCharging: Boolean
        get() = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

    val powerWatts: Double?
        get() {
            displayPowerWatts?.let { return it }
            val voltage = displayVoltageMv ?: voltageMv ?: return null
            val currentMa = displayCurrentMa ?: currentNowUa?.let(::normalizeCurrentToMa)
                ?: currentAverageUa?.let(::normalizeCurrentToMa)
                ?: return null
            return computeDisplayPowerWatts(plugged, voltage, currentMa)
        }
}

data class OplusBatteryInfo(
    val remainingCapacityMah: Int?,
    val fullChargeCapacityMah: Int?,
    val stateOfHealthPercent: Int?,
    val underVoltageThresholdMv: Int?,
    val chargingProtocol: OplusChargingProtocolInfo?,
    val serialNumber: String?,
    val manufactureDate: String?,
    val batteryType: String?,
    val designCapacityMah: Int?,
    val qmaxMah: Int?,
    val voltage0Mv: Int?,
    val voltage1Mv: Int?,
    val bccCurrentMa: Int?
)

data class OplusChargingProtocolInfo(
    val protocolType: String?,
    val fastChargeType: String?,
    val chargeTechnology: String?,
    val isVoocCharging: Boolean,
    val isPpsCharging: Boolean,
    val ppsPower: String?
) {
    val isFastCharging: Boolean
        get() = isVoocCharging ||
            isPpsCharging ||
            protocolCode in setOf(1, 2, 3, 4, 5, 6) ||
            fastChargeCode in setOf(1, 2, 3, 4, 5, 6) ||
            ppsPower.toIntOrNullTrimmed()?.let { it > 0 } == true

    val protocolCode: Int?
        get() = protocolType.toIntOrNullTrimmed()

    val fastChargeCode: Int?
        get() = fastChargeType.toIntOrNullTrimmed()

    val chargeTechnologyCode: Int?
        get() = chargeTechnology.toIntOrNullTrimmed()
}

object BatteryMonitor {
    private const val OPLUS_BATTERY_PATH = "/sys/class/oplus_chg/battery"
    private const val OPLUS_COMMON_PATH = "/sys/class/oplus_chg/common"
    private const val OPLUS_USB_PATH = "/sys/class/oplus_chg/usb"
    private const val BCC_VOLTAGE_0_INDEX = 6
    private const val BCC_CURRENT_INDEX = 8
    private const val BCC_VOLTAGE_1_INDEX = 11

    fun query(context: Context, includeOplus: Boolean = true): BatterySnapshot {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryManager = context.getSystemService(BatteryManager::class.java)
        val oplusInfo = if (includeOplus) queryOplusBatteryInfo(context) else null
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val dualCellMode = BatteryPreferences.isDualCellMode(context)
        val seriesBatteryMode = BatteryPreferences.isSeriesBatteryMode(context)
        val fallbackVoltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, Int.MIN_VALUE)
            ?.takeIf { it > 0 }
        val currentNow = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            .takeIf { it != Long.MIN_VALUE && it != 0L }
        val currentAverage = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            .takeIf { it != Long.MIN_VALUE && it != 0L }
        val displayVoltageMv = resolveDisplayVoltageMv(
            oplusInfo = oplusInfo,
            fallbackVoltageMv = fallbackVoltageMv,
            seriesBatteryMode = seriesBatteryMode
        )
        val currentMultiplier = if (dualCellMode) 2.0 else 1.0
        val displayCurrentMa = (
            oplusInfo?.bccCurrentMa?.toDouble()
                ?: currentNow?.let(::normalizeCurrentToMa)
                ?: currentAverage?.let(::normalizeCurrentToMa)
            )?.times(currentMultiplier)
        val displayPowerWatts = if (displayVoltageMv != null && displayCurrentMa != null) {
            computeDisplayPowerWatts(plugged, displayVoltageMv, displayCurrentMa)
        } else null

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)?.takeIf { it >= 0 } ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)?.takeIf { it > 0 } ?: 100
        val percent = if (level >= 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                .takeIf { it != Int.MIN_VALUE }
                ?.coerceIn(0, 100)
                ?: 0
        }

        return BatterySnapshot(
            level = level,
            scale = scale,
            percent = percent,
            status = status,
            plugged = plugged,
            health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                ?: BatteryManager.BATTERY_HEALTH_UNKNOWN,
            cycleCount = if (Build.VERSION.SDK_INT >= 34) {
                intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, Int.MIN_VALUE)
                    ?.takeIf { it >= 0 }
            } else {
                null
            },
            temperatureTenthsC = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
                ?.takeIf { it != Int.MIN_VALUE },
            voltageMv = fallbackVoltageMv,
            currentNowUa = currentNow,
            displayCurrentMa = displayCurrentMa,
            displayVoltageMv = displayVoltageMv,
            displayPowerWatts = displayPowerWatts,
            currentAverageUa = currentAverage,
            chargeCounterUah = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                .takeIf { it != Long.MIN_VALUE && it > 0L },
            energyCounterNwh = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
                .takeIf { it != Long.MIN_VALUE && it > 0L },
            technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
                ?.takeIf { it.isNotBlank() },
            oplus = oplusInfo
        )
    }

    private fun queryOplusBatteryInfo(context: Context): OplusBatteryInfo? {
        val hasRoot = readCachedRootAccessInfo(context)?.state == RootAccessState.Granted
        if (!hasRoot) return null

        val fields = listOf(
            "battery_rm",
            "battery_fcc",
            "battery_soh",
            "vbat_uv",
            "battery_sn",
            "battery_manu_date",
            "battery_type",
            "design_capacity",
            "bcc_parms",
            "charge_technology",
            "voocchg_ing",
            "ppschg_ing",
            "ppschg_power",
            "battery_log_head",
            "battery_log_content"
        )
        val paths = fields.associateWith { "$OPLUS_BATTERY_PATH/$it" } +
            mapOf(
                "protocol_type" to "$OPLUS_COMMON_PATH/protocol_type",
                "fast_chg_type" to "$OPLUS_USB_PATH/fast_chg_type"
            )
        val values = readRootFiles(paths)
        val bccParts = values["bcc_parms"].orEmpty().split(',').map { it.trim() }
        val logMap = parseBatteryLog(values["battery_log_head"], values["battery_log_content"])
        val fcc = values["battery_fcc"].toIntOrNullTrimmed()
        val qmax = logMap["batt_qmax"].toIntOrNullTrimmed()?.let { normalizeQmax(it, fcc) }

        return OplusBatteryInfo(
            remainingCapacityMah = values["battery_rm"].toIntOrNullTrimmed(),
            fullChargeCapacityMah = fcc,
            stateOfHealthPercent = values["battery_soh"].toIntOrNullTrimmed(),
            underVoltageThresholdMv = values["vbat_uv"].toIntOrNullTrimmed(),
            chargingProtocol = OplusChargingProtocolInfo(
                protocolType = values["protocol_type"].nonBlank(),
                fastChargeType = values["fast_chg_type"].nonBlank(),
                chargeTechnology = values["charge_technology"].nonBlank(),
                isVoocCharging = values["voocchg_ing"].isOne(),
                isPpsCharging = values["ppschg_ing"].isOne(),
                ppsPower = values["ppschg_power"].nonBlank()
            ),
            serialNumber = values["battery_sn"].nonBlank(),
            manufactureDate = values["battery_manu_date"].nonBlank(),
            batteryType = values["battery_type"].nonBlank(),
            designCapacityMah = values["design_capacity"].toIntOrNullTrimmed(),
            qmaxMah = qmax,
            voltage0Mv = bccParts.getOrNull(BCC_VOLTAGE_0_INDEX).toIntOrNullTrimmed(),
            voltage1Mv = bccParts.getOrNull(BCC_VOLTAGE_1_INDEX).toIntOrNullTrimmed(),
            bccCurrentMa = bccParts.getOrNull(BCC_CURRENT_INDEX).toIntOrNullTrimmed()
        )
    }

    private fun readRootFiles(paths: Map<String, String>): Map<String, String?> {
        if (paths.isEmpty()) return emptyMap()
        val command = paths.entries.joinToString("\n") { (key, path) ->
            "printf '${key}='; cat '${path}' 2>/dev/null; printf '\\n'"
        }
        val result = runCatching { Shell.cmd(command).exec() }.getOrNull() ?: return emptyMap()
        if (!result.isSuccess) return emptyMap()
        return result.out
            .mapNotNull { line ->
                val index = line.indexOf('=')
                if (index <= 0) null else line.substring(0, index) to line.substring(index + 1).trim()
            }
            .toMap()
    }

    private fun parseBatteryLog(headLine: String?, valueLine: String?): Map<String, String> {
        val heads = headLine?.split(',') ?: return emptyMap()
        val values = valueLine?.split(',') ?: return emptyMap()
        if (heads.size != values.size) return emptyMap()
        return heads.indices
            .filter { it > 0 }
            .associate { index -> heads[index].trim() to values[index].trim() }
    }

    private fun normalizeQmax(rawQ: Int, fcc: Int?): Int {
        var q = rawQ
        val reference = fcc ?: 20_000
        while (q >= reference * 2) {
            q /= 10
        }
        return q
    }
}

private fun String?.nonBlank(): String? = this?.trim()?.takeIf { it.isNotBlank() }

private fun String?.toIntOrNullTrimmed(): Int? = this?.trim()?.toIntOrNull()

private fun String?.isOne(): Boolean = this?.trim() == "1"

private fun resolveDisplayVoltageMv(
    oplusInfo: OplusBatteryInfo?,
    fallbackVoltageMv: Int?,
    seriesBatteryMode: Boolean
): Int? {
    val voltage0 = oplusInfo?.voltage0Mv?.takeIf { it > 0 }
    val voltage1 = oplusInfo?.voltage1Mv?.takeIf { it > 0 }
    return if (seriesBatteryMode) {
        when {
            voltage0 != null && voltage1 != null -> voltage0 + voltage1
            voltage0 != null -> voltage0 * 2
            fallbackVoltageMv != null -> fallbackVoltageMv * 2
            else -> null
        }
    } else {
        when {
            voltage0 != null && voltage1 != null -> (voltage0 + voltage1) / 2
            voltage0 != null -> voltage0
            else -> fallbackVoltageMv
        }
    }
}

private fun computeDisplayPowerWatts(plugged: Int, voltageMv: Int, currentMa: Double): Double? {
    if (voltageMv <= 0 || currentMa == 0.0) return null
    val orientedCurrentMa = if (plugged == 0) -abs(currentMa) else currentMa
    return orientedCurrentMa * voltageMv.toDouble() / 1_000_000.0
}

private fun normalizeCurrentToMa(raw: Long): Double {
    val value = raw.toDouble()
    return if (abs(raw) >= 100_000L) {
        value / 1000.0
    } else {
        value
    }
}
