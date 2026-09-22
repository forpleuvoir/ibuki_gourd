package moe.forpleuvoir.ibukigourd.ui.editdialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons

/**
 * 拖拽手柄：一个标准 [IconButton] + [Icons.DragHandle] 图标。
 *
 * 不做自定义绘制：先前那版用 `graphicsLayer` 做 1.15× 缩放反馈，**非整数缩放会让像素图标发糊**
 * （看起来像被矢量放大的）；按下 / 悬停反馈直接用 `IconButton` 自己的四态精灵与内容色。
 *
 * 拖拽手势仍由调用方传入的 [modifier] 提供 —— `ReorderableListItemScope.draggableHandle()`
 * 只能在 [sh.calvin.reorderable.ReorderableItem] 作用域内取得；它与 `IconButton` 的点击
 * 手势可以共存（点击不成拖动、拖动不成点击）。
 *
 * 尺寸不在这里定：`IconButton` 实际尺寸 = `max(minSize, 图标尺寸 + 2 × contentPadding)`，
 * 图标倍率由 [iconScale] 决定、贴紧程度由 [contentPadding] 决定（取对称值才能保证正方形）。
 *
 * @param modifier 必须含拖拽手势修饰符（见上）
 * @param enabled 是否可交互
 * @param iconScale 图标倍率（素材 16×16 × 倍率 = 逻辑边长）
 * @param contentPadding 按钮内边距
 */
@Composable
fun DragHandle(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconScale: Int = 2,
    contentPadding: PaddingValues = IconButtonDefaults.contentPadding,
) {
    IconButton(
        onClick = {},
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
    ) {
        Icon(Icons.DragHandle, scale = iconScale)
    }
}
