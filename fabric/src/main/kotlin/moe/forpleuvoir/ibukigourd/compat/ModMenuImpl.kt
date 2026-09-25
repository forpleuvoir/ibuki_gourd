package moe.forpleuvoir.ibukigourd.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import moe.forpleuvoir.ibukigourd.mod.ibukiGourdModScreen

/**
 * ModMenu 集成：把模组列表里的"配置"按钮接到 [ibukiGourdModScreen]。
 */
object ModMenuImpl : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        ConfigScreenFactory { parent -> ibukiGourdModScreen(parent) }
}