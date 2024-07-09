package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

abstract class ClickableElement(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Text,
    var padding: Padding = Padding(0),
) : ClickableWidget(x, y, width, height, message), Tickable {

    override fun tick() {}

    fun contentBox(): Box {
        val x = x + padding.left
        val y = y + padding.top
        return Box(x, y, width - padding.width, height - padding.height)
    }

    fun mouseHoveredContent(mouseX: Double, mouseY: Double): Boolean {
        val left = this.x + padding.left.toDouble()
        val top = this.y + padding.top.toDouble()
        val right = this.right - padding.right.toDouble()
        val bottom = this.bottom - padding.bottom.toDouble()
        return mouseX in left..right && mouseY in top..bottom
    }

    @OptIn(ExperimentalContracts::class)
    inline fun mouseHoveredContent(mouseX: Double, mouseY: Double, block: () -> Unit) {
        contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
        if (mouseHoveredContent(mouseX, mouseY)) {
            block()
        }
    }

    override fun setFocused(focused: Boolean) {
        val old = isFocused
        super.setFocused(focused)
        if (isFocused != old) onFocusedChange(isFocused)
    }

    open fun onFocusedChange(focused: Boolean) {}

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        builder.put(NarrationPart.TITLE, this.narrationMessage)
    }
}