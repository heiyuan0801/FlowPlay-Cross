# FlowPlay-Cross 产品与工程规格

规格版本 1.1.0，Round M1，2026-11-12。开发者 jiesen，联系与反馈邮箱 heiyuan6@gmail.com。

## 1. 范围与参考基线

目标平台为 Android 手机 / 平板、Android TV 与 Windows x64。桌面 JVM 同时在 macOS arm64 上进行运行检查。应用标识统一采用 `app.flowplay.cross`，应用名称 FlowPlay。Android 最低 API 26，编译与目标 API 35。Windows 最低计划 Windows 10 x64，最终最低版本在原生播放库集成时验证。

功能范围包含 Apple 版当前能力，以及历史移除或隐藏的 TVBox、JS 爬虫、直播、TMDB 热门榜单和 CMS 离线下载。来源完全由用户添加。来源列表初始为空。CMS 和 TVBox 在跨平台产品中直接可见。预览阶段仅展示已经实现的导航及说明入口。

只读基线为 `/Volumes/mac/codex/FlowPlay` 的 `1d1da24512dc6898af31de14fd33a5ef2f8815a9` 及开工时的工作区。参考仓库含用户未提交变更。参考读取覆盖 `.orchestrator/SPEC.md`、`docs/`、`Packages/`、平台导航、`Design/AppIcon/` 与历史，所有实现工作写入本仓库。

历史依据如下。

- `78b1e1c^` 的 `TVBoxDocumentDecoder.swift`、`TVBoxJavaScriptRuntime.swift`、`TVBoxRuleHost.js`、`CatalogAdapters.swift` 和直播提供器用于恢复 TVBox 与直播设计。`78b1e1c` 记录其移除。
- `77a159e` 的 `TMDBChart.swift`、`TrendingView.swift`、`TrendingDetailView.swift` 用于恢复热门榜单与用户来源匹配。
- `16af689^` 的 `DownloadManager.swift` 与离线传输实现用于恢复 CMS 下载。`16af689` 同时加入 TMDB 180 天上限并移除 CMS 新下载。
- 当前 `FlowTheme.swift` 的强调色为 RGB 0.08 / 0.57 / 0.53，圆角 14，间距 20。跨平台沿用近似青绿 `#149187`、浅灰分组表面与深色模式。

## 2. 功能对照矩阵

Apple 状态描述来自当前代码与上述历史。Android / Android TV / Windows 栏的 M 编号表示后续交付阶段。R0 仅交付工程骨架及明确列出的真实界面。

| 功能 | Apple 当前或历史状态 | Android 计划 | Android TV 计划 | Windows 计划 | 说明与验收重点 |
| --- | --- | --- | --- | --- | --- |
| 首页真实分区 | 已有继续观看、最近添加、电影、剧集 | M1 CMS 分类与列表已完成，M2 统一首页仓库 | M1 CMS 横排已完成，M2 大海报与继续观看 | M1 CMS 已实现并在 macOS 验证，M2 自适应网格 | 空来源保持真实空状态 |
| 五项主导航 | 当前 Apple 各平台布局不同 | R0 底部 首页 / 搜索 / 直播 / 下载 / 我的 | R0b 常驻侧边抽屉、五项导航与 D-pad 焦点 | R0 桌面侧栏同五项 | 历史媒体库能力进入首页与来源详情 |
| 来源增删改、启停、排序、刷新 | 当前 SourceManager 已有 | M1 已完成 | M1 已完成，遥控表单与系统输入法 | M1 已实现，macOS 已运行，Windows 待运行 | 多来源独立标识，删除来源保留独立下载 |
| 来源 JSON 导入 / 导出 | Apple 来源模型作只读参考 | M1 已完成 SAF 与 URL 导入 | M1 URL 导入与设备文件选择能力反馈 | M1 原生文件对话框已实现，macOS 已运行 | schemaVersion 1，事务导入、规范化去重、逐项错误报告，导出排除请求头与潜在认证参数 |
| Emby 多服务器登录 | 当前已有，凭证使用 Keychain | M2 Keystore 保护会话 | M2 同 Android，遥控输入与 Keystore | M2 Windows DPAPI 保护会话 | 密码仅认证时使用，服务器和用户作用域隔离 |
| Emby 媒体库和分页 | 当前已有 | M2 分类、分页与筛选 | M2 大海报横排、分页与焦点恢复 | M2 同共享 Provider | 电影、剧集、季、集、年份、类型、排序 |
| Emby 搜索与详情 | 当前已有 | M2 | M2 大屏详情与遥控分集选择 | M2 | 海报、背景、简介、关联外部 ID |
| Emby Direct Play / Stream / Transcode | 当前 PlaybackInfo 协商 | M3 Media3 设备能力协商 | M3 Media3，按电视设备能力协商 | M3 libmpv 能力协商 | 会话 ID、字幕及音轨随播放参数传递 |
| Emby 开始 / 进度 / 停止上报 | 当前已有 | M3 生命周期与 MediaSession | M3 同 Android 播放会话 | M3 窗口与进程生命周期 | seek、暂停、切集、异常退出重试去重 |
| Emby 继续观看与下一集 | 当前已有 | M3 | M3 遥控进入继续观看与下一集 | M3 | 服务端进度为 Emby 真值，保留本机暂存 |
| Emby 下载权限 | 当前检查服务器许可 | M6 根据用户策略和下载接口开放 | M6 同 Android 许可检查 | M6 同共享许可检查 | 403 / 404、权限撤销与地址失效明确反馈 |
| Apple CMS JSON / XML | 当前存在，默认高级开关隐藏 | M1 已完成 JSON / XML | M1 已完成，同共享 Provider | M1 已实现，macOS 已运行，Windows 待运行 | 分类、分页、详情、线路、分集、请求头 |
| CMS 离线下载 | `16af689` 移除新下载，保留完成文件 | M6 恢复 MP4 / HLS | M6 遥控选择分集与管理下载 | M6 同共享下载器 | 可选择单集、多集、全部，暂停恢复删除 |
| TVBox URL / 本地配置 | `78b1e1c` 移除 | M5 恢复导入刷新 | M5 同 Android 导入刷新 | M5 恢复导入刷新 | 多仓 urls、sites、parses、lives、headers、ext |
| TVBox JSONC 与包装 | 历史支持注释、尾逗号、Base64、图片尾部 | M5 共享解码器 | M5 同共享解码器 | M5 共享解码器 | 8 MiB、4 层包装上限，保留字符串中 URL |
| TVBox CMS / HTTP spider | 历史 Adapter 有支持 | M5 统一映射 Provider | M5 同共享 Provider | M5 统一映射 Provider | type 0 / 1 / 4 按接口能力识别 |
| TVBox JS / drpy | 历史 JavaScriptCore 最小规则宿主 | M5 QuickJS 与兼容宿主 | M5 同 Android QuickJS 宿主 | M5 QuickJS JVM JNI | home / category / detail / search / play 等 |
| TVBox JAR / csp | 历史大量环境不兼容 | M5 能力识别，M8 Dex 兼容插件 | M5 能力识别，M8 同 Android Dex 宿主 | M5 能力识别，M8 JVM 兼容插件 | Android 类依赖与 dex 格式需逐类适配 |
| TVBox 解析线路 | 历史配置包含解析器 | M5 HTTP / JSON 解析，M8 WebView 嗅探适配 | M5 / M8 同 Android，遥控线路选择 | M5 HTTP / JSON，M8 桌面浏览器宿主评估 | 明确区分直接地址、解析请求与网页 |
| TVBox 直播 | `78b1e1c` 移除 | M4 / M5 | M4 / M5 遥控上下换台、OK 控制 | M4 / M5 | group、频道、备用地址与台标 |
| 独立直播 M3U / TXT | 历史已有 | M4 导入和播放 | M4 频道分组、上下换台与备用地址 | M4 导入和播放 | EXTINF、分组、逗号 TXT、多地址切换 |
| 聚合搜索 | 当前覆盖 Emby、CMS、本地 | M1 CMS 分来源并发搜索与分页已完成，M2 聚合 | M1 CMS 搜索已完成，M2 聚合与焦点恢复 | M1 CMS 搜索已在 macOS 运行，M2 聚合 | 标题、类型、年份保守聚合，可展开全部来源 |
| 来源切换与备用线路 | 当前已有替代项查询 | M3 / M5 | M3 / M5 遥控选择来源与线路 | M3 / M5 | 每个播放项保留实际 sourceId，失败有原因 |
| TMDB 元数据 | 当前已有匹配、海报、简介、演职员、季集 | M2 Ktor + Coil 3 | M2 大海报 / 背景 / 详情，同共享缓存 | M2 同共享服务 | 优先外部 ID，其次标题 / 年份 / 类型置信度 |
| TMDB 人工匹配 | 当前有选项与本机匹配记录 | M2 手工选匹配 | M2 遥控选择匹配项 | M2 手工选匹配 | 同名重拍与剧集类型避免误合并 |
| TMDB 热门榜单 | `77a159e` 新增，`12e3f09` 移除入口 | M7 恢复首页榜单 | M7 大海报横排与遥控浏览 | M7 恢复首页榜单 | 今日、本周、热门电影、热门剧集、高分电影 / 剧集 |
| 热门详情匹配自有来源 | 历史已有 | M7 详情与聚合搜索衔接 | M7 详情与来源搜索衔接 | M7 同共享逻辑 | TMDB 条目 ID 与实际播放媒体 ID 独立 |
| TMDB 缓存 | 当前 7 天刷新，180 天绝对删除 | M2 共享缓存策略 | M2 同 Android 缓存策略 | M2 共享缓存策略 | 见数据模型章节 |
| 详情与分集选择 | 当前已有，详情后播放 | M1 CMS 已完成，M2 / M3 扩展 | M1 CMS 遥控线路与分集已完成，M2 / M3 扩展 | M1 CMS 已在 macOS 运行，M2 / M3 扩展 | M1 展示解析地址、复制与系统外部打开，M3 集成播放器 |
| 收藏 / 稍后观看 / 历史 / 最近播放 | 当前已有本机集合 | M2 SQLDelight | M2 同共享数据库与焦点恢复 | M2 SQLDelight | 离线使用、唯一键、持久化与撤销 |
| 评分与外部资料链接 | 当前有来源评分、TMDB / IMDb / 豆瓣链接 | M2 系统浏览器打开 | M2 按系统可处理能力提供入口 | M2 系统浏览器打开 | 豆瓣评分保留来源标识，外部资料与播放来源独立 |
| 本地视频与外部字幕 | 当前导入复制、本机索引 | M4 SAF 与持久 URI 权限 | M4 系统文件选择与持久 URI 权限 | M4 文件选择、拖放、路径重定位 | 本地文件失效有恢复入口 |
| 基础播放与 seek / 倍速 / 音量 | 当前 AVPlayer + FFmpeg | M3 Media3 | M3 OK 播放暂停、左右 seek、系统音量 | M3 libmpv | 真实 position、duration、buffered ranges |
| 宽高比、旋转、全屏 | 当前已有 | M3 手机横屏 / 平板自适应 | M3 电视全屏与宽高比选择 | M3 独立播放窗 / 全屏 | 各平台输入与窗口行为适配 |
| 音轨、内外字幕、字幕延迟 | 当前已有多轨与 ASS / bitmap 路径 | M3 原生字幕，M8 补齐 ASS / bitmap 限制 | M3 Menu / 上键打开音轨、字幕、画质菜单 | M3 libass / libmpv | Android FFmpeg 音频扩展覆盖范围单独验证 |
| 下一集、片头片尾跳过、记忆音字幕 | 当前已有 | M3 / M7 | M3 / M7 遥控选择与共享偏好 | M3 / M7 | 以剧集身份存偏好，跨集轨道按语言等匹配 |
| 缓冲与恢复 | 当前有读前、磁盘缓存、超时恢复 | M3 / M7 | M3 / M7 同共享缓冲状态与恢复 | M3 / M7 | 30 / 60 / 120 秒，1 / 2 / 5 GiB，质量自动 / 原画 / 码率 |
| 锁屏 / 后台媒体控制 | Apple Now Playing 已有 | M3 MediaSessionService | M3 MediaSession 与遥控媒体键 | M3 Windows SMTC 适配 | 生命周期与停止进度上报衔接 |
| 画中画 / 小窗 | Apple 原生支持 | M3 Android PiP | M3 按电视系统实际能力适配 | M3 置顶迷你播放窗 | 保持播放会话与控制状态 |
| 控制栏自动隐藏与快捷键 | 当前 Mac 已有 | M3 触控隐藏、硬件按键 | M3 返回先隐藏控制 / 关闭菜单，再退出播放 | M3 空格 / 方向 / F / Esc / M | 暂停、菜单展开保持控制可见 |
| DLNA / SSDP / SOAP | 当前 macOS 实现，真实接收设备仍需验证 | M6 局域网投屏 | M6 同 Android 发送端与遥控设备选择 | M6 局域网投屏 | 发现完成与连接播放成功分开验收 |
| Google Cast | 当前 iPhone / iPad 原生 SDK | M6 Android 官方 Cast SDK | M6 按电视设备的 Cast SDK 能力接入 | M8 桌面 Cast v2 sender 适配评估 | Windows 官方 sender SDK 缺口单列 |
| AirPlay 路由 | 当前 Apple 系统路由 | M8 AirPlay sender 可行性与兼容验证 | M8 同 Android 可行性验证 | M8 同协议实现研究 | Android / Windows 缺少 Apple 系统路由，兼容完成度待验证 |
| 下载管理与存储用量 | 当前已有 | M6 工作任务、后台通知、存储统计 | M6 遥控任务管理与存储统计 | M6 下载队列、进程重启恢复 | 完成后无网播放、删除后空间恢复 |
| 设置与持久化 | 当前 SwiftData / UserDefaults | M1 来源 SQLDelight 已完成，M7 偏好 | M1 来源持久化已完成，M7 大屏偏好 | M1 来源持久化已在 macOS 验证，M7 偏好 | 范围校验与运行时一致 |
| 法律 / 关于 / 反馈 | 当前已有 | R0 版本、开发者、邮箱、mailto、简要说明，M8 完整许可页 | R0b 遥控阅读、滚动与邮件能力反馈 | 同 Android | 邮件由用户自行发送 |
| 响应式、多窗口、无障碍 | Apple 各端原生布局 | M7 手机 / 平板 / 键盘 / TalkBack | R0b 10-foot / 高对比焦点，M7 完善无障碍 | M7 可调整窗口、多窗口与屏幕阅读 | 中文、深色、字体缩放、焦点可见 |

## 3. 架构与模块

采用单 Gradle 仓库，Kotlin 2.3.10、Compose Multiplatform 1.9.3、AGP 8.11.1、Gradle 8.14.3、JDK 21。版本固定在版本目录与 wrapper 中，Kotlin 编译警告按错误处理。兼容范围按 Kotlin 官方表核对，AGP 与 Gradle 处于该 Kotlin 版本支持范围。[Kotlin 兼容表](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)

```text
androidApp/       手机 / TV 共用 Activity、UiModeManager、Media3、SAF、后台任务、Keystore、DLNA 平台能力
desktopApp/       JVM Window、libmpv JNA、文件选择、DPAPI、桌面包装
shared/
  src/commonMain/kotlin/app/flowplay/cross/
    model/       MediaId、MediaItem、MediaSource、PlaybackProgress
    provider/    MediaProvider、DownloadResource，后续 Emby / CMS / TVBox / Local
    playback/    PlaybackEngine、状态流与瞬时 PlaybackRequest
    ui/          主题、中文导航与实际页面
    repository/  M1 来源 / 媒体聚合、缓存、应用状态
    storage/     M1 SQLDelight schema、仓库与迁移
    network/     M1 Ktor HTTP 策略与错误映射
    download/    M6 清单、分段、断点恢复与离线索引
    casting/     M6 SSDP、UPnP SOAP 与投屏会话
    tvbox/       M5 配置解码、站点模型、JS 宿主协议
  src/androidMain/ 平台驱动与适配
  src/desktopMain/ JVM 平台驱动与适配
assets/icons/    复制的原始图标与生成的 icns / ico
scripts/         环境与图标生成
.orchestrator/   SPEC 与阶段报告
```

入口依赖 `shared.ui`，UI 依赖应用状态与 Repository，Repository 依赖 Provider / Storage / Network。平台播放表面由平台组合到共享播放器控制层。UI 使用统一媒体标识和错误模型，源地址、认证与具体 Provider 请求在数据层处理。R0 已建立公共媒体、Provider 与 PlaybackEngine 契约。M1 的 SourceRepository、CatalogHttp、CmsProvider 和来源 / 目录 UI 已接入，平台层注入数据库驱动、文件选择、剪贴板与外部打开。后续媒体类型继续扩展统一媒体契约。

### Android TV 入口与焦点

R0b 复用 `androidApp` APK、applicationId、版本与数据层。`UiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION` 决定电视布局。MainActivity 同时声明普通 LAUNCHER 与 LEANBACK_LAUNCHER，leanback 和 touchscreen 两个 uses-feature 均为 optional。320 × 180 的 xhdpi banner 由已复制的 Apple 原始图标生成，应用名直接合成到图中。共用 APK 可以沿用网络、Keystore、Media3 与后续数据库，手机和电视各自保留输入布局。当前规模由三模块承载。[Android TV 入口规范](https://developer.android.com/training/tv/get-started/create)

`shared.ui.TvShell` 提供常驻侧边抽屉、32 dp 水平 / 24 dp 垂直安全边距、深色背景、20 sp 正文与 28–32 sp 标题。选中导航有底色，实际焦点有高对比填充与描边。使用现有 Compose Foundation / UI 的 focusGroup、FocusRequester、focusProperties、onFocusChanged、clickable 和 focusable，依赖沿用 Compose Multiplatform 1.9.3。上下移动导航，OK 进入对应栏目，右键进入内容，内容内左键优先移动到同排控件，离开内容焦点组时回到已选导航。文本框在系统输入法收起后通过上下键切换表单控件。进入子页聚焦返回按钮，返回恢复已选导航，内容区返回键先回侧栏，根导航返回交给系统。说明面板可以用上下键滚动，所有已显示操作均有实际页面或系统行为。[Compose 焦点控制](https://developer.android.com/develop/ui/compose/touch-input/focus/change-focus-behavior)

M1 首页在来源为空或全部停用时保留真实空状态。启用 CMS 后呈现来源、分类与横排分页列表。M2 使用 LazyRow 大海报横排，海报目标约 148 × 222 dp，每行独立焦点组，按 MediaId 恢复位置，聚焦时滚入视口。大海报行随 M2 Provider 接入验收，使用来源实际媒体与匹配后的 TMDB 图像。

M3 将电视遥控输入接到同一 PlaybackEngine。点播 OK 播放 / 暂停，左右快退 / 快进，Menu 或上键打开音轨 / 字幕 / 画质选择。返回优先关闭菜单、隐藏控制，再退出播放并上报进度。M4 / M5 的直播与 TVBox 播放在菜单关闭时用上下键切换频道，菜单展开时方向键只操作菜单。播放器按当前输入模式分发按键，避免频道切换与轨道菜单争用上键。媒体按键通过 MediaSession 处理。

请求使用协程结构化并发。M1 搜索通过提交关键词启动，重复提交会刷新，重新提交取消旧任务，来源分页独立推进，错误保留到各来源结果。播放引擎暴露 `StateFlow<PlaybackState>`，控制方法覆盖 load、play、pause、seek、rate、volume、音字幕轨、外部字幕、stop、release。认证 URL 与请求头使用瞬时普通类，避免数据类自动生成包含地址的日志文本。

## 4. 数据与存储

M1 已接入 SQLDelight 2.1.0。Android 使用 AndroidSqliteDriver，在应用私有 databases/flowplay.db 创建或升级数据库。桌面使用 JdbcSqliteDriver，Windows 默认 `%APPDATA%/FlowPlay/flowplay.db`，macOS 默认 `~/Library/Application Support/FlowPlay/flowplay.db`，Linux 使用 XDG 数据目录。开发运行通过 JVM 属性 `flowplay.dataDir` 隔离到仓库 `logs/m1/desktop-data`，满足开发写入范围。

当前数据库 schema 为版本 2。Sources.sq 描述当前结构，1.sqm 使用 ALTER TABLE 将版本 1 原地升级并加入 extraConfigJson，默认值为 `{}`。SQLDelight 驱动执行版本管理，M2 可通过 2.sqm 增加表。来源事务同时完成修改与重新读取，成功提交后才发布 StateFlow，写入或读取异常会回滚旧配置。轻量偏好库已锁定版本 1.3.0，通用偏好在后续阶段接入。

| 数据 | 主键与字段 | 一致性 / 保留策略 |
| --- | --- | --- |
| Source | id UUID、type、name、baseUrl、enabled、sortOrder、createdAt、updatedAt、extraConfigJson | M1 已落表，baseUrl 规范化且 type + baseUrl 唯一，来源为空初始零行，排序 / 导入事务提交，保存失败回滚 |
| SourceSite | sourceId + siteKey、类型、ext 引用、能力、配置指纹 | 多仓子站点稳定身份，更新按 siteKey 合并 |
| CredentialRef | sourceId + userId、系统安全存储引用 | 数据库只放引用，密码仅瞬时使用 |
| Media / Library | sourceId + providerId、类型、父季集、公开外部 ID、标题与索引 | 同一标题跨源保留各自身份 |
| History | sourceId + mediaId、positionMs、durationMs、updatedAt、pendingReport | 本机原子更新，Emby 会话事件重试有会话和序列标识 |
| CollectionMembership | sourceId + mediaId + collection、createdAt | 收藏与稍后观看各自唯一，重复操作幂等 |
| PlaybackPreference | seriesId 或媒体 ID、音字幕描述、倍速、跳过秒数 | 旧记录缺字段使用默认值，跨集避免依赖易变轨道 index |
| Download | UUID、mediaId、标题快照、格式、状态、进度、目标目录、任务 revision | 唯一活动任务约束，临时文件完成后原子 rename |
| DownloadSegment | downloadId + 序号 / byteRange、校验、字节数、完成状态 | 写盘成功后标记完成，崩溃恢复重新核对文件 |
| Settings | key、schemaVersion、value | 在 UI、持久化、运行时统一范围校验 |
| TMDBMetadata | mediaType + tmdbId + language、JSON、fetchedAt | 7 天刷新，获取后 180 天绝对上限 |
| TMDBMatch | source mediaId、候选 ID、confidence、manual、fetchedAt | 清理与资料一致，命中缓存保持原 fetchedAt |
| PublicImageCache | URL 哈希、路径、fetchedAt、bytes、lastAccessAt | TTL + LRU 容量控制，鉴权图仅内存缓存 |
| Search / SourceCache | 来源 revision + 查询参数、分页结果、fetchedAt | 刷新来源失效关联缓存，错误不覆盖旧成功数据 |

TMDB 缓存元数据、演职员、匹配、图片引用和图片字节均带实际获取时间。启动时、每小时以及每次读取时清理达到 180 天、缺少时间或未来异常时间的条目。7 天后尝试刷新，断网仅允许使用绝对期限内条目。热门列表计划缓存 6 小时，详情仍遵循 7 天 / 180 天策略。成功网络刷新才更新 fetchedAt。图库容量采用 LRU，不影响绝对期限。清理设置提供资料缓存、播放缓存与下载的独立统计和动作。

TMDB 配置沿用本地配置注入机制。本仓库 `secrets.properties` 被忽略，示例仅空占位。Android 生成 BuildConfig，桌面生成 `build/generated/flowplay` 源码。CI 可从环境读取 `TMDB_API_KEY`。应用构建产物可能包含客户端 API 配置，源码与报告不记录其值。Emby 会话与带认证 URL 仅流经运行时，日志使用固定事件、数值与匿名来源标识。

## 5. 网络与图片

Ktor 3.3.1，Android / Desktop 使用 OkHttp engine。kotlinx.serialization 1.9.0 解析 JSON，CMS XML 使用关闭外部实体和 DTD 的平台 XML 解析适配器，输出统一模型。协程 1.10.2 控制并发。Coil 3.3.0 使用 Ktor 网络桥接并遵循上述图片缓存策略，默认 Coil 磁盘缓存对 TMDB 图片关闭或接入带 fetchedAt 的统一存储。

HTTP 策略区分目录、搜索、播放、下载。目录连接约 15 秒，总请求约 30 秒。播放按引擎缓冲恢复策略处理长连接，下载使用读空闲超时。仅幂等 GET 使用有界指数退避，429 尊重 Retry-After，登录和播放事件依各自幂等语义重试。支持来源级 User-Agent、Referer 与必要请求头。跨主机重定向丢弃认证头，307 / 308 保持方法语义。来源错误携带中文可读原因、阶段和可重试标记。

用户提供的 HTTP 局域网来源在明确选择后允许连接，平台网络配置在 M1 随导入一起启用。证书默认正常验证，调试代理配置仅用于开发工具链。聚合搜索初始并发上限 4，每源独立超时、分页和失败状态。M1 仅向用户启用或主动测试的来源发请求。无来源时没有目录请求，TMDB 在 M2 接入。

### M1 CMS 与来源交换

来源表单支持名称、完整 HTTP / HTTPS 地址、JSON / XML / 自动识别、list / videolist / detail 列表动作和来源请求头 JSON。Ktor 请求覆盖 ac=list、ac=videolist、ac=detail&ids=、wd、t、pg。列表动作失败可回退 list，详情缺少条目可回退 videolist&ids=。JSON 数字与字符串标量、XML class / list / video / dl / dd、CDATA、字符编码声明映射到统一 CMS 模型。XML 拒绝 DTD 与实体声明，禁用外部实体加载。

vod_play_from 与 vod_play_url 以 $$$ 保留线路位置，# 或换行拆分分集，首个 $ 分开名称与地址，兼容空线路名、空分集名、裸地址、空片段、相对路径及 XML dd flag。首页展示启用来源，搜索按来源并发执行并保留独立分页与错误。详情展示海报、简介、年份、地区、演员、导演和线路 / 分集。点击分集后展示地址，复制调用系统剪贴板，外部打开使用 Android ACTION_VIEW 或桌面 Desktop.browse。播放器集成仍在 M3。

交换文档含 schemaVersion 1 与 sources 数组，每项含 type、name、baseUrl、enabled、config。导入最多 2 MiB / 500 项，先逐项校验，再用单事务加入有效且未重复的来源，已有来源保持配置与身份。错误报告包含序号和校验原因。Android 使用 SAF OpenDocument / CreateDocument，桌面使用原生 FileDialog，电视提供 URL 导入与文件选择能力错误反馈。

导出采用固定字段白名单，config 仅包含格式与列表动作，请求头留在本机。当前只导出 CMS 的无查询参数公开地址，含查询参数或未来受保护类型的来源显示跳过数量，避免认证信息进入文件。Windows 原生文件选择与外部打开仍需 Windows 环境运行验证。

## 6. 播放器方案

### Android

采用 Media3 / ExoPlayer 1.8.0，集成 HLS、DASH、常见容器、MediaSession 与平台硬件解码。音频 FFmpeg decoder extension 从对应 Media3 源码构建，与 Media3 同版本，覆盖平台缺少的音频解码。扩展作为独立 Android 库及 ABI `.so` 打包，计划 arm64-v8a、armeabi-v7a、x86_64，模拟器使用 arm64-v8a。优先平台视频硬解，FFmpeg 音频扩展按 `DefaultRenderersFactory` 设置使用。构建选 LGPL 兼容 FFmpeg 配置，保留许可证和可重建源码。[Media3 格式与扩展说明](https://developer.android.com/media/media3/exoplayer/supported-formats)

FFmpeg 音频扩展覆盖音频 sample 解码。视频 codec 和复杂 ASS / bitmap 字幕仍需 M3 实际兼容清单，M8 补齐渲染能力。Compose 通过 AndroidView / SurfaceView 嵌入视频，控制 UI 与跨平台状态保持分离。后台由 MediaSessionService 管理，PiP 使用系统 API。R0 只包含接口。

### Desktop

选用 libmpv 通过 JNA 5.17.0 调用 C API，目标固定 mpv 0.40.0 稳定源码基线，实际原生二进制版本在 M3 锁定并校验。libmpv 基于 FFmpeg 处理常见容器、HLS、音视频 codec，libass 提供 ASS 字幕。Windows 可用 D3D11 硬件解码，macOS 使用 VideoToolbox。JNA 映射的 C API 比较适合事件与属性统一到公共状态流。[libmpv 客户端 API](https://github.com/mpv-player/mpv/blob/master/include/mpv/client.h)

Windows 首版通过 AWT Canvas 的 HWND 与 libmpv `wid` 嵌入，Compose `SwingPanel` 承载原生表面，控制条安排在表面外侧或独立层，处理 AWT 重量组件的叠放限制。macOS 验证使用 libmpv render API + 平台 OpenGL 表面桥接，避免依赖 macOS window embedding 的已知限制。所有 mpv 操作串行处理，事件线程读取后转换为协程状态流，关闭时停止事件线程并销毁句柄。[libmpv 嵌入示例](https://github.com/mpv-player/mpv-examples/tree/master/libmpv)

mpv 默认构建包含 GPL 代码，原生构建使用 `-Dgpl=false` 并逐项检查 FFmpeg、libass、依赖与构建选项的许可证，保留具体源码、patch、构建脚本和许可。该开关本身不能代替依赖许可核对。采用动态链接并允许更换兼容 DLL。[mpv 版权与构建说明](https://github.com/mpv-player/mpv/blob/master/Copyright)

Windows x64 原生包在 M3 放入 `desktopApp/native-resources/windows-x64/`，包括 `mpv-2.dll`、其依赖 DLL、版本 / SHA-256 清单及许可证。通过 Compose appResourcesRootDir 随应用复制，运行时从 `compose.application.resources.dir` 得到绝对路径，JNA 在加载主库前设置该受控目录的搜索路径。JNA 自身 JNI 与 QuickJS JNI 也按 Windows x64 检查。macOS arm64 使用对应 `.dylib`，修正安装名并随 app 资源打包。R0 原生播放器目录为空，当前 app 的全部可见功能仅依赖 JVM / Compose。

## 7. 投屏

共享层实现 SSDP `M-SEARCH`，解析设备描述与服务 URL，按 UDN 去重并处理 max-age。SOAP 覆盖 AVTransport `SetAVTransportURI`、Play、Pause、Stop、Seek、GetPositionInfo，RenderingControl 音量。会话状态与设备消失、网络切换有统一中文错误。

Android 使用 MulticastLock 和局域网 UDP，按目标系统声明所需网络权限。Windows JVM 使用 DatagramSocket 与多网卡枚举，设备发现通过系统防火墙网络提示取得连接能力。macOS 运行检查阶段不扫描用户设备，M6 再验证本地网络权限。

接收端需访问媒体。公开 URL 可直接投递。需认证的流、本地文件及离线 HLS 通过用户启动的本机 HTTP 转发会话提供，仅绑定当前局域网接口、支持 Range、限制路径与会话期限，认证留在发送端。播放停止即关闭会话。转发地址与凭证不写日志。Android Google Cast 使用官方 SDK，Windows Cast v2 与 AirPlay sender 保留在 M8 专项可行性及接收端兼容验证。DLNA 真机连接播放、控制与进度是独立验收条件。

## 8. 下载引擎

统一下载任务状态为 queued、resolving、downloading、paused、completed、failed、cancelled。共享调度器负责权限、地址解析、分段清单、状态持久化与恢复，平台负责后台执行。Android 结合 WorkManager / 前台长任务通知，Windows 使用进程内队列，重启后恢复中断任务。用户取消与网络错误分开处理。

- MP4 等渐进式文件写入 `.part`，记录 ETag / Last-Modified 与长度，恢复时使用 Range + If-Range。服务器返回完整 200 时重建临时文件，206 校验 Content-Range。大小及可用空间先检查，完成后原子改名。
- HLS 先解析 master playlist，选择清晰度、音轨、字幕对应变体。处理相对 URL、EXT-X-MAP、EXT-X-BYTERANGE、DISCONTINUITY、媒体序号与 AES-128。将清单、片段与必要媒体密钥保存在任务私有目录，重写为本机 URI。SAMPLE-AES / DRM 按能力返回具体原因。直播录制单列后续能力，R0 产品离线以有限 VOD 清单为范围。
- 片段并发初始为 3，全局任务初始为 2。任务 revision 与原子文件操作防止暂停 / 删除 / 重试竞态。密钥使用平台保护存储或加密私有文件，清单避免保存账号认证。带认证地址仅在恢复时通过 Provider 重新解析。
- Emby 先查用户下载许可再解析接口，CMS / TVBox 按解析后的媒体格式走同一引擎。每集独立任务，批量操作使用组标识。完成状态以所有必需文件落盘且本地清单可解析为准。离线播放、进程重启恢复与删除后空间回收分别验证。

## 9. TVBox 与 JS 运行时

共享配置层支持 URL、本地文件、JSONC、多仓 `urls`、站点 `sites`、解析器 `parses`、直播 `lives`、`spider`、`ext` 和请求头。相对 URL 以配置或脚本地址为基准。配置更新在事务中比对站点 key 并保留用户偏好。未知类型保留诊断信息，UI 展示具体能力状态。

JS 统一选 QuickJS。Android 专用 quickjs-android 可运行脚本，但 JVM 需要额外 JNI 桥接。选用 `io.github.dokar3:quickjs-kt:1.0.3` 作为两端统一候选，已核对 Maven 元数据存在 Android 与 JVM variants。实际下载的 JVM JAR 包含 `jni/windows_x64/libquickjs.dll`、`jni/macos_aarch64/libquickjs.dylib`，另含 macOS x64 与 Linux 两种架构。其 POM 依赖 Kotlin 2.3.10，工程编译器与其统一。该项目提供协程、模块与取消中断能力。M5 验证原生库加载、脚本执行与取消，再落实随包和提取路径。[quickjs-kt 项目](https://github.com/dokar3/quickjs-kt)

每站点独立 QuickJS context 与串行 dispatcher。宿主实现 `init`、`home`、`homeVod`、`category`、`detail`、`search`、`play`，兼容 drpy 所需的 request、URL join、HTML / JSON 选择器、编码与常见加解密。使用同一 Ktor 传输层处理 HTTP，状态按站点隔离。JS 与 drpy 宿主采用明确兼容版本。内存、栈、执行时间、请求次数与结果大小有上限，取消必须可中断原生执行。外部脚本只接触显式绑定的宿主 API。脚本源码变化使字节码缓存失效，外部版本不匹配的字节码不直接加载。

`csp_*` 通常依赖 Android Spider API。Android DexClassLoader 可加载含 dex 的 JAR / APK，普通仅含 `.class` 的 JAR 需要先经 d8 转换，Android API、Spider 基类、资源、ABI 与 native so 仍需对应宿主。Android 14+ 动态代码文件需满足只读约束。M8 做独立进程宿主和接口版本检查。Desktop 仅可用 URLClassLoader 直接加载纯 JVM JAR，依赖 android.* 的 csp 扩展需要 Android API shim 或重新编译。两端均先识别能力与依赖，完整 csp 生态兼容需逐项实现并验证，M5 不用假结果占位。

## 10. Windows 分发与图标

Compose Desktop 原生分发配置 `packageName = FlowPlay`、vendor `jiesen`、Msi / Exe，稳定 upgradeUuid 与 per-user 安装。Windows 使用 `.ico`，macOS 使用 `.icns`，窗口使用透明 PNG。Android adaptive foreground 来自同一透明 PNG，背景沿用 Apple 生成器的四边插值蓝紫 / 珊瑚渐变，legacy mipmap 由同一背景与透明前景合成。原始图像从 Apple 的 Design/AppIcon 复制，生成脚本位于 `scripts/generate-icons.py`。

`createDistributable` 生成包含专用 Java runtime 的 app image，`portableZip` 压缩整个 app image。Windows x64 上运行产生可解压运行的 `FlowPlay.exe`。macOS 执行同任务生成 macOS app / zip。MSI / EXE 安装包使用 Windows JDK 的 jpackage，Windows CI 安装 WiX 3 并验证工具 PATH，工作流仅保存 artifact。跨 OS 安装器需在对应 OS 构建。[Compose 原生分发文档](https://kotlinlang.org/docs/multiplatform/compose-native-distribution.html)

## 11. 里程碑与验收

| 阶段 | 明确范围 | 完成条件 |
| --- | --- | --- |
| M0 / R0 | SPEC、工具链、wrapper、三模块、中文导航与空状态、图标、密钥注入、包装配置 | Android debug 构建，Mac 桌面 run / dmg / portable，模拟器与桌面截图，报告和提交 |
| R0b / TV | 同 APK 电视入口、320 × 180 banner、10-foot 抽屉、D-pad / 返回 / 滚动焦点 | 手机回归，fp_tv arm64 TV 镜像安装、LEANBACK 启动与方向键截图，Android / Desktop 构建零 Kotlin 警告 |
| M1 已完成 | 来源管理、SQLDelight schema 2 与迁移、Ktor、CMS JSON / XML、来源 JSON 导入导出 | 手机 / TV 同包与 macOS 短时运行，重启保留，分类 / 分页 / 详情 / 搜索取自本机临时接口，失败保留旧配置，Windows 待运行 |
| M2 | Emby 登录 / 库 / 搜索、首页、聚合搜索、收藏历史、TMDB 资料和缓存、TV 大海报横排与焦点恢复 | 两端真实目录与详情，多个来源流式结果，TMDB 匹配 / 缓存边界可观察 |
| M3 | Media3 + FFmpeg 音频扩展、libmpv + JNA 原生打包、完整播放控制与进度、TV 遥控映射与控制菜单 | 两端真实本地 / 网络播放、seek / 多轨 / 字幕 / 下一集、Emby 上报与停止清理，Windows 原生包运行 |
| M4 | 本地文件 / 拖放 / SAF、M3U / TXT 直播、TV 遥控换台 | 无网本地播放，列表分组、多地址和直播错误恢复 |
| M5 | TVBox 配置 / 多仓、HTTP 站点、QuickJS / drpy、解析线路与 TVBox 直播 | 两端用户配置导入到搜索 / 详情 / 播放，JS 取消与能力错误真实可见 |
| M6 | MP4 / HLS 离线、单集多集、Emby 权限、DLNA 与 Android Cast | 断网播放、重启续传、删任务回收空间，接收设备实际播放和控制 |
| M7 | TMDB 全部热门榜单、榜单匹配、缓冲偏好、响应式与键盘无障碍 | 来源为空仍可按配置查看真实 TMDB 榜单，匹配后进入来源详情，设置范围贯穿运行时 |
| M8 | csp / JAR 宿主、Windows Cast / AirPlay 可行性、复杂字幕、完整许可、Windows 发布复核 | 扩展逐项兼容表、原生依赖许可与来源清单、Windows 真机短时验收、剩余平台差异明确记录 |

R0 采用短时运行检查，按任务要求跳过单元测试。后续阶段分别记录编译、平台运行、真实服务器行为和实际接收端结果。M1 的来源持久化与 CMS Provider 已完成，验收证据见 report-m1.md。原生播放器、Emby / TVBox / 本地 Provider、榜单、下载和投屏按后续里程碑接入。
