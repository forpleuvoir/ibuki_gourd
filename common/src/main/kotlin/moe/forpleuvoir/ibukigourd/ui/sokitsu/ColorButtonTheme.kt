package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.padding
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec

/**
 * [ColorButton] 的主题接入声明（同 [ButtonTheme] 的三件套模式：Token + Meta + 扩展属性）。
 *
 * 与 [Button] 的差别：容器底色由调用方**任意指定**，不来自颜色槽位表，
 * 因此这里没有"容器取哪个槽位"的映射，只剩数值型 meta 与禁用态透明度。
 */
object ColorButtonTokens {

    /**
     * 禁用态内容色不透明度：内容色（黑/白之一）乘本值。
     *
     * 与 [ButtonTokens.DisabledContentOpacity] 取同值，保证同类组件禁用观感一致；
     * 底色不用禁用槽位替换（禁用外观由 `color_button/disabled` 素材表达）。
     */
    const val DisabledContentOpacity = 0.62f
}

/**
 * 颜色按钮的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`）。
 *
 * ```jsonc
 * ui_meta: {
 *   color_button: { min_width: 56, min_height: 56, padding_horizontal: 18, padding_vertical: 12 }
 * }
 * ```
 *
 * 自带独立的 sprite 段（`ui/color_button/` 下四态），与 [ButtonMeta] 互不影响；
 * 缺省尺寸/内边距与 [ButtonMeta.default] 取同值，仅纹理路径不同。
 */
data class ColorButtonMeta(
    /** 按钮最小尺寸。 */
    val minSize: DpSize,
    /** 内容内边距。 */
    val padding: PaddingValues,
    /** 按钮的纹理。 */
    val sprite: UiStateIdentifier,
) {

    companion object : Codec<ColorButtonMeta> {

        val default = ColorButtonMeta(
            minSize = DpSize(56.dp, 56.dp),
            padding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            sprite = UiStateIdentifier(
                normal = identifier("ui/color_button/normal"),
                pressed = identifier("ui/color_button/pressed"),
                focused = identifier("ui/color_button/focused"),
                disabled = identifier("ui/color_button/disabled"),
            )
        )

        private val codec = Codec.create<ColorButtonMeta>()
            .field(ColorButtonMeta::minSize).default(default.minSize).codec(Codec.dpSize(1.dp..512.dp, 1.dp..512.dp))
            .field(ColorButtonMeta::padding).default(default.padding).codec(Codec.padding(0.dp..512.dp))
            .field(ColorButtonMeta::sprite).default(default.sprite).codec(UiStateIdentifier)
            .build(::ColorButtonMeta)

        override fun serialization(target: ColorButtonMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<ColorButtonMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的颜色按钮段：缺失/解码失败回落 [ColorButtonMeta] 内置默认。
 */
val SokitsuThemeMeta.colorButton: ColorButtonMeta
    get() = decodeComponent("color_button", ColorButtonMeta, ColorButtonMeta.default)
