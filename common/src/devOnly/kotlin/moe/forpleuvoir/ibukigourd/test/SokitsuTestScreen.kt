package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.ButtonTestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalContentColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.darkColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.lightColorScheme
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.toNebulaColor

var TestScreenLight by mutableStateOf(false)

@Composable
fun TestScreenTheme(content: @Composable () -> Unit) = SokitsuTheme(
    colorScheme = if (TestScreenLight) lightColorScheme() else darkColorScheme(),
    content = content
)

fun TestScreen(content: @Composable () -> Unit) {
    ComposeScreen.open(parent = mc.gui.screen()) {
        TestScreenTheme(content)
    }
}

@Composable
fun CenterBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center, content = content)
}

fun SokitsuTestScreen() {
    ComposeScreen.open {
        TestScreenTheme {
            Column(
                Modifier.fillMaxSize()
                    .background(SokitsuTheme.colorScheme.background.base)
                    .padding(top = 32.dp)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button({ TestScreenLight = !TestScreenLight }) {
                        Text("主题:${if (TestScreenLight) "浅色" else "深色"}")
                    }

                    Text("内容色测试:${LocalContentColor.current.toNebulaColor().hexStr}")
                }
                FlowRow(
                    Modifier.fillMaxSize().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Button({
                        ButtonTestScreen()
                    }) {
                        Text("按钮测试")
                    }
                }

            }
        }
    }
}