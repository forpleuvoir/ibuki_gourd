package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/** 列表条目：[id] 作唯一 key（重排时条目组合状态跟数据走，不跟位置）。 */
private data class TestItem(val id: Int, val name: String)

/**
 * 拖拽手柄图标 + "拖拽中"视觉反馈。
 *
 * 反馈全部用 tint / 背景 / 同层 [graphicsLayer] 缩放实现，**绝不碰 [Modifier.alpha]**——
 * alpha 会套一层 clip=true 图层到被平移条目的外层，裁剪框固定、内容被裁在原地（见文件顶部说明）。
 *
 * 反馈分两层（核心：反馈在"**按下触发拖拽**"那一刻就有，不是等你开始推动才亮）：
 * - **常驻（可拖拽态，`[isDragging]` 为假、`pressed` 为假）**：手柄垫 `surfaceVariant` 圆角底 +
 *   染 `onSurfaceVariant` → 一眼看出"这是个能抓的手柄"（抓取感/可交互提示）；
 * - **按下即 armed（`pressed` 为真，还没开始动）**：立刻升级为 `primaryContainer` 底 +
 *   `onPrimaryContainer` 染色 + 轻微放大（1.15f）→ "抓起来了、可以拖了"的明确信号；
 * - **拖拽中（`[isDragging]` 为真）**：同上 armed 样式持续，平移跟随。
 *
 * `pressed` 由 [pressAware] 用 `pointerInput` 检测按下/抬起得到——库 `draggableHandle` 只发 drag
 * 交互（按下不亮），故这里独立补一个 press 态；不 consume 事件，与拖拽手势不冲突。
 * 缩放放在手柄自身的 [graphicsLayer] 里（与条目平移层无关），不受条目裁剪影响。
 *
 * @param handleModifier 必须含 [sh.calvin.reorderable.ReorderableListItemScope.draggableHandle]
 *   （由调用方在 ReorderableItem 作用域内传入），[pressAware] 在其上叠加按下检测。
 */
@Composable
private fun DragHandleIcon(isDragging: Boolean, handleModifier: Modifier) {
    val scheme = LocalColorScheme.current
    var pressed by remember { mutableStateOf(false) }
    // engaged = 按下即触发（可被拖拽），不必等推动 —— 反馈在"拖拽被 armed"那一刻就有
    val engaged = isDragging || pressed
    val scale by animateFloatAsState(if (engaged) 1.15f else 1f, label = "dragHandleScale")
    val bg = if (engaged) scheme.primaryContainer else scheme.surfaceVariant
    val tint = if (engaged) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    Box(
        modifier = handleModifier
            .pressAware { pressed = it }
            .background(bg, RoundedCornerShape(4.dp))
            .padding(2.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.DragHandle, tint = tint)
    }
}

/**
 * 按下/抬起检测：仅用于"可被拖拽"的反馈，基于 `detectTapGestures`（与 `draggable` 同包，
 * 和 [sh.calvin.reorderable.ReorderableListItemScope.draggableHandle] / `longPressDraggableHandle`
 * 的拖拽手势可共存）。`onPress` 在按下即触发、`tryAwaitRelease` 等到抬起/取消；回调
 * [onPressed]`(true/false)` 由调用方写进状态驱动反馈。
 */
private fun Modifier.pressAware(onPressed: (Boolean) -> Unit): Modifier = pointerInput(Unit) {
    detectTapGestures(
        onPress = {
            onPressed(true)
            tryAwaitRelease()
            onPressed(false)
        },
    )
}

/**
 * 长按才触发反馈（不抢 `longPressDraggableHandle` 的拖拽）：
 * 手动 `awaitPointerEventScope` 轮询，**不 consume 任何事件**——`longPressDraggableHandle`（同节点、外层
 * pointerInput）照常收到 move/up，拖拽能正常起。判定：down 记 `id`+`uptimeMillis`，其后每次事件算经过时间，
 * 超过 `viewConfiguration.longPressTimeoutMillis`（与 `longPressDraggableHandle` 同阈值）才把 armed 置真；
 * up 用 `changedToUpIgnoreConsumed`（避免被拖拽消费的事件蒙蔽）复位。普通点击/短按在阈值前抬起 → armed 始终
 * 为假、不亮。
 * 注：CMP 的 `PointerInputScope`/`PressGestureScope` 均非 `CoroutineScope`，且
 * `detectTapGestures(onLongPress)` 会在按下 consume 事件、抢走拖拽手势，故这里不依赖协程也不走 `onLongPress`，
 * 改用裸轮询（与 `draggable` 同原理的底层做法）。
 */
private fun Modifier.longPressAware(onPressed: (Boolean) -> Unit): Modifier = pointerInput(Unit) {
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
    awaitPointerEventScope {
        while (true) {
            val down = awaitPointerEvent().changes.firstOrNull { !it.previousPressed && it.pressed } ?: continue
            val downId = down.id
            val downTime = down.uptimeMillis
            var armed = false
            var ended = false
            while (!ended) {
                val change = awaitPointerEvent().changes.firstOrNull { it.id == downId } ?: continue
                if (change.previousPressed && !change.pressed) {
                    if (armed) onPressed(false)
                    ended = true
                } else if (!armed && change.uptimeMillis - downTime >= longPressTimeout) {
                    armed = true
                    onPressed(true)
                }
            }
        }
    }
}

/**
 * 可拖拽排序列表测试屏：验证内嵌 Reorderable 模块在 sokitsu 渲染体系下的基本可用性。
 *
 * 数据模型约定（缺一不可）：
 * - **不可变 List + 换位生成新实例** —— `ReorderableColumn` 的内部状态按 list 实例缓存，
 *   onSettle / 按钮换位必须 `list.toMutableList().apply { ... }` 产出新列表；
 *   原地改 SnapshotStateList 会让残留拖拽偏移作用到错位后的条目上；
 * - **唯一 key**（`key = { it.id }`）—— 重排后条目的 remember/状态跟数据走，不跟位置。
 *
 * 验证点：
 * 1. 手柄拖拽 —— 手柄常驻 `surfaceVariant` 圆角底表示"可拖拽"，**按下即**升级 `primaryContainer`
 *   底 + 放大（不必等推动）；松手后顺序经 onSettle 写回数据；
 * 2. 长按整行拖拽 —— longPressDraggableHandle 需长按延迟才进入拖拽，降低误触；
 * 3. 手柄与操作按钮共存 —— 同一行内拖拽手柄与上移/下移/删除按钮互不干扰；
 * 4. 顺序实时回显 —— 拖拽或按钮操作后下方文本即时反映当前排列；
 * 5. 虚拟列表（LazyColumn + 内嵌 Reorderable）—— 20 项只渲染可见部分、可滚动，手柄反馈与上面一致；
 *   验证 Reorderable 在 compose 自有 Lazy 列表下的虚拟列表路径（对应 26.1.2 EditDialogContentList）。
 *
 * 注意：不要给条目挂 `Modifier.alpha(<1f)` 做拖拽反馈 —— alpha 会创建 clip=true 的
 * 图层套在 translationY 层外，裁剪框固定在原始 bounds，拖拽内容会被裁在原地。
 * 拖拽反馈分两层（反馈在"**按下触发拖拽**"那一刻就有，不是等推动才亮）：手柄常驻 `surfaceVariant`
 * 圆角底 + `onSurfaceVariant`（表示可拖拽）；**按下即**升级 `primaryContainer` + `onPrimaryContainer`
 * + 同层 `graphicsLayer` 缩放（见 [DragHandleIcon] / [pressAware]，engaged = isDragging || pressed）；
 * 整行长按拖拽：rest `surfaceVariant` 底；**长按 armed 才**升级 `primaryContainer` 底
 * （用 [longPressAware]，普通点击/短按不亮——与 `longPressDraggableHandle` 触发时机一致）。
 */
@Composable
fun ReorderableTestContent() {
    // 内容比一屏高 → 整体可滚动，否则被 CenterBox 居中裁掉下半（下面的 Lazy 段看不到）
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // 1. 手柄拖拽
        Text("1. 手柄拖拽（按住 ≡ 拖动）")
        var handleItems by remember { mutableStateOf(List(6) { i -> TestItem(i + 1, "项 ${i + 1}") }) }
        ReorderableColumn(
            list = handleItems,
            onSettle = { from, to -> handleItems = handleItems.move(from, to) },
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            key = { it.id },
        ) { index, item, isDragging ->
            ReorderableItem(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DragHandleIcon(isDragging, Modifier.draggableHandle())
                    Text(item.name)
                }
            }
        }
        Text("当前顺序：${handleItems.joinToString { it.name }}")

        // 2. 长按整行拖拽
        Text("2. 长按整行拖拽（需长按）")
        var longPressItems by remember {
            mutableStateOf(List(5) { i -> TestItem(100 + i, "长按项 ${i + 1}") })
        }
        ReorderableColumn(
            list = longPressItems,
            onSettle = { from, to -> longPressItems = longPressItems.move(from, to) },
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            key = { it.id },
        ) { index, item, isDragging ->
            val scheme = LocalColorScheme.current
            var pressed by remember { mutableStateOf(false) }
            // engaged = 长按触发（拖拽 armed）才亮，普通点击/短按不亮；拖动中 isDragging 续亮
            val engaged = isDragging || pressed
            ReorderableItem(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .longPressDraggableHandle()
                        .longPressAware { pressed = it }
                        .background(
                            if (engaged) scheme.primaryContainer else scheme.surfaceVariant,
                            RoundedCornerShape(4.dp),
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        item.name,
                        color = if (engaged) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                    )
                }
            }
        }

        // 3. 手柄 + 操作按钮
        var lastAction by remember { mutableStateOf("（还没操作）") }
        Text("最近操作：$lastAction")
        var actionItems by remember {
            mutableStateOf(List(5) { i -> TestItem(200 + i, "可操作项 ${i + 1}") })
        }
        ReorderableColumn(
            list = actionItems,
            onSettle = { from, to -> actionItems = actionItems.move(from, to) },
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            key = { it.id },
        ) { index, item, isDragging ->
            ReorderableItem(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DragHandleIcon(isDragging, Modifier.draggableHandle())
                    Text(item.name)
                    IconButton(
                        onClick = {
                            actionItems = actionItems.move(index, index - 1)
                            lastAction = "上移：${item.name}"
                        },
                        enabled = index > 0,
                    ) { Icon(Icons.ArrowUp) }
                    IconButton(
                        onClick = {
                            actionItems = actionItems.move(index, index + 1)
                            lastAction = "下移：${item.name}"
                        },
                        enabled = index < actionItems.lastIndex,
                    ) { Icon(Icons.ArrowDown) }
                    // 删除仅提示：不从列表移除，避免 ReorderableListState 的 itemIntervals 越界
                    IconButton(onClick = { lastAction = "删除：${item.name}（仅提示）" }) {
                        Icon(Icons.Delete)
                    }
                }
            }
        }

        // 4. Compose 自己的 LazyColumn + 内嵌 Reorderable（虚拟列表路径，对应 26.1.2 的 EditDialogContentList）
        Text("4. LazyColumn + 拖拽（虚拟列表，可滚动）")
        val lazyItems = remember {
            mutableStateListOf(*List(20) { i -> TestItem(300 + i, "懒加载项 ${i + 1}") }.toTypedArray())
        }
        val lazyListState = rememberLazyListState()
        val lazyReorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
            // Lazy 版状态按 lazyListState 缓存（不按 list 实例），原地改 SnapshotStateList 即可
            lazyItems.add(to.index, lazyItems.removeAt(from.index))
        }
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth().height(220.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            itemsIndexed(lazyItems, key = { _, e -> e.id }) { index, item ->
                ReorderableItem(state = lazyReorderState, key = item.id) { isDragging ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DragHandleIcon(isDragging, Modifier.draggableHandle())
                        Text(item.name)
                    }
                }
            }
        }
        Text("当前顺序：${lazyItems.joinToString { it.name }}")
    }
}

/** 把 [from] 处的元素移动到 [to]，返回新列表（原列表不变）。 */
private fun <T> List<T>.move(from: Int, to: Int): List<T> =
    toMutableList().apply { add(to, removeAt(from)) }

fun ReorderableTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            ReorderableTestContent()
        }
    }
}
