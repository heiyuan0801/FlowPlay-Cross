package app.flowplay.cross.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager

internal val LocalTelevision = staticCompositionLocalOf { false }

@Composable
internal fun remoteInputModifier(): Modifier {
    val focusManager = LocalFocusManager.current
    if (!LocalTelevision.current) return Modifier
    // The TV IME owns arrows while open. Once dismissed, vertical arrows navigate the form.
    return Modifier.onPreviewKeyEvent { event ->
        val direction = when (event.key) {
            Key.DirectionUp -> FocusDirection.Up
            Key.DirectionDown -> FocusDirection.Down
            else -> null
        }
        event.type == KeyEventType.KeyDown && direction != null && focusManager.moveFocus(direction)
    }
}
