package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.test.TestDarkScheme
import moe.forpleuvoir.ibukigourd.test.TestLightScheme
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.colorpicker.ColorPickButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SurfaceDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
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
                        scope.launch { clipboard.setClipEntry(ClipEntry(scheme.toJson())) }
                    }) {
                        Text("导出")
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SchemeSlots.forEach { (base, on) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SlotPickButton(
                                name = base,
                                color = scheme[base],
                                onValueChange = { onColorChange(base, it) },
                                modifier = Modifier.weight(1f),
                            )
                            if (on != null) {
                                SlotPickButton(
                                    name = on,
                                    color = scheme[on],
                                    onValueChange = { onColorChange(on, it) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 单个槽位的取色按钮：底色即槽位色，内容写槽位名（内容色由 ColorButton 按底色亮度取黑白）。 */
@Composable
private fun SlotPickButton(
    name: String,
    color: Color,
    onValueChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    ColorPickButton(
        color = color,
        onValueChange = onValueChange,
        modifier = modifier,
        title = { Text(name) },
    ) {
        Text(name)
    }
}

/** 槽位清单：`xx` 与 `onXx` 成对（没有对应 `on` 的单独一行）。 */
private val SchemeSlots: List<Pair<String, String?>> = listOf(
    "background" to "onBackground",
    "surface" to "onSurface",
    "surfaceVariant" to "onSurfaceVariant",
    "primary" to "onPrimary",
    "primaryContainer" to "onPrimaryContainer",
    "secondary" to "onSecondary",
    "error" to "onError",
    "outline" to null,
)

/** 按名取槽位色（未知槽位给 [Color.Unspecified]）。 */
private operator fun ColorScheme.get(name: String): Color = when (name) {
    "background"         -> background
    "surface"            -> surface
    "surfaceVariant"     -> surfaceVariant
    "primary"            -> primary
    "primaryContainer"   -> primaryContainer
    "secondary"          -> secondary
    "error"              -> error
    "onBackground"       -> onBackground
    "onSurface"          -> onSurface
    "onSurfaceVariant"   -> onSurfaceVariant
    "onPrimary"          -> onPrimary
    "onPrimaryContainer" -> onPrimaryContainer
    "onSecondary"        -> onSecondary
    "onError"            -> onError
    "outline"            -> outline
    else                 -> Color.Unspecified
}

/**
 * 按名重设槽位色。
 *
 * [ColorScheme] 的字段 setter 是 internal（仅供主题内部使用），测试屏属于外部消费者，
 * 因此走公开的 [ColorScheme.copy] 重建实例。
 */
private fun ColorScheme.withSlot(name: String, color: Color): ColorScheme = when (name) {
    "background"         -> copy(background = color)
    "surface"            -> copy(surface = color)
    "surfaceVariant"     -> copy(surfaceVariant = color)
    "primary"            -> copy(primary = color)
    "primaryContainer"   -> copy(primaryContainer = color)
    "secondary"          -> copy(secondary = color)
    "error"              -> copy(error = color)
    "onBackground"       -> copy(onBackground = color)
    "onSurface"          -> copy(onSurface = color)
    "onSurfaceVariant"   -> copy(onSurfaceVariant = color)
    "onPrimary"          -> copy(onPrimary = color)
    "onPrimaryContainer" -> copy(onPrimaryContainer = color)
    "onSecondary"        -> copy(onSecondary = color)
    "onError"            -> copy(onError = color)
    "outline"            -> copy(outline = color)
    else                 -> this
}

/** 全部槽位序列化为 JSON（`#AARRGGBB`，可直接粘进主题 meta 的配色段）。 */
private fun ColorScheme.toJson(): String {
    val body = SchemeSlots
        .flatMap { (base, on) -> listOf(base) + listOfNotNull(on) }
        .joinToString(",\n    ") { "\"$it\": \"${this[it].hex()}\"" }
    return "{\n    $body\n}"
}

/** `#AARRGGBB`（含 alpha 位）。 */
private fun Color.hex(): String = "#%08X".format(toArgb())
