package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.margin
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.size
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.util.collection.notifiableList

fun testScreen4() = ColumnScreen(
    Modifier.renderOverlay { ctx, x, y, d ->
        onRenderOverlay(ctx, x, y, d)
        val lines = listOf(
            Literal(screen()?.focusedWidget.toString()),
            Literal(screen()?.hoveredWidget.toString())
        )
        ctx.batchRenderText {
            pushTextLines(
                lines, transform.asWorldBox, horizontalAlignment = Alignment.Left, verticalArrangement = Arrangement.Top
            )
        }
    }
) {
    IntEditor(mutableStateOf(5), modifier = Modifier.width(50f), editorModifier = { Modifier.weight(1) }, scope = {
        HoverTip(
            modifier = Modifier.margin(3f),
            optionalDirection = notifiableList(Direction.Top)
        ) {
            Button {
                TextLabel("悬浮测试")
            }
        }
    })
    Button {
        TextLabel("高度测试1")
        press {
            OpenDialog(
                stateOf(Literal("测试一下Dialog")),
            ) {
                val c: MutableState<Color> = mutableStateOf(Colors.BRIGHT_GRAPE)
                ColorPicker(c)
            }
        }
    }
    val switchState = mutableStateOf(false)

    SwitchButton(switchState) {
        HoverTip(
            modifier = Modifier.margin(3f),
            optionalDirection = notifiableList(Direction.Left)
        ) {
            TextLabel(mutableStateOf(switchState) { it.toString() })
        }
    }
    Spinner(listOf("下拉菜单", "选项1", "选项2", "选项3")) {
        HoverTip(
            modifier = Modifier.margin(3f),
            optionalDirection = notifiableList(Direction.Right)
        ) {
            Button {
                TextLabel("悬浮测试")
            }
        }
    }
    Row {
        val state = mutableStateOf(15)
        IntSlider(state, -50..100, colorA = HSVColor(210f, .3f, .7f), colorB = HSVColor(210f, .1f, 1f), modifier = Modifier.size(120f, 16f))
        LongSlider(mutableStateOf(30), -50L..100L, modifier = Modifier.size(120f, 16f))
        FloatSlider(mutableStateOf(30f), -50f..100f, modifier = Modifier.size(120f, 16f))
        DoubleSlider(mutableStateOf(30.0), -50.0..100.0, modifier = Modifier.size(120f, 16f))
    }

}