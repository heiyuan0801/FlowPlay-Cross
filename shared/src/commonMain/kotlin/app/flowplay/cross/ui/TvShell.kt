package app.flowplay.cross.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flowplay.cross.resources.Res
import app.flowplay.cross.resources.flowplay_icon
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TvShell(selected: Tab, page: String?, onSelect: (Tab) -> Unit, onBack: () -> Unit,
    content: @Composable () -> Unit) {
    val navigation = remember { Tab.entries.associateWith { FocusRequester() } }
    val contentFocus = remember { FocusRequester() }
    var contentHasFocus by remember { mutableStateOf(false) }
    LaunchedEffect(selected, page) {
        if (page == null) navigation.getValue(selected).requestFocus() else contentFocus.requestFocus()
    }
    BackHandler(enabled = page == null && contentHasFocus) { navigation.getValue(selected).requestFocus() }
    Surface(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
            Column(Modifier.width(172.dp).fillMaxSize().focusGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.padding(start = 12.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(Res.drawable.flowplay_icon), null, Modifier.size(38.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("FlowPlay", fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                }
                Tab.entries.forEachIndexed { index, tab ->
                    TvNavigation(tab, selected == tab, Modifier.fillMaxWidth().focusRequester(navigation.getValue(tab))
                        .focusProperties {
                            up = Tab.entries.getOrNull(index - 1)?.let(navigation::getValue) ?: FocusRequester.Cancel
                            down = Tab.entries.getOrNull(index + 1)?.let(navigation::getValue) ?: FocusRequester.Cancel
                            left = FocusRequester.Cancel
                            right = contentFocus
                        }) { onSelect(tab) }
                }
            }
            Spacer(Modifier.width(24.dp))
            Column(Modifier.weight(1f).fillMaxSize().onFocusChanged { contentHasFocus = it.hasFocus }
                .focusProperties {
                    onExit = {
                        if (requestedFocusDirection == FocusDirection.Left) navigation.getValue(selected).requestFocus()
                    }
                }.focusRequester(contentFocus).focusGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(page ?: selected.label, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                key(selected, page) {
                    if (page != null) Action("返回", onClick = onBack)
                    content()
                }
            }
        }
    }
}

@Composable
private fun TvNavigation(tab: Tab, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    val foreground = if (focused) colors.onPrimary else colors.onSurface
    Row(modifier.onFocusChanged { focused = it.isFocused }.semantics { this.selected = selected }
        .border(3.dp, if (focused) colors.onSurface else Color.Transparent, shape).clip(shape)
        .background(if (focused) colors.primary else if (selected) colors.secondaryContainer else colors.surfaceContainer)
        .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(tab.icon, null, tint = foreground)
        Text(tab.label, color = foreground, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
    }
}
