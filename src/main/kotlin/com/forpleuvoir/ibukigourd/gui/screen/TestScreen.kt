package com.forpleuvoir.ibukigourd.gui.screen

import com.forpleuvoir.ibukigourd.gui.base.Direction
import com.forpleuvoir.ibukigourd.gui.tip.mouseHoverTip
import com.forpleuvoir.ibukigourd.gui.widget.button.button
import com.forpleuvoir.ibukigourd.gui.widget.text.textField
import com.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex
import com.forpleuvoir.ibukigourd.render.renderLine
import com.forpleuvoir.ibukigourd.render.renderOutline
import com.forpleuvoir.ibukigourd.render.renderRect
import com.forpleuvoir.ibukigourd.render.renderText
import com.forpleuvoir.ibukigourd.util.*
import com.forpleuvoir.ibukigourd.util.text.literal
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
			renderOutline(matrixStack, colorVertex(rect.position.vector3f(), Colors.GREEN), rect.width, rect.height)
			renderLine(
				matrixStack, 1f,
				colorVertex(0, mouseY, 0, Colors.RED),
				colorVertex(mc.window.scaledWidth, mouseY, 0, Colors.RED),
				vertex(1f, 0f, 0f)
			)
			renderLine(
				matrixStack, 1f,
				colorVertex(mouseX + 1, 0, 0, color = Colors.GREEN),
				colorVertex(mouseX + 1, mc.window.scaledHeight, 0, Colors.GREEN),
				vertex(0f, 1f, 0f)
			)
		}
		for (i in 0..5) {
			button {
				if (i != 0) {
					margin(top = 5)
				}
				mouseHoverTip {
					if (i <= 3) forcedDirection = Direction.values()[i]
					var a = 1f
					mouseScrolling = { _, _, amount ->
						a += amount.toFloat()
						NextAction.Continue
					}
					render = { matrixStack, delta ->
						onRender(matrixStack, delta)
						renderRect(matrixStack, this.transform, Colors.GREEN.opacity(0.5f))
						textRenderer.renderText(
							matrixStack,
							literal("width:${transform.width},height:${transform.height}"),
							transform.worldX,
							transform.worldBottom,
							color = Colors.BLACK
						)
					}

					textField("多行文本测试。\n这是第二行")
					textField(Supplier {
						val sb = StringBuilder()
						(0..a.toInt()).forEach { i ->
							sb.append("-\n")
						}
						"这是鼠标滚动值${sb}"
					}) {
						renderOverlay = { matrixStack, delta ->
							renderRect(matrixStack, this.transform, Colors.RED.opacity(0.5f))
						}
					}
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