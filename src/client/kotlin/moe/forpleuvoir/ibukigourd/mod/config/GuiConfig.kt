package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.ibukigourd.config.ModConfigContainer
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.config.item.impl.*
import kotlin.time.Duration.Companion.seconds

object GuiConfig : ModConfigContainer("gui") {

    val configContainerWrapperGuidelinesColor by hsvColor("config_container_wrapper_guidelines_color", HSVColor(0f, 0f, 0f).alpha(0.15f))

    val showFirstConfigInContainer by boolean("show_first_config_in_container", false)

    val autoExpandConfigContainer by boolean("auto_expand_config_container", true)

    val autoExpandConfigContainerLimit by int("auto_expand_config_container_limit", 5, 0, 30)

    val expandableConfigContainerLimit by int("expandable_config_container_limit", 15, 0, 30)

    val textLabelUpdateInterval by duration("text_label_update_interval", 0.5.seconds, 0.seconds, 2.seconds)

    init {
        addConfig(Screen)
        addConfig(PopupScreen)
    }

    object Screen : ModConfigContainer("screen") {

        var DEFAULT_BG_BLUR_RADIUS by float("default_bg_blur_radius", 5f, 0f, 20f)

        var WIDGET_TEST_OUTLINE_COLOR by color("widget_test_outline_color", Color(0))

    }

    object PopupScreen : ModConfigContainer("popup_screen") {

        var DEFAULT_BG_BLUR_RADIUS by float("default_bg_blur_radius", 0f, 0f, 20f)

    }

}

