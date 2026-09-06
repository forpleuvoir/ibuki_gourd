package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.padding
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec

/**
 * 按钮的主题接入声明：token 映射（"什么颜色"）+ 尺寸 meta（"多大/多密"）合一。
 *
 * 一个组件与主题的全部耦合集中在这个文件里，分两部分：
 * 1. [ButtonTokens] —— 组件各部位映射到主题的哪个语义槽位；
 * 2. [ButtonMeta] —— 组件的数值型默认参数（**单位均为 dp**），可被主题资源包的
 *    `sokitsu_meta.json` 中 `ui_meta.button` 段覆盖。
 *
 * 本文件与 [Button] 平级放在同一包；主题核心（[SokitsuThemeMeta]）不认识任何具体组件，
 * 每个组件都以"Token 对象 + Meta 数据类 + 扩展属性"的三件套模式自行接入，
 * 第三方 mod 的自定义组件照抄此模式即可。
 */
object ButtonTokens {

    /** 容器精灵的染色色板。 */
    val Container = ColorSchemeToken.Primary

    /** 按钮上文字/图标的颜色（取色板的 [ColorTone.base]）。 */
    val Content = ColorSchemeToken.OnPrimary

    /** 禁用态文字取中性内容色，再用 [DisabledContentOpacity] 压透明度。 */
    val DisabledContent = ColorSchemeToken.OnSurface

    /** 禁用态内容不透明度，沿用 Material3 `DisabledLabelTextOpacity`。 */
    const val DisabledContentOpacity = 0.38f
}

/**
 * 按钮的 meta：**单位均为 dp**（逻辑像素，消费端转 `.dp`；
 * 与屏幕像素的换算由主题的 pixel_scale 决定）。
 *
 * ```jsonc
 * ui_meta: {
 *   button: { min_width: 56, min_height: 56, padding_horizontal: 18, padding_vertical: 12 }
 * }
 * ```
 */
data class ButtonMeta(
    /** 按钮最小尺寸。 */
    val minSize: DpSize,
    /** 内容内边距。 */
    val padding: PaddingValues,
    /**
     * 按钮的纹理
     */
    val sprite: UiStateIdentifier,
) {

    companion object : Codec<ButtonMeta> {

        val default = ButtonMeta(
            minSize = DpSize(56.dp, 56.dp),
            padding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            sprite = UiStateIdentifier(
                normal = identifier("ui/button/normal"),
                pressed = identifier("ui/button/pressed"),
                focused = identifier("ui/button/focused"),
                disabled = identifier("ui/button/disabled")
            )
        )

        private val codec = Codec.create<ButtonMeta>()
            .field(ButtonMeta::minSize).default(default.minSize).codec(Codec.dpSize(1.dp..512.dp, 1.dp..512.dp))
            .field(ButtonMeta::padding).default(default.padding).codec(Codec.padding(0.dp..512.dp))
            .field(ButtonMeta::sprite).default(default.sprite).codec(UiStateIdentifier)
            .build(::ButtonMeta)

        override fun serialization(target: ButtonMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<ButtonMeta> = codec.deserialization(data)
    }
}

/**
 * 主题 meta 的按钮段：缺失/解码失败回落 [ButtonMeta] 内置默认。
 */
val SokitsuThemeMeta.button: ButtonMeta
    get() = decodeComponent("button", ButtonMeta, ButtonMeta.default)

