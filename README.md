# FlowPlay-Cross

FlowPlay 的 Android 手机 / 平板、Android TV 与 Windows 版本，采用 Kotlin + Compose Multiplatform。当前版本 1.0.0，提供中文导航、真实空状态、来源接入说明、关于与系统邮件反馈。完整产品范围与阶段安排见 [.orchestrator/SPEC.md](.orchestrator/SPEC.md)。

## 本机工具链

大型工具链位于 `/Volumes/mac/codex/.toolchains`。环境脚本为当前 macOS arm64 开发机配置 Temurin JDK 21、Android SDK 和外置卷 Gradle 缓存。Homebrew 无需安装。终端执行以下命令。

```sh
source scripts/env.sh
./gradlew :androidApp:assembleDebug
./gradlew :desktopApp:run
./gradlew :desktopApp:packageDistributionForCurrentOS
./gradlew :desktopApp:portableZip
```

`local.properties` 中保存 `sdk.dir=/Volumes/mac/codex/.toolchains/android-sdk`，该文件被 Git 忽略。环境脚本会将现有本地 HTTP(S) 代理传给 Java，以便 Gradle 和 SDK Manager 使用同一路由。其他开发机可自行设置 JDK 21、ANDROID_HOME 和 PATH，直接使用 wrapper。

本机 Platform Tools 固定为 35.0.2，避免 37.0.1 的 ADB 崩溃。AVD 数据位于外置卷 `.toolchains/avd/fp_pixel.avd`，`~/.android/avd` 仅保存索引。macOS 27 的 DMG 挂载限制要求 Java 临时目录位于内置卷，环境脚本使用 `~/.android/flowplay-java-tmp`，打包完成后可清理其中的临时内容。

## TMDB 本地配置

将 `secrets.properties.example` 复制为 `secrets.properties`，在本机填写 `TMDB_API_KEY`，或使用同名环境变量。两种配置都可为空，此时后续 TMDB 功能显示配置缺失。Android 构建生成 BuildConfig，桌面构建生成被忽略的 `desktopApp/build/generated/flowplay` 源码。配置值只进入本机配置和构建输出。当前骨架不会发起 TMDB 或媒体服务器请求。

## Android 短时运行

```sh
source scripts/env.sh
emulator -avd fp_pixel -no-snapshot-save -no-audio -no-boot-anim
# 另一个终端
adb wait-for-device
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb shell am start -n app.flowplay.cross/.MainActivity
mkdir -p logs
adb exec-out screencap -p > logs/android-home.png
adb emu kill
```

AVD 使用 API 35 Google APIs arm64-v8a。模拟器准备与启动结果见本轮报告。`logs/` 为本机运行证据目录。

## Android TV 短时运行

手机与电视共用 `androidApp-debug.apk`。Activity 通过 UiModeManager 选择电视 10-foot 深色布局，电视提供五项侧栏与 D-pad 高对比焦点。上下键移动导航，OK 切换栏目，右键进入内容，左键返回已选导航，说明页支持上下滚动。进入子页后返回键恢复侧栏，内容区返回键先回侧栏。

本机已安装 API 34 Android TV arm64-v8a revision 3，AVD `fp_tv` 使用 tv_1080p、1920 × 1080、320 dpi、2 CPU / 2 GiB RAM。AVD 数据位于 `.toolchains/avd/fp_tv.avd`。下面按 LEANBACK_LAUNCHER 解析入口，再显式启动解析到的 MainActivity，与电视 Launcher 启动入口一致。

```sh
source scripts/env.sh
emulator -avd fp_tv -port 5556 -no-snapshot -no-audio -no-boot-anim
# 另一个终端，启动后确认 sys.boot_completed 为 1
adb -s emulator-5556 shell getprop sys.boot_completed
adb -s emulator-5556 install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb -s emulator-5556 shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER app.flowplay.cross
adb -s emulator-5556 shell am start -W -n app.flowplay.cross/.MainActivity -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER
adb -s emulator-5556 shell input keyevent DPAD_DOWN
adb -s emulator-5556 shell input keyevent DPAD_RIGHT
adb -s emulator-5556 exec-out screencap -p > logs/android-tv-home.png
adb -s emulator-5556 emu kill
```

Round 0b 的首页展示空来源状态。真实媒体大海报横排在 M2 接入，播放器遥控在 M3，直播上下换台在 M4 / M5。TV banner 为 320 × 180，来源为 `assets/icons` 中已复制的 Apple 图标，可用 `python3 scripts/generate-icons.py --tv-banner-only` 单独重新生成。

## Windows x64 包装

在 Windows x64 安装 Temurin JDK 21 和 Android SDK Platform 35 / Build-tools 35.0.0，设置 `JAVA_HOME` 与 `ANDROID_HOME`。MSI / EXE 安装器还需 WiX 3，确保 `candle.exe`、`light.exe` 位于 PATH。PowerShell 在仓库根目录执行。

```powershell
# 按本机位置设置，并确认 JAVA_HOME 为 x64 JDK 21
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:PATH = "$env:JAVA_HOME\bin;C:\Program Files (x86)\WiX Toolset v3.14\bin;$env:PATH"
.\gradlew.bat :desktopApp:createDistributable :desktopApp:portableZip
.\gradlew.bat :desktopApp:packageMsi :desktopApp:packageExe
```

可移植包输出到 `desktopApp/build/compose/binaries/main/portable/FlowPlay-1.0.0-windows-x64-portable.zip`，解压后运行其中的 `FlowPlay/FlowPlay.exe`，无需用户安装 Java。安装器输出到相邻 `msi/` 和 `exe/` 目录。macOS 上运行打包任务会生成本机 app / dmg，Windows 安装器由 Windows 的 jpackage 构建。当前包只包含工程骨架，libmpv 与 FFmpeg 等原生播放库在 M3 集成。

[.github/workflows/windows-package.yml](.github/workflows/windows-package.yml) 提供手动运行的 windows-latest 工作流，产物保存在 Actions artifacts。工作流文件已经写入本地仓库，尚未远端执行。

## 图标

`assets/icons` 保存复制的 Apple 原始图像和 `.icns` / `.ico`。在 macOS 安装 Pillow 后可重新生成。

```sh
python3 scripts/generate-icons.py
```

图标脚本同时生成 Android adaptive foreground、legacy mipmaps 和共享窗口 PNG。开发者 jiesen，反馈邮箱 heiyuan6@gmail.com。
