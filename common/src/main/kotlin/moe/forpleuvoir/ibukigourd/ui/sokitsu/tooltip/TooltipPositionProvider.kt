package moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import kotlin.math.roundToInt

/**
 * 把气泡弹层锚定到目标组件（对标 compose-minecraft 的 `AnchorBoundsPositionProvider`，
 * 但偏好方向用本库的 [AnchorPosition]（Above/Below/Left/Right 字面向左/右，不做 RTL 转换））。
 *
 * 沿 [position] 放置，气泡与目标在对应边中**居中对齐**；该方向空间不足时自动翻转
 * （上↔下、左↔右），再不足则转向正交方向，最后用 `coerceIn` 夹取避免溢出屏幕。
 *
 * 锚点捕获**不新增布局节点**：业务在锚组件自己的 modifier 链上用
 * `Modifier.onGloballyPositioned { anchorBounds = it.boundsInRoot() }` 记录，
 * 再以 `{ anchorBounds }` 惰性闭包传入本定位器。
 *
 * 每次 [calculatePosition] 会把结果回写到 [resolvedPosition]（实际落点，可能与偏好不同）
 * 与 [arrowRatio]（箭头沿对应边的落位），供渲染层画箭头 / 决定伸出方向；两者均为可变状态，
 * 故本类**不是** `@Immutable`。
 *
 * @param anchorBounds 锚组件在 root 坐标系中的 bounds（from `boundsInRoot`），惰性读取。
 * @param position 气泡偏好位置，空间不足自动翻转。
 * @param density 当前密度（组合内取 `LocalDensity.current`）：[spacing] 的 dp→px 换算依赖它，
 *   `calculatePosition` 拿不到密度，故必须在构造时传入；密度变化时须重建本对象。
 * @param spacing 弹层与锚组件之间的间距（同时充当与屏幕边缘的安全间距）。
 */
class TooltipPositionProvider(
    private val anchorBounds: () -> Rect,
    private val position: AnchorPosition,
    private val density: Density,
    private val spacing: Dp,
) : PopupPositionProvider {

    /**
     * 气泡实际落点方向：初始为偏好 [position]，空间不足时被翻转成对侧或正交侧。
     * 渲染层据此决定箭头贴哪条边、气泡往哪边为箭头留伸出空间。
     */
    var resolvedPosition: AnchorPosition by mutableStateOf(position)
        private set

    /**
     * 箭头沿气泡对应边的相对位置（0 = 起端，1 = 终端），由**目标中心**在该边上的投影得出
     * —— 气泡被屏幕边缘夹取而偏离目标时，箭头仍指向目标，而非气泡自身中心。
     * 实际可用范围由渲染层按气泡体边框再收窄。
     */
    var arrowRatio: Float by mutableStateOf(0.5f)
        private set

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val gap = with(density) { spacing.roundToPx() }
        // 首帧尚未记录 bounds 时退回调用方传入的窗口锚点（几乎不会发生）
        val bounds = this@TooltipPositionProvider.anchorBounds().roundToIntRect()
        val effective = if (bounds.width == 0 && bounds.height == 0) anchorBounds else bounds
        val tooltipW = popupContentSize.width.coerceAtLeast(0)
        val tooltipH = popupContentSize.height.coerceAtLeast(0)
        val rootW = windowSize.width
        val rootH = windowSize.height

        val spaceAbove = effective.top - gap
        val spaceBelow = rootH - effective.bottom - gap
        val spaceLeft = effective.left - gap
        val spaceRight = rootW - effective.right - gap

        fun pickVertical(pref: AnchorPosition, opp: AnchorPosition): AnchorPosition = when {
            (if (pref == AnchorPosition.Above) spaceAbove else spaceBelow) >= tooltipH -> pref
            (if (opp == AnchorPosition.Above) spaceAbove else spaceBelow) >= tooltipH -> opp
            spaceLeft >= tooltipW -> AnchorPosition.Left
            spaceRight >= tooltipW -> AnchorPosition.Right
            else -> if (spaceLeft >= spaceRight) AnchorPosition.Left else AnchorPosition.Right
        }

        fun pickHorizontal(pref: AnchorPosition, opp: AnchorPosition): AnchorPosition = when {
            (if (pref == AnchorPosition.Left) spaceLeft else spaceRight) >= tooltipW -> pref
            (if (opp == AnchorPosition.Left) spaceLeft else spaceRight) >= tooltipW -> opp
            spaceAbove >= tooltipH -> AnchorPosition.Above
            spaceBelow >= tooltipH -> AnchorPosition.Below
            else -> if (spaceAbove >= spaceBelow) AnchorPosition.Above else AnchorPosition.Below
        }

        val resolved = when (position) {
            AnchorPosition.Above -> pickVertical(AnchorPosition.Above, AnchorPosition.Below)
            AnchorPosition.Below -> pickVertical(AnchorPosition.Below, AnchorPosition.Above)
            AnchorPosition.Left -> pickHorizontal(AnchorPosition.Left, AnchorPosition.Right)
            AnchorPosition.Right -> pickHorizontal(AnchorPosition.Right, AnchorPosition.Left)
        }

        val xRange = gap..(rootW - tooltipW - gap).coerceAtLeast(gap)
        val yRange = gap..(rootH - tooltipH - gap).coerceAtLeast(gap)

        val offset = when (resolved) {
            AnchorPosition.Above -> {
                val y = (effective.top - tooltipH - gap).coerceIn(yRange)
                val x = (effective.left + (effective.width - tooltipW) / 2).coerceIn(xRange)
                IntOffset(x, y)
            }
            AnchorPosition.Below -> {
                val y = (effective.bottom + gap).coerceIn(yRange)
                val x = (effective.left + (effective.width - tooltipW) / 2).coerceIn(xRange)
                IntOffset(x, y)
            }
            AnchorPosition.Left -> {
                val x = (effective.left - tooltipW - gap).coerceIn(xRange)
                val y = (effective.top + (effective.height - tooltipH) / 2).coerceIn(yRange)
                IntOffset(x, y)
            }
            AnchorPosition.Right -> {
                val x = (effective.right + gap).coerceIn(xRange)
                val y = (effective.top + (effective.height - tooltipH) / 2).coerceIn(yRange)
                IntOffset(x, y)
            }
        }

        // 回写落点与箭头位置：箭头贴 resolved 的反向边，滑动方向与留白方向正交，
        // 故用弹层整体尺寸即可（该维度不含箭头伸出留白）。
        resolvedPosition = resolved
        val centerX = effective.left + effective.width / 2f
        val centerY = effective.top + effective.height / 2f
        arrowRatio = when (resolved) {
            AnchorPosition.Above, AnchorPosition.Below ->
                if (tooltipW > 0) (centerX - offset.x) / tooltipW else 0.5f
            AnchorPosition.Left, AnchorPosition.Right ->
                if (tooltipH > 0) (centerY - offset.y) / tooltipH else 0.5f
        }
        return offset
    }
}

private fun Rect.roundToIntRect(): IntRect = IntRect(
    left.roundToInt(),
    top.roundToInt(),
    right.roundToInt(),
    bottom.roundToInt(),
)
