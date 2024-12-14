package moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext

import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

val DrawContext.positionMatrix: Matrix4f get() = matrices.peek().positionMatrix

val MatrixStack.positionMatrix: Matrix4f get() = peek().positionMatrix

@OptIn(ExperimentalContracts::class)
inline fun DrawContext.useMatrixStack(block: DrawContext.(MatrixStack) -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    this.matrices.push()
    block(this, this.matrices)
    this.matrices.pop()
}

fun DrawContext.enableScissor(box: Box) {
    this.enableScissor(box.x.toInt(), box.y.toInt(), box.right.toInt(), box.bottom.toInt())
}

@OptIn(ExperimentalContracts::class)
fun DrawContext.scissor(box: Box, block: DrawContext.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    enableScissor(box)
    block()
    disableScissor()
}