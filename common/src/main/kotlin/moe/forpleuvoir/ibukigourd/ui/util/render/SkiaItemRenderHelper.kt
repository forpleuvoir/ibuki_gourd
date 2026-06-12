package moe.forpleuvoir.ibukigourd.ui.util.render


import androidx.compose.ui.graphics.ImageBitmap
import com.mojang.blaze3d.ProjectionType
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.SimpleResourceReloaderListener
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.feature.ItemFeatureRenderer
import net.minecraft.client.renderer.item.TrackingItemStackRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import kotlin.time.TimeSource

object SkiaItemRenderHelper : ClientResourceReloaderListener, SimpleResourceReloaderListener<Unit>() {

    override val identifier: Identifier = identifier("skia_item")

    private val logger = logger()

    private data class ItemCacheKey(
        val itemModel: Identifier?,
        val player: Player?,
        val world: Level?,
        val width: Int,
        val height: Int,
        val hasFoil: Boolean
    ) {
        companion object {
            fun fromItemStack(itemStack: ItemStack, width: Int, height: Int): ItemCacheKey {
                val model = itemStack.components.get(DataComponents.ITEM_MODEL)
                return ItemCacheKey(model, mc.player, mc.level, width, height, itemStack.hasFoil())
            }
        }
    }

    private val itemImageCache = LinkedHashMap<ItemCacheKey, ImageBitmap>(16, 0.75f, false)
    private var totalCacheArea: Long = 0
    private const val MAX_CACHE_AREA: Long = 16_777_216

    fun renderItemToBufferedImage(
        itemStack: ItemStack,
        width: Int = 64,
        height: Int = 64
    ): ImageBitmap {
        val now = TimeSource.Monotonic.markNow()
        val cacheKey = ItemCacheKey.fromItemStack(itemStack, width, height)
        itemImageCache[cacheKey]?.let { return it }

        val oldGuiScale = mc.window.guiScale
        mc.window.guiScale = 2
        val target = OffscreenRenderTarget("skia_item", width, height)
        val device = RenderSystem.getDevice()
        val encoder = device.createCommandEncoder()

        try {
            encoder.clearColorAndDepthTextures(
                target.requireColorTexture(), 0,
                target.requireDepthTexture(), 1.0
            )

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
                poseStack.scale(width.toFloat(), -width.toFloat(), 1f)

                mc.gameRenderer.lighting.setupFor(Lighting.Entry.ITEMS_FLAT)
                RenderSystem.enableScissorForRenderTypeDraws(0, 0, width, height)

                val byteBuffer = ByteBufferBuilder(786432)
                val bufferSource = MultiBufferSource.immediate(byteBuffer)
                val outlineBufferSource = OutlineBufferSource()

                val submitCollector = SubmitNodeStorage()
                itemState.submit(poseStack, submitCollector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0)

                val itemRenderer = ItemFeatureRenderer()
                for (collection in submitCollector.submitsPerOrder.values) {
                    itemRenderer.renderSolid(collection, bufferSource, outlineBufferSource)
                    itemRenderer.renderTranslucent(collection, bufferSource, outlineBufferSource)
                }

                bufferSource.endBatch()
                byteBuffer.close()

                RenderSystem.disableScissorForRenderTypeDraws()
            } finally {
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
            val fence = device.createCommandEncoder().createFence()
            while (!fence.awaitCompletion(1)) {
                /*await*/
            }
            fence.close()

            val nativeImage = NativeImage(width, height, false)
            encoder.mapBuffer(pbo, true, false).use { mapped ->
                val data = mapped.data()
                for (y in 0 until height) for (x in 0 until width) {
                    val abgr = data.getInt((x + y * width) * pixelSize)
                    nativeImage.setPixelABGR(x, height - y - 1, abgr or 0x00000000)
                }
            }
            pbo.close()

            val result =  nativeImage.toComposeImageBitmap()

            nativeImage.close()

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
            mc.window.guiScale = oldGuiScale
            logger.info("Create ItemImage buffer: ${now.elapsedNow()}")
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
