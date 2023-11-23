package moe.forpleuvoir.ibukigourd.gui.tip

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.flatButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.util.clamp

class DropTip(
    parent: Element,
    tipHandler: () -> TipHandler = { parent.screen() },
    width: Float? = null,
    height: Float? = null,
    padding: Margin? = Margin(6),
    val showScroller: Boolean = true,
    scrollerThickness: Float = 10f,
) : Tip({ parent }, tipHandler) {

    lateinit var scrollerBar: Scroller
        private set

    val arrow: Button = flatButton {
        fixed = true
        icon(WidgetTextures.DROP_MENU_ARROW_UP)
        click {
            this.visible = false
        }
    }

    override var visible: Boolean = false
        get() = super.visible
        set(value) {
            field = value
            if (value) {
                push()
            } else {
                pop()
            }
        }

    private val scrollerThickness: Float = if (!showScroller) 0f else scrollerThickness

    init {
        transform.width = width?.also { transform.fixedWidth = true } ?: 0f
        transform.height = height?.also { transform.fixedHeight = true } ?: 0f
        padding?.let { padding(it) }
    }

    var amount: Float
        get() {
            return if (this::scrollerBar.isInitialized) {
                scrollerBar.amount
            } else 0f
        }
        set(value) {
            if (this::scrollerBar.isInitialized) {
                scrollerBar.amount = value
            }
        }

    override fun init() {
        for (e in subElements) e.init.invoke()
        val contentSize = Arrangement.Vertical.contentSize(layout.alignRects(subElements, Arrangement.Vertical))
        if (!this::scrollerBar.isInitialized) {
            scrollerBar = scroller(
                transform.height - padding.height,
                scrollerThickness,
                { (layout.alignRects(subElements, Arrangement.Vertical).minOf { it.height } / 2f) },
                { (contentSize.height - contentRect(false).height).coerceAtLeast(0f) },
                { (contentRect(false).height / contentSize.height).clamp(0f..1f) },
                Arrangement.Vertical
            ) {
                fixed = true
                visible = showScroller
            }
            scrollerBar.amountReceiver = {
                arrange()
            }
        }
        arrange()
        if (this::scrollerBar.isInitialized) {
            scrollerBar.transform.worldX = transform.worldRight - scrollerThickness - padding.right / 2
            scrollerBar.transform.y = padding.top + arrow.transform.height
        }
        tip?.init?.invoke()
    }

    override var layout: Layout = object : Layout {

        override var spacing: Float = 0f

        override val elementContainer: () -> ElementContainer
            get() = { this@DropTip }

        override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size<Float>? {
            val alignElements = elements.filter { !it.fixed }
            if (alignElements.isEmpty()) return null

            val alignRects = alignRects(alignElements, Arrangement.Vertical)

            val alignment = PlanarAlignment.TopLeft(Arrangement.Vertical)


            val container = elementContainer()
            val size = Arrangement.Vertical.contentSize(alignRects)
            val contentRect = when {
                //固定高度和宽度
                container.transform.fixedWidth && container.transform.fixedHeight  -> {
                    container.contentRect(false)
                }
                //固定宽度 不固定高度
                container.transform.fixedWidth && !container.transform.fixedHeight -> {
                    rect(container.contentRect(false).position, container.transform.width, size.height)
                }
                //不固定宽度 固定高度
                !container.transform.fixedWidth && container.transform.fixedHeight -> {
                    rect(container.contentRect(false).position, size.width, container.transform.height)
                }
                //不固定宽度 不固定高度
                else                                                               -> {
                    rect(container.contentRect(false).position, size)
                }
            }
            alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
                val element = alignElements[index]
                val v = vector3f + (Vector3f(0f, -amount, 0f))
                element.transform.translateTo(v + Vector3f(element.margin.left, element.margin.top))
                element.visible = element.transform.inRect(contentRect, false)
            }
            return Size.create(contentRect.width + padding.width + this@DropTip.scrollerThickness, contentRect.height + padding.height)
        }

    }
        @Deprecated("Do not set the layout value of DropTip") set(@Suppress("UNUSED_PARAMETER") value) {
            throw NotImplementedError("Do not set the layout value of DropTip")
        }

    override fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>> {
        val top = if (isWorld) transform.worldTop + padding.top else padding.top

        val bottom =
            if (isWorld) transform.worldBottom - padding.bottom + 0f
            else transform.height - padding.bottom + 0f

        val left = if (isWorld) transform.worldLeft + padding.left else padding.left

        val right =
            if (isWorld) transform.worldRight - padding.right - scrollerThickness
            else transform.width - padding.right - scrollerThickness

        return rect(
            vertex(left, top, if (isWorld) transform.worldZ else transform.z), right - left, bottom - top
        )
    }

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        if (!mouseHover()) return NextAction.Continue
        return super.onMouseClick(mouseX, mouseY, button)
    }

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
        mouseHover {
            if (!scrollerBar.mouseHover()) {
                scrollerBar.amount -= scrollerBar.amountStep() * amount
            }
        }
        return super.onMouseScrolling(mouseX, mouseY, amount)
    }

    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderBackground.invoke(renderContext)
        renderContext.scissor(super.contentRect(true)) {
            renderElements.filter { it != scrollerBar || !it.fixed }.forEach { it.render(renderContext) }
        }
        fixedElements.forEach { it.render(renderContext) }
        scrollerBar.render(renderContext)
        renderOverlay.invoke(renderContext)
    }

    override fun onRenderBackground(renderContext: RenderContext) {
        renderTexture(renderContext.matrixStack, transform, WidgetTextures.DROP_MENU_EXPEND_BACKGROUND)
    }


}