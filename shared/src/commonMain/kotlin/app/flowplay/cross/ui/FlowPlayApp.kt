package app.flowplay.cross.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.flowplay.cross.resources.Res
import app.flowplay.cross.resources.flowplay_icon
import org.jetbrains.compose.resources.painterResource

internal enum class Tab(val label: String, val icon: ImageVector) {
    Home("首页", Icons.Default.Home),
    Search("搜索", Icons.Default.Search),
    Live("直播", Icons.Default.PlayArrow),
    Downloads("下载", Icons.AutoMirrored.Filled.List),
    Mine("我的", Icons.Default.Person),
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FlowPlayApp(
    desktop: Boolean,
    version: String,
    repository: app.flowplay.cross.repository.SourceRepository,
    http: app.flowplay.cross.network.CatalogHttp,
    actions: PlatformActions,
    onFeedback: () -> Unit,
    feedbackError: String? = null,
    television: Boolean = false,
) {
    val sources by repository.sources.collectAsState()
    var selected by rememberSaveable { mutableStateOf(Tab.Home) }
    var page by rememberSaveable { mutableStateOf<String?>(null) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    val editing = sources.firstOrNull { it.id == editingId }
    var detailSourceId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailVideoId by rememberSaveable { mutableStateOf("") }
    var detailTitle by rememberSaveable { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableStateOf(0) }
    LaunchedEffect(repository, reload) {
        try { repository.refresh(); loaded = true; loadError = null }
        catch (error: Exception) { loadError = app.flowplay.cross.network.readableError(error) }
    }
    fun back() {
        page = when (page) {
            "新增来源", "编辑来源", "导入来源" -> "来源管理"
            else -> null
        }
    }
    BackHandler(enabled = page != null) { back() }
    val content: @Composable () -> Unit = {
        when {
            loadError != null -> Column(Modifier.padding(24.dp)) {
                Text("读取本机来源失败")
                StatusText(loadError)
                Action("重试") { reload++ }
            }
            !loaded -> androidx.compose.material3.CircularProgressIndicator(Modifier.padding(24.dp))
            else -> when (page) {
                "来源管理" -> SourceListScreen(sources, repository, http, actions,
                    onAdd = { editingId = null; page = "新增来源" },
                    onEdit = { editingId = it.id; page = "编辑来源" }, onImport = { page = "导入来源" })
                "新增来源", "编辑来源" -> SourceFormScreen(editing, repository, http) { page = "来源管理" }
                "导入来源" -> ImportSourcesScreen(repository, http, actions)
                "媒体详情" -> sources.firstOrNull { it.id == detailSourceId }?.let { source ->
                    CmsDetailScreen(source, app.flowplay.cross.provider.CmsVideo(detailVideoId, detailTitle), http, actions)
                }
                "关于 FlowPlay" -> AboutPage(version, onFeedback, feedbackError)
                "隐私说明" -> LegalPage("隐私说明", PrivacyText)
                "使用说明" -> LegalPage("使用说明", UsageText)
                else -> when (selected) {
                    Tab.Home, Tab.Search -> CatalogScreen(sources, http, selected == Tab.Search,
                        onManage = { page = "来源管理" }, onDetail = { source, video ->
                            detailSourceId = source.id
                            detailVideoId = video.id
                            detailTitle = video.title
                            page = "媒体详情"
                        })
                    Tab.Live -> EmptyPage("还没有直播频道", "直播列表接入将在后续阶段开放。", Icons.Default.PlayArrow)
                    Tab.Downloads -> EmptyPage("还没有离线下载", "已完成的下载将显示在这里。", Icons.AutoMirrored.Filled.List)
                    Tab.Mine -> MinePage(version) { page = it }
                }
            }
        }
    }
    FlowTheme(forceDark = television) {
        if (television) {
            CompositionLocalProvider(LocalTelevision provides true) {
                TvShell(selected, page, onSelect = { selected = it; page = null }, onBack = { back() }, content = content)
            }
        } else {
            Surface(Modifier.fillMaxSize()) {
                Row {
                    if (desktop) Sidebar(selected) { selected = it; page = null }
                    Scaffold(bottomBar = {
                        if (!desktop && page == null) NavigationBar {
                            Tab.entries.forEach { tab -> NavigationBarItem(selected == tab,
                                onClick = { selected = tab }, icon = { Icon(tab.icon, null) }, label = { Text(tab.label) }) }
                        }
                    }) { padding ->
                        Column(Modifier.fillMaxSize().padding(padding)) {
                            Header(page ?: selected.label, page != null, desktop) { back() }
                            content()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Sidebar(selected: Tab, onSelect: (Tab) -> Unit) {
    Column(
        Modifier.width(220.dp).fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Image(painterResource(Res.drawable.flowplay_icon), null, Modifier.size(38.dp))
            Spacer(Modifier.width(10.dp))
            Text("FlowPlay", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(28.dp))
        Tab.entries.forEach { tab ->
            NavigationDrawerItem(
                label = { Text(tab.label) },
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 3.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Text("个人媒体中心", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun Header(title: String, hasBack: Boolean, desktop: Boolean, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = if (desktop) 36.dp else 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasBack) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
            Spacer(Modifier.width(6.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyPage(
    title: String,
    description: String,
    icon: ImageVector,
) {
    Box(Modifier.fillMaxSize().padding(24.dp).focusable(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(22.dp)) {
                Icon(icon, null, Modifier.padding(20.dp).size(36.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

        }
    }
}

@Composable
private fun ContentColumn(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 680.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SourceDescription(title: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MinePage(version: String, onPage: (String) -> Unit) {
    ContentColumn {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(12.dp)) {
                PageLink("来源管理", "添加、导入与管理 Apple CMS") { onPage("来源管理") }
                HorizontalDivider(Modifier.padding(horizontal = 12.dp))
                PageLink("关于 FlowPlay", "版本 $version · jiesen") { onPage("关于 FlowPlay") }
                HorizontalDivider(Modifier.padding(horizontal = 12.dp))
                PageLink("隐私说明", "本机数据与主动反馈") { onPage("隐私说明") }
                HorizontalDivider(Modifier.padding(horizontal = 12.dp))
                PageLink("使用说明", "自有来源与第三方资料") { onPage("使用说明") }
            }
        }
    }
}

@Composable
private fun PageLink(title: String, subtitle: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null)
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AboutPage(version: String, onFeedback: () -> Unit, error: String?) {
    ContentColumn {
        Image(painterResource(Res.drawable.flowplay_icon), "FlowPlay 图标", Modifier.size(100.dp))
        Text("FlowPlay", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("版本 $version")
        SourceDescription("开发者", "jiesen")
        SourceDescription("联系邮箱", "heiyuan6@gmail.com")
        Button(onClick = onFeedback) { Text("意见反馈") }
        if (error != null) { Text(error, color = MaterialTheme.colorScheme.error) }
        HorizontalDivider()
        Text("This product uses the TMDB API but is not endorsed or certified by TMDB.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LegalPage(title: String, text: String) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().onPreviewKeyEvent { event ->
        val offset = when (event.key) {
            Key.DirectionDown -> 140
            Key.DirectionUp -> -140
            else -> 0
        }
        if (event.type == KeyEventType.KeyDown && offset != 0) {
            scope.launch { scroll.animateScrollTo((scroll.value + offset).coerceIn(0, scroll.maxValue)) }
            true
        } else false
    }.focusable().verticalScroll(scroll).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

internal const val PrivacyText = "来源配置保存在本机数据库。应用按你的操作请求已添加的媒体接口，没有分析或自动上报。来源导出只包含公开接口与格式设置。意见反馈通过系统邮件程序打开，你可以编辑、取消或自行发送。预填内容只包含应用版本与平台。开发者 jiesen，联系邮箱 heiyuan6@gmail.com。"
internal const val UsageText = "FlowPlay 是个人媒体中心。媒体来自用户添加的服务器、CMS、TVBox、直播列表或本地文件。请确保拥有访问、下载及投屏所需的授权。TMDB 提供海报、详情、剧集资料与热门榜单，具体播放来自用户自己的媒体来源。第三方服务及开源组件遵循各自许可。"
