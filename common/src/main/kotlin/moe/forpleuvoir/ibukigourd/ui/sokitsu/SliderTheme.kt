package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

/**
 * 滑条的主题接入声明：token 映射（"什么颜色"）+ 尺寸/纹理 meta（"多大、用哪张图"）合一。
 *
 * 与 [SwitchTokens] / [ButtonTokens] 同构，本文件只声明该组件映射到主题的哪些语义槽位。
 */
object SliderTokens {

    /** 凹槽轨道底：中性容器色（[ColorSchemeToken.SurfaceVariant]），与页面主体弱区分。 */
    val Track = ColorSchemeToken.SurfaceVariant

    /** 进度填充（抬起的实心段）：主色（[ColorSchemeToken.Primary]），与轨道明显拉开明暗。 */
    val Fill = ColorSchemeToken.Primary

    /** 禁用态轨道基准色（会被 [DisabledTrackOpacity] 压透明）。 */
    val DisabledTrack = ColorSchemeToken.SurfaceVariant

    /** 禁用态填充基准色（会被 [DisabledFillOpacity] 压透明）。 */
    val DisabledFill = ColorSchemeToken.OnSurface

    /** 禁用态轨道不透明度，沿用 Material3 `DisabledContainerOpacity`。 */
    const val DisabledTrackOpacity = 0.12f

    /** 禁用态填充不透明度，沿用 Material3 `DisabledLabelTextOpacity`。 */
    const val DisabledFillOpacity = 0.38f

    /** 禁用态数值文字不透明度，同上。 */
    const val DisabledLabelOpacity = 0.38f
}

/**
 * 滑条的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   slider: {
 *     track_width: 160, track_height: 48,
 *     track_sprite: "ui/slider/track",
 *     fill_sprite: "ui/slider/fill"
 *   }
 * }
 * ```
 *
 * [trackMinSize] 的高度须与纹理比例匹配：两张素材均为 16×16 源像素，高度取
 * `16 × pixelScale`（默认 pixelScale = 3 → 48）才能一个源像素对应一个 `pixelScale` 方块。
 *
 * 轨道与填充**必须同画布尺寸、同九宫格 border**（当前均为 16×16、border 3）：
 * 填充层按进度裁剪右端，`progress = 100%` 时其右端才能与轨道的右端逐像素重合。
 */
data class SliderMeta(
    /** 轨道最小尺寸，与 slider/track 纹理比例匹配。 */
    val trackMinSize: DpSize,
    /** 轨道（凹槽）纹理。 */
    val trackSprite: Identifier,
    /** 进度填充（凸起段）纹理。 */
    val fillSprite: Identifier,
) {

    companion object : Codec<SliderMeta> {

        val default = SliderMeta(
            trackMinSize = DpSize(160.dp, 48.dp),
            trackSprite = identifier("ui/slider/track"),
            fillSprite = identifier("ui/slider/fill"),
        )

        private val codec = Codec.create<SliderMeta>()
            .field(SliderMeta::trackMinSize).default(default.trackMinSize)
            .codec(Codec.dpSize(8.dp..1024.dp, 8.dp..512.dp))
            .field(SliderMeta::trackSprite).default(default.trackSprite).codec(Codec.ibukigourdIdentifier)
            .field(SliderMeta::fillSprite).default(default.fillSprite).codec(Codec.ibukigourdIdentifier)
            .build(::SliderMeta)

        override fun serialization(target: SliderMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<SliderMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的滑条段：缺失/解码失败回落 [SliderMeta] 内置默认。
 */
val SokitsuThemeMeta.slider: SliderMeta
    get() = decodeComponent("slider", SliderMeta, SliderMeta.default)
