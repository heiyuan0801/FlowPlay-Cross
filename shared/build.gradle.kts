import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_21) } }
    jvm("desktop")
    jvmToolchain(21)
    compilerOptions { allWarningsAsErrors.set(true) }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation("org.jetbrains.compose.ui:ui-backhandler:1.9.1")
            implementation(libs.material.icons)
            implementation(libs.coroutines.core)
            implementation(libs.serialization.json)
            implementation(libs.settings)
            implementation(libs.ktor.core)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.serialization)
        }
        androidMain.dependencies { implementation(libs.ktor.okhttp) }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.okhttp)
            }
        }
    }
}

android {
    namespace = "app.flowplay.cross.shared"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "app.flowplay.cross.resources"
}
