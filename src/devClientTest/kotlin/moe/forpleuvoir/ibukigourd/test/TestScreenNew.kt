package moe.forpleuvoir.ibukigourd.test

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.render
import moe.forpleuvoir.ibukigourd.gui.screen.ScreenManager
import moe.forpleuvoir.ibukigourd.gui.screen.screen
import moe.forpleuvoir.ibukigourd.gui.widget.button.button
import moe.forpleuvoir.ibukigourd.gui.widget.text.textField

var FRT = 0.0
var FPS = 0L

fun testScreen(index: Int) {
    when (index) {
        1 -> testScreen1()
//        2 -> testScreen2
//        3 -> testScreen3
        else -> testScreen1()
    }.let {
        FRT = 0.0
        FPS = 0L
        ScreenManager.open(it)
    }
}

fun testScreen1() = screen(
    Modifier.render { renderContext ->
        onRender(renderContext)
        renderContext.tryRender {

        }
    }
) {
    textField("测试字符串")
    button(
        onPress = {
            println("点击了测试按钮")
        }
    ) {
        textField("测试按钮")
    }

}