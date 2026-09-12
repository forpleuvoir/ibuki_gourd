package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.ButtonTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.NumberFieldTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.RadioButtonTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.SliderTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.TextFieldTestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Slider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalContentColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.darkColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.lightColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ThemeType
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.systemTheme
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.toNebulaColor

var TestScreenLight by mutableStateOf(systemTheme().isLight)

var PixelScale by mutableStateOf(SokitsuThemeMeta.pixelScale)

@Composable
fun TestScreenTheme(content: @Composable () -> Unit) = SokitsuTheme(
    colorScheme = SokitsuThemeMeta.colorScheme(if (TestScreenLight) ThemeType.Light else ThemeType.Dark),
) {
    CompositionLocalProvider(LocalSokitsuPixelScale provides PixelScale) {
        content()
    }
}

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
            // 背景交给 Surface：铺 surface 色板并下发 onSurface 内容色
            // （暂无面板素材，走纯色填充；素材补齐后这里不用改）
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    Modifier.fillMaxSize()
                        .padding(top = 32.dp)
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button({ TestScreenLight = !TestScreenLight }) {
                            Text("主题:${if (TestScreenLight) "浅色" else "深色"}")
                        }

                        Text("内容色测试:${LocalContentColor.current.toNebulaColor().hexStr}")

                        Button({ PixelScale = if (PixelScale == 2) 3 else 2 }) {
                            Text("切换像素缩放${PixelScale}")
                        }
                    }
                    // 图层 alpha 传递验证：Modifier.alpha 应同时淡化精灵（按钮/滑条）与其中的文字
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        listOf(1f, 0.6f, 0.3f).forEach { a ->
                            Box(Modifier.alpha(a)) {
                                Button({}) {
                                    Text("alpha ${(a * 100).toInt()}%")
                                }
                            }
                        }
                        var alphaSlider by remember { mutableStateOf(0.6f) }
                        Box(Modifier.alpha(0.4f)) {
                            Slider(
                                value = alphaSlider,
                                onValueChange = { alphaSlider = it },
                                modifier = Modifier.width(160.dp),
                                label = { Text("40%") },
                            )
                        }
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
                        Button({
                            RadioButtonTestScreen()
                        }) {
                            Text("单选测试")
                        }
                        Button({
                            SliderTestScreen()
                        }) {
                            Text("滑条测试")
                        }
                        Button({
                            TextFieldTestScreen()
                        }) {
                            Text("文本框测试")
                        }
                        Button({
                            NumberFieldTestScreen()
                        }) {
                            Text("数字框测试")
                        }
                    }
                }
            }
        }
    }
}