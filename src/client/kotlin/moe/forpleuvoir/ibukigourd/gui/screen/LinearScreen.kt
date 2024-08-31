package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.ColumnLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.RowLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl

class RowScreen(
    override val arrangement: Arrangement.Vertical,
    override val alignment: Alignment.Horizontal,
    private val content: RowScreenScope.() -> Unit
) : IGScreenImpl<RowScreenScope>(), RowLayout {

    override fun RowScreenScope.content() = content.invoke(this)

    override val scope: Scope = RowScreenScope { this }

    fun interface Scope : ScreenScope<RowScreen>, RowLayoutScope

}

typealias RowScreenScope = RowScreen.Scope

fun RowScreen(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: RowScreenScope.() -> Unit
) = RowScreen(verticalArrangement, horizontalAlignment, content).apply { modifier.foldInApply() }


class ColumnScreen(
    override val arrangement: Arrangement.Horizontal,
    override val alignment: Alignment.Vertical,
    private val content: ColumnScreenScope.() -> Unit
) : IGScreenImpl<ColumnScreenScope>(), ColumnLayout {

    override fun ColumnScreenScope.content() = content.invoke(this)

    override val scope: Scope = ColumnScreenScope { this }

    fun interface Scope : ScreenScope<ColumnScreen>, ColumnLayoutScope

}

typealias ColumnScreenScope = ColumnScreen.Scope

fun ColumnScreen(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: ColumnScreenScope.() -> Unit
) = ColumnScreen(horizontalArrangement, verticalAlignment, content).apply { modifier.foldInApply() }
