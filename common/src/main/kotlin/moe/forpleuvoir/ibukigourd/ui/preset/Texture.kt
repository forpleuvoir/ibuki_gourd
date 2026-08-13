package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.platform.RenderBackend
import moe.forpleuvoir.ibukigourd.render.extension.texture.IGTexture
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaContext
import moe.forpleuvoir.ibukigourd.ui.util.render.AtlasConfig
import moe.forpleuvoir.ibukigourd.ui.util.render.ItemRenderAtlas
import moe.forpleuvoir.ibukigourd.ui.util.render.TextureAtlasPainter
import moe.forpleuvoir.ibukigourd.util.SimpleResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.resourceManager
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.ImageInfo
import kotlin.jvm.optionals.getOrNull
import kotlin.time.measureTimedValue

/**
 * 纹理 GPU 图集缓存。
 *
 * 长期缓存由单张 GPU [ItemRenderAtlas] 承载，不再按条目持有 CPU [ImageBitmap]。
 *
 * 纹理解码（资源读取 + Skia 解码）在挂起调用方线程执行，
 * 上传操作通过 [processOnRenderThread] 在渲染线程统一提交。
 */
object SkiaTextureHelper : ClientResourceReloaderListener, SimpleResourceReloaderListener<Unit>() {

    private val logger = logger()

    override val identifier: Identifier = identifier("skia_texture")

    // 纹理图集最大像素面积（RGBA8 每像素 4 字节，134_217_728 像素 ≈ 512 MiB）
    private const val DEFAULT_MAX_CACHE_AREA: Long = 1024L * 1024 * 128

    private val textureAtlas = ItemRenderAtlas<Identifier>(AtlasConfig(DEFAULT_MAX_CACHE_AREA))

    /**
     * 图集可观察版本。每批次成功上传后递增一次，
     * 等待缓存的 Composable 通过读取此值触发重组。
     */
    var cacheRevision by mutableLongStateOf(0L)
        private set

    //region 队列与上传

    private class PendingTextureUpload(
        val key: Identifier,
        val image: SkiaImage,
        val width: Int,
        val height: Int,
    )

    private val pendingUploads = ArrayDeque<PendingTextureUpload>()

    /**
     * 请求纹理缓存。未命中时读取资源并解码，随后由渲染线程上传到图集。
     *
     * 资源缺失时上传一个紫色占位纹理，保证调用方始终能拿到 painter。
     */
    suspend fun requestTexture(texture: Identifier) {
        if (RenderBackend.isVulkan) return
        if (textureAtlas.contains(texture)) return

        val (image, duration) = measureTimedValue {
            val bytes = withContext(Dispatchers.IO) {
                resourceManager
                    .getResource(texture)
                    .getOrNull()
                    ?.open()
                    ?.readAllBytes()
            }
            if (bytes != null) {
                SkiaImage.makeFromEncoded(bytes)
            } else {
                missingTextureImage()
            }
        }
        logger.info("Decode Texture: $duration")

        synchronized(pendingUploads) {
            // 重复请求去重
            if (textureAtlas.contains(texture) || pendingUploads.any { it.key == texture }) {
                image.close()
                return
            }
            pendingUploads.addLast(
                PendingTextureUpload(
                    key = texture,
                    image = image,
                    width = image.width,
                    height = image.height,
                )
            )
        }
    }

    /**
     * 渲染线程逐帧驱动：上传待处理纹理并递增 revision。
     */
    fun processOnRenderThread() {
        if (RenderBackend.isVulkan) return

        // 0. 应用容量修改：先重建图集，再处理当帧上传
        val pendingArea = pendingMaxAreaPixels
        if (pendingArea != null) {
            pendingMaxAreaPixels = null
            if (pendingArea != textureAtlas.requestedMaxAreaPixels) {
                SkiaContext.submit {
                    textureAtlas.reconfigure(pendingArea)
                }
            }
        }

        if (pendingInvalidation) {
            pendingInvalidation = false
            synchronized(pendingUploads) {
                pendingUploads.forEach { it.image.close() }
                pendingUploads.clear()
            }
            SkiaContext.submit {
                textureAtlas.invalidate()
            }
            cacheRevision++
        }

        val uploads = synchronized(pendingUploads) {
            buildList {
                while (pendingUploads.isNotEmpty()) {
                    add(pendingUploads.removeFirst())
                }
            }
        }
        if (uploads.isEmpty()) return

        var uploaded = false
        for (upload in uploads) {
            try {
                var ok = false
                SkiaContext.submit {
                    ok = textureAtlas.upload(
                        upload.key,
                        upload.image,
                        upload.width,
                        upload.height,
                    ) != null
                }
                uploaded = uploaded || ok
            } catch (e: Exception) {
                logger.error("An exception occurred while uploading texture ${upload.key}: ${e.message}")
                logger.error(e.stackTraceToString())
            } finally {
                upload.image.close()
            }
        }

        if (uploaded) {
            SkiaContext.submit {
                textureAtlas.endBatch()
            }
            cacheRevision++
        }
    }

    //endregion

    //region 查询与绘制

    /**
     * 获取整张纹理的 Compose [Painter]；未命中缓存时返回 null。
     */
    fun getTexturePainter(
        texture: Identifier,
        filterQuality: FilterQuality = FilterQuality.None,
        expectedSize: IntSize? = null,
    ): Painter? {
        if (RenderBackend.isVulkan) return null
        if (!textureAtlas.contains(texture)) return null
        return TextureAtlasPainter(
            atlas = textureAtlas,
            cacheKey = texture,
            filterQuality = filterQuality,
            expectedSize = expectedSize,
        )
    }

    /**
     * 获取 [IGTexture]（UV 子区域 / 九宫格）的 Compose [Painter]；未命中缓存时返回 null。
     */
    fun getTexturePainter(
        texture: IGTexture,
        filterQuality: FilterQuality = FilterQuality.None,
    ): Painter? {
        if (RenderBackend.isVulkan) return null
        val id = texture.textureInfo.textureId
        if (!textureAtlas.contains(id)) return null
        return TextureAtlasPainter(
            atlas = textureAtlas,
            cacheKey = id,
            filterQuality = filterQuality,
            texture = texture,
            expectedSize = IntSize(texture.uSize, texture.vSize),
        )
    }

    //endregion

    //region 容量修改

    @Volatile
    private var pendingMaxAreaPixels: Long? = null

    /**
     * 请求修改最大缓存像素面积。
     *
     * 只校验并记录待应用值，不直接操作 Surface；连续修改只保留最新值，
     * 由 [processOnRenderThread] 在下一次安全帧边界应用并重建图集。
     */
    fun requestMaxCacheAreaChange(maxAreaPixels: Long) {
        if (maxAreaPixels <= 0) return
        pendingMaxAreaPixels = maxAreaPixels
    }

    //endregion

    //region 资源重载

    @Volatile
    private var pendingInvalidation = false

    private fun invalidateTextureCache() {
        pendingInvalidation = true
    }

    override fun prepare(sharedState: PreparableReloadListener.SharedState) = Unit

    override fun apply(prepared: Unit, sharedState: PreparableReloadListener.SharedState) {
        invalidateTextureCache()
    }

    //endregion

    //region 统计

    val atlasWidth: Int get() = textureAtlas.atlasWidth
    val atlasHeight: Int get() = textureAtlas.atlasHeight
    val atlasAllocatedBytes: Long get() = textureAtlas.atlasAllocatedBytes
    val usedContentPixels: Long get() = textureAtlas.usedContentPixels
    val usedAllocationPixels: Long get() = textureAtlas.usedAllocationPixels
    val activeEntryCount: Int get() = textureAtlas.activeEntryCount
    val pendingUploadCount: Int
        get() = synchronized(pendingUploads) { pendingUploads.size }
    val generation: Long get() = textureAtlas.generationId

    //endregion

    //region 兼容 CPU readback

    /**
     * 显式 CPU 回读 API（不进入图集缓存）。
     *
     * 仅在调用方明确需要独立 [ImageBitmap] 时使用；正常路径请使用
     * [requestTexture] + [getTexturePainter]。
     */
    @Deprecated(
        "请使用 GPU 图集缓存路径（requestTexture + getTexturePainter）。" +
            "本方法会创建 CPU 图像且不进入缓存。",
        ReplaceWith("requestTexture(texture)")
    )
    suspend fun getTextureCache(texture: Identifier): ImageBitmap {
        val bytes = withContext(Dispatchers.IO) {
            resourceManager
                .getResource(texture)
                .getOrNull()
                ?.open()
                ?.readAllBytes()
        }
        val image = if (bytes != null) {
            SkiaImage.makeFromEncoded(bytes)
        } else {
            missingTextureImage()
        }
        return image.use { image ->
            image.toComposeImageBitmap()
        }
    }

    /**
     * 显式 CPU 回读 API（不进入图集缓存），从 [Material] 推导资源标识符。
     */
    @Deprecated(
        "请使用 GPU 图集缓存路径（requestTexture + getTexturePainter）。" +
            "本方法会创建 CPU 图像且不进入缓存。",
        ReplaceWith("requestTexture(material)")
    )
    suspend fun getTextureCache(material: Material): ImageBitmap {
        val texture = material.sprite()
        val resource = Identifier.fromNamespaceAndPath(
            texture.namespace,
            buildString {
                if (!texture.path.startsWith("textures/")) {
                    append("textures/")
                }

                append(texture.path)

                if (!texture.path.endsWith(".png")) {
                    append(".png")
                }
            },
        )
        return getTextureCache(resource)
    }

    //endregion

    /**
     * 生成 2×2 紫色占位纹理（资源缺失时使用）。
     */
    private fun missingTextureImage(): SkiaImage {
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
        return try {
            SkiaImage.makeFromBitmap(bitmap)
        } finally {
            bitmap.close()
        }
    }
}

/**
 * 渲染 Minecraft 纹理为 Compose Image 组件
 *
 * 通过资源管理器异步加载纹理并上传到 GPU 图集，加载完成后触发重组渲染。
 * 相同纹理标识符不会重复解码。
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
    LaunchedEffect(texture) {
        SkiaTextureHelper.requestTexture(texture)
    }

    // 订阅图集版本变化，上传完成后自动重组
    val revision = SkiaTextureHelper.cacheRevision
    val painter = remember(texture, revision) {
        SkiaTextureHelper.getTexturePainter(texture, filterQuality)
    }

    painter?.let {
        Image(
            painter = it,
            contentDescription = "Minecraft Texture",
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
        )
    }
}
