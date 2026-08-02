package moe.forpleuvoir.ibukigourd.ui.util.render

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asSkiaColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.ibukigourd.render.extension.texture.Corner
import moe.forpleuvoir.ibukigourd.render.extension.texture.IGTexture
import net.minecraft.resources.Identifier
import org.jetbrains.skia.Paint
import org.jetbrains.skia.SamplingMode
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * 绘制 GPU 纹理图集子区域的 Compose Painter。
 *
 * 不持有 CPU 位图。支持：
 * - 整张纹理（[texture] 为 null 且 [source] 为 null）；
 * - [IGTexture] 的 UV 子区域（按纹理实际解码尺寸换算）；
 * - [IGTexture] 的九宫格拆分绘制。
 *
 * 绘制时通过 [ItemRenderAtlas.draw] 查询当前有效 entry 与 generation，
 * 源区域统一相对 entry.content 计算。
 */
internal class TextureAtlasPainter(
    private val atlas: ItemRenderAtlas<Identifier>,
    private val cacheKey: Identifier,
    private val filterQuality: FilterQuality = FilterQuality.None,
    private val texture: IGTexture? = null,
    private val expectedSize: IntSize? = null,
) : Painter() {

    private var currentAlpha: Float = DefaultAlpha
    private var currentColorFilter: ColorFilter? = null

    override val intrinsicSize: Size
        get() = expectedSize?.let {
            Size(it.width.toFloat(), it.height.toFloat())
        } ?: Size.Unspecified

    override fun applyAlpha(alpha: Float): Boolean {
        currentAlpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        currentColorFilter = colorFilter
        return true
    }

    override fun DrawScope.onDraw() {
        if (size.width <= 0f || size.height <= 0f) return

        val entry = atlas.get(cacheKey) ?: return
        if (entry.generation != atlas.generationId) return

        val paint = Paint().apply {
            alpha = (currentAlpha.coerceIn(0f, 1f) * 255).toInt()
            colorFilter = currentColorFilter?.asSkiaColorFilter()
        }
        val samplingMode = filterQuality.toSkiaSamplingMode()
        val destination = org.jetbrains.skia.Rect(0f, 0f, size.width, size.height)

        drawIntoCanvas { canvas ->
            val native = canvas.skiaCanvas
            val t = texture
            if (t != null && t.corner.isSpecified) {
                // 九宫格：将 UV 子区域按 corner 拆分绘制
                drawNinePatch(native, entry, t.corner, t.uStart, t.vStart, t.uSize, t.vSize, samplingMode, paint)
            } else if (t != null) {
                // UV 子区域
                atlas.draw(
                    native, cacheKey, destination, samplingMode, paint,
                    source = uvSourceRect(t.u0, t.v0, t.u1, t.v1, entry.content.width, entry.content.height)
                )
            } else {
                // 整张纹理
                atlas.draw(native, cacheKey, destination, samplingMode, paint)
            }
        }
    }

    private fun DrawScope.drawNinePatch(
        native: org.jetbrains.skia.Canvas,
        entry: AtlasEntry,
        corner: Corner,
        sourceStartU: Int,
        sourceStartV: Int,
        sourceSizeU: Int,
        sourceSizeV: Int,
        samplingMode: SamplingMode,
        paint: Paint,
    ) {
        val destinationWidth = size.width.roundToInt()
        val destinationHeight = size.height.roundToInt()
        if (destinationWidth <= 0 || destinationHeight <= 0) return

        val columns = calculateNinePatchAxis(
            sourceStart = sourceStartU,
            sourceSize = sourceSizeU,
            destinationSize = destinationWidth,
            startCorner = corner.left,
            endCorner = corner.right,
        )
        val rows = calculateNinePatchAxis(
            sourceStart = sourceStartV,
            sourceSize = sourceSizeV,
            destinationSize = destinationHeight,
            startCorner = corner.top,
            endCorner = corner.bottom,
        )

        for ((sourceOffset1, sourceSize1, destinationOffset1, destinationSize1) in rows)
            for ((sourceOffset, sourceSize, destinationOffset, destinationSize) in columns) {
                if (sourceSize <= 0 || sourceSize1 <= 0 || destinationSize <= 0 || destinationSize1 <= 0) continue
                atlas.draw(
                    native,
                    cacheKey,
                    org.jetbrains.skia.Rect(
                        destinationOffset.toFloat(),
                        destinationOffset1.toFloat(),
                        (destinationOffset + destinationSize).toFloat(),
                        (destinationOffset1 + destinationSize1).toFloat(),
                    ),
                    samplingMode,
                    paint,
                    source = AtlasRect(
                        x = sourceOffset,
                        y = sourceOffset1,
                        width = sourceSize,
                        height = sourceSize1,
                    ),
                )
            }
    }
}

/** 将归一化 UV 坐标换算为相对 content 的像素源区域 */
private fun uvSourceRect(
    u0: Float,
    v0: Float,
    u1: Float,
    v1: Float,
    contentWidth: Int,
    contentHeight: Int,
): AtlasRect {
    val left = (u0 * contentWidth).roundToInt().coerceIn(0, contentWidth)
    val top = (v0 * contentHeight).roundToInt().coerceIn(0, contentHeight)
    val right = (u1 * contentWidth).roundToInt().coerceIn(0, contentWidth)
    val bottom = (v1 * contentHeight).roundToInt().coerceIn(0, contentHeight)
    return AtlasRect(
        x = left,
        y = top,
        width = (right - left).coerceAtLeast(0),
        height = (bottom - top).coerceAtLeast(0),
    )
}

private data class NinePatchAxisSegment(
    val sourceOffset: Int,
    val sourceSize: Int,
    val destinationOffset: Int,
    val destinationSize: Int,
)

private fun calculateNinePatchAxis(
    sourceStart: Int,
    sourceSize: Int,
    destinationSize: Int,
    startCorner: Int,
    endCorner: Int,
): Array<NinePatchAxisSegment> {
    val startSize = startCorner.absoluteValue
    val endSize = endCorner.absoluteValue
    val positiveStart = startCorner.coerceAtLeast(0)
    val positiveEnd = endCorner.coerceAtLeast(0)

    return arrayOf(
        // 左边或上边
        NinePatchAxisSegment(
            sourceOffset = if (startCorner >= 0) {
                sourceStart
            } else {
                sourceStart - startSize
            },
            sourceSize = startSize,
            destinationOffset = if (startCorner >= 0) {
                0
            } else {
                -startSize
            },
            destinationSize = startSize,
        ),

        // 中心
        NinePatchAxisSegment(
            sourceOffset = sourceStart + positiveStart,
            sourceSize = sourceSize - positiveStart - positiveEnd,
            destinationOffset = positiveStart,
            destinationSize = destinationSize - positiveStart - positiveEnd,
        ),

        // 右边或下边
        NinePatchAxisSegment(
            sourceOffset = if (endCorner >= 0) {
                sourceStart + sourceSize - endSize
            } else {
                sourceStart + sourceSize
            },
            sourceSize = endSize,
            destinationOffset = if (endCorner >= 0) {
                destinationSize - endSize
            } else {
                destinationSize
            },
            destinationSize = endSize,
        ),
    )
}

private fun FilterQuality.toSkiaSamplingMode(): SamplingMode = when (this) {
    FilterQuality.None,
    FilterQuality.Low,
    -> SamplingMode.DEFAULT

    FilterQuality.Medium,
    FilterQuality.High,
    -> SamplingMode.LINEAR

    else -> SamplingMode.DEFAULT
}
