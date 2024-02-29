package moe.forpleuvoir.ibukigourd.gui.widget.tabs

import moe.forpleuvoir.ibukigourd.gui.base.Direction
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Row
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
import moe.forpleuvoir.ibukigourd.render.base.Dimension
import moe.forpleuvoir.ibukigourd.render.base.Orientation
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.pick

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
        margin?.let(::margin)
    }

    val tabs: MutableList<Tab> = arrayListOf()

    val tabElements: MutableList<Element> = arrayListOf()

    val currentTabElement: Element get() = tabElements[tabs.indexOf(current)]

    fun <T : Element> addTabElement(element: T): T {
        tabElements.add(element)
        return addElement(element)
    }

    var current: Tab? = null
        set(value) {
            if (value in tabs && value != null) {
                field?.onExit?.invoke()
                field = value
                value.onEnter()
            } else throw IllegalArgumentException("current tab must be in tabs")
        }


    val content: ProxyElement = proxy {
        padding?.let { padding(it) }
        fixed = true
        Row { }
    }

    override fun init() {
        if (current == null) current = tabs[0]
        current!!.onEnter()
        switchTab(current!!)
        super.init()
    }

    override val layout: Layout = object : Layout {
        override val elementContainer: () -> ElementContainer = { this@TabsWidget }

        override var spacing: Float = this@TabsWidget.spacing

        override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Dimension<Float>? {
            if (elements.isEmpty()) return null

            val offset = when (direction) {
                Direction.Top    -> vertex(0, 1f, 0)
                Direction.Bottom -> vertex(0, -1f, 0)
                Direction.Left   -> vertex(1f, 0, 0)
                Direction.Right  -> vertex(-1f, 0, 0)
            }

            val alignment = when (direction) {
                Direction.Top    -> PlanarAlignment.BottomLeft(Orientation.Horizontal)
                Direction.Bottom -> PlanarAlignment.TopLeft(Orientation.Horizontal)
                Direction.Left   -> PlanarAlignment.TopRight(Orientation.Vertical)
                Direction.Right  -> PlanarAlignment.TopLeft(Orientation.Vertical)
            }

            val alignRects = alignRects(elements, alignment.orientation)

            val size = alignment.orientation.contentSize(alignRects)

            val contentRect = when (direction) {
                Direction.Top    -> rect(vertex(4f, 0, 0), transform.width, size.height)
                Direction.Bottom -> rect(vertex(4f, transform.height - size.height, 0), transform.width, size.height)
                Direction.Left   -> rect(vertex(0, 4f, 0), size.width, transform.height)
                Direction.Right  -> rect(vertex(transform.width - size.width, 4f, 0), size.width, transform.height)
            }
            alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
                elements[index].let {
                    it.transform.translateTo(vector3f + Vector3f(it.margin.left, it.margin.top))
                    if (it != currentTabElement) {
                        it.transform.translate(offset)
                    }
                }
            }
            return Dimension.of(contentRect.width, contentRect.height)
        }
    }

    var tabDimension: Dimension<Float> = Dimension.of(0f, 0f)

    override fun arrange() {
        println("我执行了")
        layout.layout(tabElements, margin, padding)?.let { size ->
            tabDimension = size
            contentRect(false).let {
                content.transform.translateTo(it.position)
                content.transform.width = it.width
                content.transform.height = it.height
            }
        }
    }

    fun switchTab(tab: Tab) {
        current = tab
        content.switchContent(tab.content)
    }

    override fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>> {
        val top = (if (isWorld) transform.worldTop + padding.top else padding.top) + if (direction == Direction.Top) tabDimension.height - 3 else 0f
        val bottom = (if (isWorld) transform.worldBottom - padding.bottom else transform.height - padding.bottom) - if (direction == Direction.Bottom) tabDimension.height - 3 else 0f
        val left = (if (isWorld) transform.worldLeft + padding.left else padding.left) + if (direction == Direction.Left) tabDimension.width - 3 else 0f
        val right = (if (isWorld) transform.worldRight - padding.right else transform.width - padding.right) - if (direction == Direction.Right) tabDimension.width - 3 else 0f
        return rect(
            vertex(left, top, if (isWorld) transform.worldZ else transform.z), right - left, bottom - top
        )
    }

    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        tabElements.filter { it != currentTabElement }.forEach { it.render(renderContext) }
        renderBackground.invoke(renderContext)
        tabElements.find { it == currentTabElement }?.render?.let { it(renderContext) }
        content.render(renderContext)
        renderOverlay.invoke(renderContext)
//        renderRect(renderContext.matrixStack, rect(transform.worldPosition, tabSize), Color(0x7F000000))
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
        if (active) current = this
        tabButton(this)
    }
}

internal fun TabsWidget.tabButton(
    tab: Tab,
): ButtonWidget {
    return addTabElement(object : ButtonWidget({
        this.switchTab(tab)
        NextAction.Cancel
    }, { NextAction.Cancel }, { (current == tab).pick({ backgroundColor() }, { inactiveColor() }) }, 0f, Theme.BUTTON.TEXTURE, null, null, Padding(2), null) {

        override fun onRenderBackground(renderContext: RenderContext) {
            val (active, inactive) = when (direction) {
                Direction.Top    -> TAB_ACTIVE_TOP to TAB_INACTIVE_TOP
                Direction.Bottom -> TAB_ACTIVE_BOTTOM to TAB_INACTIVE_BOTTOM
                Direction.Left   -> TAB_ACTIVE_LEFT to TAB_INACTIVE_LEFT
                Direction.Right  -> TAB_ACTIVE_RIGHT to TAB_INACTIVE_RIGHT
            }
            renderTexture(renderContext.matrixStack, transform, (current == tab).pick(active, inactive), this.color())
        }
    }.apply { addElement(tab.tab) })
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

