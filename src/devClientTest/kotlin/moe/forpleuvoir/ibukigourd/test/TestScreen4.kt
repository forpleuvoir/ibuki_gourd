package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.event.IbukiGourdEventManager
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.RadioButtons
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.color.HSVColor

fun testScreen4() = RowScreen(
    Modifier.renderOverlay { ctx, x, y, d ->
        onRenderOverlay(ctx, x, y, d)
        val lines = listOf(
            Literal(screen()?.focusedWidget.toString()),
            Literal(screen()?.hoveredWidget.toString())
        )
        ctx.batchRenderText {
            pushTextLines(
                lines, transform.asWorldCoordinateBox, horizontalAlignment = Alignment.Left, verticalArrangement = Arrangement.Top
            )
        }
    }
) {
    IntEditor(
        mutableStateOf(5),
        modifier = Modifier.width(50f)
            .hoverTip(
                modifier = Modifier.margin(3f), settings = Tip.DefaultSetting.copy(optionalDirection = listOf(Direction.Top))
            ) {
                Button {
                    TextLabel("悬浮测试")
                }
            },
        editorModifier = { Modifier.weight(1) })
    Button {
        TextLabel("高度测试1")
        click {
            SimpleDialog(
                stateOf(Literal("测试一下Dialog")),
            ) {
                val c: MutableState<Color> = mutableStateOf(Colors.BRIGHT_GRAPE)
                ColorPicker(c)
            }.open()
        }
    }
    val switchState = mutableStateOf(false)

    SwitchButton(
        switchState,
        modifier = Modifier.hoverText(mutableStateOf(switchState) { it.toString() }, Tip.DefaultSetting.copy(optionalDirection = listOf(Direction.Left)))
    )
    Selector(
        listOf("下拉菜单", "选项1", "选项2", "选项3"),
        modifier = Modifier.hoverTip(
            modifier = Modifier.margin(3f), settings = Tip.DefaultSetting.copy(optionalDirection = listOf(Direction.Top))
        ) {
            Button {
                TextLabel("悬浮测试")
            }
        }
    )
    EventSelector(IbukiGourdEventManager.eventSet(), modifier = Modifier.maxWidth(120f))

    Column {
        val state = mutableStateOf(15)
        IntSlider(state, -50..100, colorA = HSVColor(210f, .3f, .7f), colorB = HSVColor(210f, .1f, 1f), modifier = Modifier.size(120f, 16f))
        LongSlider(mutableStateOf(30), -50L..100L, modifier = Modifier.size(120f, 16f))
        FloatSlider(mutableStateOf(30f), -50f..100f, modifier = Modifier.size(120f, 16f))
        DoubleSlider(mutableStateOf(30.0), -50.0..100.0, modifier = Modifier.size(120f, 16f))
        PercentageSlider(mutableStateOf(0.5), 0.0..1.0, modifier = Modifier.size(120f, 16f))
    }
    Row {
        val selected: MutableState<String?> = mutableStateOf("选项3")
        RadioButtons(
            listOf("选项1", "选项2", "选项3"),
            selected,
            optionWrapper = {
                TextLabel(it)
            },
            onChange = {
                println("选择了$it")
            }
        )
    }

}