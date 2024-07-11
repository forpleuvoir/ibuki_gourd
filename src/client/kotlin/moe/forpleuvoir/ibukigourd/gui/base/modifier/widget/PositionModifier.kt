package moe.forpleuvoir.ibukigourd.gui.base.modifier.widget

import moe.forpleuvoir.ibukigourd.gui.base.WidgetPosition
import moe.forpleuvoir.ibukigourd.gui.base.modifier.IGWidgetModifier
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

class PositionModifier(
    override val target: IGWidget,
    private val position: WidgetPosition,
    private val isWorldAxis: Boolean =false
) : IGWidgetModifier {

    override fun applyModify(target: IGWidget) {
        if (isWorldAxis) {
            target.transform.worldX = position.x()
            target.transform.worldY = position.y()
        } else {
            target.transform.x = position.x()
            target.transform.y = position.y()
        }
    }

}