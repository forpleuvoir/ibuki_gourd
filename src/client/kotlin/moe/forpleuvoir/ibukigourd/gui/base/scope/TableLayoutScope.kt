package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.layout.WrappedTableColumnEntryData
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.WidgetModifier
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer

interface TableLayoutScope {
}

fun interface TableLayoutColumnScope : GuiScope<WidgetContainer> {

    /**
     * 在当前Modifier中应用对齐方式
     *
     * 此函数通过添加一个WidgetModifier来实现对齐，它定义了如何在布局中对齐子元素
     *
     * @param alignment 对齐方式，表示子元素在布局中的对齐方式
     * @return 返回一个新的Modifier，其中包含了指定的对齐方式
     */
    fun Modifier.align(alignment: Alignment) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedTableColumnEntryData -> it.parentData = parentData.copy(alignment = alignment)
            null                           -> it.parentData = WrappedTableColumnEntryData(alignment = alignment)
        }
    }

    /**
     * 用于解除约束条件的 Modifier 方法。通过替换或创建新的 `parentData`，更改 `unlockConstraint` 值，
     * 用以定义当前组件是否解除约束条件。
     *
     * @param unlockConstraint 指定是否解除约束条件，默认为 `true`。
     */
    fun Modifier.unlockConstraint(unlockConstraint: Boolean = true) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedTableColumnEntryData
                -> it.parentData = parentData.copy(unlockConstraint = unlockConstraint)

            null
                -> it.parentData = WrappedTableColumnEntryData(unlockConstraint = unlockConstraint)
        }
    }

}