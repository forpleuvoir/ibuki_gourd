package moe.forpleuvoir.ibukigourd.ui.preset.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.TextRange

@Composable
fun rememberTextFieldState(
    initialText: String,
    initialSelection: TextRange = TextRange(initialText.length),
    placeCursorAtEnd: Boolean = true,
    onValueChange: (String) -> Unit
): TextFieldState {
    val state = rememberTextFieldState(initialText, initialSelection)
    LaunchedEffect(initialText) {
        if (state.text.toString() != initialText) {
            state.edit {
                replace(0, length, initialText)
                if (placeCursorAtEnd) placeCursorAtEnd()
            }
        }
    }
    LaunchedEffect(state.text) {
        val text = state.text.toString()
        if (text != initialText) onValueChange(text)
    }
    return state
}