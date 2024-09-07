package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.util.collection.notifiableList

fun testScreen4() = ColumnScreen {
    IntEditor(stateOf(5), modifier = Modifier.width(50f), editorModifier = { Modifier.weight(1) }, scope = {
        HoverTip(
            modifier = Modifier.margin(3f),
            optionalDirection = notifiableList(Direction.Top)
        ) {
            Button {
                TextLabel("悬浮测试")
            }
        }
    })
    val keepState = stateOf(false)
    Button(
        Modifier
            .keyPress {
                if (it.keyCode == Keyboard.LEFT_CONTROL) {
                    keepState.setValue(true)
                }
            }.keyRelease {
                if (it.keyCode == Keyboard.LEFT_CONTROL) {
                    keepState.setValue(false)
                }
            }
    ) {
        TextLabel("高度测试1")
        HoverTip(
            modifier = Modifier.margin(3f),
            keepShow = keepState,
            optionalDirection = notifiableList(Direction.Bottom)
        ) {
            Button {
                TextLabel("悬浮测试")
            }
        }
    }
    val switchState = stateOf(false)

    SwitchButton(switchState) {
        HoverTip(
            modifier = Modifier.margin(3f),
            optionalDirection = notifiableList(Direction.Left)
        ) {
            TextLabel(stateOf(switchState) { it.toString() })
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
        val state = stateOf(15)
        IntSlider(state, -50..100, colorA = HSVColor(210f, .3f, .7f), colorB = HSVColor(210f, .1f, 1f), modifier = Modifier.size(120f, 16f))
        LongSlider(stateOf(30), -50L..100L, modifier = Modifier.size(120f, 16f))
        FloatSlider(stateOf(30f), -50f..100f, modifier = Modifier.size(120f, 16f))
        DoubleSlider(stateOf(30.0), -50.0..100.0, modifier = Modifier.size(120f, 16f))
    }

}