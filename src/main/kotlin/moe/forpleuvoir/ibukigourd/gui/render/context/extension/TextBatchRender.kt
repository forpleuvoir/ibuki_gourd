package moe.forpleuvoir.ibukigourd.gui.render.context.extension

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.math.copy
import moe.forpleuvoir.ibukigourd.render.setShader
import moe.forpleuvoir.ibukigourd.util.text.wrapToLines
import moe.forpleuvoir.ibukigourd.util.text.wrapToTextLines
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.*
import net.minecraft.client.resource.language.ReorderingUtil
import net.minecraft.text.OrderedText
import net.minecraft.text.Text

fun RenderContext.batchRenderText(
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionTexColorProgram,
    block: TextBatchRenderScope.() -> Unit
) {
    setShader(shaderSupplier)
    VertexConsumerProvider.immediate(bufferBuilder).also { v ->
        block(TextBatchRenderScope.apply { immediate = v })
    }.draw()
}

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
open class TextBatchRenderScope private constructor() {

    internal companion object : TextBatchRenderScope()

    lateinit var immediate: VertexConsumerProvider.Immediate
        internal set

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
    fun RenderContext.text(
        text: Text,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = this.textRenderer.isRightToLeft,
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
            immediate,
            layerType,
            backgroundColor.argb,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        )
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
    fun RenderContext.text(
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
            immediate,
            layerType,
            backgroundColor.argb,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
        )
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
    fun RenderContext.text(
        text: String,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = this.textRenderer.isRightToLeft,
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
            immediate,
            layerType,
            backgroundColor.argb,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            rightToLeft
        )
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
    fun RenderContext.alignmentText(
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
        text(text, position.x(), position.y(), shadow, layerType, rightToLeft, color, backgroundColor)
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
    fun RenderContext.alignmentText(
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
        text(text, position.x(), position.y(), shadow, layerType, rightToLeft, color, backgroundColor)
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
    fun RenderContext.alignmentText(
        text: Text,
        transform: Transform,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
        color: ARGBColor = Color(text.style.color?.rgb?.toLong() ?: 0xFF000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) = alignmentText(text, transform.asWorldBox, align, shadow, layerType, rightToLeft, color, backgroundColor)

    /**
     * 渲染多行文本,会以换行符分'/n'割字符串
     * @receiver RenderContext
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
    fun RenderContext.stringLines(
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
     * @receiver RenderContext
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
    fun RenderContext.stringLines(
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
    fun RenderContext.textLines(
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
     * @param matrixStack MatrixStack
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
    fun RenderContext.textLines(
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