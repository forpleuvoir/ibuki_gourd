@file:Suppress("UNUSED", "DuplicatedCode", "MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.render.helper

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.bufferBuilder
import moe.forpleuvoir.ibukigourd.render.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.math.copy
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.wrapToLines
import moe.forpleuvoir.ibukigourd.text.wrapToTextLines
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.resource.language.ReorderingUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.OrderedText


fun TextRenderer.batchRender(
    bufferBuilder: BufferBuilder = moe.forpleuvoir.ibukigourd.render.bufferBuilder,
    scope: BatchTextRenderScope.() -> Unit
) {
    BatchTextRenderScope.apply {
        vertexConsumer = VertexConsumerProvider.immediate(bufferBuilder)
        textRenderer = this@batchRender
        scope.invoke(this)
        vertexConsumer!!.draw()
        vertexConsumer = null
        textRenderer = null
    }

}

object BatchTextRenderScope {

    var vertexConsumer: VertexConsumerProvider.Immediate? = null
        internal set

    var textRenderer: TextRenderer? = null
        internal set

    fun renderText(
        matrixStack: MatrixStack,
        text: Text,
        x: Number,
        y: Number,
        z: Number,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer!!.isRightToLeft,
        color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        matrixStack.let {
            it.push()
            it.translate(0f, 0f, z.toFloat())
            textRenderer!!.draw(
                ReorderingUtil.reorder(text, rightToLeft),
                x.toFloat(),
                y.toFloat(),
                color.argb,
                shadow,
                it.peek().positionMatrix,
                vertexConsumer,
                layerType,
                backgroundColor.argb,
                MAX_LIGHT_COORDINATE
            )
            it.pop()
        }
    }

    fun renderText(
        matrixStack: MatrixStack,
        text: OrderedText,
        x: Number,
        y: Number,
        z: Number,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        color: ARGBColor = Color(0x000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        matrixStack.let {
            it.push()
            it.translate(0f, 0f, z.toFloat())
            textRenderer!!.draw(
                text,
                x.toFloat(),
                y.toFloat(),
                color.argb,
                shadow,
                it.peek().positionMatrix,
                vertexConsumer,
                layerType,
                backgroundColor.argb,
                MAX_LIGHT_COORDINATE,
            )
            it.pop()
        }
    }

    fun renderText(
        matrixStack: MatrixStack,
        text: String,
        x: Number,
        y: Number,
        z: Number,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer!!.isRightToLeft,
        color: ARGBColor = Color(0x000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        matrixStack.let {
            it.push()
            it.translate(0f, 0f, z.toFloat())
            textRenderer!!.draw(
                text,
                x.toFloat(),
                y.toFloat(),
                color.argb,
                shadow,
                it.peek().positionMatrix,
                vertexConsumer,
                layerType,
                backgroundColor.argb,
                MAX_LIGHT_COORDINATE,
                rightToLeft
            )
            it.pop()
        }
    }

    /**
     * 渲染对齐文本
     * @receiver TextRenderer
     * @param text String
     * @param rect Rectangle<Vector3<Float>>
     * @param align (Arrangement) -> Alignment
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param rightToLeft Boolean
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun renderAlignmentText(
        matrixStack: MatrixStack,
        text: String,
        rect: Box,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer!!.isRightToLeft,
        color: ARGBColor = Color(0x000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        val position = align(Orientation.Vertical).align(rect, Box(Vector3f(), textRenderer!!.getWidth(text), textRenderer!!.fontHeight))
        renderText(matrixStack, text, position.x(), position.y(), 0, shadow, layerType, rightToLeft, color, backgroundColor)
    }

    /**
     * 渲染对齐文本
     * @receiver TextRenderer
     * @param text Text
     * @param rect Rectangle<Vector3<Float>>
     * @param align (Arrangement) -> Alignment
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun renderAlignmentText(
        matrixStack: MatrixStack,
        text: Text,
        rect: Box,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer!!.isRightToLeft,
        color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        val position = align(Orientation.Vertical).align(rect, Box(Vector3f(), textRenderer!!.getWidth(text), textRenderer!!.fontHeight))
        renderText(matrixStack, text, position.x(), position.y(), 0, shadow, layerType, rightToLeft, color, backgroundColor)
    }

    /**
     * @see renderAlignmentText
     * @param text Text
     * @param transform Transform
     * @param align Alignment
     * @param shadow Boolean
     * @param layerType [TextLayerType]
     * @param color ARGBColor
     * @param backgroundColor Color
     */
    fun renderAlignmentText(
        matrixStack: MatrixStack,
        text: Text,
        transform: Transform,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer!!.isRightToLeft,
        color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) = renderAlignmentText(matrixStack, text, transform.asWorldBox, align, shadow, layerType, rightToLeft, color, backgroundColor)

    fun renderStringLines(
        matrixStack: MatrixStack,
        string: String,
        rect: Box,
        lineSpacing: Number = 1,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer!!.isRightToLeft,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        var top: Float = rect.top
        for (text in string.wrapToLines(textRenderer!!, rect.width.toInt())) {
            renderAlignmentText(
                matrixStack,
                text, Box(rect.position.copy(y = top), rect.width, textRenderer!!.fontHeight), align,
                shadow, layerType, rightToLeft, color, backgroundColor
            )
            top += textRenderer!!.fontHeight + lineSpacing.toFloat()
        }
    }


    /**
     * 绘制多行字符串文本
     * @receiver TextRenderer
     * @param lines List<String>
     * @param rect Rectangle
     * @param lineSpacing Number
     * @param align HorizontalAlignment
     * @param shadow Boolean
     * @param layerType [TextLayerType]
     * @param rightToLeft Boolean
     * @param color ARGBColor
     * @param backgroundColor Color
     */
    fun renderStringLines(
        matrixStack: MatrixStack,
        lines: List<String>,
        rect: Box,
        lineSpacing: Number = 1,
        align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        rightToLeft: Boolean = textRenderer!!.isRightToLeft,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    ) {
        var top: Float = rect.top
        for (text in lines) {
            renderAlignmentText(
                matrixStack,
                text, Box(rect.position.copy(y = top), rect.width, textRenderer!!.fontHeight), align,
                shadow, layerType, rightToLeft, color, backgroundColor
            )
            top += textRenderer!!.fontHeight + lineSpacing.toFloat()
        }
    }
}

/**
 * 渲染文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param text Text
 * @param x Number
 * @param y Number
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param color ARGBColor
 * @param backgroundColor Color
 */
fun TextRenderer.renderText(
    matrixStack: MatrixStack,
    text: Text,
    x: Number,
    y: Number,
    z: Number,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    matrixStack.let {
        it.push()
        it.translate(0f, 0f, z.toFloat())
        VertexConsumerProvider.immediate(bufferBuilder).also { v ->
            this.draw(
                ReorderingUtil.reorder(text, rightToLeft),
                x.toFloat(),
                y.toFloat(),
                color.argb,
                shadow,
                it.peek().positionMatrix,
                v,
                layerType,
                backgroundColor.argb,
                MAX_LIGHT_COORDINATE
            )
        }.draw()
        it.pop()
    }
}

fun TextRenderer.renderText(
    matrixStack: MatrixStack,
    text: OrderedText,
    x: Number,
    y: Number,
    z: Number,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    color: ARGBColor = Color(0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    matrixStack.let {
        it.push()
        it.translate(0f, 0f, z.toFloat())
        VertexConsumerProvider.immediate(bufferBuilder).also { v ->
            this.draw(
                text,
                x.toFloat(),
                y.toFloat(),
                color.argb,
                shadow,
                it.peek().positionMatrix,
                v,
                layerType,
                backgroundColor.argb,
                MAX_LIGHT_COORDINATE,
            )
        }.draw()
        it.pop()
    }
}

fun TextRenderer.renderText(
    matrixStack: MatrixStack,
    text: String,
    x: Number,
    y: Number,
    z: Number,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    matrixStack.let {
        it.push()
        it.translate(0f, 0f, z.toFloat())
        VertexConsumerProvider.immediate(bufferBuilder).also { v ->
            this.draw(
                text,
                x.toFloat(),
                y.toFloat(),
                color.argb,
                shadow,
                it.peek().positionMatrix,
                v,
                layerType,
                backgroundColor.argb,
                MAX_LIGHT_COORDINATE,
                rightToLeft
            )
        }.draw()
        it.pop()
    }
}

/**
 * 渲染对齐文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param text String
 * @param rect Rectangle<Vector3<Float>>
 * @param align (Arrangement) -> Alignment
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
inline fun TextRenderer.renderAlignmentText(
    matrixStack: MatrixStack,
    text: String,
    rect: Box,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    val position = align(Orientation.Vertical).align(rect, Box(Vector3f(), getWidth(text), fontHeight))
    renderText(matrixStack, text, position.x(), position.y(), 0, shadow, layerType, rightToLeft, color, backgroundColor)
}

/**
 * 渲染对齐文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param text Text
 * @param rect Rectangle<Vector3<Float>>
 * @param align (Arrangement) -> Alignment
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
inline fun TextRenderer.renderAlignmentText(
    matrixStack: MatrixStack,
    text: Text,
    rect: Box,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    val position = align(Orientation.Vertical).align(rect, Box(Vector3f(), getWidth(text), fontHeight))
    renderText(matrixStack, text, position.x(), position.y(), 0, shadow, layerType, rightToLeft, color, backgroundColor)
}

/**
 * @see renderAlignmentText
 * @param matrixStack MatrixStack
 * @param text Text
 * @param transform Transform
 * @param align Alignment
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param color ARGBColor
 * @param backgroundColor Color
 */
inline fun TextRenderer.renderAlignmentText(
    matrixStack: MatrixStack,
    text: Text,
    transform: Transform,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) = renderAlignmentText(matrixStack, text, transform.asWorldBox, align, shadow, layerType, rightToLeft, color, backgroundColor)


/**
 * 绘制多行字符串文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param string String
 * @param rect Rectangle<Vector3<Float>>
 * @param lineSpacing Number
 * @param align (Arrangement) -> Alignment
 * @param shadow Boolean
 * @param layerType TextRenderer.TextLayerType
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun TextRenderer.renderStringLines(
    matrixStack: MatrixStack,
    string: String,
    rect: Box,
    lineSpacing: Number = 1,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Colors.BLACK,
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    batchRender {
        var top: Float = rect.top
        for (text in string.wrapToLines(textRenderer!!, rect.width.toInt())) {
            renderAlignmentText(
                matrixStack,
                text, Box(rect.position.copy(y = top), rect.width, textRenderer!!.fontHeight), align,
                shadow, layerType, rightToLeft, color, backgroundColor
            )
            top += textRenderer!!.fontHeight + lineSpacing.toFloat()
        }
    }
}

/**
 * 绘制多行字符串文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param lines List<String>
 * @param rect Rectangle
 * @param lineSpacing Number
 * @param align HorizontalAlignment
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor Color
 */
fun TextRenderer.renderStringLines(
    matrixStack: MatrixStack,
    lines: List<String>,
    rect: Box,
    lineSpacing: Number = 1,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Colors.BLACK,
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    batchRender {
        var top: Float = rect.top
        for (text in lines.wrapToLines(textRenderer!!, rect.width.toInt())) {
            renderAlignmentText(
                matrixStack,
                text, Box(rect.position.copy(y = top), rect.width, textRenderer!!.fontHeight), align,
                shadow, layerType, rightToLeft, color, backgroundColor
            )
            top += textRenderer!!.fontHeight + lineSpacing.toFloat()
        }
    }
}

/**
 * 绘制多行文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param text [Text]
 * @param rect Rectangle
 * @param lineSpacing Number
 * @param align HorizontalAlignment
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor Color
 */
fun TextRenderer.renderTextLines(
    matrixStack: MatrixStack,
    text: Text,
    rect: Box,
    lineSpacing: Number = 1,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    batchRender {
        var top: Float = rect.top
        for (t in text.wrapToTextLines(textRenderer!!, rect.width.toInt())) {
            renderAlignmentText(
                matrixStack, text,
                Box(rect.position.copy(y = top), rect.width, textRenderer!!.fontHeight), align,
                shadow, layerType, rightToLeft, color, backgroundColor
            )
            top += textRenderer!!.fontHeight + lineSpacing.toFloat()
        }
    }
}


/**
 * 绘制多行文本
 * @receiver TextRenderer
 * @param matrixStack MatrixStack
 * @param lines List<[Text]>
 * @param rect Rectangle
 * @param lineSpacing Number
 * @param align HorizontalAlignment
 * @param shadow Boolean
 * @param layerType [TextLayerType]
 * @param rightToLeft Boolean
 * @param color ARGBColor
 * @param backgroundColor Color
 */
fun TextRenderer.renderTextLines(
    matrixStack: MatrixStack,
    lines: List<Text>,
    rect: Box,
    lineSpacing: Number = 1,
    align: (Orientation) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(lines[0].style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    renderStringLines(
        matrixStack,
        lines.map { it.plainText },
        rect, lineSpacing,
        align, shadow, layerType, rightToLeft, color, backgroundColor,
    )
}

