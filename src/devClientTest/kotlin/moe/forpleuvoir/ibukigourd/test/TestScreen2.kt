package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.screen.byRemember
import moe.forpleuvoir.ibukigourd.gui.base.screen.remember
import moe.forpleuvoir.ibukigourd.gui.screen.RowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextAreaWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextField
import moe.forpleuvoir.ibukigourd.util.mc

fun TestScreen2() = RowScreen(
    modifier = Modifier.padding(20),
    verticalArrangement = Arrangement.spacedBy(5f, Alignment.CenterVertically)
) {
    TextField {
        textConsumer {
            mc.remember("text1", it)
        }
        text = mc.byRemember("text1", "Hello world!")
    }
    Column {
        TextAreaWrapped() {
            textConsumer {
                this@RowScreen.remember(this, it)
            }
            text = this@RowScreen.byRemember(this, "这是多行文本")
        }

    }


}