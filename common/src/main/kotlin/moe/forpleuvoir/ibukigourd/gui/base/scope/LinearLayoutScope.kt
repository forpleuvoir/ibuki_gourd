package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.util.FillMode
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.WidgetModifier

@GuiDslMark
interface LinearLayoutScope<T : Alignment.Linear> {

    /**
     * [weight]权重
     *
     * 计算当前布局中的权重总和,根据剩余空间分配尺寸
     *
     * (尺寸 = (剩余空间 / 总权重) * 当前组件权重)
     *
     * @receiver Modifier
     * @param weight Int
     * @return Modifier
     */
    fun Modifier.weight(weight: Int): Modifier

    /**
     * 填充至最大空间 如果为垂直布局则填充宽度,为水平布局则填充高度
     * @receiver Modifier
     * @return Modifier
     */
    fun Modifier.fillMode(fillMode: FillMode): Modifier

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
     * 优先级,数值越高越先被测量,默认值为0
     */
    fun Modifier.priority(priority: Int): Modifier

    /**
     * 当前组件的对齐方式
     *
     * 在Row中为水平对齐[Alignment.Horizontal],Column中为垂直对齐[Alignment.Vertical]
     *
     * @receiver Modifier
     * @param alignment Linear
     * @return Modifier
     */
    fun Modifier.align(alignment: T): Modifier

}

interface RowLayoutScope : LinearLayoutScope<Alignment.Vertical> {

    override fun Modifier.weight(weight: Int) = this then WidgetModifier {
        check(weight >= 0) { "weight must be >= 0" }
        when (val parentData = it.parentData) {
            is RowLayout.WrappedRowLayoutData -> it.parentData = parentData.copy(weight = weight)
            null                              -> it.parentData = RowLayout.WrappedRowLayoutData(weight = weight)
        }
    }

    override fun Modifier.fillMode(fillMode: FillMode) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is RowLayout.WrappedRowLayoutData -> it.parentData = parentData.copy(fillMode = fillMode)
            null                              -> it.parentData = RowLayout.WrappedRowLayoutData(fillMode = fillMode)
        }
    }

    override fun Modifier.priority(priority: Int) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is RowLayout.WrappedRowLayoutData -> it.parentData = parentData.copy(priority = priority)
            null                              -> it.parentData = RowLayout.WrappedRowLayoutData(priority = priority)
        }
    }

    override fun Modifier.align(alignment: Alignment.Vertical) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is RowLayout.WrappedRowLayoutData -> it.parentData = parentData.copy(alignment = alignment)
            null                              -> it.parentData = RowLayout.WrappedRowLayoutData(alignment = alignment)
        }
    }

}

interface ColumnLayoutScope : LinearLayoutScope<Alignment.Horizontal> {

    override fun Modifier.weight(weight: Int) = this then WidgetModifier {
        check(weight >= 0) { "weight must be >= 0" }
        when (val parentData = it.parentData) {
            is ColumnLayout.WrappedColumnLayoutData -> it.parentData = parentData.copy(weight = weight)
            null                                    -> it.parentData = ColumnLayout.WrappedColumnLayoutData(weight = weight)
        }
    }

    override fun Modifier.fillMode(fillMode: FillMode) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnLayout.WrappedColumnLayoutData -> it.parentData = parentData.copy(fillMode = fillMode)
            null                                    -> it.parentData = ColumnLayout.WrappedColumnLayoutData(fillMode = fillMode)
        }
    }

    override fun Modifier.priority(priority: Int) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnLayout.WrappedColumnLayoutData -> it.parentData = parentData.copy(priority = priority)
            null                              -> it.parentData = RowLayout.WrappedRowLayoutData(priority = priority)
        }
    }

    override fun Modifier.align(alignment: Alignment.Horizontal) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnLayout.WrappedColumnLayoutData -> it.parentData = parentData.copy(alignment = alignment)
            null                                    -> it.parentData = ColumnLayout.WrappedColumnLayoutData(alignment = alignment)
        }
    }

}