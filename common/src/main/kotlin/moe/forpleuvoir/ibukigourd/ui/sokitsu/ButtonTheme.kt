package moe.forpleuvoir.ibukigourd.ui.sokitsu

import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.default

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
    /** 按钮最小宽度（dp）。 */
    val minWidth: Int = 56,

    /** 按钮最小高度（dp）。 */
    val minHeight: Int = 56,

    /** 内容内边距——水平（dp）。 */
    val paddingHorizontal: Int = 18,

    /** 内容内边距——垂直（dp）。 */
    val paddingVertical: Int = 12,
) {

    companion object : Codec<ButtonMeta> by Codec.create<ButtonMeta>()
        .field(ButtonMeta::minWidth).default(56).codec(Codec.int(1..512))
        .field(ButtonMeta::minHeight).default(56).codec(Codec.int(1..512))
        .field(ButtonMeta::paddingHorizontal).default(18).codec(Codec.int(0..128))
        .field(ButtonMeta::paddingVertical).default(12).codec(Codec.int(0..128))
        .build(::ButtonMeta)
}

/**
 * 主题 meta 的按钮段：缺失/解码失败回落 [ButtonMeta] 内置默认。
 */
val SokitsuThemeMeta.button: ButtonMeta
    get() = decodeComponent("button", ButtonMeta, ButtonMeta())
