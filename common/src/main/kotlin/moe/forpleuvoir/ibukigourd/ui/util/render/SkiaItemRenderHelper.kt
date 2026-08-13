package moe.forpleuvoir.ibukigourd.ui.util.render


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import com.mojang.blaze3d.ProjectionType
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaContext
import moe.forpleuvoir.ibukigourd.util.SimpleResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.renderer.Projection
import net.minecraft.client.renderer.ProjectionMatrixBuffer
import net.minecraft.client.renderer.SubmitNodeStorage
import net.minecraft.client.renderer.item.TrackingItemStackRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.joml.Vector4f
import java.util.*
import kotlin.time.TimeSource

/**
 * 物品渲染 GPU 图集缓存。
 *
 * 保留 ItemStack 完整模型渲染、special renderer 与 PBO 回读流程；长期缓存由
 * 单张 GPU [ItemRenderAtlas] 承载，不再按条目持有 CPU [ImageBitmap]。
 *
 * 线程模型：
 * - [requestRender] / [getPainter] 供 Compose 侧调用；
 * - [processOnRenderThread] 必须在渲染线程调用（由场景渲染器驱动），
 *   其中 GPU 相关操作通过 [SkiaContext.submit] 在共享 GL 上下文中执行。
 */
object SkiaItemRenderHelper : ClientResourceReloaderListener, SimpleResourceReloaderListener<Unit>() {

    override val identifier: Identifier = identifier("skia_item")

    private val logger = logger()

    // 用户配置的最大缓存像素面积（RGBA8 每像素 4 字节，134_217_728 像素 ≈ 512 MiB）
    private const val DEFAULT_MAX_CACHE_AREA: Long = 1024 * 1024 * 128

    private val atlas = ItemRenderAtlas<ItemCacheKey>(AtlasConfig(DEFAULT_MAX_CACHE_AREA))

    /**
     * 兼容统计：当前缓存内容像素面积（不含 padding）。
     * 与历史语义一致，仍表示像素个数而非字节数。
     */
    var totalCacheArea: Long by mutableLongStateOf(0)
        private set

    /**
     * 图集可观察版本。每批次成功上传并产生新内容后递增一次，
     * 等待缓存的 Composable 通过读取此值触发重组。
     */
    var cacheRevision by mutableLongStateOf(0L)
        private set

    //region 队列渲染

    private enum class RenderResult {
        /** 已存在缓存，无需上传 */
        ALREADY_CACHED,

        /** 成功上传到图集 */
        UPLOADED,

        /** 瞬时失败，允许后续帧重试 */
        RETRY,

        /** 永久拒绝（如超大请求），不再重试 */
        REJECTED,
    }

    private data class PendingRenderRequest(
        val cacheKey: ItemCacheKey,
        val itemStack: ItemStack,
        val width: Int,
        val height: Int,
        /** 已重试次数，达到上限后丢弃，避免永久失败的请求每帧刷屏 */
        val retryCount: Int = 0,
    )

    private val pendingQueue: MutableList<PendingRenderRequest> = mutableListOf()

    private const val MAX_PER_FRAME = 4

    /** 单个请求的最大重试帧数，超过后丢弃并记录日志 */
    private const val MAX_RETRY_COUNT = 10

    /**
     * 提交一个物品渲染请求。
     *
     * 使用与缓存一致的 [ItemCacheKey] 去重，避免队列判等语义与缓存判等语义不一致。
     */
    fun requestRender(
        itemStack: ItemStack,
        width: Int = 64,
        height: Int = 64,
    ) {
        val cacheKey = ItemCacheKey.fromItemStack(itemStack, width, height)
        if (atlas.contains(cacheKey)) return
        pendingQueue.removeAll { it.cacheKey == cacheKey }
        pendingQueue.add(PendingRenderRequest(cacheKey, itemStack, width, height))
    }

    /**
     * 查询已缓存的图集条目。
     *
     * entry 为内部类型，UI 层应优先使用 [getPainter]。
     */
    internal fun getCached(
        itemStack: ItemStack,
        width: Int = 64,
        height: Int = 64,
    ): AtlasEntry? {
        val cacheKey = ItemCacheKey.fromItemStack(itemStack, width, height)
        return atlas.get(cacheKey)
    }

    /**
     * 获取绘制图集子区域的 Compose [Painter]；未命中缓存时返回 null。
     */
    fun getPainter(
        itemStack: ItemStack,
        width: Int = 64,
        height: Int = 64,
        filterQuality: FilterQuality = FilterQuality.None,
    ): Painter? {
        val cacheKey = ItemCacheKey.fromItemStack(itemStack, width, height)
        if (!atlas.contains(cacheKey)) return null
        return ItemAtlasPainter(atlas, cacheKey, IntSize(width, height), filterQuality)
    }

    /**
     * 渲染线程逐帧驱动。
     *
     * 顺序：
     * 1. 应用待处理的缓存面积修改（若存在）；
     * 2. 处理资源重载失效；
     * 3. 最多处理 [MAX_PER_FRAME] 个渲染请求；
     * 4. 批次结束统一提交 GPU 并递增 revision。
     */
    fun processOnRenderThread() {
        // 1. 应用容量修改：先重建图集，再处理当帧上传
        val pendingArea = pendingMaxAreaPixels
        if (pendingArea != null) {
            pendingMaxAreaPixels = null
            if (pendingArea != atlas.requestedMaxAreaPixels) {
                SkiaContext.submit {
                    atlas.reconfigure(SkiaContext.sharedContext, pendingArea)
                }
            }
        }

        // 2. 资源重载失效
        if (pendingInvalidation) {
            pendingInvalidation = false
            pendingQueue.clear()
            SkiaContext.submit {
                atlas.invalidate()
            }
            totalCacheArea = 0
            cacheRevision++
        }

        // 3. 队列渲染（单帧上限）
        var uploaded = false
        var processed = 0
        while (pendingQueue.isNotEmpty() && processed < MAX_PER_FRAME) {
            val request = pendingQueue.removeAt(0)
            val result = try {
                renderItemToAtlas(request.itemStack, request.width, request.height)
            } catch (e: Exception) {
                logger.error(
                    "An exception occurred while rendering queue item " +
                        "${request.width}x${request.height}: ${e.message}"
                )
                logger.error(e.stackTraceToString())
                RenderResult.RETRY
            }
            when (result) {
                RenderResult.ALREADY_CACHED,
                RenderResult.REJECTED -> Unit

                RenderResult.UPLOADED -> {
                    uploaded = true
                }
                RenderResult.RETRY -> {
                    // 瞬时失败允许重试，但达到上限后丢弃，避免异常请求每帧反复重试
                    if (request.retryCount < MAX_RETRY_COUNT) {
                        pendingQueue.add(request.copy(retryCount = request.retryCount + 1))
                    } else {
                        logger.error(
                            "Dropping item render request ${request.width}x${request.height} " +
                                "after $MAX_RETRY_COUNT retries"
                        )
                    }
                }
            }
            processed++
        }

        // 4. 批次结束统一提交，只有真实上传过才更新可观察状态
        if (uploaded) {
            SkiaContext.submit {
                atlas.endBatch()
            }
            totalCacheArea = atlas.usedContentPixels
            cacheRevision++
        }
    }

    //endregion

    //region 渲染与上传

    /**
     * 渲染物品并写入 GPU 图集。
     *
     * 模型渲染、投影、光照与 PBO 回读保持原逻辑；末尾将临时 CPU 像素上传到图集，
     * 不再生成或长期持有 [ImageBitmap]。
     *
     * @return 是否成功写入图集；已命中缓存时返回 false（无需上传）
     */
    private fun renderItemToAtlas(
        itemStack: ItemStack,
        width: Int,
        height: Int,
    ): RenderResult {
        val cacheKey = ItemCacheKey.fromItemStack(itemStack, width, height)
        if (atlas.contains(cacheKey)) return RenderResult.ALREADY_CACHED

        val pixels = renderItemPixels(itemStack, width, height) ?: return RenderResult.RETRY

        val now = TimeSource.Monotonic.markNow()
        // 短生命周期 CPU 中转，上传后立即释放
        val bitmap = Bitmap().apply {
            allocPixels(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL))
            installPixels(pixels)
        }
        val image = Image.makeFromBitmap(bitmap)
        try {
            var uploaded = false
            SkiaContext.submit {
                uploaded = atlas.upload(
                    SkiaContext.sharedContext,
                    cacheKey,
                    image,
                    width,
                    height,
                ) != null
            }
            logger.devInfo("Atlas upload: ${now.elapsedNow()}")
            return if (uploaded) RenderResult.UPLOADED else RenderResult.REJECTED
        } finally {
            image.close()
            bitmap.close()
        }
    }

    /**
     * 渲染物品模型到离屏目标并通过 PBO/fence 回读 BGRA 像素。
     * 调用方负责在渲染线程执行。
     *
     * @return BGRA 字节数组；渲染失败时返回 null
     */
    private fun renderItemPixels(
        itemStack: ItemStack,
        width: Int,
        height: Int,
    ): ByteArray? {
        val now = TimeSource.Monotonic.markNow()
        val target = OffscreenRenderTarget("skia_item", width, height)
        val device = RenderSystem.tryGetDevice() ?: return null
        val encoder = device.createCommandEncoder()

        try {
            encoder.createRenderPass(
                { "skia_item_clear" },
                target.requireColorTextureView(), Optional.of(Vector4f(0.0f, 0.0f, 0.0f, 1.0f)),
                target.requireDepthTextureView(), OptionalDouble.of(1.0)
            ).use { }

            val itemState = TrackingItemStackRenderState()
            mc.itemModelResolver.updateForTopItem(
                itemState, itemStack, ItemDisplayContext.GUI,
                mc.level, mc.player, 0
            )

            val prevColor = RenderSystem.outputColorTextureOverride
            val prevDepth = RenderSystem.outputDepthTextureOverride
            val prevProj = RenderSystem.getProjectionMatrixBuffer()
            val prevProjType = RenderSystem.getProjectionType()

            val projection = Projection()
            projection.setupOrtho(-1000f, 1000f, width.toFloat(), height.toFloat(), true)
            val projBuffer = ProjectionMatrixBuffer("item_render_proj")

            try {
                RenderSystem.outputColorTextureOverride = target.requireColorTextureView()
                RenderSystem.outputDepthTextureOverride = target.requireDepthTextureView()
                RenderSystem.setProjectionMatrix(projBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC)

                val poseStack = PoseStack()
                poseStack.translate(width / 2.0, height / 2.0, 0.0)
                poseStack.scale(width.toFloat(), -width.toFloat(), width.toFloat())

                val lighting = if (itemState.usesBlockLight()) Lighting.Entry.ITEMS_3D else Lighting.Entry.ITEMS_FLAT
                mc.gameRenderer.lighting().setupFor(lighting)

                RenderSystem.enableScissorForRenderTypeDraws(0, 0, width, height)

                // 箱子/盾牌/旗帜/装饰罐等通过 special renderer 提交 ModelSubmit/BlockModelSubmit，
                // ItemFeatureRenderer 只处理 ItemSubmit，因此必须走 FeatureRenderDispatcher.renderAllFeatures
                // 才能让 ModelFeatureRenderer / BlockFeatureRenderer 等各自处理对应提交类型。
                val featureRenderDispatcher = mc.gameRenderer.featureRenderDispatcher()
                val submitNodeStorage = SubmitNodeStorage()
                itemState.submit(poseStack, submitNodeStorage, 0xF000F0, OverlayTexture.NO_OVERLAY, 0)
                featureRenderDispatcher.renderAllFeatures(submitNodeStorage)

            } finally {
                RenderSystem.disableScissorForRenderTypeDraws()
                RenderSystem.outputColorTextureOverride = prevColor
                RenderSystem.outputDepthTextureOverride = prevDepth
                if (prevProj != null) RenderSystem.setProjectionMatrix(prevProj, prevProjType)
                projBuffer.close()
            }

            val srcTex = target.requireColorTexture()
            val pixelSize = srcTex.format.blockSize()
            val bufSize = width * height * pixelSize
            val pbo = device.createBuffer({ "readback" }, 9, bufSize.toLong())

            encoder.copyTextureToBuffer(srcTex, pbo, 0L, {}, 0)
            val fence = encoder.createFence()
            while (!fence.awaitCompletion(0)) {
                /*await*/
            }
            fence.close()

            val pixels = ByteArray(width * height * pixelSize)

            val tPixelCopy = TimeSource.Monotonic.markNow()

            // GPU RGBA8 小端读回 int = 0xAABBGGRR；Skia N32 在本平台为 BGRA 字节序，
            // 故输出 [B,G,R,A]。内联位运算以避免每像素分配 Color 对象。
            val mapped = pbo.map(true, false)
            mapped.use { mapped ->
                val data = mapped.data()
                var k = 0
                for (y in height - 1 downTo 0) for (x in 0 until width) {
                    val v = data.getInt((x + y * width) * pixelSize)
                    pixels[k++] = (v ushr 16).toByte() // B
                    pixels[k++] = (v ushr 8).toByte()  // G
                    pixels[k++] = v.toByte()           // R
                    pixels[k++] = (v ushr 24).toByte() // A
                }
            }
            pbo.close()

            logger.devInfo("ItemImage buffer conversion: ${tPixelCopy.elapsedNow()}")
            return pixels
        } finally {
            target.dispose()
            logger.devInfo("Render ItemImage buffer: ${now.elapsedNow()}")
        }
    }

    /**
     * 显式 CPU 回读 API（不进入图集缓存）。
     *
     * 仅在调用方明确需要独立 [ImageBitmap] 时使用；正常路径请使用 [getPainter]。
     */
    @Deprecated(
        "请使用 GPU 图集缓存路径（requestRender + getPainter）。" +
            "本方法会创建 CPU 图像且不进入缓存。",
        ReplaceWith("getPainter(itemStack, width, height)")
    )
    fun renderItemToBufferedImage(
        itemStack: ItemStack,
        width: Int = 64,
        height: Int = 64,
    ): ImageBitmap {
        val pixels = renderItemPixels(itemStack, width, height)
            ?: error("Failed to render item to buffered image")
        return Bitmap().apply {
            allocPixels(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL))
            installPixels(pixels)
        }.asComposeImageBitmap()
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

    /**
     * 使缓存失效。资源重载时调用，仅标记待处理状态；
     * 实际 GPU 资源释放由 [processOnRenderThread] 在渲染线程执行。
     */
    internal fun invalidateItemImageCache() {
        pendingInvalidation = true
    }

    override fun prepare(sharedState: PreparableReloadListener.SharedState) = Unit

    override fun apply(prepared: Unit, sharedState: PreparableReloadListener.SharedState) {
        invalidateItemImageCache()
    }

    //endregion

    //region 统计

    val atlasWidth: Int get() = atlas.atlasWidth
    val atlasHeight: Int get() = atlas.atlasHeight
    val atlasAllocatedBytes: Long get() = atlas.atlasAllocatedBytes
    val requestedMaxAreaPixels: Long get() = atlas.requestedMaxAreaPixels
    val effectiveMaxAreaPixels: Long get() = atlas.effectiveMaxAreaPixels
    val estimatedConfiguredBytes: Long get() = atlas.estimatedConfiguredBytes
    val usedContentPixels: Long get() = atlas.usedContentPixels
    val usedAllocationPixels: Long get() = atlas.usedAllocationPixels
    val activeEntryCount: Int get() = atlas.activeEntryCount
    val freePixels: Long get() = atlas.freePixels
    val largestFreeRectPixels: Long get() = atlas.largestFreeRectPixels
    val pendingRequestCount: Int get() = pendingQueue.size
    val generation: Long get() = atlas.generationId

    //endregion
}
