plugins {
    id("com.android.application") version "8.7.2" apply false
    kotlin("android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
}

tasks.register("Delete", Delete::class) {
    delete(rootProject)
}

buildscript {
    dependencies {
        classpath("com.android.tools:r8:8.6.27")
    }
}
