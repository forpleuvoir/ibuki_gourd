package moe.forpleuvoir.ibukigourd.gui.render.context

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.Layer
import moe.forpleuvoir.ibukigourd.gui.render.ScissorStack
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.setScissor
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.rest
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector2fc
import org.joml.Vector3fc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class RenderContext(
    val client: MinecraftClient = mc,
    textRenderer: TextRenderer = mc.textRenderer,
    val matrixStack: MatrixStack = MatrixStack(),
    val scissorStack: ScissorStack = ScissorStack(),
) {

    lateinit var layer: Layer

    var textRenderer: TextRenderer = textRenderer
        private set

    var tickCounter: RenderTickCounter = RenderTickCounter.ZERO
        private set

    val positionMatrix by matrixStack.peek()::positionMatrix

    val normalMatrix by matrixStack.peek()::normalMatrix

    val tessellator: Tessellator get() = Tessellator.getInstance()

    lateinit var bufferBuilder: BufferBuilder

    val immediate: VertexConsumerProvider.Immediate = VertexConsumerProvider.immediate(tessellator.allocator)

    private val renderList: MutableList<Pair<Int, RenderContext.() -> Unit>> = mutableListOf()

    private var rendering: Boolean = false

    inline operator fun invoke(block: RenderContext.() -> Unit) {
        block.invoke(this)
    }

    fun canRender(element: Element): Boolean {
        return if (::layer.isInitialized) {
            element.layer == this@RenderContext.layer
        } else false
    }

    @OptIn(ExperimentalContracts::class)
    inline fun tryRender(element: Element, block: RenderContext.() -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (canRender(element)) block()
    }

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

    fun nextFrame(tickCounter: RenderTickCounter): RenderContext {
        renderList.clear()
        this.tickCounter = tickCounter
        matrixStack.rest()
        scissorStack.rest()
        return this
    }

    fun useTextRenderer(textRenderer: TextRenderer, block: RenderContext.() -> Unit) {
        val temp = this.textRenderer
        this.textRenderer = textRenderer
        this.block()
        this.textRenderer = temp
    }

    inline fun useMatrixStack(block: RenderContext.(matrixStack: MatrixStack) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        matrixStack.push()
        this.block(matrixStack)
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

    inline fun scissorOffset(offset: Vector3fc, block: RenderContext.() -> Unit) {
        scissorOffset(Vector2f(offset.x(), offset.y()), block)
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