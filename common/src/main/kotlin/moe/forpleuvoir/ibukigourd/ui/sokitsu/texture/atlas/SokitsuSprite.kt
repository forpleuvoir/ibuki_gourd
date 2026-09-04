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

    override fun toString(): String =
        "SokitsuSprite{atlas=$atlasLocation, texture=$textureId, layers=$layers}"

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
 * - [x]/[y] 图层内容区在 atlas 内的左上角物理坐标（不含 padding）
 * - [width]/[height] 图层内容物理尺寸（= 从源图按 layer.region 裁剪后的尺寸）
 * - [colorLevel] 该图层的主题层级（来自 TextureLayer.colorLevel）
 * - [tintMode] 该图层的着色模式（来自 TextureLayer.tintMode）
 * - [fill] 该图层的填充方式（来自 TextureLayer.fill；NinePatch 内含 border 与 disableSlice）
 * - [disabledSlices] 由 [fill]（NinePatch.disableSlice）派生的九宫格禁用区域索引集合（0..8，从左到右、从上到下）
 * - 归一化 UV（[u0]/[u1]/[v0]/[v1]）在构造时一次性算好，并已向内收缩 padding 偏移（防采样溢色）
 *
 * 注：源图的 region 区域信息已在 LayerExtractor 提取时消费（决定裁剪范围），精灵上不再保留；
 * 渲染所需的信息（atlas 落点、UV、fill、colorLevel、tintMode）已全部内化。
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
    padding: Int = 0,
    val colorLevel: ColorLevel? = null,
    val tintMode: TextureTintMode = TextureTintMode.Tint,
    val fill: TextureFill = TextureFill.Stretch,
) {
    val u0: Float = (x + padding).toFloat() / atlasWidth
    val u1: Float = (x + padding + width).toFloat() / atlasWidth
    val v0: Float = (y + padding).toFloat() / atlasHeight
    val v1: Float = (y + padding + height).toFloat() / atlasHeight

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

    override fun toString(): String =
        "SokitsuLayerSprite{atlas=$atlasLocation, texture=$textureId, layer='$layerId', x=$x, y=$y, size=${width}x$height, uv=[$u0,$v0,$u1,$v1], colorLevel=$colorLevel, tintMode=$tintMode, fill=$fill}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SokitsuLayerSprite) return false
        return atlasLocation == other.atlasLocation &&
            textureId == other.textureId &&
            layerId == other.layerId &&
            x == other.x && y == other.y &&
            width == other.width && height == other.height &&
            colorLevel == other.colorLevel &&
            tintMode == other.tintMode &&
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
        result = 31 * result + (colorLevel?.hashCode() ?: 0)
        result = 31 * result + tintMode.hashCode()
        result = 31 * result + fill.hashCode()
        return result
    }
}