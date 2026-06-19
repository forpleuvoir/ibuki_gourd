package moe.forpleuvoir.ibukigourd.ui.util

import moe.forpleuvoir.ibukigourd.ui.ComposeScreen
import moe.forpleuvoir.ibukigourd.util.mc

object ShouldBlockLevelRender {

    @JvmStatic
    fun shouldBlock(): Boolean {
        return (mc.screen as? ComposeScreen)?.let {
            !it.shouldRenderLevel
        } ?: false
    }
}