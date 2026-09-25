package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.test.TestDarkScheme
import moe.forpleuvoir.ibukigourd.test.TestLightScheme
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SurfaceDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeSlotButtons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.toMetaJson
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.withSlot
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.darkColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.lightColorScheme
import net.minecraft.resources.Identifier

/**
 * 配色测试屏：左右两栏并列，编辑的是 **所有测试屏幕共用的两份配色**
 * （[TestLightScheme] / [TestDarkScheme]，TestScreenTheme 取的就是它们）——
 * 这里改一个槽位，其它测试屏（按钮 / 滑条 / 弹窗……）立刻跟着变。
 *
 * 每栏：
 * - 槽位按钮一律是 [ColorPickButton] —— 点开弹取色器，确认后写回该栏那份配色；
 * - `xx` 与 `onXx` **成对排在同一行**（没有对应 `on` 的槽位单独一行，如 `outline`）；
 * - 栏头「重置」把该份配色换回主题默认值，「导出」把全部槽位序列化为 JSON 写入剪贴板
 *   （可直接粘进主题 meta 的配色段）。
 *
 * 因为 [ColorScheme] 的 setter 是 internal，测试屏改槽位走公开的 [ColorScheme.copy] 重建实例再赋值；
 * 槽位清单与 [ColorScheme] 构造参数一一对应，新增槽位时同步 [SchemeSlots] / [ColorScheme.withSlot]。
 */
fun ColorSchemeTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        ColorSchemeTestContent()
    }
}

/**
 * 测试屏内容：两栏各包一层本栏主题（[SchemeColumn]），因此同一屏里能同时看到浅色与深色两份；
 * 写入的是全局共享的 [TestLightScheme] / [TestDarkScheme]，故改动对所有测试屏生效。
 */
@Composable
private fun ColorSchemeTestContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 32.dp).padding(16.dp),
    ) {
        TextureSlotProbe()
        Row(
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SchemeColumn(
                title = "浅色",
                scheme = TestLightScheme,
                onColorChange = { name, color -> TestLightScheme = TestLightScheme.withSlot(name, color) },
                onReset = { TestLightScheme = lightColorScheme() },
                modifier = Modifier.weight(1f),
            )
            SchemeColumn(
                title = "深色",
                scheme = TestDarkScheme,
                onColorChange = { name, color -> TestDarkScheme = TestDarkScheme.withSlot(name, color) },
                onReset = { TestDarkScheme = darkColorScheme() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * 运行时探针：显示图集里 `ui/color_button/normal` 各图层实际解析出的 `颜色槽位/合成策略`。
 *
 * 用来分辨"素材标注没读到"与"染色逻辑不对"：这里显示 `tile=none/Passthrough` 就说明 `.aseprite`
 * 的 `level` / `tint` 都已正确读入，画面若仍带色，问题在图集是否重建（资源重载）或层序，而非标签。
 */
@Composable
private fun TextureSlotProbe() {
    val probeId = Identifier.fromNamespaceAndPath(IbukiGourd.MOD_ID, "ui/color_button/normal")
    val sprite = SokitsuAtlasManager.sprite(SokitsuAtlasManager.UI_ATLAS_ID, probeId)
    val text = if (sprite.isEmpty) {
        "图集未加载或未命中"
    } else {
        sprite.layers.joinToString("　") { "${it.layerId}=${it.colorSlot}/${it.tintMode}" }
    }
    Text("color_button/normal 探针：$text")
}

/**
 * 一栏：栏头（标题 + 重置 / 导出）+ 该栏配色的全部槽位。
 *
 * 整栏内容包在 **本栏主题** `SokitsuTheme(colorScheme = scheme)` 里：这样栏内的组件
 * （[ColorPickButton]、[FlatButton]、[Text]，以及取色按钮弹出的弹窗——图层会继承组合上下文）
 * 全部按这一份配色解析，而不是沿用测试屏的全局配色。面板底色 / 内容色也取自该主题
 * （`SokitsuTheme.colorScheme.surface`），改一个槽位整栏随之刷新。
 *
 * 栏头两个按钮都是**整栏级别**：重置换回一份新的默认配色，导出把本栏全部槽位写成 JSON 到剪贴板。
 * 色板列表独立滚动（`verticalScroll`）。
 */
@Composable
private fun SchemeColumn(
    title: String,
    scheme: ColorScheme,
    onColorChange: (String, Color) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    SokitsuTheme(colorScheme = scheme) {
        Surface(
            sprite = SurfaceDefaults.embeddedPanel,
            color = SokitsuTheme.colorScheme.surface,
            modifier = modifier.fillMaxHeight(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(title, modifier = Modifier.weight(1f))
                    FlatButton(onClick = onReset) {
                        Text("重置")
                    }
                    FlatButton(onClick = {
                        scope.launch { clipboard.setClipEntry(ClipEntry(scheme.toMetaJson())) }
                    }) {
                        Text("导出")
                    }
                }

                ColorSchemeSlotButtons(
                    scheme = scheme,
                    onColorChange = onColorChange,
                    modifier = Modifier.weight(1f),
                    maxHeight = Dp.Unspecified,
                )
            }
        }
    }
}
