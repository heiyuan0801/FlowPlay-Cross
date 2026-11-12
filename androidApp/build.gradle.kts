import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

val localSecrets = Properties().apply {
    rootProject.file("secrets.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val tmdbKey = providers.environmentVariable("TMDB_API_KEY")
    .orElse(localSecrets.getProperty("TMDB_API_KEY", ""))
fun String.sourceLiteral() = "\"" + replace("\\", "\\\\").replace("\"", "\\\"")
    .replace("\n", "\\n").replace("\r", "\\r") + "\""

android {
    namespace = "app.flowplay.cross"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.flowplay.cross"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = project.version.toString()
        buildConfigField("String", "TMDB_API_KEY", tmdbKey.get().sourceLiteral())
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
}

kotlin {
    jvmToolchain(21)
    compilerOptions { allWarningsAsErrors.set(true) }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.android.activity)
}
