package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LIST_BACKGROUND
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
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
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class ListLayout(
    width: Float? = null,
    height: Float? = null,
    padding: Margin? = Margin(6),
    val showScroller: Boolean = true,
    val showBackground: Boolean = true,
    val arrangement: Arrangement = Arrangement.Vertical,
    scrollerThickness: Float = 10f,
) : AbstractElement() {

    lateinit var scrollerBar: Scroller
        private set

    private val scrollerThickness: Float = if (!showScroller) 0f else scrollerThickness

    init {
        transform.width = width?.also { transform.fixedWidth = true } ?: 0f
        transform.height = height?.also { transform.fixedHeight = true } ?: 0f
        padding?.let(::padding)
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
        val contentSize = arrangement.contentSize(layout.alignRects(subElements, arrangement))
        if (!this::scrollerBar.isInitialized) {
            scrollerBar = scroller(
                arrangement.switch({ transform.height - padding.height }, { transform.width - padding.width }),
                scrollerThickness,
                { (layout.alignRects(subElements, arrangement).minOf { r -> arrangement.switch({ r.height }, { r.width }) } / 2f) },
                {
                    arrangement.switch(
                        { contentSize.height - contentRect(false).height },
                        { contentSize.width - contentRect(false).width }
                    ).coerceAtLeast(0f)
                },
                {
                    arrangement.switch(
                        { contentRect(false).height / contentSize.height },
                        { contentRect(false).width / contentSize.width }
                    ).clamp(0f..1f)
                },
                arrangement
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
            arrangement.switch(
                {
                    scrollerBar.transform.worldX = transform.worldRight - scrollerThickness - padding.right / 2
                    scrollerBar.transform.y = padding.top
                }, {
                    scrollerBar.transform.worldY = transform.worldBottom - scrollerThickness - padding.bottom / 2
                    scrollerBar.transform.x = padding.left
                }
            )
        }
        tip?.init?.invoke()
    }

    override var layout: Layout = object : Layout {

        override var spacing: Float = 0f

        override val elementContainer: () -> ElementContainer
            get() = { this@ListLayout }

        override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size<Float>? {
            val alignElements = elements.filter { !it.fixed }
            if (alignElements.isEmpty()) return null

            val alignRects = alignRects(alignElements, arrangement)

            val alignment = arrangement.switch(
                {
                    PlanarAlignment.TopLeft(arrangement)
                }, {
                    PlanarAlignment.CenterLeft(arrangement)
                }
            )

            val container = elementContainer()
            val size = arrangement.contentSize(alignRects)
            val contentRect = when {
                //固定高度和宽度
                container.transform.fixedWidth && container.transform.fixedHeight -> {
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
                else -> {
                    rect(container.contentRect(false).position, size)
                }
            }
            alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
                val element = alignElements[index]
                val v = vector3f + arrangement.switch(
                    { Vector3f(0f, -amount, 0f) },
                    { Vector3f(-amount, 0f, 0f) }
                )
                element.transform.translateTo(v + Vector3f(element.margin.left, element.margin.top))
                element.visible = element.transform.inRect(contentRect, false)
            }
            return arrangement.switch(
                {
                    Size.create(contentRect.width + padding.width + this@ListLayout.scrollerThickness, contentRect.height + padding.height)
                }, {
                    Size.create(contentRect.width + padding.width, contentRect.height + padding.height + this@ListLayout.scrollerThickness)
                }
            )
        }

    }
        @Deprecated("Do not set the layout value of ListLayout") set(@Suppress("UNUSED_PARAMETER") value) {
            throw NotImplementedError("Do not set the layout value of ListLayout")
        }

    override fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>> {
        val top = if (isWorld) transform.worldTop + padding.top else padding.top

        val bottom =
            if (isWorld) transform.worldBottom - padding.bottom + arrangement.switch({ 0f }, { -scrollerThickness })
            else transform.height - padding.bottom + arrangement.switch({ 0f }, { -scrollerThickness })

        val left = if (isWorld) transform.worldLeft + padding.left else padding.left

        val right =
            if (isWorld) transform.worldRight - padding.right + arrangement.switch({ -scrollerThickness }, { 0f })
            else transform.width - padding.right + arrangement.switch({ -scrollerThickness }, { 0f })

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
        if (showBackground) renderTexture(renderContext.matrixStack, this.transform, LIST_BACKGROUND)
    }

}

@OptIn(ExperimentalContracts::class)
fun ElementContainer.list(
    width: Float? = null,
    height: Float? = null,
    arrangement: Arrangement = Arrangement.Vertical,
    padding: Margin? = Margin(6),
    showScroller: Boolean = true,
    showBackground: Boolean = true,
    scrollerThickness: Float = 10f,
    scope: ListLayout.() -> Unit = {}
): ListLayout {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(ListLayout(width, height, padding, showScroller, showBackground, arrangement, scrollerThickness).apply(scope))
}