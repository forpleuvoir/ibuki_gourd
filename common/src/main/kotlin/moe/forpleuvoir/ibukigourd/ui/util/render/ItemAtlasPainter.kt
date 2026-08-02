package moe.forpleuvoir.ibukigourd.ui.util.render

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asSkiaColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.Paint
import org.jetbrains.skia.SamplingMode

/**
 * 绘制 GPU 图集子区域的 Compose Painter。
 *
 * 不持有 CPU 位图，绘制时通过 [ItemRenderAtlas] 查询当前有效 entry 与 generation，
 * 使用 `drawIntoCanvas` 取得 Skia native canvas 后以 `drawImageRect` 绘制图集内容区域。
 */
internal class ItemAtlasPainter<K>(
    private val atlas: ItemRenderAtlas<K>,
    private val cacheKey: K,
    private val expectedSize: IntSize,
    private val filterQuality: FilterQuality = FilterQuality.None,
) : Painter() {

    private var currentAlpha: Float = DefaultAlpha
    private var currentColorFilter: ColorFilter? = null

    override val intrinsicSize: Size
        get() = Size(
            expectedSize.width.toFloat(),
            expectedSize.height.toFloat(),
        )

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

        val paint = Paint().apply {
            alpha = (currentAlpha.coerceIn(0f, 1f) * 255).toInt()
            colorFilter = currentColorFilter?.asSkiaColorFilter()
        }
        val samplingMode = filterQuality.toSkiaSamplingMode()

        drawIntoCanvas { canvas ->
            atlas.draw(
                canvas = canvas.skiaCanvas,
                key = cacheKey,
                destination = org.jetbrains.skia.Rect(0f, 0f, size.width, size.height),
                samplingMode = samplingMode,
                paint = paint,
            )
        }
    }
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
