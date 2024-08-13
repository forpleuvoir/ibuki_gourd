package moe.forpleuvoir.ibukigourd.gui.base.widget

abstract class ExpandableWidgetContainer : IGClickableWidgetContainer(), ExpandableWidget {

    override var expandState: ExpandState = ExpandState.Closed
        set(value) {
            field = value
            value.isExpanded {
                onExpand()
                _onExpand?.invoke()
            }
            value.isClosed {
                onClose()
                _onClose?.invoke()
            }
        }

    fun toggle() {
        expandState = !expandState
    }


    private var _onExpand: (() -> Unit)? = null

    override fun onExpand(action: () -> Unit) {
        _onExpand = action
    }

    protected abstract fun onExpand()

    private var _onClose: (() -> Unit)? = null

    override fun onClose(action: () -> Unit) {
        _onClose = action
    }

    protected abstract fun onClose()

    override fun onClick(mouseX: Float, mouseY: Float) = toggle()

}
