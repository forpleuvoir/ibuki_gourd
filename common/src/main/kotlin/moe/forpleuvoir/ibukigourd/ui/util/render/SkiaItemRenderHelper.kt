package moe.forpleuvoir.ibukigourd.ui.util.render


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import com.mojang.blaze3d.ProjectionType
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.util.SimpleResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.renderer.Projection
import net.minecraft.client.renderer.ProjectionMatrixBuffer
import net.minecraft.client.renderer.item.TrackingItemStackRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import java.util.*
import kotlin.time.TimeSource

object SkiaItemRenderHelper : ClientResourceReloaderListener, SimpleResourceReloaderListener<Unit>() {

    override val identifier: Identifier = identifier("skia_item")

    private val logger = logger()

    private data class ItemCacheKey(
        val componentsHash: Long,
        val width: Int,
        val height: Int
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

    private val itemImageCache = LinkedHashMap<ItemCacheKey, ImageBitmap>(16, 0.75f, true)

    var totalCacheArea: Long by mutableLongStateOf(0)
        private set

    //    private val MAX_CACHE_AREA: Long by IGConfig.Gui.Cache::itemTextureCacheSize
    private const val MAX_CACHE_AREA: Long = 1024 * 1024 * 256 //1GB

    //region 队列渲染

    private data class PendingRenderRequest(
        val cacheKey: ItemCacheKey,
        val itemStack: ItemStack,
        val width: Int,
        val height: Int,
    )

    private val pendingQueue: MutableList<PendingRenderRequest> = mutableListOf()

    private const val MAX_PER_FRAME = 4

    fun requestRender(
        itemStack: ItemStack,
        width: Int = 64,
        height: Int = 64,
    ) {
        val cacheKey = ItemCacheKey.fromItemStack(itemStack, width, height)
        if (itemImageCache.containsKey(cacheKey)) return
        pendingQueue.removeAll {
            it.itemStack == itemStack && it.width == width && it.height == height
        }
        pendingQueue.add(PendingRenderRequest(cacheKey, itemStack, width, height))
    }

    fun getCached(
        itemStack: ItemStack,
        width: Int = 64,
        height: Int = 64,
    ): ImageBitmap? {
        val cacheKey = ItemCacheKey.fromItemStack(itemStack, width, height)
        return itemImageCache[cacheKey]
    }

    fun processOnRenderThread() {
        var processed = 0
        while (pendingQueue.isNotEmpty() && processed < MAX_PER_FRAME) {
            val request = pendingQueue.removeAt(0)
            try {
                renderItemToBufferedImage(request.itemStack, request.width, request.height)
            } catch (e: Exception) {
                logger.error("An exception occurred while rendering queue items: ${e.message}")
                logger.error(e.stackTraceToString())
            }
            processed++
        }
    }

    //endregion

    fun renderItemToBufferedImage(
        itemStack: ItemStack,
        width: Int = 64,
        height: Int = 64
    ): ImageBitmap {
        val now = TimeSource.Monotonic.markNow()
        val cacheKey = ItemCacheKey.fromItemStack(itemStack, width, height)
        itemImageCache[cacheKey]?.let { return it }

        val target = OffscreenRenderTarget("skia_item", width, height)
        val device = RenderSystem.getDevice()
        val encoder = device.createCommandEncoder()

        try {
            encoder.createRenderPass(
                { "skia_item_clear" },
                target.requireColorTextureView(), OptionalInt.of(0),
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
                mc.gameRenderer.lighting.setupFor(lighting)

                RenderSystem.enableScissorForRenderTypeDraws(0, 0, width, height)

                val bufferSource = mc.renderBuffers().bufferSource()

                // 箱子/盾牌/旗帜/装饰罐等通过 special renderer 提交 ModelSubmit/BlockModelSubmit，
                // ItemFeatureRenderer 只处理 ItemSubmit，因此必须走 FeatureRenderDispatcher.renderAllFeatures
                // 才能让 ModelFeatureRenderer / BlockFeatureRenderer 等各自处理对应提交类型。
                val featureRenderDispatcher = mc.gameRenderer.featureRenderDispatcher
                val submitNodeStorage = featureRenderDispatcher.submitNodeStorage
                itemState.submit(poseStack, submitNodeStorage, 0xF000F0, OverlayTexture.NO_OVERLAY, 0)
                featureRenderDispatcher.renderAllFeatures()

                bufferSource.endBatch()

            } finally {
                RenderSystem.disableScissorForRenderTypeDraws()
                RenderSystem.outputColorTextureOverride = prevColor
                RenderSystem.outputDepthTextureOverride = prevDepth
                if (prevProj != null) RenderSystem.setProjectionMatrix(prevProj, prevProjType)
                projBuffer.close()
            }

            val srcTex = target.requireColorTexture()
            val pixelSize = srcTex.format.pixelSize()
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
            encoder.mapBuffer(pbo, true, false).use { mapped ->
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

            val result = Bitmap().apply {
                allocPixels(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL))
                installPixels(pixels)
            }.asComposeImageBitmap()

            logger.devInfo("ItemImage buffer conversion: ${tPixelCopy.elapsedNow()}")

            val entryArea = width * height
            while (totalCacheArea + entryArea > MAX_CACHE_AREA && itemImageCache.isNotEmpty()) {
                val eldest = itemImageCache.entries.first()
                totalCacheArea -= eldest.key.width * eldest.key.height
                itemImageCache.remove(eldest.key)
            }
            totalCacheArea += entryArea
            itemImageCache[cacheKey] = result
            return result
        } finally {
            target.dispose()
            logger.devInfo("Create ItemImage buffer: ${now.elapsedNow()}")
        }
    }

    internal fun invalidateItemImageCache() {
        itemImageCache.clear()
        totalCacheArea = 0
        logger.info("Invalidate ItemImage cache")
    }

    override fun prepare(sharedState: PreparableReloadListener.SharedState) = Unit

    override fun apply(prepared: Unit, sharedState: PreparableReloadListener.SharedState) {
        invalidateItemImageCache()
    }


}
