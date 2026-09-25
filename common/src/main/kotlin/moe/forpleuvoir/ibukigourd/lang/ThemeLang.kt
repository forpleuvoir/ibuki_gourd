package moe.forpleuvoir.ibukigourd.lang

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Translatable

@Suppress("NOTHING_TO_INLINE")
object ThemeLang {

    @PublishedApi
    internal inline fun lang(key: String, vararg args: Any): MutableText =
        Translatable("${IbukiGourd.MOD_ID}.theme.$key", args = args)

    /** 配色方案编辑器的标题。 */
    inline val editorTitle get() = lang("editor.title")

    /** 编辑器里"以默认浅色为底"的按钮文案。 */
    inline val baseFromLight get() = lang("base_from_light")

    /** 编辑器里"以默认深色为底"的按钮文案。 */
    inline val baseFromDark get() = lang("base_from_dark")

    /** 编辑器里亮暗标记开关的标签（开启 = 这份配色是深色方案）。 */
    inline val darkMode get() = lang("dark_mode")
}
