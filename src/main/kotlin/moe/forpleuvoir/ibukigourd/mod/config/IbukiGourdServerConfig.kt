package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ModConfig
import moe.forpleuvoir.ibukigourd.config.ServerModConfigManager
import moe.forpleuvoir.nebula.config.item.impl.ConfigString
import moe.forpleuvoir.nebula.config.persistence.ConfigManagerPersistence
import moe.forpleuvoir.nebula.config.persistence.JsonConfigManagerPersistence

@ModConfig("config")
object IbukiGourdServerConfig : ServerModConfigManager(IbukiGourd.metadata, "config"), ConfigManagerPersistence by JsonConfigManagerPersistence {

	var SERVER_LANGUAGE by ConfigString("serve_language", "zh_cn")


}