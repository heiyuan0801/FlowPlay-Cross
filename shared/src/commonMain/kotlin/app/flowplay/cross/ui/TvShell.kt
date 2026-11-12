package app.flowplay.cross.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flowplay.cross.resources.Res
import app.flowplay.cross.resources.flowplay_icon
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TvShell(
    selected: Tab,
    page: String?,
    version: String,
    onFeedback: () -> Unit,
    feedbackError: String?,
    onSelect: (Tab) -> Unit,
    onPage: (String) -> Unit,
    onBack: () -> Unit,
) {
    val navigation = remember { Tab.entries.associateWith { FocusRequester() } }
    val contentFocus = remember { FocusRequester() }
    var contentHasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(selected, page) {
        if (page == null) navigation.getValue(selected).requestFocus() else contentFocus.requestFocus()
    }
    BackHandler(enabled = page == null && contentHasFocus) {
        navigation.getValue(selected).requestFocus()
    }

    Surface(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
            Column(
                Modifier.width(184.dp).fillMaxSize().focusGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    Modifier.padding(start = 12.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painterResource(Res.drawable.flowplay_icon), null, Modifier.size(40.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("FlowPlay", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                }
                Tab.entries.forEachIndexed { index, tab ->
                    TvAction(
                        label = tab.label,
                        icon = tab.icon,
                        selected = selected == tab,
                        modifier = Modifier.fillMaxWidth().focusRequester(navigation.getValue(tab))
                            .focusProperties {
                                up = Tab.entries.getOrNull(index - 1)?.let(navigation::getValue) ?: FocusRequester.Cancel
                                down = Tab.entries.getOrNull(index + 1)?.let(navigation::getValue) ?: FocusRequester.Cancel
                                left = FocusRequester.Cancel
                                right = contentFocus
                            },
                        onClick = { onSelect(tab) },
                    )
                }
            }
            Spacer(Modifier.width(32.dp))
            Column(
                Modifier.weight(1f).fillMaxSize().onFocusChanged { contentHasFocus = it.hasFocus }
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionLeft) {
                            navigation.getValue(selected).requestFocus()
                            true
                        } else {
                            false
                        }
                    }.focusGroup(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(page ?: selected.label, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                key(selected, page) {
                    if (page != null) {
                        TvAction("返回", Modifier.focusRequester(contentFocus), Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = onBack)
                    }
                    when (page) {
                        "添加来源" -> TvReadingPanel(Modifier.weight(1f)) {
                            Text("来源接入准备中", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                            TvBody("当前为 $version 工程预览。下一阶段将开放来源配置与导入。")
                            SourceDescriptions.forEach { (title, description) ->
                                Text(title, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                                TvBody(description)
                            }
                        }
                        "关于 FlowPlay" -> {
                            TvReadingPanel(Modifier.weight(1f)) {
                                Text("FlowPlay · $version", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                                TvBody("开发者 jiesen\n联系邮箱 heiyuan6@gmail.com")
                                TvBody("This product uses the TMDB API but is not endorsed or certified by TMDB.")
                            }
                            TvAction("意见反馈", onClick = onFeedback)
                            if (feedbackError != null) {
                                Text(feedbackError, fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        "隐私说明" -> TvReadingPanel(Modifier.weight(1f)) { TvBody(PrivacyText) }
                        "使用说明" -> TvReadingPanel(Modifier.weight(1f)) { TvBody(UsageText) }
                        else -> when (selected) {
                            Tab.Home -> TvEmptyState(
                                "还没有添加任何来源", "连接自己的媒体，建立你的私人媒体库。",
                                contentFocus, onAddSource = { onPage("添加来源") },
                            )
                            Tab.Search -> TvEmptyState(
                                "还没有可搜索的媒体", "添加来源后，可在同一处搜索多个媒体库。",
                                contentFocus, onAddSource = { onPage("添加来源") },
                            )
                            Tab.Live -> TvEmptyState(
                                "还没有直播频道", "可添加你自己的 M3U、TXT 列表或 TVBox 配置。",
                                contentFocus, onAddSource = { onPage("添加来源") },
                            )
                            Tab.Downloads -> TvEmptyState(
                                "还没有离线下载", "已完成的下载将显示在这里。", contentFocus,
                            )
                            Tab.Mine -> {
                                TvAction("关于 FlowPlay", Modifier.fillMaxWidth().focusRequester(contentFocus),
                                    subtitle = "版本 $version · jiesen", onClick = { onPage("关于 FlowPlay") })
                                TvAction("隐私说明", Modifier.fillMaxWidth(),
                                    subtitle = "本机数据与主动反馈", onClick = { onPage("隐私说明") })
                                TvAction("使用说明", Modifier.fillMaxWidth(),
                                    subtitle = "自有来源与第三方资料", onClick = { onPage("使用说明") })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvAction(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selected: Boolean = false,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val background = when {
        focused -> colors.primary
        selected -> colors.secondaryContainer
        else -> colors.surfaceContainer
    }
    val foreground = if (focused) colors.onPrimary else colors.onSurface
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier.onFocusChanged { focused = it.isFocused }.semantics { this.selected = selected }
            .border(2.dp, if (focused) colors.onSurface else Color.Transparent, shape)
            .clip(shape).background(background).clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (icon != null) Icon(icon, null, Modifier.size(24.dp), tint = foreground)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = foreground, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, color = foreground, fontSize = 18.sp)
        }
    }
}

@Composable
private fun TvEmptyState(
    title: String,
    description: String,
    contentFocus: FocusRequester,
    onAddSource: (() -> Unit)? = null,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // A focusable reading panel gives an empty downloads page a remote-accessible content target.
            if (onAddSource == null) {
                TvReadingPanel(Modifier.fillMaxWidth().focusRequester(contentFocus)) {
                    Text(title, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                    TvBody(description)
                }
            } else {
                Text(title, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                TvBody(description)
                Spacer(Modifier.height(4.dp))
                TvAction("添加来源", Modifier.focusRequester(contentFocus), Icons.Default.Add, onClick = onAddSource)
            }
        }
    }
}

@Composable
private fun TvReadingPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            .border(2.dp, if (focused) MaterialTheme.colorScheme.primary else Color.Transparent, shape)
            .clip(shape).background(MaterialTheme.colorScheme.surfaceContainer)
            .onPreviewKeyEvent { event ->
                val offset = when (event.key) {
                    Key.DirectionDown -> 160f
                    Key.DirectionUp -> -160f
                    else -> 0f
                }
                val canScroll = (offset > 0 && scroll.canScrollForward) || (offset < 0 && scroll.canScrollBackward)
                if (event.type == KeyEventType.KeyDown && canScroll) {
                    scope.launch { scroll.animateScrollTo((scroll.value + offset.toInt()).coerceIn(0, scroll.maxValue)) }
                    true
                } else {
                    false
                }
            }.focusable().verticalScroll(scroll).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) { content() }
}

@Composable
private fun TvBody(text: String) {
    Text(text, fontSize = 20.sp, lineHeight = 30.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
