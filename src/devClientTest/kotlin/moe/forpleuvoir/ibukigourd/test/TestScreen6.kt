package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.size
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors

fun testScreen6() = RowScreen(
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {

    val colorState: State<ARGBColor> = stateOf(Colors.GREEN)
    Tabs {
        Tab(
            scope = {
                TextLabel("HSV")
            }
        ) {
            val color = stateOf(colorState.getValue())
            color.subscribe {
                colorState.setValue(it)
            }
            Column {
                HSVColorPicker(color)
                ColorResult(color, Modifier.size(60f, 60f))
            }
        }
        Tab(
            scope = {
                TextLabel("RGB")
            }
        ) {
            val color = stateOf(colorState.getValue())
            color.subscribe {
                colorState.setValue(it)
            }
            Column {
                ARGBColorPicker(color)
                ColorResult(color, Modifier.size(60f, 60f))
            }
        }
    }

}


