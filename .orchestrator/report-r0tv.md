# FlowPlay-Cross Round 0b Android TV 验收报告

日期 2026-11-12。仓库 `/Volumes/mac/codex/FlowPlay-Cross`，分支 `main`。

## 提交与 Round 0 基线

代码、资源、SPEC 与任务输入提交为 `4637cb59897e3f5c0fd18379a352d73effda5736`，提交信息为“增加 Android TV 同包入口与遥控大屏导航”。本报告通过独立中文提交纳入 main。提交身份沿用本机 `jiesen <jiesen@jiesen.local>`。

开工先核对 `task-r0.md`、`report-r0.md` 和 git 历史，并执行 Android debug 与桌面当前平台包装。两项均成功，Round 0 的手机和桌面截图及交付副本齐全，随后继续 TV 实现。

## TV 方案与库

手机、平板和电视共用 `androidApp` APK，标识 `app.flowplay.cross`，版本 `1.0.0`。MainActivity 通过 `UiModeManager.currentModeType` 与 `UI_MODE_TYPE_TELEVISION` 检测电视。共用 APK 可以复用来源、数据库、网络、Keystore 和后续 Media3 会话，同时由电视专用 `shared.ui.TvShell` 承载大屏输入与布局。

Manifest 已加入 LEANBACK_LAUNCHER，leanback 和 touchscreen 两个 uses-feature 均设为 `required="false"`。`aapt2 dump badging` 实际确认普通和 Leanback 两种启动入口、两个 optional feature 及应用 banner。

Banner 位于 `androidApp/src/main/res/drawable-xhdpi/tv_banner.png`，实际尺寸 320 × 180。生成脚本复用 Round 0 已复制的 Apple 原始透明图标与背景，合成 FlowPlay 字样，支持 `--tv-banner-only` 单独生成。

| 项目 | 选择与用途 |
| --- | --- |
| Kotlin / Compose Multiplatform | 2.3.10 / 1.9.3，沿用公共 UI 和主题 |
| Compose Foundation / UI | FocusRequester、focusGroup、focusProperties、onFocusChanged、clickable、focusable 与按键预处理 |
| Compose Material 3 | 沿用颜色、图标与文字绘制，电视操作控件提供明确焦点样式 |
| Activity Compose | 1.10.1，沿用 MainActivity 和系统返回处理 |
| Coroutines | 1.10.2，说明面板滚动 |
| 图像生成 | 沿用项目内 Pillow 13.0.0 工具环境 |

电视布局使用常驻五项侧边抽屉、32 dp 水平 / 24 dp 垂直边距、20 sp 正文及 28–32 sp 标题。当前选中栏目和实际输入焦点分别有可见样式。导航上下移动、OK 切换栏目、右键进入内容、左键回到已选栏目。子页初始聚焦返回按钮，说明面板支持上下滚动。来源说明和法律文字与现有手机 / 桌面内容共用。

SPEC 已增加完整 Android TV 对照列、架构与遥控说明、R0b 阶段及后续验收范围。当前来源为空，首页展示真实空状态。大海报横排由本轮安排到 M2，随真实媒体 Provider 和 TMDB 图像接入。M3 实现 OK 播放暂停、左右 seek、Menu / 上键轨道和画质菜单、分层返回。M4 / M5 实现直播及 TVBox 的上下换台。

## 镜像与 AVD

| 项目 | 实际安装和配置 |
| --- | --- |
| SDK 根目录 | `/Volumes/mac/codex/.toolchains/android-sdk` |
| TV 镜像 | `system-images;android-34;android-tv;arm64-v8a`，revision 3，Android 14 / API 34 |
| 镜像目录 | `.toolchains/android-sdk/system-images/android-34/android-tv/arm64-v8a`，占用约 8.1 GiB |
| TV AVD | `fp_tv`，内置 `tv_1080p` profile |
| AVD 数据 | `/Volumes/mac/codex/.toolchains/avd/fp_tv.avd`，收尾约 899 MiB |
| AVD 索引 | `/Users/jiesen/.android/avd/fp_tv.ini` |
| 显示与输入 | 1920 × 1080，320 dpi，landscape，D-pad，no-touch |
| 资源 | 2 CPU，2 GiB RAM，2 GiB 数据分区，关闭快照保存 |
| Emulator / ADB | 沿用 37.2.13 / Platform Tools 35.0.2 |
| 手机回归 AVD | `fp_pixel`，API 35 Google APIs arm64-v8a |

模拟器均以 headless、SwiftShader、静音方式短时运行。TV 的 `dumpsys uimode` 为 `mCurUiMode=0x24`，低位类型为电视 4。手机为 `0x11`，类型为普通设备 1。大型工具链收尾总占用约 19 GiB。

本轮启动命令在子进程内使用 `env -u HOME`，同时明确设置 ANDROID_EMULATOR_HOME 到 `.toolchains/emulator-home`、ANDROID_AVD_HOME 到 `~/.android/avd`，使模拟器发现文件落入允许的工具链目录。

## 构建结果与警告计数

| 检查 | 结果 | 证据 |
| --- | --- | --- |
| Round 0 开工基线 | Android debug 与桌面包装成功 | `logs/build-r0tv-baseline.log` |
| 最终 Android / shared / desktopApp 强制重编译 | 成功，73 个任务全部执行，8 秒 | `logs/build-r0tv-final.log` |
| 最终桌面当前平台包装和 portableZip | 成功，24 个任务，5 执行 / 19 最新 | `logs/build-r0tv-package-final.log` |
| Kotlin 编译错误 | 0 | 三模块均保留 allWarningsAsErrors |
| Kotlin 编译警告 | 0 | 最终完整重编译日志 |
| 最终 Gradle 构建错误 / 警告 | 0 / 0 | 最终编译及包装日志 |
| 原生打包提示 | 1 条，`libandroidx.graphics.path.so` 保留原样打包 | `logs/build-r0tv-final.log` |

最终编译命令为 `./gradlew :androidApp:assembleDebug :desktopApp:compileKotlin --rerun-tasks --console=plain`。桌面包装命令为 `./gradlew :desktopApp:packageDistributionForCurrentOS :desktopApp:portableZip`，该次执行通过命令行指定相同的 in-process 编译策略，随后该策略写入工程默认配置。

SDK 工具安装阶段有 CLI 弃用提示。AVD 创建工具报告 TV 和手机镜像各一处 `devices.xml` 缺失诊断，命令退出码为 0，内置 tv_1080p profile 创建、启动和运行均成功。

产物包括 `androidApp/build/outputs/apk/debug/androidApp-debug.apk`、`desktopApp/build/compose/binaries/main/app/FlowPlay.app`、相邻 `dmg/FlowPlay-1.0.0.dmg` 与 `portable/FlowPlay-1.0.0-macos-aarch64-portable.zip`。Windows 包装配置和实际 Windows 验收继续按 SPEC 的对应阶段执行。

## 短时运行结果

电视安装成功，先通过 LEANBACK_LAUNCHER 解析得到 `app.flowplay.cross/.MainActivity`，再携带 MAIN / LEANBACK_LAUNCHER 显式启动该组件，冷启动 Status 为 ok，TotalTime 为 1249 ms。手机使用同一 APK，经普通 LAUNCHER 冷启动成功，TotalTime 为 1575 ms，截图确认底部五项导航和手机布局。

电视共记录 27 项短时检查结果，包括首页、搜索、直播、下载、我的、添加来源、关于、隐私与使用说明，以及焦点和返回路径。`DPAD_DOWN` 实际使焦点从“首页”移到“搜索”，`DPAD_RIGHT` 使焦点进入首页“添加来源”。来源说明经过连续下键可以读到最后的本地文件说明，上键可以回到返回按钮。每个阶段保存 UI 层级和真实模拟器截图。

初次逐键检查发现内容区按左键会按空间位置落到其他导航项。已将内容区左键明确路由到已选栏目，最终重新核对搜索、直播、下载及法律页的左返和系统返回路径，结果通过。

电视反馈入口实际发送 ACTION_SENDTO。该镜像由 `com.android.tv.frameworkpackagestubs/.Stubs$EmailStub` 接收，并显示系统“没有邮件应用”提示，证据为 `logs/android-tv-feedback-intent.log` 与 `logs/android-tv-feedback.png`。完整邮件撰写界面需要设备安装可处理邮件的应用。

手机与电视最终 AndroidRuntime 错误日志均为 0 字节。桌面最终 app image 在 macOS arm64 打开真实原生窗口，通过 PID 定位窗口后使用 screencapture 截图，再结束本轮进程。运行检查采用已有工程和真实空状态。

手机和电视模拟器均已执行 `adb emu kill`，两次返回 OK。收尾 `adb devices -l` 为空，本轮桌面进程已退出。按任务要求只执行短时运行检查。

## 截图与运行证据

| 内容 | 本机证据 | 交付副本 |
| --- | --- | --- |
| TV 首页，焦点进入添加来源 | `logs/android-tv-home.png` | `/Users/jiesen/fp-shots/cross/android-tv-home.png` |
| TV 初始首页焦点 | `logs/android-tv-final-focus-home.png`、同名 XML | 本仓库 logs |
| TV DPAD_DOWN 后的搜索焦点 | `logs/android-tv-final-focus-down.png`、同名 XML | 本仓库 logs |
| TV 来源说明和滚动 | `logs/android-tv-add-source.png`、`logs/android-tv-source-scrolled.png` | 本仓库 logs |
| TV 关于和法律页 | `logs/android-tv-about.png`、`logs/android-tv-privacy.png`、`logs/android-tv-usage.png` | 本仓库 logs |
| 手机首页回归 | `logs/android-phone-r0tv-home.png` | `/Users/jiesen/fp-shots/cross/android-home.png` |
| 最终桌面 app 首页 | `logs/desktop-r0tv-home.png` | `/Users/jiesen/fp-shots/cross/desktop-home.png` |
| TV 逐项运行结果 | `logs/android-tv-runtime-result.txt` | 本仓库 logs |
| 收尾设备与进程 | `logs/android-r0tv-devices-final.txt`、`logs/process-r0tv-final.txt` | 本仓库 logs |

TV 首页交付副本已逐字节比对，尺寸为 1920 × 1080。截图和运行日志位于被 Git 忽略的 logs，banner 和生成代码进入提交。

## 审计、路径偏差与待验范围

Apple 参考仓库只读，开工和收尾 `git status --porcelain` 比对一致。提交文件和本机运行证据已按本地配置的实际凭证值扫描，命中数为 0。

首次增量编译中，Kotlin 默认 daemon 曾在 `/Users/jiesen/Library/Application Support/kotlin/daemon` 生成一个 0 字节运行标记，超出任务写入路径范围。本轮已结束该 daemon，其运行标记随正常退出自动清除，收尾目录为空。`gradle.properties` 现固定 `kotlin.compiler.execution.strategy=in-process`，最终标准命令在该配置下强制重编译通过。此项为本轮工具默认行为产生并已处理的路径偏差。

当前 TV 壳层、安装、构建、焦点、截图与回归验收均具备。大海报横排在 M2 接入真实数据，播放器遥控和直播换台分别在 M3 与 M4 / M5 验收。TV 镜像邮件撰写受安装应用能力限制。当前 R0b 壳层交付的外部阻塞项为 0。
