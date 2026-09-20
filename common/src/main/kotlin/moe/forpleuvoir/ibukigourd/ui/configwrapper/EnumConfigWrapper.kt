package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.text.translateText
import moe.forpleuvoir.ibukigourd.ui.selector.Selector
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.nebula.config.item.ConfigEnum

/**
 * 枚举：下拉选择器。
 *
 * 选项文案走 `T.translateText`（键 `enum.<类名>.<常量名>`，缺翻译时回落 `TitleCase(常量名)`）；
 * 选项超过 10 个时给选择器挂搜索框（键盘键码这类长枚举在 [moe.forpleuvoir.ibukigourd.ui.keybind.KeySetter] 侧另有专用控件）。
 *
 * @param config 枚举配置项
 * @param modifier 作用于整行
 */
@Composable
fun <E : Enum<E>> EnumConfigWrapper(config: ConfigEnum<E>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val items = enumConstantsOf(value)

    ConfigRowWrapper(config, modifier) {
        EnumSelectorContent(
            value = value,
            items = items,
            onSelect = { config.setValue(it) },
        )
    }
}

@Composable
private fun <E : Enum<E>> EnumSelectorContent(
    value: E,
    items: List<E>,
    onSelect: (E) -> Unit,
) {
    Selector(
        selected = value,
        onSelect = onSelect,
        items = items,
        content = { Text(InlineStyleText(it.translateText.plainText)) },
        itemContent = { item, _ -> Text(InlineStyleText(item.translateText.plainText)) },
        searchFilter = if (items.size > EnumSearchThreshold) {
            { item, query -> item.translateText.plainText.contains(query, ignoreCase = true) }
        } else {
            null
        },
        modifier = Modifier.width(ConfigControlDefaults.SelectorWidth),
    )
}

/**
 * 取枚举常量的全部取值。
 *
 * 用 [Enum.declaringJavaClass] 而不是 `value::class`：带常量体的枚举常量其运行时类是匿名子类，
 * `enumConstants` 会是 null，而 declaring class 始终是枚举本体。
 */
private fun <E : Enum<E>> enumConstantsOf(value: E): List<E> =
    value.declaringJavaClass.enumConstants?.toList() ?: listOf(value)

/** 选项数超过该值时给选择器挂搜索框。 */
private const val EnumSearchThreshold = 10
