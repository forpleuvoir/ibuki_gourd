package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.render.base.MutableSize
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rect
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rectangle

abstract class AbstractElementContainer : Element {

    override var init: () -> Unit = ::init

    final override val transform = Transform()

    override var margin: Margin = Margin()
        protected set

    override var padding: Padding = Margin()
        protected set

    protected val subElements = ArrayList<Element>()

    /**
     * 参与布局排列的元素
     */
    open val layoutElements: List<Element> get() = subElements.filter { !it.fixed }

    override val elements: List<Element> get() = subElements.filter { it != this.tip }

    override val renderElements get() = subElements.filter { it != this.tip && it.visible }.sortedBy { it.renderPriority }

    override val fixedElements get() = subElements.filter { it != this.tip && it.fixed }.sortedBy { it.renderPriority }

    override val handleElements get() = subElements.filter { it != this.tip && it.active }.sortedByDescending { it.priority }

    override fun clearElements(predicate: (Element) -> Boolean) {
        subElements.removeAll(predicate)
    }

    override fun init() {
        for (e in subElements) e.init.invoke()
    }

    override fun layout() {

    }

    override fun measure(elementMeasureDimension: ElementMeasureDimension): ElementMeasureDimension {
        val (mWidth, mHeight) = elementMeasureDimension
        when (width) {
            is Fixed           -> measuredDimension.width = (width as Fixed).value
            FillRemainingSpace -> measuredDimension.width = mWidth.value
            MatchParent        -> measuredDimension.width = mWidth.value
            is Weight          -> measuredDimension.width = mWidth.value
            is Percentage      -> measuredDimension.width = mWidth.value * (width as Percentage).value
            is WrapContent     -> measuredDimension.width = mWidth.value
        }

        when (height) {
            is Fixed           -> measuredDimension.height = (height as Fixed).value
            FillRemainingSpace -> measuredDimension.height = mHeight.value
            MatchParent        -> measuredDimension.height = mHeight.value
            is Weight          -> measuredDimension.height = mHeight.value
            is Percentage      -> measuredDimension.height = mHeight.value * (height as Percentage).value
            is WrapContent     -> measuredDimension.height = mHeight.value
        }
        var (widthRemainingSpace, heightRemainingSpace) = 0f to 0f
        subElements.forEach {
            when (it.width) {
                FillRemainingSpace ->
                is Fixed           -> TODO()
                MatchParent        -> TODO()
                is Percentage      -> TODO()
                is Weight          -> TODO()
                is WrapContent     -> TODO()
            }
        }
    }

    var measuredDimension: MutableSize<Float> = MutableSize.of(0f, 0f)


    final override fun margin(margin: Number) {
        this.margin = Margin(margin, margin)
    }

    final override fun margin(margin: Margin) {
        this.margin = margin
    }

    final override fun margin(left: Number, right: Number, top: Number, bottom: Number) {
        this.margin = Margin(left, right, top, bottom)
    }

    final override fun padding(padding: Number) {
        this.padding = Margin(padding, padding)
    }

    final override fun padding(padding: Padding) {
        this.padding = padding
    }

    final override fun padding(left: Number, right: Number, top: Number, bottom: Number) {
        this.padding = Padding(left, right, top, bottom)
    }

    override fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>> {
        //TODO("耗时方法，待优化")
        val top = if (isWorld) transform.worldTop + padding.top else padding.top
        val bottom = if (isWorld) transform.worldBottom - padding.bottom else transform.height - padding.bottom
        val left = if (isWorld) transform.worldLeft + padding.left else padding.left
        val right = if (isWorld) transform.worldRight - padding.right else transform.width - padding.right
        return Rect(
            vertex(left, top, if (isWorld) transform.worldZ else transform.z), right - left, bottom - top
        )
    }

    override fun <T : Element> addElement(element: T): T {
        if (subElements.contains(element)) return element
        subElements.add(element)
        element.transform.parent = { this.transform }
        element.parent = { this }
        element.priority = this.priority + 1
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
        element.parent = { Element.EMPTY }
        return subElements.remove(element)
    }

    override fun removeElement(index: Int) {
        subElements.removeAt(index).apply {
            transform.parent = { null }
            parent = { Element.EMPTY }
        }
    }
}