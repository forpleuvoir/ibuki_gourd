package moe.forpleuvoir.ibukigourd.mod.gui

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.ModConfig
import moe.forpleuvoir.ibukigourd.config.ModConfigContainer
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

    object Screen : ModConfigContainer("screen") {

        val BG_BLUR_RADIUS by float("bg_blur_radius", 10f)

    }


}

