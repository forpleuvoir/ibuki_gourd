package moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.copy
import moe.forpleuvoir.ibukigourd.text.wrapToLines
import moe.forpleuvoir.ibukigourd.text.wrapToTextLines
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

fun DrawContext.batchRenderText(
    textRenderer: TextRenderer = this.client.textRenderer,
    block: TextBatchRenderScope.() -> Unit
) {
    TextBatchRenderScope(textRenderer).apply(block)
    draw()
}

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
open class TextBatchRenderScope internal constructor(private val textRenderer: TextRenderer) {

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
    fun DrawContext.text(
        text: Text,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
        color: ARGBColor = Color(text.style.color?.rgb?.toLong() ?: 0xFF000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        textRenderer.draw(
            ReorderingUtil.reorder(text, rightToLeft),
            x,
            y,
            color.argb,
            shadow,
            positionMatrix,
            vertexConsumers,
            layerType,
            backgroundColor.argb,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        )
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
    fun DrawContext.text(
        text: OrderedText,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        color: ARGBColor = Color(0xFF000000),
        backgroundColor: ARGBColor = Color(0),
    ) {
        textRenderer.draw(
            text,
            x,
            y,
            color.argb,
            shadow,
            positionMatrix,
            vertexConsumers,
            layerType,
            backgroundColor.argb,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
        )
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
    fun DrawContext.text(
        text: String,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
        color: ARGBColor = Color(0xFF000000),
        backgroundColor: ARGBColor = Color(0),
    ) {
        textRenderer.draw(
            text,
            x,
            y,
            color.argb,
            shadow,
            positionMatrix,
            vertexConsumers,
            layerType,
            backgroundColor.argb,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            rightToLeft
        )
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
    fun DrawContext.alignmentText(
        text: String,
        box: Box,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
        color: ARGBColor = Color(0x000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        val position = align(Orientation.Vertical).align(box, Size(textRenderer.getWidth(text).toFloat(), textRenderer.fontHeight.toFloat()))
        text(text, position.x(), position.y(), shadow, layerType, rightToLeft, color, backgroundColor)
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
    fun DrawContext.alignmentText(
        text: Text,
        box: Box,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
        color: ARGBColor = Color(text.style.color?.rgb?.toLong() ?: 0xFF000000),
        backgroundColor: ARGBColor = Color(0),
    ) {
        val position = align(Orientation.Vertical).align(box, Size(textRenderer.getWidth(text).toFloat(), textRenderer.fontHeight.toFloat()))
        text(text, position.x(), position.y(), shadow, layerType, rightToLeft, color, backgroundColor)
    }


    /**
     * 渲染多行文本,会以换行符分'/n'割字符串
     * @receiver DrawContext
     * @param string String
     * @param box Box
     * @param lineSpacing Number
     * @param align (Orientation) -> Alignment
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param rightToLeft Boolean
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun DrawContext.stringLines(
        string: String,
        box: Box,
        lineSpacing: Number = 1,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Color(0),
    ) {
        var top: Float = box.top
        for (text in string.wrapToLines(textRenderer, box.width.toInt())) {
            alignmentText(
                text, Box(box.position.copy(y = top), box.width, textRenderer.fontHeight), align,
                shadow, layerType, rightToLeft, color, backgroundColor
            )
            top += textRenderer.fontHeight + lineSpacing.toFloat()
        }
    }

    /**
     * 渲染多行文本
     * @receiver DrawContext
     * @param lines List<String>
     * @param box Box
     * @param lineSpacing Number
     * @param align (Orientation) -> Alignment
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param rightToLeft Boolean
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun DrawContext.stringLines(
        lines: List<String>,
        box: Box,
        lineSpacing: Number = 1,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        var top: Float = box.top
        for (text in lines.wrapToLines(textRenderer, box.width.toInt())) {
            alignmentText(
                text, Box(box.position.copy(y = top), box.width, textRenderer.fontHeight), align,
                shadow, layerType, rightToLeft, color, backgroundColor
            )
            top += textRenderer.fontHeight + lineSpacing.toFloat()
        }
    }

    /**
     * 绘制多行文本
     * @receiver TextRenderer
     * @param text [Text]
     * @param box Rectangle
     * @param lineSpacing Number
     * @param align HorizontalAlignment
     * @param shadow Boolean
     * @param layerType [TextLayerType]
     * @param rightToLeft Boolean
     * @param color ARGBColor
     * @param backgroundColor Color
     */
    fun DrawContext.textLines(
        text: Text,
        box: Box,
        lineSpacing: Number = 1,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
        color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        var top: Float = box.top
        for (t in text.wrapToTextLines(textRenderer, box.width.toInt())) {
            alignmentText(
                text,
                Box(box.position.copy(y = top), box.width, textRenderer.fontHeight), align,
                shadow, layerType, rightToLeft, color, backgroundColor
            )
            top += textRenderer.fontHeight + lineSpacing.toFloat()
        }
    }

    /**
     * 绘制多行文本
     * @receiver TextRenderer
     * @param lines List<[Text]>
     * @param box Rectangle
     * @param lineSpacing Number
     * @param align HorizontalAlignment
     * @param shadow Boolean
     * @param layerType [TextLayerType]
     * @param rightToLeft Boolean
     * @param color ARGBColor
     * @param backgroundColor Color
     */
    fun DrawContext.textLines(
        lines: List<Text>,
        box: Box,
        lineSpacing: Number = 1,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
        color: ARGBColor = Color(lines[0].style.color?.rgb ?: 0x000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        stringLines(
            lines.map { it.string },
            box, lineSpacing,
            align, shadow, layerType, rightToLeft, color, backgroundColor,
        )
    }
}