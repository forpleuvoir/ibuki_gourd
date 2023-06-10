package moe.forpleuvoir.ibukigourd.render

import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack


class RenderContext(
	val client: MinecraftClient = mc,
	val matrixStack: MatrixStack = MatrixStack(),
	val scissorStack: ScissorStack = ScissorStack(),
	val tickDelta: Float
) {

	inline fun matrixStack(action: RenderContext.() -> Unit) {
		matrixStack.push()
		action()
		matrixStack.pop()
	}

	fun enableScissor(x: Number, y: Number, width: Number, height: Number) {
		scissorStack.push(rect(x, y, 0, width, height))
		setScissor(scissorStack.peek())
	}

	fun enableScissor(rect: Rectangle<Vector3<Float>>) {
		scissorStack.push(rect)
		setScissor(scissorStack.peek())
	}

	fun disableScissor() {
		scissorStack.pop()
		setScissor(scissorStack.peek())
	}

	inline fun scissor(rect: Rectangle<Vector3<Float>>, action: RenderContext.() -> Unit) {
		enableScissor(rect)
		action()
		disableScissor()
	}

	inline fun scissorOffset(offset: Vector3<Float>, action: RenderContext.() -> Unit) {
		scissorStack.pushOffset(offset)
		action()
		scissorStack.popOffset()
		setScissor(scissorStack.peek())
	}

	inline fun scissor(rect: Rectangle<Vector3<Float>>, offset: Vector3<Float>, action: RenderContext.() -> Unit) {
		scissorStack.pushOffset(offset)
		enableScissor(rect)
		action()
		scissorStack.popOffset()
		disableScissor()
	}

}