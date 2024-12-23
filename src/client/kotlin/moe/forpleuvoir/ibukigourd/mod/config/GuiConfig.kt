package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.ibukigourd.config.ModConfigContainer
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.config.item.impl.color
import moe.forpleuvoir.nebula.config.item.impl.float
import moe.forpleuvoir.nebula.config.item.impl.hsvColor

object GuiConfig : ModConfigContainer("gui") {

    val configContainerWrapperGuidelinesColor by hsvColor("config_container_wrapper_guidelines_color", HSVColor(0f, 0f, 0f).alpha(0.15f))

    val screen = addConfig(Screen)

    val popupScreen = addConfig(PopupScreen)


    object Screen : ModConfigContainer("screen") {

        var DEFAULT_BG_BLUR_RADIUS by float("default_bg_blur_radius", 5f, 0f, 20f)

        var WIDGET_TEST_OUTLINE_COLOR by color("widget_test_outline_color", Color(0))

    }

    object PopupScreen : ModConfigContainer("popup_screen") {

        var DEFAULT_BG_BLUR_RADIUS by float("default_bg_blur_radius", 0f, 0f, 20f)

    }

}

