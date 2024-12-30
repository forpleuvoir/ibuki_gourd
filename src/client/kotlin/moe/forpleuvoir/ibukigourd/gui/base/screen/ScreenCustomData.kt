package moe.forpleuvoir.ibukigourd.gui.base.screen


object ScreenCustomData {

    private val BG_BLUR_RADIUS_KEY: String get() = "bgBlurRadius"

    val IGScreen.bgBlurRadius: Float
        get() = customData[BG_BLUR_RADIUS_KEY] as Float? ?: 0f

    fun IGScreen.setBgBlurRadius(radius: Float) {
        customData[BG_BLUR_RADIUS_KEY] = radius
    }

    private val RENDER_PARENT_SCREEN_KEY: String get() = "renderParentScreen"

    val IGScreen.renderParentScreen: Boolean
        get() = customData[RENDER_PARENT_SCREEN_KEY] as Boolean? == true

    fun IGScreen.setRenderParentScreen(render: Boolean) {
        customData[RENDER_PARENT_SCREEN_KEY] = render
    }

}