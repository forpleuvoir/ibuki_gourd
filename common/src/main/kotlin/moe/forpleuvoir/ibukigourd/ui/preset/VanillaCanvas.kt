package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.ui.skia.LocalSkiaSurface
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.GuiGraphicsExtractor

@Composable
fun VanillaCanvas(
    modifier: Modifier,
    key: Any = Unit,
    onDraw: GuiGraphicsExtractor.(VanillaCanvasDrawContext) -> Unit
) {
    val surface = LocalSkiaSurface.current
    var rect by remember { mutableStateOf<Rect?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    var alpha by remember { mutableFloatStateOf(1f) }
    var scaleFactor by remember { mutableStateOf(ScaleFactor(1f, 1f)) }

    LaunchedEffect(key) {
        while (isActive) {
            withFrameNanos { delta ->
                surface.postRender {
                    this.onDraw(VanillaCanvasDrawContext(rect, hovered, alpha, scaleFactor, delta))
                }
            }
        }
    }
    Spacer(
        modifier = modifier
            .hoverable(interactionSource)
            .graphicsLayer {
                alpha = this.alpha
                scaleFactor = ScaleFactor(scaleX, scaleY)
            }
            .onGloballyPositioned { rect = it.toVanillaGuiRect }
    )
}

/**
 * @param area 组件在原版GUI中的区域,为空则不存在或者组件已经被移出布局树
 * @param hovered 鼠标是否悬浮在组件上
 * @param alpha 组件的不透明度: 0 -> 完全透明, 1 -> 完全不透明
 * @param scaleFactor 组件的缩放因子
 * @param delta 距离上一帧经过的时间(纳秒)
 */
class VanillaCanvasDrawContext(
    val area: Rect?,
    val hovered: Boolean,
    val alpha: Float,
    val scaleFactor: ScaleFactor,
    val delta: Long,
)

val LayoutCoordinates?.toVanillaGuiRect: Rect?
    get() {
        return if (this != null && isAttached) {
            val density = 1f / mc.window.guiScale.toFloat()
            val width = size.width * density
            val height = size.height * density
            val x = positionInWindow().x * density
            val y = positionInWindow().y * density
            Rect(x, y, x + width, y + height)
        } else null
    }