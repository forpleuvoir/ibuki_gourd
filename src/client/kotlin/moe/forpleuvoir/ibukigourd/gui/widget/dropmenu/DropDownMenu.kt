package moe.forpleuvoir.ibukigourd.gui.widget.dropmenu

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Absolute
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowList
import moe.forpleuvoir.ibukigourd.gui.widget.layout.list.RowListScope
import moe.forpleuvoir.ibukigourd.util.delegate
import moe.forpleuvoir.nebula.common.util.primitive.pick

class DropDownMenuWidget {
}

class DropDownMenuScope(private val owner: ColumnWidget) : ColumnScope {

    override fun owner(): ColumnWidget = owner

    internal lateinit var content: ColumnScope.() -> Unit

    fun content(content: ColumnScope.() -> Unit) {
        this.content = content
    }

    internal lateinit var items: RowListScope.() -> Unit

    fun items(items: RowListScope.() -> Unit) {
        this.items = items
    }

}

fun WidgetContainerScope.DropDownMenu(
    scope: DropDownMenuScope.() -> Unit,
) {
    val expendState = delegate(false)
    val icon = delegate(WidgetTextures.DROP_MENU_ARROW_DOWN)
    expendState.subscribe {
        icon.setValue(it.pick(WidgetTextures.DROP_MENU_ARROW_UP, WidgetTextures.DROP_MENU_ARROW_DOWN))
    }

    Column(
        modifier = Modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val _scope = DropDownMenuScope(this.owner()).apply(scope)
        _scope.content(this)
        Icon(icon)
        Absolute {
            Column {
                RowList {
                    _scope.items(this)
                }
                Scroller()
            }
        }
    }

}