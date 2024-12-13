package moe.forpleuvoir.ibukigourd.mod.gui

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.ModConfig

@ModConfig("ibuki_gourd")
object IGConfig : ClientModConfigManager(IbukiGourd.metadata, IbukiGourd.MOD_ID) {

    val gui = addConfig(GuiConfig)


}