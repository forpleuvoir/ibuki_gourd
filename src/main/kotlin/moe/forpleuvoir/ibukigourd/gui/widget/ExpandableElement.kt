package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.util.NextAction

abstract class ExpandableElement(
    var expend: Boolean = false,
) : ClickableElement() {

    override val onClick: () -> NextAction = {
        expend = !expend
        NextAction.Cancel
    }

}