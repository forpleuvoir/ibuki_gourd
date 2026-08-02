package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import moe.forpleuvoir.ibukigourd.render.extension.texture.IGTexture

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
    LaunchedEffect(texture.textureInfo.textureId) {
        SkiaTextureHelper.requestTexture(texture.textureInfo.textureId)
    }

    // 订阅图集版本变化，上传完成后自动重组
    val revision = SkiaTextureHelper.cacheRevision
    val painter = remember(texture, revision) {
        SkiaTextureHelper.getTexturePainter(texture, filterQuality)
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
