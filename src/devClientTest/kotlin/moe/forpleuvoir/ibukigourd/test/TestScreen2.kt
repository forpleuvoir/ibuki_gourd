package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.screen.getScreenDataOr
import moe.forpleuvoir.ibukigourd.gui.base.screen.pushScreenData
import moe.forpleuvoir.ibukigourd.gui.screen.rowScreen
import moe.forpleuvoir.ibukigourd.gui.widget.text.textField
import moe.forpleuvoir.ibukigourd.util.mc

fun testScreen2() = rowScreen {

    textField {
        textConsumer {
            mc.pushScreenData("text1", it)
        }
        text = mc.getScreenDataOr("text1", "Hello world!")

    }


}