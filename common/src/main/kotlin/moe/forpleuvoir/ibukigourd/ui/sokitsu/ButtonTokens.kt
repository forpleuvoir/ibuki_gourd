package moe.forpleuvoir.ibukigourd.ui.sokitsu

import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone

/**
 * 按钮（[Button]）的 token 映射。
 *
 * 组件 token 映射表把"组件的某个部位"声明式地映射到"主题的某个语义槽位"，
 * 使换 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme] 时默认配色自动跟随。
 * 命名沿用 Material3：`Container` 容器、`Content` 内容、`Disabled*` 禁用态。
 *
 * 本表与 [Button] 平级放在同一包；后续每个组件的 token 表各自独立成文件，
 * 不再集中堆在 `theme` 包的某个文件里。
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
