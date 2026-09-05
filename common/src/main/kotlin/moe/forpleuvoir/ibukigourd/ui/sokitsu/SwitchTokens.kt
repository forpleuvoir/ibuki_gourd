package moe.forpleuvoir.ibukigourd.ui.sokitsu

import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken

/**
 * 开关（[SwitchDefaults]）的 token 映射。
 *
 * 轨道按开/关两态取不同色：开启态用主色容器色 [ColorSchemeToken.PrimaryContainer]
 * （与开启态把手 [ColorSchemeToken.Primary] 同源、更饱和/明亮，避免原 [ColorSchemeToken.SurfaceVariant] 偏淡），
 * 关闭态退回主题背景色 [ColorSchemeToken.Background]（与页面同色，弱化为"未开启"）；
 * 把手（关闭态）走中性灰 [ColorSchemeToken.OnSurfaceVariant]，
 * 与主题中性内容色一致（本库不是 M3，未设独立的 `outline` 描边槽位）。
 *
 * 本表与 [Switch] 平级放在同一包；后续每个组件的 token 表各自独立成文件，
 * 不再集中堆在 `theme` 包的某个文件里。
 */
object SwitchTokens {

    /** 开启态把手。 */
    val CheckedThumb = ColorSchemeToken.Primary

    /** 开启态轨道：主色容器色（[ColorSchemeToken.PrimaryContainer]），与开启态把手同源、明显可分。 */
    val CheckedTrack = ColorSchemeToken.PrimaryContainer

    /** 关闭态把手：中性灰（[ColorSchemeToken.OnSurfaceVariant]）。 */
    val UncheckedThumb = ColorSchemeToken.OnSurfaceVariant

    /** 关闭态轨道：退回主题背景色（[ColorSchemeToken.Background]），与页面同色、弱化为"未开启"。 */
    val UncheckedTrack = ColorSchemeToken.Background

    /** 禁用态把手基准色（会被 [DisabledThumbOpacity] 压透明）。 */
    val DisabledThumb = ColorSchemeToken.OnSurface

    /** 禁用态轨道基准色（会被 [DisabledTrackOpacity] 压透明）。 */
    val DisabledTrack = ColorSchemeToken.SurfaceVariant

    /** 禁用态把手不透明度，沿用 Material3 `DisabledLabelTextOpacity`。 */
    const val DisabledThumbOpacity = 0.38f

    /** 禁用态轨道不透明度，沿用 Material3 `DisabledContainerOpacity`。 */
    const val DisabledTrackOpacity = 0.12f
}
