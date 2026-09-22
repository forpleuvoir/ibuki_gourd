package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.editdialog.DragHandle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.LazyTableLayout
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TableLayout
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalFlatScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.logicalSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.rememberScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.util.Keyed
import moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList

/** 本屏对应的表格修订标记：改完刷新后应能在屏上看到同一串，便于确认跑的是最新代码。 */
private const val TableLayoutRev = "rev 2026-09-22 lazy 拖拽无 O(行数) 工作"

/**
 * eager 表的行数上限。
 *
 * eager 内核会把**所有**行都组合出来：任何一次数据变化（重排、删除）都要重组并重排整张表，
 * 500 行会明显掉帧。它在这里只保留小数据量，验证排版与滚动；大量数据走 [moe.forpleuvoir.ibukigourd.ui.sokitsu.LazyTableLayout]。
 */
private const val EagerRowLimit = 40

/** 表格行数据：名称长度参差（含一档超长），用于验证 `weight` 列的省略与列对齐。 */
private data class TableRowValue(val name: String, val value: String)

/**
 * 表格测试屏：eager / lazy / 无表头三张表并排，**三张表各持自己的数据**。
 *
 * 三张表各持自己的数据，行数控制只作用于 lazy 表：eager 内核会把**所有**行都组合出来、数据一变就
 * 整表重组并重排，几百行会明显掉帧，因此它在本屏固定最多 [EagerRowLimit] 行 —— 大量数据用 lazy。
 * 两者也不共用数据：共用时拖拽/删除会连带触发 eager 表的整表重组，把要测的 lazy 路径盖住。
 *
 * 其余验证点：
 * 1. 两种内核的列宽（`fixed` / `weight`）与列对齐一致，超长名称按列宽省略且不影响其它列；
 * 2. 表头两种模式：固定（eager 自持表体滚动；lazy 走 `stickyHeader`）与随内容滚动；
 * 3. eager 在"表头不固定"时不自持滚动 —— 滚动由调用方挂在 `modifier` 上（本屏即如此）；
 * 4. `spanItem` 整行不占列、在两种内核里都占满表宽；
 * 5. lazy 版行拖拽：手柄在首个列，拖拽后调用方的容器按回调序号重排（逻辑行序号跳过表头与 `spanItem`）；
 * 6. 无表头的表：整表没有表头行，也不自持滚动。
 */
fun TableLayoutTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            var fixedHeader by remember { mutableStateOf(true) }
            var rowCount by remember { mutableIntStateOf(40) }
            val lazyRows = rememberKeyedList(remember(rowCount) { tableTestRows(rowCount) })
            val eagerCount = minOf(rowCount, EagerRowLimit)
            val eagerRows = rememberKeyedList(remember(eagerCount) { tableTestRows(eagerCount) })
            val plainRows = rememberKeyedList(remember { tableTestRows(6) })
            val eagerScroll = rememberScrollState()
            val lazyListState = rememberLazyListState()
            // 图标按钮所在列的宽度必须容得下按钮的自然尺寸，否则图标被列宽压扁
            val iconScale = LocalSokitsuPixelScale.current
            val dragColumnWidth = tableIconColumnWidth(Icons.DragHandle, iconScale)
            val removeColumnWidth = tableIconColumnWidth(Icons.Close, iconScale)

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(TableLayoutRev)
                Button({ fixedHeader = !fixedHeader }) {
                    Text("表头：${if (fixedHeader) "固定" else "随内容"}")
                }
                Text("lazy 行数：")
                listOf(8, 40, 500).forEach { size ->
                    Button({ rowCount = size }) { Text("$size") }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── eager：表头固定时自持表体滚动；不固定时滚动交给调用方 ──
                TableDemoColumn("TableLayout（eager，${eagerRows.size} 行）", Modifier.weight(1.2f)) {
                    Box(Modifier.fillMaxWidth().weight(1f)) {
                        TableLayout<Keyed<TableRowValue>>(
                            modifier = if (fixedHeader) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier.fillMaxWidth().verticalScroll(eagerScroll)
                            },
                            fixedHeader = fixedHeader,
                            rowGap = 2.dp,
                            columnGap = 12.dp,
                            scrollState = eagerScroll,
                            rowModifier = { tableRowHover() },
                        ) {
                            column(width = fixed(32.dp), header = { Text("#") }) { index, _ ->
                                Text("${index + 1}")
                            }
                            column(
                                width = weight(1f),
                                alignment = Alignment.CenterStart,
                                header = { Text("名称") },
                            ) { _, entry ->
                                Text(entry.value.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            column(width = fixed(120.dp), header = { Text("值") }) { _, entry ->
                                Text(entry.value.value, maxLines = 1)
                            }
                            column(width = fixed(removeColumnWidth)) { index, _ ->
                                RemoveRowButton(iconScale) { eagerRows.removeAt(index) }
                            }

                            spanItem { TableGroupTitle("分组：常规（spanItem）") }
                            rows(eagerRows.entries, key = { it.key })
                        }
                        VerticalFlatScroller(
                            adapter = rememberScrollerAdapter(eagerScroll),
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            autoHide = true,
                            autoFade = true,
                        )
                    }
                }

                // ── lazy：行虚拟化 + 行拖拽（手柄在首个列） ──
                TableDemoColumn("LazyTableLayout（lazy + 拖拽，${lazyRows.size} 行）", Modifier.weight(1.2f)) {
                    Box(Modifier.fillMaxWidth().weight(1f)) {
                        LazyTableLayout<Keyed<TableRowValue>>(
                            modifier = Modifier.fillMaxSize(),
                            fixedHeader = fixedHeader,
                            rowGap = 2.dp,
                            columnGap = 12.dp,
                            listState = lazyListState,
                            rowModifier = { tableRowHover() },
                            onRowMove = { from, to -> lazyRows.move(from, to) },
                        ) {
                            column(width = fixed(dragColumnWidth)) { _, _ ->
                                DragHandle(
                                    modifier = Modifier.dragHandle(),
                                    iconScale = iconScale,
                                )
                            }
                            column(
                                width = weight(1f),
                                alignment = Alignment.CenterStart,
                                header = { Text("名称") },
                            ) { _, entry ->
                                Text(entry.value.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            column(width = fixed(120.dp), header = { Text("值") }) { _, entry ->
                                Text(entry.value.value, maxLines = 1)
                            }
                            column(width = fixed(removeColumnWidth)) { index, _ ->
                                RemoveRowButton(iconScale) { lazyRows.removeAt(index) }
                            }

                            spanItem { TableGroupTitle("分组：常规（spanItem）") }
                            rows(lazyRows.entries, key = { it.key })
                        }
                        VerticalFlatScroller(
                            adapter = rememberScrollerAdapter(lazyListState),
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            autoHide = true,
                            autoFade = true,
                        )
                    }
                }

                // ── 无表头：整表没有表头行，也不自持滚动 ──
                TableDemoColumn("无表头（${plainRows.size} 行）", Modifier.weight(0.8f)) {
                    TableLayout<Keyed<TableRowValue>>(
                        modifier = Modifier.fillMaxWidth(),
                        rowGap = 2.dp,
                        columnGap = 12.dp,
                        rowModifier = { tableRowHover() },
                    ) {
                        column(width = weight(1f), alignment = Alignment.CenterStart) { _, entry ->
                            Text(entry.value.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        column(width = fixed(80.dp)) { _, entry ->
                            Text(entry.value.value, maxLines = 1)
                        }
                        rows(plainRows.entries)
                    }
                }
            }
        }
    }
}

/** 一列演示：标题 + 内容区（内容区吃剩余高度）。 */
@Composable
private fun TableDemoColumn(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxHeight()) {
        Text(title)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

/** 分组标题行：验证 `spanItem` 占满表宽、不参与列宽分配。 */
@Composable
private fun TableGroupTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
        color = LocalColorScheme.current.primary,
    )
}

/**
 * 图标按钮所在列的宽度：与 `IconButton` 的尺寸关系一致
 * （`max(minSize, 图标尺寸 + 左右内边距)`）。
 *
 * 列宽不足时按钮（连同图标）会被 `Fixed` 列宽压扁 —— 表格里的 `weight` / `fixed` 都是硬约束，
 * 不会为内容让位，所以图标列的宽度必须按按钮的自然尺寸给。
 */
@Composable
private fun tableIconColumnWidth(icon: SokitsuSprite, scale: Int = LocalSokitsuPixelScale.current): Dp {
    val padding = IconButtonDefaults.contentPadding
    val horizontal = padding.calculateLeftPadding(LayoutDirection.Ltr) +
            padding.calculateRightPadding(LayoutDirection.Ltr)
    return maxOf(IconButtonDefaults.minSize.width, icon.logicalSize.width * scale + horizontal)
}

/**
 * 行尾删除按钮：按当前行下标移除，验证 `cell` 的 `index` 与数据下标一致。
 *
 * 图标倍率由调用方给（本屏按像素缩放联动），列宽用同一个倍率算，两者必须一致才不会被压扁。
 */
@Composable
private fun RemoveRowButton(iconScale: Int, onRemove: () -> Unit) {
    IconButton(onClick = onRemove) {
        Icon(Icons.Close, scale = iconScale)
    }
}

/**
 * 行悬停底色：`rowModifier` 是 composable 函数类型，因此可以在这里读悬停状态与动画。
 *
 * 只对**画在行自身边界内**的矩形做透明度动画，不使用 `Modifier.alpha` —— 后者会给行套一层
 * 裁剪图层，拖拽中被平移的行会被裁在原地。
 */
@Composable
private fun tableRowHover(): Modifier {
    val scheme = LocalColorScheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val alpha by animateFloatAsState(if (hovered) 1f else 0f, label = "tableRowHoverAlpha")
    return Modifier
        .hoverable(interactionSource)
        .drawBehind {
            if (alpha > 0f) drawRect(scheme.surfaceVariant.copy(alpha = 0.35f * alpha))
        }
}

/** 测试数据：名称长度参差，含一档超长用于验证 `weight` 列的省略。 */
private fun tableTestRows(count: Int): List<TableRowValue> = (0 until count).map { index ->
    TableRowValue(
        name = when (index % 4) {
            0    -> "常规设置 ${index + 1}"
            1    -> "渲染选项 ${index + 1}"
            2    -> "一个相当长的配置项名称 ${index + 1}"
            else -> "S${index + 1}"
        },
        value = "值 ${index + 1}",
    )
}
