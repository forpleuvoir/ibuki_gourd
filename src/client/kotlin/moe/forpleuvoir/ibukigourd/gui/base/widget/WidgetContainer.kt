package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable

interface WidgetContainer : Measurable {

    fun hoveredWidget(layer: GuiLayer): IGWidget? {
        // 遍历所有子组件
        for (child in widgetChildren()) {
            // 检查组件是否激活
            if (!child.active) continue

            // 如果组件是 WidgetContainer，递归检查它的子组件
            if (child is WidgetContainer) {
                val hovered = child.hoveredWidget(layer)
                if (hovered != null && hovered.active) {
                    return hovered
                }
            }

            // 如果组件被悬停并且层级匹配，返回该组件
            if (child.wasMouseOver && child.layer == layer) {
                return child
            }
        }
        // 未找到符合条件的组件，返回 null
        return null
    }

    fun widgetChildren(): List<IGWidget>

    fun clearWidgetChildren()

    fun <W : IGWidget> addWidgetChild(child: W): W

    fun <W : IGWidget> addWidgetChild(child: W, scope: W.() -> Unit): W = addWidgetChild(child.apply(scope))

    fun <W : IGWidget> setWidgetChildren(index: Int, child: W): W

    fun removeWidgetChild(child: IGWidget): Boolean

    fun removeWidgetChildAt(index: Int): IGWidget?

}