@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics

import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix3x2fStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

//val GuiGraphics.positionMatrix: Matrix4f get() = pose().positionMatrix

//val Matrix3x2fStack.positionMatrix: Matrix4f get() = peek().positionMatrix

inline fun GuiGraphics.useMatrixStack(block: GuiGraphics.(Matrix3x2fStack) -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    this.pose().pushMatrix()
    block(this, this.pose())
    this.pose().popMatrix()
}

fun GuiGraphics.enableScissor(box: Box) {
    this.enableScissor(box.x.toInt(), box.y.toInt(), box.right.toInt(), box.bottom.toInt())
}

inline fun GuiGraphics.useScissor(box: Box, block: GuiGraphics.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    enableScissor(box)
    block()
    disableScissor()
}