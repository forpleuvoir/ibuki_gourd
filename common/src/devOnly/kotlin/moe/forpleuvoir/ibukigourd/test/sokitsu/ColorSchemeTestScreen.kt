package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ColorButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.darkColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.lightColorScheme
import net.minecraft.resources.Identifier

/**
 * 配色测试屏：左右两栏并列，逐槽位列出色板 —— 左栏浅色（[lightColorScheme]）、右栏深色（[darkColorScheme]）。
 *
 * 每个槽位一行：槽位名 + **四个 [moe.forpleuvoir.ibukigourd.ui.sokitsu.ColorButton]**（透明度档位
 * [AlphaLevels] = 100% / 75% / 50% / 25%），按钮底色 = 该槽位色 × 档位 alpha，内容 = 完整 `#AARRGGBB`。
 * 内容色由 ColorButton 按底色亮度取纯黑/纯白（只看 RGB，与档位无关），故本屏同时是
 * "黑白内容色可读性"的检验现场。
 *
 * 栏底各铺本方 [ColorScheme.surface]，两栏的亮暗关系一眼可辨，
 * 不由外层测试屏的全局亮暗开关影响（外层主题只决定屏幕背景）。
 *
 * 槽位清单与 [ColorScheme] 的构造参数一一对应，新增槽位时同步 [ColorScheme.swatches]。
 */
fun ColorSchemeTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 32.dp).padding(16.dp),
        ) {
            TextureSlotProbe()
            Row(
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SchemeColumn("浅色", lightColorScheme(), Modifier.weight(1f))
                SchemeColumn("深色", darkColorScheme(), Modifier.weight(1f))
            }
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
 * 一栏：标题 + 该配色方案的全部槽位。
 *
 * 标题固定，色板列表独立滚动（`verticalScroll`）——一栏 15 行在常见 GUI 高度下放不满，
 * 内容不足时滚动量为 0，不产生位移。
 */
@Composable
private fun SchemeColumn(title: String, scheme: ColorScheme, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(scheme.surface)
            .padding(12.dp),
    ) {
        Text(title, color = scheme.onSurface)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            scheme.swatches().forEach { (name, color) -> SwatchRow(name, color, scheme) }
        }
    }
}

/**
 * 单个槽位：槽位名 + 四个 alpha 档位（[AlphaLevels]）的 [ColorButton] 横排。
 *
 * 四个按钮 `weight(1f)` 等宽平分栏宽：底色 = 槽位色 × 档位 alpha，内容 = 完整 `#AARRGGBB`。
 * 按钮尺寸、内边距、字号**全部用默认值**（color_button meta 的 minSize/padding + typography.button）。
 * 内容色**不指定**，由 ColorButton 按底色 RGB 亮度取纯黑/纯白（与档位无关）：
 * 因此这里同时是该规则的可读性检验（浅底上的深字、深底上的浅字）。
 */
@Composable
private fun SwatchRow(name: String, color: Color, scheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(name, color = scheme.onSurface)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AlphaLevels.forEach { alpha ->
                ColorButton(
                    onClick = {},
                    color = color.copy(alpha = alpha),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(color.copy(alpha = alpha).hex())
                }
            }
        }
    }
}

/** 色块按钮的透明度档位：100% / 75% / 50% / 25%。 */
private val AlphaLevels = listOf(1f, 0.75f, 0.5f, 0.25f)

/** 配色方案的全部颜色槽位，顺序与构造参数一致。 */
private fun ColorScheme.swatches(): List<Pair<String, Color>> = listOf(
    "background" to background,
    "surface" to surface,
    "surfaceVariant" to surfaceVariant,
    "primary" to primary,
    "secondary" to secondary,
    "error" to error,
    "primaryContainer" to primaryContainer,
    "onBackground" to onBackground,
    "onSurface" to onSurface,
    "onSurfaceVariant" to onSurfaceVariant,
    "onPrimary" to onPrimary,
    "onPrimaryContainer" to onPrimaryContainer,
    "onSecondary" to onSecondary,
    "onError" to onError,
    "outline" to outline,
)

/** `#AARRGGBB`（含 alpha 位）。 */
private fun Color.hex(): String = "#%08X".format(toArgb())
