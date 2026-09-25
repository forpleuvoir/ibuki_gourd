package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 配置驱动的主题覆盖：用户选的配色方案从此处生效，[SokitsuTheme] 的缺省色先看它。
 *
 * 与 [SokitsuThemeMeta] 同为**全局单例 + snapshot state**：值变化会立刻重组所有界面，
 * 不需要重新进屏。写入方只有一处（配置项的 `observe`），见 `IGConfig.Gui.Theme`。
 *
 * 值为 `null` 表示"没有覆盖"，此时仍按 [SokitsuThemeMeta]（资源包）+ [systemTheme] 解析。
 */
object SokitsuThemeOverride {

    /** 覆盖用的配色方案；null = 不覆盖。 */
    var scheme: ColorScheme? by mutableStateOf(null)
}
