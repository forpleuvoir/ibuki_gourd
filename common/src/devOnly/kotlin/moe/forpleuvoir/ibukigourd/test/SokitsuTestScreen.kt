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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.platform.screen.ComposeScreen
import moe.forpleuvoir.compose_minecraft.platform.ui.popup.LocalPopupHost
import moe.forpleuvoir.compose_minecraft.platform.ui.popup.PopupHostOverlay
import moe.forpleuvoir.compose_minecraft.platform.ui.popup.PopupHostState
import moe.forpleuvoir.ibukigourd.test.sokitsu.AlertDialogTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.AtlasTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.ButtonTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.ColorPickerTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.ColorSchemeTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.DividerTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.DropdownMenuTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.FlatButtonTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.IconButtonTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.IconTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.NumberFieldTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.ProgressBarTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.RadioButtonTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.ReorderableTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.SliderTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.SelectorTriggerTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.SelectorTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.TextFieldTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.ToastTestScreen
import moe.forpleuvoir.ibukigourd.test.sokitsu.TooltipTestScreen
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
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.systemTheme
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.toNebulaColor

var TestScreenLight by mutableStateOf(systemTheme().isLight)

/**
 * **所有测试屏幕共用**的浅色配色（测试屏专用，与资源包里的主题无关）。
 *
 * 在「配色」测试屏里用取色按钮改它，改动会立即反映到**每一个**测试屏幕
 * （[TestScreenTheme] 取的就是这两份）；默认值由 [lightColorScheme] 提供，可在那里重置。
 */
var TestLightScheme by mutableStateOf(lightColorScheme())

/**
 * **所有测试屏幕共用**的深色配色，语义同 [TestLightScheme]。
 */
var TestDarkScheme by mutableStateOf(darkColorScheme())

var PixelScale by mutableStateOf(SokitsuThemeMeta.pixelScale)

@Composable
fun TestScreenTheme(content: @Composable () -> Unit) {
    val popupHost = remember { PopupHostState() }
    SokitsuTheme(
        // 用测试屏自己的两份配色（而非 meta 里的）——这样在配色测试屏里调色能立刻看到全局效果
        colorScheme = if (TestScreenLight) TestLightScheme else TestDarkScheme,
    ) {
        // 场景根弹层：Tooltip 气泡经 LocalPopupHost.register 注册，由 PopupHostOverlay 统一渲染
        CompositionLocalProvider(
            LocalSokitsuPixelScale provides PixelScale,
            LocalPopupHost provides popupHost,
        ) {
            content()
            PopupHostOverlay()
        }
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
                            Box(Modifier.graphicsLayer {
                                alpha = a
                            }) {
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
                            ColorSchemeTestScreen()
                        }) {
                            Text("配色测试")
                        }
                        Button({
                            ButtonTestScreen()
                        }) {
                            Text("按钮测试")
                        }
                        Button({
                            FlatButtonTestScreen()
                        }) {
                            Text("扁平按钮测试")
                        }
                        Button({
                            IconTestScreen()
                        }) {
                            Text("图标总览")
                        }
                        Button({
                            IconButtonTestScreen()
                        }) {
                            Text("图标按钮测试")
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
                        Button({
                            TooltipTestScreen()
                        }) {
                            Text("气泡测试")
                        }
                        Button({
                            AlertDialogTestScreen()
                        }) {
                            Text("弹窗测试")
                        }
                        Button({
                            DropdownMenuTestScreen()
                        }) {
                            Text("下拉菜单测试")
                        }
                        Button({
                            ToastTestScreen()
                        }) {
                            Text("提示测试")
                        }
                        Button({
                            DividerTestScreen()
                        }) {
                            Text("分割线测试")
                        }
                        Button({
                            ProgressBarTestScreen()
                        }) {
                            Text("进度条测试")
                        }
                        Button({
                            ColorPickerTestScreen()
                        }) {
                            Text("取色器测试")
                        }
                        Button({
                            AtlasTestScreen()
                        }) {
                            Text("图集测试")
                        }
                        Button({
                            ReorderableTestScreen()
                        }) {
                            Text("拖拽排序测试")
                        }
                        Button({
                            SelectorTriggerTestScreen()
                        }) {
                            Text("选择器触发件测试")
                        }
                        Button({
                            SelectorTestScreen()
                        }) {
                            Text("单选选择器测试")
                        }
                    }
                }
            }
        }
    }
}