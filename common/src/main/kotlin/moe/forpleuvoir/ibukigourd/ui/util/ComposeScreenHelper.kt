package moe.forpleuvoir.ibukigourd.ui.util

import moe.forpleuvoir.ibukigourd.ui.ComposeScreen
import moe.forpleuvoir.ibukigourd.util.mc

object ComposeScreenHelper {

    @JvmStatic
    fun shouldBlockLevelRender(): Boolean {
        return (mc.screen as? ComposeScreen)?.let {
            !it.shouldRenderLevel
        } ?: false
    }

    @JvmStatic
    fun isComposeScreen(): Boolean {
        return mc.screen is ComposeScreen
    }

}