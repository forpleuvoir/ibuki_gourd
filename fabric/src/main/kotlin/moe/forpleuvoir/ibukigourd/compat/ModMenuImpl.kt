package moe.forpleuvoir.ibukigourd.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ibukiGourdConfigScreen

/**
 * ModMenu 集成：把模组列表里的"配置"按钮接到 [ibukiGourdConfigScreen]。
 */
object ModMenuImpl : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        ConfigScreenFactory { parent -> ibukiGourdConfigScreen(parent) }
}