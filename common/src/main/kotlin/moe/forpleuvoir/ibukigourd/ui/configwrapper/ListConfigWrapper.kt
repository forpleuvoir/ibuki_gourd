package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.EditDialogContent
import moe.forpleuvoir.ibukigourd.ui.sokitsu.EditDialogContentList
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList
import moe.forpleuvoir.ibukigourd.ui.util.values
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.item.ConfigList
import net.minecraft.network.chat.Component

/**
 * 列表：行上显示条目数，点开在浮层里编辑（增删 + 拖拽排序）。
 *
 * 元素控件按元素类型由 [ConfigElementEditor] 给出；元素类型给不出安全初值时（如自定义类型）
 * **不提供新增按钮**（塞一个无法序列化的默认值只会把配置写坏）。
 *
 * @param config 列表配置项
 * @param modifier 作用于整行
 */
@Composable
fun ConfigListWrapper(config: ConfigList<Any>, modifier: Modifier = Modifier) {
    var editing by remember(config) { mutableStateOf(false) }
    val size by config.asDerivedState { it.size }
    val newElement = defaultElementFactory(config.elementType)

    ConfigListRow(config, IGLang.ConfigWrapper.listConfigWrapperText(size), modifier) { editing = true }

    if (editing) {
        ConfigListEditDialog(
            config = config,
            newElement = newElement,
            onDismiss = { editing = false },
            elementEditor = { value, onValueChange ->
                ConfigElementEditor(value, onValueChange, Modifier.fillMaxWidth())
            },
        )
    }
}

/**
 * 对列表：每行两段（首 / 次，语义由配置用途决定）。
 *
 * 两段都用 [ConfigElementEditor]，因此**不假设分量是字符串**（`ConfigList<Pair<Int, Int>>` 也给数值框）；
 * 段类型给不出安全初值时不给新增按钮。空列表无从推断分量类型，按字符串对兜底（旧语义）。
 *
 * @param config 对列表配置项
 * @param modifier 作用于整行
 */
@Composable
fun PairListConfigWrapper(config: ConfigList<Pair<Any, Any>>, modifier: Modifier = Modifier) {
    var editing by remember(config) { mutableStateOf(false) }
    val size by config.asDerivedState { it.size }

    ConfigListRow(config, IGLang.ConfigWrapper.listConfigWrapperText(size), modifier) { editing = true }

    if (editing) {
        val sample = config.getValue().firstOrNull()
        val newElement: (() -> Pair<Any, Any>)? = if (sample == null) {
            { "" to "" }
        } else {
            val first = defaultElementFactory(sample.first::class)
            val second = defaultElementFactory(sample.second::class)
            if (first != null && second != null) {
                { first() to second() }
            } else {
                null
            }
        }

        ConfigListEditDialog(
            config = config,
            newElement = newElement,
            onDismiss = { editing = false },
            elementEditor = { value, onValueChange ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                ) {
                    Text(IGLang.ConfigWrapper.pairFirst)
                    ConfigElementEditor(
                        value = value.first,
                        onValueChange = { onValueChange(value.copy(first = it)) },
                        modifier = Modifier.width(ConfigControlDefaults.ControlWidth / 2),
                    )
                    Text(IGLang.ConfigWrapper.pairSecond)
                    ConfigElementEditor(
                        value = value.second,
                        onValueChange = { onValueChange(value.copy(second = it)) },
                        modifier = Modifier.width(ConfigControlDefaults.ControlWidth / 2),
                    )
                }
            },
        )
    }
}

/**
 * 列表类配置的行骨架：条目数按钮 + 编辑按钮，两者都打开编辑浮层。
 *
 * @param config 行绑定的配置节点
 * @param label 按钮文案（`IGLang.ConfigWrapper` 的 list / map 两条）
 * @param modifier 作用于整行
 * @param onEdit 打开编辑浮层
 */
@Composable
internal fun ConfigListRow(
    config: ConfigNode,
    label: Component,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
) {
    ConfigRowWrapper(config, modifier) {
        ConfigControlBlock(
            action = {
                IconButton(
                    onClick = onEdit,
                    contentPadding = ConfigControlDefaults.IconButtonPadding,
                ) {
                    Icon(Icons.Edit, scale = configIconScale())
                }
            },
        ) {
            // 纯展示：条目数放进 Surface（不是按钮），点开编辑走右侧的编辑按钮
            Surface(
                modifier = Modifier.weight(1f).height(configControlHeight()),
                contentAlignment = Alignment.Center,
            ) {
                Text(component = label)
            }
        }
    }
}

/**
 * 列表编辑浮层：在副本上编辑，确认时整体写回（取消 / 遮罩关闭直接丢弃）。
 *
 * @param config 目标列表
 * @param newElement 新增条目的默认值工厂；null 表示该元素类型给不出安全初值（不渲染新增按钮）
 * @param onDismiss 关闭浮层
 * @param elementEditor 单条内容编辑器
 */
@Composable
internal fun <E : Any> ConfigListEditDialog(
    config: ConfigList<E>,
    newElement: (() -> E)?,
    onDismiss: () -> Unit,
    elementEditor: @Composable (value: E, onValueChange: (E) -> Unit) -> Unit,
) {
    val keyed = rememberKeyedList(config.getValue(), key = config)

    FlexibleDialog(
        onDismissRequest = onDismiss,
        onConfirmRequest = {
            config.setValue(keyed.entries.values())
            true
        },
        title = { ConfigDialogTitle(config) },
        minWidth = ConfigDialogDefaults.MinWidth,
        content = {
            EditDialogContent(
                modifier = Modifier
                    .width(ConfigDialogDefaults.ContentWidth)
                    .heightIn(min = ConfigDialogDefaults.MinHeight),
                addButton = newElement?.let { factory ->
                    {
                        Button(
                            onClick = { keyed.add(factory()) },
                            contentPadding = ConfigControlDefaults.IconButtonPadding,
                        ) {
                            Icon(Icons.Add, scale = configIconScale())
                        }
                    }
                },
            ) { listState ->
                EditDialogContentList(state = keyed, lazyListState = listState) { index, value, _ ->
                    elementEditor(value) { keyed.setValue(index, it) }
                }
            }
        },
    )
}
