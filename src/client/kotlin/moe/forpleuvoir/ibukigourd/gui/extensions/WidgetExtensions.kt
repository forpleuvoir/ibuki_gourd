package moe.forpleuvoir.ibukigourd.gui.extensions

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.arrange.HorizontalAlignment
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import net.minecraft.client.gui.widget.Widget
import org.joml.Vector2f
import org.joml.Vector2i
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

val Widget.left: Int get() = x

@get:JvmName("right")
val Widget.right: Int get() = x + width

val Widget.top: Int get() = y

@get:JvmName("bottom")
val Widget.bottom: Int get() = y + height

val Widget.asBox: Box
    get() {
        return Box(position, this.width, this.height)
    }

val Widget.position: Vector2f
    get() {
        return Vector2f(this.x, this.y)
    }

val Widget.size: Size<Int>
    get() {
        return Size(this.width, this.height)
    }

fun Widget.contentBox(padding: Padding): Box {
    val x = x + padding.left
    val y = y + padding.top
    return Box(x, y, width - padding.width, height - padding.height)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Widget.mouseHovered(mouseX: Double, mouseY: Double): Boolean {
    return mouseX in left.toDouble()..right.toDouble() && mouseY in top.toDouble()..bottom.toDouble()
}

@OptIn(ExperimentalContracts::class)
fun Widget.mouseHovered(mouseX: Double, mouseY: Double, block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (mouseHovered(mouseX, mouseY)) block()
}

fun Widget.mouseHoveredContent(mouseX: Double, mouseY: Double, padding: Padding): Boolean {
    val left = this.x + padding.left.toDouble()
    val top = this.y + padding.top.toDouble()
    val right = this.right - padding.right.toDouble()
    val bottom = this.bottom - padding.bottom.toDouble()
    return mouseX in left..right && mouseY in top..bottom
}

@OptIn(ExperimentalContracts::class)
inline fun Widget.mouseHoveredContent(mouseX: Double, mouseY: Double, padding: Padding, block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (mouseHoveredContent(mouseX, mouseY, padding)) {
        block()
    }
}

fun Widget.translate(vector2i: Vector2i) {
    translate(vector2i.x, vector2i.y)
}

fun Widget.translate(x: Int, y: Int) {
    this.x += x
    this.y += y
}

fun Widget.translateTo(vector2i: Vector2i) {
    translateTo(vector2i.x, vector2i.y)
}

fun Widget.translateTo(x: Int, y: Int) {
    this.x = x
    this.y = y
}

fun Widget.moveToTop(target: Widget, margin: Int, alignment: (Orientation) -> HorizontalAlignment = HorizontalAlignment::Center) {
    alignment(Orientation.Horizontal).align(target.asBox, this.asBox).let { v2f ->
        translateTo(v2f.x().toInt(), target.top - margin - this.height)
    }
}

fun Widget.moveToBottom(target: Widget, margin: Int, alignment: (Orientation) -> HorizontalAlignment = HorizontalAlignment::Center) {
    alignment(Orientation.Horizontal).align(target.asBox, this.asBox).let { v2f ->
        translateTo(v2f.x().toInt(), target.bottom + margin)
    }
}

fun Widget.moveToLeft(target: Widget, margin: Int, alignment: (Orientation) -> HorizontalAlignment = HorizontalAlignment::Center) {
    alignment(Orientation.Vertical).align(target.asBox, this.asBox).let { v2f ->
        translateTo(target.left - margin - this.width, v2f.y().toInt())
    }
}

fun Widget.moveToRight(target: Widget, margin: Int, alignment: (Orientation) -> HorizontalAlignment = HorizontalAlignment::Center) {
    alignment(Orientation.Vertical).align(target.asBox, this.asBox).let { v2f ->
        translateTo(target.right + margin, v2f.y().toInt())
    }
}