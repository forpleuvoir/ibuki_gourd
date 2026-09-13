package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mojang.blaze3d.pipeline.RenderPipeline
import moe.forpleuvoir.compose_minecraft.platform.render.CustomDrawContext
import moe.forpleuvoir.compose_minecraft.platform.render.MinecraftRenderPlugin
import moe.forpleuvoir.compose_minecraft.platform.render.toMatrix3x2f
import moe.forpleuvoir.compose_minecraft.platform.render.toScreenRectangle
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuLayerSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.util.identifier
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.resources.Identifier
import org.joml.Matrix3x2f
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 气泡精灵的自定义绘制数据，由 [SokitsuBubbleSpritePlugin] 在渲染阶段消费。
 *
 * - [body] 气泡体九宫格精灵，绘制区域 = [size]（节点尺寸）
 * - [arrow] 箭头九宫格精灵，绘制区域 = 纹理尺寸 × [pixelScale]（1:1，不缩放）
 * - [arrowAnchor] 气泡相对目标的位置（[AnchorPosition]）；箭头贴在**反向边**
 *   （[AnchorPosition.Above] → 气泡体底边）
 * - [arrowRatio] 箭头沿所在边的相对位置（0 = 起端，1 = 终端，0.5 = 居中），
 *   可用范围已按气泡体边框收窄（箭头不扎进圆角）
 * - [size] / [pixelScale] / [bodyTintColors] / [arrowTintColors] / [shadowOffset]
 *   语义同 [SokitsuSpriteDrawData]
 */
data class SokitsuBubbleSpriteDrawData(
    val body: SokitsuSprite,
    val arrow: SokitsuSprite,
    val size: IntSize,
    val pixelScale: Int,
    val arrowAnchor: AnchorPosition,
    val arrowRatio: Float,
    val bodyTintColors: List<Int>,
    val arrowTintColors: List<Int>,
    val shadowOffset: IntOffset,
)

/**
 * 气泡（气泡体 + 箭头）渲染插件：消费 [SokitsuBubbleSpriteDrawData]。
 *
 * 绘制分四段：**阴影全部在最底层**，再画箭头本体，最后画气泡体本体：
 *
 * 1. 箭头阴影、气泡体阴影（同一批次，谁先谁后都对，互不遮挡）；
 * 2. 箭头本体：普通九宫格，布局盒子 = 绘制区域 − 负 border 外扩量；盒子按近边外扩量偏移后，
 *    外扩的像素带正好落进气泡体该边让出的缺口、盒子外沿贴齐该边；
 * 3. 气泡体本体：九宫格，但箭头所在那条边的**中格**按箭头绘制区域断开成两段
 *    （剩余宽度非正时整格不画），两段各用完整中带源 UV —— 即旧版的"边缘打断"，
 *    使箭头根部陷进气泡体而整体尺寸不向外扩展。
 *
 * 阴影必须先于两个本体：气泡体与箭头各自带阴影层，若按"箭头整体 → 气泡体整体"提交，
 * 气泡体的阴影会盖在已画好的箭头上。
 *
 * [body] / [arrow] 的图层 [TextureFill] 必须是 [TextureFill.NinePatch]，
 * 否则渲染阶段直接抛出 [IllegalStateException]（不做降级）。
 */
object SokitsuBubbleSpritePlugin : MinecraftRenderPlugin {

    val TAG: Identifier = identifier("sokitsu_bubble_sprite")

    override fun onDraw(tag: Identifier, data: Any?, context: CustomDrawContext): Boolean {
        if (tag != TAG) return false
        val drawData = data as? SokitsuBubbleSpriteDrawData ?: return false
        drawBubble(drawData, context)
        return true
    }

    private fun drawBubble(data: SokitsuBubbleSpriteDrawData, context: CustomDrawContext) {
        val w = data.size.width
        val h = data.size.height
        if (w <= 0 || h <= 0) return
        val pose = context.matrix.toMatrix3x2f()
        val scissor = context.scissor?.toScreenRectangle()
        val alpha = context.alpha

        val arrowRect = arrowDrawArea(data, w, h)

        // 阴影统一在最底层：气泡体阴影不得盖住箭头
        if (!data.arrow.isEmpty) {
            val outset = data.arrow.ninePatchOutsetPx(data.pixelScale)
            emitSprite(
                context, data.arrow, data.arrowTintColors,
                x = arrowRect.x + outset[0].roundToInt(),
                y = arrowRect.y + outset[1].roundToInt(),
                w = (arrowRect.width - outset[0] - outset[2]).roundToInt(),
                h = (arrowRect.height - outset[1] - outset[3]).roundToInt(),
                pixelScale = data.pixelScale,
                shadowOnly = true,
                shadowOffset = data.shadowOffset,
                alpha = alpha,
                pose = pose,
                scissor = scissor,
            )
        }
        if (!data.body.isEmpty) {
            emitBody(context, data, arrowRect, shadowOnly = true, alpha, pose, scissor)
        }

        // 箭头本体（气泡体缺口处露出）
        if (!data.arrow.isEmpty) {
            val outset = data.arrow.ninePatchOutsetPx(data.pixelScale)
            emitSprite(
                context, data.arrow, data.arrowTintColors,
                x = arrowRect.x + outset[0].roundToInt(),
                y = arrowRect.y + outset[1].roundToInt(),
                w = (arrowRect.width - outset[0] - outset[2]).roundToInt(),
                h = (arrowRect.height - outset[1] - outset[3]).roundToInt(),
                pixelScale = data.pixelScale,
                shadowOnly = false,
                shadowOffset = data.shadowOffset,
                alpha = alpha,
                pose = pose,
                scissor = scissor,
            )
        }

        // 气泡体本体（后画，覆盖箭头根部）
        if (!data.body.isEmpty) {
            emitBody(context, data, arrowRect, shadowOnly = false, alpha, pose, scissor)
        }
    }

    /**
     * 箭头的绘制区域（节点局部坐标，屏幕像素）：贴 [SokitsuBubbleSpriteDrawData.arrowAnchor]
     * 的反向边，沿边位置取 [SokitsuBubbleSpriteDrawData.arrowRatio]。
     *
     * 跨边方式：嵌入边（负 border 那一侧）的**外扩带留在气泡内**（探入气泡体缺口），
     * 其余部分落在气泡外 —— 即箭头只有根部插进气泡，主体伸在外面指向目标。
     * 故该节点需在箭头方向预留 `箭头布局盒子尺寸`（见 [boxWidthPx] / [boxHeightPx]）的留白，
     * 否则伸出部分会被弹层裁剪。
     *
     * 沿边滑动范围按气泡体边框（[bodyBorderPx]）两侧收窄，箭头不会压到圆角；气泡体过窄时
     * 范围退化为 0，箭头贴起端边框。尺寸 = 纹理逻辑尺寸 × [pixelScale]（与素材 1:1，不缩放）。
     */
    private fun arrowDrawArea(data: SokitsuBubbleSpriteDrawData, w: Int, h: Int): IntRect {
        val aw = (data.arrow.logicalWidth * data.pixelScale).roundToInt().coerceAtLeast(1)
        val ah = (data.arrow.logicalHeight * data.pixelScale).roundToInt().coerceAtLeast(1)
        val (bl, bt, br, bb) = bodyBorderPx(data)
        val ratio = data.arrowRatio.coerceIn(0f, 1f)
        val slideX = bl + ((w - bl - br - aw).coerceAtLeast(0) * ratio).roundToInt()
        val slideY = bt + ((h - bt - bb - ah).coerceAtLeast(0) * ratio).roundToInt()
        val outset = data.arrow.ninePatchOutsetPx(data.pixelScale)
        val rootLeft = outset[0].roundToInt()
        val rootTop = outset[1].roundToInt()
        val rootRight = outset[2].roundToInt()
        val rootBottom = outset[3].roundToInt()
        return when (data.arrowAnchor) {
            AnchorPosition.Above -> IntRect(slideX, h - rootTop, aw, ah)
            AnchorPosition.Below -> IntRect(slideX, rootBottom - ah, aw, ah)
            AnchorPosition.Left  -> IntRect(w - rootLeft, slideY, aw, ah)
            AnchorPosition.Right -> IntRect(rootRight - aw, slideY, aw, ah)
        }
    }

    /**
     * 气泡体的九宫格边框（屏幕像素，`[左, 上, 右, 下]`），负值边按 0 计。
     *
     * 各图层取最大值（图层间 border 可不同），非 NinePatch 图层贡献 0。
     */
    private fun bodyBorderPx(data: SokitsuBubbleSpriteDrawData): IntArray {
        var left = 0f
        var top = 0f
        var right = 0f
        var bottom = 0f
        for (layer in data.body.layers) {
            val border = (layer.fill as? TextureFill.NinePatch)?.border ?: continue
            val scale = data.pixelScale.toFloat() / layer.density.coerceAtLeast(1)
            left = maxOf(left, border.left * scale)
            top = maxOf(top, border.top * scale)
            right = maxOf(right, border.right * scale)
            bottom = maxOf(bottom, border.bottom * scale)
        }
        return intArrayOf(
            left.coerceAtLeast(0f).roundToInt(),
            top.coerceAtLeast(0f).roundToInt(),
            right.coerceAtLeast(0f).roundToInt(),
            bottom.coerceAtLeast(0f).roundToInt(),
        )
    }

    /**
     * 按九宫格提交一个精灵的图层（无断开），阴影图层按其自身位置偏移绘制。
     *
     * @param shadowOnly true = 只提交阴影层，false = 只提交非阴影层（阴影另行在最底层统一提交）
     */
    private fun emitSprite(
        context: CustomDrawContext,
        sprite: SokitsuSprite,
        tintColors: List<Int>,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        pixelScale: Int,
        shadowOnly: Boolean,
        shadowOffset: IntOffset,
        alpha: Float,
        pose: Matrix3x2f,
        scissor: ScreenRectangle?,
    ) {
        if (w <= 0 || h <= 0) return
        val atlas = SokitsuAtlasManager.atlasTexture(sprite.atlasLocation) ?: return
        val textureSetup = TextureSetup.singleTexture(atlas.getTextureView(), atlas.getSampler())
        sprite.layers.forEachIndexed { index, layer ->
            if (layer.isShadow != shadowOnly) return@forEachIndexed
            val color = scaleAlpha(tintColors.getOrNull(index) ?: return@forEachIndexed, alpha)
            val dx = x + if (layer.isShadow) shadowOffset.x else 0
            val dy = y + if (layer.isShadow) shadowOffset.y else 0
            val target = EmitTarget(context, sokitsuLayerPipeline(layer), textureSetup, pose, color, scissor)
            val slices = NinePatchSlices(layer, pixelScale, w, h)
            for (row in 0..2) {
                for (col in 0..2) {
                    if (row * 3 + col in layer.disabledSlices) continue
                    slices.emit(
                        target, col, row,
                        dx + slices.x(col), dy + slices.y(row),
                        slices.width(col), slices.height(row),
                    )
                }
            }
        }
    }

    /**
     * 提交气泡体：箭头所在边的中格按 [arrowRect] 断开成两段。
     *
     * @param shadowOnly true = 只提交阴影层，false = 只提交非阴影层；阴影层同样按缺口断开，
     *   否则阴影会填进缺口、在箭头下方露出色块。
     */
    private fun emitBody(
        context: CustomDrawContext,
        data: SokitsuBubbleSpriteDrawData,
        arrowRect: IntRect,
        shadowOnly: Boolean,
        alpha: Float,
        pose: Matrix3x2f,
        scissor: ScreenRectangle?,
    ) {
        val atlas = SokitsuAtlasManager.atlasTexture(data.body.atlasLocation) ?: return
        val textureSetup = TextureSetup.singleTexture(atlas.getTextureView(), atlas.getSampler())
        data.body.layers.forEachIndexed { index, layer ->
            if (layer.isShadow != shadowOnly) return@forEachIndexed
            val color = scaleAlpha(data.bodyTintColors.getOrNull(index) ?: return@forEachIndexed, alpha)
            val dx = if (layer.isShadow) data.shadowOffset.x else 0
            val dy = if (layer.isShadow) data.shadowOffset.y else 0
            val target = EmitTarget(context, sokitsuLayerPipeline(layer), textureSetup, pose, color, scissor)
            val slices = NinePatchSlices(layer, data.pixelScale, data.size.width, data.size.height)
            for (row in 0..2) {
                for (col in 0..2) {
                    if (row * 3 + col in layer.disabledSlices) continue
                    val cx = slices.x(col)
                    val cy = slices.y(row)
                    val cw = slices.width(col)
                    val ch = slices.height(row)
                    if (!isArrowCell(data.arrowAnchor, row, col)) {
                        slices.emit(target, col, row, dx + cx, dy + cy, cw, ch)
                        continue
                    }
                    when (data.arrowAnchor) {
                        AnchorPosition.Above, AnchorPosition.Below -> {
                            if (cw - arrowRect.width <= 0) continue
                            slices.emit(target, col, row, dx + cx, dy + cy, arrowRect.x - cx, ch)
                            slices.emit(
                                target, col, row,
                                dx + arrowRect.x + arrowRect.width, dy + cy,
                                cx + cw - arrowRect.x - arrowRect.width, ch,
                            )
                        }
                        AnchorPosition.Left, AnchorPosition.Right -> {
                            if (ch - arrowRect.height <= 0) continue
                            slices.emit(target, col, row, dx + cx, dy + cy, cw, arrowRect.y - cy)
                            slices.emit(
                                target, col, row,
                                dx + cx, dy + arrowRect.y + arrowRect.height,
                                cw, cy + ch - arrowRect.y - arrowRect.height,
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 该分片是否为箭头所在边的中格（需要断开的那一格）。
     *
     * [anchor] 为气泡位置，箭头在反向边：[AnchorPosition.Above] → 底行中格 `(2,1)`。
     */
    private fun isArrowCell(anchor: AnchorPosition, row: Int, col: Int): Boolean = when (anchor) {
        AnchorPosition.Above -> row == 2 && col == 1
        AnchorPosition.Below -> row == 0 && col == 1
        AnchorPosition.Left  -> row == 1 && col == 2
        AnchorPosition.Right -> row == 1 && col == 0
    }
}

private data class IntRect(val x: Int, val y: Int, val width: Int, val height: Int)

/** 一次图层发射所共用的渲染上下文（同一图层内不变）。 */
private class EmitTarget(
    val context: CustomDrawContext,
    val pipeline: RenderPipeline,
    val textureSetup: TextureSetup,
    val pose: Matrix3x2f,
    val color: Int,
    val scissor: ScreenRectangle?,
)

/**
 * 单个图层的九宫格切分：目标边界（屏幕像素，已吸附整数）与源 UV 边界（素材物理像素）。
 *
 * 目标边界用带符号 border（负值向绘制区外扩，见 [ninePatchBoundaries]）；
 * 源边界用 border 绝对值 —— 采样必须落在纹理内，否则 `getU` / `getV` 线性外推会取到图集
 * 隔离带的透明像素。正值下 `abs` 恒等，无副作用。
 *
 * 图层的 [TextureFill] 必须是 [TextureFill.NinePatch]，否则构造即抛 [IllegalStateException]。
 */
private class NinePatchSlices(
    private val layer: SokitsuLayerSprite,
    pixelScale: Int,
    width: Int,
    height: Int,
) {

    private val xi = IntArray(4)
    private val yi = IntArray(4)
    private val su: FloatArray
    private val sv: FloatArray
    private val srcW = layer.width.toFloat()
    private val srcH = layer.height.toFloat()

    init {
        val border = (layer.fill as? TextureFill.NinePatch ?: error(
            "bubble sprite '${layer.textureId}' layer '${layer.layerId}' requires nine-patch fill, but was ${layer.fill}"
        )).border
        val scale = pixelScale.toFloat() / layer.density
        val xs = ninePatchBoundaries(border.left * scale, border.right * scale, width.toFloat())
        val ys = ninePatchBoundaries(border.top * scale, border.bottom * scale, height.toFloat())
        for (i in 0..3) {
            xi[i] = xs[i].roundToInt()
            yi[i] = ys[i].roundToInt()
        }
        su = ninePatchBoundaries(
            abs(border.left).toFloat(), abs(border.right).toFloat(), srcW
        )
        sv = ninePatchBoundaries(
            abs(border.top).toFloat(), abs(border.bottom).toFloat(), srcH
        )
    }

    fun x(col: Int): Int = xi[col]

    fun y(row: Int): Int = yi[row]

    fun width(col: Int): Int = xi[col + 1] - xi[col]

    fun height(row: Int): Int = yi[row + 1] - yi[row]

    /**
     * 提交 [col] / [row] 分片覆盖 `[px, px + pw) × [py, py + ph)` 的部分：
     * 源 UV 恒取整片，故宽高与该分片不一致时该片图案被拉伸。
     */
    fun emit(
        target: EmitTarget,
        col: Int,
        row: Int,
        px: Int,
        py: Int,
        pw: Int,
        ph: Int,
    ) = emitSokitsuBlit(
        target.pipeline, target.textureSetup, target.pose,
        px, py, pw, ph,
        layer.getU(su[col] / srcW), layer.getU(su[col + 1] / srcW),
        layer.getV(sv[row] / srcH), layer.getV(sv[row + 1] / srcH),
        target.color, target.scissor, target.context,
    )
}
