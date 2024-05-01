plugins {
    id("com.android.application") version "8.4.0" apply false
    kotlin("android") version "1.9.23" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
}

tasks.register("Delete", Delete::class) {
    delete(rootProject.buildDir)
}

buildscript {
    dependencies {
        classpath("com.android.tools:r8:8.3.37")
    }
}
