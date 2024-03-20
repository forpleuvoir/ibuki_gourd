package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.render.MutableSize
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f

abstract class AbstractElementContainer : Element {

    override var init: () -> Unit = ::init

    final override val transform = Transform()

    private var contentBoxCache: Box? = null

    private var contentWorldBoxCache: Box? = null

    init {
        transform.subscribeChange(
            { _, _ ->
                refreshContentBoxCache()
            }, { _, _ ->
                refreshContentBoxCache()
            }
        )
    }

    override var margin: Margin = Margin()

    override var padding: Padding = Margin()

    protected val subElements = ArrayList<Element>()

    /**
     * 参与布局排列的元素
     */
    open val layoutElements: List<Element> get() = subElements.filter { !it.fixed }

    override val elements: List<Element> get() = subElements.filter { it != this.tip }

    override val renderElements get() = subElements.filter { it != this.tip && it.visible }.sortedBy { it.renderPriority }

    override val fixedElements get() = subElements.filter { it != this.tip && it.fixed }.sortedBy { it.renderPriority }

    override val handleElements get() = subElements.filter { it != this.tip && it.active }

    override fun clearElements(predicate: (Element) -> Boolean) {
        subElements.removeAll(predicate)
    }

    override fun init() {
        for (e in subElements) e.init.invoke()
    }

    override fun layout() {

    }

    override fun measure(elementMeasureDimension: ElementMeasureDimension): ElementMeasureDimension {
        return elementMeasureDimension
    }

    var measuredDimension: MutableSize<Float> = MutableSize(0f, 0f)

    fun refreshContentBoxCache() {
        contentBoxCache = null
        contentBoxCache = contentBox(false)
        contentWorldBoxCache = null
        contentWorldBoxCache = contentBox(true)
    }

    override fun contentBox(isWorld: Boolean): Box {
        if (isWorld) contentWorldBoxCache?.let { return it } else contentBoxCache?.let { return it }
        val top = if (isWorld) transform.worldTop + padding.top else padding.top
        val bottom = if (isWorld) transform.worldBottom - padding.bottom else transform.height - padding.bottom
        val left = if (isWorld) transform.worldLeft + padding.left else padding.left
        val right = if (isWorld) transform.worldRight - padding.right else transform.width - padding.right
        return Box(Vector2f(left, top), right - left, bottom - top)
    }

    override fun <T : Element> addElement(element: T): T {
        if (subElements.contains(element)) return element
        subElements.add(element)
        element.transform.parent = { this.transform }
        element.parent = { this }
        screen().eventBus.subscribe(element)
        return element
    }

    override fun preElement(element: Element): Element? {
        val indexOf = subElements.indexOf(element)
        if (indexOf < 1) return null
        return subElements[indexOf - 1]
    }

    override fun nextElement(element: Element): Element? {
        val indexOf = subElements.indexOf(element)
        if (indexOf != -1 && indexOf < subElements.size - 1) return null
        return subElements[indexOf + 1]
    }

    override fun elementIndexOf(element: Element): Int = subElements.indexOf(element)

    override fun removeElement(element: Element): Boolean {
        element.transform.parent = { null }
        element.parent = { Element }
        return subElements.remove(element)
    }

    override fun removeElement(index: Int) {
        subElements.removeAt(index).apply {
            transform.parent = { null }
            parent = { Element }
        }
    }
}