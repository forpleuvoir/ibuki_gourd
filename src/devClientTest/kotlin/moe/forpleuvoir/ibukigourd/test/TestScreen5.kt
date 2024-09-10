package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.ColorPicker
import moe.forpleuvoir.ibukigourd.mod.gui.GuiConfig
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color

fun testScreen5() = RowScreen(
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    val color: State<ARGBColor> = stateOf(GuiConfig.screen.WIDGET_TEST_OUTLINE_COLOR as ARGBColor).apply {
        subscribe {
            GuiConfig.screen.WIDGET_TEST_OUTLINE_COLOR = Color(it.argb)
        }
    }
    ColorPicker(color)

}


