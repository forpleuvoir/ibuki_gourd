package moe.forpleuvoir.ibukigourd.ui.util

import moe.forpleuvoir.ibukigourd.ui.ComposeScreen
import moe.forpleuvoir.ibukigourd.util.mc

object ComposeScreenHelper {

    @JvmStatic
    fun shouldBlockLevelRender(): Boolean {
        return (mc.gui.screen() as? ComposeScreen)?.let {
            !it.renderingLevel
        } ?: false
    }

    @JvmStatic
    fun isComposeScreen(): Boolean {
        return mc.gui.screen() is ComposeScreen
    }

}