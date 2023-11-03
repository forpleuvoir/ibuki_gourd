package moe.forpleuvoir.ibukigourd.render

import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.setScissor
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.rest
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class RenderContext(
    val client: MinecraftClient = mc,
    val matrixStack: MatrixStack = MatrixStack(),
    val scissorStack: ScissorStack = ScissorStack(),
) {

    var tickDelta: Float = 0f
        private set

    fun nextFrame(tickDelta: Float): RenderContext {
        this.tickDelta = tickDelta
        matrixStack.rest()
        scissorStack.rest()
        return this
    }

    inline fun matrixStack(action: RenderContext.() -> Unit) {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
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
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        enableScissor(rect)
        action()
        disableScissor()
    }

    inline fun scissorOffset(offset: Vector3<Float>, action: RenderContext.() -> Unit) {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        scissorStack.pushOffset(offset)
        action()
        scissorStack.popOffset()
        setScissor(scissorStack.peek())
    }

    inline fun scissor(rect: Rectangle<Vector3<Float>>, offset: Vector3<Float>, action: RenderContext.() -> Unit) {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        scissorStack.pushOffset(offset)
        enableScissor(rect)
        action()
        scissorStack.popOffset()
        disableScissor()
    }

}