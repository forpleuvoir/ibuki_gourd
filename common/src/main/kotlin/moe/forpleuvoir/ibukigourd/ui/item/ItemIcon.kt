package moe.forpleuvoir.ibukigourd.ui.item

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.forpleuvoir.compose_minecraft.platform.ui.draw.minecraftItem
import moe.forpleuvoir.compose_minecraft.platform.ui.tooltip.TooltipLines
import moe.forpleuvoir.compose_minecraft.platform.ui.tooltip.minecraftTooltip
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.world.item.ItemStack

/**
 * 物品图标：把 [ItemStack] 按节点布局尺寸绘制到 Compose 场景中。
 *
 * 物品本体由 [Modifier.minecraftItem] 绘制（原生物品模型 + 原版 GUI display context），
 * 因此与 Compose 内容共享绘制顺序与裁剪，可正常参与 Popup / Dialog 图层；
 * 悬停放大经 `graphicsLayer` 施加，尺寸变化由绘制节点的画布矩阵承担。
 *
 * 叠层内容：
 * - 数量文本（[showCount]）为 Compose 文本，按 [countStyle] 对齐到右下角，堆叠数为 1 时不显示；
 * - 悬停提示（[showTooltip]）为原版物品 tooltip（[TooltipLines.fromItem]），
 *   与 [scaleOnHover] 共用同一交互源。
 *
 * @param stack 要渲染的物品堆叠，空堆叠不绘制
 * @param modifier 应用到图标容器的 Modifier（尺寸取 [size]，内边距由调用方在外层处理）
 * @param size 图标布局尺寸，等于物品绘制尺寸（`1dp == 1` 窗口像素）
 * @param showTooltip 悬停时是否显示原版物品 tooltip
 * @param tooltipGuiScale 悬停提示是否启用原版 GUI 缩放（`true` 随原版 `guiScale` 放大，
 *   `false` 与 Compose 场景 1:1 像素）
 * @param showCount 是否在右下角显示堆叠数量（仅堆叠数大于 1 时显示）
 * @param countStyle 数量文本样式，默认取 [ItemIconDefaults.countStyle]
 * @param scaleOnHover 悬停时的放大倍数，`1f` 为不放大
 * @param color 物品着色，[Color.White] 为不调制
 * @param seed 物品模型随机种子（同一物品取不同随机外观）
 */
@Composable
fun ItemIcon(
    stack: ItemStack,
    modifier: Modifier = Modifier,
    size: DpSize = ItemIconDefaults.size,
    showTooltip: Boolean = true,
    tooltipGuiScale: Boolean = true,
    showCount: Boolean = false,
    countStyle: TextStyle = ItemIconDefaults.countStyle(size),
    scaleOnHover: Float = 1f,
    color: Color = Color.White,
    seed: Int = 0,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (hovered) scaleOnHover else 1f,
        animationSpec = tween(150),
        label = "itemIconScale",
    )
    val tooltip = remember(stack) { TooltipLines.fromItem(stack) }

    Box(
        modifier = modifier
            .size(size)
            .hoverable(interactionSource)
            .then(
                if (showTooltip) Modifier.minecraftTooltip(
                    lines = tooltip,
                    guiScaleEnabled = tooltipGuiScale,
                    interactionSource = interactionSource,
                ) else Modifier
            ),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .then(
                    if (scaleOnHover == 1f) Modifier
                    else Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                )
                .minecraftItem(stack, color = color, level = mc.level, player = mc.player, seed = seed)
        )

        if (showCount && stack.count > 1) {
            BasicText(
                text = stack.count.toString(),
                style = countStyle,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(ItemIconDefaults.countPadding),
            )
        }
    }
}

/** [ItemIcon] 的默认参数。 */
object ItemIconDefaults {

    /** 图标默认布局尺寸（= 物品绘制尺寸），取 16 的整数倍。 */
    val size: DpSize = DpSize(48.dp, 48.dp)

    /** 数量文本与图标右下角的间距。 */
    val countPadding: PaddingValues = PaddingValues(end = 1.dp, bottom = 1.dp)

    /** 数量文本样式：白色 + 半透明黑色投影，字号取图标高度的一半。 */
    fun countStyle(size: DpSize): TextStyle = TextStyle(
        color = Color.White,
        fontSize = (size.height.value * 0.5f).sp,
        shadow = Shadow(
            color = Color.Black.copy(alpha = 0.6f),
            offset = Offset(1f, 1f),
        ),
    )
}
