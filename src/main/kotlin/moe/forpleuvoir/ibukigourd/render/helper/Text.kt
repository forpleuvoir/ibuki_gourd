@file:Suppress("UNUSED", "DuplicatedCode")

package moe.forpleuvoir.ibukigourd.render.helper

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.util.text.Text
import moe.forpleuvoir.ibukigourd.util.text.wrapToLines
import moe.forpleuvoir.ibukigourd.util.text.wrapToTextLines
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.resource.language.ReorderingUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.OrderedText

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
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    VertexConsumerProvider.immediate(bufferBuilder).also {
        this.draw(
            ReorderingUtil.reorder(text, rightToLeft),
            x.toFloat(),
            y.toFloat(),
            color.argb,
            shadow,
            matrixStack.peek().positionMatrix,
            it,
            layerType,
            backgroundColor.argb,
            MAX_LIGHT_COORDINATE
        )
    }.draw()

}

fun TextRenderer.renderText(
    matrixStack: MatrixStack,
    text: OrderedText,
    x: Number,
    y: Number,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    color: ARGBColor = Color(0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    VertexConsumerProvider.immediate(bufferBuilder).also {
        this.draw(
            text,
            x.toFloat(),
            y.toFloat(),
            color.argb,
            shadow,
            matrixStack.peek().positionMatrix,
            it,
            layerType,
            backgroundColor.argb,
            MAX_LIGHT_COORDINATE,
        )
    }.draw()
}

fun TextRenderer.renderText(
    matrixStack: MatrixStack,
    text: String,
    x: Number,
    y: Number,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    VertexConsumerProvider.immediate(bufferBuilder).also {
        this.draw(
            text,
            x.toFloat(),
            y.toFloat(),
            color.argb,
            shadow,
            matrixStack.peek().positionMatrix,
            it,
            layerType,
            backgroundColor.argb,
            MAX_LIGHT_COORDINATE,
            rightToLeft
        )
    }.draw()
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
    rect: Rectangle<Vector3<Float>>,
    align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    val position = align(Arrangement.Vertical).align(rect, rect(Vector3f(), getWidth(text), fontHeight))
    renderText(matrixStack, text, position.x, position.y, shadow, layerType, rightToLeft, color, backgroundColor)
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
    rect: Rectangle<Vector3<Float>>,
    align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    val position = align(Arrangement.Vertical).align(rect, rect(Vector3f(), getWidth(text), fontHeight))
    renderText(matrixStack, text, position.x, position.y, shadow, layerType, rightToLeft, color, backgroundColor)
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
    align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) = renderAlignmentText(matrixStack, text, transform.asWorldRect, align, shadow, layerType, rightToLeft, color, backgroundColor)


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
inline fun TextRenderer.renderStringLines(
    matrixStack: MatrixStack,
    string: String,
    rect: Rectangle<Vector3<Float>>,
    lineSpacing: Number = 1,
    align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Colors.BLACK,
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    var top: Float = rect.top
    for (text in string.wrapToLines(this, rect.width.toInt())) {
        renderAlignmentText(
            matrixStack, text,
            rect(Vector3f(0f, top, 0f), rect.width, this.fontHeight), align,
            shadow, layerType, rightToLeft, color, backgroundColor
        )
        top += this.fontHeight + lineSpacing.toFloat()
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
inline fun TextRenderer.renderStringLines(
    matrixStack: MatrixStack,
    lines: List<String>,
    rect: Rectangle<Vector3<Float>>,
    lineSpacing: Number = 1,
    align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Colors.BLACK,
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    var top: Float = rect.top
    for (text in lines.wrapToLines(this, rect.width.toInt())) {
        renderAlignmentText(
            matrixStack, text,
            rect(Vector3f(0f, top, 0f), rect.width, this.fontHeight), align,
            shadow, layerType, rightToLeft, color, backgroundColor
        )
        top += this.fontHeight + lineSpacing.toFloat()
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
inline fun TextRenderer.renderTextLines(
    matrixStack: MatrixStack,
    text: Text,
    rect: Rectangle<Vector3<Float>>,
    lineSpacing: Number = 1,
    align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(text.style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    var top: Float = rect.top
    for (t in text.wrapToTextLines(this, rect.width.toInt())) {
        renderAlignmentText(
            matrixStack, text,
            rect(Vector3f(0f, top, 0f), rect.width, this.fontHeight), align,
            shadow, layerType, rightToLeft, color, backgroundColor
        )
        top += this.fontHeight + lineSpacing.toFloat()
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
inline fun TextRenderer.renderTextLines(
    matrixStack: MatrixStack,
    lines: List<Text>,
    rect: Rectangle<Vector3<Float>>,
    lineSpacing: Number = 1,
    align: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    shadow: Boolean = false,
    layerType: TextLayerType = TextLayerType.NORMAL,
    rightToLeft: Boolean = this.isRightToLeft,
    color: ARGBColor = Color(lines[0].style.color?.rgb ?: 0x000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
) {
    renderStringLines(
        matrixStack,
        ArrayList<String>().apply { lines.forEach { add(it.string) } },
        rect, lineSpacing,
        align, shadow, layerType, rightToLeft, color, backgroundColor,
    )
}

