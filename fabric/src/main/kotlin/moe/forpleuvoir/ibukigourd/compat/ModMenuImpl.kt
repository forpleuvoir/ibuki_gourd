package moe.forpleuvoir.ibukigourd.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import moe.forpleuvoir.ibukigourd.mod.ui.IbukiGourdScreen

object ModMenuImpl : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory {
            IbukiGourdScreen(parentScreen = it)
        }
    }

}