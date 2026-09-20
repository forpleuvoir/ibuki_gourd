package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalContentColor
import moe.forpleuvoir.ibukigourd.ui.util.KeyedListState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 可拖拽排序的编辑行列表：与 [EditDialogContentHeader] 的三列布局对齐
 * （首列拖拽手柄 / 内容列 / 尾列删除），行内容由 [itemContent] 提供。
 *
 * 条目 key 由 [KeyedListState] 单调分配（`Keyed.key`），因此重排、增删时各行**组合状态跟数据走**、
 * 不会错位 —— 这也是 [moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList] 的用途。
 *
 * 拖拽手柄的反馈见 [DragHandle]：**按下即高亮**，且不使用 `Modifier.alpha`（会把被平移的行裁在原地）。
 *
 * @param state 条目容器（增删改一律经它，key 唯一性由它保证）
 * @param modifier 作用于列表
 * @param lazyListState 列表状态；与 `EditDialogContent` 共用同一个，浮动按钮的随滚动显隐才有依据
 * @param maxHeight 列表最大高度（外层弹窗高度由内容决定，这里必须给上界）
 * @param removeButton 尾列槽位，默认给一个删除图标按钮；传 null 则不占尾列
 * @param itemContent 内容列槽位：当前索引、条目值、是否正在被拖拽
 */
@Composable
fun <T> EditDialogContentList(
    state: KeyedListState<T>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    maxHeight: Dp = EditDialogContentDefaults.listMaxHeight,
    removeButton: (@Composable (index: Int) -> Unit)? = { index ->
        IconButton(
            onClick = { state.removeAt(index) },
            modifier = Modifier.padding(0.dp),
        ) {
            Icon(
                icon = Icons.Delete,
                tint = LocalContentColor.current,
            )
        }
    },
    itemContent: @Composable (index: Int, value: T, isDragging: Boolean) -> Unit,
) {
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        state.move(from.index, to.index)
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxWidth().heightIn(max = maxHeight),
        verticalArrangement = Arrangement.spacedBy(EditDialogContentDefaults.rowSpacing),
    ) {
        itemsIndexed(state.entries, key = { _, entry -> entry.key }) { index, entry ->
            ReorderableItem(state = reorderableState, key = entry.key) { isDragging ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.width(EditDialogContentDefaults.moveColumnWidth),
                        contentAlignment = Alignment.Center,
                    ) {
                        DragHandle(
                            isDragging = isDragging,
                            modifier = Modifier.draggableHandle(),
                        )
                    }

                    Spacer(Modifier.width(EditDialogContentDefaults.columnSpacing))

                    Box(Modifier.weight(1f)) {
                        itemContent(index, entry.value, isDragging)
                    }

                    if (removeButton != null) {
                        Spacer(Modifier.width(EditDialogContentDefaults.columnSpacing))
                        Box(
                            modifier = Modifier.width(EditDialogContentDefaults.removeColumnWidth),
                            contentAlignment = Alignment.Center,
                        ) {
                            removeButton(index)
                        }
                    }
                }
            }
        }
    }
}
