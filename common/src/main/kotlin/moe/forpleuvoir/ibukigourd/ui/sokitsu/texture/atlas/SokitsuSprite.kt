package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import moe.forpleuvoir.ibukigourd.render.extension.texture.Corner
import moe.forpleuvoir.ibukigourd.render.extension.texture.TextureUVMapping
import moe.forpleuvoir.ibukigourd.render.extension.texture.UVMapping
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureTintMode
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorLevel
import net.minecraft.resources.Identifier

/**
 * Sokitsu 纹理精灵（对应一个 [moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.SokitsuTexture] 的容器）。
 *
 * 一个 SokitsuTexture 由多个图层（layer）组成，[SokitsuSprite] 持有该纹理的全部图层精灵
 * [layers]，顺序与 TextureLayer 声明顺序一致（渲染叠加顺序）。
 */
class SokitsuSprite(
    val atlasLocation: Identifier,
    val textureId: Identifier,
    val layers: List<SokitsuLayerSprite>,
) {
    /**
     * 按 layerId 查找单个图层精灵；不存在返回 null。
     */
    fun getLayer(layerId: String): SokitsuLayerSprite? = layers.firstOrNull { it.layerId == layerId }

    /**
     * 是否为空容器（没有图层，通常表示缺失/未加载）。
     */
    val isEmpty: Boolean get() = layers.isEmpty()

    /**
     * 素材像素密度（取自首个图层；空容器返回 1）。
     *
     * 源图 [density]×[density] 像素表达 1 个逻辑像素，见 [SokitsuLayerSprite.density]。
     */
    val density: Int get() = layers.firstOrNull()?.density ?: 1

    /**
     * 逻辑宽度（逻辑像素）= 最宽图层的逻辑宽度；空容器返回 0。
     *
     * 位图元素应按逻辑尺寸布局，再由主题渲染倍率（`SokitsuTheme.pixelScale`）换算到屏幕像素。
     */
    val logicalWidth: Float get() = layers.maxOfOrNull { it.logicalWidth } ?: 0f

    /** 见 [logicalWidth]。 */
    val logicalHeight: Float get() = layers.maxOfOrNull { it.logicalHeight } ?: 0f

    override fun toString(): String =
        "SokitsuSprite{atlas=$atlasLocation, texture=$textureId, density=$density, layers=$layers}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SokitsuSprite) return false
        return atlasLocation == other.atlasLocation &&
            textureId == other.textureId &&
            layers == other.layers
    }

    override fun hashCode(): Int {
        var result = atlasLocation.hashCode()
        result = 31 * result + textureId.hashCode()
        result = 31 * result + layers.hashCode()
        return result
    }
}

/**
 * Sokitsu atlas 中的单个图层精灵（自建实现，参考原版 TextureAtlasSprite 的形态：内容区坐标 + 归一化 UV）。
 *
 * - [x]/[y] 图层内容区在 atlas 内的左上角物理坐标（**已含 padding 偏移**，
 *   与 [SokitsuStitcher.PlacedSprite] 的合约一致）
 * - [width]/[height] 图层内容物理尺寸（= 从源图按 layer.region 裁剪后的尺寸）
 * - [colorLevel] 该图层的主题层级（来自 TextureLayer.colorLevel）
 * - [tintMode] 该图层的着色模式（来自 TextureLayer.tintMode）
 * - [tintAlpha] alpha 是否也由主题接管（来自 TextureLayer.tintAlpha，与 [tintMode] 正交）
 * - [fill] 该图层的填充方式（来自 TextureLayer.fill；NinePatch 内含 border 与 disableSlice）
 * - [disabledSlices] 由 [fill]（NinePatch.disableSlice）派生的九宫格禁用区域索引集合（0..8，从左到右、从上到下）
 * - [density] **素材像素密度**（@1x / @2x，来自 atlas 定义）：源图 [density]×[density] 像素
 *   表达 1 个逻辑像素。图集内始终按源图 1:1 存储，该值只用于换算 [logicalWidth]/[logicalHeight]。
 * - [logicalWidth]/[logicalHeight] 逻辑尺寸 = 物理尺寸 / [density]（与设备、主题无关，
 *   是素材"原本该占多大"的抽象尺寸）
 * - 归一化 UV（[u0]/[u1]/[v0]/[v1]）= 内容区矩形直接归一化，**无额外内缩**
 *   （采样防溢色由 sprite 之间的 padding 隔离带保证；负 border 九宫格向内容区外
 *   线性外推时正好落在隔离带内）
 *
 * **渲染倍率不在本类**：源图 1px 最终占多少屏幕像素 = `SokitsuTheme.pixelScale / density`
 * （主题渲染密度 ÷ 素材密度），由绘制层在渲染时换算——这样才能运行时切换渲染密度而不重建图集。
 *
 * 注：源图的 region 区域信息已在 LayerExtractor 提取时消费（决定裁剪范围），精灵上不再保留；
 * 渲染所需的信息（atlas 落点、UV、fill、colorLevel、tintMode、tintAlpha、density）已全部内化。
 */
class SokitsuLayerSprite(
    val atlasLocation: Identifier,
    val textureId: Identifier,
    val layerId: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    atlasWidth: Int,
    atlasHeight: Int,
    val density: Int = 1,
    val colorLevel: ColorLevel? = null,
    val tintMode: TextureTintMode = TextureTintMode.Tint,
    val fill: TextureFill = TextureFill.Stretch,
    val tintAlpha: Boolean = false,
) {
    val u0: Float = x.toFloat() / atlasWidth
    val u1: Float = (x + width).toFloat() / atlasWidth
    val v0: Float = y.toFloat() / atlasHeight
    val v1: Float = (y + height).toFloat() / atlasHeight

    /**
     * 九宫格渲染中被禁用的区域索引集合（来自 [fill] 的 NinePatch.disableSlice，缺省空 = 全开）。
     */
    val disabledSlices: Set<Int> get() = (fill as? TextureFill.NinePatch)?.disableSlice?.toSet() ?: emptySet()

    /**
     * 在 [0, 1] 区间内插值取 U 坐标（供 blit 时按内部偏移取 UV）。
     */
    fun getU(offset: Float): Float = u0 + (u1 - u0) * offset

    /**
     * 在 [0, 1] 区间内插值取 V 坐标。
     */
    fun getV(offset: Float): Float = v0 + (v1 - v0) * offset

    /**
     * 转为整数像素 UV 映射（内容区，不含 padding）。
     */
    fun uvMapping(): UVMapping = UVMapping(x, y, x + width, y + height)

    /**
     * 转为带九宫格边框的 UV 映射（corner 缺省为不指定）。
     */
    fun textureUVMapping(corner: Corner = Corner.Unspecified): TextureUVMapping = TextureUVMapping(corner, uvMapping())

    /**
     * 逻辑宽度（逻辑像素）= 源图物理宽度 / [density]。
     *
     * 位图元素应按逻辑尺寸布局；直接用 [width] 等于把 @2x 素材当成两倍大来排版。
     */
    val logicalWidth: Float get() = width / density.toFloat()

    /** 见 [logicalWidth]。 */
    val logicalHeight: Float get() = height / density.toFloat()

    /**
     * 是否为阴影图层（[layerId] 等于 [SHADOW_LAYER_ID]，忽略大小写）。
     *
     * 阴影图层由渲染端特殊处理：向 [moe.forpleuvoir.compose_minecraft.platform.ui.LocalShadowLight]
     * 光源反方向偏移后先于普通图层绘制。
     */
    val isShadow: Boolean
        get() = layerId.equals(SHADOW_LAYER_ID, ignoreCase = true)

    override fun toString(): String =
        "SokitsuLayerSprite{atlas=$atlasLocation, texture=$textureId, layer='$layerId', x=$x, y=$y, size=${width}x$height, density=$density, uv=[$u0,$v0,$u1,$v1], colorLevel=$colorLevel, tintMode=$tintMode, tintAlpha=$tintAlpha, fill=$fill}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SokitsuLayerSprite) return false
        return atlasLocation == other.atlasLocation &&
            textureId == other.textureId &&
            layerId == other.layerId &&
            x == other.x && y == other.y &&
            width == other.width && height == other.height &&
            density == other.density &&
            colorLevel == other.colorLevel &&
            tintMode == other.tintMode &&
            tintAlpha == other.tintAlpha &&
            fill == other.fill
    }

    override fun hashCode(): Int {
        var result = atlasLocation.hashCode()
        result = 31 * result + textureId.hashCode()
        result = 31 * result + layerId.hashCode()
        result = 31 * result + x
        result = 31 * result + y
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + density
        result = 31 * result + (colorLevel?.hashCode() ?: 0)
        result = 31 * result + tintMode.hashCode()
        result = 31 * result + tintAlpha.hashCode()
        result = 31 * result + fill.hashCode()
        return result
    }

    companion object {

        /** 阴影图层的 layerId 约定（[SokitsuLayerSprite.isShadow]）。 */
        const val SHADOW_LAYER_ID = "shadow"
    }
}