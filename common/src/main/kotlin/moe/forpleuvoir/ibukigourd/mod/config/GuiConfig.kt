package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.ibukigourd.config.ModConfigContainer
import moe.forpleuvoir.ibukigourd.config.item.impl.keyCode
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.config.item.impl.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
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
        addConfig(Scroller)
    }

    object Screen : ModConfigContainer("screen") {

//        var defaultBgBlurRadius by float("default_bg_blur_radius", 5f, 0f, 20f)

        val fadeInOffset by float("fade_in_offset", 5f, -200f, 200f)

        val fadeInDuration by duration("fade_in_duration", 150.milliseconds, Duration.ZERO, 5.seconds)

        var widgetTestOutlineColor by color("widget_test_outline_color", Color.ofARGB(0))

    }

    object PopupScreen : ModConfigContainer("popup_screen") {

//        var defaultBgBlurRadius by float("default_bg_blur_radius", 0f, 0f, 20f)

        val fadeInOffset by float("fade_in_offset", 5f, -200f, 200f)

        val fadeInDuration by duration("fade_in_duration", 150.milliseconds, Duration.ZERO, 5.seconds)

        val enableReturnHotkey by boolean("enable_return_hotkey", true)

        val returnHotkeyKeycode by keyCode("return_hotkey_keycode", Mouse.BUTTON_4)
    }

    object Scroller : ModConfigContainer("scroller") {

        val scrollMultiplier1 by float("scroll_multiplier_1", 2.5f, 0f, 20f)

        val scrollMultiplier1KeyCode by keyCode("scroll_multiplier_1_key_code", Keyboard.LEFT_SHIFT)

        val scrollMultiplier2 by float("scroll_multiplier_2", 5f, 0f, 20f)

        val scrollMultiplier2KeyCode by keyCode("scroll_multiplier_2_key_code", Keyboard.LEFT_CONTROL)

        val scrollMultiplier3 by float("scroll_multiplier_3", 10f, 0f, 20f)

        val scrollMultiplier3KeyCode by keyCode("scroll_multiplier_3_key_code", Keyboard.LEFT_ALT)

        fun applyScrollMultiplier(amount: Float): Float {
            return if (InputHandler.wasKeyPressed(scrollMultiplier1KeyCode)) {
                amount * scrollMultiplier1
            } else if (InputHandler.wasKeyPressed(scrollMultiplier2KeyCode)) {
                amount * scrollMultiplier2
            } else if (InputHandler.wasKeyPressed(scrollMultiplier3KeyCode)) {
                amount * scrollMultiplier3
            } else {
                amount
            }
        }

    }

}

