package app.flowplay.cross

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.flowplay.cross.ui.FlowPlayApp
import app.flowplay.cross.resources.Res
import app.flowplay.cross.resources.flowplay_icon
import java.awt.Desktop
import java.net.URI
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "FlowPlay",
        state = rememberWindowState(width = 1100.dp, height = 760.dp),
        icon = painterResource(Res.drawable.flowplay_icon),
    ) {
        var feedbackError by remember { mutableStateOf<String?>(null) }
        FlowPlayApp(
            desktop = true,
            version = "1.0.0",
            feedbackError = feedbackError,
            onFeedback = {
                try {
                    Desktop.getDesktop().mail(URI("mailto:heiyuan6@gmail.com?subject=FlowPlay%20Desktop%20Feedback&body=FlowPlay%201.0.0%0ADesktop%0A"))
                    feedbackError = null
                } catch (_: Exception) {
                    feedbackError = "无法打开邮件应用，请发送邮件至 heiyuan6@gmail.com。"
                }
            },
        )
    }
}
