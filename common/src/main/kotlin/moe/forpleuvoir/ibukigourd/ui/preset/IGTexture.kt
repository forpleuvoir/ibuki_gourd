package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.ibukigourd.render.extension.texture.IGTexture
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun BlitTexture(
    texture: IGTexture,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.None,
) {
    var bitmap by remember(texture.textureInfo.textureId) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(texture.textureInfo.textureId) {
        bitmap = SkiaTextureHelper.getTextureCache(
            texture.textureInfo.textureId
        )
    }

    val imageBitmap = bitmap ?: return

    val painter = remember(imageBitmap, texture, filterQuality) {
        if (texture.corner.isSpecified) {
            IGTextureNinePatchPainter(
                image = imageBitmap,
                texture = texture,
                filterQuality = filterQuality,
            )
        } else {
            createBitmapPainter(
                image = imageBitmap,
                texture = texture,
                filterQuality = filterQuality,
            )
        }
    } ?: return

    Image(
        painter = painter,
        contentDescription = "Minecraft Texture",
        modifier = modifier,
        alignment = alignment,
        contentScale = if (texture.corner.isSpecified) {
            ContentScale.FillBounds
        } else {
            contentScale
        },
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

private class IGTextureNinePatchPainter(
    private val image: ImageBitmap,
    private val texture: IGTexture,
    private val filterQuality: FilterQuality,
) : Painter() {

    override val intrinsicSize: Size
        get() = Size(
            width = texture.uSize.toFloat(),
            height = texture.vSize.toFloat(),
        )

    override fun DrawScope.onDraw() {
        val destinationWidth = size.width.roundToInt()
        val destinationHeight = size.height.roundToInt()

        if (destinationWidth <= 0 || destinationHeight <= 0) {
            return
        }

        val columns = calculateNinePatchAxis(
            sourceStart = texture.uStart,
            sourceSize = texture.uSize,
            destinationSize = destinationWidth,
            startCorner = texture.corner.left,
            endCorner = texture.corner.right,
        )

        val rows = calculateNinePatchAxis(
            sourceStart = texture.vStart,
            sourceSize = texture.vSize,
            destinationSize = destinationHeight,
            startCorner = texture.corner.top,
            endCorner = texture.corner.bottom,
        )

        for ((sourceOffset1, sourceSize1, destinationOffset1, destinationSize1) in rows)
            for ((sourceOffset, sourceSize, destinationOffset, destinationSize) in columns) {
                drawSegment(
                    sourceOffset = IntOffset(
                        x = sourceOffset,
                        y = sourceOffset1,
                    ),
                    sourceSize = IntSize(
                        width = sourceSize,
                        height = sourceSize1,
                    ),
                    destinationOffset = IntOffset(
                        x = destinationOffset,
                        y = destinationOffset1,
                    ),
                    destinationSize = IntSize(
                        width = destinationSize,
                        height = destinationSize1,
                    ),
                )
            }
    }

    private fun DrawScope.drawSegment(
        sourceOffset: IntOffset,
        sourceSize: IntSize,
        destinationOffset: IntOffset,
        destinationSize: IntSize,
    ) {
        if (
            sourceSize.width <= 0 ||
            sourceSize.height <= 0 ||
            destinationSize.width <= 0 ||
            destinationSize.height <= 0
        ) {
            return
        }

        drawImage(
            image = image,
            srcOffset = sourceOffset,
            srcSize = sourceSize,
            dstOffset = destinationOffset,
            dstSize = destinationSize,
            filterQuality = filterQuality,
        )
    }
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

private fun createBitmapPainter(
    image: ImageBitmap,
    texture: IGTexture,
    filterQuality: FilterQuality,
): BitmapPainter? {
    val sourceLeft = (texture.u0 * image.width)
        .roundToInt()
        .coerceIn(0, image.width)

    val sourceTop = (texture.v0 * image.height)
        .roundToInt()
        .coerceIn(0, image.height)

    val sourceRight = (texture.u1 * image.width)
        .roundToInt()
        .coerceIn(0, image.width)

    val sourceBottom = (texture.v1 * image.height)
        .roundToInt()
        .coerceIn(0, image.height)

    val sourceWidth = sourceRight - sourceLeft
    val sourceHeight = sourceBottom - sourceTop

    if (sourceWidth <= 0 || sourceHeight <= 0) {
        return null
    }

    return BitmapPainter(
        image = image,
        srcOffset = IntOffset(
            x = sourceLeft,
            y = sourceTop,
        ),
        srcSize = IntSize(
            width = sourceWidth,
            height = sourceHeight,
        ),
        filterQuality = filterQuality,
    )
}