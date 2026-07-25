package com.mi.fluidbox.ui.common

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object ConfigBackup {
    private const val BACKUP_VERSION = 1
    private const val APP_PREFS_NAME = "fluidbox_prefs"
    private const val LSP_PREFS_NAME = "lsp_features"
    private const val TYPE_BOOLEAN = "boolean"
    private const val TYPE_INT = "int"
    private const val TYPE_LONG = "long"
    private const val TYPE_FLOAT = "float"
    private const val TYPE_STRING = "string"
    private const val TYPE_STRING_SET = "string_set"

    private val supportedPrefs = listOf(APP_PREFS_NAME, LSP_PREFS_NAME)

    fun exportToJson(context: Context): String {
        val prefsJson = JSONObject()
        supportedPrefs.forEach { prefsName ->
            prefsJson.put(prefsName, prefsToJson(prefs(context, prefsName)))
        }

        return JSONObject()
            .put("version", BACKUP_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("preferences", prefsJson)
            .toString(2)
    }

    fun importFromJson(context: Context, jsonText: String) {
        val root = JSONObject(jsonText)
        val version = root.optInt("version", -1)
        require(version == BACKUP_VERSION) { "Unsupported config version: $version" }

        val prefsRoot = root.getJSONObject("preferences")
        supportedPrefs.forEach { prefsName ->
            if (prefsRoot.has(prefsName)) {
                restorePrefsFromJson(prefs(context, prefsName), prefsRoot.getJSONObject(prefsName))
            }
        }
    }

    private fun prefs(context: Context, name: String): SharedPreferences {
        return if (name == LSP_PREFS_NAME) {
            context.createDeviceProtectedStorageContext()
                .getSharedPreferences(name, Context.MODE_PRIVATE)
        } else {
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }
    }

    private fun prefsToJson(prefs: SharedPreferences): JSONObject {
        val json = JSONObject()
        prefs.all.forEach { (key, value) ->
            json.put(key, prefValueToJson(value))
        }
        return json
    }

    private fun prefValueToJson(value: Any?): JSONObject {
        return when (value) {
            is Boolean -> JSONObject().put("type", TYPE_BOOLEAN).put("value", value)
            is Int -> JSONObject().put("type", TYPE_INT).put("value", value)
            is Long -> JSONObject().put("type", TYPE_LONG).put("value", value)
            is Float -> JSONObject().put("type", TYPE_FLOAT).put("value", value.toDouble())
            is String -> JSONObject().put("type", TYPE_STRING).put("value", value)
            is Set<*> -> JSONObject()
                .put("type", TYPE_STRING_SET)
                .put("value", JSONArray(value.filterIsInstance<String>()))
            else -> JSONObject().put("type", TYPE_STRING).put("value", value?.toString().orEmpty())
        }
    }

    private fun restorePrefsFromJson(prefs: SharedPreferences, json: JSONObject) {
        val editor = prefs.edit().clear()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val item = json.optJSONObject(key) ?: continue
            when (item.optString("type")) {
                TYPE_BOOLEAN -> editor.putBoolean(key, item.getBoolean("value"))
                TYPE_INT -> editor.putInt(key, item.getInt("value"))
                TYPE_LONG -> editor.putLong(key, item.getLong("value"))
                TYPE_FLOAT -> editor.putFloat(key, item.getDouble("value").toFloat())
                TYPE_STRING -> editor.putString(key, item.optString("value", ""))
                TYPE_STRING_SET -> {
                    val values = item.optJSONArray("value") ?: JSONArray()
                    editor.putStringSet(
                        key,
                        buildSet {
                            for (index in 0 until values.length()) {
                                values.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                            }
                        }
                    )
                }
            }
        }
        require(editor.commit()) { "Failed to write preferences" }
    }
}
