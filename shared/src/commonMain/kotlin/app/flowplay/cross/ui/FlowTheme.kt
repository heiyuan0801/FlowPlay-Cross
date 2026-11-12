package app.flowplay.cross.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF149187)
private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF2EE),
    onPrimaryContainer = Color(0xFF124B46),
    background = Color(0xFFF7F9F8),
    surface = Color(0xFFF7F9F8),
    surfaceContainer = Color.White,
    secondaryContainer = Color(0xFFE2EEEB),
    onSecondaryContainer = Color(0xFF124B46),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF6CD3C6),
    background = Color(0xFF131918),
    surface = Color(0xFF131918),
    surfaceContainer = Color(0xFF1D2624),
    secondaryContainer = Color(0xFF24453F),
)

@Composable
fun FlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
