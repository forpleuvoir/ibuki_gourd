package com.forpleuvoir.ibukigourd.gui.screen

import com.forpleuvoir.ibukigourd.gui.widget.button.button
import com.forpleuvoir.ibukigourd.gui.widget.text.textField
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import com.forpleuvoir.ibukigourd.render.renderOutline
import com.forpleuvoir.ibukigourd.render.renderRect
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.mouseX
import com.forpleuvoir.ibukigourd.util.mouseY
import com.forpleuvoir.ibukigourd.util.text.literal
import com.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.util.math.MatrixStack

fun testScreen() {
	ScreenManager.setScreen(object : AbstractScreen() {
		override var renderBackground: (matrixStack: MatrixStack, delta: Float) -> Unit = { matrixStack, _ ->
			renderRect(matrixStack, this.transform, Colors.BLACK.opacity(0.5f))
		}
		override var renderOverlay: (matrixStack: MatrixStack, delta: Float) -> Unit = { matrixStack, _ ->
			val rect = contentRect(true)
			renderOutline(matrixStack, this.transform, Colors.YELLOW, 2)
			renderOutline(matrixStack, colorVertex(rect.position.vector3f(), Colors.GREEN), rect.width, rect.height)
			renderRect(matrixStack, colorVertex(Vector3f(mouseX.toFloat(), mouseY.toFloat()), Colors.RED), 5f, 5f)
		}
	}.apply {
		for (i in 0..5) {
			button {
				if (i != 0) {
					margin(left = 5)
				}
				render = { matrixStack, delta ->
					onRender(matrixStack, delta)
				}
				val t = literal("按钮$i").style {
					it.withColor(Colors.BLACK.rgb)
				}
				val text = textField(t, shadow = false)
				onClick = {
					println("我点击了${text.text.string}")
					NextAction.Cancel
				}
			}
		}
	})
	println("当前屏幕:${ScreenManager.current}")
	println("当前屏幕:${ScreenManager.current!!.elementTree}")
}