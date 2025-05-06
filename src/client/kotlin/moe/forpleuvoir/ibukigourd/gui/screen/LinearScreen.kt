package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.name
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope

fun ColumnScreen(
    screenModifier: Modifier = Modifier,
    modifier: BoxScreenScope.() -> Modifier = { Modifier },
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: ColumnScope.() -> Unit
) = BoxScreen(Modifier.name("ColumnScreen").then(screenModifier)) {
    Column(Modifier.fill().then(modifier()), verticalArrangement, horizontalAlignment, content)
}

fun RowScreen(
    screenModifier: Modifier = Modifier,
    modifier: BoxScreenScope.() -> Modifier = { Modifier },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: RowScope.() -> Unit
) = BoxScreen(Modifier.name("RowScreen").then(screenModifier)) {
    Row(Modifier.fill().then(modifier()), horizontalArrangement, verticalAlignment, content)
}