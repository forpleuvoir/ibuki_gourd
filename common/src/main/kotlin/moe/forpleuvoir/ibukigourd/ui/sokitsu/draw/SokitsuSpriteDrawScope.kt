package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.SLOT_NONE
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.SLOT_TONE
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.fromToken

/**
 * Sokitsu 精灵的自定义绘制数据，由 [SokitsuSpritePlugin] 在渲染阶段消费。
 *
 * - [sprite]：精灵定义（图层 + UV + fill + colorSlot）
 * - [size]：目标绘制尺寸（屏幕像素，来自节点 DrawModifierNode 的 size）
 * - [pixelScale]：像素放大倍率（LocalSokitsuPixelScale，1 逻辑像素 → N×N 屏幕像素块，整数）
 * - [tintColors]：每个图层的顶点着色色（0xAARRGGBB，组合阶段用主题色解析），与 [SokitsuSprite.layers] 一一对应
 * - [shadowOffset]：阴影图层（[SokitsuLayerSprite.isShadow]）的绘制偏移（屏幕像素，已乘像素放大倍率；
 *   = 向 [moe.forpleuvoir.compose_minecraft.platform.ui.LocalShadowLight] 光源反方向投射）
 */
data class SokitsuSpriteDrawData(
    val sprite: SokitsuSprite,
    val size: IntSize,
    val pixelScale: Int,
    val tintColors: List<Int>,
    val shadowOffset: IntOffset,
)

/**
 * 按槽位名解析图层的顶点着色色：
 * - `tone`：组件主色（[componentColor]）
 * - `none`：直出，返回白色（插件端据此使用原色直通管线）
 * - `shadow`：投影固定黑（素材像素自带半透明）
 * - `outline`：[outlineColor] 覆盖优先（悬停/选中/错误描边），否则取 [scheme] 全局描边色
 * - 其它：按名取 [scheme] 语义槽位色；未知名回落 [componentColor]
 */
fun resolveSlotColor(
    slot: String,
    componentColor: Color,
    scheme: ColorScheme,
    outlineColor: Color = Color.Unspecified,
): Color = when (slot) {
    SLOT_TONE  -> componentColor
    SLOT_NONE  -> Color.White
    "shadow"   -> Color.Black
    "outline"  -> outlineColor.takeOrElse { scheme.outline }
    else -> runCatching {
        scheme.fromToken(ColorSchemeToken.valueOf(slot.replaceFirstChar { it.uppercase() }))
    }.getOrDefault(componentColor)
}

/**
 * 解析精灵各图层的顶点着色色（渲染节点内调用，可读 [scheme]）。
 *
 * [componentColor] 为组件主色（组件 token 解析结果）。
 */
fun buildSokitsuSpriteDrawData(
    sprite: SokitsuSprite,
    size: IntSize,
    pixelScale: Int,
    componentColor: Color,
    scheme: ColorScheme,
    shadowOffset: IntOffset,
    outlineColor: Color = Color.Unspecified,
): SokitsuSpriteDrawData {
    val tintColors = sprite.layers.map { layer ->
        resolveSlotColor(layer.colorSlot, componentColor, scheme, outlineColor).toArgb()
    }
    return SokitsuSpriteDrawData(sprite, size, pixelScale, tintColors, shadowOffset)
}
