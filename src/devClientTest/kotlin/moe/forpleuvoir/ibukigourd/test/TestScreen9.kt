package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.maxWidth
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.modifier.*
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import org.joml.Quaternionf
import java.lang.Math.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun testScreen9() = RowScreen(
    Modifier.debugInfo {
        ScreenFPS()
        ScreenRenderTime()
        MouseCursor()
        MousePosition()
    }
) {
    Button(Modifier.weight(1).maxWidth(120f)) {
        TextLabel("最大宽度：120")
    }
    Button(Modifier.weight(1)) {
        TextLabel("没有最大宽度限制")
    }
}


fun drawSineCurve(context: IGDrawContext, xStart: Float, yStart: Float, scale: Double, color: Int) {
    val vertices = mutableListOf<Pair<Double, Double>>()

    // 生成曲线点
    for (x in 0..360 step 5) {
        val rad = toRadians(x.toDouble())
        val xf = rad * scale
        val yf = sin(rad) * scale
        vertices.add(Pair(xf, yf))
    }

    for (i in 0 until vertices.size - 1) {
        val (x1, y1) = vertices[i]
        val (x2, y2) = vertices[i + 1]

        val dx = x2 - x1
        val dy = y2 - y1
        val length = sqrt(dx * dx + dy * dy)
        val angle = atan2(dy, dx)

        context.matrices.push()

        // 获取 MatrixStack 并进行平移和旋转变换

        context.matrices.translate((xStart + x1).toFloat(), (yStart + y1).toFloat(), 0f)

        // 使用 Quaternion 构造绕 Z 轴旋转的四元数
        val rotation = Quaternionf(0f, 0f, sin(angle / 2).toFloat(), cos(angle / 2).toFloat())
        context.matrices.multiply(rotation)

        // 假设你有一个 fill 方法用于绘制矩形（模拟线段）
        context.fill(0, 0, length.toInt(), 2, color)

        context.matrices.pop()
    }
}