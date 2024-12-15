package moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.text.draw
import moe.forpleuvoir.ibukigourd.text.size
import moe.forpleuvoir.ibukigourd.text.style.argbColor
import moe.forpleuvoir.ibukigourd.text.wrapToLines
import moe.forpleuvoir.ibukigourd.text.wrapToTextLines
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.resource.language.ReorderingUtil
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import org.joml.Matrix4f

fun DrawContext.batchRenderText(
    textRenderer: TextRenderer = this.client.textRenderer,
    block: TextBatchRenderScope.() -> Unit
) = textRenderer.batchRenderText(vertexConsumers, positionMatrix, block)

fun TextRenderer.batchRenderText(
    vertexConsumers: VertexConsumerProvider.Immediate,
    positionMatrix: Matrix4f,
    block: TextBatchRenderScope.() -> Unit
) {
    TextBatchRenderScope(this, vertexConsumers, positionMatrix).apply(block)
    vertexConsumers.draw()
}

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
open class TextBatchRenderScope internal constructor(
    private val textRenderer: TextRenderer,
    private val vertexConsumers: VertexConsumerProvider,
    private val positionMatrix: Matrix4f
) {

    /**
     * 渲染文本
     * @param text Text
     * @param x Float
     * @param y Float
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param rightToLeft Boolean
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushText(
        text: Text,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        color: ARGBColor = text.style.argbColor ?: Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE,
        rightToLeft: Boolean = textRenderer.isRightToLeft
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
            light,
            rightToLeft
        )
    }

    /**
     * 渲染有序文本
     * @param text OrderedText
     * @param x Float
     * @param y Float
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushText(
        text: OrderedText,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        color: ARGBColor = Color(0xFF000000),
        backgroundColor: ARGBColor = Color(0),
        light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE,
        rightToLeft: Boolean = textRenderer.isRightToLeft
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
            light,
            rightToLeft
        )
    }

    /**
     * 渲染文本
     * @param text String
     * @param x Float
     * @param y Float
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param rightToLeft Boolean
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushText(
        text: String,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        color: ARGBColor = Color(0xFF000000),
        backgroundColor: ARGBColor = Color(0),
        light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE,
        rightToLeft: Boolean = textRenderer.isRightToLeft
    ) {
        textRenderer.draw(
            if (rightToLeft) textRenderer.mirror(text) else text,
            x,
            y,
            color.argb,
            shadow,
            positionMatrix,
            vertexConsumers,
            layerType,
            backgroundColor.argb,
            light,
        )
    }

    /**
     * 渲染对齐文本
     * @param text String
     * @param box Box 需要对齐的[Box]
     * @param alignment [Alignment]
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param rightToLeft Boolean
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushAlignmentText(
        text: String,
        box: Box,
        alignment: Alignment = Alignment.CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
    ) {
        val offset = alignment.align(box, text.size(textRenderer).toFloat())
        pushText(text, box.x + offset.x(), box.y + offset.y(), shadow, layerType, color, backgroundColor, light, rightToLeft)
    }

    /**
     * 渲染对齐文本
     * @param text Text
     * @param box Box 需要对齐的[Box]
     * @param alignment [Alignment]
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param rightToLeft Boolean
     * @param defaultColor ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushAlignmentText(
        text: Text,
        box: Box,
        alignment: Alignment = Alignment.CenterLeft,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        defaultColor: ARGBColor = text.style.argbColor ?: Color(0xFF000000),
        backgroundColor: ARGBColor = Color(0),
        light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
    ) {
        val offset = alignment.align(box, text.size(textRenderer).toFloat())
        pushText(text, box.x + offset.x(), box.y + offset.y(), shadow, layerType, defaultColor, backgroundColor, light, rightToLeft)

    }


    /**
     * 渲染多行文本,会以换行符分'/n'割字符串
     * @param string String
     * @param box Box
     * @param horizontalAlignment Alignment.Horizontal
     * @param verticalArrangement Arrangement.Vertical
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param rightToLeft Boolean
     * @param defaultColor ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushStringLines(
        string: String,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Color(0),
        light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
    ) {
        val texts = string.wrapToLines(textRenderer, box.width.toInt())
        val verticalOffsets = verticalArrangement.arrange(box.width, List(texts.size) { textRenderer.fontHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, textRenderer.getWidth(it).toFloat()) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, shadow, layerType, defaultColor, backgroundColor, light, rightToLeft)
        }
    }

    /**
     * 渲染多行文本
     * @param lines List<String>
     * @param box Box
     * @param horizontalAlignment [Alignment.Horizontal]
     * @param verticalArrangement [Arrangement.Vertical]
     * @param shadow Boolean
     * @param layerType TextRenderer.TextLayerType
     * @param rightToLeft Boolean
     * @param defaultColor ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushStringLines(
        lines: List<String>,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
    ) {
        val texts = lines.wrapToLines(textRenderer, box.width.toInt())
        val verticalOffsets = verticalArrangement.arrange(box.width, List(texts.size) { textRenderer.fontHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, textRenderer.getWidth(it).toFloat()) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, shadow, layerType, defaultColor, backgroundColor, light, rightToLeft)
        }
    }

    /**
     * 绘制多行文本
     * @param text [Text]
     * @param box Rectangle
     * @param horizontalAlignment Alignment.Horizontal
     * @param verticalArrangement Arrangement.Vertical
     * @param shadow Boolean
     * @param layerType [TextLayerType]
     * @param rightToLeft Boolean
     * @param defaultColor ARGBColor
     * @param backgroundColor Color
     */
    fun pushTextLines(
        text: Text,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
    ) {
        val texts = text.wrapToTextLines(textRenderer, box.width.toInt())
        val verticalOffsets = verticalArrangement.arrange(box.width, List(texts.size) { textRenderer.fontHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, textRenderer.getWidth(it).toFloat()) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, shadow, layerType, defaultColor, backgroundColor, light, rightToLeft)
        }
    }

    /**
     * 绘制多行文本
     * @param lines List<[Text]>
     * @param box Rectangle
     * @param horizontalAlignment Alignment.Horizontal
     * @param verticalArrangement Arrangement.Vertical
     * @param shadow Boolean
     * @param layerType [TextLayerType]
     * @param rightToLeft Boolean
     * @param defaultColor ARGBColor
     * @param backgroundColor Color
     */
    fun pushTextLines(
        lines: List<Text>,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        layerType: TextLayerType = TextLayerType.NORMAL,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE,
        rightToLeft: Boolean = textRenderer.isRightToLeft,
    ) {
        val texts = lines.wrapToTextLines(textRenderer, box.width.toInt())
        val verticalOffsets = verticalArrangement.arrange(box.width, List(texts.size) { textRenderer.fontHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, textRenderer.getWidth(it).toFloat()) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, shadow, layerType, defaultColor, backgroundColor, light, rightToLeft)
        }
    }

}
