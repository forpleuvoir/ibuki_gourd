package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.AbsoluteLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.name
import moe.forpleuvoir.ibukigourd.gui.base.scope.AbsoluteLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl

fun interface AbsoluteScreenScope : ScreenScope<IGScreen>, AbsoluteLayoutScope

fun AbsoluteScreen(
    modifier: Modifier = Modifier,
    content: AbsoluteScreenScope.() -> Unit
): IGScreenImpl<AbsoluteScreenScope> {
    return object : IGScreenImpl<AbsoluteScreenScope>(), AbsoluteLayout {

        override fun AbsoluteScreenScope.content() = content()

        override val scope: AbsoluteScreenScope = AbsoluteScreenScope { this }

    }.apply {
        Modifier.name("AbsoluteScreen").then(modifier).foldInApply()
    }
}
