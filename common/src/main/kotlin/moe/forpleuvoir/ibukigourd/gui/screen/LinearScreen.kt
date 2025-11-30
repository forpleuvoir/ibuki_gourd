package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.name
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope

fun ColumnScreen(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: ColumnScope.() -> Unit
): IGScreenImpl = object : IGScreenImpl(), ColumnLayout {
    override var compose: () -> Unit = { ColumnScope { this }.content() }
    override val arrangement: Arrangement.Vertical get() = verticalArrangement
    override val alignment: Alignment.Horizontal get() = horizontalAlignment
}.apply {
    Modifier
//        .bgBlurRadius(defaultBgBlurRadius)
        .name("ColumnScreen")
        .then(modifier).foldInApply()
}


fun RowScreen(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: RowScope.() -> Unit
): IGScreenImpl = object : IGScreenImpl(), RowLayout {
    override var compose: () -> Unit = { RowScope { this }.content() }
    override val arrangement: Arrangement.Horizontal get() = horizontalArrangement
    override val alignment: Alignment.Vertical get() = verticalAlignment

}.apply {
    Modifier
//        .bgBlurRadius(defaultBgBlurRadius)
        .name("RowScreen")
        .then(modifier).foldInApply()
}