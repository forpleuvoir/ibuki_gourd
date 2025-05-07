package moe.forpleuvoir.ibukigourd.gui.base.render

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.enableScissor
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector2fc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import moe.forpleuvoir.ibukigourd.gui.base.render.ScissorStack as IGScissorStack

class IGDrawContext(
    client: MinecraftClient,
    vertexConsumers: VertexConsumerProvider.Immediate
) : DrawContext(client, vertexConsumers) {


    companion object {
        fun DrawContext.toIGDrawContext(): IGDrawContext =
            this as? IGDrawContext ?: IGDrawContext(this.client, this.vertexConsumers)
    }

    private val afterRenderList: MutableList<Pair<Int, IGDrawContext.() -> Unit>> = mutableListOf()

    private var afterRendering: Boolean = false

    val igScissorStack: IGScissorStack = IGScissorStack()

    fun postRender(renderPriority: Int, render: IGDrawContext.() -> Unit) {
        if (afterRendering) return
        afterRenderList.add(renderPriority to render)
    }

    fun renderAfterRendering() {
        if (afterRenderList.isEmpty()) return
        afterRendering = true
        afterRenderList.sortedBy { it.first }.forEach { (_, render) ->
            render.invoke(this)
        }
        afterRendering = false
    }

    @OptIn(ExperimentalContracts::class)
    inline fun useMatrixStack(block: IGDrawContext.(matrices: MatrixStack) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        matrices.push()
        this.block(matrices)
        matrices.pop()
    }


    override fun enableScissor(x1: Int, y1: Int, x2: Int, y2: Int) {
        igScissorStack.push(Box(x1, y1, x2 - x1, y2 - y1))
        this.setScissor(igScissorStack.peek())
    }

    override fun disableScissor() {
        igScissorStack.pop()
        this.setScissor(igScissorStack.peek())
    }

    fun setScissor(box: Box?) {
        this.setScissor(box?.asScreenRect)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun useScissor(box: Box, block: IGDrawContext.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        enableScissor(box)
        this.block()
        disableScissor()
    }


    @OptIn(ExperimentalContracts::class)
    inline fun scissorOffset(offset: Vector2fc, block: IGDrawContext.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        igScissorStack.pushOffset(offset)
        this.block()
        igScissorStack.popOffset()
        setScissor(igScissorStack.peek())
    }

    @OptIn(ExperimentalContracts::class)
    inline fun useScissor(box: Box, offset: Vector2fc, block: IGDrawContext.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        igScissorStack.pushOffset(offset)
        enableScissor(box)
        this.block()
        igScissorStack.popOffset()
        disableScissor()
    }

}