package moe.forpleuvoir.ibukigourd.mod.gui

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.ModConfig
import moe.forpleuvoir.ibukigourd.config.ModConfigContainer
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.config.item.impl.color
import moe.forpleuvoir.nebula.config.item.impl.float
import moe.forpleuvoir.nebula.config.manager.component.autoSave
import moe.forpleuvoir.nebula.config.manager.components
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ModConfig("gui")
object GuiConfig : ClientModConfigManager(IbukiGourd.metadata, "${IbukiGourd.MOD_ID}_gui") {

    init {
        components {
            autoSave(30.seconds, 5.minutes)
        }
    }

    val screen = addConfig(Screen)

    val popupScreen = addConfig(PopupScreen)

    object Screen : ModConfigContainer("screen") {

        var DEFAULT_BG_BLUR_RADIUS by float("default_bg_blur_radius", 10f, 0f, 100f)

        var WIDGET_TEST_OUTLINE_COLOR by color("widget_test_outline_color", Colors.AQUA)

    }

    object PopupScreen : ModConfigContainer("popup_screen") {

        var DEFAULT_BG_BLUR_RADIUS by float("default_bg_blur_radius", 1f, 0f, 100f)

    }


}

