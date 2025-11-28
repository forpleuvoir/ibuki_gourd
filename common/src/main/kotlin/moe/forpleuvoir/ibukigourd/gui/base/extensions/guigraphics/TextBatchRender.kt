package moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.ibukigourd.text.style.argbColor
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import org.joml.Matrix4f

fun IGGuiGraphics.batchRenderText(
    font: Font = this.minecraft.font,
    block: TextBatchRenderScope.() -> Unit
) = font.batchRenderText(bufferSource, matrix4f, block)

fun Font.batchRenderText(
    bufferSource: MultiBufferSource.BufferSource,
    pose: Matrix4f,
    block: TextBatchRenderScope.() -> Unit
) {
    TextBatchRenderScope(this, bufferSource, pose).apply(block)
    bufferSource.endBatch()
}

fun Font.batchRenderText(
    bufferSource: MultiBufferSource,
    pose: Matrix4f,
    block: TextBatchRenderScope.() -> Unit
) {
    TextBatchRenderScope(this, bufferSource, pose).apply(block)
}

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
open class TextBatchRenderScope internal constructor(
    val font: Font,
    val bufferSource: MultiBufferSource,
    val pose: Matrix4f
) {

    /**
     * 渲染文本
     * @param text Text
     * @param x Float
     * @param y Float
     * @param shadow Boolean
     * @param displayMode Font.DisplayMode
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushText(
        text: Component,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
        color: ARGBColor = text.style.argbColor ?: Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightTexture.FULL_BRIGHT
    ) {
        font.drawInBatch(
            text,
            x,
            y,
            color.argb,
            shadow,
            pose,
            bufferSource,
            displayMode,
            backgroundColor.argb,
            light
        )
    }

    /**
     * 渲染有序文本
     * @param text FormattedCharSequence
     * @param x Float
     * @param y Float
     * @param shadow Boolean
     * @param displayMode Font.DisplayMode
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushText(
        text: FormattedCharSequence,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
        color: ARGBColor = Color(0xFF000000),
        backgroundColor: ARGBColor = Color(0),
        light: Int = LightTexture.FULL_BRIGHT
    ) {
        font.drawInBatch(
            text,
            x,
            y,
            color.argb,
            shadow,
            pose,
            bufferSource,
            displayMode,
            backgroundColor.argb,
            light
        )
    }

    /**
     * 渲染文本
     * @param text String
     * @param x Float
     * @param y Float
     * @param shadow Boolean
     * @param displayMode Font.DisplayMode
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushText(
        text: String,
        x: Float,
        y: Float,
        shadow: Boolean = false,
        displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
        color: ARGBColor = Color(0xFF000000),
        backgroundColor: ARGBColor = Color(0),
        light: Int = LightTexture.FULL_BRIGHT
    ) {
        font.drawInBatch(
            InlineStyleText(text),
            x,
            y,
            color.argb,
            shadow,
            pose,
            bufferSource,
            displayMode,
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
     * @param displayMode Font.DisplayMode
     * @param color ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushAlignmentText(
        text: String,
        box: Box,
        alignment: Alignment = Alignment.CenterLeft,
        shadow: Boolean = false,
        displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightTexture.FULL_BRIGHT
    ) {
        val offset = alignment.align(box, text.size)
        pushText(text, box.x + offset.x(), box.y + offset.y(), shadow, displayMode, color, backgroundColor, light)
    }

    /**
     * 渲染对齐文本
     * @param text Text
     * @param box Box 需要对齐的[Box]
     * @param alignment [Alignment]
     * @param shadow Boolean
     * @param displayMode Font.DisplayMode
     * @param defaultColor ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushAlignmentText(
        text: McText,
        box: Box,
        alignment: Alignment = Alignment.CenterLeft,
        shadow: Boolean = false,
        displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
        defaultColor: ARGBColor = text.style.argbColor ?: Color(0xFF000000),
        backgroundColor: ARGBColor = Color(0),
        light: Int = LightTexture.FULL_BRIGHT
    ) {
        val offset = alignment.align(box, text.size)
        pushText(text, box.x + offset.x(), box.y + offset.y(), shadow, displayMode, defaultColor, backgroundColor, light)

    }


    /**
     * 渲染多行文本,会以换行符分'/n'割字符串
     * @param string String
     * @param box Box
     * @param horizontalAlignment Alignment.Horizontal
     * @param verticalArrangement Arrangement.Vertical
     * @param shadow Boolean
     * @param displayMode Font.DisplayMode
     * @param defaultColor ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushStringLines(
        string: String,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Color(0),
        light: Int = LightTexture.FULL_BRIGHT
    ) {
        val texts = string.wrapToLines(box.width)
        val verticalOffsets = verticalArrangement.arrange(box.width, List(texts.size) { font.lineHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, it.width) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, shadow, displayMode, defaultColor, backgroundColor, light)
        }
    }

    /**
     * 渲染多行文本
     * @param lines List<String>
     * @param box Box
     * @param horizontalAlignment [Alignment.Horizontal]
     * @param verticalArrangement [Arrangement.Vertical]
     * @param shadow Boolean
     * @param displayMode Font.DisplayMode
     * @param defaultColor ARGBColor
     * @param backgroundColor ARGBColor
     */
    fun pushStringLines(
        lines: List<String>,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightTexture.FULL_BRIGHT
    ) {
        val texts = lines.wrapToLines(box.width)
        val verticalOffsets = verticalArrangement.arrange(box.width, List(texts.size) { font.lineHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, it.width) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, shadow, displayMode, defaultColor, backgroundColor, light)
        }
    }

    /**
     * 绘制多行文本
     * @param text [buildText]
     * @param box Rectangle
     * @param horizontalAlignment Alignment.Horizontal
     * @param verticalArrangement Arrangement.Vertical
     * @param shadow Boolean
     * @param displayMode [Font.DisplayMode]
     * @param defaultColor ARGBColor
     * @param backgroundColor Color
     */
    fun pushTextLines(
        text: McText,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightTexture.FULL_BRIGHT
    ) {
        val texts = text.wrapToTextLines(box.width)
        val verticalOffsets = verticalArrangement.arrange(box.height, List(texts.size) { font.lineHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, it.width) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, shadow, displayMode, defaultColor, backgroundColor, light)
        }
    }

    /**
     * 绘制多行文本
     * @param lines List<[buildText]>
     * @param box Rectangle
     * @param horizontalAlignment Alignment.Horizontal
     * @param verticalArrangement Arrangement.Vertical
     * @param shadow Boolean
     * @param displayMode [Font.DisplayMode]
     * @param defaultColor ARGBColor
     * @param backgroundColor Color
     */
    fun pushTextLines(
        lines: List<McText>,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        light: Int = LightTexture.FULL_BRIGHT
    ) {
        val texts = lines.wrapToTextLines(box.width)
        val verticalOffsets = verticalArrangement.arrange(box.height, List(texts.size) { font.lineHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, it.width) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, shadow, displayMode, defaultColor, backgroundColor, light)
        }
    }

}
