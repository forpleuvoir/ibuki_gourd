package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList
import moe.forpleuvoir.ibukigourd.ui.util.values
import moe.forpleuvoir.nebula.config.item.ConfigMap

/**
 * 映射：行上显示条目数，点开在浮层里编辑（键值对增删 + 拖拽排序）。
 *
 * 键固定是字符串；值控件按值类型由 [ConfigElementEditor] 给出，值类型给不出安全初值时
 * （如自定义类型）**不提供新增按钮**。
 *
 * 顺序按 `LinkedHashMap` 保留；键重复时**拒绝关闭**并弹提示（键是映射的身份，静默覆盖会丢数据）。
 * 编辑在副本上进行，确认时才整体写回。
 *
 * @param config 映射配置项
 * @param modifier 作用于整行
 */
@Composable
fun ConfigMapWrapper(config: ConfigMap<Any>, modifier: Modifier = Modifier) {
    var editing by remember(config) { mutableStateOf(false) }
    val size by config.asDerivedState { it.size }

    ConfigListRow(config, IGLang.ConfigWrapper.mapConfigWrapperText(size), modifier) { editing = true }

    if (editing) {
        MapEditDialog(config, onDismiss = { editing = false })
    }
}

/** 映射编辑浮层：键值两列 + 增删 + 拖拽排序。 */
@Composable
private fun MapEditDialog(config: ConfigMap<Any>, onDismiss: () -> Unit) {
    val keyed = rememberKeyedList(config.getValue().entries.map { it.key to it.value }, key = config)
    val newValue = defaultElementFactory(config.entryValueType)

    FlexibleDialog(
        onDismissRequest = onDismiss,
        onConfirmRequest = {
            val entries = keyed.entries.values()
            val duplicate = entries.groupingBy { it.first }
                .eachCount()
                .entries
                .firstOrNull { it.value > 1 }
                ?.key

            if (duplicate != null) {
                ToastHandler.showContent { Text(IGLang.ConfigWrapper.keyExists(duplicate)) }
                false
            } else {
                config.setValue(entries.toMap())
                true
            }
        },
        title = { ConfigDialogTitle(config) },
        minWidth = ConfigDialogDefaults.MinWidth,
        content = {
            EditDialogContent(
                modifier = Modifier.width(ConfigDialogDefaults.ContentWidth),
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
                EditDialogContentList(state = keyed, lazyListState = listState) { index, entry, _ ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                    ) {
                        Text(IGLang.ConfigWrapper.mapKey)
                        StringValueField(
                            value = entry.first,
                            onValueChange = { keyed.setValue(index, it to entry.second) },
                            modifier = Modifier.width(ConfigControlDefaults.ControlWidth / 2),
                        )
                        Text(IGLang.ConfigWrapper.mapValue)
                        ConfigElementEditor(
                            value = entry.second,
                            onValueChange = { keyed.setValue(index, entry.first to it) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
    )
}

/** 生成一个当前不冲突的默认键（`key1`、`key2`…）。 */
private fun uniqueKey(existing: List<Pair<String, Any>>): String {
    val used = existing.mapTo(mutableSetOf()) { it.first }
    var index = 1
    while ("key$index" in used) index++
    return "key$index"
}
