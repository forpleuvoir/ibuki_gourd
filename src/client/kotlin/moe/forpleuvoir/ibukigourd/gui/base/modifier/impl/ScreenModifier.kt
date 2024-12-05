package moe.forpleuvoir.ibukigourd.gui.base.modifier.impl

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenCustomData

fun interface ScreenModifier : Modifier.Element {
    fun applyModify(element: IGScreen)

    override fun tryApplyModify(target: Any) {
        if (target is IGScreen) applyModify(target)
    }
}

fun Modifier.bgBlurRadius(bgBlurRadius: Float) = this then ScreenModifier {
    it.customData[ScreenCustomData.BG_BLUR_RADIUS] = bgBlurRadius
}

fun Modifier.onClose(onClose: () -> Unit) = this then ScreenModifier {
    it.onClose = onClose
}