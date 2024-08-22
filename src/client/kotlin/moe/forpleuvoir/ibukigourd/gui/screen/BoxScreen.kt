package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl

fun interface BoxScreenScope : ScreenScope<IGScreen>, BoxLayoutScope

fun BoxScreen(
    modifier: Modifier = Modifier,
    content: BoxScreenScope.() -> Unit
): IGScreenImpl<BoxScreenScope> {
    return object : IGScreenImpl<BoxScreenScope>(), BoxLayout {

        override fun BoxScreenScope.content() = content()

        override val scope: BoxScreenScope = BoxScreenScope { this }

    }.apply {
        modifier.foldInApply()
    }
}
