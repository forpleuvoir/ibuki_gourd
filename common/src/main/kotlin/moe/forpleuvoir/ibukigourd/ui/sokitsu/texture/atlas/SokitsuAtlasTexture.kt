package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureTintMode
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorLevel
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier

/**
 * 一次 atlas 上传的全部准备数据（由 SokitsuAtlasManager 构建，多线程阶段产出）。
 *
 * [regions] 的顺序即图层声明顺序的扁平展开（Manager 保证），上传时按 textureId 分组
 * 重建为每纹理一个 [SokitsuSprite] 容器。
 */
data class SokitsuAtlasPreparations(
    val width: Int,
    val height: Int,
    val padding: Int,
    val regions: List<SokitsuAtlasRegion>,
    /** 素材像素密度（@1x/@2x）；不参与 atlas 生成，仅随 sprite 下发渲染层。 */
    val density: Int = 1,
)

/**
 * atlas 内一个图层区域：image 为待上传的图层图，x/y 为缝合后的内容区坐标（含 padding 偏移）。
 * [colorLevel]/[tintMode]/[tintAlpha]/[fill] 携带该图层（TextureLayer）的渲染所需信息，随 sprite 构建传递。
 */
data class SokitsuAtlasRegion(
    val textureId: Identifier,
    val layerId: String,
    val x: Int,
    val y: Int,
    val image: NativeImage,
    val colorLevel: ColorLevel? = null,
    val tintMode: TextureTintMode = TextureTintMode.Tint,
    val fill: TextureFill = TextureFill.Stretch,
    val tintAlpha: Boolean = false,
)

/**
 * Sokitsu atlas 纹理（自建实现，参考原版 TextureAtlas 的形态：GpuTexture 上传 + 按名查 sprite + missing 兜底）。
 *
 * - [upload] 在渲染线程调用：创建 GpuTexture（RGBA8_UNORM，clamp-to-edge + NEAREST 采样），逐区域 blit 上传
 * - NativeImage 的生命周期由调用方（Manager）负责，上传完成后即可 close
 * - [getSprite] 按 textureId 返回整个纹理容器（[SokitsuSprite]）；[getLayer] 按 textureId+layerId 返回单个图层
 * - 未命中的查询返回 missing 兜底（不会抛异常）
 */
class SokitsuAtlasTexture(
    val location: Identifier
) : AbstractTexture() {

    private var spritesByName: Map<Identifier, SokitsuSprite> = emptyMap()
    private var layersByName: Map<Pair<Identifier, String>, SokitsuLayerSprite> = emptyMap()

    val missingSprite: SokitsuSprite by lazy {
        SokitsuSprite(location, location, emptyList())
    }

    val missingLayer: SokitsuLayerSprite by lazy {
        SokitsuLayerSprite(location, location, "<missing>", 0, 0, 0, 0, 1, 1, 1)
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
        val density = preparations.density
        val location = this.location

        val layers = preparations.regions.map { region ->
            SokitsuLayerSprite(
                atlasLocation = location,
                textureId = region.textureId,
                layerId = region.layerId,
                x = region.x,
                y = region.y,
                width = region.image.width,
                height = region.image.height,
                atlasWidth = atlasWidth,
                atlasHeight = atlasHeight,
                density = density,
                colorLevel = region.colorLevel,
                tintMode = region.tintMode,
                fill = region.fill,
                tintAlpha = region.tintAlpha,
            )
        }

        // 按 textureId 分组重建为每纹理一个容器，保持图层声明顺序
        this.spritesByName = layers.groupBy({ it.textureId }, { it }).mapValues { (textureId, layerList) ->
            SokitsuSprite(location, textureId, layerList)
        }
        this.layersByName = layers.associateBy { it.textureId to it.layerId }
    }

    /**
     * 按 textureId 返回整个纹理精灵（容器，含全部图层）；未命中返回空容器 missingSprite。
     */
    fun getSprite(textureId: Identifier): SokitsuSprite = spritesByName[textureId] ?: missingSprite

    /**
     * 按 textureId + layerId 返回单个图层精灵；未命中返回 missingLayer。
     */
    fun getLayer(textureId: Identifier, layerId: String): SokitsuLayerSprite =
        layersByName[textureId to layerId] ?: missingLayer

    /**
     * 某 SokitsuTexture 的全部图层精灵，保持定义声明顺序（= 容器.layers）。
     */
    fun layers(textureId: Identifier): List<SokitsuLayerSprite> = getSprite(textureId).layers

    override fun close() {
        spritesByName = emptyMap()
        layersByName = emptyMap()
        super.close()
    }
}