package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.ModConfig
import moe.forpleuvoir.ibukigourd.config.item.impl.keyBind
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.gui.IbukiGourdModScreen
import moe.forpleuvoir.ibukigourd.util.mc

@ModConfig("config")
object IGConfig : ClientModConfigManager(IbukiGourd.MOD_ID, "config") {

    val openScreen by keyBind("open_screen", KeyBind(Keyboard.I, Keyboard.G) {
        IbukiGourdModScreen().open(mc.screen)
    })

    init {
        addConfig(GuiConfig)
        addConfig(Toast.Config)
    }

}