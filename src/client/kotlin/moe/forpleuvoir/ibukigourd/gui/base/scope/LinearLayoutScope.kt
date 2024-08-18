package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier

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
    fun Modifier.fill(fill: Boolean = true): Modifier

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