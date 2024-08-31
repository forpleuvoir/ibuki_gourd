package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.width
import moe.forpleuvoir.ibukigourd.gui.base.screen.byRemember
import moe.forpleuvoir.ibukigourd.gui.base.screen.remember
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextAreaWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.util.mc

fun TestScreen2() = RowScreen(
    modifier = Modifier.padding(20),
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    TextEditor {
        textConsumer {
            mc.remember("text1", it)
        }
        text = mc.byRemember("text1", "Hello world!")
    }
    TextAreaWrapped(
        modifier = Modifier.width(160f).weight(1)
    ) {
        textConsumer {
            this@RowScreen.remember(this, it)
        }
        text = this@RowScreen.byRemember(this, "这是多行文本")
    }


}