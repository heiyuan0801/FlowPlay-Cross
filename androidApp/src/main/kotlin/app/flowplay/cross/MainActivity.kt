package app.flowplay.cross

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.flowplay.cross.ui.FlowPlayApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var feedbackError by remember { mutableStateOf<String?>(null) }
            FlowPlayApp(
                desktop = false,
                version = BuildConfig.VERSION_NAME,
                feedbackError = feedbackError,
                onFeedback = {
                    val mail = Uri.parse("mailto:heiyuan6@gmail.com?subject=FlowPlay%20Android%20反馈" +
                        "&body=" + Uri.encode("FlowPlay ${BuildConfig.VERSION_NAME}\nAndroid\n\n"))
                    try {
                        startActivity(Intent(Intent.ACTION_SENDTO, mail))
                        feedbackError = null
                    } catch (_: ActivityNotFoundException) {
                        feedbackError = "未找到邮件应用，请发送邮件至 heiyuan6@gmail.com。"
                    }
                },
            )
        }
    }
}
