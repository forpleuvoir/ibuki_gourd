package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone

/**
 * Sokitsu 精灵的自定义绘制数据，由 [SokitsuSpritePlugin] 在渲染阶段消费。
 *
 * - [sprite]：精灵定义（图层 + UV + fill + tintMode）
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
 * 解析精灵各图层的顶点着色色（组合阶段调用，可读主题色）。
 *
 * [tone] 为染色色板（主题主色 / 辅色 / 错误色 / 自定义）；[colorLevel] 为 null 的图层无主题染色，
 * 返回白色（插件端据此使用原色直通管线，而非着色管线）。
 */
fun buildSokitsuSpriteDrawData(
    sprite: SokitsuSprite,
    size: IntSize,
    pixelScale: Int,
    tone: ColorTone,
    shadowOffset: IntOffset,
): SokitsuSpriteDrawData {
    val tintColors = sprite.layers.map { layer ->
        tintColor(tone, layer.colorLevel, layer.tintAlpha).toArgb()
    }
    return SokitsuSpriteDrawData(sprite, size, pixelScale, tintColors, shadowOffset)
}
