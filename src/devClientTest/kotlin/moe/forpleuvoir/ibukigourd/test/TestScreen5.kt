package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.size
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.width
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color

fun testScreen5() = RowScreen(
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    Column(horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)) {
        val color: State<ARGBColor> = stateOf(Color(255, 255, 255))
        Row(
            verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically),
        ) {
            Column {
                IntEditor(
                    stateOf(color, { it.red }) { Color(color.getValue().argb).red(it) },
                    range = 0..255,
                    modifier = Modifier.width(40f),
                    editorModifier = { Modifier.weight(1) })
                RedColorSlider(color, modifier = Modifier.size(160f, 14f))
            }
            Column {
                IntEditor(
                    stateOf(color, { it.green }) { Color(color.getValue().argb).green(it) },
                    range = 0..255,
                    modifier = Modifier.width(40f),
                    editorModifier = { Modifier.weight(1) })
                GreenColorSlider(color, modifier = Modifier.size(160f, 14f))
            }
            Column {
                IntEditor(
                    stateOf(color, { it.blue }) { Color(color.getValue().argb).blue(it) },
                    range = 0..255,
                    modifier = Modifier.width(40f),
                    editorModifier = { Modifier.weight(1) })
                BlueColorSlider(color, modifier = Modifier.size(160f, 14f))
            }
            Column {
                IntEditor(
                    stateOf(color, { it.alpha }) { Color(color.getValue().argb).alpha(it) },
                    range = 0..255,
                    modifier = Modifier.width(40f),
                    editorModifier = { Modifier.weight(1) })
                AlphaColorSlider(color, modifier = Modifier.size(160f, 14f))
            }
        }

        Box(modifier = Modifier
            .size(80f, 80f)
            .render { context, _, _, _ ->
                context.useScissor(transform.asWorldBox) {
                    batchRenderTextureColored {
                        pushTileTexture(transform, WidgetTextures.ALPHA)
                    }
                    batchRenderBox {
                        pushBox(transform, color.getValue())
                    }
                }
            }
        )
    }

    Column(horizontalArrangement = Arrangement.spacedBy(5f, Alignment.CenterHorizontally)) {
        val color: State<ARGBColor> = stateOf(Color(255, 255, 255))
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
                context.useScissor(transform.asWorldBox) {
                    batchRenderTextureColored {
                        pushTileTexture(transform, WidgetTextures.ALPHA)
                    }
                    batchRenderBox {
                        pushBox(transform, color.getValue())
                    }
                }
            }
        )
    }


}


