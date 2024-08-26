package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowListLayout
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

interface ColumnListLayoutScope : ListLayoutScope<Alignment.Vertical> {

    override fun Modifier.fill(fill: Boolean) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnListLayout.WrappedColumnListLayoutData
                -> it.parentData = parentData.copy(fill = fill)

            null
                -> it.parentData = ColumnListLayout.WrappedColumnListLayoutData(fill = fill)
        }
    }

    override fun Modifier.align(alignment: Alignment.Vertical) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnListLayout.WrappedColumnListLayoutData
                -> it.parentData = parentData.copy(alignment = alignment)

            null
                -> it.parentData = ColumnListLayout.WrappedColumnListLayoutData(alignment = alignment)
        }
    }

}

interface RowListLayoutScope : ListLayoutScope<Alignment.Horizontal> {

    override fun Modifier.fill(fill: Boolean) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is RowListLayout.WrappedRowListLayoutData
                -> it.parentData = parentData.copy(fill = fill)

            null
                -> it.parentData = RowListLayout.WrappedRowListLayoutData(fill = fill)
        }
    }

    override fun Modifier.align(alignment: Alignment.Horizontal) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is RowListLayout.WrappedRowListLayoutData
                -> it.parentData = parentData.copy(alignment = alignment)

            null
                -> it.parentData = RowListLayout.WrappedRowListLayoutData(alignment = alignment)
        }
    }

}