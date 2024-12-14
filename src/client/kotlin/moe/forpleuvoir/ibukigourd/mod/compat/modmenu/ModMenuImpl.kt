package moe.forpleuvoir.ibukigourd.mod.compat.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import moe.forpleuvoir.ibukigourd.mod.gui.IbukiGourdModScreen

object ModMenuImpl : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*>? {
        return ConfigScreenFactory { IbukiGourdModScreen().apply { parentScreen = it } }
    }

}