package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.extensions.bottom
import moe.forpleuvoir.ibukigourd.gui.base.extensions.mouseHovered
import moe.forpleuvoir.ibukigourd.gui.base.extensions.right
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2i
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Translatable
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import java.util.function.Consumer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * 所有组件的基类
 */
abstract class IGWidget(
    y: Int,
    x: Int,
    width: Int,
    height: Int,
    var padding: Padding = Padding(0)
) : Widget, Element, Drawable, Selectable, Tickable {

    val transform: Transform = Transform(Vector2i(x, y), width, height)

    val contentWidth: Int get() = transform.width - padding.width

    val contentHeight: Int get() = transform.height - padding.height

    override fun tick() {}

    fun contentBox(isWorldAxis: Boolean = true): Box {
        val x = if (isWorldAxis) transform.worldX + padding.left else padding.left
        val y = if (isWorldAxis) transform.worldY + padding.top else padding.top
        return Box(x, y, contentWidth, contentHeight)
    }

    //--------- Widget Begin ----------

    var active: Boolean = true
        protected set

    var visible: Boolean = true

    protected var hovered: Boolean = false

    private var focused = false

    final override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (visible) {
            this.hovered = context.scissorContains(mouseX, mouseY) && transform.isMouseOvered(mouseX, mouseY)
            this.renderWidget(context, mouseX, mouseY, delta)
            //TODO 渲染tooltip
        }
    }

    abstract fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float)

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
        if (this.focused != focused) {
            this.focused = focused
            onFocusedChange(isFocused)
        }
    }

    open fun onFocusedChange(focused: Boolean) {}

    override fun setX(x: Int) {
        transform.x = x
    }

    override fun setY(y: Int) {
        transform.y = y
    }

    override fun getX(): Int {
        return transform.x
    }

    override fun getY(): Int {
        return transform.y
    }

    override fun getWidth(): Int {
        return transform.width
    }

    override fun getHeight(): Int {
        return transform.height
    }

    override fun isFocused(): Boolean = this.focused

    override fun getNavigationFocus(): ScreenRect {
        return super<Widget>.getNavigationFocus()
    }

    abstract fun forEachElement(consumer: Consumer<IGWidget>)

    override fun forEachChild(consumer: Consumer<ClickableWidget>) {
        forEachElement { it.forEachChild(consumer) }
    }

    //---------- Widget End ------------


    //------- Selectable Begin ---------

    var message: Text = Literal("")

    override fun appendNarrations(builder: NarrationMessageBuilder) {
        //TODO 实现ToolTip
    }

    protected fun appendDefaultNarrations(builder: NarrationMessageBuilder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage())
        if (this.active) {
            if (this.isFocused) {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.button.usage.focused"))
            } else {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.button.usage.hovered"))
            }
        }
    }

    protected open fun getNarrationMessage(): MutableText {
        return Translatable("gui.narrate.button", null, arrayOf(message))
    }

    override fun getType(): Selectable.SelectionType {
        return if (this.isFocused) {
            Selectable.SelectionType.FOCUSED
        } else {
            if (this.hovered) Selectable.SelectionType.HOVERED else Selectable.SelectionType.NONE
        }
    }
    //------- Selectable End -----------


    //-------- Element Begin -----------

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        return mouseHovered(mouseX, mouseY)
    }

    //-------- Element End -------------

}