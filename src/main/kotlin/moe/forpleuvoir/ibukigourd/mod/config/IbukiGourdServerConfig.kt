package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ModConfig
import moe.forpleuvoir.ibukigourd.config.ServerModConfigManager
import moe.forpleuvoir.nebula.config.impl.HoconConfigManagerSerializer
import moe.forpleuvoir.nebula.config.item.impl.ConfigString

@ModConfig("config")
object IbukiGourdServerConfig : ServerModConfigManager(IbukiGourd.metadata, "config"), HoconConfigManagerSerializer {

	var SERVER_LANGUAGE by ConfigString("serve_language", "zh_cn")

}