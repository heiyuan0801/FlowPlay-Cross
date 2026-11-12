package app.flowplay.cross.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.flowplay.cross.resources.Res
import app.flowplay.cross.resources.flowplay_icon
import org.jetbrains.compose.resources.painterResource

private enum class Tab(val label: String, val icon: ImageVector) {
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
    onFeedback: () -> Unit,
    feedbackError: String? = null,
) {
    var selected by rememberSaveable { mutableStateOf(Tab.Home) }
    var page by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(enabled = page != null) { page = null }
    FlowTheme {
        Surface(Modifier.fillMaxSize()) {
            Row {
                if (desktop) {
                    Sidebar(selected) { selected = it; page = null }
                }
                Scaffold(
                    bottomBar = {
                        if (!desktop && page == null) {
                            NavigationBar {
                                Tab.entries.forEach { tab ->
                                    NavigationBarItem(
                                        selected = selected == tab,
                                        onClick = { selected = tab },
                                        icon = { Icon(tab.icon, contentDescription = null) },
                                        label = { Text(tab.label) },
                                    )
                                }
                            }
                        }
                    },
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        Header(page ?: selected.label, page != null, desktop) { page = null }
                        when (page) {
                            "添加来源" -> AddSourcePage()
                            "关于 FlowPlay" -> AboutPage(version, onFeedback, feedbackError)
                            "隐私说明" -> LegalPage("隐私说明", PrivacyText)
                            "使用说明" -> LegalPage("使用说明", UsageText)
                            else -> when (selected) {
                                Tab.Home -> EmptyPage(
                                    "还没有添加任何来源",
                                    "连接自己的媒体，建立你的私人媒体库。",
                                    Icons.Default.Home,
                                    "添加来源",
                                ) { page = "添加来源" }
                                Tab.Search -> EmptyPage(
                                    "还没有可搜索的媒体",
                                    "添加来源后，可在同一处搜索多个媒体库。",
                                    Icons.Default.Search,
                                    "添加来源",
                                ) { page = "添加来源" }
                                Tab.Live -> EmptyPage(
                                    "还没有直播频道",
                                    "可添加你自己的 M3U、TXT 列表或 TVBox 配置。",
                                    Icons.Default.PlayArrow,
                                    "添加来源",
                                ) { page = "添加来源" }
                                Tab.Downloads -> EmptyPage(
                                    "还没有离线下载",
                                    "已完成的下载将显示在这里。",
                                    Icons.AutoMirrored.Filled.List,
                                )
                                Tab.Mine -> MinePage(version) { page = it }
                            }
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
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(22.dp)) {
                Icon(icon, null, Modifier.padding(20.dp).size(36.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (action != null) {
                Button(onClick = onAction, contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(action)
                }
            }
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
private fun AddSourcePage() {
    ContentColumn {
        Text("来源接入准备中", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text("当前为 1.0.0 工程预览。下一阶段将开放来源配置与导入。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                SourceDescription("Emby", "连接服务器，浏览媒体库与同步播放进度")
                SourceDescription("Apple CMS", "JSON / XML 接口、多线路与分集")
                SourceDescription("TVBox", "配置导入、JS 爬虫与直播")
                SourceDescription("直播列表", "M3U / TXT 频道列表")
                SourceDescription("本地文件", "导入本机视频与字幕")
            }
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
    ContentColumn {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

private const val PrivacyText = "当前工程预览没有媒体来源和账号数据，也没有分析或自动上报。后续添加的来源、观看记录和下载将保存在本机。意见反馈通过系统邮件程序打开，你可以编辑、取消或自行发送。预填内容只包含应用版本与平台。开发者 jiesen，联系邮箱 heiyuan6@gmail.com。"
private const val UsageText = "FlowPlay 是个人媒体中心。媒体来自用户添加的服务器、CMS、TVBox、直播列表或本地文件。请确保拥有访问、下载及投屏所需的授权。TMDB 提供海报、详情、剧集资料与热门榜单，具体播放来自用户自己的媒体来源。第三方服务及开源组件遵循各自许可。"
