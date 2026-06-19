package moe.forpleuvoir.ibukigourd.render.extension.texture

import moe.forpleuvoir.ibukigourd.render.asTexture
import moe.forpleuvoir.nebula.common.util.checkType
import moe.forpleuvoir.nebula.common.util.requireKey
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.client.gui.render.TextureSetup

class IGTexture(
    corner: Corner,
    u1: Int, v1: Int, u2: Int, v2: Int,
    val textureInfo: TextureInfo
) : TextureUVMapping(corner, u1, v1, u2, v2) {

    constructor(textureUVMapping: TextureUVMapping, textureInfo: TextureInfo) : this(
        textureUVMapping.corner,
        textureUVMapping.uStart,
        textureUVMapping.vStart,
        textureUVMapping.uEnd,
        textureUVMapping.vEnd,
        textureInfo
    )

    companion object : Codec<IGTexture> {

        override fun deserialization(data: SerializeElement): Result<IGTexture> = DeserializationException.runCatching {
            data.checkType<SerializeObject, IGTexture> {
                IGTexture(
                    textureUVMapping = TextureUVMapping.deserialization(it).getOrThrow(),
                    textureInfo = TextureInfo.deserialization(it.requireKey("texture_info")).getOrThrow()
                )
            }
        }

        override fun serialization(target: IGTexture): SerializeElement =
            TextureUVMapping.serialization(target).asObject!!.apply { this["texture_info"] = TextureInfo.serialization(target.textureInfo) }

    }

    val textureSetup: TextureSetup by lazy {
        textureInfo.textureId.asTexture.let {
            TextureSetup.singleTexture(it.textureView, it.sampler)
        }
    }

    val u0 = uStart.toFloat() / textureInfo.width

    val v0 = vStart.toFloat() / textureInfo.height

    val u1 = uEnd.toFloat() / textureInfo.width

    val v1 = vEnd.toFloat() / textureInfo.height


    override fun toString(): String {
        return "IGTexture(corner=$corner,uStart=$uStart, vStart=$vStart, uEnd=$uEnd, vEnd=$vEnd,textureInfo=$textureInfo)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as IGTexture

        return textureInfo == other.textureInfo && super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + textureInfo.hashCode()
        return result
    }

}