package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.height
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.size
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.width
import moe.forpleuvoir.ibukigourd.gui.base.screen.getScreenDataOr
import moe.forpleuvoir.ibukigourd.gui.base.screen.pushScreenData
import moe.forpleuvoir.ibukigourd.gui.screen.rowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.layout.column
import moe.forpleuvoir.ibukigourd.gui.widget.text.textArea
import moe.forpleuvoir.ibukigourd.gui.widget.text.textAreaWidthScroller
import moe.forpleuvoir.ibukigourd.gui.widget.text.textField
import moe.forpleuvoir.ibukigourd.util.mc

fun testScreen2() = rowScreen(
    modifier = { Modifier.padding(20) }
) {
    spacing(5f)
    textField {
        textConsumer {
            mc.pushScreenData("text1", it)
        }
        text = mc.getScreenDataOr("text1", "Hello world!")

    }
    column {
        textAreaWidthScroller(
            modifier = Modifier.height(240f).width(160f)
        ) {
            textConsumer {
                mc.pushScreenData("t", it)
            }
            text = mc.getScreenDataOr("t", "这是多行文本")
        }
        textArea(modifier = Modifier.size(160f, 240f)) {
            textConsumer {
                mc.pushScreenData("text2", it)
            }
            text = mc.getScreenDataOr("text2", "这是多行文本")
        }

    }


}