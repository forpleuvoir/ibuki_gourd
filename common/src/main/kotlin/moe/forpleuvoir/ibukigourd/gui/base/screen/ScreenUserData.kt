package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig
import moe.forpleuvoir.nebula.common.util.countParents
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


object ScreenUserData {

    //------------ BG BLUR RADIUS ------------\\

    private val BG_BLUR_RADIUS_KEY: String get() = "#bg_blur_radius"

    //TODO 可能会修改实现方式
    var IGScreen.bgBlurRadius: Float
        get() = userData[BG_BLUR_RADIUS_KEY] as Float? ?: 0f
        set(value) {
            userData[BG_BLUR_RADIUS_KEY] = value
        }

    //------------ RENDER PARENT SCREEN ------------\\

    private val RENDER_PANORAMA: String get() = "#render_panorama"

    var IGScreen.renderPanorama: Boolean
        get() = userData[RENDER_PANORAMA] as Boolean? ?: true
        set(value) {
            userData[RENDER_PANORAMA] = value
        }

    //------------ RENDER PARENT SCREEN ------------\\

    private val RENDER_PARENT_SCREEN_KEY: String get() = "#render_parent_screen"

    var IGScreen.renderParentScreen: Boolean
        get() = userData[RENDER_PARENT_SCREEN_KEY] as Boolean? == true
        set(value) {
            userData[RENDER_PARENT_SCREEN_KEY] = value
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

    //------------ FadeIn Offset ------------\\

    private val FADE_IN_OFFSET_KEY: String get() = "#fade_in_offset"

    var IGScreen.fadeInOffset: Float
        get() = userData[FADE_IN_OFFSET_KEY] as Float? ?: GuiConfig.Screen.fadeInOffset
        set(value) {
            userData[FADE_IN_OFFSET_KEY] = value
        }


    //------------ FadeIn Duration ------------\\

    private val FADE_IN_DURATION_KEY: String get() = "#fade_in_duration"

    var IGScreen.fadeInDuration: Duration
        get() = userData[FADE_IN_DURATION_KEY] as Duration? ?: GuiConfig.Screen.fadeInDuration
        set(value) {
            userData[FADE_IN_DURATION_KEY] = value
        }

    //------------ FadeIn Direction ------------\\

    private val FADE_IN_DIRECTION_KEY: String get() = "#fade_in_direction"

    var IGScreen.fadeInDirection: Direction
        get() = userData[FADE_IN_DIRECTION_KEY] as Direction? ?: Direction.Bottom
        set(value) {
            userData[FADE_IN_DIRECTION_KEY] = value
        }

}