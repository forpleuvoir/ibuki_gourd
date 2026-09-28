package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.text.translateText
import moe.forpleuvoir.ibukigourd.ui.selector.Selector
import moe.forpleuvoir.ibukigourd.ui.selector.SelectorExpandStyle
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
 * @param controlModifier 附加到控件区（选择器那一格）；用于给选择器挂 tooltip 一类修饰
 * @param onSelected 选中某项后的回调（值已写入配置）；用于"选中特定项时顺带做点什么"
 */
@Composable
fun <E : Enum<E>> EnumConfigWrapper(
    config: ConfigEnum<E>,
    modifier: Modifier = Modifier,
    controlModifier: Modifier = Modifier,
    onSelected: ((E) -> Unit)? = null,
) {
    val value by config.asState()
    val items = enumConstantsOf(value)

    ConfigRowWrapper(config, modifier) {
        ConfigControlBlock(modifier = controlModifier) {
            EnumSelector(
                selected = value,
                items = items,
                onSelect = {
                    config.setValue(it)
                    onSelected?.invoke(it)
                },
            )
        }
    }
}

@Composable
fun <E : Enum<E>> EnumSelector(
    selected: E,
    onSelect: (E) -> Unit,
    items: List<E>,
    content: @Composable (E) -> Unit = { Text(InlineStyleText(it.translateText.plainText)) },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemEquals: (E, E) -> Boolean = { a, b -> a == b },
    itemContent: @Composable (E, Boolean) -> Unit = { item, _ -> Text(InlineStyleText(item.translateText.plainText)) },
    itemLeadingIcon: ((Boolean) -> (@Composable (E) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (E) -> Unit)?)? = null,
    searchFilter: ((E, String) -> Boolean)? = if (items.size > ConfigControlDefaults.EnumSelectSearchThreshold) {
        { item, query -> item.translateText.plainText.contains(query, ignoreCase = true) }
    } else {
        null
    },
    expandStyle: SelectorExpandStyle = SelectorExpandStyle.Auto(),
    onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    Selector(
        selected = selected,
        onSelect = onSelect,
        items = items,
        content = content,
        modifier = modifier,
        enabled = enabled,
        itemEquals = itemEquals,
        itemContent = itemContent,
        searchFilter =searchFilter,
        itemLeadingIcon = itemLeadingIcon,
        itemTrailingIcon = itemTrailingIcon,
        expandStyle = expandStyle,
        onExpandedChange = onExpandedChange,
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

