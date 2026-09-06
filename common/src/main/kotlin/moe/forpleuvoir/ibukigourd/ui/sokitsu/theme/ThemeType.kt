package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

/**
 * 主题类型：[Light] 浅色 / [Dark] 深色 / [Unknown] 未知。
 *
 * [Unknown] 表示系统主题探测失败或平台不支持，无法确知亮暗；
 * 需要二值决策时经 [isLight] / [isDark] 收敛，**未知一律按浅色处理**，
 * 与 Sokitsu 其余 API（[SokitsuThemeMeta.colorScheme] 等）的浅色默认保持一致。
 */
enum class ThemeType {

    Light, Dark, Unknown;

    /** 是否按浅色处理：[Unknown] 回落浅色。 */
    val isLight: Boolean get() = this != Dark

    /** 是否确知为深色：[Unknown] 不算深色。 */
    val isDark: Boolean get() = this == Dark

}
