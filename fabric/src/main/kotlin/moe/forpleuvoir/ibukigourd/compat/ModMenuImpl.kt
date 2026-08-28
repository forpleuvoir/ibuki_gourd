package moe.forpleuvoir.ibukigourd.compat

import com.terraformersmc.modmenu.api.ModMenuApi

/**
 * ModMenu 集成。
 *
 * 配置界面已随 Compose Desktop UI 一并移除，待 compose-minecraft UI 落地后
 * 在此重新注册 [com.terraformersmc.modmenu.api.ConfigScreenFactory]。
 */
object ModMenuImpl : ModMenuApi