package moe.forpleuvoir.ibukigourd.gui.base.modifier.widget

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.IGWidgetModifier
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

class PaddingModifier(
    override val target: IGWidget,
    private val padding: Padding,
    private val isPadding: Boolean = true,
) : IGWidgetModifier {

    override fun applyModify(target: IGWidget) {
        if (isPadding)
            target.padding = padding
        else
            target.margin = padding
    }

}