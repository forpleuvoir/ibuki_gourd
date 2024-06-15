package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.util.NextAction

abstract class ExpandableElement(
    expend: Boolean = false,
) : PressableElement() {
    var expend: Boolean = expend
        set(value) {
            field = value
            if (value) {
                onExpand?.invoke()
            } else {
                onCollapse?.invoke()
            }
        }

    open var onExpand: (() -> Unit)? = null

    open var onCollapse: (() -> Unit)? = null


    override val onPress: () -> NextAction = {
        this.expend = !this.expend
        NextAction.Cancel
    }

}