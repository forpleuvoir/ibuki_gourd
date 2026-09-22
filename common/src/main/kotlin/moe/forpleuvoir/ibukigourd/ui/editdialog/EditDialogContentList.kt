package moe.forpleuvoir.ibukigourd.ui.editdialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.LazyTableLayout
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TableLayoutScope
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalFlatScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.rememberScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.util.Keyed
import moe.forpleuvoir.ibukigourd.ui.util.KeyedListState

/**
 * 可拖拽排序的编辑表格：表体交给 [LazyTableLayout]（列对齐、行虚拟化与行拖拽都由它承担）。
 *
 * 拖拽手柄列与删除按钮列由本组件补在两端，[columns] 声明的是**中间的内容列**（可多列，
 * 如「键 / 值」）；所有列共用同一行表头，故列宽天然对齐。
 *
 * 行 key 用 [KeyedListState] 单调分配的 `Keyed.key`，因此重排、增删时各行**组合状态跟数据走**、
 * 不会错位 —— 这也是 [moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList] 的用途。
 * 行高统一为 [EditDialogContentDefaults.rowHeight]，与文本输入框的最小高度一致。
 *
 * 高度**填满外层可用高度**（上限 [maxHeight]）：拖拽时被拖的行要能在行间的空白区域里移动，
 * 只按内容高度定尺寸的话下方空白会被裁掉。因此外层必须给出**有界高度**。
 *
 * 右侧挂一条 flat 细条滚动条（[VerticalFlatScroller]）：它占自己的一列、不叠在内容上，也不占表头那行。
 *
 * 拖拽手柄的反馈见 [DragHandle]：**按下即高亮**，且不使用 `Modifier.alpha`（会把被平移的行裁在原地）。
 *
 * @param state 条目容器（增删改一律经它，key 唯一性由它保证）
 * @param modifier 作用于整张表
 * @param lazyListState 表格滚动状态；与 `EditDialogContent` 共用同一个，浮动按钮的随滚动显隐才有依据
 * @param maxHeight 表格高度上限，[Dp.Unspecified] 表示只受外层约束
 * @param removeButton 尾列槽位，默认给带二次确认的删除按钮；传 null 则不占尾列
 * @param columns 内容列声明（`column(...)`），列的表头一并写在这里
 */
@Composable
fun <T> EditDialogContentList(
    state: KeyedListState<T>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    maxHeight: Dp = Dp.Unspecified,
    removeButton: (@Composable (index: Int, value: T) -> Unit)? = { index, value ->
        RemoveConfirmButton(
            message = value.toString(),
            onConfirm = { state.removeAt(index) },
            modifier = Modifier.height(EditDialogContentDefaults.rowHeight),
            iconScale = LocalSokitsuPixelScale.current,
            contentPadding = EditDialogContentDefaults.iconPadding,
        )
    },
    columns: TableLayoutScope<Keyed<T>>.() -> Unit,
) {
    val iconScale = LocalSokitsuPixelScale.current

    LazyTableLayout<Keyed<T>>(
        modifier = modifier.fillMaxWidth().fillMaxHeight().heightIn(max = maxHeight),
        rowGap = EditDialogContentDefaults.rowSpacing,
        listState = lazyListState,
        rowModifier = { Modifier.height(EditDialogContentDefaults.rowHeight) },
        // 库默认 animateItemModifier = Modifier.animateItem()：新条目淡入 + 走位，看着像"添加有延迟"。
        // 这里只关掉**淡入**（添加即时出现），保留默认的 fadeOut（删除仍有淡出）与位移；拖拽位移由库自身承担
        rowAnimateItemModifier = { Modifier.animateItem(fadeInSpec = null) },
        listTrailing = {
            VerticalFlatScroller(
                adapter = rememberScrollerAdapter(lazyListState),
                modifier = Modifier.fillMaxHeight().padding(start = EditDialogContentDefaults.scrollbarGap),
                autoHide = true,
                autoFade = true,
            )
        },
        onRowMove = { from, to -> state.move(from, to) },
    ) {
        column(
            width = fixed(EditDialogContentDefaults.moveColumnWidth),
            header = { Text(IGLang.ConfigWrapper.move) },
        ) { _, _ ->
            DragHandle(
                modifier = Modifier.dragHandle(),
                iconScale = iconScale,
                contentPadding = EditDialogContentDefaults.iconPadding,
            )
        }

        columns()

        if (removeButton != null) {
            column(
                width = fixed(EditDialogContentDefaults.removeColumnWidth),
                header = { Text(IGLang.Misc.remove) },
            ) { index, entry ->
                removeButton(index, entry.value)
            }
        }

        rows(state.entries, key = { it.key })
    }
}
