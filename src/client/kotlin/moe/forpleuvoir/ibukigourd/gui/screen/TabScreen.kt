package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.widget.TabScope
import moe.forpleuvoir.ibukigourd.gui.widget.Tabs
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors

fun TabScreen(
    modifier: Modifier = Modifier,
    header: ColumnScope.() -> Unit = { },
    footer: ColumnScope.() -> Unit = { },
    tabColor: State<ARGBColor> = stateOf(Colors.WHITE),
    inactiveColor: State<ARGBColor> = stateOf(Colors.GRAY),
    tabScope: TabScope.() -> Unit
) = ColumnScreen(
    modifier = modifier
) {
    header()
    Tabs(
        modifier = Modifier.fill().weight(1)
    ) {
        this.tabColor.bind(tabColor) { it }
        this.inactiveColor.bind(inactiveColor) { it }
        tabScope()
    }
    footer()
}