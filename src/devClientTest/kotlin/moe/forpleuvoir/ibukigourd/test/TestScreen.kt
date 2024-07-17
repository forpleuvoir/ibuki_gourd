package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.widget.box
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.icon
import moe.forpleuvoir.ibukigourd.gui.widget.layout.row
import moe.forpleuvoir.nebula.common.color.Colors

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


    override fun ScreenScope<out IGScreen>.content() {
        val icons = listOf(
            IconTextures.CLOSE,
            IconTextures.SEARCH,
            IconTextures.FILTER,
            IconTextures.MINUS,
        )
        row {
            icons.forEach {
                icon(it)
                box(Modifier.height(8f))
            }
        }
        icons.forEachIndexed { index, texture ->
            box(
                Modifier.height(8f)
                    .width(8f)
                    .render { context, mouseX, mouseY, delta ->
                        this as IGWidget
                        context.batchRenderBox {
                            context.boxOutline(transform.asWorldBox, Colors.BLUE.opacity(.8f), inner = true)
                        }
                    }
            )
            button(
                Modifier.maxHeight(24f)
                    .maxWidth(80f)
                    .renderOverlay { context, mouseX, mouseY, delta ->
                        this as IGWidget
                        context.batchRenderBox {
                            if (wasMouseOver) context.boxOutline(transform.asWorldBox, Colors.RED.opacity(.8f))
                        }
                    }
            ) {
                press {
                    println("按下了测试按钮$index")
                    println(this@TestScreen.focusedWidget)
                }
                longPress(10) {
                    println("长按了按钮$index")
                }
                icon(texture)
            }
        }
    }


}