package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl

data class LinearScreenScope(
    val screen: IGScreen,
    override val linearLayout: LinearLayout
) : ScreenScope<IGScreen>, LinearLayoutScope {
    override fun owner(): IGScreen = screen

}

fun linearScreen(
    orientation: Orientation,
    alignment: (Orientation) -> Alignment = BoxAlignment::CenterCenter,
    modifier: Modifier = Modifier,
    content: LinearScreenScope.() -> Unit
): IGScreenImpl<LinearScreenScope> {
    return object : IGScreenImpl<LinearScreenScope>(), LinearLayout {

        override fun LinearScreenScope.content() = content()

        override val scope: LinearScreenScope = LinearScreenScope(this, this)

        override var spacing: Float = 0f

        override val orientation: Orientation = orientation

        override val alignment: (Orientation) -> Alignment = alignment

    }.apply {
        modifier.foldInApply()
    }
}

fun rowScreen(
    alignment: (Orientation) -> Alignment = BoxAlignment::CenterCenter,
    modifier: Modifier = Modifier,
    content: LinearScreenScope.() -> Unit
): IGScreenImpl<LinearScreenScope> = linearScreen(Orientation.Vertical, alignment, modifier, content)

fun columnScreen(
    alignment: (Orientation) -> Alignment = BoxAlignment::CenterCenter,
    modifier: Modifier = Modifier,
    content: LinearScreenScope.() -> Unit
): IGScreenImpl<LinearScreenScope> = linearScreen(Orientation.Horizontal, alignment, modifier, content)
