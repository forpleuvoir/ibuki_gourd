package moe.forpleuvoir.ibukigourd.test

import kotlinx.coroutines.delay
import moe.forpleuvoir.ibukigourd.gui.screen.ColumnScreen
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.nebula.common.util.defaultLaunch
import java.util.concurrent.atomic.AtomicInteger

val count = AtomicInteger(0)

fun testScreen9() = ColumnScreen {
    defaultLaunch {
        println("启动")
        while (true) {
            delay(500)
            count.incrementAndGet()
        }
    }
    count.set(0)
    TextLabel(mutableStateBy {
        Literal(count.toString())
    })
}