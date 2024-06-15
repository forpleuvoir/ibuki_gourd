package moe.forpleuvoir.ibukigourd.gui.base.layout
//
//import moe.forpleuvoir.ibukigourd.gui.base.Margin
//import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
//import moe.forpleuvoir.ibukigourd.gui.base.element.Element
//import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
//import moe.forpleuvoir.ibukigourd.gui.base.event.MouseMoveEvent
//import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
//import moe.forpleuvoir.ibukigourd.gui.base.event.MouseScrollEvent
//import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
//import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
//import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
//import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.LIST_BACKGROUND
//import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
//import moe.forpleuvoir.ibukigourd.gui.widget.scroller
//import moe.forpleuvoir.ibukigourd.render.helper.renderTexture
//import moe.forpleuvoir.ibukigourd.util.NextAction
//import moe.forpleuvoir.nebula.common.util.clamp
//
//open class ListLayout(
//    modifier: Modifier = Modifier,
//    val showScroller: Boolean = true,
//    val showBackground: Boolean = true,
//    val orientation: Orientation = Orientation.Vertical,
//    scrollerThickness: Float = 10f,
//) : AbstractElement(modifier) {
//
//    lateinit var scrollerBar: Scroller
//        private set
//
//    private val scrollerThickness: Float = if (!showScroller) 0f else scrollerThickness
//
//
//    var amount: Float
//        get() {
//            return if (this::scrollerBar.isInitialized) {
//                scrollerBar.amount
//            } else 0f
//        }
//        set(value) {
//            if (this::scrollerBar.isInitialized) {
//                scrollerBar.amount = value
//            }
//        }
//
//    override fun init() {
//        for (e in subElements) e.init.invoke()
//        val contentSize = orientation.contentSize(layout.alignRects(subElements, orientation))
//        if (!this::scrollerBar.isInitialized) {
//            scrollerBar = scroller(
//                orientation.peek({ transform.height - padding.height }, { transform.width - padding.width }),
//                scrollerThickness,
//                { (layout.alignRects(subElements, orientation).minOf { r -> orientation.peek(r.height, r.width) } / 2f) },
//                {
//                    orientation.peek(
//                        contentSize.height - contentBox(false).height,
//                        contentSize.width - contentBox(false).width
//                    ).coerceAtLeast(0f)
//                },
//                {
//                    orientation.peek(
//                        contentBox(false).height / contentSize.height,
//                        contentBox(false).width / contentSize.width
//                    ).clamp(0f..1f)
//                },
//                orientation
//            ) {
//                fixed = true
//                visible = showScroller
//            }
//            scrollerBar.amountReceiver = {
//                arrange()
//            }
//        }
//        arrange()
//        if (this::scrollerBar.isInitialized) {
//            orientation.peek(
//                {
//                    scrollerBar.transform.worldX = transform.worldRight - scrollerThickness - padding.right / 2
//                    scrollerBar.transform.y = padding.top
//                }, {
//                    scrollerBar.transform.worldY = transform.worldBottom - scrollerThickness - padding.bottom / 2
//                    scrollerBar.transform.x = padding.left
//                }
//            )
//        }
//        tip?.init?.invoke()
//    }
//
//    open var onElementTranslate: (Element, Vector3<Float>) -> Unit = { e, v ->
//        e.transform.translateTo(v)
//    }
//
//    override val layout: Layout = object : Layout {
//
//        override var spacing: Float = 0f
//
//        override val elementContainer: () -> ElementContainer
//            get() = { this@ListLayout }
//
//        override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size<Float>? {
//            val alignElements = elements.filter { !it.fixed }
//            if (alignElements.isEmpty()) return null
//
//            val alignRects = alignRects(alignElements, orientation)
//
//            val alignment = orientation.peek(PlanarAlignment.TopLeft(orientation), PlanarAlignment.CenterLeft(orientation))
//
//            val container = elementContainer()
//            val containerContentRect = container.contentBox(false)
//
//            val size = orientation.contentSize(alignRects)
//            val contentRect = when {
//                //固定高度和宽度
//                container.transform.fixedWidth && container.transform.fixedHeight -> {
//                    containerContentRect
//                }
//                //固定宽度 不固定高度
//                container.transform.fixedWidth && !container.transform.fixedHeight -> {
//                    Rect(containerContentRect.position, containerContentRect.width, size.height)
//                }
//                //不固定宽度 固定高度
//                !container.transform.fixedWidth && container.transform.fixedHeight -> {
//                    Rect(containerContentRect.position, size.width, containerContentRect.height)
//                }
//                //不固定宽度 不固定高度
//                else -> {
//                    Rect(containerContentRect.position, size)
//                }
//            }
//            alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
//                val element = alignElements[index]
//                val v = vector3f + orientation.peek(Vector3f(0f, -amount, 0f), Vector3f(-amount, 0f, 0f))
//                onElementTranslate(element, v + Vector3f(element.margin.left, element.margin.top))
////                element.transform.translateTo(v + Vector3f(element.margin.left, element.margin.top))
//                element.visible = element.transform.inBox(contentRect, false)
//            }
//            return orientation.peek(
//                Size.of(contentRect.width + padding.width + this@ListLayout.scrollerThickness, contentRect.height + padding.height),
//                Size.of(contentRect.width + padding.width, contentRect.height + padding.height + this@ListLayout.scrollerThickness)
//            )
//        }
//
//    }
//
//    override fun contentBox(isWorld: Boolean): Rectangle {
//        val top = if (isWorld) transform.worldTop + padding.top else padding.top
//
//        val bottom =
//            if (isWorld) transform.worldBottom - padding.bottom + orientation.peek(0f, -scrollerThickness)
//            else transform.height - padding.bottom + orientation.peek(0f, -scrollerThickness)
//
//        val left = if (isWorld) transform.worldLeft + padding.left else padding.left
//
//        val right =
//            if (isWorld) transform.worldRight - padding.right + orientation.peek(-scrollerThickness, 0f)
//            else transform.width - padding.right + orientation.peek(-scrollerThickness, 0f)
//
//        return Rect(
//            vertex(left, top, if (isWorld) transform.worldZ else transform.z), right - left, bottom - top
//        )
//    }
//
//    override fun onMouseMove(event: MouseMoveEvent): NextAction {
//        if (!mouseHover()) return NextAction.Continue
//        return super.onMouseMove()
//    }
//
//    override fun onMouseClick(event: MousePressEvent): NextAction {
//        if (!mouseHover()) return NextAction.Continue
//        return super.onMouseClick()
//    }
//
//    override fun onMouseScrolling(event: MouseScrollEvent): NextAction {
//        mouseHover {
//            if (!scrollerBar.mouseHover()) {
//                scrollerBar.amount -= scrollerBar.amountStep() * amount
//            }
//        }
//        return super.onMouseScrolling()
//    }
//
//    override fun onRender(renderContext: RenderContext) {
//        if (!visible) return
//        renderBackground.invoke(renderContext)
//        renderContext.scissor(super.contentBox(true)) {
//            renderElements.filter { it != scrollerBar || !it.fixed }.forEach { it.render(renderContext) }
//        }
//        fixedElements.forEach { it.render(renderContext) }
//        scrollerBar.render(renderContext)
//        renderOverlay.invoke(renderContext)
//    }
//
//    override fun onRenderBackground(renderContext: RenderContext) {
//        if (showBackground) renderTexture(renderContext.matrixStack, this.transform, LIST_BACKGROUND)
//    }
//
//}
