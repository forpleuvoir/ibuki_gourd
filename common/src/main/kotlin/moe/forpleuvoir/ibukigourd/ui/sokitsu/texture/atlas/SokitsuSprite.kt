package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import moe.forpleuvoir.ibukigourd.render.extension.texture.Corner
import moe.forpleuvoir.ibukigourd.render.extension.texture.TextureUVMapping
import moe.forpleuvoir.ibukigourd.render.extension.texture.UVMapping
import net.minecraft.resources.Identifier

/**
 * Sokitsu atlas 中的单个精灵（自建实现，参考原版 TextureAtlasSprite 的形态：内容区坐标 + 归一化 UV）。
 *
 * - [x]/[y] 图层内容区在 atlas 内的左上角物理坐标（不含 padding）
 * - [width]/[height] 图层内容物理尺寸（= 基础图尺寸，即 SokitsuTexture.size）
 * - [padding] 缝合时预留的像素间距；归一化 UV 会向内收缩 padding 偏移（防止采样溢色，与原版公式一致）
 * - 归一化 UV（[u0]/[u1]/[v0]/[v1]）在构造时一次性算好
 */
class SokitsuSprite(
    val atlasLocation: Identifier,
    val textureId: Identifier,
    val layerId: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    atlasWidth: Int,
    atlasHeight: Int,
    val padding: Int = 0,
) {
    val u0: Float = (x + padding).toFloat() / atlasWidth
    val u1: Float = (x + padding + width).toFloat() / atlasWidth
    val v0: Float = (y + padding).toFloat() / atlasHeight
    val v1: Float = (y + padding + height).toFloat() / atlasHeight

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
        "SokitsuSprite{atlas=$atlasLocation, texture=$textureId, layer='$layerId', x=$x, y=$y, size=${width}x$height, uv=[$u0,$v0,$u1,$v1]}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SokitsuSprite) return false
        return atlasLocation == other.atlasLocation &&
            textureId == other.textureId &&
            layerId == other.layerId &&
            x == other.x && y == other.y &&
            width == other.width && height == other.height &&
            padding == other.padding
    }

    override fun hashCode(): Int {
        var result = atlasLocation.hashCode()
        result = 31 * result + textureId.hashCode()
        result = 31 * result + layerId.hashCode()
        result = 31 * result + x
        result = 31 * result + y
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + padding
        return result
    }
}