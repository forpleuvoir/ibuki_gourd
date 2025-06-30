package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.size
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.modifier.bgHoverHighlightBox
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.PercentageSlider
import moe.forpleuvoir.ibukigourd.gui.widget.SimpleDialog
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf

fun testScreen5() = ColumnScreen(
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    Button {
        TextLabel("滑条测试")
        click {
            SimpleDialog(stateOf(Literal("滑条测试"))) {
                Row(Modifier.bgHoverHighlightBox()) {
                    PercentageSlider(mutableStateOf(0.5), 0.0..1.0, modifier = Modifier.size(120f, 16f))
                }
            }.open()
        }
    }

    val text = mutableStateOf("test")
    TextEditor {
        bindState(text)
    }

    Button {
        TextLabel(text)
        click {
            text.setValue("test2")
        }
    }

}


