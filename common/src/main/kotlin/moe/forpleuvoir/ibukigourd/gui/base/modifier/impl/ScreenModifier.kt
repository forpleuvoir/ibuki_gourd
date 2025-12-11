package moe.forpleuvoir.ibukigourd.gui.base.modifier.impl

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.bgBlurRadius
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.fadeInDirection
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.fadeInDuration
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.fadeInOffset
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.renderPanorama
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.renderParentScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import kotlin.time.Duration

fun interface ScreenModifier : Modifier.Element {
    fun applyModify(element: IGScreen)

    override fun tryApplyModify(target: Any) {
        if (target is IGScreen) applyModify(target)
    }
}

@Deprecated("Temporarily invalid")
fun Modifier.bgBlurRadius(bgBlurRadius: Float) = this then ScreenModifier {
    it.bgBlurRadius = bgBlurRadius
}

fun Modifier.renderPanorama(renderPanorama: Boolean) = this then ScreenModifier {
    it.renderPanorama = renderPanorama
}

fun Modifier.renderParent(renderParent: Boolean) = this then ScreenModifier {
    it.renderParentScreen = renderParent
}

fun Modifier.fadeInOffset(fadeInOffset: Float) = this then ScreenModifier {
    it.fadeInOffset = fadeInOffset
}

fun Modifier.fadeInDuration(fadeInDuration: Duration) = this then ScreenModifier {
    it.fadeInDuration = fadeInDuration
}

fun Modifier.fadeInDirection(fadeInDirection: Direction) = this then ScreenModifier {
    it.fadeInDirection = fadeInDirection
}

fun Modifier.onClose(onClose: () -> Unit) = this then ScreenModifier {
    it.onClose = onClose
}