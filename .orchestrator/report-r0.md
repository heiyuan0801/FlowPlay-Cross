# FlowPlay-Cross Round 0 验收报告

日期 2026-11-12。仓库 `/Volumes/mac/codex/FlowPlay-Cross`，分支 `main`。

## 提交与交付范围

代码与资源提交为 `efedd79347b0eece117c421884b9be18eb0ab68b`，提交信息为“初始化 FlowPlay 跨平台规格、工具链与应用骨架”。本报告随后单独提交。Git 使用本机现有默认身份 `jiesen <jiesen@jiesen.local>`。

已交付完整 SPEC、Gradle wrapper、shared / androidApp / desktopApp 三模块、环境脚本、密钥本地注入、中文五项导航、真实空状态、关于与系统邮件反馈、平台图标、macOS 分发产物和 Windows 包装配置。

应用名称 FlowPlay，标识 `app.flowplay.cross`，版本 `1.0.0`。Android 最低 API 26，编译与目标 API 35。版本选择满足 macOS DMG 的主版本大于零要求。添加来源入口进入真实的最小来源说明页，当前预览说明后续接入阶段。来源配置保存、实际媒体访问与播放由 SPEC 中的后续里程碑实现。

参考 Apple 仓库只读基线为 `1d1da24512dc6898af31de14fd33a5ef2f8815a9`。已读取当前 SPEC、docs、共享模型、主要 Provider / Playback / Features 与图标生成方式，并通过历史读取恢复 TVBox、榜单及 CMS 下载范围。开工和收尾的参考仓库 `git status --porcelain` 完全一致。

## 工具链与磁盘

大型工具链根目录为 `/Volumes/mac/codex/.toolchains`，本轮收尾约占 9.9 GiB。所有版本通过实际安装、构建或包元数据核对。

| 项目 | 版本与位置 | 实际占用约值 |
| --- | --- | --- |
| Temurin JDK | 21.0.13+9，`.toolchains/jdk-21.0.13+9/Contents/Home` | 335 MiB |
| Android SDK | `.toolchains/android-sdk` | 5.5 GiB |
| Command-line Tools | 22.0，下载包 `commandlinetools-mac_arm64-15859902_latest.zip` | 包含在 SDK 中 |
| SDK Platform / Build Tools | Android 35 revision 2 / 35.0.0 | 包含在 SDK 中 |
| Platform Tools | 35.0.2，ADB 1.0.41 | 包含在 SDK 中 |
| Emulator | 37.2.13.0，build 16740729 | 包含在 SDK 中 |
| 系统镜像 | API 35 Google APIs arm64-v8a revision 9 | 包含在 SDK 中 |
| AVD | `fp_pixel`，`.toolchains/avd/fp_pixel.avd` | 1.6 GiB |
| Gradle | 8.14.3，`.toolchains/gradle-8.14.3` | 146 MiB |
| Gradle wrapper / 依赖缓存 | `.toolchains/gradle-user-home` | 1.8 GiB |
| 安装归档与依赖检查下载 | `.toolchains/downloads` | 488 MiB |
| ADB 37.0.1 诊断副本 | `.toolchains/platform-tools-37.0.1` | 37 MiB |

AVD 配置为 2 CPU、2 GiB RAM、约 1.5 GiB 数据分区、1080 × 2400，使用 headless + SwiftShader，退出时关闭快照保存。`~/.android/avd/fp_pixel.ini` 为小型索引。`~/Library/Android/sdk` 已链接到外置卷 SDK。

Java 临时目录使用 `~/.android/flowplay-java-tmp`，DMG 打包期间峰值约 440 MiB，收尾约 132 KiB。当前主卷剩余约 28 GiB，外置卷约 510 GiB。图标转换使用项目内被忽略的 Python venv 与 Pillow 13.0.0。

`scripts/env.sh` 配置 JAVA_HOME、ANDROID_HOME、外置卷 Gradle 缓存与现有本地代理。Gradle 发行 ZIP 的 SHA-256 已与官方校验值核对。

## 技术选择

| 组件 | 选择与验证 |
| --- | --- |
| Kotlin / Compose / AGP | 2.3.10 / 1.9.3 / 8.11.1，Android 与 JVM 实际编译通过 |
| 网络 | Ktor 3.3.1 + OkHttp，当前依赖已解析，Provider 网络功能在 M1 开始 |
| 并发与 JSON | Coroutines 1.10.2、Serialization 1.9.0，公共模型与状态契约已编译 |
| 轻量设置 | multiplatform-settings 1.3.0，已解析，持久化接入在 M1 |
| 结构化存储 | SQLDelight 2.1.0，已核对发布元数据，M1 接入 SQL 与迁移 |
| 图片 | Coil 3.3.0，已核对发布元数据，M2 接入带时间戳缓存策略 |
| Android 播放 | Media3 1.8.0 + 同版本源码构建的 FFmpeg 音频扩展，M3 接入 |
| Desktop 播放 | libmpv + JNA 5.17.0，mpv 0.40.0 为稳定源码基线，M3 锁定原生构建 |
| JS | quickjs-kt 1.0.3，实际 JAR 含 Windows x64 DLL 与 macOS arm64 dylib，M5 接入执行宿主 |

QuickJS 发布包使用 Kotlin 2.3.10，因此已将工程编译器统一到同版本。播放器当前提供真实的接口与状态模型。SPEC 记录 libmpv codec 覆盖、动态链接许可、Windows DLL 装载、Compose 嵌入方案与原生文件随包路径，也记录 Android FFmpeg 音频扩展的能力边界。

TVBox 规格覆盖配置包装、多仓、CMS / HTTP / JS spider、drpy 宿主、直播、解析线路，以及 Android DexClassLoader 与桌面 JVM JAR / csp 的可行性。投屏规格覆盖两端 DLNA、Android Cast，以及 Windows Cast / AirPlay 的后续平台兼容验证。

TMDB 配置已从只读参考配置复制到被忽略的 `secrets.properties`，权限为 0600。Android BuildConfig 与桌面生成源码均注入成功。示例文件为空占位。已扫描提交文件和日志，TMDB 值出现次数为 0。

## 构建结果

| 检查 | 结果 | 证据 |
| --- | --- | --- |
| Android debug APK | 成功 | `logs/build-kotlin-final.log`、`logs/build-delivery.log` |
| shared Android / Desktop 与 desktopApp Kotlin 编译 | 成功，最终 0 错误 / 0 Kotlin 编译警告 | `logs/build-kotlin-final.log` |
| 全任务重跑 | 已用 `--rerun-tasks` 完整重跑编译及包装 | `logs/build-verified.log` |
| 最终 Kotlin 2.3.10 分发构建 | 成功，82 个任务，9 执行 / 73 最新 | `logs/build-delivery.log` |
| macOS `packageDistributionForCurrentOS` | 成功，生成 DMG 与 app image | `logs/build-delivery.log` |
| `portableZip` | 成功，生成 macOS arm64 ZIP | `logs/build-delivery.log` |
| Desktop `run` | 原生窗口启动与截图成功，截图后脚本以 SIGTERM 结束进程，命令退出 143 | `logs/desktop-run-final.log`、`logs/desktop-runtime-result.txt` |
| 打包后 app | 最终产物启动、窗口与截图通过，随后结束进程 | `logs/desktop-distributable-final.log` |

最终 Kotlin 编译全部启用 `allWarningsAsErrors`。非 Kotlin 信息包括 AndroidX `libandroidx.graphics.path.so` 保留原样打包提示。SDK 安装期间另有 sdkmanager 弃用提示及 AGP / SDK XML 格式版本提示，均已记录在本机日志。

构建产物如下。

- `androidApp/build/outputs/apk/debug/androidApp-debug.apk`，约 13 MiB。
- `desktopApp/build/compose/binaries/main/app/FlowPlay.app`，含私有 Java runtime。
- `desktopApp/build/compose/binaries/main/dmg/FlowPlay-1.0.0.dmg`，约 74 MiB。
- `desktopApp/build/compose/binaries/main/portable/FlowPlay-1.0.0-macos-aarch64-portable.zip`，约 70 MiB。

## 短时运行与截图

Android 模拟器启动完成，APK 安装成功，主 Activity 冷启动成功。已实际检查首页、添加来源说明、系统返回、我的、关于页的版本 / 开发者 / 联系邮箱。反馈按钮发出 ACTION_SENDTO，系统接收组件为 Gmail 的 `ComposeActivityGmailExternal`。最终 Kotlin 2.3.10 APK 已重新安装并核对首页与关于页。运行检查期间 AndroidRuntime 错误日志为空。

Desktop 在 macOS arm64 上通过 `:desktopApp:run` 启动。使用 CoreGraphics 按本轮进程 PID 定位窗口，并用 `screencapture -x -l` 获取真实窗口截图。最终打包 app 另行启动并验证。模拟器及本轮两种桌面启动进程均已结束。

| 截图 | 本机证据路径 | 交付副本 |
| --- | --- | --- |
| Android 首页 | `logs/android-home.png` | `/Users/jiesen/fp-shots/cross/android-home.png` |
| Android 我的 | `logs/android-mine.png` | 本仓库 logs |
| Android 关于 | `logs/android-about.png` | 本仓库 logs |
| Android 来源说明 | `logs/android-add-source.png` | 本仓库 logs |
| Desktop 首页 | `logs/desktop-home.png` | `/Users/jiesen/fp-shots/cross/desktop-home.png` |
| Desktop 最终分发版首页 | `logs/desktop-distributable-home.png` | 本仓库 logs |

原始 PNG、生成的窗口图标、Android adaptive icon / legacy mipmaps、macOS icns 和 Windows ico 均已进入代码提交。Android 图标背景复用 Apple 生成器的蓝紫 / 珊瑚渐变插值方式。

## Windows 包装

已配置 Msi / Exe、包名 FlowPlay、vendor jiesen、稳定 upgradeUuid、Windows ico 与 per-user 安装。`createDistributable` 与 `portableZip` 在 Windows x64 上生成含私有 Java runtime 的便携包。README 提供精确 PowerShell 命令。

`.github/workflows/windows-package.yml` 提供 windows-latest 手动打包工作流，准备 JDK 21、Android SDK 和 WiX 3，上传 ZIP / MSI / EXE artifacts。当前 Mac 的 Msi / Exe 任务按平台跳过。Windows 安装器与工作流执行结果待 Windows 环境验证。

## 已处理问题与路径偏差

- Java 工具链通过现有本地代理下载依赖，完成初始网络等待恢复。
- 最新 Platform Tools 37.0.1 的 ADB 在当前 macOS 出现 `libunwind` 崩溃，固定为 35.0.2 后完成全部手机运行检查。
- SDK Manager 与首次 Gradle 自动准备 SDK 产生的同版本重复目录已核对并清理。
- jpackage 在外置卷临时目录挂载 DMG 返回 EACCES，改用允许范围内的 `~/.android/flowplay-java-tmp` 后包装成功。
- Android Emulator 在 macOS 默认的 `~/Library/Caches/TemporaryItems/avd/running` 自动生成过临时发现文件，此目录超出任务的写入路径白名单。模拟器退出后该目录剩余文件数已核对为 0，少量空目录可能保留。此项为本轮工具运行产生的路径偏差。

## 后续阶段与待验范围

M0 交付完成。M1 实现来源、数据库与 CMS，M2 实现 Emby / 搜索 / TMDB / 集合，M3 集成播放器与原生包，M4 完成本地文件和直播，M5 实现 TVBox / QuickJS，M6 实现离线下载及 DLNA / Android Cast，M7 恢复热门榜单并完善体验，M8 处理 csp / JAR、平台投屏兼容及完整许可。

本轮按要求执行短时运行检查。播放器、媒体服务、下载与投屏的真实内容验证按对应阶段进行。Windows 安装器待 Windows 环境执行。当前 R0 构建与截图交付均已具备。

运行期间工作区新增下一轮输入 `.orchestrator/STEER-tv.md` 与 `.orchestrator/task-r0tv.md`。它们作为 Android TV 下一轮任务输入保留在工作区。
