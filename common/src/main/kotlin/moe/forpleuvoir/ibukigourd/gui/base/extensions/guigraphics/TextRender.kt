package moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.McText
import moe.forpleuvoir.ibukigourd.text.size
import moe.forpleuvoir.ibukigourd.util.math.Vector3f
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.util.FormattedCharSequence
import org.joml.Matrix4f
import org.joml.Vector3fc


val textRenderOffset: Vector3fc by lazy {
    Vector3f(0.0f, 0.4f, 0f)
//    ModernUICompat.textEngineEnabled(
//        Vector3f(0.0f, 0.0f, 0f), Vector3f(0.0f, 0.4f, 0f)
//    )
}


/**
 * 渲染文本
 * @receiver [IGGuiGraphics]
 * @param text McText
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param displayMode Font.TextLayerType
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun IGGuiGraphics.renderText(
    text: McText,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
    color: ARGBColor = Color(text.style.color?.value?.toLong() ?: 0xFF000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    font: Font = this.font,
    light: Int = LightTexture.FULL_BRIGHT,
) = font.renderText(bufferSource, matrix4f, text, x, y, shadow, displayMode, color, backgroundColor, light)

/**
 * 渲染文本
 * @receiver DrawContext
 * @param text McText
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param displayMode Font.DisplayMode
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun Font.renderText(
    bufferSource: MultiBufferSource.BufferSource,
    pose: Matrix4f,
    text: McText,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
    color: ARGBColor = Color(text.style.color?.value?.toLong() ?: 0xFF000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    light: Int = LightTexture.FULL_BRIGHT
) {
    drawInBatch(
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
    bufferSource.endBatch()
}


/**
 * 渲染有序文本
 * @receiver DrawContext
 * @param text FormattedCharSequence
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param displayMode Font.DisplayMode
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun IGGuiGraphics.renderText(
    text: FormattedCharSequence,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Color(0),
    font: Font = this.font,
    light: Int = LightTexture.FULL_BRIGHT
) = font.renderText(bufferSource, matrix4f, text, x, y, shadow, displayMode, color, backgroundColor, light)

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
fun Font.renderText(
    bufferSource: MultiBufferSource.BufferSource,
    pose: Matrix4f,
    text: FormattedCharSequence,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Color(0),
    light: Int = LightTexture.FULL_BRIGHT
) {
    drawInBatch(
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
    bufferSource.endBatch()
}


/**
 * 渲染文本
 * @receiver DrawContext
 * @param text String
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param displayMode Font.DisplayMode
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun IGGuiGraphics.renderText(
    text: String,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Color(0),
    font: Font = this.font,
    light: Int = LightTexture.FULL_BRIGHT
) = font.renderText(bufferSource, matrix4f, text, x, y, shadow, displayMode, color, backgroundColor, light)

/**
 * 渲染文本
 * @receiver DrawContext
 * @param text String
 * @param x Float
 * @param y Float
 * @param shadow Boolean
 * @param displayMode Font.DisplayMode
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun Font.renderText(
    bufferSource: MultiBufferSource.BufferSource,
    pose: Matrix4f,
    text: String,
    x: Float,
    y: Float,
    shadow: Boolean = false,
    displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Color(0),
    light: Int = LightTexture.FULL_BRIGHT
) {
    drawInBatch(
        InlineStyleText(text),
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
    bufferSource.endBatch()
}

/**
 * 渲染对齐文本
 * @receiver DrawContext
 * @param text String
 * @param box Box 需要对齐的[Box]
 * @param alignment ([Orientation]) -> [Alignment] 对齐方式
 * @param shadow Boolean
 * @param displayMode Font.DisplayMode
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun IGGuiGraphics.renderAlignmentText(
    text: String,
    box: Box,
    alignment: Alignment = Alignment.Center,
    shadow: Boolean = false,
    displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    font: Font = this.font,
    light: Int = LightTexture.FULL_BRIGHT
) {
    alignment.align(box, text.size).apply {
        renderText(text, box.x + x(), box.y + y(), shadow, displayMode, color, backgroundColor, font, light)
    }
}

/**
 * 渲染对齐文本
 * @receiver DrawContext
 * @param text String
 * @param box Box 需要对齐的[Box]
 * @param alignment ([Orientation]) -> [Alignment] 对齐方式
 * @param shadow Boolean
 * @param displayMode Font.DisplayMode
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun Font.renderAlignmentText(
    bufferSource: MultiBufferSource.BufferSource,
    pose: Matrix4f,
    text: String,
    box: Box,
    alignment: Alignment = Alignment.Center,
    shadow: Boolean = false,
    displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
    color: ARGBColor = Color(0xFF000000),
    backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
    light: Int = LightTexture.FULL_BRIGHT,
) {
    alignment.align(box, text.size).apply {
        renderText(bufferSource, pose, text, box.x + x(), box.y + y(), shadow, displayMode, color, backgroundColor, light)
    }
}

/**
 * 渲染对齐文本
 * @receiver DrawContext
 * @param text McText
 * @param box Box 需要对齐的[Box]
 * @param alignment ([Orientation]) -> [Alignment] 对齐方式
 * @param shadow Boolean
 * @param displayMode Font.DisplayMode
 * @param color ARGBColor
 * @param backgroundColor ARGBColor
 */
fun IGGuiGraphics.renderAlignmentText(
    text: McText,
    box: Box,
    alignment: Alignment = Alignment.Center,
    shadow: Boolean = false,
    displayMode: Font.DisplayMode = Font.DisplayMode.NORMAL,
    color: ARGBColor = Color(text.style.color?.value?.toLong() ?: 0xFF000000),
    backgroundColor: ARGBColor = Color(0),
    font: Font = this.font,
    light: Int = LightTexture.FULL_BRIGHT,
) {
    alignment.align(box, text.size).apply {
        renderText(text, box.x + x(), box.y + y(), shadow, displayMode, color, backgroundColor, font, light)
    }
}
