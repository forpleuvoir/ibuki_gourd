package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.editdialog.EditDialogContent
import moe.forpleuvoir.ibukigourd.ui.editdialog.EditDialogContentList
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TableColumnWidth
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip
import moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList
import moe.forpleuvoir.ibukigourd.ui.util.values
import moe.forpleuvoir.nebula.config.item.ConfigMap

/**
 * 映射：行上显示条目数，点开在浮层里编辑（键值对增删 + 拖拽排序）。
 *
 * 键固定是字符串；值控件按值类型由 [ConfigElementEditor] 给出，值类型给不出安全初值时
 * （如自定义类型）**不提供新增按钮**。
 *
 * 顺序按 `LinkedHashMap` 保留。键**不在行内编辑**：点开单独的键浮层修改，重复的键在那一步就被拒绝
 * （键是映射的身份，静默覆盖会丢数据）。编辑在副本上进行，确认时才整体写回。
 *
 * @param config 映射配置项
 * @param modifier 作用于整行
 * @param keyColumnWidth 键列宽度
 * @param valueColumnWidth 值列宽度
 */
@Composable
fun ConfigMapWrapper(
    config: ConfigMap<Any>,
    modifier: Modifier = Modifier,
    keyColumnWidth: TableColumnWidth = ConfigDialogDefaults.KeyColumnWidth,
    valueColumnWidth: TableColumnWidth = ConfigDialogDefaults.FillColumnWidth,
) {
    var editing by remember(config) { mutableStateOf(false) }
    val size by config.asDerivedState { it.size }

    ConfigListRow(config, IGLang.ConfigWrapper.mapConfigWrapperText(size), modifier) { editing = true }

    if (editing) {
        MapEditDialog(config, keyColumnWidth, valueColumnWidth, onDismiss = { editing = false })
    }
}

/** 映射编辑浮层：键值两列 + 增删 + 拖拽排序。 */
@Composable
private fun MapEditDialog(
    config: ConfigMap<Any>,
    keyColumnWidth: TableColumnWidth,
    valueColumnWidth: TableColumnWidth,
    onDismiss: () -> Unit,
) {
    val keyed = rememberKeyedList(config.getValue().entries.map { it.key to it.value }, key = config)
    val newValue = defaultElementFactory(config.entryValueType)
    // 正在改键的条目：存条目 key 而不是下标 —— 拖拽重排后下标会变，key 不会
    var editingKey by remember { mutableStateOf<Long?>(null) }

    FlexibleDialog(
        onDismissRequest = onDismiss,
        onConfirmRequest = {
            config.setValue(keyed.entries.values().toMap())
            true
        },
        title = { ConfigDialogTitle(config) },
        minWidth = ConfigDialogDefaults.MinWidth,
        maxHeight = ConfigDialogDefaults.MaxHeight,
        content = {
            EditDialogContent(
                modifier = Modifier.width(ConfigDialogDefaults.ContentWidth),
                // 表头交给表格自己（键 / 值两列由表格保证跨行对齐），这里不再叠一层
                header = {},
                addButton = newValue?.let { factory ->
                    {
                        Button(
                            onClick = { keyed.add(uniqueKey(keyed.entries.values()) to factory()) },
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
                    columns = {
                        column(
                            width = keyColumnWidth,
                            header = { Text(IGLang.ConfigWrapper.mapKey) },
                        ) { _, entry ->
                            // 键只做展示，改键走右侧的编辑按钮：行内输入没法在提交时拦住重复键
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(configControlHeight()),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        entry.value.first,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    MapEditButton(
                                        onClick = { editingKey = entry.key },
                                        iconScale = MapKeyIconScale,
                                        minSize = MapKeyButtonMinSize,
                                    )
                                }
                            }
                        }
                        column(
                            width = valueColumnWidth,
                            alignment = Alignment.CenterStart,
                            header = { Text(IGLang.ConfigWrapper.mapValue) },
                        ) { index, entry ->
                            ConfigElementEditor(
                                value = entry.value.second,
                                onValueChange = { keyed.setValue(index, entry.value.first to it) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    },
                )

                // 键浮层叠在编辑浮层之上；条目被删掉（key 找不到）时直接不组合
                val target = editingKey
                val index = target?.let { key -> keyed.entries.indexOfFirst { it.key == key } } ?: -1
                if (target != null && index >= 0) {
                    MapKeyEditDialog(
                        initial = keyed.entries[index].value.first,
                        isTaken = { candidate ->
                            keyed.entries.any { it.key != target && it.value.first == candidate }
                        },
                        onDismiss = { editingKey = null },
                        onConfirm = { newKey ->
                            keyed.setValue(index, newKey to keyed.entries[index].value.second)
                            editingKey = null
                        },
                    )
                }
            }
        },
    )
}

/**
 * 键编辑浮层：确认时校验新键是否与**其它**条目撞车。撞车时输入框转错误态、并把提示钉在输入框上，
 * 改动文本即清掉错误。
 *
 * @param initial 当前键
 * @param isTaken 该键是否已被其它条目占用
 * @param onDismiss 关闭浮层
 * @param onConfirm 接受新键
 */
@Composable
private fun MapKeyEditDialog(
    initial: String,
    isTaken: (String) -> Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var key by remember { mutableStateOf(initial) }
    var showError by remember { mutableStateOf(false) }
    val taken = isTaken(key)

    FlexibleDialog(
        onDismissRequest = onDismiss,
        onConfirmRequest = {
            if (taken) {
                showError = true
                false
            } else {
                onConfirm(key)
                true
            }
        },
        title = { Text(IGLang.ConfigWrapper.mapKey) },
        content = {
            StringValueField(
                value = key,
                onValueChange = {
                    key = it
                    showError = false
                },
                modifier = Modifier
                    .width(ConfigControlDefaults.ControlWidth)
                    .tooltip(pinned = showError) { Text(IGLang.ConfigWrapper.keyExists(key)) },
                isError = taken,
            )
        },
    )
}

/**
 * 行内「编辑」图标按钮：键列窄，用比配置页默认小一档的图标与最小尺寸，热区仍够点。
 *
 * @param iconScale 图标倍率，缺省跟随 sokitsu 像素缩放
 * @param minSize 按钮最小尺寸
 */
@Composable
private fun MapEditButton(
    onClick: () -> Unit,
    iconScale: Int = LocalSokitsuPixelScale.current,
    minSize: DpSize = IconButtonDefaults.minSize,
) {
    IconButton(
        onClick = onClick,
        contentPadding = ConfigControlDefaults.IconButtonPadding,
        minSize = minSize,
    ) {
        Icon(Icons.Edit, scale = iconScale)
    }
}

/** 键列编辑按钮的最小尺寸：按 2 倍图标（32dp）留一圈。 */
private val MapKeyButtonMinSize = DpSize(40.dp, 40.dp)

/** 键列编辑按钮的图标倍率：键列窄，比配置页默认的像素倍率小一档。 */
private const val MapKeyIconScale = 2

/** 生成一个当前不冲突的默认键（`key1`、`key2`…）。 */
private fun uniqueKey(existing: List<Pair<String, Any>>): String {
    val used = existing.mapTo(mutableSetOf()) { it.first }
    var index = 1
    while ("key$index" in used) index++
    return "key$index"
}
