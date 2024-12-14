package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.ColorPicker
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.Color

fun testScreen5() = RowScreen(
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    val color = mutableStateOf(GuiConfig.screen.WIDGET_TEST_OUTLINE_COLOR).apply {
        subscribe {
            GuiConfig.screen.WIDGET_TEST_OUTLINE_COLOR = Color(it.argb)
        }
    }
    ColorPicker(color)
}


