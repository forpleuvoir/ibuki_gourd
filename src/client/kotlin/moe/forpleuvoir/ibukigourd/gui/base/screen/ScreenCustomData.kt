package moe.forpleuvoir.ibukigourd.gui.base.screen

object ScreenCustomData {

    val BG_BLUR_RADIUS: String get() = "bgBlurRadius"

    val IGScreen.bgBlurRadius: Float
        get() = customData[BG_BLUR_RADIUS] as Float? ?: 0f


}