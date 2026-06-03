package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.ui.configwrapper.UiWrapper

@Composable
fun ConfigTest() {
    Box {
        val scrollState = rememberScrollState()
        Column(Modifier.verticalScroll(scrollState)) {
            UiWrapper(TestConfig)
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(scrollState),
            style = defaultScrollbarStyle().copy(
                hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            )
        )
    }
}