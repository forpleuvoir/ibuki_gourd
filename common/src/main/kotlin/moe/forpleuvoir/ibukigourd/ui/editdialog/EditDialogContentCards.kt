package moe.forpleuvoir.ibukigourd.ui.editdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalFlatScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.rememberScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.util.KeyedListState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

/**
 * 可拖拽排序的编辑卡片列表：一个条目一张卡片，卡片按 [columns] 列排布。
 *
 * 卡片头部一行**两端**分别是拖拽手柄与尾部操作组（[actions] 与 [removeButton] 同处一组，
 * 组内按顺序排列、不受头部的 [Arrangement.SpaceBetween] 影响），下面是 [content] 给的卡片体；
 * 卡片用 [Surface] 承载，卡片之间按 [cardSpacing] 留缝。
 *
 * 与 [EditDialogContentList] 的分工：那个是**列对齐的表格**（键 / 值、前项 / 后项这类多列同行），
 * 这个适合"每条目自己一坨编辑界面"的情形（如缓动曲线卡片）—— 两者都不必自己接拖拽。
 *
 * 高度**填满外层可用高度**（上限 [maxHeight]）：可视区里能排下多少张就排多少张，不留空档；
 * 右侧挂一条 flat 细条滚动条（[VerticalFlatScroller]），它占自己的一列、不叠在卡片上。
 *
 * 条目 key 用 [KeyedListState] 的 `Keyed.key`，重排、增删时卡片内的组合状态跟数据走。
 *
 * @param state 条目容器（增删改一律经它，key 唯一性由它保证）
 * @param modifier 作用于整个列表区
 * @param lazyGridState 网格滚动状态
 * @param columns 列数（一行放几张卡片）
 * @param maxHeight 列表高度上限，[Dp.Unspecified] 表示只受外层约束
 * @param cardPadding 卡片内容内边距
 * @param cardSpacing 卡片间距（横竖相同）
 * @param actions 卡片头部尾端操作组里的额外操作（如"编辑"），排在 [removeButton] 前面；null 时不占位
 * @param removeButton 卡片头部尾端的删除槽位；传 null 时不占位
 * @param content 卡片体（头部下方）；`onValueChange` 写回副本
 */
@Composable
fun <T> EditDialogContentCards(
    state: KeyedListState<T>,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    columns: Int = 1,
    maxHeight: Dp = Dp.Unspecified,
    cardPadding: PaddingValues = EditDialogContentDefaults.cardPadding,
    cardSpacing: Dp = EditDialogContentDefaults.cardSpacing,
    actions: (@Composable (index: Int, value: T) -> Unit)? = null,
    removeButton: (@Composable (index: Int, value: T) -> Unit)? = { index, value ->
        RemoveConfirmButton(
            message = value.toString(),
            onConfirm = { state.removeAt(index) },
            iconScale = LocalSokitsuPixelScale.current,
            contentPadding = EditDialogContentDefaults.iconPadding,
        )
    },
    content: @Composable (index: Int, value: T, onValueChange: (T) -> Unit) -> Unit,
) {
    val iconScale = LocalSokitsuPixelScale.current
    val reorderableState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
        state.move(from.index, to.index)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .fillMaxHeight(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns.coerceAtLeast(1)),
            state = lazyGridState,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(cardSpacing),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing),
        ) {
            itemsIndexed(state.entries, key = { _, entry -> entry.key }) { index, entry ->
                // 库默认 animateItemModifier = Modifier.animateItem()：新条目淡入 + 走位，看着像"添加有延迟"。
                // 这里只关掉**淡入**（添加即时出现），保留默认的 fadeOut（删除仍有淡出）与位移；拖拽位移由库自身承担
                ReorderableItem(
                    state = reorderableState,
                    key = entry.key,
                    animateItemModifier = Modifier.animateItem(fadeInSpec = null),
                ) {
                    Surface(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(cardPadding)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                DragHandle(
                                    modifier = Modifier.draggableHandle(),
                                    iconScale = iconScale,
                                    contentPadding = EditDialogContentDefaults.iconPadding,
                                )
                                // 尾部操作是**一组**：组内顺序由调用方定，不被外层的 SpaceBetween 拆开
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    actions?.invoke(index, entry.value)
                                    removeButton?.invoke(index, entry.value)
                                }
                            }

                            Spacer(Modifier.height(EditDialogContentDefaults.cardHeaderGap))

                            content(index, entry.value) { state.setValue(index, it) }
                        }
                    }
                }
            }
        }

        VerticalFlatScroller(
            adapter = rememberScrollerAdapter(lazyGridState, columns),
            modifier = Modifier.fillMaxHeight().padding(start = EditDialogContentDefaults.scrollbarGap),
            autoHide = true,
            autoFade = true,
        )
    }
}
