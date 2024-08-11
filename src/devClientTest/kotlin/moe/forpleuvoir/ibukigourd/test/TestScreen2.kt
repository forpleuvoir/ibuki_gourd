package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.screen.byRemember
import moe.forpleuvoir.ibukigourd.gui.base.screen.remember
import moe.forpleuvoir.ibukigourd.gui.screen.rowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.layout.column
import moe.forpleuvoir.ibukigourd.gui.widget.text.textAreaWidthScroller
import moe.forpleuvoir.ibukigourd.gui.widget.text.textField
import moe.forpleuvoir.ibukigourd.util.mc

fun testScreen2() = rowScreen(
    modifier = Modifier.padding(20)
) {
    spacing(5f)
    textField {
        textConsumer {
            mc.remember("text1", it)
        }
        text = mc.byRemember("text1", "Hello world!")
    }
    column {
        textAreaWidthScroller() {
            textConsumer {
                this@rowScreen.remember(this, it)
            }
            text = this@rowScreen.byRemember(this, "这是多行文本")
        }

    }


}