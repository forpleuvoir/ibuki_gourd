package moe.forpleuvoir.ibukigourd.ui.sokitsu.texture

import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorLevel
import moe.forpleuvoir.ibukigourd.util.codec.intSize
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.util.requireType
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.base.builder.build
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.color
import moe.forpleuvoir.nebula.serialization.codec.enum
import moe.forpleuvoir.nebula.serialization.codec.list
import moe.forpleuvoir.nebula.serialization.codec.nullable
import moe.forpleuvoir.nebula.serialization.extensions.requireInt

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
    val colorLevel: ColorLevel?,
    val tintMode: TextureTintMode,
    val tintAlpha: Boolean = false,
    val fill: TextureFill,
    val region: TextureRegion? = null
) {

    companion object : Codec<TextureLayer> by Codec.create<TextureLayer>()
        .field(TextureLayer::id).codec(Codec.string)
        .field(TextureLayer::keys).default(emptyList()).codec(Codec.list(Codec.color))
        .field(TextureLayer::colorLevel).codec(Codec.enum<ColorLevel>().nullable())
        .field(TextureLayer::tintMode).default(TextureTintMode.Tint).skipDefault().codec(TintModeCodec)
        .field(TextureLayer::tintAlpha).default(false).skipDefault().codec(Codec.boolean)
        .field(TextureLayer::fill).codec(TextureFill)
        .field(TextureLayer::region).codec(TextureRegion.nullable())
        .build({ i, k, c, t, ta, f, r -> TextureLayer(i, k, c, t, ta, f, r) })
}

/**
 * tintMode 序列化：兼容历史命名（Luminance / Flat / Hsv / Hsl），读取时映射为现名
 * 以免旧数据反序列化失败：Luminance/Hsv→[TextureTintMode.Tint]，Flat→[TextureTintMode.Mask]，Hsl→[TextureTintMode.HueShift]。
 */
private object TintModeCodec : Codec<TextureTintMode> {

    override fun serialization(target: TextureTintMode): SerializeElement =
        SerializePrimitive(target.name)

    override fun deserialization(data: SerializeElement): Result<TextureTintMode> =
        DeserializationException.runCatching {
            val name = data.asString ?: throw IllegalArgumentException("tintMode 应为字符串, 实际: $data")
            when (name) {
                "Luminance", "Hsv" -> TextureTintMode.Tint
                "Flat" -> TextureTintMode.Mask
                "Hsl" -> TextureTintMode.HueShift
                else -> TextureTintMode.valueOf(name)
            }
        }
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

/**
 * 主题色 T（colorLevel 取出的 ColorTone 档）与纹理像素色 C 的 RGB 合成策略：
 * - [Mask]：完全替换为 T（C 只当形状/遮罩，由 alpha 决定）
 * - [Tint]：输出 HSV(T.H, T.S, C.V)——主题提供色相与饱和，纹理只贡献明度结构（默认，灰阶纹理）
 * - [HueShift]：输出 HSL(T.H, C.S, C.L)——只把色相转到主题，纹理自身的饱和与亮度保留（彩色纹理）
 *
 * alpha 是否也由主题接管由 [TextureLayer.tintAlpha] 单独控制，与模式正交。
 */
enum class TextureTintMode {
    Mask, Tint, HueShift;
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