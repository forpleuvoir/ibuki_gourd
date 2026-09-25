package moe.forpleuvoir.ibukigourd.mod.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import moe.forpleuvoir.ibukigourd.ui.configwrapper.asState
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeEditorDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ThemeType
import moe.forpleuvoir.nebula.config.Config

/**
 * 主题配色模式：跟随系统 / 深色 / 浅色 / 自定义。
 *
 * [Custom] 时取用户自己那份 [ColorScheme]（在配色编辑器里整份编辑，含亮暗标记）。
 */
enum class ThemeMode { FollowSystem, Dark, Light, Custom }

/**
 * 配置 → 运行时配色。
 *
 * - [ThemeMode.FollowSystem]：返回 `null`，表示不覆盖 —— 由资源包配色 + 系统探测决定；
 * - [ThemeMode.Dark] / [ThemeMode.Light]：取主题 meta 对应 section 的配色；
 * - [ThemeMode.Custom]：直接用 [customScheme]。
 *
 * @param mode 配置里的主题配色模式
 * @param customScheme 用户那份配色
 */
fun resolveThemeScheme(mode: ThemeMode, customScheme: ColorScheme): ColorScheme? = when (mode) {
    ThemeMode.FollowSystem -> null
    ThemeMode.Dark         -> SokitsuThemeMeta.colorScheme(ThemeType.Dark)
    ThemeMode.Light        -> SokitsuThemeMeta.colorScheme(ThemeType.Light)
    ThemeMode.Custom       -> customScheme
}

/**
 * 自定义配色编辑器：编辑配置里那一整份 [ColorScheme]，确认后整体写回。
 *
 * @param schemeConfig 存配色的配置项
 * @param onDismiss 关闭编辑器
 */
@Composable
fun CustomColorSchemeEditor(schemeConfig: Config<ColorScheme>, onDismiss: () -> Unit) {
    val scheme by schemeConfig.asState()
    ColorSchemeEditorDialog(
        scheme = scheme,
        onDismiss = onDismiss,
        onSchemeChange = { schemeConfig.setValue(it) },
    )
}
