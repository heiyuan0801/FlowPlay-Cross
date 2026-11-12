import java.util.Properties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
    compilerOptions { allWarningsAsErrors.set(true) }
}

val generatedSecrets = layout.buildDirectory.dir("generated/flowplay")
val generateLocalSecrets by tasks.registering {
    val secretFile = rootProject.file("secrets.properties")
    val environmentKey = providers.environmentVariable("TMDB_API_KEY").orElse("")
    // Always regenerate so an edited local file or environment cannot leave a stale credential.
    outputs.dir(generatedSecrets)
    outputs.upToDateWhen { false }
    doLast {
        val properties = Properties().apply {
            secretFile.takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        val key = environmentKey.get().ifEmpty { properties.getProperty("TMDB_API_KEY", "") }
        val literal = key.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("$", "\\$").replace("\n", "\\n").replace("\r", "\\r")
        val output = generatedSecrets.get().file("app/flowplay/cross/LocalSecrets.kt").asFile
        output.parentFile.mkdirs()
        output.writeText("package app.flowplay.cross\ninternal object LocalSecrets { const val tmdbApiKey = \"$literal\" }\n")
    }
}
kotlin.sourceSets.main { kotlin.srcDir(generatedSecrets) }
tasks.named("compileKotlin") { dependsOn(generateLocalSecrets) }

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
}

compose.desktop {
    application {
        mainClass = "app.flowplay.cross.MainKt"
        jvmArgs += "-Dfile.encoding=UTF-8"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe)
            packageName = "FlowPlay"
            packageVersion = project.version.toString()
            vendor = "jiesen"
            description = "FlowPlay 个人媒体中心"
            modules("java.net.http", "java.prefs", "java.sql", "java.xml", "jdk.crypto.ec", "jdk.unsupported")
            appResourcesRootDir.set(project.layout.projectDirectory.dir("native-resources"))
            macOS {
                bundleID = "app.flowplay.cross"
                iconFile.set(rootProject.file("assets/icons/FlowPlay.icns"))
            }
            windows {
                iconFile.set(rootProject.file("assets/icons/FlowPlay.ico"))
                menuGroup = "FlowPlay"
                shortcut = true
                perUserInstall = true
                upgradeUuid = "fd7a19c8-54f8-45e6-91d8-84de2f82e404"
            }
        }
    }
}

val currentPlatform = System.getProperty("os.name").lowercase().let {
    when {
        it.contains("windows") -> "windows-x64"
        it.contains("mac") -> "macos-${System.getProperty("os.arch")}"
        else -> "linux-${System.getProperty("os.arch")}"
    }
}

tasks.register<Zip>("portableZip") {
    group = "distribution"
    description = "Zip the current OS app image, including its private Java runtime. Run on Windows x64 for Windows."
    dependsOn("createDistributable")
    from(layout.buildDirectory.dir("compose/binaries/main/app"))
    archiveFileName.set("FlowPlay-${project.version}-$currentPlatform-portable.zip")
    destinationDirectory.set(layout.buildDirectory.dir("compose/binaries/main/portable"))
}
