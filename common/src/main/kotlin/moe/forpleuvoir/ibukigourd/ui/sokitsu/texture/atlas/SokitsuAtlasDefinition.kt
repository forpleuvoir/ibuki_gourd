package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas

import moe.forpleuvoir.ibukigourd.util.codec.identifier
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.list
import net.minecraft.resources.Identifier

/**
 * Sokitsu atlas 定义（对应资源文件 assets/<ns>/sokitsu_atlas/<atlasId>.json）。
 *
 * 每个 atlas 定义对应一张 atlas 大图：
 * - [maxSize] atlas 最大边长限制，0 表示取平台纹理上限
 * - [padding] sprite 之间的像素间距（防止采样溢色）
 * - [mipLevel] 图集 mipmap 层级（UI 素材一般用 0）
 * - [density] **本 atlas 素材的像素密度**（@1x / @2x / @3x 语义）：density=2 表示素材按
 *   2×2 源图像素表达 1 个逻辑像素（高清素材）。**不参与 atlas 生成**（图集内按源图 1:1 存储），
 *   只作为素材侧的换算基准随 sprite 下发渲染层。
 *
 * 注意与渲染倍率区分：渲染倍率是用户在 `SokitsuTheme.pixelScale` 里的设置（1 逻辑像素渲染成多大）。
 * 源图 → 屏幕的最终倍率 = `SokitsuTheme.pixelScale / atlas.density`：@2x 素材在 2x 渲染倍率下
 * 正好 1:1，@1x 素材在 2x 渲染密度下放大 2 倍。
 * - [textures] 可选：额外显式声明本 atlas 合批的 SokitsuTexture id 列表
 *
 * 纹理不在此逐个声明：atlas 会自动扫描其自身对应目录 assets/<ns>/texture/sokitsu/<atlasId.path>/ 下的所有
 * <textureId>.json 定义（如 sokitsu_atlas/ui.json → 扫描 texture/sokitsu/ui/）。
 */
data class SokitsuAtlasDefinition(
    val maxSize: Int = 0,
    val padding: Int = 1,
    val mipLevel: Int = 0,
    val density: Int = 1,
    val textures: List<Identifier> = emptyList()
) {
    companion object : Codec<SokitsuAtlasDefinition> by Codec.create<SokitsuAtlasDefinition>()
        .field(SokitsuAtlasDefinition::maxSize, "max_size").default(0).codec(Codec.int(0..Int.MAX_VALUE))
        .field(SokitsuAtlasDefinition::padding, "padding").default(1).codec(Codec.int(0..Int.MAX_VALUE))
        .field(SokitsuAtlasDefinition::mipLevel, "mip_level").default(0).codec(Codec.int(0..Int.MAX_VALUE))
        .field(SokitsuAtlasDefinition::density, "density").default(1).codec(Codec.int(1..Int.MAX_VALUE))
        .field(SokitsuAtlasDefinition::textures).default(emptyList()).codec(Codec.list(Codec.identifier))
        .build({ maxSize, padding, mipLevel, density, textures ->
            SokitsuAtlasDefinition(maxSize, padding, mipLevel, density, textures)
        })
}