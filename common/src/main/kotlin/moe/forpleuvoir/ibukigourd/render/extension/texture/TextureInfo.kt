package moe.forpleuvoir.ibukigourd.render.extension.texture

import moe.forpleuvoir.ibukigourd.util.codec.identifier
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

data class TextureInfo(
    val width: Int = 256,
    val height: Int = 256,
    val textureId: Identifier
) {
    companion object : Codec<TextureInfo> by Codec.create<TextureInfo>()
        .field<Int>("width").getter(TextureInfo::width).codec(Codec.int(0..65535))
        .field<Int>("height").getter(TextureInfo::height).codec(Codec.int(0..65535))
        .field<Identifier>("texture_id").getter(TextureInfo::textureId).codec(Codec.identifier)
        .build(::TextureInfo)
}