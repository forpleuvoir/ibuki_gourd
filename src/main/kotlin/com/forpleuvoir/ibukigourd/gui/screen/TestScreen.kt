package com.forpleuvoir.ibukigourd.gui.screen

import com.forpleuvoir.ibukigourd.gui.base.Direction
import com.forpleuvoir.ibukigourd.gui.tip.tip
import com.forpleuvoir.ibukigourd.gui.widget.button.button
import com.forpleuvoir.ibukigourd.gui.widget.text.textField
import com.forpleuvoir.ibukigourd.render.*
import com.forpleuvoir.ibukigourd.render.base.Arrangement
import com.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.rectangle.rect
import com.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import com.forpleuvoir.ibukigourd.util.*
import com.forpleuvoir.ibukigourd.util.text.literal
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.util.math.MatrixStack
import java.util.function.Supplier

fun testScreen() {
	ScreenManager.open {
		renderBackground = { matrixStack, _ ->
			renderRect(matrixStack, this.transform, Colors.BLACK.opacity(0.5f))
		}
		padding(4)
		renderOverlay = { matrixStack, _ ->
			val rect = contentRect(false)
			textRenderer.renderAlignmentText(
				matrixStack,
				literal("x:%.2f, y:%.2f".format(mouseX, mouseY)),
				rect,
				align = PlanarAlignment::BottomLeft,
				false,
				color = Colors.WHITE
			)
			renderCrossHairs(matrixStack, rect.center.x, rect.center.y)
			renderOutline(matrixStack, rect, Colors.RED.opacity(0.5f))
			renderCrossHairs(matrixStack, mouseX, mouseY)
		}
		for (i in 0..5) {
			button(color = Colors.PINK_CUPCAKE) {
				if (i != 0) {
					margin(top = 5)
				}
				var ro = 0.0f
				tick = {
					tick()
					ro += 0.1f
					if (ro >= 360) ro = 0f
				}
				tip(color = Colors.PINK_CUPCAKE) {
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
						onClick = {
							println("TIP里的按钮居然可以点！")
							NextAction.Cancel
						}
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

fun renderCrossHairs(matrixStack: MatrixStack, x: Number, y: Number) {
	renderLine(
		matrixStack, 2f,
		colorVertex(0, y, 0, Colors.RED),
		colorVertex(mc.window.scaledWidth, y, 0, Colors.RED),
		normal = Vector3f(1f, 0f, 0f)
	)
	renderLine(
		matrixStack, 2f,
		colorVertex(x.toDouble() + 1, 0, 0, color = Colors.LIME),
		colorVertex(x.toDouble() + 1, mc.window.scaledHeight, 0, Colors.LIME),
		normal = Vector3f(0f, 1f, 0f)
	)
}

fun colorTest(matrixStack: MatrixStack) {
	renderHueGradientRect(matrixStack, rect(3f, 3f, 0f, 160, 5f), 240, hueRange = 0f..360f)
	renderHueGradientRect(matrixStack, rect(164f, 3f, 0f, 160, 5f), 240, reverse = true, hueRange = 0f..360f)

	renderHueGradientRect(matrixStack, rect(3, 109f, 0f, 5f, 120f), 240, arrangement = Arrangement.Vertical, hueRange = 0f..360f)
	renderHueGradientRect(matrixStack, rect(9, 109f, 0f, 5f, 120f), 240, arrangement = Arrangement.Vertical, reverse = true, hueRange = 0f..360f)


	renderSaturationGradientRect(matrixStack, rect(3f, 9f, 0f, 160, 5f), saturationRange = 0f..1f, hue = 210f, value = 1f)
	renderSaturationGradientRect(matrixStack, rect(164f, 9f, 0f, 160, 5f), reverse = true, saturationRange = 0f..1f, hue = 210f, value = 1f)

	renderSaturationGradientRect(matrixStack, rect(15, 109f, 0f, 5, 120), arrangement = Arrangement.Vertical, saturationRange = 0f..1f, hue = 210f, value = 1f)
	renderSaturationGradientRect(
		matrixStack,
		rect(21, 109f, 0f, 5, 120),
		arrangement = Arrangement.Vertical,
		reverse = true,
		saturationRange = 0f..1f,
		hue = 210f,
		value = 1f
	)


	renderValueGradientRect(matrixStack, rect(3f, 15f, 0f, 160, 5f), valueRange = 0f..1f, hue = 210f, saturation = 1f)
	renderValueGradientRect(matrixStack, rect(164f, 15f, 0f, 160, 5f), reverse = true, valueRange = 0f..1f, hue = 210f, saturation = 1f)

	renderValueGradientRect(matrixStack, rect(27, 109, 0f, 5, 120), arrangement = Arrangement.Vertical, valueRange = 0f..1f, hue = 210f, saturation = 1f)
	renderValueGradientRect(
		matrixStack,
		rect(33, 109, 0f, 5, 120),
		arrangement = Arrangement.Vertical,
		reverse = true,
		valueRange = 0f..1f,
		hue = 210f,
		saturation = 1f
	)


	renderAlphaGradientRect(matrixStack, rect(3f, 21f, 0f, 160, 5f), alphaRange = 0f..1f, color = Color(0, 128, 255))
	renderAlphaGradientRect(matrixStack, rect(164f, 21f, 0f, 160, 5f), reverse = true, alphaRange = 0f..1f, color = Color(0, 128, 255))

	renderAlphaGradientRect(matrixStack, rect(39, 109, 0f, 5, 120), arrangement = Arrangement.Vertical, alphaRange = 0f..1f, color = Color(0, 128, 255))
	renderAlphaGradientRect(
		matrixStack,
		rect(45, 109, 0f, 5, 120),
		arrangement = Arrangement.Vertical,
		reverse = true,
		alphaRange = 0f..1f,
		color = Color(0, 128, 255)
	)


	renderSVGradientRect(matrixStack, rect(3f, 27f, 0f, 120f, 80f), hue = 210f)
	renderSVGradientRect(matrixStack, rect(124f, 27f, 0f, 120f, 80f), hue = 210f, reverse = true)

	renderSVGradientRect(matrixStack, rect(51, 109, 0f, 80, 120), arrangement = Arrangement.Vertical, hue = 210f)
	renderSVGradientRect(matrixStack, rect(132, 109, 0f, 80, 120), arrangement = Arrangement.Vertical, hue = 210f, reverse = true)
}