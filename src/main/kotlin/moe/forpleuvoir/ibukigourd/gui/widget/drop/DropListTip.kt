package moe.forpleuvoir.ibukigourd.gui.widget.drop

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.button.ButtonWidget
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
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.rectBatchRender
import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.clamp

class DropListTip(
    val dropMenu: DropMenuWidget,
    private val maxHeight: Float? = null,
    padding: Margin? = Padding(2),
    val showScroller: Boolean = true,
    scrollerThickness: Float? = null
) : Tip({ dropMenu }, { dropMenu.screen() }) {

    override var layout: Layout = object : Layout {

        override var spacing: Float = 1f

        override val elementContainer: () -> ElementContainer
            get() = { this@DropListTip }

        override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size<Float>? {
            val alignElements = elements.filter { !it.fixed }
            if (alignElements.isEmpty()) return null

            val alignRects = alignRects(alignElements, Arrangement.Vertical)

            val alignment = PlanarAlignment.TopLeft(Arrangement.Vertical)


            val container = elementContainer()
            val containerContentRect = container.contentRect(false)

            val size = Arrangement.Vertical.contentSize(alignRects)
            val contentRect = when {
                //固定高度和宽度
                container.transform.fixedWidth && container.transform.fixedHeight  -> {
                    containerContentRect
                }
                //固定宽度 不固定高度
                container.transform.fixedWidth && !container.transform.fixedHeight -> {
                    rect(containerContentRect.position, containerContentRect.width, size.height)
                }
                //不固定宽度 固定高度
                !container.transform.fixedWidth && container.transform.fixedHeight -> {
                    rect(containerContentRect.position, size.width, containerContentRect.height)
                }
                //不固定宽度 不固定高度
                else                                                               -> {
                    rect(containerContentRect.position, size)
                }
            }
            alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
                val element = alignElements[index]
                val v = vector3f + (Vector3f(0f, -amount, 0f))
                element.transform.translateTo(v + Vector3f(element.margin.left, element.margin.top))
                element.visible = element.transform.inRect(contentRect, false)
            }
            return Size.create(contentRect.width + padding.width, contentRect.height + padding.height)
        }

    }
        @Deprecated("Do not set the layout value of DropTip") set(@Suppress("UNUSED_PARAMETER") value) {
            throw NotImplementedError("Do not set the layout value of DropTip")
        }

    lateinit var scrollerBar: Scroller
        private set

    val arrow: ButtonWidget = flatButton {
        fixed = true
        transform.fixedWidth = true
        transform.fixedHeight = true
        icon(WidgetTextures.DROP_MENU_ARROW_UP)
        click {
            dropMenu.expend = false
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

    init {
        transform.parent = { dropMenu.transform }
        padding?.let(::padding)
    }

    private val scrollerThickness: Float = if (!showScroller) 0f else scrollerThickness ?: (dropMenu.arrow.transform.width - this.padding.width)


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
                transform.height - parent().transform.height - spacing,
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
            scrollerBar.transform.x = arrow.transform.x + arrow.transform.halfWidth - scrollerBar.transform.halfWidth
            scrollerBar.transform.y = arrow.transform.bottom + spacing
        }
        tip?.init?.invoke()
    }

    override fun arrange() {
        layout.arrange(this.subElements, this.margin, this.padding)?.let { size ->
            if (!transform.fixedHeight) {
                if (maxHeight != null && size.height >= maxHeight) {
                    this.transform.height = maxHeight
                } else {
                    this.transform.height = size.height
                }
            }
            if (!transform.fixedWidth) {
                this.transform.width = size.width + dropMenu.arrow.transform.width + spacing
            }
            if (!transform.fixedHeight || !transform.fixedWidth) parent().arrange()
        }
        arrow.transform.width = dropMenu.arrow.transform.width
        arrow.transform.height = dropMenu.arrow.transform.height

        arrow.transform.y = dropMenu.arrow.transform.y
        arrow.transform.x = dropMenu.arrow.transform.x

        arrow.layout.arrange(arrow.elements, arrow.margin, arrow.padding)
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

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
        if (!dropMenu.expend) return NextAction.Continue
        if (!mouseHover()) return NextAction.Continue
        super.onMouseMove(mouseX, mouseY).ifCancel { return NextAction.Cancel }
        if (mouseHover()) return NextAction.Cancel
        return NextAction.Continue
    }

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        if (!mouseHover() && dropMenu.expend) {
            dropMenu.expend = false
        }
        if (!mouseHover()) return NextAction.Continue
        super.onMouseClick(mouseX, mouseY, button).ifCancel { return NextAction.Cancel }
        if (mouseHover()) {
            return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
        if (!dropMenu.expend) return NextAction.Continue
        super.onMouseScrolling(mouseX, mouseY, amount).ifCancel { return NextAction.Cancel }
        mouseHover {
            if (!scrollerBar.mouseHover()) {
                scrollerBar.amount -= scrollerBar.amountStep() * amount
            }
            return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderContext.postRender {
            renderBackground.invoke(renderContext)
            renderContext.scissor(super.contentRect(true)) {
                renderElements.filter { it != scrollerBar || !it.fixed }.forEach { it.render(renderContext) }
            }
            fixedElements.forEach { it.render(renderContext) }
            scrollerBar.render(renderContext)
            renderContext.scissor(super.contentRect(true)) {
                renderOverlay.invoke(renderContext)
            }
        }
    }

    override fun onRenderBackground(renderContext: RenderContext) {
        renderTexture(renderContext.matrixStack, transform, WidgetTextures.DROP_MENU_EXPEND_BACKGROUND)
    }

    override fun onRenderOverlay(renderContext: RenderContext) {
        rectBatchRender {
            renderElements.filter { it != arrow && it != scrollerBar }.let { list ->
                val maxWidth = if (dropMenu.transform.fixedWidth) dropMenu.transform.width else list.maxOf { it.transform.width }
                //箭头下的横线
                renderRect(
                    renderContext.matrixStack,
                    rect(
                        arrow.transform.worldX,
                        arrow.transform.worldBottom,
                        transform.worldZ,
                        arrow.transform.width,
                        spacing
                    ),
                    Colors.GRAY.alpha(0.2f)
                )
                for ((index, element) in list.withIndex()) {
                    //选中颜色
                    dropMenu.selectedColor?.let {
                        if (element.mouseHover())
                            renderRect(
                                renderContext.matrixStack,
                                rect(element.transform.worldX, element.transform.worldTop, transform.worldZ, maxWidth, element.transform.height),
                                it()
                            )
                    }
                    //元素下的横线
                    if (index != list.lastIndex) {
                        renderRect(
                            renderContext.matrixStack,
                            rect(element.transform.worldX, element.transform.worldBottom, transform.worldZ, maxWidth, spacing),
                            Colors.GRAY.alpha(0.2f)
                        )
                    }
                }
                //绘制竖线
                renderRect(
                    renderContext.matrixStack,
                    rect(arrow.transform.worldX - spacing, transform.worldTop + padding.top, transform.worldZ, spacing, transform.height - padding.height),
                    Colors.GRAY.alpha(0.2f)
                )
            }
        }
    }

}

fun DropMenuWidget.itemList(
    maxHeight: Float? = null,
    padding: Margin? = Padding(2),
    showScroller: Boolean = true,
    scrollerThickness: Float? = null,
    scope: ElementContainer.() -> Unit
) {
    this.tip = DropListTip(this, maxHeight, padding, showScroller, scrollerThickness)
    this.tip!!.scope()
}