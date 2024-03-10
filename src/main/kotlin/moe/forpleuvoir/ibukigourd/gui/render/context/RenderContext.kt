package moe.forpleuvoir.ibukigourd.gui.render.context

import moe.forpleuvoir.ibukigourd.gui.render.ScissorStack
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.setScissor
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.rest
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector2fc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class RenderContext(
    val client: MinecraftClient = mc,
    val textRenderer: TextRenderer = mc.textRenderer,
    val matrixStack: MatrixStack = MatrixStack(),
    val scissorStack: ScissorStack = ScissorStack(),
) {

    var tickDelta: Float = 0f
        private set

    val positionMatrix by matrixStack.peek()::positionMatrix

    val normalMatrix by matrixStack.peek()::normalMatrix

    val tessellator: Tessellator get() = Tessellator.getInstance()

    val bufferBuilder: BufferBuilder get() = tessellator.buffer

    private val renderList: MutableList<Pair<Int, RenderContext.() -> Unit>> = mutableListOf()

    private var rendering: Boolean = false

    fun postRender(renderPriority: Int, render: RenderContext.() -> Unit) {
        if (rendering) return
        renderList.add(renderPriority to render)
    }

    fun render() {
        rendering = true
        renderList.sortedBy { it.first }.forEach { (_, render) ->
            render.invoke(this)
        }
        rendering = false
    }

    fun nextFrame(tickDelta: Float): RenderContext {
        renderList.clear()
        this.tickDelta = tickDelta
        matrixStack.rest()
        scissorStack.rest()
        return this
    }

    inline fun matrixStack(block: RenderContext.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        matrixStack.push()
        this.block()
        matrixStack.pop()
    }

    fun enableScissor(x: Number, y: Number, width: Number, height: Number) {
        scissorStack.push(Box(x, y, width, height))
        setScissor(scissorStack.peek())
    }

    fun enableScissor(rect: Box) {
        scissorStack.push(rect)
        setScissor(scissorStack.peek())
    }

    fun disableScissor() {
        scissorStack.pop()
        setScissor(scissorStack.peek())
    }

    inline fun scissor(rect: Box, block: RenderContext.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        enableScissor(rect)
        this.block()
        disableScissor()
    }

    inline fun scissorOffset(offset: Vector2fc, block: RenderContext.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        scissorStack.pushOffset(offset)
        this.block()
        scissorStack.popOffset()
        setScissor(scissorStack.peek())
    }

    inline fun scissor(rect: Box, offset: Vector2fc, block: RenderContext.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        scissorStack.pushOffset(offset)
        enableScissor(rect)
        this.block()
        scissorStack.popOffset()
        disableScissor()
    }

}