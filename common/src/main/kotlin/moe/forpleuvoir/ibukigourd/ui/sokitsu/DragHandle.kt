package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme

/**
 * 拖拽手柄：可抓取的方形手柄，按下（拖拽被 armed）即高亮放大。
 *
 * 反馈只有两层：常态 `surfaceVariant` 底 + `onSurfaceVariant` 图标（表明"可以抓"）；
 * 按下或拖拽中升级为 `primaryContainer` 底 + `onPrimaryContainer` 图标 + 同层 `graphicsLayer` 缩放。
 *
 * **不使用 `Modifier.alpha`**：alpha 会给条目套一层 `clip = true` 的图层，裁剪框固定在原始
 * bounds，被拖拽平移的条目会被裁在原地。
 *
 * 手柄的拖拽手势由调用方传入的 [modifier] 提供
 * （`ReorderableListItemScope.draggableHandle()` 只能在 [sh.calvin.reorderable.ReorderableItem]
 * 作用域内取得）；本组件只负责外观与按下反馈。
 *
 * @param isDragging 是否正在被拖拽（由 `ReorderableItem` 的内容槽给出）
 * @param modifier 必须含拖拽手势修饰符（见上）
 */
@Composable
fun DragHandle(isDragging: Boolean, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val engaged = isDragging || pressed
    val scale by animateFloatAsState(if (engaged) 1.15f else 1f, label = "dragHandleScale")
    val scheme = LocalColorScheme.current

    Box(
        modifier = modifier
            .pressAware { pressed = it }
            .background(if (engaged) scheme.primaryContainer else scheme.surfaceVariant)
            .padding(2.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon = Icons.DragHandle,
            tint = if (engaged) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        )
    }
}

/**
 * 按下 / 抬起检测：只用于"可被拖拽"的反馈。
 *
 * 走 `detectTapGestures(onPress)`（与 `draggableHandle` 的 `draggable` 同包、可共存），
 * **不 consume 事件**，因此不会抢走同节点上的拖拽手势。
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
