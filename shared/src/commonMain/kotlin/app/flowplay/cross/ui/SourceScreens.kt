package app.flowplay.cross.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.flowplay.cross.network.CatalogHttp
import app.flowplay.cross.network.readableError
import app.flowplay.cross.provider.CmsProvider
import app.flowplay.cross.repository.SourceRepository
import app.flowplay.cross.repository.SourceTransfer
import app.flowplay.cross.storage.CmsConfig
import app.flowplay.cross.storage.SourceJson
import app.flowplay.cross.storage.SourceRecord
import app.flowplay.cross.storage.validateSource
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

@Composable
internal fun Action(label: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Button(onClick, enabled = enabled, modifier = modifier.onFocusChanged { focused = it.isFocused }
        .border(3.dp, if (focused) MaterialTheme.colorScheme.onSurface else Color.Transparent, RoundedCornerShape(24.dp))) {
        Text(label)
    }
}

@Composable
internal fun StatusText(text: String?) {
    if (text.isNullOrBlank()) return
    if (!LocalTelevision.current) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    var focused by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().heightIn(max = 160.dp).onFocusChanged { focused = it.isFocused }
        .border(2.dp, if (focused) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
        .onPreviewKeyEvent { event ->
            val offset = when (event.key) {
                Key.DirectionDown -> 100
                Key.DirectionUp -> -100
                else -> 0
            }
            val canScroll = (offset > 0 && scroll.canScrollForward) || (offset < 0 && scroll.canScrollBackward)
            if (event.type == KeyEventType.KeyDown && canScroll) {
                scope.launch { scroll.animateScrollTo((scroll.value + offset).coerceIn(0, scroll.maxValue)) }
                true
            } else false
        }.focusable().verticalScroll(scroll).padding(10.dp)) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun SourceListScreen(
    sources: List<SourceRecord>, repository: SourceRepository, http: CatalogHttp, actions: PlatformActions,
    onAdd: () -> Unit, onEdit: (SourceRecord) -> Unit, onImport: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<SourceRecord?>(null) }
    fun runAction(block: suspend () -> String) {
        scope.launch {
            busy = true
            status = try { block() } catch (error: Exception) { readableError(error) }
            busy = false
        }
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Action("新增来源", enabled = !busy, onClick = onAdd)
            Action("导入", enabled = !busy, onClick = onImport)
            Action("导出", enabled = !busy) { runAction {
                val result = SourceTransfer.encode(sources)
                if (actions.writeSourceDocument(result.content)) "已导出 ${result.included} 个，跳过 ${result.skipped} 个含查询参数或受保护类型的来源。"
                else "已取消导出。"
            } }
            Action("刷新列表", enabled = !busy) { runAction { repository.refresh(); "已重新读取本机来源。" } }
        }
        Text("导出包含公开接口与格式设置。请求头仅保存在本机。", style = MaterialTheme.typography.bodySmall)
        if (busy) CircularProgressIndicator()
        StatusText(status)
        if (sources.isEmpty()) {
            Text("还没有添加任何来源", style = MaterialTheme.typography.titleLarge)
            Text("添加 Apple CMS 的 JSON 或 XML 接口，或导入来源 JSON。")
        }
        sources.forEachIndexed { index, source ->
            Column(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(source.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Apple CMS · ${if (source.enabled) "已启用" else "已停用"}")
                Text(source.baseUrl, style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Action("编辑", enabled = !busy) { onEdit(source) }
                    Action(if (source.enabled) "停用" else "启用", enabled = !busy) {
                        runAction { repository.enable(source.id, !source.enabled); "来源状态已保存。" }
                    }
                    Action("连接测试", enabled = !busy) {
                        runAction { CmsProvider(http, source).testConnection() }
                    }
                    if (index > 0) Action("上移", enabled = !busy) { runAction { repository.move(source.id, -1); "排序已保存。" } }
                    if (index < sources.lastIndex) Action("下移", enabled = !busy) { runAction { repository.move(source.id, 1); "排序已保存。" } }
                    Action("删除", enabled = !busy) { deleting = source }
                }
            }
        }
    }
    deleting?.let { source ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("删除来源？") },
            text = { Text("将从本机移除“${source.name}”的来源配置。") },
            confirmButton = { Action("确认删除") {
                deleting = null
                runAction { repository.delete(source.id); "来源已删除。" }
            } }, dismissButton = { Action("取消") { deleting = null } })
    }
}

@Composable
internal fun SourceFormScreen(source: SourceRecord?, repository: SourceRepository, http: CatalogHttp, onSaved: () -> Unit) {
    var name by remember(source?.id) { mutableStateOf(source?.name.orEmpty()) }
    var endpoint by remember(source?.id) { mutableStateOf(source?.baseUrl.orEmpty()) }
    var configFormat by remember(source?.id) { mutableStateOf(source?.config()?.format ?: "auto") }
    var listAction by remember(source?.id) { mutableStateOf(source?.config()?.listAction ?: "videolist") }
    var headerText by remember(source?.id) { mutableStateOf(SourceJson.encodeToString(
        MapSerializer(String.serializer(), String.serializer()), source?.config()?.headers ?: emptyMap())) }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun config(): CmsConfig {
        val headers = try { SourceJson.decodeFromString<Map<String, String>>(headerText) }
            catch (_: Exception) { throw IllegalArgumentException("请求头需要 JSON 字符串对象。") }
        return CmsConfig(configFormat, headers, listAction)
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Apple CMS", style = MaterialTheme.typography.titleLarge)
        Text("填写接口的完整路径。支持 JSON / XML、分类、分页、搜索和多线路分集。")
        OutlinedTextField(name, { name = it }, label = { Text("来源名称") }, singleLine = true,
            enabled = !busy, modifier = Modifier.fillMaxWidth().then(remoteInputModifier()))
        OutlinedTextField(endpoint, { endpoint = it }, label = { Text("接口地址（http:// 或 https://）") }, singleLine = true,
            enabled = !busy, modifier = Modifier.fillMaxWidth().then(remoteInputModifier()))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("auto" to "自动识别", "json" to "JSON", "xml" to "XML").forEach { (value, label) ->
                Action(if (configFormat == value) "✓ $label" else label, enabled = !busy) { configFormat = value }
            }
        }
        Text("列表请求动作")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("videolist", "list", "detail").forEach { value ->
                Action(if (listAction == value) "✓ $value" else value, enabled = !busy) { listAction = value }
            }
        }
        OutlinedTextField(headerText, { headerText = it }, label = { Text("请求头 JSON（可选）") },
            enabled = !busy, modifier = Modifier.fillMaxWidth().then(remoteInputModifier()), minLines = 2)
        StatusText(status)
        if (busy) CircularProgressIndicator()
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Action("保存", enabled = !busy) {
                scope.launch {
                    busy = true
                    try {
                        repository.save(source?.id, name, endpoint, source?.enabled ?: true, config())
                        onSaved()
                    } catch (error: Exception) { status = readableError(error) }
                    finally { busy = false }
                }
            }
            Action("连接测试", enabled = !busy) {
                scope.launch {
                    busy = true
                    status = try {
                        val settings = config()
                        val (validName, validUrl) = validateSource(name, endpoint, settings)
                        CmsProvider(http, SourceRecord("draft", name = validName, baseUrl = validUrl,
                            createdAt = 0, updatedAt = 0, extraConfigJson = SourceJson.encodeToString(settings))).testConnection()
                    } catch (error: Exception) { readableError(error) }
                    busy = false
                }
            }
        }
    }
}

@Composable
internal fun ImportSourcesScreen(repository: SourceRepository, http: CatalogHttp, actions: PlatformActions) {
    var url by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun importDocument(load: suspend () -> String?) {
        scope.launch {
            busy = true
            status = try { load()?.let { repository.importSources(it) } ?: "已取消导入。" }
                catch (error: Exception) { readableError(error) }
            busy = false
        }
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("导入来源 JSON", style = MaterialTheme.typography.titleLarge)
        Text("schemaVersion 为 1，sources 数组包含 type、name、baseUrl、enabled 和 config。按类型与规范化地址去重，已有配置保持原值。")
        Action("选择 JSON 文件", enabled = !busy) { importDocument { actions.readSourceDocument() } }
        Text("电视可输入局域网 HTTP / HTTPS 地址导入。文件选择器是否可用由设备系统决定。")
        OutlinedTextField(url, { url = it }, label = { Text("JSON 文件 URL") }, singleLine = true,
            enabled = !busy, modifier = Modifier.fillMaxWidth().then(remoteInputModifier()))
        Action("从 URL 导入", enabled = !busy) { importDocument { http.importDocument(url) } }
        if (busy) CircularProgressIndicator()
        StatusText(status)
    }
}
