package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl


data class BoxScreenScope(val screen: IGScreen) : ScreenScope<IGScreen>, BoxLayoutScope {
    override fun owner(): IGScreen = screen

}

fun boxScreen(
    modifier: BoxScreenScope.() -> Modifier = { Modifier },
    content: BoxScreenScope.() -> Unit
): IGScreenImpl<BoxScreenScope> {
    return object : IGScreenImpl<BoxScreenScope>(), BoxLayout {

        override fun BoxScreenScope.content() = content()

        override val scope: BoxScreenScope = BoxScreenScope(this)

    }.apply {
        scope.modifier().foldInApply()
    }
}
