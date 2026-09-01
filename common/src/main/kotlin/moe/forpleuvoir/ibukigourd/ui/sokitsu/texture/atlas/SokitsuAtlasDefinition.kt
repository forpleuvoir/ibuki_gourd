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
 * - [density] atlas 级渲染缩放倍率，仅描述给渲染层使用，不参与 atlas 生成（density=2 → 逻辑 1×1px 按 2×2px 算）
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
        .field(SokitsuAtlasDefinition::textures).default(emptyList<Identifier>()).codec(Codec.list(Codec.identifier))
        .build({ maxSize, padding, mipLevel, density, textures ->
            SokitsuAtlasDefinition(maxSize, padding, mipLevel, density, textures)
        })
}