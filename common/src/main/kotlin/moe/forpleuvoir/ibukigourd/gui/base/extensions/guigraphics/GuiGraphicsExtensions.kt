@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics

import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix3x2fStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T : GuiGraphics> T.useMatrixStack(block: T.(Matrix3x2fStack) -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    this.pose().pushMatrix()
    block(this, this.pose())
    this.pose().popMatrix()
}

fun <T : GuiGraphics> T.enableScissor(box: Box) {
    this.enableScissor(box.x.toInt(), box.y.toInt(), box.right.toInt(), box.bottom.toInt())
}

inline fun <T : GuiGraphics> T.useScissor(box: Box, block: T.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    enableScissor(box)
    block()
    disableScissor()
}