package com.forpleuvoir.ibukigourd.gui.screen

import com.forpleuvoir.ibukigourd.gui.base.Direction
import com.forpleuvoir.ibukigourd.gui.tip.mouseHoverTip
import com.forpleuvoir.ibukigourd.gui.widget.button.button
import com.forpleuvoir.ibukigourd.gui.widget.text.textField
import com.forpleuvoir.ibukigourd.render.*
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.rectangle.rect
import com.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.mc
import com.forpleuvoir.ibukigourd.util.mouseX
import com.forpleuvoir.ibukigourd.util.mouseY
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors
import java.util.function.Supplier

fun testScreen() {
	ScreenManager.open {
		renderBackground = { matrixStack, _ ->
			renderRect(matrixStack, this.transform, Colors.BLACK.opacity(0.5f))
		}
		renderOverlay = { matrixStack, _ ->
			val rect = contentRect(true)
			renderOutline(matrixStack, this.transform, Colors.YELLOW, 2)
			renderOutline(matrixStack, colorVertex(rect.position, Colors.GREEN), rect.width, rect.height)
			renderLine(
				matrixStack, 2f,
				colorVertex(0, mouseY, 0, Colors.RED),
				colorVertex(mc.window.scaledWidth, mouseY, 0, Colors.RED),
				normal = Vector3f(1f, 0f, 0f)
			)
			renderHueGradientRect(matrixStack, rect(3f, 3f, 0f, 240f, 5f), 240, 0f..360f)
			renderSaturationGradientRect(matrixStack, rect(3f, 9f, 0f, 240f, 5f), 240, 0f..1f, 210f, 1f)
			renderValueGradientRect(matrixStack, rect(3f, 15f, 0f, 240f, 5f), 240, 0f..1f, 210f, 1f)
			renderAlphaGradientRect(matrixStack, rect(3f, 21f, 0f, 240f, 5f), 240, 0f..1f, Color(0, 128, 255))
			renderSVGradientRect(matrixStack, rect(3f, 27f, 0f, 160f, 120f), hue = 210f)
			renderLine(
				matrixStack, 2f,
				colorVertex(mouseX + 1, 0, 0, color = Colors.GREEN),
				colorVertex(mouseX + 1, mc.window.scaledHeight, 0, Colors.GREEN),
				normal = Vector3f(0f, 1f, 0f)
			)
		}
		for (i in 0..5) {
			button(color = Colors.MAHOGANY) {
				if (i != 0) {
					margin(top = 5)
				}
				var ro = 0.0f
				tick = {
					tick()
					ro += 0.1f
					if (ro >= 360) ro = 0f
				}
				mouseHoverTip {
					if (i <= 3) forcedDirection = Direction.values()[i]
					var a = 1f
					mouseScrolling = { _, _, amount ->
						a += amount.toFloat()
						NextAction.Continue
					}
					textField("多行文本测试。\n这是第二行")
					textField(Supplier {
						val sb = StringBuilder()
						(0..a.toInt()).forEach { i ->
							sb.append("-\n")
						}
						"这是鼠标滚动值${sb}"
					})
					button {
						textField("居然是按钮$i,我加长呢")
					}
				}
				var count = 0
				val text = textField(Supplier { "按钮$i:$count" })
				onClick = {
					println("我点击了${text.text().string}")
					count++
					NextAction.Cancel
				}
			}
		}
	}
	println("当前屏幕:${ScreenManager.current}")
	println("当前屏幕:${ScreenManager.current!!.elementTree}")
}