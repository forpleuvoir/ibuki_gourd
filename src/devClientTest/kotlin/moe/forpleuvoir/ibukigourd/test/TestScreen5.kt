package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.size
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.HSVColor

fun testScreen5() = RowScreen(
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {

    Column(horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)) {
        val color: State<ARGBColor> = stateOf(Color(255, 255, 255))
        Row(
            verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically),
        ) {
            RedColorSlider(color, modifier = Modifier.size(100f, 10f))
            GreenColorSlider(color, modifier = Modifier.size(100f, 10f))
            BlueColorSlider(color, modifier = Modifier.size(100f, 10f))
            AlphaColorSlider(color, modifier = Modifier.size(100f, 10f))
        }

        Box(modifier = Modifier
            .size(55f, 55f)
            .render { context, _, _, _ ->
                context.batchRenderBox {
                    pushBox(transform, color.getValue())
                }
            }
        )
    }

    Column(horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)) {
        val color: State<HSVColor> = stateOf(HSVColor(210f, 1f, 1f))
        Row(
            verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically),
        ) {
            HueColorSlider(color, modifier = Modifier.size(100f, 10f))
            SaturationColorSlider(color, modifier = Modifier.size(100f, 10f))
            ValueColorSlider(color, modifier = Modifier.size(100f, 10f))
            AlphaColorSlider(color, modifier = Modifier.size(100f, 10f))
        }

        Box(modifier = Modifier
            .size(55f, 55f)
            .render { context, _, _, _ ->
                context.batchRenderBox {
                    pushBox(transform, color.getValue())
                }
            }
        )
    }


}


