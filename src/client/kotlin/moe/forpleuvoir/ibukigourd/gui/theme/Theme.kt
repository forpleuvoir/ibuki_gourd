package moe.forpleuvoir.ibukigourd.gui.theme

import moe.forpleuvoir.nebula.common.color.ARGBColor

interface Theme {

    data class Colors(
        /**
         * 应用程序的主要颜色，用于突出界面中的核心元素。
         */
        val primary: ARGBColor,

        /**
         * 与 `primary` 颜色相近的颜色，用于提供颜色的不同浓度或用于强调。
         */
        val primaryVariant: ARGBColor,

        /**
         * 应用程序的辅助颜色，用于区分次级内容，与 `primary` 形成对比。
         */
        val secondary: ARGBColor,

        /**
         * 与 `secondary` 颜色相近的颜色，用于为次级内容提供不同的外观层次。
         */
        val secondaryVariant: ARGBColor,

        /**
         * 应用程序的背景颜色，通常用于大型容器或视图的底色。
         */
        val background: ARGBColor,

        /**
         * 表面的颜色，用于组件表面，例如卡片或对话框的颜色背景。
         */
        val surface: ARGBColor,

        /**
         * 表示错误状态的颜色，用于向用户传递错误或警告信息。
         */
        val error: ARGBColor,

        /**
         * 主要颜色上的文本或内容的颜色，以确保内容在 `primary` 背景上的可读性。
         */
        val onPrimary: ARGBColor,

        /**
         * 辅助颜色上的文本或内容的颜色，以确保内容在 `secondary` 背景上的可读性。
         */
        val onSecondary: ARGBColor,

        /**
         * 背景颜色上的文本或内容的颜色，用于保证背景上的信息清晰可见。
         */
        val onBackground: ARGBColor,

        /**
         * 表面颜色上的文本或内容的颜色，确保信息在表面组件上的易读性。
         */
        val onSurface: ARGBColor,

        /**
         * 错误颜色上的文本或内容的颜色，用于在错误状态中提供清晰的指导或警告信息。
         */
        val onError: ARGBColor,

        /**
         * 表示当前主题是否为浅色主题的布尔值。如果为 `true`，表示是浅色（light）主题；否则为深色（dark）主题。
         */
        val isLight: Boolean
    )


    val colors: Colors

}