package moe.forpleuvoir.ibukigourd.render.extension

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.mojang.blaze3d.pipeline.RenderPipeline
import moe.forpleuvoir.ibukigourd.render.IGRenderPipelines
import moe.forpleuvoir.ibukigourd.render.extension.state.ColoredBoxRenderState
import moe.forpleuvoir.ibukigourd.render.peekScissorRect
import moe.forpleuvoir.ibukigourd.render.renderState
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.RenderPipelines
import org.joml.Matrix3x2f
import kotlin.math.abs
import kotlin.math.min

fun GuiGraphicsExtractor.pushRect(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    /**
     * TOP LEFT
     */
    col1: Color,
    /**
     * BOTTOM LEFT
     */
    col2: Color,
    /**
     * BOTTOM RIGHT
     */
    col3: Color,
    /**
     * TOP RIGHT
     */
    col4: Color,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    pipeline: RenderPipeline = RenderPipelines.GUI,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = renderState.addGuiElement(
    ColoredBoxRenderState(
        x0, y0, x1, y1,
        col1, col2, col3, col4,
        pose, pipeline,
        scissorArea
    )
)

fun GuiGraphicsExtractor.pushRect(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: Color,
    pipeline: RenderPipeline = RenderPipelines.GUI,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = pushRect(x, y, x + width, y + height, color, color, color, color, Matrix3x2f(pose()), pipeline, scissorArea)

fun GuiGraphicsExtractor.pushRect(rect: Rect, color: Color, pipeline: RenderPipeline = RenderPipelines.GUI, scissorArea: ScreenRectangle? = peekScissorRect()) {
    pushRect(rect.left, rect.top, rect.right, rect.bottom, color, color, color, color, Matrix3x2f(pose()), pipeline, scissorArea)
}

fun GuiGraphicsExtractor.pushRectOutline(
    rect: Rect,
    color: Color,
    borderSize: Float = 1f,
    inner: Boolean = false,
    pipeline: RenderPipeline = RenderPipelines.GUI,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    check(borderSize > 0) { "borderSize must be greater than 0" }
    val x = rect.left
    val y = rect.top
    val width = rect.width
    val height = rect.height
    if (inner) {
        //top
        pushRect(x = x, y = y, width = width - borderSize, height = borderSize, color = color, pipeline, scissorArea)
        //right
        pushRect(x = x + width - borderSize, y = y, width = borderSize, height = height - borderSize, color = color, pipeline, scissorArea)
        //bottom
        pushRect(x = x + borderSize, y = y + height - borderSize, width = width - borderSize, height = borderSize, color = color, pipeline, scissorArea)
        //left
        pushRect(x = x, y = y + borderSize, width = borderSize, height = height - borderSize, color = color, pipeline, scissorArea)
    } else {
        //top
        pushRect(x = x - borderSize, y = y - borderSize, width = width + borderSize, height = borderSize, color = color, pipeline, scissorArea)
        //right
        pushRect(x = x + width, y = y - borderSize, width = borderSize, height = height + borderSize, color = color, pipeline, scissorArea)
        //bottom
        pushRect(x = x, y = y + height, width = width + borderSize, height = borderSize, color = color, pipeline, scissorArea)
        //left
        pushRect(x = x - borderSize, y = y, width = borderSize, height = height + borderSize, color = color, pipeline, scissorArea)
    }
}

fun GuiGraphicsExtractor.pushGradientRect(
    rect: Rect,
    color1: Color,
    color2: Color,
    orientation: Orientation,
    pipeline: RenderPipeline = RenderPipelines.GUI,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    val state = if (orientation == Orientation.Vertical) {
        ColoredBoxRenderState.vertical(
            rect.left, rect.top, rect.right, rect.bottom,
            color1, color2, Matrix3x2f(pose()), pipeline,
            scissorArea
        )
    } else {
        ColoredBoxRenderState.horizontal(
            rect.left, rect.top, rect.right, rect.bottom,
            color1, color2, Matrix3x2f(pose()), pipeline,
            scissorArea
        )
    }
    renderState.addGuiElement(state)
}

fun GuiGraphicsExtractor.pushHueGradientRect(
    rect: Rect,
    orientation: Orientation = Orientation.Horizontal,
    inverse: Boolean = false,
    hueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    saturation: Float = 1f,
    value: Float = 1f,
    alpha: Float = 1f,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    check(hueRange.endInclusive >= hueRange.start) { "Hue range must be in ascending order" }
    check(hueRange.endInclusive in 0f..1f && hueRange.start in 0f..1f) {
        "Hue range must be between 0 and 1,but was ${hueRange.start} and ${hueRange.endInclusive}"
    }
    val colorStart = Color.fromARGB((if (inverse) hueRange.endInclusive else hueRange.start).coerceIn(0f..1f), saturation, value, alpha)
    val colorEnd = Color.fromARGB((if (!inverse) hueRange.endInclusive else hueRange.start).coerceIn(0f..1f), saturation, value, alpha)
    pushGradientRect(rect, colorStart, colorEnd, orientation, IGRenderPipelines.GUI_HSV_COLOR, scissorArea)
}

fun GuiGraphicsExtractor.pushSaturationGradientRect(
    rect: Rect,
    orientation: Orientation = Orientation.Horizontal,
    inverse: Boolean = false,
    saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 1f,
    value: Float = 1f,
    alpha: Float = 1f,
    pipeline: RenderPipeline = RenderPipelines.GUI,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    check(saturationRange.endInclusive >= saturationRange.start) { "Saturation range must be in ascending order" }
    check(saturationRange.endInclusive in 0f..1f && saturationRange.start in 0f..1f) {
        "Saturation range must be between 0 and 1,but was ${saturationRange.start} and ${saturationRange.endInclusive}"
    }
    val colorStart = Color.fromHSV(hue, (if (inverse) saturationRange.endInclusive else saturationRange.start).coerceIn(0f..1f), value, alpha)
    val colorEnd = Color.fromHSV(hue, (if (!inverse) saturationRange.endInclusive else saturationRange.start).coerceIn(0f..1f), value, alpha)
    pushGradientRect(rect, colorStart, colorEnd, orientation, pipeline, scissorArea)
}

fun GuiGraphicsExtractor.pushValueGradientRect(
    rect: Rect,
    orientation: Orientation = Orientation.Horizontal,
    inverse: Boolean = false,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 1f,
    saturation: Float = 1f,
    alpha: Float = 1f,
    pipeline: RenderPipeline = RenderPipelines.GUI,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    check(valueRange.endInclusive >= valueRange.start) { "Value range must be in ascending order" }
    check(valueRange.endInclusive in 0f..1f && valueRange.start in 0f..1f) {
        "Value range must be between 0 and 1,but was ${valueRange.start} and ${valueRange.endInclusive}"
    }
    val colorStart = Color.fromHSV(hue, saturation, (if (inverse) valueRange.endInclusive else valueRange.start).coerceIn(0f..1f), alpha)
    val colorEnd = Color.fromHSV(hue, saturation, (if (!inverse) valueRange.endInclusive else valueRange.start).coerceIn(0f..1f), alpha)
    pushGradientRect(rect, colorStart, colorEnd, orientation, pipeline, scissorArea)
}


fun GuiGraphicsExtractor.pushTextHighLightRect(rect: Rect, invertColor: Color = Colors.WHITE, highLightColor: Color = Colors.BLUE) {
    pushRect(rect, invertColor, RenderPipelines.GUI_INVERT)
    pushRect(rect, highLightColor, RenderPipelines.GUI_TEXT_HIGHLIGHT)
}


fun GuiGraphicsExtractor.pushRoundRect(
    rect: Rect,
    color: Color,
    round: Int,
    pixelSize: Float = 1f,
    pipeline: RenderPipeline = RenderPipelines.GUI,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    if (round > 0) {
        pushRect(
            Rect(rect.topLeft + Offset(0f, (round + 1) * pixelSize), Size(rect.width, rect.height - ((round + 1) * pixelSize) * 2)),
            color,
            pipeline,
            scissorArea
        )
        roundRectCache[RoundRect(round, pixelSize, rect.width, rect.height)]?.let {
            it.forEach { (position, size) -> pushRect(Rect(rect.topLeft + position, size), color, pipeline, scissorArea) }
            return
        }
        val yPoints = mutableMapOf<Int, Int>()
        val xPoints = mutableMapOf<Int, Pair<Int, Int>>()
        pointsInCircleRange(round, round, round, 180.0..270.0).forEach { point ->
            yPoints[point.y] = yPoints[point.y]?.let { min(it, point.x) } ?: point.x
            xPoints[point.x] = xPoints[point.x]?.let { min(it.first, point.y) to it.second + 1 } ?: (point.y to 1)
        }
        buildSet {
            yPoints.map { (_, x) -> x to xPoints[x] }
                .toSet().forEach { (x, y) ->
                    add(
                        Offset(abs(x * pixelSize), abs(y!!.first) * pixelSize) to
                                Size(rect.width - abs(x * pixelSize * 2), y.second * pixelSize)
                    )
                    add(
                        Offset(abs(x * pixelSize), rect.height - y.second * pixelSize - (abs(y.first)) * pixelSize) to
                                Size(rect.width - abs(x * pixelSize * 2), y.second * pixelSize)
                    )
                }
            roundRectCache[RoundRect(round, pixelSize, rect.width, rect.height)] = this
            if (size > roundBoxCacheSize) roundRectCache.remove(roundRectCache.keys.first())
        }.forEach { (position, size) -> pushRect(Rect(rect.topLeft + position, size), color, pipeline, scissorArea) }
    } else pushRect(rect, color, pipeline, scissorArea)
}


internal data class RoundRect(
    val round: Int,
    val pixelSize: Float,
    val width: Float,
    val height: Float
)

internal const val roundBoxCacheSize = 50

internal val roundRectCache = LinkedHashMap<RoundRect, Set<Pair<Offset, Size>>>(roundBoxCacheSize)