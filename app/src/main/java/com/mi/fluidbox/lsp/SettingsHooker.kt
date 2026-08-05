package com.mi.fluidbox.lsp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mi.fluidbox.R
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

object SettingsHooker {
    private const val MODULE_PACKAGE = "com.mi.fluidbox"
    private const val ENTRY_KEY = "statusbar_aod_icons_entry"
    private const val SCREEN_MARKER = "oxygen_material_module_settings_screen"

    private val hookedLoaders = ConcurrentHashMap.newKeySet<Int>()

    fun hook(classLoader: ClassLoader, packageName: String) {
        if (packageName != "com.android.settings") return
        if (!hookedLoaders.add(System.identityHashCode(classLoader))) return

        hookConfigureNotificationRedirect(classLoader)
        hookForceGoogleEntry(classLoader)
        hookPreferenceFragment(classLoader)
        hookDashboardFragments(classLoader)
    }

    private fun hookForceGoogleEntry(classLoader: ClassLoader) {
        val hookClass = XposedHelpers.findClassIfExists(
            "com.oplus.settings.feature.homepage.controller.GooglePreferenceController",
            classLoader
        ) ?: return
        runCatching {
            XposedBridge.hookAllMethods(hookClass, "getAvailabilityStatus", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!LspConfig.isSettingsForceGoogleEntryEnabledXposed()) return
                    param.result = 0
                }
            })
        }.onFailure { log("hook GooglePreferenceController.getAvailabilityStatus failed", it) }
    }

    private fun hookConfigureNotificationRedirect(classLoader: ClassLoader) {
        val hookClass = XposedHelpers.findClassIfExists(
            "com.oplus.settings.SettingsActivityPlugin\$ConfigureNotificationSettings",
            classLoader
        ) ?: return
        runCatching {
            XposedBridge.hookAllMethods(hookClass, "onCreate", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!LspConfig.isNativeNotificationBubblesEnabledXposed()) return
                    param.result = null
                }
            })
        }.onFailure { log("hook ConfigureNotificationSettings redirect failed", it) }
    }

    private fun hookPreferenceFragment(classLoader: ClassLoader) {
        val fragmentClass = XposedHelpers.findClassIfExists(
            "androidx.preference.PreferenceFragmentCompat",
            classLoader
        ) ?: return

        runCatching {
            XposedBridge.hookAllMethods(fragmentClass, "onCreatePreferences", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    handleFragment(param.thisObject, classLoader)
                }
            })
        }.onFailure { log("hook PreferenceFragmentCompat.onCreatePreferences failed", it) }

        runCatching {
            XposedBridge.hookAllMethods(fragmentClass, "onResume", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    handleFragment(param.thisObject, classLoader)
                }
            })
        }.onFailure { log("hook PreferenceFragmentCompat.onResume failed", it) }
    }

    private fun hookDashboardFragments(classLoader: ClassLoader) {
        listOf(
            "com.android.settings.homepage.TopLevelSettings",
            "com.android.settings.dashboard.DashboardFragment"
        ).forEach { className ->
            val hookClass = XposedHelpers.findClassIfExists(className, classLoader) ?: return@forEach
            runCatching {
                XposedBridge.hookAllMethods(hookClass, "onCreatePreferences", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        handleFragment(param.thisObject, classLoader)
                    }
                })
            }.onFailure { log("hook $className.onCreatePreferences failed", it) }
        }
    }

    private fun handleFragment(fragment: Any?, classLoader: ClassLoader) {
        if (fragment == null) return
        val context = getContext(fragment) ?: return
        HookLog.bindContext(context)
        if (isHookSettingsScreen(fragment)) {
            buildHookSettingsScreen(fragment, context, classLoader)
        } else {
            injectEntry(fragment, context, classLoader)
        }
    }

    private fun injectEntry(fragment: Any, context: Context, classLoader: ClassLoader) {
        if (!shouldInjectEntry(fragment)) return
        val screen = getPreferenceScreen(fragment) ?: return

        val anchor = listOf(
            "notification_and_statusbar",
            "top_level_notifications"
        ).firstNotNullOfOrNull { key -> findPreference(screen, key) }
            ?: findPreferenceByTitle(
                group = screen,
                classLoader = classLoader,
                titleCandidates = listOf(
                    "\u901a\u77e5\u4e0e\u63a7\u5236\u4e2d\u5fc3",
                    "\u901a\u77e5\u8207\u63a7\u5236\u4e2d\u5fc3",
                    "\u901a\u77e5\u548c\u72b6\u6001\u680f",
                    "\u901a\u77e5\u8207\u72c0\u614b\u5217",
                    "Notifications & quick settings",
                    "Notifications and quick settings",
                    "Notifications and status bar"
                )
            )
        val group = parentOf(anchor) ?: listOf(
            "notification_settings_category"
        ).firstNotNullOfOrNull { key ->
            findPreference(screen, key)?.takeIf { isPreferenceGroup(it, classLoader) }
        }
        val existing = findPreference(screen, ENTRY_KEY)
        if (group == null) {
            existing?.let { parentOf(it)?.let { parent -> removePreference(parent, it) } }
            return
        }

        existing?.let { parentOf(it)?.let { parent -> removePreference(parent, it) } }

        val styleAnchor = anchor ?: firstChild(group) ?: return
        val preference = if (anchor != null) {
            newPreferenceLike(anchor, context, classLoader)
        } else {
            newPreferenceLike(styleAnchor, context, classLoader)
        } ?: return
        configureEntryPreference(preference, context, classLoader)
        copyVisualStyle(styleAnchor, preference)
        setIntent(preference, hookSettingsIntent(context))
        val order = (anchor?.let { call(it, "getOrder") as? Int } ?: maxChildOrder(group))?.plus(1)
        if (order != null) {
            call(preference, "setOrder", order)
        }
        addPreference(group, preference)
    }

    private fun configureEntryPreference(preference: Any, context: Context, classLoader: ClassLoader) {
        val moduleContext = moduleContext(context)
        call(preference, "setKey", ENTRY_KEY)
        call(preference, "setTitle", moduleContext.getText(R.string.settings_hook_entry_title))
        runCatching { call(preference, "setSummary", null) }
        runCatching { call(preference, "setIcon", moduleContext.getDrawable(R.drawable.ic_settings_fluidbox_hook)) }
        setClickListener(preference, classLoader) {
            openHookSettings(context)
            true
        }
    }

    private fun setOrderBelow(preference: Any, anchor: Any) {
        val anchorOrder = call(anchor, "getOrder") as? Int ?: return
        call(preference, "setOrder", anchorOrder + 1)
    }

    private fun buildHookSettingsScreen(fragment: Any, context: Context, classLoader: ClassLoader) {
        val manager = call(fragment, "getPreferenceManager") ?: return
        val screen = call(manager, "createPreferenceScreen", context) ?: return
        val moduleContext = moduleContext(context)
        val categoryClass = settingsCategoryClass(classLoader)
            ?: XposedHelpers.findClassIfExists("androidx.preference.PreferenceCategory", classLoader)
        val switchClass = switchPreferenceClass(classLoader)
            ?: XposedHelpers.findClassIfExists("androidx.preference.SwitchPreferenceCompat", classLoader)
            ?: return
        val categoryLayout = context.resources.getIdentifier(
            "oplus_settings_preference_category_layout",
            "layout",
            "com.android.settings"
        )
        val switchLayout = context.resources.getIdentifier(
            "switch_preference_simple_layout_no_icon",
            "layout",
            "com.android.settings"
        )
        val switchWidgetLayout = context.resources.getIdentifier(
            "coui_preference_widget_switch_compat",
            "layout",
            "com.android.settings"
        )
        val categoryMarginType = runCatching {
            XposedHelpers.getStaticIntField(
                XposedHelpers.findClass("com.coui.appcompat.preference.COUIPreferenceCategory", classLoader),
                "MARGIN_TYPE_ZERO"
            )
        }.getOrDefault(0)

        val iconGroup = addCategory(
            parent = screen,
            context = context,
            categoryClass = categoryClass,
            layoutResource = categoryLayout,
            marginType = categoryMarginType,
            isFirst = true
        ) ?: screen
        val iconRows = mutableListOf<Any>()
        addSwitch(
            parent = iconGroup,
            context = context,
            classLoader = classLoader,
            switchClass = switchClass,
            layoutResource = switchLayout,
            widgetLayoutResource = switchWidgetLayout,
            title = moduleContext.getText(R.string.feature_native_notify_icon_title),
            summary = moduleContext.getText(R.string.feature_native_notify_icon_summary),
            toggle = Toggle.NATIVE_NOTIFY_ICON
        )?.let(iconRows::add)
        applyUnifiedSectionStyle(iconRows)

        val systemGroup = addCategory(screen, context, categoryClass, categoryLayout, categoryMarginType) ?: screen
        val systemRows = listOfNotNull(
            addSwitch(systemGroup, context, classLoader, switchClass, switchLayout, switchWidgetLayout, moduleContext.getText(R.string.feature_native_notification_bubbles_title), moduleContext.getText(R.string.feature_native_notification_bubbles_summary), Toggle.NATIVE_NOTIFICATION_BUBBLES),
            addSwitch(systemGroup, context, classLoader, switchClass, switchLayout, switchWidgetLayout, moduleContext.getText(R.string.feature_aod_enhance_title), moduleContext.getText(R.string.feature_aod_enhance_summary), Toggle.AOD_ENHANCE),
            addSwitch(systemGroup, context, classLoader, switchClass, switchLayout, switchWidgetLayout, moduleContext.getText(R.string.feature_extreme_refresh_165_title), moduleContext.getText(R.string.feature_extreme_refresh_165_summary), Toggle.EXTREME_REFRESH)
        )
        applyUnifiedSectionStyle(systemRows)

        val featureGroup = addCategory(screen, context, categoryClass, categoryLayout, categoryMarginType) ?: screen
        val featureRows = listOfNotNull(
            addSwitch(featureGroup, context, classLoader, switchClass, switchLayout, switchWidgetLayout, moduleContext.getText(R.string.feature_oos_localizer_title), moduleContext.getText(R.string.feature_oos_localizer_summary), Toggle.OOS_LOCALIZER)
        )
        applyUnifiedSectionStyle(featureRows)

        val actionGroup = addCategory(screen, context, categoryClass, categoryLayout, categoryMarginType) ?: screen
        val action = addAction(
            parent = actionGroup,
            context = context,
            classLoader = classLoader,
            title = moduleContext.getText(R.string.settings_hook_restart_systemui_title),
            summary = moduleContext.getText(R.string.settings_hook_restart_systemui_summary)
        ) {
            restartSystemUi(context, moduleContext)
            true
        }
        applyUnifiedSectionStyle(listOfNotNull(action))

        call(fragment, "setPreferenceScreen", screen)
        applyListPadding(fragment, context)
    }

    private fun openHookSettings(context: Context) {
        runCatching {
            context.startActivity(hookSettingsIntent(context))
        }.onFailure { error ->
            log("open hook settings failed", error)
            Toast.makeText(context, moduleContext(context).getString(R.string.settings_hook_open_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun hookSettingsIntent(context: Context): Intent {
        val args = Bundle().apply { putBoolean(SCREEN_MARKER, true) }
        return Intent().apply {
            component = ComponentName("com.android.settings", "com.android.settings.SubSettings")
            putExtra(":settings:show_fragment", "com.android.settings.homepage.TopLevelSettings")
            putExtra(":settings:show_fragment_args", args)
            putExtra(":settings:show_fragment_title", moduleContext(context).getString(R.string.settings_hook_entry_title))
            putExtra(SCREEN_MARKER, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun shouldInjectEntry(fragment: Any): Boolean {
        val className = fragment.javaClass.name
        if (className.contains("TopLevelSettings")) return true
        if (className.contains("Notification", ignoreCase = true)) return true
        if (className.contains("StatusBar", ignoreCase = true)) return true

        val screen = getPreferenceScreen(fragment) ?: return false
        return listOf(
            "top_level_notifications",
            "notification_and_statusbar",
            "notification_settings_category"
        ).any { findPreference(screen, it) != null }
    }

    private fun isHookSettingsScreen(fragment: Any): Boolean {
        val args = call(fragment, "getArguments") as? Bundle
        if (args?.getBoolean(SCREEN_MARKER, false) == true) return true
        val activity = call(fragment, "getActivity")
        val intent = call(activity, "getIntent") as? Intent
        return intent?.getBooleanExtra(SCREEN_MARKER, false) == true ||
            intent?.getBundleExtra(":settings:show_fragment_args")?.getBoolean(SCREEN_MARKER, false) == true
    }

    private fun settingsCategoryClass(classLoader: ClassLoader): Class<*>? {
        return XposedHelpers.findClassIfExists(
            "com.oplus.settings.widget.preference.SettingsPreferenceCategory",
            classLoader
        )
    }

    private fun switchPreferenceClass(classLoader: ClassLoader): Class<*>? {
        listOf(
            "com.coui.appcompat.preference.COUISwitchPreferenceCompat",
            "com.coui.appcompat.preference.COUISwitchPreference"
        ).forEach { className ->
            XposedHelpers.findClassIfExists(className, classLoader)?.let { return it }
        }
        return null
    }

    private fun addCategory(
        parent: Any,
        context: Context,
        categoryClass: Class<*>?,
        layoutResource: Int,
        marginType: Int,
        isFirst: Boolean = false
    ): Any? {
        if (categoryClass == null) return null
        val category = runCatching { XposedHelpers.newInstance(categoryClass, context) }.getOrNull() ?: return null
        if (layoutResource != 0 && categoryClass.name != "com.oplus.settings.widget.preference.SettingsPreferenceCategory") {
            runCatching { call(category, "setLayoutResource", layoutResource) }
        }
        if (isFirst && categoryClass.name == "com.oplus.settings.widget.preference.SettingsPreferenceCategory") {
            runCatching { XposedHelpers.setObjectField(category, "mIsSimpleFirstCategory", true) }
            runCatching { XposedHelpers.setObjectField(category, "mMarginTopType", marginType) }
        }
        addPreference(parent, category)
        return category
    }

    private fun addSwitch(
        parent: Any,
        context: Context,
        classLoader: ClassLoader,
        switchClass: Class<*>,
        layoutResource: Int,
        widgetLayoutResource: Int,
        title: CharSequence,
        summary: CharSequence,
        toggle: Toggle,
        afterChanged: (Boolean) -> Unit = {}
    ): Any? {
        val preference = runCatching { XposedHelpers.newInstance(switchClass, context) }.getOrNull() ?: return null
        call(preference, "setKey", "fluidbox_hook_${toggle.settingsKey}")
        call(preference, "setTitle", title)
        call(preference, "setSummary", summary)
        setSwitchChecked(preference, readToggle(context, toggle))
        if (switchClass.name == "androidx.preference.SwitchPreferenceCompat") {
            if (layoutResource != 0) runCatching { call(preference, "setLayoutResource", layoutResource) }
            if (widgetLayoutResource != 0) runCatching { call(preference, "setWidgetLayoutResource", widgetLayoutResource) }
        }
        setChangeListener(preference, classLoader) { enabled ->
            writeToggle(context, toggle, enabled)
            afterChanged(enabled)
            true
        }
        addPreference(parent, preference)
        return preference
    }

    private fun addAction(
        parent: Any,
        context: Context,
        classLoader: ClassLoader,
        title: CharSequence,
        summary: CharSequence,
        onClick: () -> Boolean
    ): Any? {
        val preference = newPreference(context, classLoader) ?: return null
        call(preference, "setKey", "fluidbox_hook_restart_systemui")
        call(preference, "setTitle", title)
        call(preference, "setSummary", summary)
        setClickListener(preference, classLoader) { onClick() }
        addPreference(parent, preference)
        return preference
    }

    private fun applyUnifiedSectionStyle(preferences: List<Any>) {
        if (preferences.isEmpty()) return
        val lastIndex = preferences.lastIndex
        preferences.forEachIndexed { index, preference ->
            val layoutCategory = when {
                preferences.size == 1 -> 0
                index == 0 -> 1
                index == lastIndex -> 3
                else -> 2
            }
            runCatching { call(preference, "setLayoutCategory", layoutCategory) }
            runCatching { call(preference, "setNeedChangeDrawType", true) }
            runCatching { call(preference, "setShowDivider", index != lastIndex) }
        }
    }

    private fun applyListPadding(fragment: Any, context: Context) {
        val root = (call(fragment, "getView") as? View)
            ?: ((call(fragment, "getActivity")?.let { call(it, "getWindow") }?.let { call(it, "getDecorView") }) as? View)
            ?: return
        val listView = findListView(root) ?: return
        val density = context.resources.displayMetrics.density
        listView.setPadding(
            listView.paddingLeft,
            (-2f * density).toInt(),
            listView.paddingRight,
            maxOf(listView.paddingBottom, (56f * density).toInt())
        )
        runCatching { call(listView, "setClipToPadding", false) }
        listView.requestLayout()
    }

    private fun findListView(root: View): View? {
        if (root.id == android.R.id.list || root.javaClass.name.contains("RecyclerView")) return root
        if (root is ViewGroup) {
            for (index in 0 until root.childCount) {
                findListView(root.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private fun restartSystemUi(context: Context, moduleContext: Context) {
        val commands = listOf(
            "pkill -f com.android.systemui",
            "am force-stop com.android.systemui"
        )
        val success = commands.any { command ->
            runCatching {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor() == 0
            }.getOrDefault(false)
        }
        log("Restart SystemUI by shell success=$success")
        val message = if (success) {
            R.string.settings_hook_restart_systemui_done
        } else {
            R.string.settings_hook_restart_systemui_failed
        }
        Toast.makeText(context, moduleContext.getString(message), Toast.LENGTH_SHORT).show()
    }

    private fun readToggle(context: Context, toggle: Toggle): Boolean {
        toggle.persistPropertyKey?.let { key ->
            parseToggleValue(readSystemProperty(key))?.let { return it }
        }
        toggle.propertyKey?.let { key ->
            parseToggleValue(readSystemProperty(key))?.let { return it }
        }
        return runCatching {
            val value = Settings.Global.getString(context.contentResolver, toggle.settingsKey)
            parseToggleValue(value) ?: toggle.defaultValue
        }.getOrDefault(toggle.defaultValue)
    }

    private fun parseToggleValue(value: String?): Boolean? {
        return when (value?.trim()?.lowercase()) {
            "1", "true", "enabled", "on" -> true
            "0", "false", "disabled", "off" -> false
            else -> null
        }
    }

    private fun readSystemProperty(key: String): String? {
        return runCatching {
            val systemProperties = Class.forName("android.os.SystemProperties")
            systemProperties.getMethod("get", String::class.java).invoke(null, key) as? String
        }.getOrNull()
    }

    private fun writeToggle(context: Context, toggle: Toggle, enabled: Boolean) {
        val value = if (enabled) "1" else "0"
        runCatching {
            Settings.Global.putInt(context.contentResolver, toggle.settingsKey, if (enabled) 1 else 0)
        }.onFailure { log("write ${toggle.settingsKey} failed", it) }
        runCatching {
            val commands = buildList {
                toggle.persistPropertyKey?.let { add("setprop $it $value") }
                toggle.propertyKey?.let { add("setprop $it $value") }
                add("settings put global ${toggle.settingsKey} $value")
            }.joinToString("; ")
            Runtime.getRuntime().exec(arrayOf("su", "-c", commands)).waitFor()
        }.onSuccess {
            log("write ${toggle.settingsKey}=$value by shell")
        }.onFailure {
            log("write ${toggle.settingsKey} shell failed", it)
        }
    }

    private fun setChangeListener(preference: Any, classLoader: ClassLoader, onChange: (Boolean) -> Boolean) {
        val listenerClass = XposedHelpers.findClassIfExists(
            "androidx.preference.Preference\$OnPreferenceChangeListener",
            classLoader
        ) ?: return
        val listener = Proxy.newProxyInstance(classLoader, arrayOf(listenerClass)) { _, method, args ->
            if (method.name == "onPreferenceChange") {
                onChange(args?.getOrNull(1) as? Boolean ?: false)
            } else {
                null
            }
        }
        call(preference, "setOnPreferenceChangeListener", listener)
    }

    private fun setClickListener(preference: Any, classLoader: ClassLoader, onClick: () -> Boolean) {
        val listenerClass = XposedHelpers.findClassIfExists(
            "androidx.preference.Preference\$OnPreferenceClickListener",
            classLoader
        ) ?: return
        val listener = Proxy.newProxyInstance(classLoader, arrayOf(listenerClass)) { _, method, _ ->
            if (method.name == "onPreferenceClick") onClick() else null
        }
        call(preference, "setOnPreferenceClickListener", listener)
    }

    private fun setSwitchChecked(preference: Any?, checked: Boolean) {
        if (preference == null) return
        runCatching { call(preference, "setChecked", checked) }
    }

    private fun getPreferenceScreen(fragment: Any): Any? {
        return call(fragment, "getPreferenceScreen")
    }

    private fun getContext(fragment: Any): Context? {
        return call(fragment, "getContext") as? Context
            ?: call(fragment, "requireContext") as? Context
    }

    private fun findPreference(screen: Any, key: String): Any? {
        return runCatching { call(screen, "findPreference", key) }.getOrNull()
    }

    private fun newPreference(context: Context, classLoader: ClassLoader): Any? {
        val preferenceClass = XposedHelpers.findClassIfExists("androidx.preference.Preference", classLoader) ?: return null
        return runCatching { preferenceClass.getConstructor(Context::class.java).newInstance(context) }.getOrNull()
    }

    private fun newPreferenceLike(anchor: Any, context: Context, classLoader: ClassLoader): Any? {
        return runCatching {
            val constructor = anchor.javaClass.getDeclaredConstructor(Context::class.java)
            constructor.isAccessible = true
            constructor.newInstance(context)
        }.getOrElse {
            newPreference(context, classLoader)
        }
    }

    private fun isPreferenceGroup(preference: Any, classLoader: ClassLoader): Boolean {
        val groupClass = XposedHelpers.findClassIfExists("androidx.preference.PreferenceGroup", classLoader) ?: return false
        return groupClass.isInstance(preference)
    }

    private fun parentOf(preference: Any?): Any? {
        return if (preference == null) null else call(preference, "getParent")
    }

    private fun firstChild(group: Any): Any? {
        val count = call(group, "getPreferenceCount") as? Int ?: return null
        if (count <= 0) return null
        return call(group, "getPreference", 0)
    }

    private fun maxChildOrder(group: Any): Int? {
        val count = call(group, "getPreferenceCount") as? Int ?: return null
        var maxOrder: Int? = null
        for (index in 0 until count) {
            val child = call(group, "getPreference", index) ?: continue
            val order = call(child, "getOrder") as? Int ?: continue
            maxOrder = maxOrder?.let { maxOf(it, order) } ?: order
        }
        return maxOrder
    }

    private fun copyVisualStyle(source: Any, target: Any) {
        copyProperty(source, target, "getLayoutResource", "setLayoutResource")
        copyProperty(source, target, "getWidgetLayoutResource", "setWidgetLayoutResource")
        copyProperty(source, target, "isIconSpaceReserved", "setIconSpaceReserved")
        copyProperty(source, target, "isSingleLineTitle", "setSingleLineTitle")
        copyProperty(source, target, "isPersistent", "setPersistent")
        copyProperty(source, target, "isSelectable", "setSelectable")
        copyAnyProperty(source, target, listOf("getLayoutCategory"), listOf("setLayoutCategory"))
        copyAnyProperty(source, target, listOf("isNeedChangeDrawType", "getNeedChangeDrawType"), listOf("setNeedChangeDrawType"))
        copyAnyProperty(source, target, listOf("isShowSummary", "getShowSummary"), listOf("setShowSummary"))
        copyAnyProperty(source, target, listOf("isShowTwoToneColor", "getShowTwoToneColor"), listOf("setShowTwoToneColor"))
        copyAnyProperty(source, target, listOf("getTintType"), listOf("setTintType"))
        copyAnyProperty(source, target, listOf("getTintIconNew"), listOf("setTintIconNew"))
    }

    private fun copyProperty(source: Any, target: Any, getter: String, setter: String) {
        runCatching {
            call(target, setter, call(source, getter))
        }
    }

    private fun copyAnyProperty(source: Any, target: Any, getters: List<String>, setters: List<String>) {
        getters.forEach { getter ->
            val value = runCatching { call(source, getter) }.getOrNull() ?: return@forEach
            setters.forEach { setter ->
                if (runCatching { call(target, setter, value) }.isSuccess) return
            }
        }
    }

    private fun findPreferenceByTitle(group: Any, classLoader: ClassLoader, titleCandidates: List<String>): Any? {
        if (!isPreferenceGroup(group, classLoader)) return null
        val count = call(group, "getPreferenceCount") as? Int ?: return null
        for (index in 0 until count) {
            val preference = call(group, "getPreference", index) ?: continue
            val title = (call(preference, "getTitle") as? CharSequence)?.toString()
            if (title != null && titleCandidates.any { title.contains(it, ignoreCase = true) }) {
                return preference
            }
            findPreferenceByTitle(preference, classLoader, titleCandidates)?.let { return it }
        }
        return null
    }

    private fun addPreference(parent: Any, preference: Any) {
        runCatching { call(parent, "addPreference", preference) }
    }

    private fun removePreference(parent: Any, preference: Any) {
        runCatching { call(parent, "removePreference", preference) }
    }

    private fun setIntent(preference: Any, intent: Intent) {
        runCatching { call(preference, "setIntent", intent) }
    }

    private fun moduleContext(context: Context): Context {
        return runCatching {
            context.createPackageContext(MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY)
        }.getOrDefault(context)
    }

    private fun call(target: Any?, methodName: String, vararg args: Any?): Any? {
        return if (target == null) null else runCatching {
            XposedHelpers.callMethod(target, methodName, *args)
        }.getOrNull()
    }

    private fun log(message: String, throwable: Throwable? = null) {
        HookLog.i("SettingsHooker", message, throwable)
    }

    private enum class Toggle(
        val settingsKey: String,
        val defaultValue: Boolean,
        val persistPropertyKey: String? = null,
        val propertyKey: String? = null
    ) {
        NATIVE_NOTIFY_ICON("oost_native_notify_icon", true, "persist.sys.oost.native_notify_icon", "oost.native_notify_icon"),
        EXTREME_REFRESH("oost_extreme_refresh_165", false, "persist.sys.oost.extreme_refresh_165", "oost.extreme_refresh_165"),
        NATIVE_NOTIFICATION_BUBBLES("oost_native_notification_bubbles", false, "persist.sys.oost.native_notification_bubbles", "oost.native_notification_bubbles"),
        AOD_ENHANCE("oost_aod_enhance", false, "persist.sys.oost.aod_enhance", "oost.aod_enhance"),
        OOS_LOCALIZER("oost_oos_localizer", false, "persist.sys.oost.oos_localizer", "oost.oos_localizer")
    }
}
