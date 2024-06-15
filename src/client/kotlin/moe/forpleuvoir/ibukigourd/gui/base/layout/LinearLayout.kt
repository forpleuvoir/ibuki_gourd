@file:Suppress("DuplicatedCode")

package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.gui.base.element.MeasureSpec.Mode
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout.Gravity.*
import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.render.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.Vector2f

@Suppress("MemberVisibilityCanBePrivate")
open class LinearLayout(
    val orientation: Orientation,
    val alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    var spacing: Float = 0f
) : Layout {

    enum class Gravity : LayoutData {
        Start, Center, End;
    }

    private fun getGravity(element: Element): Gravity {
        return element.layoutData[Gravity::class] as Gravity? ?: Center
    }

    private fun layoutBoxes(children: List<Element>): List<Box> {
        return children.mapIndexed { index, child ->
            var spacing = spacing
            if (children.lastIndex == index) {
                spacing = 0f
            }
            val size: Size<Float> = orientation.peek(
                Size(child.transform.width + child.margin.width, child.transform.height + child.margin.height + spacing),
                Size(child.transform.width + child.margin.width + spacing, child.transform.height + child.margin.height)
            )
            Box(Vector2f(0f, 0f), size)
        }
    }

    override fun Element.layout() {
        this@LinearLayout.apply {
            if (orientation == Orientation.Vertical) {
                this@layout.onVerticalLayout()
            } else {
                this@layout.onHorizontalLayout()
            }
        }
    }

    private fun Element.onVerticalLayout() {
        val children = layoutElements
        if (children.isEmpty()) return
        val contentBox = contentBox(false)
        val layoutBoxes = this@LinearLayout.layoutBoxes(children)
        this@LinearLayout.alignment(this@LinearLayout.orientation).align(contentBox, layoutBoxes).forEachIndexed { index, vector2fc ->
            val child = children[index]
            val gravity = this@LinearLayout.getGravity(child)
            val x = when (gravity) {
                Start  -> padding.left + child.margin.left
                Center -> transform.halfWidth - (child.margin.left + child.transform.halfWidth)
                End    -> transform.width - padding.right - child.transform.width - child.margin.right
            }
            child.transform.translate(x, vector2fc.y())
        }
    }

    private fun Element.onHorizontalLayout() {
        val children = layoutElements
        if (children.isEmpty()) return
        val contentBox = contentBox(false)
        val layoutBoxes = this@LinearLayout.layoutBoxes(children)
        this@LinearLayout.alignment(this@LinearLayout.orientation).align(contentBox, layoutBoxes).forEachIndexed { index, vector2fc ->
            val child = children[index]
            val gravity = this@LinearLayout.getGravity(child)
            val y = when (gravity) {
                Start  -> padding.top + child.margin.top
                Center -> transform.halfHeight - (child.margin.top + child.transform.halfHeight)
                End    -> transform.height - padding.bottom - child.transform.height - child.margin.bottom
            }
            child.transform.translate(vector2fc.x(), y)
        }
    }

    override fun Element.measureWidth(measureSpec: MeasureSpec): Float {
        val children = layoutElements
        if (children.isEmpty() && this.width is WrapContent) {
            setMeasureWidth((this.width as WrapContent).default ?: this.padding.width)
            return transform.width + margin.width
        }
        if (this@LinearLayout.orientation == Orientation.Vertical) {
            //如果不为-1,则表示为精确尺寸
            var width = -1f
            var maxWidth = -1f
            if (measureSpec.mode == Mode.EXACTLY) {
                width = measureSpec.value - margin.width
            } else {
                maxWidth = measureSpec.value - margin.width
            }
            val contentWidth = if (width != -1f) width - padding.width else -1f
            val contentMaxWidth = if (maxWidth != -1f) maxWidth - padding.width else -1f
            var maxChildWidth = -1f

            children.apply {
                filter { it.width is Fixed }.forEach { child ->
                    val cWidth = child.onMeasureWidth(MeasureSpec.exactly((child.width as Fixed).value))
                    if (maxChildWidth < cWidth) maxChildWidth = cWidth.coerceAtMost(contentMaxWidth)
                }
                filter { it.width is WrapContent }.forEach wrapContent@{ child ->
                    if (contentWidth != -1f) {
                        val cWidth = child.onMeasureWidth(MeasureSpec.atMost(contentWidth))
                        if (maxChildWidth < cWidth) maxChildWidth = cWidth
                        return@wrapContent
                    }
                    if (contentMaxWidth != -1f) {
                        val cWidth = child.onMeasureWidth(MeasureSpec.atMost(contentMaxWidth))
                        if (maxChildWidth < cWidth) maxChildWidth = cWidth
                        return@wrapContent
                    }
                    throw MeasureException("Unknown error")
                }
                filter { it.width is Proportion }.forEach proportion@{ child ->
                    if (contentWidth != -1f) {
                        val cWidth = child.onMeasureWidth(MeasureSpec.exactly(contentWidth * (child.width as Proportion).proportion))
                        if (maxChildWidth < cWidth) maxChildWidth = cWidth
                        return@proportion
                    }
                    if (maxChildWidth != -1f) {
                        child.onMeasureWidth(MeasureSpec.exactly(maxChildWidth * (child.width as Proportion).proportion))
                        return@proportion
                    }
                    if (contentMaxWidth != -1f) {
                        val cWidth = child.onMeasureWidth(MeasureSpec.exactly(contentMaxWidth * (child.width as Proportion).proportion))
                        if (maxChildWidth < cWidth) maxChildWidth = cWidth
                        return@proportion
                    }
                    throw MeasureException("Unknown error")
                }
                filter { it.width is MatchParent }.forEach matchParent@{ child ->
                    if (contentWidth != -1f) {
                        val cWidth = child.onMeasureWidth(MeasureSpec.exactly(contentWidth))
                        if (maxChildWidth < cWidth) maxChildWidth = cWidth
                        return@matchParent
                    }
                    if (maxChildWidth != -1f) {
                        child.onMeasureWidth(MeasureSpec.exactly(maxChildWidth))
                        return@matchParent
                    }
                    if (contentMaxWidth != -1f) {
                        val cWidth = child.onMeasureWidth(MeasureSpec.exactly(contentMaxWidth))
                        if (maxChildWidth < cWidth) maxChildWidth = cWidth
                        return@matchParent
                    }
                    throw MeasureException("Unknown error")
                }
            }
            if (width != -1f) {
                setMeasureWidth(width)
            } else if (maxChildWidth != -1f) {
                setMeasureWidth(maxChildWidth + padding.width)
            }
        } else {
            //如果不为-1,则表示为精确尺寸
            var width = -1f
            var maxWidth = -1f
            if (measureSpec.mode == Mode.EXACTLY) {
                width = measureSpec.value - margin.width
            } else {
                maxWidth = measureSpec.value - margin.width
            }
            val totalSpacing = children.lastIndex * this@LinearLayout.spacing
            val contentWidth = if (width != -1f) width - padding.width - totalSpacing else -1f
            val contentMaxWidth = if (maxWidth != -1f) maxWidth - padding.width - totalSpacing else -1f
            var remainingContentWidth = if (contentWidth != -1f) contentWidth else contentMaxWidth
            var usedContentWidth = 0f

            fun use(value: Float) {
                usedContentWidth += value
                remainingContentWidth -= value
                if (remainingContentWidth < 0) throw MeasureException("The space used exceeds the maximum usable space")
            }

            //总权重
            val weight = children.filter { it.width is WeightElementDimension }.sumOf { (it.width as WeightElementDimension).weight }
            children.apply {
                filter { it.width is Fixed }.forEach { child ->
                    use(child.onMeasureWidth(MeasureSpec.exactly((child.width as Fixed).value)))
                }
                filter { it.width is Proportion }.forEach proportion@{ child ->
                    if (contentWidth != -1f) {
                        use(child.onMeasureWidth(MeasureSpec.exactly(contentWidth * (child.width as Proportion).proportion)))
                        return@proportion
                    }
                    if (contentMaxWidth != -1f) {
                        use(child.onMeasureWidth(MeasureSpec.exactly(contentMaxWidth * (child.width as Proportion).proportion)))
                        return@proportion
                    }
                    throw MeasureException("Unknown error")
                }
                val widthOfEachWeight = remainingContentWidth / weight
                filter { it.width is WrapContent }.forEach wrapContent@{ child ->
                    use(child.onMeasureWidth(MeasureSpec.atMost((child.width as WrapContent).weight * widthOfEachWeight)))
                }
                filter { it.width is MatchParent }.forEach matchParent@{ child ->
                    use(child.onMeasureWidth(MeasureSpec.atMost((child.width as WrapContent).weight * widthOfEachWeight)))
                }
            }
            if (width != -1f) {
                setMeasureWidth(width)
            } else if (maxWidth != -1f) {
                setMeasureWidth(usedContentWidth + padding.width)
            }
        }
        return transform.width + margin.width
    }

    override fun Element.measureHeight(measureSpec: MeasureSpec): Float {
        val children = layoutElements
        if (children.isEmpty() && this.height is WrapContent) {
            setMeasureHeight((this.height as WrapContent).default ?: this.padding.height)
            return transform.height + margin.height
        }
        if (this@LinearLayout.orientation == Orientation.Horizontal) {
            //如果不为-1,则表示为精确尺寸
            var height = -1f
            var maxHeight = -1f
            if (measureSpec.mode == Mode.EXACTLY) {
                height = measureSpec.value - margin.height
            } else {
                maxHeight = measureSpec.value - margin.height
            }
            val contentHeight = if (height != -1f) height - padding.height else -1f
            val contentMaxHeight = if (maxHeight != -1f) maxHeight - padding.height else -1f
            var maxChildHeight = -1f

            children.apply {
                filter { it.height is Fixed }.forEach { child ->
                    val cHeight = child.onMeasureHeight(MeasureSpec.exactly((child.height as Fixed).value))
                    if (maxChildHeight < cHeight) maxChildHeight = cHeight.coerceAtMost(contentMaxHeight)
                }
                filter { it.height is WrapContent }.forEach wrapContent@{ child ->
                    if (contentHeight != -1f) {
                        val cHeight = child.onMeasureHeight(MeasureSpec.atMost(contentHeight))
                        if (maxChildHeight < cHeight) maxChildHeight = cHeight
                        return@wrapContent
                    }
                    if (contentMaxHeight != -1f) {
                        val cHeight = child.onMeasureHeight(MeasureSpec.atMost(contentMaxHeight))
                        if (maxChildHeight < cHeight) maxChildHeight = cHeight
                        return@wrapContent
                    }
                    throw MeasureException("Unknown error")
                }
                filter { it.height is Proportion }.forEach proportion@{ child ->
                    if (contentHeight != -1f) {
                        val cHeight = child.onMeasureHeight(MeasureSpec.exactly(contentHeight * (child.height as Proportion).proportion))
                        if (maxChildHeight < cHeight) maxChildHeight = cHeight
                        return@proportion
                    }
                    if (maxChildHeight != -1f) {
                        child.onMeasureHeight(MeasureSpec.exactly(maxChildHeight * (child.height as Proportion).proportion))
                        return@proportion
                    }
                    if (contentMaxHeight != -1f) {
                        val cHeight = child.onMeasureHeight(MeasureSpec.exactly(contentMaxHeight * (child.height as Proportion).proportion))
                        if (maxChildHeight < cHeight) maxChildHeight = cHeight
                        return@proportion
                    }
                    throw MeasureException("Unknown error")
                }
                filter { it.height is MatchParent }.forEach matchParent@{ child ->
                    if (contentHeight != -1f) {
                        val cHeight = child.onMeasureHeight(MeasureSpec.exactly(contentHeight))
                        if (maxChildHeight < cHeight) maxChildHeight = cHeight
                        return@matchParent
                    }
                    if (maxChildHeight != -1f) {
                        child.onMeasureHeight(MeasureSpec.exactly(maxChildHeight))
                        return@matchParent
                    }
                    if (contentMaxHeight != -1f) {
                        val cHeight = child.onMeasureHeight(MeasureSpec.exactly(contentMaxHeight))
                        if (maxChildHeight < cHeight) maxChildHeight = cHeight
                        return@matchParent
                    }
                    throw MeasureException("Unknown error")
                }
            }
            if (height != -1f) {
                setMeasureHeight(height)
            } else if (maxChildHeight != -1f) {
                setMeasureHeight(maxChildHeight + padding.height)
            }
        } else {
            //如果不为-1,则表示为精确尺寸
            var height = -1f
            var maxHeight = -1f
            if (measureSpec.mode == Mode.EXACTLY) {
                height = measureSpec.value - margin.height
            } else {
                maxHeight = measureSpec.value - margin.height
            }
            val totalSpacing = children.lastIndex * this@LinearLayout.spacing
            val contentHeight = if (height != -1f) height - padding.height - totalSpacing else -1f
            val contentMaxHeight = if (maxHeight != -1f) maxHeight - padding.height - totalSpacing else -1f
            var remainingContentHeight = if (contentHeight != -1f) contentHeight else contentMaxHeight
            var usedContentHeight = 0f

            fun use(value: Float) {
                usedContentHeight += value
                remainingContentHeight -= value
                if (remainingContentHeight < 0) throw MeasureException("The space used exceeds the maximum usable space")
            }

            //总权重
            val weight = children.filter { it.height is WeightElementDimension }.sumOf { (it.height as WeightElementDimension).weight }
            children.apply {
                filter { it.height is Fixed }.forEach { child ->
                    use(child.onMeasureHeight(MeasureSpec.exactly((child.height as Fixed).value)))
                }
                filter { it.height is Proportion }.forEach proportion@{ child ->
                    if (contentHeight != -1f) {
                        use(child.onMeasureHeight(MeasureSpec.exactly(contentHeight * (child.height as Proportion).proportion)))
                        return@proportion
                    }
                    if (contentMaxHeight != -1f) {
                        use(child.onMeasureHeight(MeasureSpec.exactly(contentMaxHeight * (child.height as Proportion).proportion)))
                        return@proportion
                    }
                    throw MeasureException("Unknown error")
                }
                val heightOfEachWeight = remainingContentHeight / weight
                filter { it.height is WrapContent }.forEach wrapContent@{ child ->
                    use(child.onMeasureHeight(MeasureSpec.atMost((child.height as WrapContent).weight * heightOfEachWeight)))
                }
                filter { it.height is MatchParent }.forEach matchParent@{ child ->
                    use(child.onMeasureHeight(MeasureSpec.atMost((child.height as WrapContent).weight * heightOfEachWeight)))
                }
            }
            if (height != -1f) {
                setMeasureHeight(height)
            } else if (maxHeight != -1f) {
                setMeasureHeight(usedContentHeight + padding.height)
            }
        }
        return transform.height + margin.height
    }
}
