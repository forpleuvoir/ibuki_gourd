package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.util.FillMode
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.WidgetModifier

/**
 * 表示线性布局中的列表布局作用域
 * T 是对齐类型，继承自 Alignment.Linear
 */
@GuiDslMark
interface ListLayoutScope<T : Alignment.Linear> {

    /**
     * 填充至最大空间 如果为垂直布局则填充宽度,为水平布局则填充高度
     * @receiver Modifier
     * @return Modifier
     */
    fun Modifier.fillMode(fillMode: FillMode): Modifier

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

    /**
     * 将当前组件设置为填充兄弟组件的空间。
     *
     * 该方法会将当前组件的填充模式设置为 `FillMode.MatchSibling`，即使组件在垂直布局中能填充相同宽度或在水平布局中能填充相同高度。
     *
     * @return 修改后的 Modifier
     */
    fun Modifier.matchSibling(): Modifier = fillMode(FillMode.MatchSibling)

    /**
     * 将当前组件设置为填充父组件的空间。
     *
     * 该方法会将当前组件的填充模式设置为 `FillMode.MatchParent`，
     * 即使组件在垂直布局中填充整个宽度或在水平布局中填充整个高度。
     *
     * @return 修改后的 Modifier
     */
    fun Modifier.fill(): Modifier = fillMode(FillMode.MatchParent)

    /**
     * 解除当前组件的布局约束。
     *
     * 使用此方法可以使组件在布局中取消其先前设定的约束条件，使其不再受到父布局或兄弟组件设置的特定限制。
     * 调用此方法后，组件可能恢复到默认的未受约束状态。
     *
     * @receiver Modifier 当前的修饰符实例
     * @return 修改后的 Modifier 对象
     */
    fun Modifier.unlockConstraint(unlockConstraint: Boolean = true): Modifier

}

interface RowListLayoutScope : ListLayoutScope<Alignment.Vertical> {

    override fun Modifier.fillMode(fillMode: FillMode) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is RowListLayout.WrappedRowListLayoutData
                -> it.parentData = parentData.copy(fillMode = fillMode)

            null
                -> it.parentData = RowListLayout.WrappedRowListLayoutData(fillMode = fillMode)
        }
    }

    override fun Modifier.align(alignment: Alignment.Vertical) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is RowListLayout.WrappedRowListLayoutData
                -> it.parentData = parentData.copy(alignment = alignment)

            null
                -> it.parentData = RowListLayout.WrappedRowListLayoutData(alignment = alignment)
        }
    }

    override fun Modifier.unlockConstraint(unlockConstraint: Boolean) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is RowListLayout.WrappedRowListLayoutData
                -> it.parentData = parentData.copy(unlockConstraint = unlockConstraint)

            null
                -> it.parentData = RowListLayout.WrappedRowListLayoutData(unlockConstraint = unlockConstraint)
        }
    }

}

interface ColumnListLayoutScope : ListLayoutScope<Alignment.Horizontal> {

    override fun Modifier.fillMode(fillMode: FillMode) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnListLayout.WrappedColumnListLayoutData
                -> it.parentData = parentData.copy(fillMode = fillMode)

            null
                -> it.parentData = ColumnListLayout.WrappedColumnListLayoutData(fillMode = fillMode)
        }
    }

    override fun Modifier.align(alignment: Alignment.Horizontal) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnListLayout.WrappedColumnListLayoutData
                -> it.parentData = parentData.copy(alignment = alignment)

            null
                -> it.parentData = ColumnListLayout.WrappedColumnListLayoutData(alignment = alignment)
        }
    }

    override fun Modifier.unlockConstraint(unlockConstraint: Boolean) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnListLayout.WrappedColumnListLayoutData
                -> it.parentData = parentData.copy(unlockConstraint = unlockConstraint)

            null
                -> it.parentData = ColumnListLayout.WrappedColumnListLayoutData(unlockConstraint = unlockConstraint)
        }
    }

}