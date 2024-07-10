package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.IGDrawable
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.input.MousePosition
import net.minecraft.client.gui.DrawContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 所有组件的基类
 */
abstract class IGWidget(
    var padding: Padding = Padding(0),
    var margin: Margin = Margin(0)
) : IGElement, IGDrawable {

    val transform: Transform = Transform()

    val contentWidth: Float get() = transform.width - padding.width

    val contentHeight: Float get() = transform.height - padding.height

    override fun tick() {}

    fun contentBox(isWorldAxis: Boolean = true): Box {
        val x = if (isWorldAxis) transform.worldX + padding.left else padding.left
        val y = if (isWorldAxis) transform.worldY + padding.top else padding.top
        return Box(x, y, contentWidth, contentHeight)
    }

    //--------- Widget Begin ----------

    var active: Boolean = true
        protected set

    override var visible: Boolean = true

    protected var hovered: Boolean = false

    private var wasFocused = false

    final override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (visible) {
            this.hovered = context.scissorContains(mouseX, mouseY) && transform.isMouseOvered(mouseX, mouseY)
            this.renderWidget(context, mouseX, mouseY, delta)
            //TODO 渲染tooltip
        }
    }

    abstract fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float)

    fun mouseHoveredContent(mouseX: Double, mouseY: Double): Boolean {
        return MousePosition(mouseX, mouseY) in contentBox(true)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun mouseHoveredContent(mouseX: Double, mouseY: Double, block: () -> Unit) {
        contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
        if (mouseHoveredContent(mouseX, mouseY)) {
            block()
        }
    }

    override fun setFocused(focused: Boolean) {
        if (this.wasFocused != focused) {
            this.wasFocused = focused
            onFocusedChange(isFocused)
        }
    }

    open fun onFocusedChange(focused: Boolean) {}


    override fun isFocused(): Boolean = this.wasFocused

}
