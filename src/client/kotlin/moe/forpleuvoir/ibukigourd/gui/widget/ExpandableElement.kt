package moe.forpleuvoir.ibukigourd.gui.widget

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


    override val onPress: () -> Unit = {
        this.expend = !this.expend
    }

}