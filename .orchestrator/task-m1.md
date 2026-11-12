# Round m1：SPEC 里程碑 M1 —— 来源管理 + SQLDelight 持久化 + Apple CMS 完整支持

仓库 /Volumes/mac/codex/FlowPlay-Cross（main 当前 9b03b85）。开工先读 .orchestrator/SPEC.md、report-r0.md、report-r0tv.md 与 git log。工具链在 /Volumes/mac/codex/.toolchains，环境用 `source scripts/env.sh`。沿用 r0/r0tv 的写入路径约束（gradle.properties 已固定 in-process 编译）。

只读参考：/Volumes/mac/codex/FlowPlay（Apple 版）。绝对不要修改它，里面有用户未提交的改动；只用 `git -C /Volumes/mac/codex/FlowPlay show <rev>:<path>` 或只读 cat 查看。CMS 解析参考 Packages/ 下 CMS 相关 Provider、SourceManager、16af689^ 的下载实现（本轮不做下载，仅参考数据模型）。

## 本轮交付（手机 / TV 同包 APK + 桌面，共享 shared 模块）
1. SQLDelight 数据库与 schema 迁移（版本化，后续 M2 可加表）：sources 表（id、type、name、baseUrl、enabled、sortOrder、createdAt、updatedAt、额外配置 JSON 等），Android 用 AndroidSqliteDriver，桌面用 JdbcSqliteDriver（落在用户数据目录，Windows 走 %APPDATA%/FlowPlay，macOS 走 ~/Library/Application Support/FlowPlay）。
2. 来源管理 UI：列表、新增、编辑、删除（二次确认）、启用/停用、排序或刷新、连接测试（真实请求，失败显示真实错误）。保存失败保留旧配置。手机、TV（D-pad 可达、系统输入法、焦点可见）、桌面三端都可用。来源列表初始为空，空状态引导添加。
3. Apple CMS 来源类型完整支持（Ktor 客户端）：JSON 与 XML 两种接口（?ac=list / ?ac=videolist / ?ac=detail&ids= / &wd= 搜索 / &t= 分类 / &pg= 分页），分类浏览、分页列表、详情（海报、简介、年份、地区、演员等）、搜索、播放地址解析（vod_play_from 用 $$$ 分线路，vod_play_url 用 # 分集、$ 分名称与地址，兼容各类不规范格式），线路/分集选择 UI。播放器在 M3 才实现：本轮点击分集时不得出现“无效按钮”——可以展示解析出的地址并提供“复制地址”/“用外部播放器打开”（Android ACTION_VIEW、桌面 Desktop.browse 或系统打开）这类真实可用的动作，或者先不显示播放按钮；不要做假播放器。
4. 来源导入 / 导出：JSON 格式（含 schema 版本），Android 用 SAF（CreateDocument/OpenDocument），桌面用文件对话框，TV 至少支持从 URL 导入或说明合理替代；导入做校验、去重、错误报告；导出文件不得包含任何密码/token（M2 的 Emby 凭证不导出）。
5. 首页 / 搜索页接入真实来源数据（有启用的 CMS 来源时显示分类与列表；无来源时保持真实空状态）。TV 端在本轮可先用横排列表，大海报横排在 M2 完善。
6. 更新 SPEC 对照矩阵中 M1 的状态。

## 规则
- 完整重编译并保持 allWarningsAsErrors：`./gradlew :androidApp:assembleDebug :desktopApp:compileKotlin --rerun-tasks --console=plain`，0 错误、0 Kotlin 警告；再执行桌面当前平台包装并在 macOS 上短时运行检查。
- 不写测试。只做短时运行检查：手机模拟器 fp_pixel、TV 模拟器 fp_tv、桌面。用完 `adb emu kill` 关闭模拟器，结束桌面进程。
- 应用里禁止任何假数据 / 演示数据 / 预置来源；禁止无效按钮。
- 当前没有任何用户来源，不要编造凭证或地址。运行检查用：空状态、表单校验、对不存在主机/非法地址的真实错误状态、导入导出往返（用你在 logs/ 下手写的仅含示例结构的文件，跑完后在应用里删掉该来源，不要留在数据库里）。若要验证 CMS 解析，只能用你在本机 logs/ 下启动的临时本地 HTTP 服务（例如 python -m http.server 提供手写的 JSON/XML 样例文件，样例内容用中性占位文本、不要引用任何真实影视资源站或盗版站点）；不要访问任何第三方影视资源站。跑完关停本地服务并清掉测试来源。其余通过代码审阅确认，并在报告中明确标注哪些是运行验证、哪些只是代码审阅。
- 绝不把 token / key / 密码写进代码、日志、提交或报告；TMDB key 只留在被忽略的 secrets.properties。
- 截图放 logs/m1/（手机、TV、桌面各至少一张：来源管理列表/空状态、添加表单、CMS 分类或详情页）。
- 完成后中文提交信息提交到 main，并写 .orchestrator/report-m1.md（提交号、各平台可用情况、运行验证 vs 仅代码审阅、阻塞项、截图路径），报告也一并提交。
