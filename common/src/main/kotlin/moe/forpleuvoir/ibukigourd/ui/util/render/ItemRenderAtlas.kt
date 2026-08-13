package moe.forpleuvoir.ibukigourd.ui.util.render

import com.mojang.blaze3d.systems.RenderSystem
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaContext
import moe.forpleuvoir.ibukigourd.ui.skia.backend.SkiaRenderTarget
import moe.forpleuvoir.ibukigourd.util.logger
import net.minecraft.world.item.ItemStack
import org.jetbrains.skia.*
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * 物品缓存键。
 *
 * 本次仅迁移缓存载体，不重构缓存键算法；哈希碰撞、数量是否参与缓存键等
 * 独立问题留待后续任务处理。
 */
internal data class ItemCacheKey(
    val componentsHash: Long,
    val width: Int,
    val height: Int,
) {
    companion object {
        fun fromItemStack(itemStack: ItemStack, width: Int, height: Int): ItemCacheKey {
            var hash = 0L
            for (component in itemStack.components) {
                hash = 31 * hash + component.hashCode()
            }
            return ItemCacheKey(hash, width, height)
        }
    }
}

/**
 * 物品渲染图集配置。
 *
 * [maxAreaPixels] 为用户可配置的最大缓存像素面积（不含对齐产生的额外空间），
 * 其 RGBA8 显存占用约为 `maxAreaPixels * 4` 字节。
 */
internal data class AtlasConfig(
    val maxAreaPixels: Long,
    val padding: Int = 1,
)

/**
 * 图集中的一个条目。
 *
 * @property allocation 包含边缘 padding 的完整占用区域，用于释放和复用
 * @property content    真正保存物品像素的区域，用于绘制
 * @property generation 图集整体清空或重建后递增，用于让旧 Painter/旧引用安全失效
 */
internal data class AtlasEntry(
    val allocation: AtlasRect,
    val content: AtlasRect,
    val generation: Long,
)

/**
 * 物品渲染 GPU 图集。
 *
 * 维护单张由后端渲染目标（GL FBO / 未来 Vulkan Image）承载的 GPU 图集，
 * 并管理 `K → AtlasEntry` 的 access-order LRU。
 *
 * 所有 GPU 操作（创建、上传、快照替换、释放）必须位于 [SkiaContext.submit]
 * 的渲染线程与对应后端上下文中。
 */
internal class ItemRenderAtlas<K>(
    private val config: AtlasConfig,
) {

    private val logger = logger()

    /** 图集纹理尺寸对齐单位 */
    private val alignment = 64

    // ── GPU 资源 ───────────────────────────────────────────────────────
    private var target: SkiaRenderTarget? = null

    // ── 图集状态 ───────────────────────────────────────────────────────
    private var allocator: AtlasRectAllocator? = null
    private val entries = LinkedHashMap<K, AtlasEntry>(16, 0.75f, true)

    private var generation: Long = 0L
    var atlasWidth: Int = 0
        private set
    var atlasHeight: Int = 0
        private set

    /** 用户请求的最大缓存像素面积 */
    var requestedMaxAreaPixels: Long = config.maxAreaPixels
        private set

    /** 受 GPU 单纹理能力限制后实际生效的最大缓存像素面积 */
    var effectiveMaxAreaPixels: Long = config.maxAreaPixels
        private set

    // ── 统计 ───────────────────────────────────────────────────────────
    var usedContentPixels: Long = 0L
        private set
    var usedAllocationPixels: Long = 0L
        private set
    var evictionCount: Long = 0L
        private set
    var fragmentationResetCount: Long = 0L
        private set

    val activeEntryCount: Int get() = entries.size
    val atlasAllocatedBytes: Long
        get() = atlasWidth.toLong() * atlasHeight * ATLAS_BYTES_PER_PIXEL
    val estimatedConfiguredBytes: Long
        get() = effectiveMaxAreaPixels * ATLAS_BYTES_PER_PIXEL
    val freePixels: Long get() = allocator?.freePixels ?: 0L
    val largestFreeRectPixels: Long get() = allocator?.largestFreeRectPixels ?: 0L
    val generationId: Long get() = generation

    /**
     * 确保图集已初始化。若目标不存在，则通过当前后端创建。
     */
    fun ensureInitialized() {
        if (target != null) return
        if (config.maxAreaPixels <= 0) return

        val maxTextureSize = RenderSystem.getDevice()
            .deviceInfo
            .limits()
            .maxTextureSize()
            .coerceAtLeast(1024)
        val effectiveMaxAreaPixels = minOf(requestedMaxAreaPixels, maxTextureSize.toLong() * maxTextureSize)
        if (effectiveMaxAreaPixels != requestedMaxAreaPixels) {
            logger.warn(
                "ItemRenderAtlas: requested cache area $requestedMaxAreaPixels pixels exceeds single texture capability" +
                    " (maxTextureSize=$maxTextureSize, max area=${maxTextureSize.toLong() * maxTextureSize})," +
                    " clamped to $effectiveMaxAreaPixels pixels"
            )
        }
        this.effectiveMaxAreaPixels = effectiveMaxAreaPixels

        val size = calculateAtlasSize(effectiveMaxAreaPixels, maxTextureSize, alignment)
        atlasWidth = size.width
        atlasHeight = size.height

        val created = SkiaContext.current.createAtlasTarget(atlasWidth, atlasHeight)
        target = created
        allocator = AtlasRectAllocator(atlasWidth, atlasHeight)
        created.skiaSurface.canvas.clear(0)
        created.flushAndSubmit()

        logger.info(
            "ItemRenderAtlas: created atlas ${atlasWidth}x${atlasHeight}," +
                " requested ${requestedMaxAreaPixels} pixels / effective ${effectiveMaxAreaPixels} pixels," +
                " estimated video memory ~${atlasAllocatedBytes / (1024 * 1024)} MiB"
        )
    }

    fun contains(key: K): Boolean = entries.containsKey(key)

    /**
     * 查询条目，命中时更新 access-order 顺序。
     */
    fun get(key: K): AtlasEntry? = entries[key]

    /**
     * 上传一个临时 GPU/CPU Image 到图集。
     *
     * @param width  content 区域宽度（不含 padding）
     * @param height content 区域高度（不含 padding）
     * @return 上传成功后返回条目；key 已存在时直接返回已有条目；失败返回 null
     */
    fun upload(
        key: K,
        source: Image,
        width: Int,
        height: Int,
    ): AtlasEntry? {
        ensureInitialized()
        val s = target?.skiaSurface ?: return null
        val alloc = allocator ?: return null

        entries[key]?.let { return it }

        val padding = config.padding
        val allocWidth = width + padding * 2
        val allocHeight = height + padding * 2
        if (allocWidth > atlasWidth || allocHeight > atlasHeight) {
            logger.warn(
                "ItemRenderAtlas: rejected oversized request ${width}x$height (with padding ${allocWidth}x$allocHeight)," +
                    " exceeds atlas ${atlasWidth}x$atlasHeight"
            )
            return null
        }

        var rect = alloc.allocate(allocWidth, allocHeight)
        if (rect == null) {
            // LRU 淘汰直到可以分配
            while (rect == null && entries.isNotEmpty()) {
                val eldest = entries.entries.first()
                entries.remove(eldest.key)
                alloc.release(eldest.value.allocation)
                usedAllocationPixels -= eldest.value.allocation.area
                usedContentPixels -= eldest.value.content.area
                evictionCount++
                rect = alloc.allocate(allocWidth, allocHeight)
            }
        }
        if (rect == null) {
            // 空闲面积充足但碎片化：整体失效并按需重建
            clearAll(s)
            fragmentationResetCount++
            rect = allocator?.allocate(allocWidth, allocHeight) ?: return null
        }

        val content = AtlasRect(
            x = rect.x + padding,
            y = rect.y + padding,
            width = width,
            height = height,
        )

        try {
            // 清理整个 allocation 为透明，避免复用旧区域时残留像素
            s.canvas.drawRect(
                Rect(
                    rect.x.toFloat(), rect.y.toFloat(),
                    rect.right.toFloat(), rect.bottom.toFloat()
                ),
                clearPaint
            )
            s.canvas.drawImageRect(
                source,
                Rect(0f, 0f, width.toFloat(), height.toFloat()),
                Rect(
                    content.x.toFloat(), content.y.toFloat(),
                    content.right.toFloat(), content.bottom.toFloat()
                ),
                SamplingMode.DEFAULT,
                null,
                true
            )
        } catch (e: Exception) {
            // 上传失败：归还区域、不写入 LRU、不递增统计
            alloc.release(rect)
            logger.error("ItemRenderAtlas: upload failed ${width}x$height: ${e.message}")
            return null
        }

        val entry = AtlasEntry(
            allocation = rect,
            content = content,
            generation = generation,
        )
        entries[key] = entry
        usedAllocationPixels += rect.area
        usedContentPixels += content.area
        return entry
    }

    /**
     * 将 [key] 对应图集子区域绘制到 [canvas] 的 [destination] 区域。
     *
     * entry 不存在、generation 失效或图集未初始化时返回 false 且不绘制。
     */
    fun draw(
        canvas: org.jetbrains.skia.Canvas,
        key: K,
        destination: Rect,
        samplingMode: SamplingMode,
        paint: Paint?,
        /**
         * 相对 content 区域的源子区域；为 null 时绘制整个 content。
         * 供纹理 UV 子区域与九宫格等需要局部绘制的调用方使用。
         */
        source: AtlasRect? = null,
    ): Boolean {
        val image = target?.textureImage ?: return false
        val entry = entries[key] ?: return false
        if (entry.generation != generation) return false

        val content = entry.content
        val srcRect = if (source != null) {
            Rect(
                (content.x + source.x).toFloat(),
                (content.y + source.y).toFloat(),
                (content.x + source.right).toFloat(),
                (content.y + source.bottom).toFloat(),
            )
        } else {
            Rect(
                content.x.toFloat(), content.y.toFloat(),
                content.right.toFloat(), content.bottom.toFloat()
            )
        }
        canvas.drawImageRect(
            image,
            srcRect,
            destination,
            samplingMode,
            paint,
            true
        )
        return true
    }

    /**
     * 批次写入完成后调用：将图集表面内容提交到 GPU，使纹理可见新内容。
     */
    fun endBatch() {
        target?.flushAndSubmit()
    }

    /**
     * 修改最大缓存像素面积。
     *
     * 立即销毁旧图集资源并重置条目；下一次 [ensureInitialized] 时按新值重建。
     * 必须在渲染线程与对应后端上下文中调用。
     */
    fun reconfigure(newMaxAreaPixels: Long) {
        if (newMaxAreaPixels <= 0) return
        if (newMaxAreaPixels == requestedMaxAreaPixels && target != null) return
        requestedMaxAreaPixels = newMaxAreaPixels
        closeResources()
        clearAllState()
        logger.info("ItemRenderAtlas: cache area changed to $newMaxAreaPixels pixels, atlas will be rebuilt")
        ensureInitialized()
    }

    /**
     * 使图集整体失效：清空条目、释放 GPU 资源并递增 generation。
     * 下一次上传时延迟重建。
     */
    fun invalidate() {
        closeResources()
        clearAllState()
        logger.info("ItemRenderAtlas: atlas has been fully invalidated")
    }

    /**
     * 释放全部 GPU 资源并清空状态。对象不可再使用。
     */
    fun close() {
        closeResources()
        clearAllState()
    }

    // ── 内部工具 ───────────────────────────────────────────────────────

    private fun clearAll(surface: Surface) {
        entries.clear()
        usedAllocationPixels = 0L
        usedContentPixels = 0L
        generation++
        allocator = AtlasRectAllocator(atlasWidth, atlasHeight)
        surface.canvas.clear(0)
        logger.info("ItemRenderAtlas: heavy fragmentation triggered full clear, generation incremented to $generation")
    }

    private fun clearAllState() {
        entries.clear()
        allocator = null
        usedAllocationPixels = 0L
        usedContentPixels = 0L
        atlasWidth = 0
        atlasHeight = 0
        generation++
    }

    private fun closeResources() {
        target?.let {
            try {
                it.close()
            } catch (_: Exception) {
            }
        }
        target = null
    }

    private val clearPaint = Paint().apply {
        color = 0
        blendMode = BlendMode.SRC
    }

    private companion object {
        const val ATLAS_BYTES_PER_PIXEL = 4L
    }
}

/**
 * 根据请求的像素面积计算单张图集尺寸。
 *
 * 规则：
 * 1. 以覆盖面积的近似正方形宽度为基准；
 * 2. 宽度向上取不超过 [maxTextureSize] 的 2 次幂；
 * 3. 高度为 `ceilDiv(requestedAreaPixels, width)` 并向上对齐到 [alignment]；
 * 4. 宽高均不超过 [maxTextureSize]，请求面积超过单张纹理能力时按整张纹理限制。
 */
internal fun calculateAtlasSize(
    requestedAreaPixels: Long,
    maxTextureSize: Int,
    alignment: Int = 64,
): androidx.compose.ui.unit.IntSize {
    require(requestedAreaPixels > 0) { "requestedAreaPixels 必须大于 0" }
    require(maxTextureSize > 0) { "maxTextureSize 必须大于 0" }
    require(alignment > 0) { "alignment 必须大于 0" }

    val maxArea = maxTextureSize.toLong() * maxTextureSize
    val area = minOf(requestedAreaPixels, maxArea)

    // 近似正方形宽度
    val approxWidth = ceil(sqrt(area.toDouble())).toLong()
    // 向上取 2 次幂，不超过 maxTextureSize
    var width = 1L
    while (width < approxWidth && width < maxTextureSize) width = width shl 1
    width = minOf(width, maxTextureSize.toLong())

    var height = alignUp(ceilDiv(area, width), alignment.toLong())
    if (height > maxTextureSize) {
        // 增大宽度直到高度可容纳
        while (width < maxTextureSize && alignUp(ceilDiv(area, width), alignment.toLong()) > maxTextureSize) {
            width = (width shl 1).coerceAtMost(maxTextureSize.toLong())
        }
        height = alignUp(ceilDiv(area, width), alignment.toLong()).coerceAtMost(maxTextureSize.toLong())
        if (width * height < area) {
            width = maxTextureSize.toLong()
            height = maxTextureSize.toLong()
        }
    }
    if (width * height < area) {
        width = maxTextureSize.toLong()
        height = maxTextureSize.toLong()
    }

    return androidx.compose.ui.unit.IntSize(width.toInt(), height.toInt())
}

private fun ceilDiv(a: Long, b: Long): Long = (a + b - 1) / b

private fun alignUp(value: Long, alignment: Long): Long =
    ((value + alignment - 1) / alignment) * alignment
