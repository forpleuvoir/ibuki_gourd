package moe.forpleuvoir.ibukigourd.gui.base.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.RoundBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.roundBoxCache
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.roundBoxCacheSize
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.useMatrixStack
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.pointsInCircleRange
import moe.forpleuvoir.ibukigourd.gui.base.render.state.ColoredBoxRenderState
import moe.forpleuvoir.ibukigourd.gui.base.render.state.IGBlitRenderState
import moe.forpleuvoir.ibukigourd.gui.base.render.state.IGGuiTextRenderState
import moe.forpleuvoir.ibukigourd.gui.base.render.state.IGTiledBlitRenderState
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.render.IGRenderPipelines
import moe.forpleuvoir.ibukigourd.text.size
import moe.forpleuvoir.ibukigourd.text.width
import moe.forpleuvoir.ibukigourd.text.wrapToLines
import moe.forpleuvoir.ibukigourd.text.wrapToTextLines
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.math.plus
import moe.forpleuvoir.nebula.common.color.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.state.GuiRenderState
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.min

class IGGuiGraphics(
    client: Minecraft,
    pose: Matrix3x2fStack,
    guiRenderState: GuiRenderState
) : GuiGraphics(client, pose, guiRenderState) {

    companion object {
        fun GuiGraphics.toIGGUIGraphics(): IGGuiGraphics =
            this as? IGGuiGraphics ?: IGGuiGraphics(this.minecraft, this.pose(), this.guiRenderState)

        private val transparent = Color(0, 0, 0, 0)
    }

    private val endRenderableList: MutableList<Pair<Int, IGGuiGraphics.() -> Unit>> = mutableListOf()

    val font: Font = client.font

    private var endRendering: Boolean = false

//    val igScissorStack: IGScissorStack = IGScissorStack()

    fun postEndRender(renderPriority: Int, render: IGGuiGraphics.() -> Unit) {
        if (endRendering) return
        endRenderableList.add(renderPriority to render)
    }

    fun renderEndRenderable() {
        if (endRenderableList.isEmpty()) return
        endRendering = true
        endRenderableList.sortedBy { it.first }.forEach { (_, render) ->
            render.invoke(this)
        }
        endRendering = false
    }

    inline operator fun invoke(block: IGGuiGraphics.() -> Unit) = block()

    /**
     * 原来的RenderSystem.setShaderColor没了,感觉现在原版的colorModulator有点麻烦,所以自己实现一个.CPU实现,效率可能不高
     */
    val colorModulator: Color = Color(1f, 1f, 1f, 1f)

    @OptIn(ExperimentalContracts::class)
    inline fun modulateColor(color: ARGBColor, block: IGGuiGraphics.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val old = colorModulator.argb
        colorModulator.argb = color.argb
        block()
        colorModulator.argb = old
    }

    private fun applyModulatedColor(color: ARGBColor): ARGBColor {
        return if (colorModulator.argb == -1) color
        else if (colorModulator.alpha == 0) transparent
        else color * colorModulator
    }

    fun peekScissorBox() = scissorStack.peek()?.let { Box(it.position.x, it.position.y, it.width, it.height) }

    /**
     * 原版的绘制坐标全是Int,现在只是换成Float重新实现一遍
     */

    //------------ Blit ------------\\

    fun pushBlit(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        u0: Float,
        v0: Float,
        u1: Float,
        v1: Float,
        textureSetup: TextureSetup,
        color: ARGBColor,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        pipeline: RenderPipeline,
        scissorBox: Box? = peekScissorBox()
    ) = guiRenderState.submitGuiElement(
        IGBlitRenderState(
            x, y, x + width, y + height,
            u0, v0, u1, v1,
            applyModulatedColor(color), pose, pipeline, textureSetup, scissorBox?.asScreenRectangle
        )
    )

    fun pushBlit(
        box: Box,
        widgetTexture: WidgetTexture,
        color: ARGBColor = Colors.WHITE,
        pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
        scissorBox: Box? = peekScissorBox()
    ) = pushBlit(
        box.x, box.y, box.width, box.height,
        widgetTexture.u0, widgetTexture.v0, widgetTexture.u1, widgetTexture.v1,
        widgetTexture.textureSetup, color, Matrix3x2f(pose()), pipeline, scissorBox
    )

    fun pushBlit(
        transform: Transform,
        widgetTexture: WidgetTexture,
        color: ARGBColor = Colors.WHITE,
        pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
        scissorBox: Box? = peekScissorBox()
    ) = pushBlit(transform.asWorldCoordinateBox, widgetTexture, color, pipeline, scissorBox)


    fun pushNineSlicedBlit(
        box: Box,
        widgetTexture: WidgetTexture,
        color: ARGBColor = Colors.WHITE,
        pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
        scissorBox: Box? = peekScissorBox()
    ) {
        val corner = widgetTexture.corner
        if (!widgetTexture.corner.isSpecified) {
            pushBlit(box, widgetTexture, color, pipeline, scissorBox)
            return
        }

        val pose = Matrix3x2f(pose())
        val textureSetup = widgetTexture.textureSetup
        val x = box.x
        val y = box.y
        val width = box.width
        val height = box.height
        val u = widgetTexture.uStart
        val v = widgetTexture.vStart
        val uSize = widgetTexture.uSize
        val vSize = widgetTexture.vSize
        val textureWidth = widgetTexture.textureInfo.width
        val textureHeight = widgetTexture.textureInfo.height

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

        val leftU = if (corner.left >= 0) u else u - cl.toInt()
        val centerU = if (corner.left >= 0) u + cl.toInt() else u
        val rightU = if (corner.right >= 0) u + (uSize - cr.toInt()) else u + uSize

        val topV = if (corner.top >= 0) v else v - ct.toInt()
        val centerV = if (corner.top >= 0) v + ct.toInt() else v
        val bottomV = if (corner.bottom >= 0) v + (vSize - cb.toInt()) else v + vSize

        val leftUS = cl.toInt()
        val centerUS = uSize - (corner.left.coerceAtLeast(0) + corner.right.coerceAtLeast(0))
        val rightUS = cr.toInt()

        val topVS = ct.toInt()
        val centerVS = vSize - (corner.top.coerceAtLeast(0) + corner.bottom.coerceAtLeast(0))
        val bottomVS = cb.toInt()
        //top left
        nineSlicedSegment(pose, leftX, topY, cl, ct, leftU, topV, leftUS, topVS, color, textureWidth, textureHeight, textureSetup, pipeline, scissorBox)
        //top center
        nineSlicedSegment(pose, centerX, topY, cw, ct, centerU, topV, centerUS, topVS, color, textureWidth, textureHeight, textureSetup, pipeline, scissorBox)
        //top right
        nineSlicedSegment(pose, rightX, topY, cr, ct, rightU, topV, rightUS, topVS, color, textureWidth, textureHeight, textureSetup, pipeline, scissorBox)

        //center left
        nineSlicedSegment(
            pose,
            leftX,
            centerY,
            cl,
            ch,
            leftU,
            centerV,
            leftUS,
            centerVS,
            color,
            textureWidth,
            textureHeight,
            textureSetup,
            pipeline,
            scissorBox
        )
        //center
        nineSlicedSegment(
            pose,
            centerX,
            centerY,
            cw,
            ch,
            centerU,
            centerV,
            centerUS,
            centerVS,
            color,
            textureWidth,
            textureHeight,
            textureSetup,
            pipeline,
            scissorBox
        )
        //center right
        nineSlicedSegment(
            pose,
            rightX,
            centerY,
            cr,
            ch,
            rightU,
            centerV,
            rightUS,
            centerVS,
            color,
            textureWidth,
            textureHeight,
            textureSetup,
            pipeline,
            scissorBox
        )

        //bottom left
        nineSlicedSegment(
            pose,
            leftX,
            bottomY,
            cl,
            cb,
            leftU,
            bottomV,
            leftUS,
            bottomVS,
            color,
            textureWidth,
            textureHeight,
            textureSetup,
            pipeline,
            scissorBox
        )
        //bottom center
        nineSlicedSegment(
            pose,
            centerX,
            bottomY,
            cw,
            cb,
            centerU,
            bottomV,
            centerUS,
            bottomVS,
            color,
            textureWidth,
            textureHeight,
            textureSetup,
            pipeline,
            scissorBox
        )
        //bottom right
        nineSlicedSegment(
            pose,
            rightX,
            bottomY,
            cr,
            cb,
            rightU,
            bottomV,
            rightUS,
            bottomVS,
            color,
            textureWidth,
            textureHeight,
            textureSetup,
            pipeline,
            scissorBox
        )
    }

    fun pushNineSlicedBlit(
        transform: Transform,
        widgetTexture: WidgetTexture,
        color: ARGBColor = Colors.WHITE,
        pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
        scissorBox: Box? = peekScissorBox()
    ) = pushNineSlicedBlit(transform.asWorldCoordinateBox, widgetTexture, color, pipeline, scissorBox)


    private fun nineSlicedSegment(
        pose: Matrix3x2f,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        u: Int,
        v: Int,
        uSize: Int,
        vSize: Int,
        color: ARGBColor = Colors.WHITE,
        textureWidth: Int,
        textureHeight: Int,
        textureSetup: TextureSetup,
        pipeline: RenderPipeline,
        scissorBox: Box? = peekScissorBox()
    ) {
        val u0 = u.toFloat() / textureWidth.toFloat()
        val v0 = v.toFloat() / textureHeight.toFloat()
        val u1 = (u + uSize).toFloat() / textureWidth.toFloat()
        val v1 = (v + vSize).toFloat() / textureHeight.toFloat()
        guiRenderState.submitGuiElement(
            IGBlitRenderState(
                x, y, x + width, y + height,
                u0, v0, u1, v1,
                applyModulatedColor(color), pose, pipeline, textureSetup, scissorBox?.asScreenRectangle
            )
        )
    }

    fun pushWidgetTexture(
        box: Box,
        widgetTexture: WidgetTexture,
        color: ARGBColor = Colors.WHITE,
        pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
        scissorBox: Box? = peekScissorBox()
    ) = pushNineSlicedBlit(box, widgetTexture, color, pipeline, scissorBox)

    fun pushWidgetTexture(
        transform: Transform,
        widgetTexture: WidgetTexture,
        color: ARGBColor = Colors.WHITE,
        pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
        scissorBox: Box? = peekScissorBox()
    ) = pushNineSlicedBlit(transform, widgetTexture, color, pipeline, scissorBox)


    fun pushTiledBlit(
        box: Box,
        widgetTexture: WidgetTexture,
        tileSize: Size<Float> = Size(widgetTexture.uSize, widgetTexture.vSize).toFloat(),
        color: ARGBColor = Colors.WHITE,
        pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
        scissorBox: Box? = peekScissorBox()
    ) = guiRenderState.submitGuiElement(
        IGTiledBlitRenderState(
            box.x, box.y, box.endX, box.endY,
            widgetTexture.u0, widgetTexture.v0, widgetTexture.u1, widgetTexture.v1,
            tileSize.width, tileSize.height,
            applyModulatedColor(color), Matrix3x2f(pose()), pipeline, widgetTexture.textureSetup, scissorBox?.asScreenRectangle
        )
    )


    fun pushTiledBlit(
        transform: Transform,
        widgetTexture: WidgetTexture,
        tileSize: Size<Float> = Size(widgetTexture.uSize, widgetTexture.vSize).toFloat(),
        color: ARGBColor = Colors.WHITE,
        pipeline: RenderPipeline = RenderPipelines.GUI_TEXTURED,
        scissorBox: Box? = peekScissorBox()
    ) = pushTiledBlit(transform.asWorldCoordinateBox, widgetTexture, tileSize, color, pipeline, scissorBox)


    //------------ Box ------------\\

    fun pushBox(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        /**
         * TOP LEFT
         */
        col1: ARGBColor,
        /**
         * BOTTOM LEFT
         */
        col2: ARGBColor,
        /**
         * BOTTOM RIGHT
         */
        col3: ARGBColor,
        /**
         * TOP RIGHT
         */
        col4: ARGBColor,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        pipeline: RenderPipeline = RenderPipelines.GUI,
        scissorBox: Box? = peekScissorBox()
    ) = guiRenderState.submitGuiElement(
        ColoredBoxRenderState(
            x0, y0, x1, y1,
            applyModulatedColor(col1), applyModulatedColor(col2), applyModulatedColor(col3), applyModulatedColor(col4),
            pose, pipeline,
            scissorBox?.asScreenRectangle
        )
    )


    fun pushBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: ARGBColor,
        pipeline: RenderPipeline = RenderPipelines.GUI,
        scissorBox: Box? = peekScissorBox()
    ) {
        pushBox(x, y, x + width, y + height, color, color, color, color, Matrix3x2f(pose()), pipeline, scissorBox)
    }

    fun pushBox(box: Box, color: ARGBColor, pipeline: RenderPipeline = RenderPipelines.GUI, scissorBox: Box? = peekScissorBox()) {
        pushBox(box.x, box.y, box.endX, box.endY, color, color, color, color, Matrix3x2f(pose()), pipeline, scissorBox)
    }

    fun pushBox(transform: Transform, color: ARGBColor, pipeline: RenderPipeline = RenderPipelines.GUI, scissorBox: Box? = peekScissorBox()) {
        pushBox(transform.asWorldCoordinateBox, color, pipeline, scissorBox)
    }

    fun pushBoxOutline(
        box: Box,
        color: ARGBColor,
        borderSize: Float = 1f,
        inner: Boolean = false,
        pipeline: RenderPipeline = RenderPipelines.GUI,
        scissorBox: Box? = peekScissorBox()
    ) {
        check(borderSize > 0) { "borderSize must be greater than 0" }
        val x = box.x
        val y = box.y
        val width = box.width
        val height = box.height
        if (inner) {
            //top
            pushBox(x = x, y = y, width = width - borderSize, height = borderSize, color = color, pipeline, scissorBox)
            //right
            pushBox(x = x + width - borderSize, y = y, width = borderSize, height = height - borderSize, color = color, pipeline, scissorBox)
            //bottom
            pushBox(x = x + borderSize, y = y + height - borderSize, width = width - borderSize, height = borderSize, color = color, pipeline, scissorBox)
            //left
            pushBox(x = x, y = y + borderSize, width = borderSize, height = height - borderSize, color = color, pipeline, scissorBox)
        } else {
            //top
            pushBox(x = x - borderSize, y = y - borderSize, width = width + borderSize, height = borderSize, color = color, pipeline, scissorBox)
            //right
            pushBox(x = x + width, y = y - borderSize, width = borderSize, height = height + borderSize, color = color, pipeline, scissorBox)
            //bottom
            pushBox(x = x, y = y + height, width = width + borderSize, height = borderSize, color = color, pipeline, scissorBox)
            //left
            pushBox(x = x - borderSize, y = y, width = borderSize, height = height + borderSize, color = color, pipeline, scissorBox)
        }
    }

    fun pushBoxOutline(
        transform: Transform,
        color: ARGBColor,
        borderSize: Float = 1f,
        inner: Boolean = false,
        pipeline: RenderPipeline = RenderPipelines.GUI,
        scissorBox: Box? = peekScissorBox()
    ) {
        pushBoxOutline(transform.asWorldCoordinateBox, color, borderSize, inner, pipeline, scissorBox)
    }

    fun pushGradientBox(
        box: Box,
        color1: ARGBColor,
        color2: ARGBColor,
        orientation: Orientation,
        pipeline: RenderPipeline = RenderPipelines.GUI,
        scissorBox: Box? = peekScissorBox()
    ) {
        val state = orientation.peek({
            ColoredBoxRenderState.vertical(
                box.x, box.y, box.endX, box.endY,
                applyModulatedColor(color1), applyModulatedColor(color2), Matrix3x2f(pose()), pipeline,
                scissorBox?.asScreenRectangle
            )
        }, {
            ColoredBoxRenderState.horizontal(
                box.x, box.y, box.endX, box.endY,
                applyModulatedColor(color1), applyModulatedColor(color2), Matrix3x2f(pose()), pipeline,
                scissorBox?.asScreenRectangle
            )
        })
        guiRenderState.submitGuiElement(state)
    }

    fun pushGradientBox(
        transform: Transform,
        color1: ARGBColor,
        color2: ARGBColor,
        orientation: Orientation,
        pipeline: RenderPipeline = RenderPipelines.GUI,
        scissorBox: Box? = peekScissorBox()
    ) {
        pushGradientBox(transform.asWorldCoordinateBox, color1, color2, orientation, pipeline, scissorBox)
    }

    fun pushSaturationGradientBox(
        box: Box,
        orientation: Orientation = Orientation.Horizontal,
        inverse: Boolean = false,
        saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        value: Float = 1f,
        alpha: Float = 1f,
        pipeline: RenderPipeline = IGRenderPipelines.GUI_HSV_COLOR,
        scissorBox: Box? = peekScissorBox()
    ) {
        check(saturationRange.endInclusive >= saturationRange.start) { "Saturation range must be in ascending order" }
        check(saturationRange.endInclusive in 0f..1f && saturationRange.start in 0f..1f) {
            "Saturation range must be between 0 and 1,but was ${saturationRange.start} and ${saturationRange.endInclusive}"
        }
        val colorStart = HSVColor(hue, (if (inverse) saturationRange.endInclusive else saturationRange.start).coerceIn(alphaFRange), value, alpha)
        val colorEnd = HSVColor(hue, (if (!inverse) saturationRange.endInclusive else saturationRange.start).coerceIn(alphaFRange), value, alpha)
        pushGradientBox(box, colorStart, colorEnd, orientation, pipeline, scissorBox)
    }

    fun pushSaturationGradientBox(
        transform: Transform,
        orientation: Orientation = Orientation.Horizontal,
        inverse: Boolean = false,
        saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        value: Float = 1f,
        alpha: Float = 1f,
        pipeline: RenderPipeline = IGRenderPipelines.GUI_HSV_COLOR,
        scissorBox: Box? = peekScissorBox()
    ) {
        pushSaturationGradientBox(transform.asWorldCoordinateBox, orientation, inverse, saturationRange, hue, value, alpha, pipeline, scissorBox)
    }

    fun pushValueGradientBox(
        box: Box,
        orientation: Orientation = Orientation.Horizontal,
        inverse: Boolean = false,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        saturation: Float = 1f,
        alpha: Float = 1f,
        pipeline: RenderPipeline = IGRenderPipelines.GUI_HSV_COLOR,
        scissorBox: Box? = peekScissorBox()
    ) {
        check(valueRange.endInclusive >= valueRange.start) { "Value range must be in ascending order" }
        check(valueRange.endInclusive in 0f..1f && valueRange.start in 0f..1f) {
            "Value range must be between 0 and 1,but was ${valueRange.start} and ${valueRange.endInclusive}"
        }
        val colorStart = HSVColor(hue, saturation, (if (inverse) valueRange.endInclusive else valueRange.start).coerceIn(alphaFRange), alpha)
        val colorEnd = HSVColor(hue, saturation, (if (!inverse) valueRange.endInclusive else valueRange.start).coerceIn(alphaFRange), alpha)
        pushGradientBox(box, colorStart, colorEnd, orientation, pipeline, scissorBox)
    }

    fun pushValueGradientBox(
        transform: Transform,
        orientation: Orientation = Orientation.Horizontal,
        inverse: Boolean = false,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        saturation: Float = 1f,
        alpha: Float = 1f,
        pipeline: RenderPipeline = IGRenderPipelines.GUI_HSV_COLOR,
        scissorBox: Box? = peekScissorBox()
    ) {
        pushValueGradientBox(transform.asWorldCoordinateBox, orientation, inverse, valueRange, hue, saturation, alpha, pipeline, scissorBox)
    }

    fun pushRoundBox(
        box: Box,
        color: ARGBColor,
        round: Int,
        pixelSize: Float = 1f,
        pipeline: RenderPipeline = RenderPipelines.GUI,
        scissorBox: Box? = peekScissorBox()
    ) {
        if (round > 0) {
            pushBox(
                Box(box.position + Vector2f(0f, (round + 1) * pixelSize), box.width, box.height - ((round + 1) * pixelSize) * 2),
                color,
                pipeline,
                scissorBox
            )
            roundBoxCache[RoundBox(round, pixelSize, box.width, box.height)]?.let {
                it.forEach { (position, size) -> pushBox(Box(box.position + position, size), color, pipeline, scissorBox) }
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
                            Vector2f(abs(x * pixelSize), abs(y!!.first) * pixelSize) to
                                    Size(box.width - abs(x * pixelSize * 2), y.second * pixelSize)
                        )
                        add(
                            Vector2f(abs(x * pixelSize), box.height - y.second * pixelSize - (abs(y.first)) * pixelSize) to
                                    Size(box.width - abs(x * pixelSize * 2), y.second * pixelSize)
                        )
                    }
                roundBoxCache[RoundBox(round, pixelSize, box.width, box.height)] = this
                if (size > roundBoxCacheSize) roundBoxCache.remove(roundBoxCache.keys.first())
            }.forEach { (position, size) -> pushBox(Box(box.position + position, size), color, pipeline, scissorBox) }
        } else pushBox(box, color, pipeline, scissorBox)
    }

    fun pushTextHighLight(box: Box, invertColor: ARGBColor = Colors.WHITE, highLightColor: ARGBColor = Colors.BLUE) {
        pushBox(box, invertColor, RenderPipelines.GUI_INVERT)
        pushBox(box, highLightColor, RenderPipelines.GUI_TEXT_HIGHLIGHT)
    }

    //------------ Text ------------\\

    fun pushText(
        text: FormattedCharSequence,
        x: Float,
        y: Float,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0f),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) = guiRenderState.submitText(IGGuiTextRenderState(font, text, pose, x, y, applyModulatedColor(color), backgroundColor, shadow, scissorBox))

    fun pushText(
        text: Component,
        x: Float,
        y: Float,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0f),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) = pushText(text.visualOrderText, x, y, color, backgroundColor, shadow, pose, font, scissorBox)

    fun pushText(
        text: String,
        x: Float,
        y: Float,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0f),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) = pushText(Language.getInstance().getVisualOrder(FormattedText.of(text)), x, y, color, backgroundColor, shadow, pose, font, scissorBox)

    fun pushAlignmentText(
        text: FormattedCharSequence,
        box: Box,
        alignment: Alignment = Alignment.Center,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0f),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) = alignment.align(box, text.size).run {
        pushText(text, box.x + x(), box.y + y(), color, backgroundColor, shadow, pose, font, scissorBox)
    }

    fun pushAlignmentText(
        text: Component,
        box: Box,
        alignment: Alignment = Alignment.Center,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0f),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) = alignment.align(box, text.size).run {
        pushText(text, box.x + x(), box.y + y(), color, backgroundColor, shadow, pose, font, scissorBox)
    }

    fun pushAlignmentText(
        text: String,
        box: Box,
        alignment: Alignment = Alignment.Center,
        color: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0f),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) = alignment.align(box, text.size).run {
        pushText(text, box.x + x(), box.y + y(), color, backgroundColor, shadow, pose, font, scissorBox)
    }

    fun pushStringLines(
        string: String,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Color(0),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) {
        val texts = string.wrapToLines(box.width)
        val verticalOffsets = verticalArrangement.arrange(box.width, List(texts.size) { font.lineHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, it.width) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, defaultColor, backgroundColor, shadow, pose, font, scissorBox)
        }
    }

    fun pushStringLines(
        string: List<String>,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Color(0),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) {
        val texts = string.wrapToLines(box.width)
        val verticalOffsets = verticalArrangement.arrange(box.width, List(texts.size) { font.lineHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, it.width) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, defaultColor, backgroundColor, shadow, pose, font, scissorBox)
        }
    }

    fun pushTextLines(
        text: Component,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) {
        val texts = text.wrapToTextLines(box.width)
        val verticalOffsets = verticalArrangement.arrange(box.height, List(texts.size) { font.lineHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, it.width) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, defaultColor, backgroundColor, shadow, pose, font, scissorBox)
        }
    }

    fun pushTextLines(
        text: List<Component>,
        box: Box,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Colors.BLACK.alpha(0),
        shadow: Boolean = false,
        pose: Matrix3x2f = Matrix3x2f(pose()),
        font: Font = this.font,
        scissorBox: Box? = peekScissorBox()
    ) {
        val texts = text.wrapToTextLines(box.width)
        val verticalOffsets = verticalArrangement.arrange(box.height, List(texts.size) { font.lineHeight.toFloat() })
        val horizontalOffsets = texts.map { horizontalAlignment.align(box.width, it.width) }
        horizontalOffsets.zip(verticalOffsets) { x, y ->
            Vector2f(box.x + x, box.y + y)
        }.forEachIndexed { index, offset ->
            pushText(texts[index], offset.x, offset.y, defaultColor, backgroundColor, shadow, pose, font, scissorBox)
        }
    }

    //------------ Item ------------\\

    fun pushItem(
        itemStack: ItemStack,
        x: Float,
        y: Float,
        scale: Float = 1f,
        entity: LivingEntity? = minecraft.player,
        level: Level? = minecraft.level,
        seed: Int = 0
    ) {
        val x = x / scale
        val y = y / scale
        val xi = x.toInt()
        val yi = y.toInt()
        useMatrixStack {
            it.scale(scale)
            it.translate(x - xi, y - yi)
            renderItem(entity, level, itemStack, xi, yi, seed)
        }
    }
}