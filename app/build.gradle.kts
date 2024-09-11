import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.diffplug.spotless")
}

android {
    compileSdk = 35
    buildToolsVersion = "35.0.0"
    ndkVersion = "26.3.11579264"

    splits {
        abi {
            isEnable = true
            reset()
            if (gradle.startParameter.taskNames.any { it.contains("Release") }) {
                include("arm64-v8a", "x86_64", "armeabi-v7a", "x86")
                isUniversalApk = true
            } else {
                include("arm64-v8a", "x86_64")
            }
        }
    }

    val signConfig = signingConfigs.create("release") {
        storeFile = File(projectDir.path + "/keystore/androidkey.jks")
        storePassword = "000000"
        keyAlias = "key0"
        keyPassword = "000000"
        enableV3Signing = true
        enableV4Signing = true
    }

    val commitSha by lazy {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine = "git rev-parse --short=7 HEAD".split(' ')
            standardOutput = stdout
        }
        stdout.toString().trim()
    }

    val buildTime by lazy {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(ZoneOffset.UTC)
        formatter.format(Instant.now())
    }

    defaultConfig {
        applicationId = "org.error.ehviewer"
        minSdk = 28
        targetSdk = 35
        versionCode = 173901
        versionName = "1.7.39.1"
        resourceConfigurations.addAll(
            listOf(
                "zh",
                "zh-rCN",
                "zh-rHK",
                "zh-rTW",
                "ja",
            ),
        )
        buildConfigField("String", "VERSION_CODE", "\"${defaultConfig.versionCode}\"")
        buildConfigField("String", "COMMIT_SHA", "\"$commitSha\"")
    }

    externalNativeBuild {
        cmake {
            path = File("src/main/cpp/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf(
            // https://kotlinlang.org/docs/compiler-reference.html#progressive
            "-progressive",
            "-Xwhen-guards",

            "-opt-in=coil.annotation.ExperimentalCoilApi",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.coroutines.InternalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        disable.add("MissingTranslation")
    }

    packaging {
        resources {
            excludes += "/META-INF/**"
            excludes += "/kotlin/**"
            excludes += "**.txt"
            excludes += "**.bin"
        }
    }

    dependenciesInfo.includeInApk = false

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
            signingConfig = signConfig
            buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
        }
        debug {
            applicationIdSuffix = ".debug"
            buildConfigField("String", "BUILD_TIME", "\"\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    namespace = "com.hippo.ehviewer"
}

dependencies {
    // https://developer.android.com/jetpack/androidx/releases/activity
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.biometric:biometric-ktx:1.4.0-alpha02")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.collection:collection-ktx:1.4.5")

    implementation("androidx.core:core-ktx:1.15.0")

    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    // https://developer.android.com/jetpack/androidx/releases/lifecycle
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")

    // https://developer.android.com/jetpack/androidx/releases/paging
    implementation("androidx.paging:paging-runtime-ktx:3.3.4")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // https://developer.android.com/jetpack/androidx/releases/room
    val room = "2.6.1"
    ksp("androidx.room:room-compiler:$room")
    implementation("androidx.room:room-paging:$room")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("com.drakeet.drawer:drawer:1.0.3")
    implementation("com.google.android.material:material:1.12.0")

    // https://square.github.io/okhttp/changelogs/changelog/
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.0.0-alpha.14"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps")
    implementation("com.squareup.okhttp3:okhttp-coroutines")

    implementation("com.squareup.okio:okio-jvm:3.9.1")

    // https://github.com/RikkaApps/RikkaX
    implementation("dev.rikka.rikkax.core:core-ktx:1.4.1")
    implementation("dev.rikka.rikkax.insets:insets:1.3.0")
    implementation("dev.rikka.rikkax.layoutinflater:layoutinflater:1.3.0")
    //noinspection GradleDependency
    implementation("dev.rikka.rikkax.material:material:1.6.6")
    implementation("dev.rikka.rikkax.preference:simplemenu-preference:1.0.3")

    // https://coil-kt.github.io/coil/changelog/
    implementation("io.coil-kt:coil:2.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.7.3")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("io.ktor:ktor-utils:3.0.1")
}

configurations.all {
    exclude("dev.rikka.rikkax.appcompat", "appcompat")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_multiline-expression-wrapping" to "disabled",
            ),
        )
    }
}
