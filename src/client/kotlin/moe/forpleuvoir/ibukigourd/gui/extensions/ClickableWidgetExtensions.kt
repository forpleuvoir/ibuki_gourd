@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.gui.extensions

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import net.minecraft.client.gui.widget.ClickableWidget
import org.joml.Vector2f
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

val ClickableWidget.left: Int get() = x

val ClickableWidget.top: Int get() = y

val ClickableWidget.asBox: Box
    get() {
        return Box(position, this.width, this.height)
    }

val ClickableWidget.position: Vector2f
    get() {
        return Vector2f(this.x, this.y)
    }

val ClickableWidget.size: Size<Int>
    get() {
        return Size(this.width, this.height)
    }

fun ClickableWidget.contentBox(padding: Padding): Box {
    val x = x + padding.left
    val y = y + padding.top
    return Box(x, y, width - padding.width, height - padding.height)
}

fun ClickableWidget.mouseHovered(mouseX: Double, mouseY: Double): Boolean {
    return mouseX in this.left.toDouble()..right.toDouble()
            && mouseY in this.top.toDouble()..bottom.toDouble()
}

fun ClickableWidget.mouseHovered(mouseX: Double, mouseY: Double, block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (mouseHovered(mouseX, mouseY)) block()
}

fun ClickableWidget.mouseHoveredContent(mouseX: Double, mouseY: Double, padding: Padding): Boolean {
    val left = this.x + padding.left.toDouble()
    val top = this.y + padding.top.toDouble()
    val right = this.right - padding.right.toDouble()
    val bottom = this.bottom - padding.bottom.toDouble()
    return mouseX in left..right && mouseY in top..bottom
}

inline fun ClickableWidget.mouseHoveredContent(mouseX: Double, mouseY: Double, padding: Padding, block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (mouseHoveredContent(mouseX, mouseY, padding)) {
        block()
    }
}