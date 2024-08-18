package moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.draw
import moe.forpleuvoir.ibukigourd.text.size
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.resource.language.ReorderingUtil
import net.minecraft.text.OrderedText
import net.minecraft.text.Text

/**
 * 渲染文本
 * @receiver DrawContext
 * @param text Text
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun DrawContext.renderText(
    text: Text,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    color: ARGBColor = Color(text.style.color?.rgb?.toLong() ?: 0xFF000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    textRenderer: TextRenderer = this.client.textRenderer,
    rightToLeft: Boolean = textRenderer.isRightToLeft,
) {
    textRenderer.draw(
        ReorderingUtil.reorder(text, rightToLeft),
        x,
        y,
        color,
        shadow,
        positionMatrix,
        vertexConsumers,
        layerType,
        backgroundColor,
        LightmapTextureManager.MAX_LIGHT_COORDINATE
    )
    draw()
}

/**
 * 渲染有序文本
 * @receiver DrawContext
 * @param text OrderedText
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun DrawContext.renderText(
    text: OrderedText,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Color(0),
    textRenderer: TextRenderer = this.client.textRenderer,
) {
    textRenderer.draw(
        text,
        x,
        y,
        color,
        shadow,
        positionMatrix,
        vertexConsumers,
        layerType,
        backgroundColor,
        LightmapTextureManager.MAX_LIGHT_COORDINATE,
    )
    draw()
}

/**
 * 渲染文本
 * @receiver DrawContext
 * @param text String
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun DrawContext.renderText(
    text: String,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Color(0),
    textRenderer: TextRenderer = this.client.textRenderer,
    rightToLeft: Boolean = textRenderer.isRightToLeft,
) {
    textRenderer.draw(
        ReorderingUtil.reorder(Literal(text), rightToLeft),
        x,
        y,
        color,
        shadow,
        positionMatrix,
        vertexConsumers,
        layerType,
        backgroundColor,
        LightmapTextureManager.MAX_LIGHT_COORDINATE,
    )
    draw()
}

/**
 * 渲染对齐文本
 * @receiver DrawContext
 * @param text String
 * @param box Box 需要对齐的[Box]
 * @param align ([Orientation]) -> [Alignment] 对齐方式
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun DrawContext.renderAlignmentText(
    text: String,
    box: Box,
    alignment: Alignment = Alignment.Center,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    color: ARGBColor = Color(0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    textRenderer: TextRenderer = this.client.textRenderer,
    rightToLeft: Boolean = textRenderer.isRightToLeft,
) {
    alignment.align(box, text.size(textRenderer).toFloat()).apply {
        renderText(text, box.x + x(), box.y + y(), shadow, layerType, color, backgroundColor, textRenderer, rightToLeft)
    }
}

/**
 * 渲染对齐文本
 * @receiver DrawContext
 * @param text Text
 * @param box Box 需要对齐的[Box]
 * @param align ([Orientation]) -> [Alignment] 对齐方式
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun DrawContext.renderAlignmentText(
    text: Text,
    box: Box,
    alignment: Alignment = Alignment.Center,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    color: ARGBColor = Color(text.style.color?.rgb?.toLong() ?: 0xFF000000),
    backgroundColor: ARGBColor = Color(0),
    textRenderer: TextRenderer = this.client.textRenderer,
    rightToLeft: Boolean = textRenderer.isRightToLeft,
) {
    alignment.align(box, text.size(textRenderer).toFloat()).apply {
        renderText(text, box.x + x(), box.y + y(), shadow, layerType, color, backgroundColor, textRenderer, rightToLeft)
    }
}
