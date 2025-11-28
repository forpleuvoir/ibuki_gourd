package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.nebula.common.util.countParents


object ScreenUserData {

    //------------ BG BLUR RADIUS ------------\\

    private val BG_BLUR_RADIUS_KEY: String get() = "#bg_blur_radius"

    //TODO 可能会修改实现方式
    val IGScreen.bgBlurRadius: Float
        get() = userData[BG_BLUR_RADIUS_KEY] as Float? ?: 0f

    fun IGScreen.setBgBlurRadius(radius: Float) {
        userData[BG_BLUR_RADIUS_KEY] = radius
    }

    //------------ RENDER PARENT SCREEN ------------\\

    private val RENDER_PARENT_SCREEN_KEY: String get() = "#render_parent_screen"

    val IGScreen.renderParentScreen: Boolean
        get() = userData[RENDER_PARENT_SCREEN_KEY] as Boolean? == true

    fun IGScreen.setRenderParentScreen(render: Boolean) {
        userData[RENDER_PARENT_SCREEN_KEY] = render
    }

    //------------ Parent Count ------------\\

    private val PARENT_COUNT_KEY: String get() = "#parent_key"

    val IGScreen.parentCount: Int
        get() {
            return userData[PARENT_COUNT_KEY] as Int? ?: run {
                val count = this.countParents { it.parentScreen as? IGScreen }
                userData[PARENT_COUNT_KEY] = count
                count
            }
        }

}