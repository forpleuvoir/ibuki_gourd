package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.editdialog.EditDialogContent
import moe.forpleuvoir.ibukigourd.ui.editdialog.EditDialogContentList
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TableColumnWidth
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TableLayoutScope
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.util.Keyed
import moe.forpleuvoir.ibukigourd.ui.util.KeyedListState
import moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList
import moe.forpleuvoir.ibukigourd.ui.util.values
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.item.ConfigList
import net.minecraft.network.chat.Component

/**
 * 列表：行上显示条目数，点开在浮层里编辑（增删 + 拖拽排序）。
 *
 * 条目控件按元素类型由 [ConfigElementEditor] 给出；元素类型给不出安全初值时（如自定义类型）
 * **不提供新增按钮**（塞一个无法序列化的默认值只会把配置写坏）。
 *
 * @param config 列表配置项
 * @param modifier 作用于整行
 * @param contentColumnWidth 内容列宽度
 */
@Composable
fun ConfigListWrapper(
    config: ConfigList<Any>,
    modifier: Modifier = Modifier,
    contentColumnWidth: TableColumnWidth = ConfigDialogDefaults.FillColumnWidth,
) {
    var editing by remember(config) { mutableStateOf(false) }
    val size by config.asDerivedState { it.size }
    val newElement = defaultElementFactory(config.elementType)

    ConfigListRow(config, IGLang.ConfigWrapper.listConfigWrapperText(size), modifier) { editing = true }

    if (editing) {
        ConfigListEditDialog(
            config = config,
            newElement = newElement,
            onDismiss = { editing = false },
            columns = { state ->
                column(
                    width = contentColumnWidth,
                    alignment = Alignment.CenterStart,
                    header = { Text(IGLang.Misc.content) },
                ) { index, entry ->
                    ConfigElementEditor(
                        value = entry.value,
                        onValueChange = { state.setValue(index, it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        )
    }
}

/**
 * 对列表：每行两段，**前项 / 后项各占一列**，语义由配置用途决定。
 *
 * 两列都用 [ConfigElementEditor]，因此**不假设分量是字符串**（`ConfigList<Pair<Int, Int>>` 也给数值框）；
 * 分量类型给不出安全初值时不给新增按钮。空列表无从推断分量类型，按字符串对兜底（旧语义）。
 *
 * @param config 对列表配置项
 * @param modifier 作用于整行
 * @param firstColumnWidth 前项列宽度
 * @param secondColumnWidth 后项列宽度
 */
@Composable
fun PairListConfigWrapper(
    config: ConfigList<Pair<Any, Any>>,
    modifier: Modifier = Modifier,
    firstColumnWidth: TableColumnWidth = ConfigDialogDefaults.FillColumnWidth,
    secondColumnWidth: TableColumnWidth = ConfigDialogDefaults.FillColumnWidth,
) {
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
            columns = { state ->
                column(
                    width = firstColumnWidth,
                    alignment = Alignment.CenterStart,
                    header = { Text(IGLang.ConfigWrapper.pairFirst) },
                ) { index, entry ->
                    ConfigElementEditor(
                        value = entry.value.first,
                        onValueChange = { state.setValue(index, entry.value.copy(first = it)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                column(
                    width = secondColumnWidth,
                    alignment = Alignment.CenterStart,
                    header = { Text(IGLang.ConfigWrapper.pairSecond) },
                ) { index, entry ->
                    ConfigElementEditor(
                        value = entry.value.second,
                        onValueChange = { state.setValue(index, entry.value.copy(second = it)) },
                        modifier = Modifier.fillMaxWidth(),
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
 * 内容列由调用方声明（普通列表一列、对列表前项 / 后项两列），**判定用的条目状态通过 [columns]
 * 的参数交回**，单元格才能写回副本。
 *
 * @param config 目标列表
 * @param newElement 新增条目的默认值工厂；null 表示该元素类型给不出安全初值（不渲染新增按钮）
 * @param onDismiss 关闭浮层
 * @param columns 内容列声明；参数为可写回的条目容器
 */
@Composable
internal fun <E : Any> ConfigListEditDialog(
    config: ConfigList<E>,
    newElement: (() -> E)?,
    onDismiss: () -> Unit,
    columns: TableLayoutScope<Keyed<E>>.(state: KeyedListState<E>) -> Unit,
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
        maxHeight = ConfigDialogDefaults.MaxHeight,
        content = {
            EditDialogContent(
                modifier = Modifier.width(ConfigDialogDefaults.ContentWidth),
                // 表头交给表格自己（列宽与单元格天然对齐），这里不再叠一层
                header = {},
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
                EditDialogContentList(
                    state = keyed,
                    lazyListState = listState,
                    columns = { columns(keyed) },
                )
            }
        },
    )
}
