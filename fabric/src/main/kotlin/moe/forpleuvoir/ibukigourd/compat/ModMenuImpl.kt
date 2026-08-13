package moe.forpleuvoir.ibukigourd.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import moe.forpleuvoir.ibukigourd.mod.ui.IbukiGourdScreen
import moe.forpleuvoir.ibukigourd.mod.ui.UnsupportedBackendScreen
import moe.forpleuvoir.ibukigourd.platform.RenderBackend

object ModMenuImpl : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            if (RenderBackend.isVulkan) UnsupportedBackendScreen(parent)
            else IbukiGourdScreen(parentScreen = parent) ?: UnsupportedBackendScreen(parent)
        }
    }

}
