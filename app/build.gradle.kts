import java.time.Instant

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val ciVersionName = (project.findProperty("FLUIDBOX_VERSION_NAME") as String?)
    ?.takeIf { it.isNotBlank() }
    ?: "16.0"

val ciVersionCode = (project.findProperty("FLUIDBOX_VERSION_CODE") as String?)
    ?.toIntOrNull()
    ?: 1

val ciSignReleaseWithDebug = (project.findProperty("FLUIDBOX_CI_SIGN_RELEASE_WITH_DEBUG") as String?)
    ?.toBooleanStrictOrNull()
    ?: false

val isGithubCi = System.getenv("GITHUB_ACTIONS") == "true"
val fluidBoxBuildTimestamp = System.currentTimeMillis()
val fluidBoxBuildTime = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss 'UTC+8'")
    .withZone(ZoneId.of("Asia/Shanghai"))
    .format(Instant.ofEpochMilli(fluidBoxBuildTimestamp))
val yukiHookApiVersion = libs.versions.yukiHookApi.get()

val releaseStoreFilePath = (project.findProperty("FLUIDBOX_RELEASE_STORE_FILE") as String?)
    ?.takeIf { it.isNotBlank() }
val releaseStorePassword = (project.findProperty("FLUIDBOX_RELEASE_STORE_PASSWORD") as String?)
    ?.takeIf { it.isNotBlank() }
val releaseKeyAlias = (project.findProperty("FLUIDBOX_RELEASE_KEY_ALIAS") as String?)
    ?.takeIf { it.isNotBlank() }
val releaseKeyPassword = (project.findProperty("FLUIDBOX_RELEASE_KEY_PASSWORD") as String?)
    ?.takeIf { it.isNotBlank() }

val hasExternalReleaseSigning =
    !releaseStoreFilePath.isNullOrBlank() &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank() &&
    file(releaseStoreFilePath).exists()

android {
    namespace = "com.mi.fluidbox"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.mi.fluidbox"
        minSdk = 28
        targetSdk = 36
        versionCode = ciVersionCode
        versionName = ciVersionName
        buildConfigField("String", "APP_BUILD_TIME", "\"$fluidBoxBuildTime\"")
        buildConfigField("long", "APP_BUILD_TIMESTAMP", "${fluidBoxBuildTimestamp}L")
        buildConfigField("String", "YUKIHOOKAPI_VERSION", "\"$yukiHookApiVersion\"")
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasExternalReleaseSigning) {
            create("ciRelease") {
                storeFile = file(releaseStoreFilePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            if (isGithubCi) {
                // Keep CI debug APK size smaller (e.g. Telegram upload limit).
                isMinifyEnabled = true
                isShrinkResources = true
                if (hasExternalReleaseSigning) {
                    // Ensure CI debug/release variants can share the same certificate when both are built.
                    signingConfig = signingConfigs.getByName("ciRelease")
                }
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        create("debugSlim") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            when {
                hasExternalReleaseSigning -> {
                    signingConfig = signingConfigs.getByName("ciRelease")
                }
                ciSignReleaseWithDebug -> {
                    // CI fallback signing so release APK can be installed for testing.
                    signingConfig = signingConfigs.getByName("debug")
                }
                else -> {
                    // Last-resort fallback to avoid unsigned release outputs.
                    signingConfig = signingConfigs.getByName("debug")
                }
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        localeFilters += listOf(
            "en",
            "zh-rCN",
            "zh-rHK",
            "zh-rMO",
            "zh-rTW",
            "b+yue+Hant",
            "b+zh+CN+catgirl",
            "ja",
            "ko",
            "b+ko+KP",
            "vi",
            "ru",
            "de",
            "fr",
            "b+id",
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.palette.ktx)
    implementation("io.github.suqi8.coui.kmp:coui-ui:1.0.0")
    implementation("io.github.suqi8.coui.kmp:coui-preference:1.0.0")
    implementation("io.github.suqi8.coui.kmp:coui-blur:1.0.0")
    implementation("io.github.suqi8.coui.kmp:coui-icons:1.0.0")
    implementation("io.github.suqi8.coui.kmp:coui-navigation3-ui:1.0.0")
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kyant.capsule)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Root (Magisk) support
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("com.github.topjohnwu.libsu:service:6.0.0")

    // Shizuku / Sui support
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")

    // LSPosed ecosystem utility
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")
    // Legacy XposedBridge API (current module code path)
    compileOnly("de.robv.android.xposed:api:82")
    implementation(libs.yukihook.api)
    ksp(libs.yukihook.ksp.xposed)
}
