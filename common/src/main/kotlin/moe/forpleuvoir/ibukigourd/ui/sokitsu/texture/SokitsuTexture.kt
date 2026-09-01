package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture

import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorRef
import moe.forpleuvoir.ibukigourd.util.codec.intSize
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.color
import moe.forpleuvoir.nebula.serialization.codec.enum
import moe.forpleuvoir.nebula.serialization.codec.list
import moe.forpleuvoir.nebula.serialization.codec.nullable

data class SokitsuTexture(
    val size: IntSize,
    val layers: List<TextureLayer>
) {
    companion object : Codec<SokitsuTexture> by Codec.create<SokitsuTexture>()
        .field(SokitsuTexture::size).codec(Codec.intSize(0..Int.MAX_VALUE, 0..Int.MAX_VALUE))
        .field(SokitsuTexture::layers).codec(Codec.list(TextureLayer))
        .build({ r, l -> SokitsuTexture(r, l) })

}

data class TextureLayer(
    val id: String,
    val keys: List<Color>,
    val colorRef: ColorRef?,
    val tintMode: TextureTintMode,
    val fill: TextureFill
) {

    companion object : Codec<TextureLayer> by Codec.create<TextureLayer>()
        .field(TextureLayer::id).codec(Codec.string)
        .field(TextureLayer::keys).default(emptyList()).codec(Codec.list(Codec.color))
        .field(TextureLayer::colorRef).codec(ColorRef.nullable())
        .field(TextureLayer::tintMode).default(TextureTintMode.Luminance).skipDefault().codec(Codec.enum())
        .field(TextureLayer::fill).codec(TextureFill)
        .build({ i, k, c, t, f -> TextureLayer(i, k, c, t, f) })
}

enum class TextureTintMode {
    Luminance, Flat, Hsl;
}

sealed class TextureFill(internal val mode: String) {

    companion object : Codec<TextureFill> {
        override fun serialization(target: TextureFill): SerializeElement =
            when (target) {
                is NinePatch -> NinePatch.serialization(target)
                is Tile      -> Tile.serialization(target)
                is Stretch   -> Stretch.serialization(target)
            }


        override fun deserialization(data: SerializeElement): Result<TextureFill> =
            NinePatch.deserialization(data)
                .recoverCatching { Tile.deserialization(data).getOrThrow() }
                .recoverCatching { Stretch.deserialization(data).getOrThrow() }

    }

    data class NinePatch(
        val border: Border,
    ) : TextureFill("ninepatch") {

        companion object : Codec<NinePatch> by Codec.create<NinePatch>()
            .field(NinePatch::mode).codec(Codec.string)
            .field(NinePatch::border).codec(Border)
            .build({ _, border -> NinePatch(border) })

        data class Border(
            val left: Int,
            val top: Int,
            val right: Int,
            val bottom: Int
        ) {
            companion object : Codec<Border> by Codec.create<Border>()
                .field(Border::left).codec(Codec.int)
                .field(Border::top).codec(Codec.int)
                .field(Border::right).codec(Codec.int)
                .field(Border::bottom).codec(Codec.int)
                .build({ l, t, r, b -> Border(l, t, r, b) })
        }

    }

    data class Tile(val scale: Float) : TextureFill("tile") {
        companion object : Codec<Tile> by Codec.create<Tile>()
            .field(Tile::mode).codec(Codec.string)
            .field(Tile::scale).codec(Codec.float(1f..Float.MAX_VALUE))
            .build({ _, scale -> Tile(scale) })
    }

    data object Stretch : TextureFill("stretch"), Codec<Stretch> {
        override fun serialization(target: Stretch): SerializeElement =
            SerializePrimitive(target.mode)

        override fun deserialization(data: SerializeElement): Result<Stretch> = DeserializationException.runCatching {
            require(data.asString == mode) { "Invalid Stretch value: expected '$mode', actual '$data'" }
            Stretch
        }

    }
}