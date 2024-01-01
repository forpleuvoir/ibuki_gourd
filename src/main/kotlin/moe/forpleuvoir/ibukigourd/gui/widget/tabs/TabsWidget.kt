package moe.forpleuvoir.ibukigourd.gui.widget.tabs

import moe.forpleuvoir.ibukigourd.gui.base.Direction
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TAB_ACTIVE_BOTTOM
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TAB_ACTIVE_LEFT
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TAB_ACTIVE_RIGHT
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TAB_ACTIVE_TOP
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TAB_INACTIVE_BOTTOM
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TAB_INACTIVE_LEFT
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TAB_INACTIVE_RIGHT
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.TAB_INACTIVE_TOP
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonWidget
import moe.forpleuvoir.ibukigourd.mod.gui.Theme
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.ternary

class TabsWidget(
    width: Float,
    height: Float,
    padding: Padding? = null,
    margin: Margin? = null,
    override var spacing: Float = 2f,
    var backgroundColor: () -> ARGBColor,
    var inactiveColor: () -> ARGBColor,
    val direction: Direction = Direction.Top
) : AbstractElement() {

    var backgroundTexture = WidgetTextures.TABS_BACKGROUND

    init {
        width.let {
            transform.fixedWidth = true
            transform.width = it
        }
        height.let {
            transform.fixedHeight = true
            transform.height = it
        }
        padding?.let(::padding)
        margin?.let(::margin)
    }

    val tabs: MutableList<Tab> = arrayListOf()

    var current: Tab = tabs[0]
        set(value) {
            if (value in tabs) {
                field.onExit()
                field = value
                value.onEnter()
            } else throw IllegalArgumentException("current tab must be in tabs")
        }


    val content: ProxyElement = proxy { current.content }

    override fun init() {
        current.onEnter()
        super.init()
    }

    fun switchTab(tab: Tab) {
        current = tab
        content.switchContent(tab.content)
    }


    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderBackground.invoke(renderContext)
        current.content.render(renderContext)
        renderOverlay.invoke(renderContext)
    }


    override fun onRenderBackground(renderContext: RenderContext) {
        renderTexture(renderContext.matrixStack, contentRect(true), backgroundTexture, backgroundColor())
    }
}

fun TabsWidget.tab(
    active: Boolean = false,
    tab: Element,
    content: Element,
    scope: Tab.() -> Unit = {}
): Tab {
    return object : Tab {
        override val tab: Element = tab
        override var onEnter: () -> Unit = {}
        override var onExit: () -> Unit = {}
        override val content: Element = content
    }.apply {
        scope()
        tabs.add(this)
        tabButton(this)
        if (active) current = this
    }
}

internal fun TabsWidget.tabButton(
    tab: Tab,
): ButtonWidget {
    return object : ButtonWidget({
        this.switchTab(tab)
        NextAction.Cancel
    }, { NextAction.Cancel }, { backgroundColor() }, 0f, Theme.BUTTON.TEXTURE, null, null, null, null) {

        override fun onRenderBackground(renderContext: RenderContext) {
            val (active, inactive) = when (direction) {
                Direction.Top    -> TAB_ACTIVE_TOP to TAB_INACTIVE_TOP
                Direction.Bottom -> TAB_ACTIVE_BOTTOM to TAB_INACTIVE_BOTTOM
                Direction.Left   -> TAB_ACTIVE_LEFT to TAB_INACTIVE_LEFT
                Direction.Right  -> TAB_ACTIVE_RIGHT to TAB_INACTIVE_RIGHT
            }
            renderTexture(renderContext.matrixStack, transform, (current == tab).ternary(active, inactive), this.color())
        }
    }.apply { addElement(tab.tab) }
}

fun ElementContainer.tabs(
    width: Float,
    height: Float,
    padding: Padding? = null,
    margin: Margin? = null,
    spacing: Float = 2f,
    backgroundColor: () -> ARGBColor,
    inactiveColor: () -> ARGBColor,
    direction: Direction = Direction.Top,
    scope: TabsWidget.() -> Unit
) = addElement(Tabs(width, height, padding, margin, spacing, backgroundColor, inactiveColor, direction, scope))


fun Tabs(
    width: Float,
    height: Float,
    padding: Padding? = null,
    margin: Margin? = null,
    spacing: Float = 2f,
    backgroundColor: () -> ARGBColor,
    inactiveColor: () -> ARGBColor,
    direction: Direction = Direction.Top,
    scope: TabsWidget.() -> Unit
) = TabsWidget(width, height, padding, margin, spacing, backgroundColor, inactiveColor, direction).apply(scope)

