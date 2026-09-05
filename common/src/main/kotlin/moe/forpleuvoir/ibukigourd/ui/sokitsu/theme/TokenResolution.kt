package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified

/**
 * 组件 token 的标准解析链（与 [ColorSchemeToken] 配合）：把组件登记的颜色回退到主题槽位。
 *
 * 具体每个组件的 token 映射表（[moe.forpleuvoir.ibukigourd.ui.sokitsu.ButtonTokens]、
 * [moe.forpleuvoir.ibukigourd.ui.sokitsu.SwitchTokens] 等）已各自独立成文件，
 * 与组件平级放在 `ui.sokitsu` 包；本文件只保留组件无关的解析设施。
 */

/**
 * [Color] 版本的禁用态解析链，用于内容色（文字/图标）这类**单色**场景。
 *
 * 与 [ColorTone.resolveFaded] 的区别：这里取的是槽位色板的 [ColorTone.base]，
 * 因为文字/图标只需要一个颜色，不需要完整的色阶家族。
 *
 * 调用点显式传入时**不**叠加 alpha；只有走主题推导时才套用 [alpha]。
 */
@Composable
fun Color.resolveFaded(token: ColorSchemeToken, alpha: Float): Color =
    if (isSpecified) this
    else LocalColorScheme.current.fromToken(token).base.copy(alpha = alpha)

/**
 * 组件 token 的**标准解析链**：调用点 → 作用域色板 → 组件 token → 主题槽位。
 *
 * ```kotlin
 * tone.resolve(SwitchTokens.CheckedTrack)
 * ```
 * 等价于
 * ```kotlin
 * tone.takeOrElse { LocalSokitsuTone.current }
 *     .takeOrElse { LocalColorScheme.current.fromToken(SwitchTokens.CheckedTrack) }
 * ```
 *
 * 中间那一层 [LocalSokitsuTone] 是 Sokitsu 比 Material3 多出来的一级：
 * 允许在子树内整体切换色板（如把某块面板里的所有按钮换成 secondary），
 * 作用域值本身也可以是 [ColorTone.Unspecified]，从而继续往下落到 token 表。
 *
 * @param token 该部位在组件 token 表中登记的语义槽位
 * @return 必定是已解析的色板（[ColorTone.isSpecified] == true）
 */
@Composable
fun ColorTone.resolve(token: ColorSchemeToken): ColorTone =
    this.takeOrElse { LocalSokitsuTone.current }
        .takeOrElse { LocalColorScheme.current.fromToken(token) }

/**
 * 组件 token 的**禁用态解析链**：与 [resolve] 相同的回退顺序，最后统一压一层 alpha。
 *
 * 调用点显式传入时**不**叠加 alpha（调用方已经决定了禁用态长什么样）；
 * 只有走主题推导时才套用 [alpha]，避免二次压暗导致过淡。
 *
 * 叠加用 [ColorTone.withAlpha] 而非只压 [ColorTone.base]，
 * 因为精灵染色会分别取 outline/dark/base/highlight 各档。
 *
 * @param token 该部位在组件 token 表中登记的语义槽位
 * @param alpha 推导时的不透明度
 */
@Composable
fun ColorTone.resolveFaded(token: ColorSchemeToken, alpha: Float): ColorTone =
    if (isSpecified) this
    else this.takeOrElse { LocalSokitsuTone.current }
        .takeOrElse { LocalColorScheme.current.fromToken(token) }
        .withAlpha(alpha)
