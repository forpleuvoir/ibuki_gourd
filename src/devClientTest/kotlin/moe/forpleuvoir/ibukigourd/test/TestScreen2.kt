package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextAreaWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf

fun TestScreen2() = ColumnScreen(
    modifier = Modifier.padding(20),
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    val t = mutableStateOf("hello minecraft")
    TextLabel(t)
    TextEditor {
        textConsumer {
            t.setValue(it)
        }
        text = t.getValue()
    }
    TextAreaWrapped(
        modifier = Modifier.width(160f).weight(1)
    ) {
        textConsumer {
            t.setValue(it)
        }
        text = t.getValue()
    }


}