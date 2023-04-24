package com.forpleuvoir.ibukigourd.mod.config

import com.forpleuvoir.ibukigourd.IbukiGourd
import com.forpleuvoir.ibukigourd.config.ModConfig
import com.forpleuvoir.ibukigourd.config.ServerModConfigManager
import com.forpleuvoir.nebula.config.impl.HoconConfigManagerSerializer
import com.forpleuvoir.nebula.config.item.impl.ConfigString

@ModConfig("config")
object IbukiGourdServerConfig : ServerModConfigManager(IbukiGourd.metadata, "config"), HoconConfigManagerSerializer {

	var SERVER_LANGUAGE by ConfigString("serve_language", "zh_cn")

}