package moe.forpleuvoir.ibukigourd.ui.sokitsu

import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken

/**
 * 表面容器（[Surface]）的 token 映射。
 *
 * 对应 Material3 `Surface` 的默认取色：容器 = `surface`、内容 = `onSurface`。
 * 内容色**不登记 token**，而是由解析后的容器色板配对推导
 * （[moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone.contentColor]）：
 * 这样作用域把 tone 切成 secondary / error 时，内容色会自动跟着变成
 * `onSecondary` / `onError`；固定取 token 反而会丢掉这个联动
 * （与 [ButtonTokens.Content] 的处理一致）。
 *
 * 本文件与 [Surface] 平级放在同一包；后续若引入 Surface 的数值型 meta
 * （如面板最小内边距），也应并入本文件，与 [ButtonTheme.kt] / [SwitchTheme.kt] 同规矩。
 */
object SurfaceTokens {

    /** 容器背景精灵的染色色板。 */
    val Container = ColorSchemeToken.Surface
}
