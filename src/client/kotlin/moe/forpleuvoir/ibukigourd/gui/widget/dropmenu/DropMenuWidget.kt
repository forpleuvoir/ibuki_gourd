package moe.forpleuvoir.ibukigourd.gui.widget.dropmenu

import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.ExpandableWidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.button.button

class DropMenuWidget : ExpandableWidgetContainer(), BoxLayout {

    override fun onExpand() {}

    override fun onClose() {}


}

data class DropMenuScope(
    private val dropMenu: DropMenuWidget,
    val itemScope: ListLayoutScope,
) : GuiScope<DropMenuWidget> {

    override fun owner(): DropMenuWidget = dropMenu

    fun items(scope: ListLayoutScope.() -> Unit) {

    }

}


fun GuiScope<out WidgetContainer>.dropMenu(
    modifier: Modifier? = null,
    scope: DropMenuScope.() -> Unit,
): IGButtonWidget {
    var expanded = false

    return button {


    }
}