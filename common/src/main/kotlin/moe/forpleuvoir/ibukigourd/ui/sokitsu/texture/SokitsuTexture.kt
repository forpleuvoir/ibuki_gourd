package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture

import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.ibukigourd.util.codec.intSize
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.base.builder.build
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.color
import moe.forpleuvoir.nebula.serialization.codec.list
import moe.forpleuvoir.nebula.serialization.codec.nullable
import moe.forpleuvoir.nebula.serialization.extensions.requireInt
import moe.forpleuvoir.nebula.common.util.requireType

/** 素材层槽位名：组件主色（Multiply 渲染：槽位色 × 素材灰度）。 */
const val SLOT_TONE = "tone"

/** 素材层槽位名：直出（纹理原样，不参与主题染色）。 */
const val SLOT_NONE = "none"

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
    /**
     * 该层绑定的主题颜色槽位名（如 `tone` / `outline` / `shadow` / 具体槽位名）：
     * - `tone`：组件主色，Multiply 渲染（槽位色 × 素材灰度，正片叠底）
     * - 其它槽位名：Mask 渲染（纯该槽位色替换）
     * - `none`：直出（纹理原样）
     */
    val colorSlot: String,
    val fill: TextureFill,
    val region: TextureRegion? = null
) {

    companion object : Codec<TextureLayer> by Codec.create<TextureLayer>()
        .field(TextureLayer::id).codec(Codec.string)
        .field(TextureLayer::keys).default(emptyList()).codec(Codec.list(Codec.color))
        .field(TextureLayer::colorSlot).default(SLOT_TONE).codec(Codec.string)
        .field(TextureLayer::fill).codec(TextureFill)
        .field(TextureLayer::region).codec(TextureRegion.nullable())
        .build({ i, k, c, f, r -> TextureLayer(i, k, c, f, r) })
}

/**
 * 图层在源图中的渲染区域（整数像素 UV 区间：起点 [u]/[v] 到终点 [u1]/[v1]，相对源图左上角）。
 * [width]/[height] 由区间计算得出（width = u1 - u，height = v1 - v）。
 * 缺省为 null 时表示整张源图都属于该图层。
 */
data class TextureRegion(
    val u: Int,
    val v: Int,
    val u1: Int,
    val v1: Int
) {
    val width: Int get() = u1 - u
    val height: Int get() = v1 - v

    companion object : Codec<TextureRegion> {

        override fun serialization(target: TextureRegion): SerializeElement = SerializeObject.build {
            "u" to target.u
            "v" to target.v
            "u1" to target.u1
            "v1" to target.v1
        }

        override fun deserialization(data: SerializeElement): Result<TextureRegion> = DeserializationException.runCatching {
            val obj = data.requireType<SerializeObject>()
            val u = obj.requireInt("u")
            val v = obj.requireInt("v")
            if (obj.containsKey("width") && obj.containsKey("height")) {
                // 兼容 {u, v, width, height}：width/height 换算为 u1/v1
                val width = obj.requireInt("width")
                val height = obj.requireInt("height")
                TextureRegion(u, v, u + width, v + height)
            } else {
                // 标准 {u, v, u1, v1}
                TextureRegion(u, v, obj.requireInt("u1"), obj.requireInt("v1"))
            }
        }

    }
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
        val disableSlice: List<Int> = emptyList(),
    ) : TextureFill("ninepatch") {

        companion object : Codec<NinePatch> by Codec.create<NinePatch>()
            .field(NinePatch::mode).codec(Codec.string)
            .field(NinePatch::border).codec(Border)
            .field(NinePatch::disableSlice).default(emptyList()).codec(Codec.list(Codec.int(0..8)))
            .build({ _, border, disableSlice -> NinePatch(border, disableSlice) })

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