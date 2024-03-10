package moe.forpleuvoir.ibukigourd.gui.render.context.extension

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.resource.language.ReorderingUtil
import net.minecraft.text.OrderedText
import net.minecraft.text.Text

/**
 * 渲染文本
 * @receiver RenderContext
 * @param text Text
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun RenderContext.renderText(
    text: Text,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.textRenderer.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb?.toLong() ?: 0xFF000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    VertexConsumerProvider.immediate(bufferBuilder).also { v ->
        textRenderer.draw(
            ReorderingUtil.reorder(text, rightToLeft),
            x,
            y,
            color.argb,
            shadow,
            positionMatrix,
            v,
            layerType,
            backgroundColor.argb,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        )
    }.draw()
}

/**
 * 渲染有序文本
 * @receiver RenderContext
 * @param text OrderedText
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun RenderContext.renderText(
    text: OrderedText,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Color(0),
) {
    VertexConsumerProvider.immediate(bufferBuilder).also { v ->
        textRenderer.draw(
            text,
            x,
            y,
            color.argb,
            shadow,
            positionMatrix,
            v,
            layerType,
            backgroundColor.argb,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
        )
    }.draw()
}

/**
 * 渲染文本
 * @receiver RenderContext
 * @param text String
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun RenderContext.renderText(
    text: String,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.textRenderer.isRightToLeft,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Color(0),
) {
    VertexConsumerProvider.immediate(bufferBuilder).also { v ->
        textRenderer.draw(
            text,
            x,
            y,
            color.argb,
            shadow,
            positionMatrix,
            v,
            layerType,
            backgroundColor.argb,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            rightToLeft
        )
    }.draw()
}

/**
 * 渲染对齐文本
 * @receiver RenderContext
 * @param text String
 * @param box Box 需要对齐的[Box]
 * @param align ([Orientation]) -> [Alignment] 对齐方式
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun RenderContext.renderAlignmentText(
    text: String,
    box: Box,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.textRenderer.isRightToLeft,
    color: ARGBColor = Color(0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    val position = align(Orientation.Vertical).align(box, Box(Vector2f(), textRenderer.getWidth(text), textRenderer.fontHeight))
    renderText(text, position.x(), position.y(), shadow, layerType, rightToLeft, color, backgroundColor)
}

/**
 * 渲染对齐文本
 * @receiver RenderContext
 * @param text Text
 * @param box Box 需要对齐的[Box]
 * @param align ([Orientation]) -> [Alignment] 对齐方式
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun RenderContext.renderAlignmentText(
    text: Text,
    box: Box,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = textRenderer.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb?.toLong() ?: 0xFF000000),
    backgroundColor: ARGBColor = Color(0),
) {
    val position = align(Orientation.Vertical).align(box, Box(Vector2f(), textRenderer.getWidth(text), textRenderer.fontHeight))
    renderText(text, position.x(), position.y(), shadow, layerType, rightToLeft, color, backgroundColor)
}

/**
 * @see [RenderContext.renderAlignmentText]
 * @receiver RenderContext
 * @param text Text
 * @param transform Transform
 * @param align (Orientation) -> Alignment
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun RenderContext.renderAlignmentText(
    text: Text,
    transform: Transform,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = textRenderer.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb?.toLong() ?: 0xFF000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) = renderAlignmentText(text, transform.asWorldBox, align, shadow, layerType, rightToLeft, color, backgroundColor)