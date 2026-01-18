package moe.forpleuvoir.ibukigourd.gui.base.tip

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import kotlin.time.Duration

//------------ Hover Tip ------------\\

var HOVER_TIP: Tip? = null

fun TipHandler.pushHoverTip(parent: () -> Transform, tip: Tip) {
    HOVER_TIP = pushTip(parent, tip)
}

fun TipHandler.pushHoverTip(duration: Duration, parent: () -> Transform, tip: Tip) {
    popHoverTip()
    HOVER_TIP = pushTip(duration, parent, tip)
}

fun TipHandler.popHoverTip() {
    popTip(HOVER_TIP)
}

//------------ Screen Hover Tip ------------\\

var SCREEN_HOVER_TIP: Tip? = null

fun TipHandler.pushScreenHoverTip(parent: () -> Transform, tip: Tip) {
    SCREEN_HOVER_TIP = pushTip(parent, tip)
}

fun TipHandler.pushScreenHoverTip(duration: Duration, parent: () -> Transform, tip: Tip) {
    popScreenHoverTip()
    SCREEN_HOVER_TIP = pushTip(duration, parent, tip)
}

fun TipHandler.popScreenHoverTip() {
    popTip(SCREEN_HOVER_TIP)
}

//------------ Pop Tip ------------\\

var POP_TIP: Tip? = null

fun TipHandler.pushPopTip(parent: () -> Transform, tip: Tip) {
    popPopTip()
    POP_TIP = pushTip(parent, tip)
}

fun TipHandler.pushPopTip(duration: Duration, parent: () -> Transform, tip: Tip) {
    popPopTip()
    POP_TIP = pushTip(duration, parent, tip)
}


fun TipHandler.popPopTip() {
    popTip(POP_TIP)
}