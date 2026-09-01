package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier

/**
 * 一次 atlas 上传的全部准备数据（由 SokitsuAtlasManager 构建，多线程阶段产出）。
 *
 * [regions] 的顺序即图层声明顺序的扁平展开（Manager 保证），用于 [SokitsuAtlasTexture.sprites]
 * 保持渲染所需的有序列表。
 */
data class SokitsuAtlasPreparations(
    val width: Int,
    val height: Int,
    val padding: Int,
    val regions: List<SokitsuAtlasRegion>
)

/**
 * atlas 内一个图层区域：image 为待上传的图层图，x/y 为缝合后的内容区坐标（含 padding 偏移）。
 */
data class SokitsuAtlasRegion(
    val textureId: Identifier,
    val layerId: String,
    val x: Int,
    val y: Int,
    val image: NativeImage
)

/**
 * Sokitsu atlas 纹理（自建实现，参考原版 TextureAtlas 的形态：GpuTexture 上传 + 按名查 sprite + missing 兜底）。
 *
 * - [upload] 在渲染线程调用：创建 GpuTexture（RGBA8_UNORM，clamp-to-edge + NEAREST 采样），逐区域 blit 上传
 * - NativeImage 的生命周期由调用方（Manager）负责，上传完成后即可 close
 * - [getSprite]/[sprites] 查询；未命中的 sprite 返回 [missingSprite]（全 0 UV 占位）
 */
class SokitsuAtlasTexture(
    val location: Identifier
) : AbstractTexture() {

    private var spritesList: List<SokitsuSprite> = emptyList()
    private var spritesByName: Map<Pair<Identifier, String>, SokitsuSprite> = emptyMap()

    val missingSprite: SokitsuSprite by lazy {
        SokitsuSprite(location, location, "<missing>", 0, 0, 0, 0, 1, 1, 0)
    }

    fun upload(preparations: SokitsuAtlasPreparations) {
        releaseTextures()
        val device = RenderSystem.getDevice()
        this.texture = device.createTexture(
            "sokitsu-atlas:${location}",
            5,
            GpuFormat.RGBA8_UNORM,
            preparations.width,
            preparations.height,
            1,
            1
        )
        this.textureView = device.createTextureView(this.texture!!)
        this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)

        val encoder = device.createCommandEncoder()
        for ((_, _, x, y, image) in preparations.regions) {
            encoder.writeToTexture(this.texture!!, image, 0, 0, x, y)
        }

        val atlasWidth = preparations.width
        val atlasHeight = preparations.height
        val padding = preparations.padding
        this.spritesList = preparations.regions.map { region ->
            SokitsuSprite(
                atlasLocation = location,
                textureId = region.textureId,
                layerId = region.layerId,
                x = region.x,
                y = region.y,
                width = region.image.width,
                height = region.image.height,
                atlasWidth = atlasWidth,
                atlasHeight = atlasHeight,
                padding = padding
            )
        }
        this.spritesByName = spritesList.associateBy { it.textureId to it.layerId }
    }

    /**
     * 未初始化/未命中时返回 missing sprite（查询不抛异常，与原版 missingSprite 兜底形态一致）。
     */
    fun getSprite(textureId: Identifier, layerId: String): SokitsuSprite =
        spritesByName[textureId to layerId] ?: missingSprite

    /**
     * 某 SokitsuTexture 的全部图层 sprite，保持定义声明顺序（regions 顺序即声明顺序）。
     */
    fun sprites(textureId: Identifier): List<SokitsuSprite> =
        spritesList.filter { it.textureId == textureId }

    override fun close() {
        spritesList = emptyList()
        spritesByName = emptyMap()
        super.close()
    }
}