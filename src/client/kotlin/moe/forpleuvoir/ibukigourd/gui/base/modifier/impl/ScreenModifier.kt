package moe.forpleuvoir.ibukigourd.gui.base.modifier.impl

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenCustomData.setBgBlurRadius
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenCustomData.setRenderParentScreen

fun interface ScreenModifier : Modifier.Element {
    fun applyModify(element: IGScreen)

    override fun tryApplyModify(target: Any) {
        if (target is IGScreen) applyModify(target)
    }
}

fun Modifier.bgBlurRadius(bgBlurRadius: Float) = this then ScreenModifier {
    it.setBgBlurRadius(bgBlurRadius)
}

fun Modifier.renderParent(renderParent: Boolean) = this then ScreenModifier {
    it.setRenderParentScreen(renderParent)
}


fun Modifier.onClose(onClose: () -> Unit) = this then ScreenModifier {
    it.onClose = onClose
}