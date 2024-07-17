package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.height
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.margin
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon

class TestScreen : IGScreenImpl(), LinearLayout {
    override val orientation: Orientation
        get() = Orientation.Vertical
    override val alignment: (Orientation) -> Alignment
        get() = PlanarAlignment::Center

    override val widget: IGWidget
        get() = this


    override fun measurableChildren(): List<Measurable> = widgetChildren()

    override fun measure(constraints: Constraints): Placeable =
        super<LinearLayout>.measure(constraints)

    override fun applyResult(size: Size<Float>): Placeable {
        return this
    }


    override fun GuiScope<out IGScreen>.content() {
//        icon(
//            IconTextures.CLOSE,
//            modifier = Modifier.width(16f)
//        )
        button {
            icon(IconTextures.CLOSE, modifier = Modifier.height(8f))
            icon(IconTextures.SEARCH, modifier = Modifier.height(8f))
            icon(IconTextures.FILTER, modifier = Modifier.height(8f))
        }
    }


}