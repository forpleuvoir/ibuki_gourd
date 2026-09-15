package moe.forpleuvoir.ibukigourd.ui.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import kotlin.math.ceil
import kotlin.math.min

/**
 * 透明棋盘底：两色交替方格，垫在带透明度的颜色下方，用来直观表达 alpha。
 *
 * 纯绘制实现（不依赖素材）：每格一次 [DrawScope.drawRect]，末行 / 末列按剩余尺寸截断，
 * 因此不会画出组件边界（详见 [drawCheckerboard]）。
 *
 * 需要在同一个画布里和其它内容一起画时（如通道条），直接调 [drawCheckerboard]，
 * 不要另外嵌一层组合节点。
 *
 * @param tileSize 单格边长
 * @param colorA 浅色格
 * @param colorB 深色格
 */
@Composable
fun Checkerboard(
    tileSize: Dp,
    modifier: Modifier = Modifier,
    colorA: Color = CheckerboardDefaults.Light,
    colorB: Color = CheckerboardDefaults.Dark,
) {
    Canvas(modifier) { drawCheckerboard(tileSize, colorA, colorB) }
}

/**
 * 在当前 [DrawScope] 的整个尺寸上画棋盘（供单画布绘制的场合直接调用）。
 *
 * 末行 / 末列按**剩余尺寸截断**（不画整格）：绘制节点（`Canvas` / `drawBehind`）不会把内容裁到
 * 节点边界，若照整格画，放不下的那一格会溢出到组件外面（条高 28dp / 格 8dp = 3.5 格就是这种情况）。
 */
fun DrawScope.drawCheckerboard(
    tileSize: Dp,
    colorA: Color = CheckerboardDefaults.Light,
    colorB: Color = CheckerboardDefaults.Dark,
) {
    val tile = tileSize.toPx().coerceAtLeast(1f)
    val cols = ceil(size.width / tile).toInt()
    val rows = ceil(size.height / tile).toInt()
    for (row in 0 until rows) {
        val cellHeight = min(tile, size.height - row * tile)
        if (cellHeight <= 0f) continue
        for (col in 0 until cols) {
            val cellWidth = min(tile, size.width - col * tile)
            if (cellWidth <= 0f) continue
            drawRect(
                color = if ((row + col) % 2 == 0) colorA else colorB,
                topLeft = Offset(col * tile, row * tile),
                size = Size(cellWidth, cellHeight),
            )
        }
    }
}

object CheckerboardDefaults {

    val Light = Color(0xFFFFFFFF)

    val Dark = Color(0xFFCCCCCC)
}
