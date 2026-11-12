package app.flowplay.cross.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.flowplay.cross.network.CatalogHttp
import app.flowplay.cross.network.readableError
import app.flowplay.cross.provider.CmsCategory
import app.flowplay.cross.provider.CmsEpisode
import app.flowplay.cross.provider.CmsProvider
import app.flowplay.cross.provider.CmsVideo
import app.flowplay.cross.provider.resolveCmsAddress
import app.flowplay.cross.storage.SourceRecord
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

@Composable
internal fun CatalogScreen(sources: List<SourceRecord>, http: CatalogHttp, search: Boolean,
    onManage: () -> Unit, onDetail: (SourceRecord, CmsVideo) -> Unit) {
    val enabled = sources.filter { it.enabled && it.type == "cms" }
    var sourceId by remember { mutableStateOf<String?>(null) }
    var input by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var searchRevision by remember { mutableStateOf(0) }
    val semaphore = remember { Semaphore(4) }
    if (enabled.isEmpty()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (sources.isEmpty()) "还没有添加任何来源" else "还没有启用的 CMS 来源", style = MaterialTheme.typography.titleLarge)
            Text("添加并启用 Apple CMS 来源后，可浏览分类与搜索。")
            Action("来源管理", onClick = onManage)
        }
        return
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (search) {
            OutlinedTextField(input, { input = it }, singleLine = true, label = { Text("搜索启用的 CMS 来源") },
                modifier = Modifier.fillMaxWidth().then(remoteInputModifier()))
            Action("搜索", enabled = input.isNotBlank()) { query = input.trim(); searchRevision++ }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(enabled, key = { it.id }) { source ->
                    val selected = (sourceId ?: enabled.first().id) == source.id
                    Action(if (selected) "✓ ${source.name}" else source.name) { sourceId = source.id }
                }
            }
        }
        if (search && query.isBlank()) {
            Text("输入关键词后搜索。")
        } else if (search) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(enabled, key = { it.id }) { source ->
                    key(source.updatedAt, query, searchRevision) {
                        SourceCatalog(source, http, query, semaphore, onDetail)
                    }
                }
            }
        } else {
            val source = enabled.firstOrNull { it.id == sourceId } ?: enabled.first()
            key(source.id, source.updatedAt) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    SourceCatalog(source, http, "", semaphore, onDetail)
                }
            }
        }
    }
}

@Composable
private fun SourceCatalog(source: SourceRecord, http: CatalogHttp, query: String, semaphore: Semaphore,
    onDetail: (SourceRecord, CmsVideo) -> Unit) {
    val provider = remember(source) { CmsProvider(http, source) }
    var categories by remember { mutableStateOf<List<CmsCategory>>(emptyList()) }
    var category by remember { mutableStateOf<String?>(null) }
    var videos by remember { mutableStateOf<List<CmsVideo>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var requestedPage by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(source, category, query, refresh, requestedPage) {
        loading = true
        error = null
        try {
            semaphore.withPermit {
                if (query.isBlank() && categories.isEmpty()) categories = provider.categories().categories
                val response = provider.items(category, requestedPage, query)
                videos = if (requestedPage == 1) response.videos else (videos + response.videos).distinctBy { it.id }
                page = requestedPage
                hasMore = response.hasNext(requestedPage)
            }
        } catch (failure: Exception) { error = readableError(failure) }
        finally { loading = false }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(source.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        if (query.isBlank()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Action(if (category == null) "✓ 全部" else "全部") { category = null; requestedPage = 1; videos = emptyList(); refresh++ } }
                items(categories, key = { it.id }) { item ->
                    Action(if (category == item.id) "✓ ${item.name}" else item.name) { category = item.id; requestedPage = 1; videos = emptyList(); refresh++ }
                }
            }
        }
        if (loading) CircularProgressIndicator(Modifier.size(28.dp))
        StatusText(error)
        if (!loading && error == null && videos.isEmpty()) Text("此来源未返回条目。")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(videos, key = { it.id }) { video ->
                Column(Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (video.poster.isNotBlank()) Poster(video.poster, Modifier.size(110.dp, 145.dp))
                    Action(video.title) { onDetail(source, video) }
                    if (video.year.isNotBlank()) Text(video.year, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Action("刷新", enabled = !loading) { requestedPage = 1; refresh++ }
            if (hasMore) Action("加载下一页", enabled = !loading) {
                requestedPage = page + 1
                refresh++
            }
        }
    }
}

@Composable
private fun Poster(url: String, modifier: Modifier) {
    val context = LocalPlatformContext.current
    AsyncImage(model = ImageRequest.Builder(context).data(url).diskCachePolicy(CachePolicy.DISABLED).build(),
        contentDescription = "海报", modifier = modifier)
}

@Composable
internal fun CmsDetailScreen(source: SourceRecord, initial: CmsVideo, http: CatalogHttp, actions: PlatformActions) {
    var video by remember { mutableStateOf<CmsVideo?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    var lineIndex by remember { mutableStateOf(0) }
    var episode by remember { mutableStateOf<CmsEpisode?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(source, initial.id, refresh) {
        error = null
        try { video = CmsProvider(http, source).detail(initial.id) }
        catch (failure: Exception) { error = readableError(failure) }
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(video?.title ?: initial.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (video == null && error == null) CircularProgressIndicator()
        error?.let {
            StatusText(it)
            Action("重试") { refresh++ }
        }
        video?.let { detail ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (detail.poster.isNotBlank()) Poster(detail.poster, Modifier.height(180.dp).size(125.dp, 180.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(detail.category, detail.year, detail.area, detail.remarks).filter(String::isNotBlank).forEach { Text(it) }
                }
            }
            if (detail.actors.isNotBlank()) Text("演员　${detail.actors}")
            if (detail.director.isNotBlank()) Text("导演　${detail.director}")
            if (detail.overview.isNotBlank()) Text(detail.overview)
            if (detail.lines.isEmpty()) Text("此条目未提供可解析的分集地址。")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(detail.lines.size) { index ->
                    val line = detail.lines[index]
                    Action(if (index == lineIndex) "✓ ${line.name}" else line.name) { lineIndex = index; episode = null }
                }
            }
            detail.lines.getOrNull(lineIndex)?.let { line ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    line.episodes.forEach { item -> Action(item.name) { episode = item; status = null } }
                }
            }
            episode?.let { selected ->
                val address = resolveCmsAddress(source.baseUrl, selected.address)
                Text(selected.name, style = MaterialTheme.typography.titleMedium)
                Text(address)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Action("复制地址") {
                        status = try { actions.copyAddress(address); "地址已复制。" } catch (failure: Exception) { readableError(failure) }
                    }
                    if (address.startsWith("http://", true) || address.startsWith("https://", true) || address.startsWith("rtsp://", true)) {
                        Action("用外部播放器打开") {
                            status = try { actions.openExternal(address); "已交给系统打开。" }
                                catch (failure: Exception) { readableError(failure) }
                        }
                    }
                }
                if (source.config().headers.isNotEmpty()) Text("外部播放器需自行配置此来源所需的请求头。")
                StatusText(status)
            }
        }
    }
}
