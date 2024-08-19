package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.layout.HorizontalListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.VerticalListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.WidgetModifier

@GuiDslMark
interface ListLayoutScope<T : Alignment.Linear> {


    /**
     * 填充至最大空间 如果为垂直布局则填充宽度,为水平布局则填充高度
     * @receiver Modifier
     * @return Modifier
     */
    fun Modifier.fill(fill: Boolean = true): Modifier

    /**
     * 当前组件的对齐方式
     *
     * 在Vertical中为水平对齐[Alignment.Horizontal],Horizontal中为垂直对齐[Alignment.Vertical]
     *
     * @receiver Modifier
     * @param alignment Linear
     * @return Modifier
     */
    fun Modifier.align(alignment: T): Modifier

}

interface HorizontalListLayoutScope : ListLayoutScope<Alignment.Vertical> {

    override fun Modifier.fill(fill: Boolean) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is HorizontalListLayout.WrappedHorizontalListLayoutData
                -> it.parentData = parentData.copy(fill = fill)

            null
                -> it.parentData = HorizontalListLayout.WrappedHorizontalListLayoutData(fill = fill)
        }
    }

    override fun Modifier.align(alignment: Alignment.Vertical) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is HorizontalListLayout.WrappedHorizontalListLayoutData
                -> it.parentData = parentData.copy(alignment = alignment)

            null
                -> it.parentData = HorizontalListLayout.WrappedHorizontalListLayoutData(alignment = alignment)
        }
    }

}

interface VerticalListLayoutScope : ListLayoutScope<Alignment.Horizontal> {

    override fun Modifier.fill(fill: Boolean) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is VerticalListLayout.WrappedVerticalListLayoutData
                -> it.parentData = parentData.copy(fill = fill)

            null
                -> it.parentData = VerticalListLayout.WrappedVerticalListLayoutData(fill = fill)
        }
    }

    override fun Modifier.align(alignment: Alignment.Horizontal) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is VerticalListLayout.WrappedVerticalListLayoutData
                -> it.parentData = parentData.copy(alignment = alignment)

            null
                -> it.parentData = VerticalListLayout.WrappedVerticalListLayoutData(alignment = alignment)
        }
    }

}