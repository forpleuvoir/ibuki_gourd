package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.SimpleResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.resourceManager
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import kotlin.jvm.optionals.getOrNull
import kotlin.time.measureTimedValue

object SkiaTextureHelper : ClientResourceReloaderListener, SimpleResourceReloaderListener<Unit>() {

    private val logger = logger()

    private val textureCache = LinkedHashMap<Identifier, ImageBitmap>(32, 0.75f, false)
    private var totalCacheArea: Long = 0
    private const val MAX_CACHE_AREA: Long = 1024 * 1024 * 32

    override val identifier: Identifier = identifier("skia_texture")

    private val missingTexture: ImageBitmap by lazy {
        val size = 2
        val pixels = ByteArray(size * size * 4)

        val purple = Colors.PURPLE
        val black = Colors.BLACK

        var k = 0
        for (y in 0 until size) for (x in 0 until size) {
            val color = if ((x + y) % 2 == 0) purple else black
            pixels[k++] = color.blue.toByte()
            pixels[k++] = color.green.toByte()
            pixels[k++] = color.red.toByte()
            pixels[k++] = color.alpha.toByte()
        }
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(size, size, ColorAlphaType.UNPREMUL))
        bitmap.installPixels(pixels)
        bitmap.asComposeImageBitmap()
    }

    /**
     * 获取纹理位图缓存，未命中时从资源管理器加载并解码
     *
     * @param texture 纹理资源标识符
     * @return 解码后的位图，资源不存在时返回 null
     */
    suspend fun getTextureCache(texture: Identifier): ImageBitmap {
        textureCache[texture]?.let { return it }

        val (result, duration) = measureTimedValue {
            val bytes = withContext(Dispatchers.IO) {
                resourceManager
                    .getResource(texture)
                    .getOrNull()
                    ?.open()
                    ?.readAllBytes()
            } ?: return missingTexture

            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        }
        logger.info("Create Texture buffer: $duration")

        val entryArea = result.width.toLong() * result.height.toLong()
        while (totalCacheArea + entryArea > MAX_CACHE_AREA && textureCache.isNotEmpty()) {
            val eldest = textureCache.entries.first()
            textureCache.remove(eldest.key)
        }
        totalCacheArea += entryArea
        textureCache[texture] = result
        return result
    }

    private fun invalidateTextureCache() {
        textureCache.clear()
        totalCacheArea = 0
        logger.info("Invalidate Texture cache")
    }

    override fun prepare(sharedState: PreparableReloadListener.SharedState) = Unit
    override fun apply(prepared: Unit, sharedState: PreparableReloadListener.SharedState) {
        invalidateTextureCache()
    }
}

/**
 * 渲染 Minecraft 纹理为 Compose Image 组件
 *
 * 通过资源管理器异步加载纹理文件并解码为位图，加载完成后触发重组渲染。
 * 转换结果会被缓存，相同纹理标识符不会重复解码。
 *
 * @param texture 纹理资源标识符
 * @param modifier 应用于 [Image] 组件的 Compose 修饰符
 * @param alignment 图像在组件空间内的对齐方式
 * @param contentScale 图像的缩放策略
 * @param alpha 图像的不透明度，范围 [0, 1]
 * @param colorFilter 可选的颜色滤镜
 * @param filterQuality 图像采样质量,一般都是像素风的图所以使用临近过滤
 */
@Composable
fun BlitTexture(
    texture: Identifier,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.None,
) {
    var bitmap by remember(texture) { mutableStateOf<ImageBitmap?>(null) }

    /* 异步加载纹理：优先查缓存，未命中则在 IO 线程通过 SkiaTextureHelper 加载 */
    LaunchedEffect(texture) {
        bitmap = SkiaTextureHelper.getTextureCache(texture)
    }

    /* 位图就绪后渲染，加载期间不占位 */
    bitmap?.let {
        Image(
            it,
            "Minecraft Texture",
            modifier,
            alignment,
            contentScale,
            alpha,
            colorFilter,
            filterQuality
        )
    }
}
