package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.element.GuiElement
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetUserData.isHoverable
import java.util.*

interface GuiWidgetContainer : GuiWidget, Layout {

    override val widget: GuiWidget
        get() = this

    fun hoveredWidget(): GuiWidget? {
        // 遍历所有子组件
        for (child in widgetChildren()) {
            // 检查组件是否激活
            if (!child.isHoverable) continue

            // 如果组件是 WidgetContainer，递归检查它的子组件
            if (child is GuiWidgetContainer) {
                val hovered = child.hoveredWidget()
                if (hovered != null && hovered.isHoverable) {
                    return hovered
                }
            }

            // 如果组件被悬停并且层级匹配，返回该组件
            if (child.wasMouseOver) {
                return child
            }
        }
        // 未找到符合条件的组件，返回 null
        return null
    }

    /**
     * - 用于定义组件的组合逻辑的函数类型变量。
     *
     * - 该变量存储一个无参且无返回值的函数，用于动态构建或更新组件的内容。
     *
     * - 在组件需要重新组合其内部结构时，此变量会被调用以执行相关的逻辑。
     *
     * - 所有的子组件都应该在此高阶函数内添加
     */
    var compose: () -> Unit

    fun recompose()

    fun widgetChildren(): List<GuiWidget>

    fun clearWidgetChildren()

    fun <W : GuiWidget> addWidgetChild(child: W): W

    fun <W : GuiWidget> addWidgetChild(child: W, scope: W.() -> Unit): W = addWidgetChild(child.apply(scope))

    fun <W : GuiWidget> setWidgetChildren(index: Int, child: W): W

    fun swapWidgetChildren(index1: Int, index2: Int)

    fun removeWidgetChild(child: GuiWidget): Boolean

    fun removeWidgetChildAt(index: Int): GuiWidget?

    fun flat(): List<GuiWidget>

    fun findTargetDFS(target: GuiWidget): Boolean {
        // 检查当前节点是否为空
        if (widgetChildren().isEmpty()) return false
        // 检查当前节点是否为目标节点
        if (this == target) return true

        // 遍历所有子节点
        for (child in widgetChildren()) {
            if (child == target) {
                return true
            }
            // 如果子节点是 WidgetContainer，则递归查找其子节点
            if (child is GuiWidgetContainer) {
                if (child.findTargetDFS(target)) return true
            }
        }

        return false
    }

    fun findTargetBFS(target: GuiWidget): Boolean {
        val queue: Queue<Any> = LinkedList()
        queue.add(this)

        while (queue.isNotEmpty()) {
            val current = queue.poll()

            // 检查当前节点是否为目标节点
            if (current == target) {
                return true
            }

            // 获取子节点并加入队列
            if (current is GuiWidgetContainer) {
                val children = current.widgetChildren()
                for (child in children) {
                    queue.add(child)
                }
            }
        }

        return false
    }

}

fun GuiWidgetContainer.Compose(compose: () -> Unit) {
    this.compose = compose
    this.compose()
}

fun <T : GuiScope<out GuiWidgetContainer>> T.Compose(compose: T.() -> Unit) {
    this.owner().compose = { compose(this) }
    this.owner().compose()
}

fun <T> T.executeRecompose() where T : GuiWidgetContainer, T : GuiElement {
    this.screen()?.execute { recompose() }
}