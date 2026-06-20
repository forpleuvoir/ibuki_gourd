package moe.forpleuvoir.ibukigourd.render.extension

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.mojang.blaze3d.pipeline.RenderPipeline
import moe.forpleuvoir.ibukigourd.render.extension.state.IGBlitRenderState
import moe.forpleuvoir.ibukigourd.render.extension.state.IGTiledBlitRenderState
import moe.forpleuvoir.ibukigourd.render.extension.texture.IGTexture
import moe.forpleuvoir.ibukigourd.render.peekScissorRect
import moe.forpleuvoir.ibukigourd.render.renderState
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import org.joml.Matrix3x2f
import kotlin.math.absoluteValue


fun GuiGraphicsExtractor.pushBlit(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    u0: Float,
    v0: Float,
    u1: Float,
    v1: Float,
    textureSetup: TextureSetup,
    color: Color,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    pipeline: RenderPipeline,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = if (width + height > 0f) {
    renderState.addGuiElement(
        IGBlitRenderState(
            x, y, x + width, y + height,
            u0, v0, u1, v1,
            color, pose, pipeline, textureSetup, scissorArea
        )
    )
} else Unit

fun GuiGraphicsExtractor.pushBlit(
    area: Rect,
    texture: IGTexture,
    color: Color = Colors.WHITE,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = pushBlit(
    area.left, area.top, area.width, area.height,
    texture.u0, texture.v0, texture.u1, texture.v1,
    texture.textureSetup, color, pose, pipeline, scissorArea
)

fun GuiGraphicsExtractor.pushNineSlicedBlit(
    area: Rect,
    widgetTexture: IGTexture,
    color: Color = Colors.WHITE,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    if (area.width <= 0 || area.height <= 0) return

    val corner = widgetTexture.corner
    if (!widgetTexture.corner.isSpecified) {
        pushBlit(area, widgetTexture, color, pose, pipeline, scissorArea)
        return
    }

    val texture = widgetTexture.textureSetup
    val x = area.left
    val y = area.top
    val width = area.width
    val height = area.height
    val u0 = widgetTexture.uStart
    val v0 = widgetTexture.vStart
    val u1 = widgetTexture.uSize
    val v1 = widgetTexture.vSize
    val tw = widgetTexture.textureInfo.width
    val th = widgetTexture.textureInfo.height

    //corner.left
    val cl = corner.left.absoluteValue.toFloat()
    //corner.right
    val cr = corner.right.absoluteValue.toFloat()
    //corner.top
    val ct = corner.top.absoluteValue.toFloat()
    //corner.bottom
    val cb = corner.bottom.absoluteValue.toFloat()

    /**
     * centerWidth
     */
    val cw = width - (corner.left.coerceAtLeast(0) + corner.right.coerceAtLeast(0))

    /**
     * centerHeight
     */
    val ch = height - (corner.top.coerceAtLeast(0) + corner.bottom.coerceAtLeast(0))

    val leftX = if (corner.left >= 0) x else x - cl
    val centerX = if (corner.left >= 0) x + cl else x
    val rightX = if (corner.right >= 0) x + (width - corner.right) else x + width

    val topY = if (corner.top >= 0) y else y - ct
    val centerY = if (corner.top >= 0) y + ct else y
    val bottomY = if (corner.bottom >= 0) y + (height - corner.bottom) else y + height

    val leftU = if (corner.left >= 0) u0 else u0 - cl.toInt()
    val centerU = if (corner.left >= 0) u0 + cl.toInt() else u0
    val rightU = if (corner.right >= 0) u0 + (u1 - cr.toInt()) else u0 + u1

    val topV = if (corner.top >= 0) v0 else v0 - ct.toInt()
    val centerV = if (corner.top >= 0) v0 + ct.toInt() else v0
    val bottomV = if (corner.bottom >= 0) v0 + (v1 - cb.toInt()) else v0 + v1

    val leftUS = cl.toInt()
    val centerUS = u1 - (corner.left.coerceAtLeast(0) + corner.right.coerceAtLeast(0))
    val rightUS = cr.toInt()

    val topVS = ct.toInt()
    val centerVS = v1 - (corner.top.coerceAtLeast(0) + corner.bottom.coerceAtLeast(0))
    val bottomVS = cb.toInt()
    //top left
    nineSlicedSegment(pose, leftX, topY, cl, ct, leftU, topV, leftUS, topVS, color, tw, th, texture, pipeline, scissorArea)
    //top center
    nineSlicedSegment(pose, centerX, topY, cw, ct, centerU, topV, centerUS, topVS, color, tw, th, texture, pipeline, scissorArea)
    //top right
    nineSlicedSegment(pose, rightX, topY, cr, ct, rightU, topV, rightUS, topVS, color, tw, th, texture, pipeline, scissorArea)

    //center left
    nineSlicedSegment(pose, leftX, centerY, cl, ch, leftU, centerV, leftUS, centerVS, color, tw, th, texture, pipeline, scissorArea)
    //center
    nineSlicedSegment(pose, centerX, centerY, cw, ch, centerU, centerV, centerUS, centerVS, color, tw, th, texture, pipeline, scissorArea)
    //center right
    nineSlicedSegment(pose, rightX, centerY, cr, ch, rightU, centerV, rightUS, centerVS, color, tw, th, texture, pipeline, scissorArea)

    //bottom left
    nineSlicedSegment(pose, leftX, bottomY, cl, cb, leftU, bottomV, leftUS, bottomVS, color, tw, th, texture, pipeline, scissorArea)
    //bottom center
    nineSlicedSegment(pose, centerX, bottomY, cw, cb, centerU, bottomV, centerUS, bottomVS, color, tw, th, texture, pipeline, scissorArea)
    //bottom right
    nineSlicedSegment(pose, rightX, bottomY, cr, cb, rightU, bottomV, rightUS, bottomVS, color, tw, th, texture, pipeline, scissorArea)
}


private fun GuiGraphicsExtractor.nineSlicedSegment(
    pose: Matrix3x2f,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    u: Int,
    v: Int,
    uSize: Int,
    vSize: Int,
    color: Color = Colors.WHITE,
    textureWidth: Int,
    textureHeight: Int,
    textureSetup: TextureSetup,
    pipeline: RenderPipeline,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    val u0 = u.toFloat() / textureWidth.toFloat()
    val v0 = v.toFloat() / textureHeight.toFloat()
    val u1 = (u + uSize).toFloat() / textureWidth.toFloat()
    val v1 = (v + vSize).toFloat() / textureHeight.toFloat()
    if (width <= 0f || height <= 0f || color.alpha == 0) return
    renderState.addGuiElement(
        IGBlitRenderState(
            x, y, x + width, y + height,
            u0, v0, u1, v1,
            color, pose, pipeline, textureSetup, scissorArea
        )
    )
}

@JvmInline
value class AnchorPosition private constructor(private val value: Int) {
    override fun toString(): String {
        return when (this) {
            Above -> "Above"
            Below -> "Below"
            Left  -> "Left"
            Right -> "Right"
            else  -> "Invalid"
        }
    }

    companion object {
        val Above = AnchorPosition(1)

        val Below = AnchorPosition(2)

        val Left = AnchorPosition(3)

        val Right = AnchorPosition(4)
    }
}

fun GuiGraphicsExtractor.pushSpeechBubbleTexture(
    bubbleArea: Rect,
    bubbleTexture: IGTexture,
    arrowArea: Rect,
    arrowTexture: IGTexture,
    /**
     * 箭头所在的方向
     */
    arrowAnchorPosition: AnchorPosition,
    color: Color = Colors.WHITE,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
    scissorArea: ScreenRectangle? = peekScissorRect()
) {
    pushWidgetTexture(arrowArea, arrowTexture, color, pose, pipeline, scissorArea)
    if (bubbleArea.width <= 0 || bubbleArea.height <= 0) return
    val corner = bubbleTexture.corner
    if (!bubbleTexture.corner.isSpecified) {
        pushBlit(bubbleArea, bubbleTexture, color, pose, pipeline, scissorArea)
        return
    }

    val aw = arrowArea.width
    val ah = arrowArea.height
    val ax = arrowArea.left
    val ax2 = arrowArea.right
    val ay = arrowArea.top
    val ay2 = arrowArea.bottom

    val texture = bubbleTexture.textureSetup
    val x = bubbleArea.left
    val y = bubbleArea.top
    val width = bubbleArea.width
    val height = bubbleArea.height
    val u0 = bubbleTexture.uStart
    val v0 = bubbleTexture.vStart
    val u1 = bubbleTexture.uSize
    val v1 = bubbleTexture.vSize
    val tw = bubbleTexture.textureInfo.width
    val th = bubbleTexture.textureInfo.height

    //corner.left
    val cl = corner.left.absoluteValue.toFloat()
    //corner.right
    val cr = corner.right.absoluteValue.toFloat()
    //corner.top
    val ct = corner.top.absoluteValue.toFloat()
    //corner.bottom
    val cb = corner.bottom.absoluteValue.toFloat()

    /**
     * centerWidth
     */
    val cw = width - (corner.left.coerceAtLeast(0) + corner.right.coerceAtLeast(0))

    /**
     * centerHeight
     */
    val ch = height - (corner.top.coerceAtLeast(0) + corner.bottom.coerceAtLeast(0))

    val leftX = if (corner.left >= 0) x else x - cl
    val centerX = if (corner.left >= 0) x + cl else x
    val rightX = if (corner.right >= 0) x + (width - corner.right) else x + width

    val topY = if (corner.top >= 0) y else y - ct
    val centerY = if (corner.top >= 0) y + ct else y
    val bottomY = if (corner.bottom >= 0) y + (height - corner.bottom) else y + height

    val leftU = if (corner.left >= 0) u0 else u0 - cl.toInt()
    val centerU = if (corner.left >= 0) u0 + cl.toInt() else u0
    val rightU = if (corner.right >= 0) u0 + (u1 - cr.toInt()) else u0 + u1

    val topV = if (corner.top >= 0) v0 else v0 - ct.toInt()
    val centerV = if (corner.top >= 0) v0 + ct.toInt() else v0
    val bottomV = if (corner.bottom >= 0) v0 + (v1 - cb.toInt()) else v0 + v1

    val leftUS = cl.toInt()
    val centerUS = u1 - (corner.left.coerceAtLeast(0) + corner.right.coerceAtLeast(0))
    val rightUS = cr.toInt()

    val topVS = ct.toInt()
    val centerVS = v1 - (corner.top.coerceAtLeast(0) + corner.bottom.coerceAtLeast(0))
    val bottomVS = cb.toInt()

    //top left
    nineSlicedSegment(pose, leftX, topY, cl, ct, leftU, topV, leftUS, topVS, color, tw, th, texture, pipeline, scissorArea)
    //top center
    if (arrowAnchorPosition == AnchorPosition.Above) {
        if (cw - aw > 0) {
            nineSlicedSegment(pose, centerX, topY, ax - centerX, ct, centerU, topV, centerUS, topVS, color, tw, th, texture, pipeline, scissorArea)
            nineSlicedSegment(pose, ax2, topY, rightX - ax2, ct, centerU, topV, centerUS, topVS, color, tw, th, texture, pipeline, scissorArea)
        }
    } else {
        nineSlicedSegment(pose, centerX, topY, cw, ct, centerU, topV, centerUS, topVS, color, tw, th, texture, pipeline, scissorArea)
    }
    //top right
    nineSlicedSegment(pose, rightX, topY, cr, ct, rightU, topV, rightUS, topVS, color, tw, th, texture, pipeline, scissorArea)

    //center left
    if (arrowAnchorPosition == AnchorPosition.Left) {
        if (ch - ah > 0) {
            nineSlicedSegment(pose, leftX, centerY, cl, ay - centerY, leftU, centerV, leftUS, centerVS, color, tw, th, texture, pipeline, scissorArea)
            nineSlicedSegment(pose, leftX, ay2, cl, bottomY - ay2, leftU, centerV, leftUS, centerVS, color, tw, th, texture, pipeline, scissorArea)
        }
    } else {
        nineSlicedSegment(pose, leftX, centerY, cl, ch, leftU, centerV, leftUS, centerVS, color, tw, th, texture, pipeline, scissorArea)
    }
    //center
    nineSlicedSegment(pose, centerX, centerY, cw, ch, centerU, centerV, centerUS, centerVS, color, tw, th, texture, pipeline, scissorArea)
    //center right
    if (arrowAnchorPosition == AnchorPosition.Right) {
        if (ch - ah > 0) {
            nineSlicedSegment(pose, rightX, centerY, cr, ay - centerY, rightU, centerV, rightUS, centerVS, color, tw, th, texture, pipeline, scissorArea)
            nineSlicedSegment(pose, rightX, ay2, cr, bottomY - ay2, rightU, centerV, rightUS, centerVS, color, tw, th, texture, pipeline, scissorArea)
        }
    } else {
        nineSlicedSegment(pose, rightX, centerY, cr, ch, rightU, centerV, rightUS, centerVS, color, tw, th, texture, pipeline, scissorArea)
    }

    //bottom left
    nineSlicedSegment(pose, leftX, bottomY, cl, cb, leftU, bottomV, leftUS, bottomVS, color, tw, th, texture, pipeline, scissorArea)
    //bottom center
    if (arrowAnchorPosition == AnchorPosition.Below) {
        if (cw - aw > 0) {
            nineSlicedSegment(pose, centerX, bottomY, ax - centerX, cb, centerU, bottomV, centerUS, bottomVS, color, tw, th, texture, pipeline, scissorArea)
            nineSlicedSegment(pose, ax2, bottomY, rightX - ax2, cb, centerU, bottomV, centerUS, bottomVS, color, tw, th, texture, pipeline, scissorArea)
        }
    } else {
        nineSlicedSegment(pose, centerX, bottomY, cw, cb, centerU, bottomV, centerUS, bottomVS, color, tw, th, texture, pipeline, scissorArea)
    }
    //bottom right
    nineSlicedSegment(pose, rightX, bottomY, cr, cb, rightU, bottomV, rightUS, bottomVS, color, tw, th, texture, pipeline, scissorArea)
}

fun GuiGraphicsExtractor.pushWidgetTexture(
    area: Rect,
    texture: IGTexture,
    color: Color = Colors.WHITE,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = pushNineSlicedBlit(area, texture, color, pose, pipeline, scissorArea)

fun GuiGraphicsExtractor.pushTiledBlit(
    area: Rect,
    texture: IGTexture,
    tileSize: Size = Size(texture.uSize.toFloat(), texture.vSize.toFloat()),
    color: Color = Colors.WHITE,
    pose: Matrix3x2f = Matrix3x2f(pose()),
    pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
    scissorArea: ScreenRectangle? = peekScissorRect()
) = renderState.addGuiElement(
    IGTiledBlitRenderState(
        area.left, area.right, area.right, area.bottom,
        texture.u0, texture.v0, texture.u1, texture.v1,
        tileSize.width, tileSize.height,
        color, pose, pipeline, texture.textureSetup, scissorArea
    )
)

