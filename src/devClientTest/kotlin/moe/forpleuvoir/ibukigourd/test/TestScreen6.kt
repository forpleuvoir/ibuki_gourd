package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.size
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors

fun testScreen6() = RowScreen(
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {

    val colorState: MutableState<ARGBColor> = mutableStateOf(Colors.GREEN)
    Tabs {
        tabColor.setValue(Color(255, 204, 240))
        inactiveColor.setValue(Color(179, 242, 255))
        Tab(
            scope = {
                TextLabel("HSV")
            }
        ) {
            val color = mutableStateOf(colorState.getValue())
            color.subscribe {
                colorState.setValue(it)
            }
            Column(
                horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)
            ) {
                HSVColorPicker(color)
                ColorResult(color, Modifier.size(78f, 78f))
            }
        }
        Tab(
            scope = {
                TextLabel("RGB")
            }
        ) {
            val color = mutableStateOf(colorState.getValue())
            color.subscribe {
                colorState.setValue(it)
            }
            Column(
                horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)
            ) {
                ARGBColorPicker(color)
                ColorResult(color, Modifier.size(78f, 78f))
            }
        }
    }

}


